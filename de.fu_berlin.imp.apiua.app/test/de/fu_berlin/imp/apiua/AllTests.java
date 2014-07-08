package de.fu_berlin.imp.apiua;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ de.fu_berlin.imp.apiua.core.AllTests.class,
		de.fu_berlin.imp.apiua.groundedtheory.AllTests.class,
		de.fu_berlin.imp.apiua.diff.AllTests.class,
		de.fu_berlin.imp.apiua.doclog.AllTests.class,
		de.fu_berlin.imp.apiua.survey.AllTests.class,
		de.fu_berlin.imp.apiua.stats.AllTests.class,
		de.fu_berlin.imp.apiua.entity.AllTests.class })
public class AllTests {

}
