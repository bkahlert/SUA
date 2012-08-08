package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTreeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.GTCodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class EpisodeViewer extends Composite implements ISelectionProvider {

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(EpisodeViewer.class);
	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

	private SortableTreeViewer treeViewer;

	public EpisodeViewer(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		Tree tree = new Tree(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(false);

		this.treeViewer = new SortableTreeViewer(tree);
		createColumns();
		this.treeViewer.setAutoExpandLevel(2);
		this.treeViewer.setContentProvider(new EpisodeViewerContentProvider());
		this.treeViewer.setInput(PlatformUI.getWorkbench().getService(
				ICodeService.class));
	}

	private void createColumns() {
		TreeViewerColumn episodeColumn = treeViewer
				.createColumn("Episode", 150);
		episodeColumn.setLabelProvider(new ColumnLabelProvider() {

			ILabelProvider labelProvider = new GTCodeableProvider()
					.getLabelProvider();

			@Override
			public String getText(Object element) {
				if (ID.class.isInstance(element)) {
					ID id = (ID) element;
					return id.toString();
				}
				if (Fingerprint.class.isInstance(element)) {
					Fingerprint fingerprint = (Fingerprint) element;
					return fingerprint.toString();
				}
				if (IEpisode.class.isInstance(element)) {
					return labelProvider.getText(element);
				}
				return "ERROR";
			}

			@Override
			public Image getImage(Object element) {
				if (ID.class.isInstance(element)) {
					return de.fu_berlin.imp.seqan.usability_analyzer.core.ui.ImageManager.ID;
				}
				if (Fingerprint.class.isInstance(element)) {
					return de.fu_berlin.imp.seqan.usability_analyzer.core.ui.ImageManager.FINGERPRINT;
				}
				if (IEpisode.class.isInstance(element)) {
					return labelProvider.getImage(element);
				}
				return null;
			}
		});
		episodeColumn.setEditingSupport(new EpisodeEditingSupport(treeViewer));
		TreeViewerEditor.create(treeViewer,
				new ColumnViewerEditorActivationStrategy(treeViewer) {
					protected boolean isEditorActivationEvent(
							ColumnViewerEditorActivationEvent event) {
						return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
					}
				}, TreeViewerEditor.DEFAULT);
		treeViewer.createColumn("Start", 170).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (IEpisode.class.isInstance(element)) {
							IEpisode episode = (IEpisode) element;
							return episode.getStart() != null ? preferenceUtil
									.getDateFormat().format(
											episode.getStart().getDate())
									: "-∞";
						}
						return "";
					}
				});
		treeViewer.createColumn("End", 170).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (IEpisode.class.isInstance(element)) {
							IEpisode episode = (IEpisode) element;
							return episode.getEnd() != null ? preferenceUtil
									.getDateFormat().format(
											episode.getEnd().getDate()) : "+∞";
						}
						return "";
					}
				});
		treeViewer.createColumn("Date Created", 170).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (IEpisode.class.isInstance(element)) {
							IEpisode episode = (IEpisode) element;
							return preferenceUtil.getDateFormat().format(
									episode.getCreation().getDate());
						}
						return "";
					}
				});
	}

	public Control getControl() {
		if (this.treeViewer != null)
			return this.treeViewer.getTree();
		return null;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.treeViewer.addSelectionChangedListener(listener);
	}

	@Override
	public ISelection getSelection() {
		return this.treeViewer.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		this.treeViewer.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		this.treeViewer.setSelection(selection);
	}

	public AbstractTreeViewer getViewer() {
		return treeViewer;
	}

}
