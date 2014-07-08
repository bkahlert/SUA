package de.fu_berlin.imp.apiua.groundedtheory;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.imp.apiua.groundedtheory.storage.CodeStoreTest;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.apiua.groundedtheory.propertyTesters.AllTests.class,
		de.fu_berlin.imp.apiua.groundedtheory.model.AllTests.class,
		de.fu_berlin.imp.apiua.groundedtheory.storage.AllTests.class,
		de.fu_berlin.imp.apiua.groundedtheory.services.AllTests.class,
		CodeTest.class, CodeStoreTest.class, URIUtilsTest.class })
public class AllTests {

}
