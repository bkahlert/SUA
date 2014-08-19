package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonOption;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonSize;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonStyle;
import com.bkahlert.nebula.widgets.itemlist.ItemList;
import com.bkahlert.nebula.widgets.itemlist.ItemList.ItemListAdapter;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.NominalDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.apiua.groundedtheory.views.DimensionView.DimensionType;

/**
 * Displays and provides editing capabilities for {@link IDimension} for the
 * given objects.
 * 
 * @author bkahlert
 * 
 */
public class DimensionComposite extends Composite {
	private static final Logger LOGGER = Logger
			.getLogger(DimensionComposite.class);

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private URI loaded = null;

	private final Combo typeCombo;
	private final ItemList valueList;

	private DimensionType dimensionType = DimensionType.None;
	private List<String> values = new ArrayList<String>();

	public DimensionComposite(Composite parent, int style) {
		super(parent, style);

		this.setLayout(GridLayoutFactory.fillDefaults().spacing(5, 5).create());

		this.typeCombo = new Combo(this, SWT.DROP_DOWN | SWT.BORDER
				| SWT.READ_ONLY);
		this.typeCombo.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());
		for (DimensionType dimensionType : DimensionType.values()) {
			this.typeCombo.add(dimensionType.toString());
		}
		this.typeCombo.select(0);
		this.typeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DimensionComposite.this.dimensionType = DimensionType
						.valueOf(DimensionComposite.this.typeCombo.getText());
				try {
					DimensionComposite.this.save();
				} catch (CodeStoreWriteException e1) {
					LOGGER.error(e);
				}
				DimensionComposite.this.refresh();
			}
		});

		this.valueList = new ItemList(this, SWT.NONE);
		this.valueList.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());
		this.valueList.setMargin(5);
		this.valueList.setSpacing(5);
		this.refresh();
		this.valueList.addListener(new ItemListAdapter() {
			@Override
			public void itemClicked(String key, int i) {
				if (key.equals("add")) {
					try {
						DimensionRenameDialog renameDialog = new DimensionRenameDialog(
								DimensionComposite.this.getShell(), "");
						renameDialog.create();
						if (renameDialog.open() == Window.OK) {
							DimensionComposite.this.values.add(renameDialog
									.getTitle());
							DimensionComposite.this.refresh();
						}
					} catch (Exception e) {
						LOGGER.error("Error creating nominal value", e);
					}
				} else {
					try {
						int idx = Integer.valueOf(key);
						switch (i) {
						case 0:
							break;
						case 1:
							DimensionRenameDialog renameDialog = new DimensionRenameDialog(
									DimensionComposite.this.getShell(),
									DimensionComposite.this.values.get(idx));
							renameDialog.create();
							if (renameDialog.open() == Window.OK) {
								DimensionComposite.this.values.set(idx,
										renameDialog.getTitle());
								DimensionComposite.this.refresh();
							}
							break;
						case 2:
							DimensionComposite.this.values.remove(idx);
							DimensionComposite.this.refresh();
							break;
						}
					} catch (Exception e) {
						LOGGER.error("Error renaming nominal value", e);
					}
				}
			}
		});
	}

	@Override
	public void dispose() {
		try {
			this.save();
		} catch (CodeStoreWriteException e) {
			LOGGER.error(e);
		}
		super.dispose();
	}

	public void load(URI uri) throws CodeStoreWriteException {
		this.save();

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
			this.dimensionType = DimensionType.None;
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
			if (code == null) {
				return; // deleted
			}
		} catch (Exception e) {
			LOGGER.error("Error", e);
		}

		switch (this.dimensionType) {
		case None:
			CODE_SERVICE.setDimension(code, null);
			break;
		case Nominal:
			NominalDimension dimension = new NominalDimension(this.values);
			CODE_SERVICE.setDimension(code, dimension);
			break;
		default:
			LOGGER.error("Unknown dimension type selected");
			break;
		}
	}

	private void refresh() {
		this.typeCombo.select(this.dimensionType.ordinal());
		DimensionComposite.this.valueList.clear();

		switch (DimensionComposite.this.dimensionType) {
		case None:
			DimensionComposite.this.values.clear();
			DimensionComposite.this.valueList.clear();
			break;
		case Nominal:
			for (int i = 0; i < DimensionComposite.this.values.size(); i++) {
				DimensionComposite.this.valueList.addItem(i + "",
						DimensionComposite.this.values.get(i),
						ButtonOption.PRIMARY, ButtonSize.EXTRA_SMALL,
						ButtonStyle.HORIZONTAL, Arrays.asList("↩", "⌫"));
			}
			DimensionComposite.this.valueList.addItem("add", "Add Nominal",
					ButtonOption.PRIMARY, ButtonSize.EXTRA_SMALL,
					ButtonStyle.HORIZONTAL, null);
			try {
				DimensionComposite.this.save();
			} catch (CodeStoreWriteException e) {
				LOGGER.error(e);
			}
			break;
		default:
			LOGGER.error("Unknown dimension type selected");
			break;
		}
	}

	@Override
	public boolean setFocus() {
		return this.typeCombo.setFocus();
	}
}