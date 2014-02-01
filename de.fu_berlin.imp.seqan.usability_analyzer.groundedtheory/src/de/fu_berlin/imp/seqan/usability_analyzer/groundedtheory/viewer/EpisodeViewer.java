package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.net.URI;

import org.apache.log4j.Logger;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.GTLabelProvider;

public class EpisodeViewer extends Composite implements ISelectionProvider {

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(EpisodeViewer.class);
	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

	private ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	private SortableTreeViewer treeViewer;

	public EpisodeViewer(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		Tree tree = new Tree(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(false);

		this.treeViewer = new SortableTreeViewer(tree);
		this.createColumns();
		this.treeViewer.sort(0);
		this.treeViewer.setAutoExpandLevel(2);
		this.treeViewer.setContentProvider(new EpisodeViewerContentProvider());
		this.treeViewer.setInput(PlatformUI.getWorkbench().getService(
				ICodeService.class));
		TreeViewerEditor.create(this.treeViewer,
				new ColumnViewerEditorActivationStrategy(this.treeViewer) {
					@Override
					protected boolean isEditorActivationEvent(
							ColumnViewerEditorActivationEvent event) {
						return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
								|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION;
					}
				}, TreeViewerEditor.DEFAULT);

		int operations = DND.DROP_LINK;
		Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };
		this.treeViewer.addDragSupport(operations, transferTypes,
				new DragSourceListener() {
					@Override
					public void dragStart(DragSourceEvent event) {
						if (((TreeSelection) EpisodeViewer.this.treeViewer
								.getSelection()).size() > 0) {
							LocalSelectionTransfer.getTransfer().setSelection(
									EpisodeViewer.this.treeViewer
											.getSelection());
							LocalSelectionTransfer.getTransfer()
									.setSelectionSetTime(
											event.time & 0xFFFFFFFFL);
							event.doit = true;
						} else {
							event.doit = false;
						}
					};

					@Override
					public void dragSetData(DragSourceEvent event) {
						if (LocalSelectionTransfer.getTransfer()
								.isSupportedType(event.dataType)) {
							event.data = LocalSelectionTransfer.getTransfer()
									.getSelection();
						}
					}

					@Override
					public void dragFinished(DragSourceEvent event) {
						LocalSelectionTransfer.getTransfer().setSelection(null);
						LocalSelectionTransfer.getTransfer()
								.setSelectionSetTime(0);
					}
				});
	}

	private void createColumns() {
		TreeViewerColumn episodeColumn = this.treeViewer.createColumn(
				"Episode", 150);
		episodeColumn.setLabelProvider(new ColumnLabelProvider() {

			ILabelProvider labelProvider = new GTLabelProvider();

			@Override
			public String getText(Object element) {
				if (IIdentifier.class.isInstance(element)) {
					IIdentifier identifier = (IIdentifier) element;
					return identifier.toString();
				}
				return this.labelProvider.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (ID.class.isInstance(element)) {
					return de.fu_berlin.imp.seqan.usability_analyzer.core.ui.ImageManager.ID;
				}
				if (Fingerprint.class.isInstance(element)) {
					return de.fu_berlin.imp.seqan.usability_analyzer.core.ui.ImageManager.FINGERPRINT;
				}
				return this.labelProvider.getImage(element);
			}
		});
		episodeColumn.setEditingSupport(new EpisodeEditingSupport(
				this.treeViewer, EpisodeEditingSupport.Field.NAME));

		TreeViewerColumn startColumn = this.treeViewer.createColumn("Start",
				170);
		startColumn
				.setLabelProvider(new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						ILocatable locatable = EpisodeViewer.this.locatorService
								.resolve(uri, null).get();
						if (IEpisode.class.isInstance(locatable)) {
							IEpisode episode = (IEpisode) locatable;
							return episode.getStart() != null ? EpisodeViewer.this.preferenceUtil
									.getDateFormat().format(
											episode.getStart().getDate())
									: "-∞";
						}
						return "";
					}
				});
		startColumn.setEditingSupport(new EpisodeEditingSupport(
				this.treeViewer, EpisodeEditingSupport.Field.STARTDATE));

		TreeViewerColumn endColumn = this.treeViewer.createColumn("End", 170);
		endColumn
				.setLabelProvider(new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						ILocatable locatable = EpisodeViewer.this.locatorService
								.resolve(uri, null).get();
						if (IEpisode.class.isInstance(locatable)) {
							IEpisode episode = (IEpisode) locatable;
							return episode.getEnd() != null ? EpisodeViewer.this.preferenceUtil
									.getDateFormat().format(
											episode.getEnd().getDate()) : "+∞";
						}
						return "";
					}
				});
		endColumn.setEditingSupport(new EpisodeEditingSupport(this.treeViewer,
				EpisodeEditingSupport.Field.ENDDATE));

		this.treeViewer.createColumn("Date Created", 170).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						ILocatable locatable = EpisodeViewer.this.locatorService
								.resolve(uri, null).get();
						if (IEpisode.class.isInstance(locatable)) {
							IEpisode episode = (IEpisode) locatable;
							return EpisodeViewer.this.preferenceUtil
									.getDateFormat().format(
											episode.getCreation().getDate());
						}
						return "";
					}
				});
	}

	public Control getControl() {
		if (this.treeViewer != null) {
			return this.treeViewer.getTree();
		}
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
		return this.treeViewer;
	}

}
