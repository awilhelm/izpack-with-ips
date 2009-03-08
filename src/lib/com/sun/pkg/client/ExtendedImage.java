/*
 * Copyright 2009 Alexis Wilhelm. This program is free software: you can do
 * anything, but lay off of my blue suede shoes.
 */

package com.sun.pkg.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The pkg Java API isn't complete yet, so this class is intended to complete it
 * using the pkg Python tool for unimplemented tasks. See
 * http://opensolaris.org/sc/src/pkg/gate/src/man/pkg.1.txt for details on the
 * pkg Python scripts and
 * http://download.java.net/updatecenter2/promoted/latest/javadocs/ for details
 * on the pkg Java API.
 * 
 * @author Alexis Wilhelm
 * @since January 2009
 */
public class ExtendedImage extends Image {

	/**
	 * Extract, at location given by dir, a predefined image suitable for
	 * package operations. No attempt to retrieve catalogs will be made
	 * following the initial creation operations.
	 * 
	 * @param dir The location for the new image.
	 * @param template A {@link ZipInputStream} streaming the predefined image.
	 * @return An image suitable for package operations.
	 * @throws Exception When the new image can't be created for some reason.
	 */
	public static ExtendedImage create (File dir, ZipInputStream template)
			throws Exception {
		for (;;) {
			ZipEntry entry = template.getNextEntry();
			if (entry == null) break;
			if (entry.isDirectory()) new File(dir, entry.getName()).mkdirs();
			else {
				File file = new File(dir, entry.getName());
				FileOutputStream writer = new FileOutputStream(file);
				byte[] buffer = new byte[0x1000];
				for (;;) {
					int len = template.read(buffer);
					if (len == -1) break;
					writer.write(buffer, 0, len);
				}
				writer.close();
				file.setExecutable(true);
			}
			template.closeEntry();
		}
		template.close();
		return new ExtendedImage(dir);
	}

	/**
	 * Initialize this Image.
	 * 
	 * @param path Path to this image's folder.
	 * @throws Exception When the given folder contains no valid IPS image.
	 */
	public ExtendedImage (File path) throws Exception {
		super(path);
	}

	/**
	 * Initialize this Image.
	 * 
	 * @param path This image's folder.
	 * @throws Exception When the given folder contains no valid IPS image.
	 */
	public ExtendedImage (String path) throws Exception {
		super(path);
	}

	/**
	 * Update an existing authority or add an additional package authority, and
	 * set the specified authority as the preferred authority.
	 * 
	 * @param origin The URL prefix for the origin repository for the authority.
	 * @throws IOException When this image can't be updated.
	 */
	public void addAuthority (URL origin) throws IOException {
		String authname = origin.getHost();
		setAuthority(authname, origin, null);
		saveConfig();
		refreshCatalog(authname);
	}

	/**
	 * Get a set of all packages in the repository.
	 * 
	 * @return A set of all available packages.
	 */
	public Set<String> getInventory () {
		HashSet<String> pkgset = new HashSet<String>();
		for (FmriState s: getInventory(null, true))
			pkgset.add(s.fmri.getName());
		return pkgset;
	}
}
