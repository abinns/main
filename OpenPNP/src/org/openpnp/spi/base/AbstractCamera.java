package org.openpnp.spi.base;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;

import org.openpnp.CameraListener;
import org.openpnp.ConfigurationListener;
import org.openpnp.gui.support.Icons;
import org.openpnp.model.Configuration;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Head;
import org.openpnp.spi.VisionProvider;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public abstract class AbstractCamera implements Camera
{
	protected class ListenerEntry
	{
		public CameraListener	listener;
		public int				maximumFps;
		public long				lastFrameSent;

		public ListenerEntry(CameraListener listener, int maximumFps)
		{
			this.listener = listener;
			this.maximumFps = maximumFps;
		}

		@Override
		public boolean equals(Object obj)
		{
			return obj.equals(this.listener);
		}

		@Override
		public int hashCode()
		{
			return this.listener.hashCode();
		}
	}

	@Attribute
	protected String id;

	@Attribute(required = false)
	protected String name;

	@Attribute
	protected Looking looking = Looking.Down;

	@Element
	protected Location unitsPerPixel = new Location(LengthUnit.Millimeters);

	@Element(required = false)
	protected VisionProvider visionProvider;

	@Attribute(required = false)
	protected long settleTimeMs = 250;

	protected Set<ListenerEntry> listeners = Collections.synchronizedSet(new HashSet<>());

	protected Head head;

	protected Integer width;

	protected Integer height;

	public AbstractCamera()
	{
		this.id = Configuration.createId();
		this.name = this.getClass().getSimpleName();
		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationLoaded(Configuration configuration) throws Exception
			{
				if (AbstractCamera.this.visionProvider != null)
					AbstractCamera.this.visionProvider.setCamera(AbstractCamera.this);
			}
		});
	}

	protected void broadcastCapture(BufferedImage img)
	{
		for (ListenerEntry listener : new ArrayList<>(this.listeners))
			if (listener.lastFrameSent < System.currentTimeMillis() - 1000 / listener.maximumFps)
			{
				listener.listener.frameReceived(img);
				listener.lastFrameSent = System.currentTimeMillis();
			}
	}

	@Override
	public Head getHead()
	{
		return this.head;
	}

	@Override
	public int getHeight()
	{
		if (this.width == null)
		{
			BufferedImage image = this.capture();
			this.width = image.getWidth();
			this.height = image.getHeight();
		}
		return this.height;
	}

	@Override
	public String getId()
	{
		return this.id;
	}

	@Override
	public Looking getLooking()
	{
		return this.looking;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public Icon getPropertySheetHolderIcon()
	{
		return Icons.captureCamera;
	}

	@Override
	public long getSettleTimeMs()
	{
		return this.settleTimeMs;
	}

	@Override
	public Location getUnitsPerPixel()
	{
		return this.unitsPerPixel;
	}

	@Override
	public VisionProvider getVisionProvider()
	{
		return this.visionProvider;
	}

	@Override
	public int getWidth()
	{
		if (this.width == null)
		{
			BufferedImage image = this.capture();
			this.width = image.getWidth();
			this.height = image.getHeight();
		}
		return this.width;
	}

	@Override
	public void setHead(Head head)
	{
		this.head = head;
	}

	@Override
	public void setLooking(Looking looking)
	{
		this.looking = looking;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public void setSettleTimeMs(long settleTimeMs)
	{
		this.settleTimeMs = settleTimeMs;
	}

	@Override
	public BufferedImage settleAndCapture()
	{
		try
		{
			Thread.sleep(this.getSettleTimeMs());
		} catch (Exception e)
		{

		}
		return this.capture();
	}

	@Override
	public void setUnitsPerPixel(Location unitsPerPixel)
	{
		this.unitsPerPixel = unitsPerPixel;
	}

	@Override
	public void setVisionProvider(VisionProvider visionProvider)
	{
		this.visionProvider = visionProvider;
		visionProvider.setCamera(this);
	}

	@Override
	public void startContinuousCapture(CameraListener listener, int maximumFps)
	{
		this.listeners.add(new ListenerEntry(listener, maximumFps));
	}

	@Override
	public void stopContinuousCapture(CameraListener listener)
	{
		this.listeners.remove(new ListenerEntry(listener, 0));
	}
}
