package de.fu_berlin.imp.apiua.doclog;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.apiua.doclog.util.AllTests.class,
		de.fu_berlin.imp.apiua.doclog.model.AllTests.class })
public class AllTests {

}
