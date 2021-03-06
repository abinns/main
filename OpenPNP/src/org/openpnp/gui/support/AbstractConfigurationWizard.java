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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Converter;
import org.openpnp.gui.support.JBindings.WrappedBinding;
import org.openpnp.model.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConfigurationWizard extends JPanel implements Wizard
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	protected final Logger		logger				= LoggerFactory.getLogger(this.getClass());
	protected WizardContainer	wizardContainer;
	private JButton				btnApply;
	private JButton				btnReset;
	protected JPanel			contentPanel;
	private JScrollPane			scrollPane;

	private List<WrappedBinding>		wrappedBindings	= new ArrayList<>();
	private ApplyResetBindingListener	listener;

	protected Action applyAction = new AbstractAction("Apply")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			AbstractConfigurationWizard.this.saveToModel();
			AbstractConfigurationWizard.this.wizardContainer.wizardCompleted(AbstractConfigurationWizard.this);
		}
	};

	protected Action resetAction = new AbstractAction("Reset")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			AbstractConfigurationWizard.this.loadFromModel();
		}
	};

	public AbstractConfigurationWizard()
	{
		this.setLayout(new BorderLayout());

		this.contentPanel = new JPanel();
		this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.Y_AXIS));

		this.scrollPane = new JScrollPane(this.contentPanel);

		this.scrollPane.setBorder(null);
		this.add(this.scrollPane, BorderLayout.CENTER);

		JPanel panelActions = new JPanel();
		panelActions.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.add(panelActions, BorderLayout.SOUTH);

		this.btnReset = new JButton(this.resetAction);
		panelActions.add(this.btnReset);

		this.btnApply = new JButton(this.applyAction);
		panelActions.add(this.btnApply);
	}

	public WrappedBinding addWrappedBinding(Object source, String sourceProperty, JComponent component, String componentProperty)
	{
		return this.addWrappedBinding(JBindings.bind(source, sourceProperty, component, componentProperty));
	}

	public WrappedBinding addWrappedBinding(Object source, String sourceProperty, JComponent component, String componentProperty, Converter converter)
	{
		return this.addWrappedBinding(JBindings.bind(source, sourceProperty, component, componentProperty, converter));
	}

	public WrappedBinding addWrappedBinding(WrappedBinding binding)
	{
		binding.addBindingListener(this.listener);
		this.wrappedBindings.add(binding);
		return binding;
	}

	public AutoBinding bind(UpdateStrategy updateStrategy, Object source, String sourceProperty, Object target, String targetProperty)
	{
		AutoBinding binding = Bindings.createAutoBinding(updateStrategy, source, BeanProperty.create(sourceProperty), target, BeanProperty.create(targetProperty));
		binding.bind();
		return binding;
	}

	public AutoBinding bind(UpdateStrategy updateStrategy, Object source, String sourceProperty, Object target, String targetProperty, Converter converter)
	{
		AutoBinding binding = Bindings.createAutoBinding(updateStrategy, source, BeanProperty.create(sourceProperty), target, BeanProperty.create(targetProperty));
		if (converter != null)
			binding.setConverter(converter);
		binding.bind();
		return binding;
	}

	public abstract void createBindings();

	@Override
	public String getWizardName()
	{
		return null;
	}

	@Override
	public JPanel getWizardPanel()
	{
		return this;
	}

	/**
	 * When overriding this method you should call super.loadFromModel() AFTER
	 * doing any work that you need to do, not before.
	 */
	protected void loadFromModel()
	{
		for (WrappedBinding wrappedBinding : this.wrappedBindings)
			wrappedBinding.reset();
		this.applyAction.setEnabled(false);
		this.resetAction.setEnabled(false);
	}

	/**
	 * This method should be called when the caller wishes to notify the user
	 * that there has been a change to the state of the wizard. This is done
	 * automatically for wrapped bindings but this method is provided for
	 * operations that do not use wrapped bindings.
	 */
	protected void notifyChange()
	{
		this.applyAction.setEnabled(true);
		this.resetAction.setEnabled(true);
	}

	/**
	 * When overriding this method you should call super.loadFromModel() AFTER
	 * doing any work that you need to do, not before.
	 */
	protected void saveToModel()
	{
		try
		{
			this.validateInput();
		} catch (Exception e)
		{
			MessageBoxes.errorBox(this.getTopLevelAncestor(), "Validation Error", e.getMessage());
		}
		for (WrappedBinding wrappedBinding : this.wrappedBindings)
			wrappedBinding.save();
		this.applyAction.setEnabled(false);
		this.resetAction.setEnabled(false);
	}

	@Override
	public void setWizardContainer(WizardContainer wizardContainer)
	{
		this.wizardContainer = wizardContainer;
		this.scrollPane.getVerticalScrollBar().setUnitIncrement(Configuration.get().getVerticalScrollUnitIncrement());
		this.listener = new ApplyResetBindingListener(this.applyAction, this.resetAction);
		this.createBindings();
		this.loadFromModel();
	}

	public void validateInput() throws Exception
	{

	}
}
