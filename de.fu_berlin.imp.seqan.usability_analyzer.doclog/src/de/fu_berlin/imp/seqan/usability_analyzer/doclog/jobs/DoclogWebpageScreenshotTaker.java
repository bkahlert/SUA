package de.fu_berlin.imp.seqan.usability_analyzer.doclog.jobs;

import org.eclipse.swt.widgets.Shell;

import com.bkahlert.nebula.screenshots.impl.webpage.WebpageScreenshotTaker;

public class DoclogWebpageScreenshotTaker extends
		WebpageScreenshotTaker<DoclogWebpage> {

	public DoclogWebpageScreenshotTaker(int numThreads, Shell parentShell) {
		super(numThreads, new DoclogWebpageScreenshotRenderer(parentShell));
	}

}
