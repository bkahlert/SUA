package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.DNDUtils;
import com.bkahlert.nebula.utils.DNDUtils.Oracle;
import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.DistributionUtils.RelativeWidth;
import com.bkahlert.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.EpisodeRenderer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.GTLabelProvider;

public class EpisodeViewer extends Composite implements ISelectionProvider {

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(EpisodeViewer.class);
	private final SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	private SortableTreeViewer treeViewer;

	public EpisodeViewer(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		Tree tree = new Tree(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		tree.setHeaderVisible(false);
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

		DNDUtils.addLocalDragSupport(this.treeViewer, new Oracle() {
			@Override
			public boolean allowDND() {
				return EpisodeViewer.this.getControl().getData(
						EpisodeRenderer.CONTROL_DATA_STRING) == null;
			}
		}, URI.class);
	}

	private void createColumns() {
		TreeViewerColumn episodeColumn = this.treeViewer.createColumn(
				"Episode", new RelativeWidth(1.0, 150));
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
				new AbsoluteWidth(0));
		startColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(
				new ILabelProviderService.StyledLabelProvider() {
					@Override
					public StyledString getStyledText(URI uri) throws Exception {
						ILocatable locatable = EpisodeViewer.this.locatorService
								.resolve(uri, null).get();
						if (IEpisode.class.isInstance(locatable)) {
							IEpisode episode = (IEpisode) locatable;
							return new StyledString(
									episode.getStart() != null ? EpisodeViewer.this.preferenceUtil
											.getDateFormat().format(
													episode.getStart()
															.getDate()) : "-∞");
						}
						return new StyledString();
					}
				}));
		startColumn.setEditingSupport(new EpisodeEditingSupport(
				this.treeViewer, EpisodeEditingSupport.Field.STARTDATE));

		TreeViewerColumn endColumn = this.treeViewer.createColumn("End",
				new AbsoluteWidth(0));
		endColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(
				new ILabelProviderService.StyledLabelProvider() {
					@Override
					public StyledString getStyledText(URI uri) throws Exception {
						ILocatable locatable = EpisodeViewer.this.locatorService
								.resolve(uri, null).get();
						if (IEpisode.class.isInstance(locatable)) {
							IEpisode episode = (IEpisode) locatable;
							return new StyledString(
									episode.getEnd() != null ? EpisodeViewer.this.preferenceUtil
											.getDateFormat().format(
													episode.getEnd().getDate())
											: "+∞");
						}
						return new StyledString();
					}
				}));
		endColumn.setEditingSupport(new EpisodeEditingSupport(this.treeViewer,
				EpisodeEditingSupport.Field.ENDDATE));

		this.treeViewer
				.createColumn("Date Created", new AbsoluteWidth(0))
				.setLabelProvider(
						new DelegatingStyledCellLabelProvider(
								new ILabelProviderService.StyledLabelProvider() {
									@Override
									public StyledString getStyledText(URI uri)
											throws Exception {
										ILocatable locatable = EpisodeViewer.this.locatorService
												.resolve(uri, null).get();
										if (IEpisode.class
												.isInstance(locatable)) {
											IEpisode episode = (IEpisode) locatable;
											return new StyledString(
													EpisodeViewer.this.preferenceUtil
															.getDateFormat()
															.format(episode
																	.getCreation()
																	.getDate()));
										}
										return new StyledString();
									}
								}));
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
