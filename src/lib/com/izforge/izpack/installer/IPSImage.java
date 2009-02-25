package com.izforge.izpack.installer;

import com.sun.pkg.client.Image.FmriState;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/** The pkg Java API isn't complete yet, so this class is intended to complete it using the pkg Python tool for unimplemented tasks.  See http://opensolaris.org/sc/src/pkg/gate/src/man/pkg.1.txt for details on the pkg Python scripts, and http://download.java.net/updatecenter2/promoted/latest/javadocs/ for details on the pkg Java API.
*/

public class
IPSImage extends com.sun.pkg.client.Image
{
	/// The path to the pkg Python tool.
	private static final String pkgpath = "/home/aoyama/ISIMA/Projet/pkg-toolkit-linux-i386/pkg/bin/pkg";

	public IPSImage (File path) throws Exception { super(path); }
	public IPSImage (String path) throws Exception { super(path); }

/** Update an existing authority or add an additional package authority, and set the specified authority as the preferred authority.

@param authname
	The name for the authority.
@param origin
	The URL prefix for the origin packaging repository for the authority.
*/

	public IPSImage
	addAuthority (String authname, URL origin) throws java.io.IOException, InterruptedException, Exception
	{
		String[] cmd = { pkgpath, "set-authority", "-P", "-O", origin.toString(), authname };
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(getRootDirectory());
		Process proc = pb.start();
		proc.waitFor();
		if (proc.exitValue() != 0)
		{
			throw new Exception("Subprocess " + pb.command() + " failed.");
		}

		// Update the state of packages in the current image.
		refreshCatalog(authname);
		return new IPSImage(getRootDirectory());
	}

/** Create, at location given by dir, an image suitable for package operations.  An attempt to retrieve the catalog associated with this authority will be made following the initial creation operations.

@param dir
	The location for the new image.
@param authname
	The preferred authority name.
@param origin
	The preferred authority origin.
@return
	An image suitable for package operations.
*/

	public static IPSImage
	createImage (File dir, String authname, URL origin) throws java.io.IOException, InterruptedException, Exception
	{
		String[] cmd = { pkgpath, "image-create", "-a",
			String.format("%s=%s", authname, origin), dir.getAbsolutePath() };
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process proc = pb.start();
		proc.waitFor();
		return new IPSImage(dir);
	}

/** Create an image without an authority.

@param dir
	The location for the new image.
@return
	An image suitable for package operations.
*/

	public static IPSImage
	createImage (File dir) throws java.net.MalformedURLException, java.io.IOException, InterruptedException, Exception
	{
		IPSImage img = createImage(dir, "default", new URL("http://pkg.sun.com/glassfish/v3prelude/release/"));
		img.unsetAuthority("default");
		return img;
	}

/** Remove the configuration associated with the given authority.  Well, actually, it seems not to do a thing...

@param authname
	The authority to remove.
*/

	public void
	unsetAuthority (String authname) throws java.io.IOException, InterruptedException, Exception
	{
		String[] cmd = { pkgpath, "set-authority", authname };
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(getRootDirectory());
		Process proc = pb.start();
		proc.waitFor();
	}

/** Get a set of all packages in the repository.

@return
	A set of all available packages.
*/

	public Set<String>
	getInventory ()
	{
		HashSet<String> pkgset = new HashSet<String>();
		for (FmriState s: getInventory(null, true))
		{
			pkgset.add(s.fmri.getName());
		}
		return pkgset;
	}

/** Call refresh on all of the Catalogs for this image.
*/

	public void
	refreshCatalog (String authname) throws java.io.IOException
	{
		String[] cmd = { pkgpath, "refresh", authname };
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(getRootDirectory());
		Process proc = pb.start();
        try {
		proc.waitFor();
        }
        catch (Exception e) {}
		if (proc.exitValue() != 0)
		{
			//throw new Exception("Subprocess " + pb.command() + " failed.");
		}
	}
}
