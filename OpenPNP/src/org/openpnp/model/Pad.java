package org.openpnp.model;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import org.simpleframework.xml.Attribute;

public abstract class Pad extends AbstractModelObject
{
	public static class Circle extends Pad
	{
		@Attribute
		private double radius;

		@Override
		public Circle convertToUnits(LengthUnit units)
		{
			Circle that = new Circle();
			that.setUnits(units);
			that.setRadius(Length.convertToUnits(this.radius, this.units, units));
			return that;
		}

		public double getRadius()
		{
			return this.radius;
		}

		@Override
		public Shape getShape()
		{
			return new Ellipse2D.Double(-this.radius, -this.radius, this.radius * 2, this.radius * 2);
		}

		public void setRadius(double radius)
		{
			double oldValue = this.radius;
			this.radius = radius;
			this.firePropertyChange("radius", oldValue, radius);
		}
	}

	public static class Ellipse extends Pad
	{
		@Attribute
		private double width;

		@Attribute
		private double height;

		@Override
		public Ellipse convertToUnits(LengthUnit units)
		{
			Ellipse that = new Ellipse();
			that.setUnits(units);
			that.setHeight(Length.convertToUnits(this.height, this.units, units));
			that.setWidth(Length.convertToUnits(this.width, this.units, units));
			return that;
		}

		public double getHeight()
		{
			return this.height;
		}

		@Override
		public Shape getShape()
		{
			return new Ellipse2D.Double(-this.width / 2, -this.height / 2, this.width, this.height);
		}

		public double getWidth()
		{
			return this.width;
		}

		public void setHeight(double height)
		{
			double oldValue = this.height;
			this.height = height;
			this.firePropertyChange("height", oldValue, height);
		}

		public void setWidth(double width)
		{
			double oldValue = this.width;
			this.width = width;
			this.firePropertyChange("width", oldValue, width);
		}
	}

	public static class RoundRectangle extends Pad
	{
		@Attribute
		private double width;

		@Attribute
		private double height;

		@Attribute(required = false)
		private double roundness;

		@Override
		public RoundRectangle convertToUnits(LengthUnit units)
		{
			RoundRectangle that = new RoundRectangle();
			that.setUnits(units);
			that.setHeight(Length.convertToUnits(this.height, this.units, units));
			that.setWidth(Length.convertToUnits(this.width, this.units, units));
			// don't convert roundness because it's a percentage
			return that;
		}

		public double getHeight()
		{
			return this.height;
		}

		public double getRoundness()
		{
			return this.roundness;
		}

		@Override
		public Shape getShape()
		{
			return new RoundRectangle2D.Double(-this.width / 2, -this.height / 2, this.width, this.height, this.width / 1.0 * this.roundness, this.height / 1.0 * this.roundness);
		}

		public double getWidth()
		{
			return this.width;
		}

		public void setHeight(double height)
		{
			double oldValue = this.height;
			this.height = height;
			this.firePropertyChange("height", oldValue, height);
		}

		public void setRoundness(double roundness)
		{
			double oldValue = this.roundness;
			this.roundness = roundness;
			this.firePropertyChange("roundness", oldValue, roundness);
		}

		public void setWidth(double width)
		{
			double oldValue = this.width;
			this.width = width;
			this.firePropertyChange("width", oldValue, width);
		}
	}

	@Attribute
	protected LengthUnit units = LengthUnit.Millimeters;

	public abstract Pad convertToUnits(LengthUnit units);

	// TODO: Line doesn't really work as a shape, so I am removing it
	// until we really have a need for it at which point it can be revisited.
	// public static class Line extends Pad {
	// @Attribute
	// private double x1;
	//
	// @Attribute
	// private double y1;
	//
	// @Attribute
	// private double x2;
	//
	// @Attribute
	// private double y2;
	//
	// public double getX1() {
	// return x1;
	// }
	//
	// public void setX1(double x1) {
	// double oldValue = this.x1;
	// this.x1 = x1;
	// firePropertyChange("x1", oldValue, x1);
	// }
	//
	// public double getY1() {
	// return y1;
	// }
	//
	// public void setY1(double y1) {
	// double oldValue = this.y1;
	// this.y1 = y1;
	// firePropertyChange("y1", oldValue, y1);
	// }
	//
	// public double getX2() {
	// return x2;
	// }
	//
	// public void setX2(double x2) {
	// double oldValue = this.x2;
	// this.x2 = x2;
	// firePropertyChange("x2", oldValue, x2);
	// }
	//
	// public double getY2() {
	// return y2;
	// }
	//
	// public void setY2(double y2) {
	// double oldValue = this.y2;
	// this.y2 = y2;
	// firePropertyChange("y2", oldValue, y2);
	// }
	//
	// public Shape getShape() {
	// return new Line2D.Double(
	// x1,
	// y1,
	// x2,
	// y2);
	// }
	// }

	public abstract Shape getShape();

	public LengthUnit getUnits()
	{
		return this.units;
	}

	public void setUnits(LengthUnit units)
	{
		Object oldValue = units;
		this.units = units;
		this.firePropertyChange("units", oldValue, units);
	}
}
