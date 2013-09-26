package de.fu_berlin.imp.seqan.usability_analyzer.doclog.jobs;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.bkahlert.nebula.screenshots.IScreenshotTaker.Format;
import com.bkahlert.nebula.screenshots.impl.webpage.WebpageScreenshotTaker;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;

public class ScreenshotsJob extends Job {

	private static final Logger LOGGER = Logger.getLogger(ScreenshotsJob.class);

	private WebpageScreenshotTaker<DoclogWebpage> screenshotTaker;
	private int timeout;
	private List<DoclogRecord> doclogRecords;
	private List<String> generatedFilenames;

	public ScreenshotsJob(
			WebpageScreenshotTaker<DoclogWebpage> screenshotTaker, int timeout,
			List<DoclogRecord> doclogRecords, String title) {
		super(title);
		this.screenshotTaker = screenshotTaker;
		this.timeout = timeout;
		this.doclogRecords = new ArrayList<DoclogRecord>(doclogRecords);
		this.generatedFilenames = new ArrayList<String>(doclogRecords.size());
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				this.doclogRecords.size());
		try {
			if (subMonitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			List<DoclogWebpage> requests = new ArrayList<DoclogWebpage>();
			for (final DoclogRecord doclogRecord : this.doclogRecords) {
				try {
					LOGGER.info("Preparing screenshot in for " + doclogRecord);
					if (doclogRecord.getWindowDimensions().x < 0
							|| doclogRecord.getWindowDimensions().y < 0) {
						LOGGER.warn("Could not create screenshot for "
								+ doclogRecord
								+ " because of negative window dimensions.");
						continue;
					}
					requests.add(new DoclogWebpage(doclogRecord, this.timeout));
				} catch (URISyntaxException e) {
					LOGGER.error("Could not create screenshot for "
							+ doclogRecord, e);
				}
			}

			if (subMonitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			List<Future<File>> screenshotFiles = this.screenshotTaker
					.takeScreenshots(requests, Format.PNG);
			while (screenshotFiles.size() > 0) {
				for (int i = 0, m = screenshotFiles.size(); i < m; i++) {
					if (screenshotFiles.get(i).isDone()) {
						Future<File> screenshot = screenshotFiles.remove(i);
						DoclogRecord doclogRecord = this.doclogRecords
								.remove(i);
						try {
							File file = screenshot.get();
							doclogRecord.setScreenshot(file);
							this.generatedFilenames.add(doclogRecord
									.getScreenshot().getFile().getName());
							LOGGER.info("Created screenshot for "
									+ doclogRecord
									+ "\nto "
									+ doclogRecord.getScreenshot().getFile()
											.getAbsolutePath());
						} catch (InterruptedException e) {
							LOGGER.error("Could not create screenshot for "
									+ doclogRecord, e);
						} catch (ExecutionException e) {
							LOGGER.error("Could not create screenshot for "
									+ doclogRecord, e);
						} catch (IOException e) {
							LOGGER.error("Could not create screenshot for "
									+ doclogRecord, e);
						}
						subMonitor.worked(1);
						break;
					}
				}
			}
		} catch (CancellationException e) {
			subMonitor.setCanceled(true);
			return Status.CANCEL_STATUS;
		}
		subMonitor.done();
		return Status.OK_STATUS;
	}

	public List<String> getGeneratedFilenames() {
		return this.generatedFilenames;
	}

}
