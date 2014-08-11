package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.utils.selection.retriever.ISelectionRetriever;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonOption;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonSize;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonStyle;
import com.bkahlert.nebula.widgets.itemlist.ItemList;
import com.bkahlert.nebula.widgets.itemlist.ItemList.ItemListAdapter;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.NominalDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.ViewerURI;

public class DimensionView extends ViewPart {

	private static final Logger LOGGER = Logger.getLogger(DimensionView.class);

	public static final String ID = "de.fu_berlin.imp.apiua.groundedtheory.views.DimensionView";

	private final ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	private final ISelectionRetriever<URI> uriRetriever = SelectionRetrieverFactory
			.getSelectionRetriever(URI.class);

	private URI loaded = null;

	private final ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part == DimensionView.this) {
				return;
			}
			List<URI> uris = DimensionView.this.uriRetriever.getSelection();
			try {
				if (uris.size() > 0) {
					DimensionView.this.load(uris.get(0));
				} else {
					DimensionView.this.load(null);
				}
			} catch (CodeStoreWriteException e) {
				LOGGER.error(e);
			}
		}
	};

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	public static enum DimensionType {
		None, Nominal;

		@Override
		public String toString() {
			return super.toString();
		};
	}

	private String initPartName;
	private Combo typeCombo;
	private ItemList valueList;

	private DimensionType dimensionType = DimensionType.None;
	private List<String> values = new ArrayList<String>();

	public DimensionView() {
		SelectionUtils.getSelectionService().addSelectionListener(
				this.selectionListener);
	}

	@Override
	public void dispose() {
		try {
			this.save();
		} catch (CodeStoreWriteException e) {
			LOGGER.error(e);
		}
		SelectionUtils.getSelectionService().removeSelectionListener(
				this.selectionListener);
		super.dispose();
	}

	private void refreshPartName(List<URI> uris) {
		if (uris == null) {
			uris = new LinkedList<URI>();
		}

		if (this.initPartName == null) {
			this.initPartName = this.getPartName();
		}

		List<String> labels = new ArrayList<String>(uris.size());
		for (URI uri : uris) {
			ILabelProvider lp = this.labelProviderService.getLabelProvider(uri);
			try {
				labels.add(lp.getText(uri));
			} catch (Exception e) {
				if (uri instanceof ViewerURI) {
					labels.add("-");
				} else {
					LOGGER.error("Error getting label for " + uri);
					labels.add("ERROR");
				}
			}
		}

		String label = StringUtils.join(labels, ", ");
		this.setPartName(this.initPartName + ": " + label);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().spacing(5, 5)
				.margins(5, 5).create());

		this.typeCombo = new Combo(parent, SWT.DROP_DOWN | SWT.BORDER
				| SWT.READ_ONLY);
		this.typeCombo.setLayoutData(GridDataFactory.fillDefaults().create());
		for (DimensionType dimensionType : DimensionType.values()) {
			this.typeCombo.add(dimensionType.toString());
		}
		this.typeCombo.select(0);
		this.typeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DimensionView.this.dimensionType = DimensionType
						.valueOf(DimensionView.this.typeCombo.getText());
				try {
					DimensionView.this.save();
				} catch (CodeStoreWriteException e1) {
					LOGGER.error(e);
				}
				DimensionView.this.refresh();
			}
		});

		this.valueList = new ItemList(parent, SWT.NONE);
		this.valueList.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());
		this.valueList.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		this.valueList.setSpacing(5);
		this.refresh();
		this.valueList.addListener(new ItemListAdapter() {
			@Override
			public void itemClicked(String key, int i) {
				if (key.equals("add")) {
					try {
						DimensionViewRenameDialog renameDialog = new DimensionViewRenameDialog(
								DimensionView.this.getViewSite().getShell(), "");
						renameDialog.create();
						if (renameDialog.open() == Window.OK) {
							DimensionView.this.values.add(renameDialog
									.getTitle());
							DimensionView.this.refresh();
						}
					} catch (Exception e) {
						AxialCodingView.LOGGER.error(
								"Error creating nominal value", e);
					}
				} else {
					try {
						int idx = Integer.valueOf(key);
						switch (i) {
						case 0:
							break;
						case 1:
							DimensionViewRenameDialog renameDialog = new DimensionViewRenameDialog(
									DimensionView.this.getViewSite().getShell(),
									DimensionView.this.values.get(idx));
							renameDialog.create();
							if (renameDialog.open() == Window.OK) {
								DimensionView.this.values.set(idx,
										renameDialog.getTitle());
								DimensionView.this.refresh();
							}
							break;
						case 2:
							DimensionView.this.values.remove(idx);
							DimensionView.this.refresh();
							break;
						}
					} catch (Exception e) {
						AxialCodingView.LOGGER.error(
								"Error renaming nominal value", e);
					}
				}
			}
		});

		// new ContextMenu(this.episodeViewer.getViewer(), this.getSite()) {
		// @Override
		// protected String getDefaultCommandID() {
		// return null;
		// }
		// };

	}

	private void load(URI uri) throws CodeStoreWriteException {
		this.save();

		DimensionView.this.refreshPartName(uri != null ? Arrays.asList(uri)
				: null);

		if (uri != null && LocatorService.INSTANCE.getType(uri) == ICode.class) {
			ICode code = null;
			try {
				code = LocatorService.INSTANCE.resolve(uri, ICode.class, null)
						.get();
			} catch (Exception e) {
				LOGGER.error("Error", e);
			}

			IDimension dimension = CODE_SERVICE.getDimension(code.getUri());
			if (dimension == null) {
				this.dimensionType = DimensionType.None;
			} else if (dimension.getClass().equals(NominalDimension.class)) {
				this.dimensionType = DimensionType.Nominal;
				this.values = ((NominalDimension) dimension)
						.getPossibleValues();
			} else {
				LOGGER.error("Unknown dimension type loaded");
			}

			this.typeCombo.setEnabled(true);
			this.valueList.setEnabled(true);
			this.loaded = uri;
		} else {
			DimensionView.this.dimensionType = DimensionType.None;
			this.typeCombo.setEnabled(false);
			this.valueList.setEnabled(false);
			this.loaded = null;
		}

		this.refresh();
	}

	private void save() throws CodeStoreWriteException {
		URI uri = this.loaded;
		if (uri == null) {
			return;
		}

		ICode code = null;
		try {
			code = LocatorService.INSTANCE.resolve(uri, ICode.class, null)
					.get();
			Assert.isNotNull(code, uri
					+ " could be loaded but now cannot be resolved anymore.");
		} catch (Exception e) {
			LOGGER.error("Error", e);
		}

		switch (DimensionView.this.dimensionType) {
		case None:
			CODE_SERVICE.setDimension(code, null);
			break;
		case Nominal:
			NominalDimension dimension = new NominalDimension(
					DimensionView.this.values);
			CODE_SERVICE.setDimension(code, dimension);
			break;
		default:
			LOGGER.error("Unknown dimension type selected");
			break;
		}
	}

	private void refresh() {
		DimensionView.this.typeCombo.select(DimensionView.this.dimensionType
				.ordinal());
		ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				DimensionView.this.valueList.clear().get();

				switch (DimensionView.this.dimensionType) {
				case None:
					DimensionView.this.values.clear();
					DimensionView.this.valueList.clear();
					break;
				case Nominal:
					for (int i = 0; i < DimensionView.this.values.size(); i++) {
						DimensionView.this.valueList.addItem(i + "",
								DimensionView.this.values.get(i),
								ButtonOption.PRIMARY, ButtonSize.EXTRA_SMALL,
								ButtonStyle.HORIZONTAL, Arrays.asList("↩", "⌫"));
					}
					DimensionView.this.valueList.addItem("add", "Add Nominal",
							ButtonOption.PRIMARY, ButtonSize.EXTRA_SMALL,
							ButtonStyle.HORIZONTAL, null);
					DimensionView.this.save();
					break;
				default:
					LOGGER.error("Unknown dimension type selected");
					break;
				}

				return null;
			}
		});
	}

	@Override
	public void setFocus() {
		this.typeCombo.setFocus();
	}

}
