package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.graphics.Image;

import com.bkahlert.devel.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.GTCodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class CodeViewerUtils {

	public static void createCodeColumn(SortableTreeViewer treeViewer,
			final ICodeService codeService) {
		TreeViewerColumn codeColumn = treeViewer.createColumn("Code", 220);

		final ILabelProvider labelProvider = new GTCodeableProvider()
				.getLabelProvider();

		codeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return labelProvider.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				return labelProvider.getImage(element);
			}
		});

		codeColumn.setEditingSupport(new CodeEditingSupport(treeViewer));
		TreeViewerEditor.create(treeViewer,
				new ColumnViewerEditorActivationStrategy(treeViewer) {
					protected boolean isEditorActivationEvent(
							ColumnViewerEditorActivationEvent event) {
						return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
					}
				}, TreeViewerEditor.DEFAULT);
	}

}
