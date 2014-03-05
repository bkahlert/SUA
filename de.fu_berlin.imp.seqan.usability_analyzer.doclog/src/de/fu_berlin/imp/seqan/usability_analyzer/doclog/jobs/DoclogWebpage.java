package de.fu_berlin.imp.seqan.usability_analyzer.doclog.jobs;

import java.net.URISyntaxException;

import com.bkahlert.nebula.screenshots.impl.webpage.FuzzyFieldWebpage;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.util.DoclogUtils;

public class DoclogWebpage extends FuzzyFieldWebpage {

	public DoclogWebpage(DoclogRecord doclogRecord, int timeout)
			throws URISyntaxException {
		super(new java.net.URI(doclogRecord.getUrl()),
				doclogRecord.getBounds(), timeout,
				doclogRecord.getAction() == DoclogAction.TYPING ? DoclogUtils
						.getPossibleFields(doclogRecord.getActionParameter())
						: null, 500);
	}

}
