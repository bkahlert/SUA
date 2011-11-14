package de.fu_berlin.imp.seqan.usability_analyzer.doclog.jobs;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.web.screenshots.ScreenshotInfo;
import com.bkahlert.devel.web.screenshots.ScreenshotTaker;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.preferences.SUADoclogPreferenceUtil;

public class TakeScreenshotsJob extends Job {

	private Logger logger = Logger.getLogger(TakeScreenshotsJob.class);

	private List<DoclogRecord> doclogRecords;

	public TakeScreenshotsJob(List<DoclogRecord> doclogRecords) {
		super("Taking " + doclogRecords.size() + " screenshots");
		this.doclogRecords = doclogRecords;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		int timeout = new SUADoclogPreferenceUtil().getScreenshotPageloadTimeout();

		List<DoclogRecord> filteredDoclogRecords = this
				.getRelevantDoclogRecords(monitor, doclogRecords);

		monitor.beginTask("Capturing", filteredDoclogRecords.size() * 11);
		try {
			ScreenshotTaker screenshotTaker = new ScreenshotTaker();
			for (final DoclogRecord doclogRecord : filteredDoclogRecords) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;

				if (doclogRecord.getWindowDimensions().x > 0
						|| doclogRecord.getWindowDimensions().y > 0) {
					ScreenshotInfo screenshotInfo = new ScreenshotInfo(
							doclogRecord.getUrl(),
							doclogRecord.getScrollPosition(),
							doclogRecord.getWindowDimensions());
					File screenshotFile = screenshotTaker.takeScreenshot(
							screenshotInfo, DoclogScreenshot.FORMAT, timeout);
					try {
						doclogRecord.setScreenshot(screenshotFile);
					} catch (IOException e) {
						logger.error("Could not create screenshot for "
								+ doclogRecord, e);
					}
				} else {
					logger.warn("Could not create screenshot for "
							+ doclogRecord
							+ " because of incorrect window dimensions");
				}

				monitor.worked(10);
			}
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CancellationException e) {
			monitor.setCanceled(true);
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	private List<DoclogRecord> getRelevantDoclogRecords(
			IProgressMonitor monitor, final List<DoclogRecord> doclogRecords) {
		int numOkScreenshots = 0;
		List<DoclogRecord> relevantDoclogRecords = new ArrayList<DoclogRecord>();
		for (DoclogRecord doclogRecord : doclogRecords) {
			if (monitor.isCanceled())
				return doclogRecords;

			if (doclogRecord.getScreenshot().getStatus() == DoclogScreenshot.Status.OK
					|| doclogRecord.getUrl().startsWith("file")) {
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
			if (rt.get() == SWT.YES)
				return relevantDoclogRecords;
			else
				return doclogRecords;

		} else {
			return doclogRecords;
		}
	}
}
