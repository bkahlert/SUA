package de.fu_berlin.imp.seqan.usability_analyzer.doclog.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.jobs.TakeScreenshotsJob;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecordList;

public class TakeAllScreenshotHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), SWT.ICON_QUESTION
				| SWT.YES | SWT.NO);
		messageBox.setText("Screenshots");
		messageBox
				.setMessage("Do you really want to take all screenshots?\nThis can take some time and will overwrite dirty existing screenshots.");
		if (messageBox.open() == SWT.YES) {
			DoclogFileList doclogFiles = Activator.getDefault()
					.getDoclogManager().getDoclogFiles();
			DoclogFileList fingerprintDoclogFiles = Activator.getDefault()
					.getDoclogManager().getFingerprintDoclogFiles();

			DoclogRecordList doclogRecords = new DoclogRecordList();
			for (DoclogFile doclogFile : doclogFiles) {
				doclogRecords.addAll(doclogFile.getDoclogRecords());
			}
			for (DoclogFile fingerprintDoclogFile : fingerprintDoclogFiles) {
				doclogRecords.addAll(fingerprintDoclogFile.getDoclogRecords());
			}

			Job job = new TakeScreenshotsJob(doclogRecords);
			job.setPriority(Job.SHORT);
			job.schedule();
		}

		return null;
	}

}
