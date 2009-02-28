/*
 * Copyright 2009 Alexis Wilhelm. This program is free software: you can do
 * anything, but lay off of my blue suede shoes.
 */

package com.izforge.izpack.panels;

import javax.swing.JCheckBox;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;

/**
 * A panel that allows the end user to customize the behavior of the IPS packs'
 * unpacker.
 * 
 * @author Alexis Wilhelm
 * @since February 2009
 * @see com.izforge.izpack.installer.IPSUnpacker
 */
public class IPSPrefsPanel extends IzPanel {

	/**
	 * This allows this panel to get serialized.
	 */
	private static final long serialVersionUID = -7714579480159174801L;

	/**
	 * Asks the user if he wants to be able to update with the installer.
	 */
	public final JCheckBox keep = new JCheckBox(parent.langpack.getString("IPSPrefsPanel.keeptoupdate"));

	/**
	 * Asks the user if he wants to install Sun Update Center.
	 */
	public final JCheckBox uc = new JCheckBox(parent.langpack.getString("IPSPrefsPanel.installupdatecenter"));

	/**
	 * Draw this panel.
	 * 
	 * @param parent The frame hosting this panel.
	 * @param data The all important object describing everything we need for
	 *        the installation.
	 */
	public IPSPrefsPanel (InstallerFrame parent, InstallData data) {
		super(parent, data, new IzPanelLayout());
		add(
				createMultiLineLabel(parent.langpack.getString("IPSPrefsPanel.instructions")),
				NEXT_LINE);
		add(IzPanelLayout.createVerticalStrut(20));
		add(keep, NEXT_LINE);
		add(uc, NEXT_LINE);
		getLayoutHelper().completeLayout();
	}

	/**
	 * Save the user's choices in the installer data.
	 * 
	 * @see IzPanel#panelDeactivate()
	 */
	@Override
	public void panelDeactivate () {
		idata.keepInstallerWhenDone = keep.isSelected();
		idata.installUpdateCenter = uc.isSelected();
	}
}
