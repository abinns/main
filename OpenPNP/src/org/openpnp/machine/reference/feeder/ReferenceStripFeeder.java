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

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.ReferenceFeeder;
import org.openpnp.machine.reference.feeder.wizards.ReferenceStripFeederConfigurationWizard;
import org.openpnp.model.Length;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.model.Point;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Nozzle;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.Utils2D;
import org.openpnp.vision.FluentCv;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Feeder that indexes through a strip of cut tape. This is a specialization of
 * the tray feeder that knows specifics about tape so that vision capabilities can be added.
 */

/**
 * SMD tape standard info from http://www.liteplacer.com/setup-tape-positions-2/
 * holes 1.5mm hole pitch 4mm first part center to reference hole linear is 2mm
 * tape width is multiple of 4mm part pitch is multiple of 4mm except for 0402
 * and smaller, where it is 2mm hole to part lateral is tape width / 2 - 0.5mm
 */
public class ReferenceStripFeeder extends ReferenceFeeder
{
	public enum TapeType
	{
		WhitePaper("White Paper"), BlackPlastic("Black Plastic"), ClearPlastic("Clear Plastic");

		private String name;

		TapeType(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return this.name;
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(ReferenceStripFeeder.class);

	// Stolen from StackOverflow
	static public double getAngleFromPoint(Location firstPoint, Location secondPoint)
	{
		double angle = 0.0;
		// above 0 to 180 degrees
		if (secondPoint.getX() > firstPoint.getX())
			angle = Math.atan2(secondPoint.getX() - firstPoint.getX(), firstPoint.getY() - secondPoint.getY()) * 180 / Math.PI;
		else if (secondPoint.getX() <= firstPoint.getX())
			angle = 360 - Math.atan2(firstPoint.getX() - secondPoint.getX(), firstPoint.getY() - secondPoint.getY()) * 180 / Math.PI;
		return angle;
	}

	static public Location getPointAlongLine(Location a, Location b, Length distance)
	{
		Point vab = b.subtract(a).getXyPoint();
		double lab = a.getLinearDistanceTo(b);
		Point vu = new Point(vab.x / lab, vab.y / lab);
		vu = new Point(vu.x * distance.getValue(), vu.y * distance.getValue());
		return a.add(new Location(a.getUnits(), vu.x, vu.y, 0, 0));
	}

	@Element(required = false)
	private Location referenceHoleLocation = new Location(LengthUnit.Millimeters);

	@Element(required = false)
	private Location lastHoleLocation = new Location(LengthUnit.Millimeters);

	@Element(required = false)
	private Length partPitch = new Length(4, LengthUnit.Millimeters);

	@Element(required = false)
	private Length tapeWidth = new Length(8, LengthUnit.Millimeters);

	@Attribute(required = false)
	private TapeType tapeType = TapeType.WhitePaper;

	@Attribute(required = false)
	private boolean visionEnabled = true;

	@Attribute
	private int feedCount = 0;

	private Length holeDiameter = new Length(1.5, LengthUnit.Millimeters);

	private Length	holePitch					= new Length(4, LengthUnit.Millimeters);
	private Length	referenceHoleToPartLinear	= new Length(2, LengthUnit.Millimeters);

	private Location visionOffsets;

	private Location visionLocation;

	@Override
	public void feed(Nozzle nozzle) throws Exception
	{
		this.setFeedCount(this.getFeedCount() + 1);

		this.updateVisionOffsets(nozzle);
	}

	private Location findClosestHole(Camera camera)
	{
		List<Location> holeLocations = new ArrayList<>();
		new FluentCv().setCamera(camera).settleAndCapture().toGray().blurGaussian(this.getHoleBlurKernelSize())
				.findCirclesHough(this.getHoleDiameterMin(), this.getHoleDiameterMax(), this.getHolePitchMin()).convertCirclesToLocations(holeLocations);
		if (holeLocations.isEmpty())
			return null;
		return holeLocations.get(0);
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
		return new ReferenceStripFeederConfigurationWizard(this);
	}

	public int getFeedCount()
	{
		return this.feedCount;
	}

	public int getHoleBlurKernelSize()
	{
		return 9;
	}

	public Length getHoleDiameter()
	{
		return this.holeDiameter;
	}

	public Length getHoleDiameterMax()
	{
		return this.getHoleDiameter().multiply(1.1);
	}

	public Length getHoleDiameterMin()
	{
		return this.getHoleDiameter().multiply(0.9);
	}

	public Length getHoleDistanceMax()
	{
		return this.getTapeWidth().multiply(1.5);
	}

	public Length getHoleDistanceMin()
	{
		return this.getTapeWidth().multiply(0.25);
	}

	public Length getHoleLineDistanceMax()
	{
		return new Length(0.5, LengthUnit.Millimeters);
	}

	public Length getHolePitch()
	{
		return this.holePitch;
	}

	public Length getHolePitchMin()
	{
		return this.getHolePitch().multiply(0.9);
	}

	private Length getHoleToPartLateral()
	{
		Length tapeWidth = this.tapeWidth.convertToUnits(LengthUnit.Millimeters);
		return new Length(tapeWidth.getValue() / 2 - 0.5, LengthUnit.Millimeters);
	}

	public Location[] getIdealLineLocations()
	{
		if (this.visionLocation == null)
			return new Location[]
			{ this.referenceHoleLocation, this.lastHoleLocation };
		double d1 = this.referenceHoleLocation.getLinearLengthTo(this.lastHoleLocation).convertToUnits(LengthUnit.Millimeters).getValue();
		double d2 = this.referenceHoleLocation.getLinearLengthTo(this.visionLocation).convertToUnits(LengthUnit.Millimeters).getValue();
		if (d2 > d1)
			return new Location[]
			{ this.referenceHoleLocation, this.visionLocation };
		else
			return new Location[]
			{ this.referenceHoleLocation, this.lastHoleLocation };
	}

	public Location getLastHoleLocation()
	{
		return this.lastHoleLocation;
	}

	public Length getPartPitch()
	{
		return this.partPitch;
	}

	@Override
	public Location getPickLocation() throws Exception
	{
		// Find the location of the part linearly along the tape
		Location[] lineLocations = this.getIdealLineLocations();
		Location l = ReferenceStripFeeder.getPointAlongLine(lineLocations[0], lineLocations[1], new Length((this.feedCount - 1) * this.partPitch.getValue(), this.partPitch.getUnits()));
		// Create the offsets that are required to go from a reference hole
		// to the part in the tape
		Length x = this.getHoleToPartLateral().convertToUnits(l.getUnits());
		Length y = this.referenceHoleToPartLinear.convertToUnits(l.getUnits());
		Point p = new Point(x.getValue(), y.getValue());
		// Determine the angle that the tape is at
		double angle = ReferenceStripFeeder.getAngleFromPoint(lineLocations[0], lineLocations[1]);
		// Rotate the part offsets by the angle to move it into the right
		// coordinate space
		p = Utils2D.rotatePoint(p, angle);
		// And add the offset to the location we calculated previously
		l = l.add(new Location(l.getUnits(), p.x, p.y, 0, 0));
		// Add in the angle of the tape plus the angle of the part in the tape
		// so that the part is picked at the right angle
		l = l.derive(null, null, null, angle + this.getLocation().getRotation());
		// and if vision was performed, add the offsets
		if (this.visionEnabled && this.visionOffsets != null)
			l = l.add(this.visionOffsets);
		return l;
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

	public Location getReferenceHoleLocation()
	{
		return this.referenceHoleLocation;
	}

	public Length getReferenceHoleToPartLinear()
	{
		return this.referenceHoleToPartLinear;
	}

	public TapeType getTapeType()
	{
		return this.tapeType;
	}

	public Length getTapeWidth()
	{
		return this.tapeWidth;
	}

	public boolean isVisionEnabled()
	{
		return this.visionEnabled;
	}

	public void setFeedCount(int feedCount)
	{
		int oldValue = this.feedCount;
		this.feedCount = feedCount;
		this.visionOffsets = null;
		this.firePropertyChange("feedCount", oldValue, feedCount);
	}

	public void setHoleDiameter(Length holeDiameter)
	{
		this.holeDiameter = holeDiameter;
	}

	public void setHolePitch(Length holePitch)
	{
		this.holePitch = holePitch;
	}

	public void setLastHoleLocation(Location lastHoleLocation)
	{
		this.lastHoleLocation = lastHoleLocation;
		this.visionLocation = null;
	}

	public void setPartPitch(Length partPitch)
	{
		this.partPitch = partPitch;
	}

	public void setReferenceHoleLocation(Location referenceHoleLocation)
	{
		this.referenceHoleLocation = referenceHoleLocation;
		this.visionLocation = null;
	}

	public void setReferenceHoleToPartLinear(Length referenceHoleToPartLinear)
	{
		this.referenceHoleToPartLinear = referenceHoleToPartLinear;
	}

	public void setTapeType(TapeType tapeType)
	{
		this.tapeType = tapeType;
	}

	public void setTapeWidth(Length tapeWidth)
	{
		this.tapeWidth = tapeWidth;
	}

	public void setVisionEnabled(boolean visionEnabled)
	{
		this.visionEnabled = visionEnabled;
	}

	@Override
	public String toString()
	{
		return this.getName();
	}

	private void updateVisionOffsets(Nozzle nozzle) throws Exception
	{
		if (!this.visionEnabled)
			return;
		// go to where we expect to find the next reference hole
		Camera camera = nozzle.getHead().getDefaultCamera();
		Location expectedLocation = null;
		Location[] lineLocations = this.getIdealLineLocations();

		if (this.partPitch.convertToUnits(LengthUnit.Millimeters).getValue() < 4)
			// For tapes with a part pitch < 4 we need to check each hole
			// twice since there are two parts per reference hole.
			// Note the use of holePitch here and partPitch in the
			// alternate case below.
			expectedLocation = ReferenceStripFeeder.getPointAlongLine(lineLocations[0], lineLocations[1], this.holePitch.multiply((this.feedCount - 1) / 2));
		else
			// For tapes with a part pitch >= 4 there is always a reference
			// hole 2mm from a part so we just multiply by the part pitch
			// skipping over holes that are not reference holes.
			expectedLocation = ReferenceStripFeeder.getPointAlongLine(lineLocations[0], lineLocations[1], this.partPitch.multiply(this.feedCount - 1));
		MovableUtils.moveToLocationAtSafeZ(camera, expectedLocation, 1.0);
		// and look for the hole
		Location actualLocation = this.findClosestHole(camera);
		if (actualLocation == null)
			throw new Exception("Feeder " + this.getName() + ": Unable to locate reference hole. End of strip?");
		// make sure it's not too far away
		Length distance = actualLocation.getLinearLengthTo(expectedLocation).convertToUnits(LengthUnit.Millimeters);
		if (distance.getValue() > 2)
			throw new Exception("Feeder " + this.getName() + ": Unable to locate reference hole. End of strip?");
		this.visionOffsets = actualLocation.subtract(expectedLocation).derive(null, null, 0d, 0d);
		this.visionLocation = actualLocation;
	}
}

// this code left here in case we want to use it in the future. it is for
// calculating how many parts there are based on the first and last reference
// hole.
//// figure out how many parts there should be by taking the delta
//// between the two holes and dividing it by part pitch
// double holeToHoleDistance =
// lastHoleLocation.getLinearDistanceTo(referenceHoleLocation);
//// take the ceil of the distance to account for any minor offset from
//// center of the hole
// holeToHoleDistance = Math.ceil(holeToHoleDistance);
// double partPitch =
// this.partPitch.convertToUnits(lastHoleLocation.getUnits()).getValue();
//// And floor the part count because you can't have a partial part.
// double partCount = Math.floor(holeToHoleDistance / partPitch);
//
//// if (feedCount > partCount) {
//// throw new Exception(String.format("No more parts available in feeder %s",
// getName()));
//// }
//
