package de.fu_berlin.imp.apiua.doclog.jobs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.services.IDataService;
import de.fu_berlin.imp.apiua.doclog.model.DoclogRecord;
import de.fu_berlin.imp.apiua.doclog.model.DoclogScreenshot;
import de.fu_berlin.imp.apiua.doclog.preferences.SUADoclogPreferenceUtil;

public class ScreenshotsJobGroup extends Job {

	private static final Logger LOGGER = Logger
			.getLogger(ScreenshotsJobGroup.class);

	public static final int NUM_THREADS = Runtime.getRuntime()
			.availableProcessors() * 3;
	public static final int NUM_SCREENSHOTS_PER_CYCLE = NUM_THREADS * 5;

	private List<DoclogRecord> doclogRecords;

	private DoclogWebpageScreenshotTaker screenshotTaker;
	private List<ScreenshotsJob> screenshotsJobs;

	public ScreenshotsJobGroup(List<DoclogRecord> doclogRecords) {
		super("Taking " + doclogRecords.size() + " screenshots");
		this.doclogRecords = doclogRecords;
		this.screenshotsJobs = null;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		int timeout = new SUADoclogPreferenceUtil()
				.getScreenshotPageloadTimeout();

		SubMonitor subMonitor = SubMonitor.convert(monitor,
				50 + this.doclogRecords.size());

		List<DoclogRecord> filteredDoclogRecords = getRelevantDoclogRecords(
				monitor, this.doclogRecords);
		List<String> generatedFilenames = new ArrayList<String>();

		subMonitor.worked(50);
		subMonitor.setWorkRemaining(filteredDoclogRecords.size());

		this.screenshotTaker = new DoclogWebpageScreenshotTaker(NUM_THREADS,
				null);
		this.screenshotsJobs = new ArrayList<ScreenshotsJob>();
		for (int i = 0; i < filteredDoclogRecords.size(); i += NUM_SCREENSHOTS_PER_CYCLE) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			int j = Math.min(i + NUM_SCREENSHOTS_PER_CYCLE,
					filteredDoclogRecords.size());
			List<DoclogRecord> doclogRecords = new ArrayList<DoclogRecord>(
					filteredDoclogRecords.subList(i, j));
			for (Iterator<DoclogRecord> iterator = doclogRecords.iterator(); iterator
					.hasNext();) {
				DoclogRecord doclogRecord = iterator.next();
				try {
					String futureFilename = doclogRecord.getScreenshot()
							.getFile().getName();
					if (generatedFilenames.contains(futureFilename)) {
						iterator.remove();
					}
				} catch (Exception e) {
				}
			}
			ScreenshotsJob screenshotsJob = new ScreenshotsJob(
					this.screenshotTaker, timeout, doclogRecords,
					"Taking screenshots " + (i + 1) + " to " + j);
			screenshotsJob.setProgressGroup(subMonitor, j - i);
			screenshotsJob.schedule();
			this.screenshotsJobs.add(screenshotsJob);

			// TODO only join every two scheduled jobs to improve performance
			// (even more)
			try {
				screenshotsJob.join();
				for (String generatedFilename : screenshotsJob
						.getGeneratedFilenames()) {
					if (!generatedFilenames.contains(generatedFilename)) {
						generatedFilenames.add(generatedFilename);
					}
				}
			} catch (InterruptedException e) {
				LOGGER.error(
						"Error waiting for "
								+ ScreenshotsJob.class.getSimpleName()
								+ " to finish", e);
			}
		}

		for (ScreenshotsJob screenshotsJob : this.screenshotsJobs) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			try {
				screenshotsJob.join();
			} catch (InterruptedException e) {
				LOGGER.error(
						"Error waiting for "
								+ ScreenshotsJob.class.getSimpleName()
								+ " to finish", e);
			}
		}

		return Status.OK_STATUS;
	}

	@Override
	protected void canceling() {
		if (this.screenshotsJobs != null) {
			for (ScreenshotsJob screenshotsJob : this.screenshotsJobs) {
				screenshotsJob.cancel();
			}
		}
		if (this.screenshotTaker != null) {
			this.screenshotTaker.dispose();
		}
		super.canceling();
	}

	private static List<DoclogRecord> getRelevantDoclogRecords(
			IProgressMonitor monitor, final List<DoclogRecord> doclogRecords) {
		IDataService dataService = (IDataService) PlatformUI.getWorkbench()
				.getService(IDataService.class);
		TimeZoneDateRange range = dataService.getActiveDataDirectories().get(0)
				.getInfo().getDateRange();

		int numOkScreenshots = 0;
		List<DoclogRecord> relevantDoclogRecords = new ArrayList<DoclogRecord>();

		for (DoclogRecord doclogRecord : doclogRecords) {
			if (doclogRecord.getUrl().contains("seqan/dev/INDEX_Page.html")) {
				// relevantDoclogRecords.add(doclogRecord);
			} else if (doclogRecord.getUrl().contains("dev2/panel/index.html")) {
				// relevantDoclogRecords.add(doclogRecord);
			}
		}
		if (true) {
			// return relevantDoclogRecords;
		}

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
					messageBox.setMessage(finalNumOkScreenshots
							+ " of the "
							+ doclogRecords.size()
							+ " requested screenshots don't seem to need an update.\n"
							+ "Do you want to exclude them from your request?");
					rt.set(messageBox.open());
				}
			});
			if (rt.get() == SWT.YES) {
				return relevantDoclogRecords;
			} else {
				return doclogRecords;
			}

		} else {
			return doclogRecords;
		}
	}
}
