package org.openpnp.spi;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * Provides an interface that allows a caller to build a tree of configurable
 * items each having one or more JPanel based property sheets for configuring
 * that item. By descending through the children with
 * getChildPropertySheetProviders() a tree can be built.
 */
public interface PropertySheetHolder
{
	public interface PropertySheet
	{
		JPanel getPropertySheetPanel();

		String getPropertySheetTitle();
	}

	PropertySheetHolder[] getChildPropertySheetHolders();

	Action[] getPropertySheetHolderActions();

	Icon getPropertySheetHolderIcon();

	String getPropertySheetHolderTitle();

	PropertySheet[] getPropertySheets();
}
