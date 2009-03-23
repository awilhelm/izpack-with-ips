/*
 * Copyright 2009 Alexis Wilhelm. This program is free software: you can do
 * anything, but lay off of my blue suede shoes.
 */

package com.izforge.izpack.panels;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.jar.JarInputStream;
import javax.swing.JCheckBox;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.UnpackerBase;
import com.izforge.izpack.updater.Updater;

/**
 * A panel that allows the end user to customize the behavior of the IPS packs'
 * unpacker.
 * 
 * @author Alexis Wilhelm
 * @since February 2009
 * @see com.izforge.izpack.installer.IPSUnpacker
 */
public class IPSPrefsPanel extends IzPanel
{
	/**
	 * A method for generating the updater. It bases either on the updater
	 * prepared during the compilation phase or on the installer currently
	 * running.
	 */
	private static class UpdaterGenerator implements Runnable
	{
		/**
		 * The informations on the current installation.
		 */
		private final AutomatedInstallData data;

		/**
		 * @param data The informations on the current installation.
		 */
		public UpdaterGenerator (AutomatedInstallData data)
		{
			this.data = data;
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run ()
		{
			try
			{
				new File(data.getInstallPath() + "/Updater").mkdirs();
				Updater out;
				out = new Updater(data.getInstallPath() + "/Updater/"
						+ Updater.FILE_NAME);
				/*
				 * Extract the prepared updater, or copy this whole installer
				 * into the updater if no updater was prepared.
				 */
				InputStream in = UnpackerBase.class.getResourceAsStream("/res/"
						+ Updater.FILE_NAME);
				if (in == null)
				{
					out.append(new JarInputStream(new FileInputStream(
							System.getProperties().getProperty(
									"java.class.path")), true));
				}
				else
				{
					out.append(new JarInputStream(in, true));
					in.close();
				}
				/*
				 * Add some informations about this installation in the updater.
				 */
				out.append(AutomatedInstallData.FILE_NAME, data.getVariables());
				/*
				 * Finalize the just-created Jar.
				 */
				out.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * This allows this panel to get serialized.
	 */
	private static final long serialVersionUID = -7714579480159174801L;

	/**
	 * Asks the user if he wants to be able to update with the installer.
	 */
	public final JCheckBox keep = new JCheckBox(
			idata.langpack.getString("IPSPrefsPanel.keeptoupdate"));

	/**
	 * Asks the user if he wants to install the Sun Update Center.
	 */
	public final JCheckBox uc = new JCheckBox(
			idata.langpack.getString("IPSPrefsPanel.installupdatecenter"));

	/**
	 * Draw this panel.
	 * 
	 * @param parent The frame hosting this panel.
	 * @param data The all important object describing everything we need for
	 *        the installation.
	 */
	public IPSPrefsPanel (InstallerFrame parent, InstallData data)
	{
		super(parent, data, new IzPanelLayout());
		add(
				createMultiLineLabel(idata.langpack.getString("IPSPrefsPanel.instructions")),
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
	public void panelDeactivate ()
	{
		idata.keepInstallerWhenDone = keep.isSelected();
		idata.installUpdateCenter = uc.isSelected();
		/*
		 * Move this installer in the installation directory if the user wants
		 * it to be done. We hook it to the VM shutdown so that we store the
		 * latest version of the variables.
		 */
		if (keep.isSelected())
		{
			Runtime.getRuntime().addShutdownHook(
					new Thread(new UpdaterGenerator(idata)));
		}
	}
}
