/*
 * Copyright 2009 Alexis Wilhelm. This program is free software: you can do
 * anything, but lay off of my blue suede shoes.
 */

package com.izforge.izpack.installer;

import java.io.File;
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
 * @since January 2009
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
			handler.nextStep("Begining", ++job, 1);
			handler.progress(0, "Extracting an empty image...");
			img = getImage();
			img.setProxy(idata.proxy);
			/*
			 * Remove installed packages not marked as installed.
			 */
			handler.nextStep("Update", ++job, 2);
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
			 * TODO Install the Sun Update Center if the user wants it to be
			 * done.
			 */
			if (idata.installUpdateCenter) {}
			/*
			 * TODO Move this installer in the installation directory if the
			 * user wants it to be done.
			 */
			if (idata.keepInstallerWhenDone) {}
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
			handler.progress(++step, "Retrieving the catalog...");
			img.addAuthority(pack.getAuthority());
			/*
			 * Install the packages this pack requires.
			 */
			handler.progress(++step, "Resolving dependencies...");
			ImagePlan plan = img.makeInstallPlan(pack.getPackages(img.getInventory()));            
			plan.execute(new ImagePlanProgressTrackerForIzPack(handler, step));
		}
		catch (Exception e) {
			e.printStackTrace();
			handler.emitWarning(e.getClass().getSimpleName(),
					e.getLocalizedMessage());
		}
	}
}
