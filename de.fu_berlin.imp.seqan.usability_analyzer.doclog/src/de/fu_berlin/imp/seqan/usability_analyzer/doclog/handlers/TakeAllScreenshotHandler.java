package de.fu_berlin.imp.seqan.usability_analyzer.doclog.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.jobs.TakeScreenshotsJob;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
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
			final DoclogRecordList doclogRecords = new DoclogRecordList();
			Job job = new Job("Parsing " + Doclog.class.getSimpleName() + "s") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					DoclogDataContainer doclogDataContainer = Activator
							.getDefault().getDoclogContainer();
					IIdentifier[] identifiers = doclogDataContainer
							.getIdentifiers();
					monitor.beginTask("Parsing " + Doclog.class.getSimpleName()
							+ "s", identifiers.length);

					for (IIdentifier identifier : identifiers) {
						doclogRecords.addAll(doclogDataContainer.getDoclogFile(
								identifier, new SubProgressMonitor(monitor, 1))
								.getDoclogRecords());
					}
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.setPriority(Job.SHORT);
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					Job job = new TakeScreenshotsJob(doclogRecords
							.toArray(new DoclogRecord[0]));
					job.setPriority(Job.SHORT);
					job.schedule();
				}
			});
			job.schedule();
		}

		return null;
	}

}
