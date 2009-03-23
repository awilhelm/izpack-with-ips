/*
 * Copyright 2009 Alexis Wilhelm. This program is free software: you can do
 * anything, but lay off of my blue suede shoes.
 */

package com.izforge.izpack;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * A pack useful for installing IPS packages.
 * 
 * @author Alexis Wilhelm
 * @author Romain Tertiaux
 * @since January 2009
 */
public class IPSPack implements Serializable
{
	/**
	 * Allow this pack to get serialized.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The origin URL for this pack's authority.
	 */
	private final URL authority;

	/**
	 * This pack's name.
	 */
	private final String name;

	/**
	 * A list of wanted and unwanted packages.
	 */
	private final List<Clude> cludes;

	/**
	 * The version that the installer will try to get, if available.
	 */
	private final String version;

	/**
	 * The description of this pack's.
	 */
	private final String description;

	/**
	 * Whether this pack is checked by default in the installer's pack list.
	 */
	private final Boolean checked;

	/**
	 * @param authority The authority for this pack.
	 * @param name This pack's name.
	 * @param description This pack's description.
	 * @param version This pack's version. It can be null if you just don't care
	 *        about it.
	 * @param checked Whether this pack will be initially checked or not.
	 * @param cludes A list of cludes defining the packages this pack requires.
	 * @throws MalformedURLException When the URL given for this pack's
	 *         authority is invalid.
	 */
	public IPSPack (String authority, String name, String description,
			String version, Boolean checked, List<Clude> cludes)
			throws MalformedURLException
	{
		this.name = name;
		this.description = description;
		this.version = version;
		this.checked = checked;
		this.authority = new URL(authority);
		this.cludes = cludes;
	}

	/**
	 * @return The origin URL for this pack's authority.
	 */
	public URL getAuthority ()
	{
		return authority;
	}

	/**
	 * @return The description of this pack's.
	 */
	public String getDescription ()
	{
		return description;
	}

	/**
	 * @return This pack's name.
	 */
	public String getName ()
	{
		return name;
	}

	/**
	 * Get every package this pack requires.
	 * 
	 * @param candidates A set of all available packages.
	 * @return An array filled with names of the packages this pack requires.
	 */
	public String[] getPackages (Collection<String> candidates)
	{
		/*
		 * Start with an empty set if the first clude describes an inclusion,
		 * and with every available packages if the first clude is an exclusion
		 * or if there is no clude at all.
		 */
		Collection<String> pkgset = !cludes.isEmpty()
				&& cludes.get(0).isIncluded() ? new HashSet<String>()
				: new HashSet<String>(candidates);
		/*
		 * Refine the set according to every cludes.
		 */
		for (Clude c: cludes)
		{
			c.filter(pkgset, candidates);
		}
		return pkgset.toArray(new String[0]);
	}

	/**
	 * @return The version that the installer will try to get, if available.
	 */
	public String getVersion ()
	{
		return version;
	}

	/**
	 * @return True when this pack will be initially checked in the installer's
	 *         pack list, false otherwise.
	 */
	public Boolean isCheckedByDefault ()
	{
		return checked;
	}
}
