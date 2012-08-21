package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
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
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTreeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.GTCodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.EpisodeRenderer.EpisodeColors;
import de.fu_berlin.inf.nebula.utils.PaintUtils;

public class EpisodeViewer extends Composite implements ISelectionProvider {

	private static Logger LOGGER = Logger.getLogger(EpisodeViewer.class);
	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

	private SortableTreeViewer treeViewer;

	public EpisodeViewer(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		Tree tree = new Tree(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(false);
		tree.addListener(SWT.PaintItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!(event.item instanceof TreeItem)
						|| !(event.item.getData() instanceof IEpisode))
					return;
				TreeItem item = (TreeItem) event.item;
				Rectangle bounds = item.getImageBounds(0);
				bounds.x -= bounds.width + 2;

				IEpisode episode = (IEpisode) item.getData();
				EpisodeColors info = new EpisodeColors(episode.getColor());
				event.gc.setAlpha(128);
				PaintUtils.drawRoundedRectangle(event.gc, bounds,
						info.getBackgroundColor(), info.getBorderColor());
			}
		});
		tree.addListener(SWT.MouseMove, new Listener() {
			private Cursor hand = new Cursor(Display.getCurrent(),
					SWT.CURSOR_HAND);

			@Override
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Tree))
					return;
				Tree tree = ((Tree) event.widget);
				TreeItem item = tree.getItem(new Point(event.getBounds().x,
						event.getBounds().y));
				if (item != null) {
					Rectangle bounds = item.getImageBounds(0);
					bounds.x -= bounds.width + 2;

					if (event.getBounds().x >= bounds.x
							&& event.getBounds().x <= bounds.x + bounds.width) {
						tree.setCursor(hand);
					} else {
						tree.setCursor(null);
					}
				}
			}
		});
		tree.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Tree))
					return;
				Tree tree = ((Tree) event.widget);
				if (tree.getCursor() != null) {
					ICommandService cmdService = (ICommandService) PlatformUI
							.getWorkbench().getService(ICommandService.class);
					Command cmd = cmdService
							.getCommand("de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.commands.recolorEpisode");
					try {
						cmd.executeWithChecks(new ExecutionEvent());
					} catch (Exception e) {
						LOGGER.error("Error recoloring " + event.item.getData());
					}
				}
			}
		});

		this.treeViewer = new SortableTreeViewer(tree);
		createColumns();
		this.treeViewer.sort(0);
		this.treeViewer.setAutoExpandLevel(2);
		this.treeViewer.setContentProvider(new EpisodeViewerContentProvider());
		this.treeViewer.setInput(PlatformUI.getWorkbench().getService(
				ICodeService.class));
		TreeViewerEditor.create(this.treeViewer,
				new ColumnViewerEditorActivationStrategy(this.treeViewer) {
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
					public void dragStart(DragSourceEvent event) {
						if (((TreeSelection) treeViewer.getSelection()).size() > 0) {
							LocalSelectionTransfer.getTransfer().setSelection(
									treeViewer.getSelection());
							LocalSelectionTransfer.getTransfer()
									.setSelectionSetTime(
											event.time & 0xFFFFFFFFL);
							event.doit = true;
						} else {
							event.doit = false;
						}
					};

					public void dragSetData(DragSourceEvent event) {
						if (LocalSelectionTransfer.getTransfer()
								.isSupportedType(event.dataType)) {
							event.data = LocalSelectionTransfer.getTransfer()
									.getSelection();
						}
					}

					public void dragFinished(DragSourceEvent event) {
						LocalSelectionTransfer.getTransfer().setSelection(null);
						LocalSelectionTransfer.getTransfer()
								.setSelectionSetTime(0);
					}
				});
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
		episodeColumn.setEditingSupport(new EpisodeEditingSupport(treeViewer,
				EpisodeEditingSupport.Field.NAME));

		TreeViewerColumn startColumn = treeViewer.createColumn("Start", 170);
		startColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (IEpisode.class.isInstance(element)) {
					IEpisode episode = (IEpisode) element;
					return episode.getStart() != null ? preferenceUtil
							.getDateFormat().format(
									episode.getStart().getDate()) : "-∞";
				}
				return "";
			}
		});
		startColumn.setEditingSupport(new EpisodeEditingSupport(treeViewer,
				EpisodeEditingSupport.Field.STARTDATE));

		TreeViewerColumn endColumn = treeViewer.createColumn("End", 170);
		endColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (IEpisode.class.isInstance(element)) {
					IEpisode episode = (IEpisode) element;
					return episode.getEnd() != null ? preferenceUtil
							.getDateFormat().format(episode.getEnd().getDate())
							: "+∞";
				}
				return "";
			}
		});
		endColumn.setEditingSupport(new EpisodeEditingSupport(treeViewer,
				EpisodeEditingSupport.Field.ENDDATE));

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
