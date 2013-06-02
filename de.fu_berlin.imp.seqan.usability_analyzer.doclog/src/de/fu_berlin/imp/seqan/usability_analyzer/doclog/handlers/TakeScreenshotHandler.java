package de.fu_berlin.imp.seqan.usability_analyzer.doclog.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.jobs.TakeScreenshotsJob;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;

public class TakeScreenshotHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final List<DoclogRecord> doclogRecords = SelectionRetrieverFactory
				.getSelectionRetriever(DoclogRecord.class).getSelection();

		Job job = new TakeScreenshotsJob(
				doclogRecords.toArray(new DoclogRecord[0]));
		job.setPriority(Job.SHORT);
		job.schedule();

		return null;
	}

}
