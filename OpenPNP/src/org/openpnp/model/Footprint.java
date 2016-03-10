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

package org.openpnp.model;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

/**
 * A Footprint is a group of SMD pads along with length unit information.
 * Footprints can be rendered to a Shape for easy display using 2D primitives.
 */
public class Footprint
{
	public static class Pad
	{
		@Attribute
		private String name;

		@Attribute
		private double x;

		@Attribute
		private double y;

		@Attribute
		private double width;

		@Attribute
		private double height;

		@Attribute(required = false)
		private double rotation = 0;

		/**
		 * Roundness as a percentage of the width and height. 0 is square, 100
		 * is round.
		 */
		@Attribute(required = false)
		private double roundness = 0;

		public double getHeight()
		{
			return this.height;
		}

		public String getName()
		{
			return this.name;
		}

		public double getRotation()
		{
			return this.rotation;
		}

		public double getRoundness()
		{
			return this.roundness;
		}

		public Shape getShape()
		{
			Shape shape = new RoundRectangle2D.Double(-this.width / 2, -this.height / 2, this.width, this.height, this.width / 100.0 * this.roundness, this.height / 100.0 * this.roundness);
			AffineTransform tx = new AffineTransform();
			tx.translate(this.x, -this.y);
			tx.rotate(Math.toRadians(-this.rotation));
			return tx.createTransformedShape(shape);
		}

		public double getWidth()
		{
			return this.width;
		}

		public double getX()
		{
			return this.x;
		}

		public double getY()
		{
			return this.y;
		}

		public void setHeight(double height)
		{
			this.height = height;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public void setRotation(double rotation)
		{
			this.rotation = rotation;
		}

		public void setRoundness(double roundness)
		{
			this.roundness = roundness;
		}

		public void setWidth(double width)
		{
			this.width = width;
		}

		public void setX(double x)
		{
			this.x = x;
		}

		public void setY(double y)
		{
			this.y = y;
		}
	}

	@Attribute
	private LengthUnit units = LengthUnit.Millimeters;

	@ElementList(inline = true, required = false)
	private ArrayList<Pad> pads = new ArrayList<>();

	@Attribute(required = false)
	private double bodyWidth;

	@Attribute(required = false)
	private double bodyHeight;

	public void addPad(Pad pad)
	{
		this.pads.add(pad);
	}

	public double getBodyHeight()
	{
		return this.bodyHeight;
	}

	public double getBodyWidth()
	{
		return this.bodyWidth;
	}

	public List<Pad> getPads()
	{
		return this.pads;
	}

	public Shape getShape()
	{
		Path2D.Double shape = new Path2D.Double();
		for (Pad pad : this.pads)
			shape.append(pad.getShape(), false);

		Pad body = new Pad();
		body.setWidth(this.bodyWidth);
		body.setHeight(this.bodyHeight);
		shape.append(body.getShape(), false);

		return shape;
	}

	public LengthUnit getUnits()
	{
		return this.units;
	}

	public void removePad(Pad pad)
	{
		this.pads.remove(pad);
	}

	public void setBodyHeight(double bodyHeight)
	{
		this.bodyHeight = bodyHeight;
	}

	public void setBodyWidth(double bodyWidth)
	{
		this.bodyWidth = bodyWidth;
	}

	public void setUnits(LengthUnit units)
	{
		this.units = units;
	}
}
