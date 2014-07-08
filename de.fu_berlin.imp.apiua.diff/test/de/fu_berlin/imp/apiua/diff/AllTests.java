package de.fu_berlin.imp.apiua.diff;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.apiua.diff.util.AllTests.class,
		de.fu_berlin.imp.apiua.diff.preferences.AllTests.class,
		de.fu_berlin.imp.apiua.diff.model.AllTests.class,
		de.fu_berlin.imp.apiua.diff.services.impl.AllTests.class })
public class AllTests {

}
