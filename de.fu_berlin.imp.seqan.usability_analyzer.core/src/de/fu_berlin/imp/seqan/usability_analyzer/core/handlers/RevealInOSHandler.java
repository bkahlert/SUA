package de.fu_berlin.imp.seqan.usability_analyzer.core.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.bkahlert.devel.nebula.utils.FileUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IData;

public class RevealInOSHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(RevealInOSHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final List<IData> datas = SelectionRetrieverFactory
				.getSelectionRetriever(IData.class).getSelection();

		if (datas.size() > 0) {
			for (IData data : datas) {
				try {
					FileUtils.showFileInFilesystem(data.getStaticFile());
				} catch (Exception e) {
					LOGGER.warn("Could not reveal file in your OS", e);
				}
			}
		}

		return null;
	}
}