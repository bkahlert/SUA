package de.fu_berlin.imp.apiua.groundedtheory.storage;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.apiua.groundedtheory.storage.impl.AllTests.class,
		CodeStoreTest.class })
public class AllTests {

}
