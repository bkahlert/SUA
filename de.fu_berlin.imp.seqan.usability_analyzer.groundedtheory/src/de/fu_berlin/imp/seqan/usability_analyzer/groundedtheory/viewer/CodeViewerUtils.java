package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.net.URI;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.DistributionUtils.RelativeWidth;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.GTLabelProvider;

public class CodeViewerUtils {

	private static final ILocatorService LOCATOR_SERVICE = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	public static void createCodeColumn(SortableTreeViewer treeViewer,
			final ICodeService codeService) {
		TreeViewerColumn codeColumn = treeViewer.createColumn("Code",
				new RelativeWidth(1.0, 150));

		final ILabelProvider labelProvider = new GTLabelProvider();

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
					@Override
					protected boolean isEditorActivationEvent(
							ColumnViewerEditorActivationEvent event) {
						return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
					}
				}, TreeViewerEditor.DEFAULT);
	}

	public static void createNumPhaenomenonsColumn(
			SortableTreeViewer treeViewer, final ICodeService codeService) {
		TreeViewerColumn countColumn = treeViewer.createColumn("# ph",
				new AbsoluteWidth(60));
		countColumn.getColumn().setAlignment(SWT.RIGHT);
		countColumn
				.setLabelProvider(new ILabelProviderService.StyledLabelProvider() {
					@Override
					public StyledString getStyledText(URI uri) throws Exception {
						ILocatable element = LOCATOR_SERVICE.resolve(uri, null)
								.get();

						if (ICode.class.isInstance(element)) {
							ICode code = (ICode) element;
							int all = codeService.getAllInstances(code).size();
							int here = codeService.getInstances(code).size();
							StyledString text = new StyledString(all + "",
									Stylers.DEFAULT_STYLER);
							text.append("   " + here, Stylers.COUNTER_STYLER);
							return text;
						}
						return new StyledString();
					}
				});
	}

}
