package de.fu_berlin.imp.apiua.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonOption;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonSize;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonStyle;
import com.bkahlert.nebula.widgets.itemlist.ItemList;
import com.bkahlert.nebula.widgets.itemlist.ItemListViewer;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProvider;

public class SelectionHistoryContribution extends
		WorkbenchWindowControlContribution {

	public static final int NUM_ENTRIES = 15;

	private static final Logger LOGGER = Logger
			.getLogger(SelectionHistoryContribution.class);

	private List<URI> history = new LinkedList<>();

	private ISelectionListener selectionListener = (part, selection) -> {
		List<URI> uris = SelectionUtils
				.getAdaptableObjects(selection, URI.class).stream()
				.filter(uri1 -> uri1.getClass() == URI.class) // don't allow
																// sub classes
																// (like
																// ViewerURI)
				.collect(Collectors.toList());
		for (URI uri2 : uris) {
			SelectionHistoryContribution.this.history.remove(uri2);
			SelectionHistoryContribution.this.history.add(0, uri2);
		}
		if (SelectionHistoryContribution.this.history.size() > NUM_ENTRIES) {
			SelectionHistoryContribution.this.history = SelectionHistoryContribution.this.history
					.subList(0, NUM_ENTRIES);
		}
		new SUACorePreferenceUtil()
				.setSelectionHistory(SelectionHistoryContribution.this.history);
		if (SelectionHistoryContribution.this.itemListViewer != null) {
			SelectionHistoryContribution.this.itemListViewer
					.setInput(SelectionHistoryContribution.this.history);
			SelectionHistoryContribution.this.itemListViewer.refresh();
			Composite parent = SelectionHistoryContribution.this.itemListViewer
					.getControl().getParent().getParent();
			parent.layout(true, true);
		}
	};

	private ItemListViewer itemListViewer = null;

	public SelectionHistoryContribution() {
		this.init();
	}

	public SelectionHistoryContribution(String id) {
		super(id);
		this.init();
	}

	private void init() {
		SelectionUtils.getSelectionService().addSelectionListener(
				this.selectionListener);
		this.history = new SUACorePreferenceUtil().getSelectionHistory();
	}

	@Override
	public void dispose() {
		new SUACorePreferenceUtil().setSelectionHistory(this.history);
		SelectionUtils.getSelectionService().addSelectionListener(
				this.selectionListener);
		super.dispose();
	}

	@Override
	protected Control createControl(Composite parent) {
		ItemList itemList = new ItemList(parent, SWT.HORIZONTAL) {
			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				Point size = super.computeSize(wHint, hHint, changed);
				size.x = 1200;
				return size;
			};
		};
		itemList.setMargin(0);
		itemList.setSpacing(2);

		this.itemListViewer = new ItemListViewer(itemList);
		this.itemListViewer
				.setContentProvider(new IStructuredContentProvider() {
					@Override
					public void inputChanged(Viewer viewer, Object oldInput,
							Object newInput) {
					}

					@SuppressWarnings("unchecked")
					@Override
					public Object[] getElements(Object inputElement) {
						List<Object> objects = new ArrayList<>();
						if (inputElement instanceof List<?>) {
							List<URI> uris = (List<URI>) inputElement;
							int i = 0;
							for (URI uri : uris) {
								objects.add(new Pair<Integer, URI>(i, uri));
								i++;
							}
						}
						return objects.toArray();
					}

					@Override
					public void dispose() {
					}
				});
		this.itemListViewer
				.setLabelProvider(new ItemListViewer.ButtonLabelProvider() {
					ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
							.getWorkbench().getService(
									ILabelProviderService.class);

					@Override
					public String getText(Object element) {
						@SuppressWarnings("unchecked")
						URI uri = ((Pair<Integer, URI>) element).getSecond();
						ILabelProvider lp = this.labelProviderService
								.getLabelProvider(uri);
						try {
							if (lp != null) {
								String text = lp.getText(uri);
								Image image = lp.getImage(uri);
								// if(true) return text;
								return "<span class='no_click'>"
										+ (image != null ? "<img src='"
												+ ImageUtils
														.createUriFromImage(image)
												+ "' class='no_click'/> "
												: "")
										+ "<span class='no_click' style='display: inline-block; position: relative; top: 1px'>"
										+ text + "</span></span>";
							} else {
								return uri.toString();
							}
						} catch (Exception e) {
							LOGGER.error(e);
							return "error (" + uri.toString() + ")";
						}
					}

					@Override
					public ButtonOption getOption(Object object) {
						return null;
					}

					@Override
					public RGB getColor(Object object) {
						@SuppressWarnings("unchecked")
						Pair<Integer, Object> element = (Pair<Integer, Object>) object;
						RGB color = new RGB(RGB.INFO);

						int stepDifferenceToFirstElement = 15;
						double steps = (1.0 / (NUM_ENTRIES + stepDifferenceToFirstElement));
						if (element.getFirst() > 0) {
							double alpha = 1.0
									- (element.getFirst() + stepDifferenceToFirstElement)
									* steps;
							color.setAlpha(alpha);
						} else {
							color.setAlpha(1.0);
						}
						return color;
					}

					@Override
					public ButtonSize getSize(Object object) {
						return ButtonSize.EXTRA_SMALL;
					}

					@Override
					public ButtonStyle getStyle(Object object) {
						return ButtonStyle.HORIZONTAL;
					}
				});

		this.itemListViewer
				.addSelectionChangedListener(event -> {
					ISelection selection = event.getSelection();
					List<Pair<Integer, URI>> pairs = SelectionUtils
							.getAdaptableObjects(selection, Pair.class);
					if (pairs.size() > 0) {
						URI uri = pairs.get(0).getSecond();
						final Clipboard cb = new Clipboard(Display.getCurrent());
						TextTransfer transfer = TextTransfer.getInstance();
						cb.setContents(new Object[] { uri.toString() },
								new Transfer[] { transfer });

						itemList.run("var old = $('body').html(); $('body').fadeOut(100).queue(function(n) { $(this).html('"
								+ "<p class=\"text-success\" style=\"margin-left: 1em;\">"
								+ uri
								+ " successfully copied</p>"
								+ " clicked'); n(); }).fadeIn(100).delay(500).fadeOut(100).queue(function(n) { $(this).html(old); n(); }).fadeIn()");
					}
				});
		return itemList;
	}

}
