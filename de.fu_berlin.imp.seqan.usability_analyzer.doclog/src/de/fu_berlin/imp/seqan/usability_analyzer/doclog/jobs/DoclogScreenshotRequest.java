package de.fu_berlin.imp.seqan.usability_analyzer.doclog.jobs;

import com.bkahlert.nebula.screenshots.impl.webpage.FuzzyFormFiller;
import com.bkahlert.nebula.screenshots.impl.webpage.FuzzyFormFiller.Strategy;
import com.bkahlert.nebula.screenshots.impl.webpage.WebpageScreenshotRequest;
import com.bkahlert.nebula.screenshots.webpage.IWebpageScreenshotRequest;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.util.DoclogUtils;

public class DoclogScreenshotRequest extends WebpageScreenshotRequest implements
		IWebpageScreenshotRequest {

	public DoclogScreenshotRequest(DoclogRecord doclogRecord, int timeout) {
		super(
				FORMAT.PNG,
				doclogRecord.getUri(),
				doclogRecord.getBounds(),
				doclogRecord.getAction() == DoclogAction.TYPING ? new FuzzyFormFiller(
						DoclogUtils.getPossibleFields(doclogRecord
								.getActionParameter()), Strategy.FILL_FIRST,
						1000) : null, timeout);
	}

}
