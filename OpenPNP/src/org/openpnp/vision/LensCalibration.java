package org.openpnp.vision;

import java.util.ArrayList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs OpenCV based lens calibration based on the techniques described in:
 * http://opencv-java-tutorials.readthedocs.org/en/latest/09-camera-calibration.
 * html http://docs.opencv.org/2.4/doc/tutorials/calib3d/camera_calibration/
 * camera_calibration.html
 * https://github.com/Itseez/opencv/blob/master/samples/cpp/tutorial_code/
 * calib3d/camera_calibration /camera_calibration.cpp FishEye model code is
 * included but unfinished. This code cannot be finished until we are using
 * OpenCV 3.
 */
public class LensCalibration
{
	public enum LensModel
	{
		Pinhole, Fisheye
	}

	public enum Pattern
	{
		Chessboard, CirclesGrid, AsymmetricCirclesGrid
	}

	protected final static Logger logger = LoggerFactory.getLogger(LensCalibration.class);;

	static
	{
		nu.pattern.OpenCV.loadShared();
		System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
	}

	final private LensModel		lensModel;
	final private Pattern		pattern;
	final private Size			patternSize;
	final private double		objectSize;
	final private MatOfPoint3f	objectPoints;
	final private long			applyDelayMs;

	private List<Mat>	imagePointsList		= new ArrayList<>();
	private List<Mat>	objectPointsList	= new ArrayList<>();
	private Size		imageSize;
	private Mat			cameraMatrix;
	private Mat			distortionCoefficients;
	private long		lastApplyMs;

	public LensCalibration(LensModel lensModel, Pattern pattern, int patternWidth, int patternHeight, double objectSize, long applyDelayMs)
	{
		if (lensModel == LensModel.Fisheye)
			throw new Error(lensModel + " LensModel not yet supported. OpenCV 3+ needed.");
		this.lensModel = lensModel;
		this.pattern = pattern;
		this.patternSize = new Size(patternWidth, patternHeight);
		this.objectSize = objectSize;
		this.applyDelayMs = applyDelayMs;
		// We only need to calculate this once, so we do it ahead of time
		// and then add it to the list with each processed image.
		this.objectPoints = this.calculateObjectPoints();
	}

	public Mat apply(Mat mat)
	{
		if (this.imageSize == null)
			this.imageSize = mat.size();

		MatOfPoint2f imagePoints = this.findImagePoints(mat);
		if (imagePoints == null)
			return null;

		Calib3d.drawChessboardCorners(mat, this.patternSize, imagePoints, true);

		if (System.currentTimeMillis() - this.lastApplyMs > this.applyDelayMs)
		{
			this.objectPointsList.add(this.objectPoints);
			this.imagePointsList.add(imagePoints);
			this.lastApplyMs = System.currentTimeMillis();
		}

		return mat;
	}

	private MatOfPoint3f calculateObjectPoints()
	{
		MatOfPoint3f obj = new MatOfPoint3f();

		switch (this.pattern)
		{
			case Chessboard:
			case CirclesGrid:
				for (int i = 0; i < this.patternSize.height; ++i)
					for (int j = 0; j < this.patternSize.width; ++j)
						obj.push_back(new MatOfPoint3f(new Point3(j * this.objectSize, i * this.objectSize, 0)));
				break;
			case AsymmetricCirclesGrid:
				for (int i = 0; i < this.patternSize.height; i++)
					for (int j = 0; j < this.patternSize.width; j++)
						obj.push_back(new MatOfPoint3f(new Point3((2 * j + i % 2) * this.objectSize, i * this.objectSize, 0)));
				break;
		}
		return obj;
	}

	public boolean calibrate()
	{
		Mat cameraMatrix;
		Mat distortionCoefficients;

		cameraMatrix = Mat.eye(3, 3, CvType.CV_64F);

		if (this.lensModel == LensModel.Fisheye)
			distortionCoefficients = Mat.zeros(4, 1, CvType.CV_64F);
		else
			distortionCoefficients = Mat.zeros(8, 1, CvType.CV_64F);

		List<Mat> rvecs = new ArrayList<>();
		List<Mat> tvecs = new ArrayList<>();

		double rms;

		if (this.lensModel == LensModel.Fisheye)
			// TODO:
			throw new Error(this.lensModel + " LensModel not yet supported. OpenCV 3+ needed.");
		// Mat _rvecs, _tvecs;
		// rms = fisheye::calibrate(objectPoints, imagePoints, imageSize,
		// cameraMatrix,
		// distCoeffs, _rvecs,
		// _tvecs, s.flag);
		//
		// rvecs.reserve(_rvecs.rows);
		// tvecs.reserve(_tvecs.rows);
		// for(int i = 0; i < int(objectPoints.size()); i++){
		// rvecs.push_back(_rvecs.row(i));
		// tvecs.push_back(_tvecs.row(i));
		// }
		else
			rms = Calib3d.calibrateCamera(this.objectPointsList, this.imagePointsList, this.imageSize, cameraMatrix, distortionCoefficients, rvecs, tvecs);

		boolean ok = Core.checkRange(cameraMatrix) && Core.checkRange(distortionCoefficients);

		LensCalibration.logger.info("calibrate() ok {}, rms {}", ok, rms);

		if (ok)
		{
			this.cameraMatrix = cameraMatrix;
			this.distortionCoefficients = distortionCoefficients;
		}

		return ok;
	}

	private MatOfPoint2f findImagePoints(Mat mat)
	{
		MatOfPoint2f imagePoints = new MatOfPoint2f();
		boolean found = false;
		switch (this.pattern)
		{
			case Chessboard:
				int chessBoardFlags = Calib3d.CALIB_CB_ADAPTIVE_THRESH | Calib3d.CALIB_CB_NORMALIZE_IMAGE;
				if (this.lensModel != LensModel.Fisheye)
					// fast check erroneously fails with high distortions like
					// fisheye
					chessBoardFlags |= Calib3d.CALIB_CB_FAST_CHECK;
				found = Calib3d.findChessboardCorners(mat, this.patternSize, imagePoints, chessBoardFlags);
				if (found)
				{
					// improve the found corners' coordinate accuracy for
					// chessboard
					Mat matGray = new Mat();
					Imgproc.cvtColor(mat, matGray, Imgproc.COLOR_BGR2GRAY);
					Imgproc.cornerSubPix(matGray, imagePoints, new Size(11, 11), new Size(-1, -1), new TermCriteria(TermCriteria.EPS + TermCriteria.COUNT, 30, 0.1));
				}
				break;
			case CirclesGrid:
				found = Calib3d.findCirclesGridDefault(mat, this.patternSize, imagePoints);
				break;
			case AsymmetricCirclesGrid:
				found = Calib3d.findCirclesGridDefault(mat, this.patternSize, imagePoints, Calib3d.CALIB_CB_ASYMMETRIC_GRID);
				break;
		}
		return found ? imagePoints : null;
	}

	public Mat getCameraMatrix()
	{
		return this.cameraMatrix;
	}

	public Mat getDistortionCoefficients()
	{
		return this.distortionCoefficients;
	}

	public int getPatternFoundCount()
	{
		return this.imagePointsList.size();
	}

	public boolean isCalibrated()
	{
		return this.cameraMatrix != null && this.distortionCoefficients != null;
	}
}
