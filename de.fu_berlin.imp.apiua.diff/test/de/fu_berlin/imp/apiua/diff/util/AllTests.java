package de.fu_berlin.imp.apiua.diff.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TrunkUtilsTest.class, DiffUtilsTest.class, DiffCacheTest.class,
		DiffRecordUtilsTest.class })
public class AllTests {

}
