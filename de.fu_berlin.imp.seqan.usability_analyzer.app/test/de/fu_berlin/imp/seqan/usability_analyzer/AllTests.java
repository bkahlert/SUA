package de.fu_berlin.imp.seqan.usability_analyzer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.seqan.usability_analyzer.core.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.diff.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.doclog.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.survey.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.stats.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.entity.AllTests.class })
public class AllTests {

}
