package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.CodeStoreTest;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.AllTests.class,
		CodeTest.class, CodeStoreTest.class, CodeableUtilsTest.class })
public class AllTests {

}
