package de.fu_berlin.imp.apiua.diff.io;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * {@link FileFilter} that used regular expressions and - in contract to
 * {@link org.apache.commons.io.filefilter.RegexFileFilter} - computes on the
 * unmodified file path and not only on the filename portion.
 * 
 * @author bkahlert
 * 
 */
public class RegexFileFilter implements FileFilter {

	private final Pattern pattern;

	public RegexFileFilter(String patternString) {
		this.pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public boolean accept(File pathname) {
		String path = pathname.toString();
		return !pattern.matcher(path).matches();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + this.pattern.pattern();
	}

}
