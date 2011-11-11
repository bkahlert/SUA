package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;

@RunWith(Parameterized.class)
public class DoclogRecordTest {

	private String strLine;

	public DoclogRecordTest(String strLine) {
		this.strLine = strLine;
	}

	@Parameters
	public static List<Object[]> getParameters() {
		return Arrays
				.asList(new Object[][] {
						{ "2011-09-13T11-15-15	ready	http://trac.mi.fu-berlin.de/seqan/wiki/Tutorial/Seed-and-Extend	141.14.249.204	-	0	0	1353	680" },
						{ "2011-09-13T11-15-18	scroll	http://trac.mi.fu-berlin.de/seqan/wiki/Tutorial/Seed-and-Extend	141.14.249.204	-	50	114	1353	680" },
						{ "2011-09-13T11-24-48	link-http://www.seqan.de/dddoc/html_devel/DEMO_Local+_Alignments.html	http://www.seqan.de/dddoc/html_devel/FUNCTION.local_Alignment.html	141.14.249.204	-	0	366	1353	680" },
						{ "2011-09-13T11-24-49	unload	http://www.seqan.de/dddoc/html_devel/INDEX_Function_Alignments.html#localAlignment	141.14.249.204	-	0	0	181	814" },
						{ "2011-09-13T11-41-20	ready	http://www.seqan.de/dddoc2011-09-13T15-12-56	ready	http://trac.mi.fu-berlin.de/seqan/wiki/Tutorial/FileIO2	141.14.249.204	-	0	0	1353	680" } });
	}

	@Test
	public void parsing() throws DataSourceInvalidException {
		DoclogRecord doclogRecord = new DoclogRecord(null, this.strLine);
		Assert.assertNotNull(doclogRecord.getDate());
		Assert.assertNotNull(doclogRecord.getAction());
		if (doclogRecord.getAction() == DoclogAction.LINK) {
			Assert.assertNotNull(doclogRecord.getActionParameter());
		} else {
			Assert.assertNull(doclogRecord.getActionParameter());
		}
		Assert.assertNotNull(doclogRecord.getUrl());
		Assert.assertNotNull(doclogRecord.getIp());
		Assert.assertTrue(true);// (doclogRecord.getProxyIp());
		Assert.assertNotNull(doclogRecord.getScrollPosition());
		Assert.assertNotNull(doclogRecord.getWindowDimensions());
	}
}
