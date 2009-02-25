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
	 * The label do the talking well enough...
	 */
	public final JCheckBox keep = new JCheckBox(
			"Yes, I wanna keep this installer so that I can manually upgrade my IPS packages.");

	/**
	 * The label do the talking well enough...
	 */
	public final JCheckBox uc = new JCheckBox(
			"Yes, I wanna install the Sun Update Center and use it to automatically upgrade my IPS packages.");

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
				createMultiLineLabel("Please check the ways you want the software to get upgraded."),
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
