package de.fu_berlin.imp.seqan.usability_analyzer.doclog.jobs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.screenshots.impl.webpage.WebpageScreenshotTaker;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.preferences.SUADoclogPreferenceUtil;

public class TakeScreenshotsJob extends Job {

	private static final Logger LOGGER = Logger
			.getLogger(TakeScreenshotsJob.class);

	private DoclogRecord[] doclogRecords;

	public TakeScreenshotsJob(DoclogRecord[] doclogRecords) {
		super("Taking " + doclogRecords.length + " screenshots");
		this.doclogRecords = doclogRecords;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		int timeout = new SUADoclogPreferenceUtil()
				.getScreenshotPageloadTimeout();

		DoclogRecord[] filteredDoclogRecords = getRelevantDoclogRecords(
				monitor, this.doclogRecords);

		monitor.beginTask("Capturing", filteredDoclogRecords.length * 11);
		HashSet<String> handledScreenshots = new HashSet<String>();
		try {
			WebpageScreenshotTaker screenshotTaker = new WebpageScreenshotTaker(
					Runtime.getRuntime().availableProcessors() * 5, null);
			for (final DoclogRecord doclogRecord : filteredDoclogRecords) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}

				File file = doclogRecord.getScreenshot().getFile();
				if (file == null) {
					file = File.createTempFile("sua", "screenshot.png");
				}
				String filename = file.getCanonicalPath();
				if (!handledScreenshots.contains(filename)) {
					LOGGER.info("Creating screenshot in " + filename);
					if (doclogRecord.getWindowDimensions().x < 0
							|| doclogRecord.getWindowDimensions().y < 0) {
						LOGGER.warn("Could not create screenshot for "
								+ doclogRecord
								+ " because of incorrect window dimensions");
						continue;
					}

					try {
						Future<File> screenshotFile = screenshotTaker
								.submitOrder(new DoclogScreenshotRequest(
										doclogRecord, timeout));
						doclogRecord.setScreenshot(screenshotFile.get());
						handledScreenshots.add(filename);
					} catch (IOException e) {
						LOGGER.error("Could not create screenshot for "
								+ doclogRecord, e);
					} catch (InterruptedException e) {
						LOGGER.error("Could not create screenshot for "
								+ doclogRecord, e);
					} catch (ExecutionException e) {
						LOGGER.error("Could not create screenshot for "
								+ doclogRecord, e);
					}
				} else {
					LOGGER.info("Screenshot already created in " + filename);
				}

				monitor.worked(10);
			}
			screenshotTaker.dispose();
		} catch (CancellationException e) {
			monitor.setCanceled(true);
			return Status.CANCEL_STATUS;
		} catch (IOException e) {
			LOGGER.fatal("Error creating screenshots", e);
		}
		return Status.OK_STATUS;
	}

	private static DoclogRecord[] getRelevantDoclogRecords(
			IProgressMonitor monitor, final DoclogRecord[] doclogRecords) {
		IDataService dataService = (IDataService) PlatformUI.getWorkbench()
				.getService(IDataService.class);
		TimeZoneDateRange range = dataService.getActiveDataDirectories().get(0)
				.getInfo().getDateRange();

		int numOkScreenshots = 0;
		List<DoclogRecord> relevantDoclogRecords = new ArrayList<DoclogRecord>();
		for (DoclogRecord doclogRecord : doclogRecords) {
			if (monitor.isCanceled()) {
				return doclogRecords;
			}

			if (doclogRecord.getScreenshot().getStatus() == DoclogScreenshot.Status.OK
					|| doclogRecord.getUrl().startsWith("file")

					|| doclogRecord.getUrl()
							.contains("trac.seqan.de/newticket")
					|| !range.isIntersected(doclogRecord.getDateRange())) {
				numOkScreenshots++;
			} else {
				relevantDoclogRecords.add(doclogRecord);
			}

			monitor.worked(1);
		}

		if (numOkScreenshots > 0) {
			final int finalNumOkScreenshots = numOkScreenshots;
			final AtomicInteger rt = new AtomicInteger();
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageBox messageBox = new MessageBox(PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setText("Screenshots");
					messageBox
							.setMessage(finalNumOkScreenshots
									+ " of the "
									+ doclogRecords.length
									+ " requested screenshots don't seem to need an update.\n"
									+ "Do you want to exclude them from your request?");
					rt.set(messageBox.open());
				}
			});
			if (rt.get() == SWT.YES) {
				return relevantDoclogRecords.toArray(new DoclogRecord[0]);
			} else {
				return doclogRecords;
			}

		} else {
			return doclogRecords;
		}
	}
}
