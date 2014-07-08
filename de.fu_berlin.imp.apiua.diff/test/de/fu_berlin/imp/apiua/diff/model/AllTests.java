package de.fu_berlin.imp.apiua.diff.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.apiua.diff.model.diff.AllTests.class,
		de.fu_berlin.imp.apiua.diff.model.source.AllTests.class,
		DiffFileRecordTest.class, DiffFileDirectoryTest.class,
		DiffTest.class })
public class AllTests {

}
