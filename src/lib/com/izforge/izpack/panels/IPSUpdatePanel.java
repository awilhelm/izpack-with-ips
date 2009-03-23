/*
 * Copyright 2009 Alexis Wilhelm. This program is free software: you can do
 * anything, but lay off of my blue suede shoes.
 */

package com.izforge.izpack.panels;

import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.sun.pkg.client.Fmri;
import com.sun.pkg.client.Image;
import com.sun.pkg.client.Image.FmriState;

/**
 * A panel that allows the end user to upgrade or remove the software. It should
 * look like a IPSPanel.
 * <p>
 * Don't forget to put at least one IPSInstallPanel before this panel, and at
 * least one IPSInstallPanel after. Typical sequences implying this panel are
 * [IPSPacksPanel, IPSInstallPanel, IPSUpdatePanel, IPSInstallPanel] or
 * [IPSInstallPanel, IPSUpdatePanel, IPSPacksPanel, IPSInstallPanel].
 * </p>
 * 
 * @author Alexis Wilhelm
 * @since February 2009
 * @see IPSPacksPanel
 * @see IPSInstallPanel
 */
public class IPSUpdatePanel extends IzPanel
{
	/**
	 * A table model useful for displaying the state of installed packages in an
	 * IPS image.
	 */
	private static class FmriStateTableModel extends AbstractTableModel
	{
		/**
		 * This allows this table to get serialized.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The names for the columns of this table's.
		 */
		private final String[] columnNames;

		/**
		 * A list of the packages to be displayed in this table.
		 */
		private final List<FmriState> pkgs;

		/**
		 * @param lang The localized strings.
		 * @param pkgs A list of the packages to be displayed and their current
		 *        state.
		 * @see Image#getInventory(String[], boolean)
		 */
		public FmriStateTableModel (LocaleDatabase lang, List<FmriState> pkgs)
		{
			this.pkgs = pkgs;
			columnNames = new String[] { lang.getString(this, "name"),
					lang.getString(this, "installed"),
					lang.getString(this, "upgradable") };
		}

		/**
		 * This is used for rendering strings as text, booleans as checkboxes,
		 * and so forth...
		 * 
		 * @param col The index of the queried column.
		 * @return The data type displayed in the queried column.
		 * @see AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass (int col)
		{
			return getValueAt(0, col).getClass();
		}

		/**
		 * {@inheritDoc}
		 */
		public int getColumnCount ()
		{
			return columnNames.length;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getColumnName (int col)
		{
			return columnNames[col];
		}

		/**
		 * {@inheritDoc}
		 */
		public int getRowCount ()
		{
			return pkgs.size();
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getValueAt (int row, int col)
		{
			switch (col)
			{
				case 0:
					Fmri pkg = pkgs.get(row).fmri;
					return String.format("%s (%s)", pkg.getName(),
							pkg.getAuthority());
				case 1:
					return pkgs.get(row).installed;
				case 2:
					return pkgs.get(row).upgradable;
				default:
					return null;
			}
		}

		/**
		 * Any column but the first is editable. The first column displays the
		 * package's name while the others contain checkboxes.
		 * 
		 * @param row The ordinate of the cell.
		 * @param col The abscissa of the cell.
		 * @return True when the user is allowed to edit the cell, false
		 *         otherwise.
		 * @see AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable (int row, int col)
		{
			return col > 0;
		}

		/**
		 * This allows the user to edit what's displayed in this table, that is,
		 * whether a listed package should be installed/upgraded or not.
		 * 
		 * @param value The new value for the selected cell.
		 * @param row The ordinate of the selected cell.
		 * @param col The abscissa of the selected cell.
		 * @see AbstractTableModel#setValueAt(Object, int, int)
		 */
		@Override
		public void setValueAt (Object value, int row, int col)
		{
			switch (col)
			{
				case 1:
					pkgs.get(row).installed = (Boolean) value;
					break;
				case 2:
					pkgs.get(row).upgradable = (Boolean) value;
					break;
				default:
					return;
			}
			fireTableCellUpdated(row, col);
		}
	}

	/**
	 * This allows this panel to get serialized.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Draw this panel.
	 * 
	 * @param parent The frame hosting this panel.
	 * @param data The all important object describing everything we need for
	 *        the installation.
	 */
	public IPSUpdatePanel (InstallerFrame parent, InstallData data)
	{
		super(parent, data, new IzPanelLayout());
		add(new JScrollPane(new JTable(new FmriStateTableModel(idata.langpack,
				idata.installedIPSPackages))));
	}

	/**
	 * Draw this panel, or skip to the next panel if no package is installed.
	 * This method gets called when this panel becomes active.
	 * 
	 * @see IzPanel#panelActivate()
	 */
	@Override
	public void panelActivate ()
	{
		super.panelActivate();
		if (idata.installedIPSPackages.isEmpty())
		{
			parent.skipPanel();
		}
	}

	/**
	 * Mark the selected packages either for update or for removal. The actual
	 * update/removal will be done in the next IPSUnpacker.
	 * 
	 * @see IzPanel#panelDeactivate()
	 */
	@Override
	public void panelDeactivate ()
	{
		idata.selectedIPSPackages.clear();
		idata.unwantedIPSPackages.clear();
		for (FmriState pkg: idata.installedIPSPackages)
		{
			if (!pkg.installed)
			{
				idata.unwantedIPSPackages.add(pkg.fmri);
			}
			else if (pkg.upgradable)
			{
				idata.selectedIPSPackages.add(pkg.fmri);
			}
		}
	}
}
