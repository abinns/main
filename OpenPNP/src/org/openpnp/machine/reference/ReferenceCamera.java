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

package org.openpnp.machine.reference;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.openpnp.ConfigurationListener;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.base.AbstractCamera;
import org.openpnp.util.OpenCvUtils;
import org.openpnp.vision.LensCalibration;
import org.openpnp.vision.LensCalibration.LensModel;
import org.openpnp.vision.LensCalibration.Pattern;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.Persist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ReferenceCamera extends AbstractCamera implements ReferenceHeadMountable
{
	public interface CalibrationCallback
	{
		public void callback(int progressCurrent, int progressMax, boolean complete);
	}

	public static class LensCalibrationParams
	{
		@Attribute(required = false)
		private boolean enabled = false;

		@Element(name = "cameraMatrix", required = false)
		private double[] cameraMatrixArr = new double[9];

		@Element(name = "distortionCoefficients", required = false)
		private double[] distortionCoefficientsArr = new double[5];

		private Mat	cameraMatrix			= new Mat(3, 3, CvType.CV_64FC1);
		private Mat	distortionCoefficients	= new Mat(5, 1, CvType.CV_64FC1);

		@Commit
		private void commit()
		{
			this.cameraMatrix.put(0, 0, this.cameraMatrixArr);
			this.distortionCoefficients.put(0, 0, this.distortionCoefficientsArr);
		}

		public Mat getCameraMatrixMat()
		{
			return this.cameraMatrix;
		}

		public Mat getDistortionCoefficientsMat()
		{
			return this.distortionCoefficients;
		}

		public boolean isEnabled()
		{
			return this.enabled;
		}

		@Persist
		private void persist()
		{
			this.cameraMatrix.get(0, 0, this.cameraMatrixArr);
			this.distortionCoefficients.get(0, 0, this.distortionCoefficientsArr);
		}

		public void setCameraMatrixMat(Mat cameraMatrix)
		{
			this.cameraMatrix = cameraMatrix;
		}

		public void setDistortionCoefficientsMat(Mat distortionCoefficients)
		{
			this.distortionCoefficients = distortionCoefficients;
		}

		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}
	}

	static
	{
		nu.pattern.OpenCV.loadShared();
		System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
	}

	protected final static Logger logger = LoggerFactory.getLogger(ReferenceCamera.class);

	@Element(required = false)
	private Location headOffsets = new Location(LengthUnit.Millimeters);

	@Attribute(required = false)
	protected double rotation = 0;

	@Attribute(required = false)
	protected boolean flipX = false;

	@Attribute(required = false)
	protected boolean flipY = false;

	@Element(required = false)
	protected Length safeZ = new Length(0, LengthUnit.Millimeters);

	@Attribute(required = false)
	protected int offsetX = 0;

	@Attribute(required = false)
	protected int					offsetY		= 0;
	@Element(required = false)
	private LensCalibrationParams	calibration	= new LensCalibrationParams();
	private boolean					calibrating;

	private CalibrationCallback	calibrationCallback;
	private int					calibrationCountGoal	= 25;

	protected ReferenceMachine machine;

	protected ReferenceDriver driver;

	private LensCalibration lensCalibration;

	public ReferenceCamera()
	{
		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationLoaded(Configuration configuration) throws Exception
			{
				ReferenceCamera.this.machine = (ReferenceMachine) configuration.getMachine();
				ReferenceCamera.this.driver = ReferenceCamera.this.machine.getDriver();
			}
		});
	}

	private Mat calibrate(Mat mat)
	{
		if (!this.calibrating)
			return mat;

		int count = this.lensCalibration.getPatternFoundCount();

		Mat appliedMat = this.lensCalibration.apply(mat);
		if (appliedMat == null)
			// nothing was found in the image
			return mat;

		if (count != this.lensCalibration.getPatternFoundCount())
			// a new image was counted, so let the caller know
			if (this.lensCalibration.getPatternFoundCount() == this.calibrationCountGoal)
			{
				this.calibrationCallback.callback(this.lensCalibration.getPatternFoundCount(), this.calibrationCountGoal, true);
				this.lensCalibration.calibrate();
				this.calibration.setCameraMatrixMat(this.lensCalibration.getCameraMatrix());
				this.calibration.setDistortionCoefficientsMat(this.lensCalibration.getDistortionCoefficients());
				this.calibration.setEnabled(true);
				this.calibrating = false;
			} else
				this.calibrationCallback.callback(this.lensCalibration.getPatternFoundCount(), this.calibrationCountGoal, false);

		return appliedMat;
	}

	public void cancelCalibration()
	{
		this.calibrating = false;
	}

	@Override
	public void close() throws IOException
	{
	}

	public LensCalibrationParams getCalibration()
	{
		return this.calibration;
	}

	@Override
	public Location getHeadOffsets()
	{
		return this.headOffsets;
	}

	@Override
	public Location getLocation()
	{
		// If this is a fixed camera we just treat the head offsets as it's
		// table location.
		if (this.getHead() == null)
			return this.getHeadOffsets();
		return this.driver.getLocation(this);
	}

	public int getOffsetX()
	{
		return this.offsetX;
	}

	public int getOffsetY()
	{
		return this.offsetY;
	}

	public double getRotation()
	{
		return this.rotation;
	}

	public Length getSafeZ()
	{
		return this.safeZ;
	}

	public boolean isFlipX()
	{
		return this.flipX;
	}

	public boolean isFlipY()
	{
		return this.flipY;
	}

	@Override
	public void moveTo(Location location, double speed) throws Exception
	{
		ReferenceCamera.logger.debug("moveTo({}, {})", new Object[]
		{ location, speed });
		this.driver.moveTo(this, location, speed);
		this.machine.fireMachineHeadActivity(this.head);
	}

	@Override
	public void moveToSafeZ(double speed) throws Exception
	{
		ReferenceCamera.logger.debug("{}.moveToSafeZ({})", new Object[]
		{ this.getName(), speed });
		Length safeZ = this.safeZ.convertToUnits(this.getLocation().getUnits());
		Location l = new Location(this.getLocation().getUnits(), Double.NaN, Double.NaN, safeZ.getValue(), Double.NaN);
		this.driver.moveTo(this, l, speed);
		this.machine.fireMachineHeadActivity(this.head);
	}

	public void setFlipX(boolean flipX)
	{
		this.flipX = flipX;
	}

	public void setFlipY(boolean flipY)
	{
		this.flipY = flipY;
	}

	@Override
	public void setHeadOffsets(Location headOffsets)
	{
		this.headOffsets = headOffsets;
	}

	public void setOffsetX(int offsetX)
	{
		this.offsetX = offsetX;
	}

	public void setOffsetY(int offsetY)
	{
		this.offsetY = offsetY;
	}

	public void setRotation(double rotation)
	{
		this.rotation = rotation;
	}

	public void setSafeZ(Length safeZ)
	{
		this.safeZ = safeZ;
	}

	public void startCalibration(CalibrationCallback callback)
	{
		this.calibrationCallback = callback;
		this.calibration.setEnabled(false);
		this.lensCalibration = new LensCalibration(LensModel.Pinhole, Pattern.AsymmetricCirclesGrid, 4, 11, 15, 750);
		this.calibrating = true;
	}

	protected BufferedImage transformImage(BufferedImage image)
	{
		Mat mat = OpenCvUtils.toMat(image);

		mat = this.calibrate(mat);

		mat = this.undistort(mat);

		// apply affine transformations
		if (this.rotation != 0)
		{
			// TODO: Fix cropping of rotated image:
			// http://stackoverflow.com/questions/22041699/rotate-an-image-without-cropping-in-opencv-in-c
			Point center = new Point(mat.width() / 2D, mat.height() / 2D);
			Mat mapMatrix = Imgproc.getRotationMatrix2D(center, this.rotation, 1.0);
			Imgproc.warpAffine(mat, mat, mapMatrix, mat.size(), Imgproc.INTER_LINEAR);
			mapMatrix.release();
		}

		if (this.offsetX != 0 || this.offsetY != 0)
		{
			Mat mapMatrix = new Mat(2, 3, CvType.CV_32F)
			{
				{
					this.put(0, 0, 1, 0, ReferenceCamera.this.offsetX);
					this.put(1, 0, 0, 1, ReferenceCamera.this.offsetY);
				}
			};
			Imgproc.warpAffine(mat, mat, mapMatrix, mat.size(), Imgproc.INTER_LINEAR);
			mapMatrix.release();
		}

		if (this.flipX || this.flipY)
		{
			int flipCode;
			if (this.flipX && this.flipY)
				flipCode = -1;
			else
				flipCode = this.flipX ? 0 : 1;
			Mat dst = new Mat();
			Core.flip(mat, dst, flipCode);
			mat = dst;
		}

		image = OpenCvUtils.toBufferedImage(mat);
		mat.release();
		return image;
	}

	private Mat undistort(Mat mat)
	{
		if (!this.calibration.isEnabled())
			return mat;
		Mat dst = mat.clone();
		Imgproc.undistort(mat, dst, this.calibration.getCameraMatrixMat(), this.calibration.getDistortionCoefficientsMat());
		mat.release();
		return dst;
	}
}
