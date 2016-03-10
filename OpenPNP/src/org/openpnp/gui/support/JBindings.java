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

package org.openpnp.gui.support;

import java.awt.Color;

import javax.swing.JComponent;

import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Binding.SyncFailure;
import org.jdesktop.beansbinding.BindingListener;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Converter;
import org.openpnp.model.AbstractModelObject;

/**
 * Provides convenience bindings for JComponents that add value buffering and
 * visual feedback on conversion failure. Buffered values have a read-write
 * binding with the JComponent and a read binding with the source value. The
 * returned WrappedBinding allows you to save the buffered value to the source
 * or to reset it from the source.
 * 
 * @author Jason von Nieda <jason@vonnieda.org>
 */
public class JBindings
{
	private static class JComponentBackgroundUpdater extends AbstractBindingListener
	{
		private static Color	errorColor	= new Color(0xff, 0xdd, 0xdd);
		private JComponent		component;
		private Color			oldBackground;

		public JComponentBackgroundUpdater(JComponent component)
		{
			this.component = component;
			this.oldBackground = component.getBackground();
		}

		@Override
		public void synced(Binding binding)
		{
			this.component.setBackground(this.oldBackground);
		}

		@Override
		public void syncFailed(Binding binding, SyncFailure failure)
		{
			this.component.setBackground(JComponentBackgroundUpdater.errorColor);
		}
	}

	public static class WrappedBinding<SS, SV, TS extends JComponent, TV>
	{
		private SS						source;
		private BeanProperty<SS, SV>	sourceProperty;
		private Wrapper<SV>				wrapper;
		private AutoBinding				wrappedBinding;

		public WrappedBinding(SS source, String sourcePropertyName, TS component, String targetPropertyName, Converter<SV, TV> converter, BindingListener... listeners)
		{
			this.source = source;
			this.sourceProperty = BeanProperty.create(sourcePropertyName);
			this.wrapper = new Wrapper<>(this.sourceProperty.getValue(source));
			BeanProperty<Wrapper<SV>, SV> wrapperProperty = BeanProperty.create("value");
			BeanProperty<TS, TV> targetProperty = BeanProperty.create(targetPropertyName);
			this.wrappedBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this.wrapper, wrapperProperty, component, targetProperty);
			if (converter != null)
				this.wrappedBinding.setConverter(converter);
			this.wrappedBinding.addBindingListener(new JComponentBackgroundUpdater(component));
			if (listeners != null)
				for (BindingListener listener : listeners)
					this.wrappedBinding.addBindingListener(listener);
			this.wrappedBinding.bind();
			AutoBinding<SS, SV, Wrapper<SV>, SV> binding = Bindings.createAutoBinding(UpdateStrategy.READ, source, this.sourceProperty, this.wrapper, wrapperProperty);
			binding.bind();
		}

		public final void addBindingListener(BindingListener listener)
		{
			this.wrappedBinding.addBindingListener(listener);
		}

		public Wrapper<SV> getWrapper()
		{
			return this.wrapper;
		}

		public void reset()
		{
			this.wrapper.setValue(this.sourceProperty.getValue(this.source));
		}

		public void save()
		{
			this.sourceProperty.setValue(this.source, this.wrapper.getValue());
		}
	}

	public static class Wrapper<T> extends AbstractModelObject
	{
		private T value;

		public Wrapper(T value)
		{
			this.value = value;
		}

		public T getValue()
		{
			return this.value;
		}

		public void setValue(T value)
		{
			T oldValue = this.value;
			this.value = value;
			this.firePropertyChange("value", oldValue, this.value);
		}
	}

	public static <SS, SV, TS extends JComponent, TV> WrappedBinding<SS, SV, TS, TV> bind(SS source, String sourcePropertyName, TS component, String targetPropertyName)
	{
		return new WrappedBinding<>(source, sourcePropertyName, component, targetPropertyName, null, (BindingListener[]) null);
	}

	public static <SS, SV, TS extends JComponent, TV> WrappedBinding<SS, SV, TS, TV> bind(SS source, String sourcePropertyName, TS component, String targetPropertyName, BindingListener... listeners)
	{
		return new WrappedBinding<>(source, sourcePropertyName, component, targetPropertyName, null, listeners);
	}

	public static <SS, SV, TS extends JComponent, TV> WrappedBinding<SS, SV, TS, TV> bind(SS source, String sourcePropertyName, TS component, String targetPropertyName, Converter<SV, TV> converter)
	{
		return new WrappedBinding<>(source, sourcePropertyName, component, targetPropertyName, converter, (BindingListener[]) null);
	}

	public static <SS, SV, TS extends JComponent, TV> WrappedBinding<SS, SV, TS, TV> bind(SS source, String sourcePropertyName, TS component, String targetPropertyName, Converter<SV, TV> converter,
			BindingListener... listeners)
	{
		return new WrappedBinding<>(source, sourcePropertyName, component, targetPropertyName, converter, listeners);
	}
}
