/*
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 * 
 * This file is part of OpenPnP.
 * 
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.machine.reference.feeder;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Action;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.ReferenceFeeder;
import org.openpnp.machine.reference.feeder.wizards.ReferenceDragFeederConfigurationWizard;
import org.openpnp.model.Configuration;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.model.Rectangle;
import org.openpnp.spi.Actuator;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Head;
import org.openpnp.spi.Nozzle;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.VisionProvider;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.core.Persist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vision System Description The Vision Operation is defined as moving the
 * Camera to the defined Pick Location, performing a template match against the
 * Template Image bound by the Area of Interest and then storing the offsets
 * from the Pick Location to the matched image as Vision Offsets. The feed
 * operation consists of: 1. Apply the Vision Offsets to the Feed Start Location
 * and Feed End Location. 2. Feed the tape with the modified Locations. 3.
 * Perform the Vision Operation. 4. Apply the new Vision Offsets to the Pick
 * Location and return the Pick Location for Picking. This leaves the head
 * directly above the Pick Location, which means that when the Feeder is then
 * commanded to pick the Part it only needs to move the distance of the Vision
 * Offsets and do the pick. The Vision Offsets are then used in the next feed
 * operation to be sure to hit the tape at the right position.
 */
public class ReferenceDragFeeder extends ReferenceFeeder
{
	public static class Vision
	{
		@Attribute(required = false)
		private boolean		enabled;
		@Attribute(required = false)
		private String		templateImageName;
		@Element(required = false)
		private Rectangle	areaOfInterest				= new Rectangle();
		@Element(required = false)
		private Location	templateImageTopLeft		= new Location(LengthUnit.Millimeters);
		@Element(required = false)
		private Location	templateImageBottomRight	= new Location(LengthUnit.Millimeters);

		private BufferedImage	templateImage;
		private boolean			templateImageDirty;

		public Vision()
		{
			Configuration.get().addListener(new ConfigurationListener.Adapter()
			{
				@Override
				public void configurationComplete(Configuration configuration) throws Exception
				{
					if (Vision.this.templateImageName != null)
					{
						File file = configuration.getResourceFile(Vision.this.getClass(), Vision.this.templateImageName);
						Vision.this.templateImage = ImageIO.read(file);
					}
				}
			});
		}

		public Rectangle getAreaOfInterest()
		{
			return this.areaOfInterest;
		}

		public BufferedImage getTemplateImage()
		{
			return this.templateImage;
		}

		public Location getTemplateImageBottomRight()
		{
			return this.templateImageBottomRight;
		}

		public Location getTemplateImageTopLeft()
		{
			return this.templateImageTopLeft;
		}

		public boolean isEnabled()
		{
			return this.enabled;
		}

		@SuppressWarnings("unused")
		@Persist
		private void persist() throws IOException
		{
			if (this.templateImageDirty)
			{
				File file = null;
				if (this.templateImageName != null)
					file = Configuration.get().getResourceFile(this.getClass(), this.templateImageName);
				else
				{
					file = Configuration.get().createResourceFile(this.getClass(), "tmpl_", ".png");
					this.templateImageName = file.getName();
				}
				ImageIO.write(this.templateImage, "png", file);
				this.templateImageDirty = false;
			}
		}

		public void setAreaOfInterest(Rectangle areaOfInterest)
		{
			this.areaOfInterest = areaOfInterest;
		}

		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}

		public void setTemplateImage(BufferedImage templateImage)
		{
			if (templateImage != this.templateImage)
			{
				this.templateImage = templateImage;
				this.templateImageDirty = true;
			}
		}

		public void setTemplateImageBottomRight(Location templateImageBottomRight)
		{
			this.templateImageBottomRight = templateImageBottomRight;
		}

		public void setTemplateImageTopLeft(Location templateImageTopLeft)
		{
			this.templateImageTopLeft = templateImageTopLeft;
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(ReferenceDragFeeder.class);

	private final PropertyChangeSupport	propertyChangeSupport	= new PropertyChangeSupport(this);
	@Element
	protected Location					feedStartLocation		= new Location(LengthUnit.Millimeters);
	@Element
	protected Location					feedEndLocation			= new Location(LengthUnit.Millimeters);
	@Element(required = false)
	protected double					feedSpeed				= 1.0;
	@Attribute(required = false)
	protected String					actuatorName;

	@Element(required = false)
	protected Vision vision = new Vision();

	protected Location pickLocation;

	/*
	 * visionOffset contains the difference between where the part was expected
	 * to be and where it is. Subtracting these offsets from the pickLocation
	 * produces the correct pick location. Likewise, subtracting the offsets
	 * from the feedStart and feedEndLocations should produce the correct feed
	 * locations.
	 */
	protected Location visionOffset;

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		this.propertyChangeSupport.addPropertyChangeListener(listener);
	}

	@Override
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public void feed(Nozzle nozzle) throws Exception
	{
		ReferenceDragFeeder.logger.debug("feed({})", nozzle);

		if (this.actuatorName == null)
			throw new Exception("No actuator name set.");

		Head head = nozzle.getHead();

		/*
		 * TODO: We can optimize the feed process: If we are already higher than
		 * the Z we will move to to index plus the height of the tape, we don't
		 * need to Safe Z first. There is also probably no reason to Safe Z
		 * after extracting the pin since if the tool was going to hit it would
		 * have already hit.
		 */

		Actuator actuator = head.getActuatorByName(this.actuatorName);

		if (actuator == null)
			throw new Exception(String.format("No Actuator found with name %s on feed Head %s", this.actuatorName, head.getName()));

		head.moveToSafeZ(1.0);

		if (this.vision.isEnabled())
		{
			if (this.visionOffset == null)
			{
				// This is the first feed with vision, or the offset has
				// been invalidated for some reason. We need to get an offset,
				// complete the feed operation and then get a new offset
				// for the next operation. By front loading this we make sure
				// that all future calls can go directly to the feed operation
				// and skip checking the vision first.
				ReferenceDragFeeder.logger.debug("First feed, running vision pre-flight.");

				this.visionOffset = this.getVisionOffsets(head, this.location);
			}
			ReferenceDragFeeder.logger.debug("visionOffsets " + this.visionOffset);
		}

		// Now we have visionOffsets (if we're using them) so we
		// need to create a local, offset version of the feedStartLocation,
		// feedEndLocation and pickLocation. pickLocation will be saved
		// for the pick operation while feed start and end are used
		// here and then discarded.
		Location feedStartLocation = this.feedStartLocation;
		Location feedEndLocation = this.feedEndLocation;
		this.pickLocation = this.location;
		if (this.visionOffset != null)
		{
			feedStartLocation = feedStartLocation.subtract(this.visionOffset);
			feedEndLocation = feedEndLocation.subtract(this.visionOffset);
			this.pickLocation = this.pickLocation.subtract(this.visionOffset);
		}

		// Move the actuator to the feed start location.
		actuator.moveTo(feedStartLocation.derive(null, null, Double.NaN, Double.NaN), 1.0);

		// extend the pin
		actuator.actuate(true);

		// insert the pin
		actuator.moveTo(feedStartLocation, 1.0);

		// drag the tape
		actuator.moveTo(feedEndLocation, this.feedSpeed);

		head.moveToSafeZ(1.0);

		// retract the pin
		actuator.actuate(false);

		if (this.vision.isEnabled())
		{
			this.visionOffset = this.getVisionOffsets(head, this.location);

			ReferenceDragFeeder.logger.debug("final visionOffsets " + this.visionOffset);
		}

		ReferenceDragFeeder.logger.debug("Modified pickLocation {}", this.pickLocation);
	}

	public String getActuatorName()
	{
		return this.actuatorName;
	}

	@Override
	public PropertySheetHolder[] getChildPropertySheetHolders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Wizard getConfigurationWizard()
	{
		return new ReferenceDragFeederConfigurationWizard(this);
	}

	public Location getFeedEndLocation()
	{
		return this.feedEndLocation;
	}

	public Double getFeedSpeed()
	{
		return this.feedSpeed;
	}

	public Location getFeedStartLocation()
	{
		return this.feedStartLocation;
	}

	@Override
	public Location getPickLocation() throws Exception
	{
		if (this.pickLocation == null)
			this.pickLocation = this.location;
		return this.pickLocation;
	}

	@Override
	public Action[] getPropertySheetHolderActions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPropertySheetHolderTitle()
	{
		return this.getClass().getSimpleName() + " " + this.getName();
	}

	@Override
	public PropertySheet[] getPropertySheets()
	{
		return new PropertySheet[]
		{ new PropertySheetWizardAdapter(this.getConfigurationWizard()) };
	}

	public Vision getVision()
	{
		return this.vision;
	}

	// TODO: Throw an Exception if vision fails.
	private Location getVisionOffsets(Head head, Location pickLocation) throws Exception
	{
		ReferenceDragFeeder.logger.debug("getVisionOffsets({}, {})", head.getName(), pickLocation);
		// Find the Camera to be used for vision
		// TODO: Consider caching this
		Camera camera = null;
		for (Camera c : head.getCameras())
			if (c.getVisionProvider() != null)
				camera = c;

		if (camera == null)
			throw new Exception("No vision capable camera found on head.");

		head.moveToSafeZ(1.0);

		// Position the camera over the pick location.
		ReferenceDragFeeder.logger.debug("Move camera to pick location.");
		camera.moveTo(pickLocation, 1.0);

		// Move the camera to be in focus over the pick location.
		// head.moveTo(head.getX(), head.getY(), z, head.getC());

		// Settle the camera
		Thread.sleep(camera.getSettleTimeMs());

		VisionProvider visionProvider = camera.getVisionProvider();

		Rectangle aoi = this.getVision().getAreaOfInterest();

		// Perform the template match
		ReferenceDragFeeder.logger.debug("Perform template match.");
		Point[] matchingPoints = visionProvider.locateTemplateMatches(aoi.getX(), aoi.getY(), aoi.getWidth(), aoi.getHeight(), 0, 0, this.vision.getTemplateImage());

		// Get the best match from the array
		Point match = matchingPoints[0];

		// match now contains the position, in pixels, from the top left corner
		// of the image to the top left corner of the match. We are interested
		// in
		// knowing how far from the center of the image the center of the match
		// is.
		BufferedImage image = camera.capture();
		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();
		double templateWidth = this.vision.getTemplateImage().getWidth();
		double templateHeight = this.vision.getTemplateImage().getHeight();
		double matchX = match.x;
		double matchY = match.y;

		ReferenceDragFeeder.logger.debug("matchX {}, matchY {}", matchX, matchY);

		// Adjust the match x and y to be at the center of the match instead of
		// the top left corner.
		matchX += templateWidth / 2;
		matchY += templateHeight / 2;

		ReferenceDragFeeder.logger.debug("centered matchX {}, matchY {}", matchX, matchY);

		// Calculate the difference between the center of the image to the
		// center of the match.
		double offsetX = imageWidth / 2 - matchX;
		double offsetY = imageHeight / 2 - matchY;

		ReferenceDragFeeder.logger.debug("offsetX {}, offsetY {}", offsetX, offsetY);

		// Invert the Y offset because images count top to bottom and the Y
		// axis of the machine counts bottom to top.
		offsetY *= -1;

		ReferenceDragFeeder.logger.debug("negated offsetX {}, offsetY {}", offsetX, offsetY);

		// And convert pixels to units
		Location unitsPerPixel = camera.getUnitsPerPixel();
		offsetX *= unitsPerPixel.getX();
		offsetY *= unitsPerPixel.getY();

		ReferenceDragFeeder.logger.debug("final, in camera units offsetX {}, offsetY {}", offsetX, offsetY);

		return new Location(unitsPerPixel.getUnits(), offsetX, offsetY, 0, 0);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		this.propertyChangeSupport.removePropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		this.propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}

	public void setActuatorName(String actuatorName)
	{
		String oldValue = this.actuatorName;
		this.actuatorName = actuatorName;
		this.propertyChangeSupport.firePropertyChange("actuatorName", oldValue, actuatorName);
	}

	public void setFeedEndLocation(Location feedEndLocation)
	{
		this.feedEndLocation = feedEndLocation;
	}

	public void setFeedSpeed(Double feedSpeed)
	{
		this.feedSpeed = feedSpeed;
	}

	public void setFeedStartLocation(Location feedStartLocation)
	{
		this.feedStartLocation = feedStartLocation;
	}

	public void setVision(Vision vision)
	{
		this.vision = vision;
	}

	@Override
	public String toString()
	{
		return String.format("ReferenceTapeFeeder id %s", this.id);
	}
}
