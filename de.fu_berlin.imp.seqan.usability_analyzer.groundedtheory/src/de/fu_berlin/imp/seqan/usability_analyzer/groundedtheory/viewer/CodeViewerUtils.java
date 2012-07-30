package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.HashMap;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.graphics.Image;

import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTreeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;

public class CodeViewerUtils {

	public static void createCodeColumn(SortableTreeViewer treeViewer,
			final ICodeService codeService) {
		TreeViewerColumn codeColumn = treeViewer.createColumn("Code", 150);

		codeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (ICode.class.isInstance(element)) {
					ICode code = (ICode) element;
					return code.getCaption();
				}
				if (ICodeInstance.class.isInstance(element)) {
					ICodeInstance codeInstance = (ICodeInstance) element;
					ICodeable codedObject = codeService
							.getCodedObject(codeInstance.getId());
					ILabelProvider labelProvider = codeService
							.getLabelProvider(codeInstance.getId());
					return (labelProvider != null) ? labelProvider
							.getText(codedObject) : "[UNKNOWN ORIGIN]";
				}
				if (NoCodesNode.class.isInstance(element)) {
					return "no code";
				}
				return "ERROR";
			}

			@Override
			public Image getImage(Object element) {
				if (ICode.class.isInstance(element)) {
					return codeService.isMemo((ICode) element) ? ImageManager.CODE_MEMO
							: ImageManager.CODE;
				}
				if (ICodeInstance.class.isInstance(element)) {
					ICodeInstance codeInstance = (ICodeInstance) element;
					ICodeable codedObject = codeService
							.getCodedObject(codeInstance.getId());
					ILabelProvider labelProvider = codeService
							.getLabelProvider(codeInstance.getId());
					Image image = (labelProvider != null) ? labelProvider
							.getImage(codedObject) : null;

					return (codeService.isMemo(codeInstance)) ? getMemoAnnotatedImage(image)
							: image;
				}
				return null;
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

	private static HashMap<Image, Image> annotatedImages = new HashMap<Image, Image>();

	protected static Image getMemoAnnotatedImage(Image image) {
		if (image == null)
			return null;

		if (!annotatedImages.containsKey(image)) {
			Image annotatedImage = new DecorationOverlayIcon(image,
					ImageManager.OVERLAY_MEMO, IDecoration.TOP_RIGHT)
					.createImage();
			annotatedImages.put(image, annotatedImage);
		}

		return annotatedImages.get(image);
	}

}
