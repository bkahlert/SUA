package de.fu_berlin.imp.seqan.usability_analyzer.doclog.util;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Test;

import com.bkahlert.devel.nebula.widgets.timeline.Timeline;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;

public class JsonUtilsTest {
	@Test
	public void testFormatDate() {
		Assert.assertEquals("May 10 1961 17:05:37 GMT+0100",
				JsonUtils.formatDate(DateUtil.getDate(1961, 4, 10, 16, 5, 37)));
		Assert.assertEquals("May 15 1985 14:30:00 GMT+0200",
				JsonUtils.formatDate(DateUtil.getDate(1985, 4, 15, 12, 30, 0)));
		Assert.assertEquals("Dec 31 2000 01:00:00 GMT+0100",
				JsonUtils.formatDate(DateUtil.getDate(2000, 11, 31, 0, 0, 0)));
	}

	@SuppressWarnings("serial")
	@Test
	public void testGenerateJSON() throws URISyntaxException {
		File x = FileUtils.getFile("data/small.doclog");
		DoclogFile doclogFile = new DoclogFile(x.toString());
		for (DoclogRecord doclogRecord : doclogFile.getDoclogRecords()) {
			System.err.println(doclogRecord);
		}

		System.out.println(JsonUtils.generateJSON(doclogFile,
				new HashMap<String, Object>() {
					{
						put("start_date", "2011-09-13T12:05:22+01:00");
						put("zones", new Object[] { new Timeline.Zone(
								"2011-09-13T12:05:22+01:00",
								"2011-09-13T12-05-25+01:00") });
					}
				}, true));
	}
}
