package de.fu_berlin.imp.apiua.doclog.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;

import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.doclog.jobs.ScreenshotsJobGroup;
import de.fu_berlin.imp.apiua.doclog.model.DoclogRecord;

public class TakeScreenshotHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final List<DoclogRecord> doclogRecords = SelectionRetrieverFactory
				.getSelectionRetriever(DoclogRecord.class).getSelection();

		Job job = new ScreenshotsJobGroup(doclogRecords);
		job.setPriority(Job.SHORT);
		job.schedule();

		return null;
	}

}
