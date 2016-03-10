package org.openpnp.gui.support;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Icons
{
	public static Icon	add		= Icons.getIcon("/icons/file-add.svg");
	public static Icon	delete	= Icons.getIcon("/icons/file-remove.svg");
	public static Icon	neww	= Icons.getIcon("/icons/file-new.svg");
	public static Icon	copy	= Icons.getIcon("/icons/copy.svg");
	public static Icon	paste	= Icons.getIcon("/icons/paste.svg");

	public static Icon	captureCamera	= Icons.getIcon("/icons/capture-camera.svg");
	public static Icon	captureTool		= Icons.getIcon("/icons/capture-nozzle.svg");
	public static Icon	capturePin		= Icons.getIcon("/icons/capture-actuator.svg");

	public static Icon	centerCamera		= Icons.getIcon("/icons/position-camera.svg");
	public static Icon	centerTool			= Icons.getIcon("/icons/position-nozzle.svg");
	public static Icon	centerToolNoSafeZ	= Icons.getIcon("/icons/position-nozzle-no-safe-z.svg");
	public static Icon	centerPin			= Icons.getIcon("/icons/position-actuator.svg");

	public static Icon	start	= Icons.getIcon("/icons/control-start.svg");
	public static Icon	pause	= Icons.getIcon("/icons/control-pause.svg");
	public static Icon	step	= Icons.getIcon("/icons/control-next.svg");
	public static Icon	stop	= Icons.getIcon("/icons/control-stop.svg");

	public static Icon	load	= Icons.getIcon("/icons/nozzletip-load.svg");
	public static Icon	unload	= Icons.getIcon("/icons/nozzletip-unload.svg");

	public static Icon	twoPointLocate	= Icons.getIcon("/icons/board-two-placement-locate.svg");
	public static Icon	fiducialCheck	= Icons.getIcon("/icons/board-fiducial-locate.svg");

	public static Icon	feed		= Icons.getIcon("/icons/feeder-feed.svg");
	public static Icon	showPart	= Icons.getIcon("/icons/feeder-show-part-outline.svg");
	public static Icon	editFeeder	= Icons.getIcon("/icons/feeder-edit.svg");

	public static Icon	arrowUp					= Icons.getIcon("/icons/ic_arrow_upward_black_18px.svg");
	public static Icon	arrowDown				= Icons.getIcon("/icons/ic_arrow_downward_black_18px.svg");
	public static Icon	arrowLeft				= Icons.getIcon("/icons/ic_arrow_back_black_18px.svg");
	public static Icon	arrowRight				= Icons.getIcon("/icons/ic_arrow_forward_black_18px.svg");
	public static Icon	home					= Icons.getIcon("/icons/ic_home_black_18px.svg");
	public static Icon	refresh					= Icons.getIcon("/icons/ic_home_black_18px.svg");
	public static Icon	rotateClockwise			= Icons.getIcon("/icons/ic_rotate_clockwise_black_18px.svg");
	public static Icon	rotateCounterclockwise	= Icons.getIcon("/icons/ic_rotate_counterclockwise_black_18px.svg");
	public static Icon	zero					= Icons.getIcon("/icons/ic_exposure_zero_black_18px.svg");

	public static Icon getIcon(String resourceName)
	{
		return Icons.getIcon(resourceName, 24, 24);
	}

	public static Icon getIcon(String resourceName, int width, int height)
	{
		if (resourceName.endsWith(".svg"))
			return new SvgIcon(Icons.class.getResource(resourceName), width, height);
		else
			return new ImageIcon(Icons.class.getResource(resourceName));
	}
}
