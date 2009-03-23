
package com.izforge.izpack.util;

import java.util.regex.Pattern;

/**
 * A wildcarded sequence. It's there with letting the user use wildcards instead
 * of regexs as its only goal.
 * 
 * @author Alexis Wilhelm
 * @since February 2009
 */
public class Wildcard
{
	/**
	 * The characters describing this wildcarded sequence.
	 */
	private final char[] value;

	/**
	 * Initialize this wildcard.
	 * 
	 * @param value The string describing this wildcarded sequence.
	 */
	public Wildcard (String value)
	{
		this.value = value.toCharArray();
	}

	/**
	 * Turn this wildcarded sequence into a standard pattern. Basically, it
	 * converts '*' and '?' into their standard regular expressions equivalent
	 * and escapes every character happening to be special for the
	 * java.util.regex API.
	 * 
	 * @return A {@link Pattern} one can use with the java.util.regex API.
	 */
	public Pattern toPattern ()
	{
		StringBuffer s = new StringBuffer();
		s.append("^");
		for (char c: value)
		{
			switch (c)
			{
				case '*':
					s.append(".*");
					continue;
				case '?':
					s.append(".");
					continue;
				case '$':
				case '(':
				case ')':
				case '+':
				case '.':
				case '[':
				case '\\':
				case ']':
				case '^':
				case '{':
				case '|':
				case '}':
					s.append("\\");
			}
			s.append(c);
		}
		s.append("$");
		return Pattern.compile(s.toString());
	}
}
