package de.fu_berlin.imp.apiua.core.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.apiua.core.model.data.FileBaseDataContainerTest.class,
		DataSetInfoTest.class, TimeZoneDateTest.class,
		TimeZoneDateRangeTest.class })
public class AllTests {

}
