package de.fu_berlin.imp.apiua.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.apiua.core.util.AllTests.class,
		de.fu_berlin.imp.apiua.core.model.AllTests.class })
public class AllTests {

}
