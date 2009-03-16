/*
 * Copyright 2009 Alexis Wilhelm. This program is free software: you can do
 * anything, but lay off of my blue suede shoes.
 */

package com.izforge.izpack.installer;

import java.io.*;
import java.util.zip.ZipInputStream;
import com.izforge.izpack.IPSPack;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.sun.pkg.client.ExtendedImage;
import com.sun.pkg.client.ImagePlanProgressTracker;
import com.sun.pkg.client.Image.FmriState;
import com.sun.pkg.client.Image.ImagePlan;

/**
 * An unpacker for the IPS packs.
 * 
 * @author Alexis Wilhelm
 * @author Romain Tertiaux
 * @since January-March 2009
 */
public class IPSUnpacker extends UnpackerBase {

	/**
	 * Some callbacks useful for tracking the progress of the actions performed
	 * by {@link ImagePlan#execute(ImagePlanProgressTracker)}.
	 */
	private static class ImagePlanProgressTrackerForIzPack extends
			ImagePlanProgressTracker {

		/**
		 * The installation progress handler.
		 */
		private AbstractUIProgressHandler handler;

		/**
		 * The current step within the current job.
		 */
		private int step;

		/**
		 * Initialize this tracker.
		 * 
		 * @param handler The installation progress handler.
		 * @param step The current step.
		 */
		public ImagePlanProgressTrackerForIzPack (
				AbstractUIProgressHandler handler, int step) {
			this.handler = handler;
			this.step = step;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void startDownloadPhase (int total) {
			handler.progress(++step, String.format(
					"Downloading %d package%s...", total, total > 1 ? "s" : ""));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void startInstallPhase (int total) {
			handler.progress(++step, String.format("Installing %d file%s...",
					total, total > 1 ? "s" : ""));
            handler.emitNotification("plop");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void startRemovalPhase (int total) {
			handler.progress(++step, String.format("Removing %d package%s...",
					total, total > 1 ? "s" : ""));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void startUpdatePhase (int total) {
			handler.progress(++step, String.format("Updating %d package%s...",
					total, total > 1 ? "s" : ""));
		}
	}

	/**
	 * The name for the empty IPS image in this installer's resource set.
	 */
	public static final String template = "emptyimage.zip";

	/**
	 * The IPS image dwelling in the installation directory.
	 */
	private static ExtendedImage img = null;

	/**
	 * @return True if there's an image in the installation directory, false
	 *         it's still to be created.
	 */
	public static boolean hasImage () {
		return img != null;
	}

	/**
	 * The frame hosting the installation progress handler.
	 */
	private InstallerFrame frame;

	/**
	 * True when only trivial operations where performed, false otherwise. A
	 * trivial operation isn't that important; therefore it doesn't require the
	 * user to press the "next" button.
	 */
	private boolean trivial = true;

	/**
	 * Initialize this unpacker.
	 * 
	 * @param data The installation data.
	 * @param handler The installation progress handler.
	 * @param parent The frame hosting the installation progress handler.
	 */
	public IPSUnpacker (AutomatedInstallData data,
			AbstractUIProgressHandler handler, InstallerFrame parent) {
		super(data, handler);
		frame = parent;
	}

	/**
	 * Actually install the IPS packs.
	 * 
	 * @see UnpackerBase#run()
	 */
	@Override
	public void run () {
		handler.startAction("IPSUnpacker", idata.selectedIPSPacks.size() + 2);
		int job = 0;
		try {
			/*
			 * Create and prepare a new IPS image in the installation directory.
			 */
			handler.nextStep(langpack.getString("beginning"), ++job, 1);
			handler.progress(0, langpack.getString("emptyimage"));
			img = getImage();
			img.setProxy(idata.proxy);
			/*
			 * Remove installed packages not marked as installed.
			 */
			handler.nextStep(langpack.getString("update"), ++job, 2);
			if (!idata.unwantedIPSPackages.isEmpty()) {
				trivial = false;
				img.uninstallPackages(idata.unwantedIPSPackages);
			}
			/*
			 * Upgrade installed packages marked as upgradable.
			 */
			ImagePlan plan = img.makeInstallPlan(idata.selectedIPSPackages);
			if (plan.getProposedFmris().length > 0) trivial = false;
			plan.execute(new ImagePlanProgressTrackerForIzPack(handler, 1));
			/*
			 * Install each IPS pack.
			 */
			for (IPSPack pack: idata.selectedIPSPacks) {
				trivial = false;
				handler.nextStep(pack.getName(), ++job, 4);
				installPack(pack);
			}
			idata.selectedIPSPacks.clear();
			/*
			 * Install the Sun Update Center if the user wants it to be
			 * done.
			 */
			if (idata.installUpdateCenter)
            {

                handler.nextStep(langpack.getString("IPSInstallPanel.updatecenterinstall"), ++job, 1);

                String configFile = idata.getInstallPath() + "/pkg/lib/bootstrap.properties";

                /* Create the bootstrap configuration file */
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(configFile)));
                out.println("image.path=" + idata.getInstallPath());
                out.println("install.pkg=true");
                out.println("install.updatetool=true");

                if (idata.proxy.type().toString()=="HTTP")
                    out.println("proxy.URL=http://" + idata.proxy.address().toString());
                else if (idata.proxy.type().toString()=="SOCKS")
                {
                    /* TODO: */
                }
                                    
                out.close();

                /* Prepare the command line */
                String[] args = new String[4];
                args[0]="java";
                args[1]="-jar";
                args[2]=idata.getInstallPath() + "/pkg/lib/pkg-bootstrap.jar";
                args[3]=configFile;

                /* Launch the bootstrap */
                /* The path /pkg/lib is to allow pkg-bootstrap.jar to find pkg-client.jar, which is needed */
                Process bootstrap = Runtime.getRuntime().exec(args, null, new File(idata.getInstallPath() + "/pkg/lib"));

                /* If the command didn't end as expected */
                if (bootstrap.waitFor()!=0)
                {
                    handler.emitNotification(langpack.getString("IPSInstallPanel.updatecentererror"));
                }
                else
                {
                    /* Launch the Sun Update Center */
                    Runtime.getRuntime().exec(idata.getInstallPath() + "/updatetool/bin/updatetool");
                }


            }
			/*
			 * Move this installer in the installation directory if the
			 * user wants it to be done.
			 */
			if (idata.keepInstallerWhenDone) {

                new File(idata.getInstallPath() + "/Updater").mkdir();

                /* emitNotifications are temporaries */
                if (copyFile(System.getProperties().getProperty("java.class.path"), idata.getInstallPath() + "/Updater/updater.jar"))
                    handler.emitNotification("Updater créé.");
                else handler.emitNotification("Impossible de créer l'updater.");

                /* write a file in the jar, called "installer_done", with the install path in it
                   to be able to test if we should display some panels or not (IPSPacksPanel, etc) ?
                 */
                
            }
			/*
			 * Get the current state of the image.
			 */
			img = getImage();
			idata.installedIPSPackages.clear();
			for (FmriState pkg: img.getInventory(null, true))
				if (pkg.installed) idata.installedIPSPackages.add(pkg);
		}
		catch (Exception e) {
			e.printStackTrace();
			handler.emitError(e.getClass().getSimpleName(),
					e.getLocalizedMessage());
		}
		finally {
			handler.stopAction();
		}
		/*
		 * Skip to the next panel if no non trivial operations is performed.
		 */
		if (trivial) frame.skipPanel();
	}

	/**
	 * Read the IPS image dwelling in the installation directory. Create a new
	 * image if there's none available.
	 * 
	 * @return An image suitable for package operations.
	 * @throws Exception When the image can't be created for some reason.
	 */
	private ExtendedImage getImage () throws Exception {
		File dir = new File(idata.getInstallPath());
		try {
			return new ExtendedImage(dir);
		}
		catch (Exception e) {
			return ExtendedImage.create(dir, new ZipInputStream(
					UnpackerBase.class.getResourceAsStream("/res/" + template)));
		}
	}

	/**
	 * Install an IPS pack in the local IPS image.
	 * 
	 * @param pack The IPS pack we're to install.
	 */
	private void installPack (IPSPack pack) {                                      
		int step = 0;
		try {
			/*
			 * Update an existing authority or add an additional authority for
			 * packages retrieving.
			 */
			handler.progress(++step, langpack.getString("IPSInstallPanel.retrievingcatalog"));
			img.addAuthority(pack.getAuthority());
			/*
			 * Install the packages this pack requires.
			 */
			handler.progress(++step, langpack.getString("IPSInstallPanel.resolvingdependencies"));
			ImagePlan plan = img.makeInstallPlan(pack.getPackages(img.getInventory()));            
			plan.execute(new ImagePlanProgressTrackerForIzPack(handler, step));
		}
		catch (Exception e) {
			e.printStackTrace();
			handler.emitWarning(e.getClass().getSimpleName(),
					e.getLocalizedMessage());
		}
	}

    public static boolean copyFile(String inSource, String inDest )
    {

        try
        {
            File source = new File(inSource);
            File dest = new File(inDest);            
            // Create the new file

            dest.createNewFile();

            // Streams declaration
            java.io.FileInputStream sourceFile = new java.io.FileInputStream(source);

            try
            {
                java.io.FileOutputStream destinationFile =  new java.io.FileOutputStream(dest);

                try
                {
                    // Read the file, 512KB at a time
                    byte buffer[]=new byte[512*1024];
                    int nbReads;

                    while( (nbReads = sourceFile.read(buffer)) != -1 )
                    {
                        destinationFile.write(buffer, 0, nbReads);
                    }
                }
                finally
                {
                    destinationFile.close();
                }
            }
            finally
            {
                sourceFile.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false; // Error
        }

        return true; // OK
    }
}
