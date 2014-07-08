package de.fu_berlin.imp.apiua.diff.preferences;

import java.io.File;
import java.io.FileFilter;

import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.apiua.diff.io.RegexFileFilter;
import de.fu_berlin.imp.apiua.diff.preferences.SUADiffPreferenceInitializer;

public class SUADiffPreferenceInitializerTest {

	@Test
	public void testBinFileFilters() {
		FileFilter binFilter = new RegexFileFilter(
				SUADiffPreferenceInitializer.defaultFileFilterPatterns[0]);

		Assert.assertTrue(binFilter.accept(new File("abc")));
		Assert.assertFalse(binFilter.accept(new File("bin")));
		Assert.assertTrue(binFilter.accept(new File("binabc")));
		Assert.assertFalse(binFilter.accept(new File("bin/abc")));
		Assert.assertTrue(binFilter.accept(new File("/abc")));
		Assert.assertFalse(binFilter.accept(new File("/bin")));
		Assert.assertTrue(binFilter.accept(new File("/binabc")));
		Assert.assertFalse(binFilter.accept(new File("/bin/abc")));
	}

	@Test
	public void testFileFilters() {
		FileFilter coreFilter = new RegexFileFilter(
				SUADiffPreferenceInitializer.defaultFileFilterPatterns[1]);

		Assert.assertTrue(coreFilter.accept(new File("abc")));
		Assert.assertFalse(coreFilter.accept(new File("core")));
		Assert.assertTrue(coreFilter.accept(new File("coreabc")));
		Assert.assertFalse(coreFilter.accept(new File("core/abc")));
		Assert.assertTrue(coreFilter.accept(new File("/abc")));
		Assert.assertFalse(coreFilter.accept(new File("/core")));
		Assert.assertTrue(coreFilter.accept(new File("/coreabc")));
		Assert.assertFalse(coreFilter.accept(new File("/core/abc")));
	}
}
