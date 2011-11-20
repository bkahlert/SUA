package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;

public class DataSetInfoTest {

	@Test
	public void test() throws URISyntaxException {
		DataSetInfo dataSetInfo = new DataSetInfo(FileUtils.getFile(
				"data/" + DataSetInfo.FILENAME).getAbsolutePath());
		Assert.assertEquals("SeqAn Workshop '11", dataSetInfo.getName());
		Assert.assertEquals(new TimeZoneDate("2011-09-13T10:00:00+02:00"),
				dataSetInfo.getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-15T18:00:00+02:00"),
				dataSetInfo.getEndDate());
	}

}
