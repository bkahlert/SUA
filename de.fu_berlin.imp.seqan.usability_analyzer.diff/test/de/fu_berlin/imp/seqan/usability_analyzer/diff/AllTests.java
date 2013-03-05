package de.fu_berlin.imp.seqan.usability_analyzer.diff;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.seqan.usability_analyzer.diff.util.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.diff.model.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.diff.services.impl.AllTests.class })
public class AllTests {

}
