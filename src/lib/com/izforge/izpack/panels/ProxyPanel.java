/*
 * Copyright 2009 Alexis Wilhelm. This program is free software: you can do
 * anything, but lay off of my blue suede shoes.
 */

package com.izforge.izpack.panels;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.net.InetSocketAddress;
import java.net.Proxy;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;

/**
 * A panel that allows the end user to use a proxy when downloading files or
 * packages.
 * 
 * @author Alexis Wilhelm
 * @since February 2009
 */
public class ProxyPanel extends IzPanel {

	/**
	 * This allows this panel to get serialized.
	 */
	private static final long serialVersionUID = -8352080000808343274L;

	/**
	 * The tinyest dimension a component can have.
	 */
	private static final Dimension MIN_DIM = new Dimension(0, 0);

	/**
	 * The hugest dimension one can dream of.
	 */
	private static final Dimension MAX_DIM = new Dimension(Integer.MAX_VALUE,
			Integer.MAX_VALUE);

	/**
	 * The field where the user puts the host name for their proxy.
	 */
	public final JTextField host = new JTextField();

	/**
	 * The field where the user puts the port number for their proxy.
	 */
	public final JSpinner port = new JSpinner(new SpinnerNumberModel(0, 0,
			0xFFFF, 1));

	/**
	 * The box the user uses to choose their proxy's type.
	 */
	public final JComboBox type = new JComboBox(new Proxy.Type[] {
			Proxy.Type.DIRECT, Proxy.Type.HTTP, Proxy.Type.SOCKS });

	/**
	 * Draw this panel.
	 * 
	 * @param parent The frame hosting this panel.
	 * @param data The all important object describing everything we need for
	 *        the installation.
	 */
	public ProxyPanel (InstallerFrame parent, InstallData data) {
		super(parent, data);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new Box.Filler(MIN_DIM, MAX_DIM, MAX_DIM));
		add(
				createMultiLineLabel("Put the infos of your proxy's in there (let this blank if you ain't none):"),
				NEXT_LINE);
		add(IzPanelLayout.createVerticalStrut(20));
		JPanel grid = new JPanel(new GridLayout(3, 2));
		grid.add(new JLabel("Host name:"));
		grid.add(host);
		grid.add(new JLabel("Port number:"));
		grid.add(port);
		grid.add(new JLabel("Proxy type:"));
		grid.add(type);
		add(grid);
		add(new Box.Filler(MIN_DIM, MAX_DIM, MAX_DIM));
		getLayoutHelper().completeLayout();
	}

	/**
	 * Save the user's choices in the installer data.
	 * 
	 * @see IzPanel#panelDeactivate()
	 */
	@Override
	public void panelDeactivate () {
		try {
			idata.proxy = new Proxy((Proxy.Type) type.getSelectedItem(),
					new InetSocketAddress(host.getText(),
							(Integer) port.getValue()));
		}
		catch (Exception e) {
			idata.proxy = Proxy.NO_PROXY;
		}
	}
}
