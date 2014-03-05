package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataSetInfo;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;

public class DataSetInfoTest {

	@Test
	public void test() {
		IDataSetInfo dataSetInfo = new DataSetInfo(new FileData(
				FileUtils.getFile("data/__dataset.txt")));
		Assert.assertEquals("SeqAn Workshop '11", dataSetInfo.getName());
		Assert.assertEquals(new TimeZoneDate("2011-09-13T10:00:00+02:00"),
				dataSetInfo.getDateRange().getStartDate());
		Assert.assertEquals(new TimeZoneDate("2011-09-15T18:00:00+02:00"),
				dataSetInfo.getDateRange().getEndDate());
	}

}
