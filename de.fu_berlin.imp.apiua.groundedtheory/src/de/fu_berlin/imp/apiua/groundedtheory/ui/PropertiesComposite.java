package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonOption;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonSize;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonStyle;
import com.bkahlert.nebula.widgets.itemlist.ItemList;
import com.bkahlert.nebula.widgets.itemlist.ItemList.ItemListAdapter;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;

/**
 * Displays and provides editing capabilities for properties of the given
 * objects.
 * 
 * @author bkahlert
 * 
 */
public class PropertiesComposite extends Composite {
	private static final Logger LOGGER = Logger
			.getLogger(PropertiesComposite.class);

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private URI loaded = null;

	private final ItemList propertiesList;

	private List<ICode> properties = new ArrayList<ICode>();

	public PropertiesComposite(Composite parent, int style) {
		super(parent, style);

		this.setLayout(GridLayoutFactory.fillDefaults().spacing(5, 5).create());

		this.propertiesList = new ItemList(this, SWT.NONE);
		this.propertiesList.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());
		this.propertiesList.setMargin(5);
		this.propertiesList.setSpacing(5);
		this.refresh();
		this.propertiesList.addListener(new ItemListAdapter() {
			@Override
			public void itemClicked(String key, int i) {
				if (key.equals("add")) {
					try {
						PropertiesAddDialog propertiesAddDialog = new PropertiesAddDialog(
								PropertiesComposite.this.getShell(), null);
						propertiesAddDialog.create();
						if (propertiesAddDialog.open() == Window.OK) {
							List<ICode> newProperties = LocatorService.INSTANCE
									.resolve(
											propertiesAddDialog.getProperties(),
											ICode.class, null).get();
							PropertiesComposite.this.properties
									.addAll(newProperties);
							PropertiesComposite.this.refresh();
						}
					} catch (Exception e) {
						LOGGER.error("Error adding property", e);
					}
				} else {
					try {
						int idx = Integer.valueOf(key);
						switch (i) {
						case 0:
							break;
						case 1:
							PropertiesComposite.this.properties.remove(idx);
							PropertiesComposite.this.refresh();
							break;
						}
					} catch (Exception e) {
						LOGGER.error("Error removing property", e);
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
		boolean load = true;

		try {
			this.save();
		} catch (CodeStoreWriteException e) {
			MessageBox box = new MessageBox(this.getShell(), SWT.ICON_WARNING
					| SWT.YES | SWT.NO);
			box.setText("Cyclic Dependency");
			box.setMessage("Saving the changes would result in a cyclic dependency. Do you want to discard your changes?");
			load = box.open() == SWT.YES;
			LOGGER.error(e);
		}

		if (load) {
			if (uri != null
					&& LocatorService.INSTANCE.getType(uri) == ICode.class) {
				ICode code = null;
				try {
					code = LocatorService.INSTANCE.resolve(uri, ICode.class,
							null).get();
				} catch (Exception e) {
					LOGGER.error("Error", e);
				}

				this.properties = CODE_SERVICE.getProperties(code);
				this.propertiesList.setEnabled(true);
				this.loaded = uri;
			} else {
				this.properties = null;
				this.propertiesList.setEnabled(false);
				this.loaded = null;
			}

			this.refresh();
		}
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

		CODE_SERVICE.setProperties(code, this.properties);
	}

	private void refresh() {
		PropertiesComposite.this.propertiesList.clear();

		if (this.properties != null) {
			for (int i = 0; i < PropertiesComposite.this.properties.size(); i++) {
				PropertiesComposite.this.propertiesList
						.addItem(i + "", PropertiesComposite.this.properties
								.get(i).getCaption(), ButtonOption.PRIMARY,
								ButtonSize.EXTRA_SMALL, ButtonStyle.HORIZONTAL,
								Arrays.asList("⌫"));
			}
		}
		if (this.loaded != null) {
			PropertiesComposite.this.propertiesList.addItem("add",
					"Add Property", ButtonOption.PRIMARY,
					ButtonSize.EXTRA_SMALL, ButtonStyle.HORIZONTAL, null);
		}
		try {
			PropertiesComposite.this.save();
		} catch (CodeStoreWriteException e) {
			LOGGER.error(e);
		}
	}

	@Override
	public boolean setFocus() {
		return this.propertiesList.setFocus();
	}
}