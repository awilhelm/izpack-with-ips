/*
 * Copyright 2009 Alexis Wilhelm. This program is free software: you can do
 * anything, but lay off of my blue suede shoes.
 */

package com.izforge.izpack;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;
import com.izforge.izpack.util.Wildcard;

/**
 * A tuple defining a set of files or packages we want to include or exclude.
 * 
 * @author Alexis Wilhelm
 * @since January 2009
 */
public class Clude implements Serializable
{
	/**
	 * Allow this clude to get serialized.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The pattern the target must match.
	 */
	private final Pattern pattern;

	/**
	 * Whether we want it or not. True would correspond to an &lt;include&gt;
	 * rule in the XML descriptor, and false would come along with an
	 * &lt;exclude&gt; rule.
	 */
	private final Boolean included;

	/**
	 * Tell whether this clude is describes an inclusion or an exclusion.
	 * 
	 * @return True when this clude describes an inclusion, false when it
	 *         describes an exclusion.
	 */
	public Boolean isIncluded ()
	{
		return included;
	}

	/**
	 * Initialize this clude with its pattern and its type.
	 * 
	 * @param pattern The pattern the elements in this clude are to match.
	 * @param included True when this clude describes an inclusion, false when
	 *        it describes an exclusion.
	 */
	public Clude (String pattern, Boolean included)
	{
		this.pattern = new Wildcard(pattern).toPattern();
		this.included = included;
	}

	/**
	 * Filter a set of files or packages, keeping only those matching the
	 * pattern.
	 * 
	 * @param pkgset A set of currently selected packages. It will probably be
	 *        modified through this method.
	 * @param candidates All of the available files or packages.
	 * @return A set of files or packages matching the pattern.
	 */
	public Collection<String> filter (Collection<String> pkgset,
			Iterable<String> candidates)
	{
		if (included)
		{
			/*
			 * Search the repository for every packages matching the pattern,
			 * and include them in the set.
			 */
			for (String in: candidates)
			{
				if (pattern.matcher(in).matches())
				{
					pkgset.add(in);
				}
			}
		}
		else
		{
			/*
			 * Search the set for every excludable packages.
			 */
			Iterable<String> nominees = new HashSet<String>(pkgset);
			for (String ex: nominees)
			{
				if (pattern.matcher(ex).matches())
				{
					pkgset.remove(ex);
				}
			}
		}
		return pkgset;
	}
}
