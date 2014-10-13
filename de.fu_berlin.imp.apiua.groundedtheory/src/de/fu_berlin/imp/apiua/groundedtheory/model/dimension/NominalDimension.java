package de.fu_berlin.imp.apiua.groundedtheory.model.dimension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.nebula.dialogs.RenameDialog;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonOption;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonSize;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonStyle;
import com.bkahlert.nebula.widgets.itemlist.ItemList;
import com.bkahlert.nebula.widgets.itemlist.ItemList.ItemListAdapter;
import com.bkahlert.nebula.widgets.scale.IScale;

/**
 * A {@link IDimension} with nominals.
 * 
 * @author bkahlert
 * 
 */
public class NominalDimension implements IDimension {

	private static final Logger LOGGER = Logger
			.getLogger(NominalDimension.class);

	private final List<String> possibleValues;

	public NominalDimension(List<String> possibleValues) {
		this.possibleValues = possibleValues != null ? new ArrayList<String>(
				possibleValues) : new ArrayList<String>();
	}

	public NominalDimension(String... possibleValues) {
		this(possibleValues != null ? Arrays.asList(possibleValues) : null);
	}

	public NominalDimension() {
		this(new ArrayList<String>());
	}

	@Override
	public boolean isLegal(String value) {
		return value == null || this.possibleValues.contains(value);
	}

	@Override
	public String represent() {
		StringBuilder sb = new StringBuilder("");
		switch (this.possibleValues.size()) {
		case 0:
			sb.append("-");
			break;
		case 1:
			sb.append(this.possibleValues.get(0));
			break;
		case 2:
			sb.append(this.possibleValues.get(0));
			sb.append(", ");
			sb.append(this.possibleValues.get(1));
			break;
		case 3:
			sb.append(this.possibleValues.get(0));
			sb.append(", ");
			sb.append(this.possibleValues.get(1));
			sb.append(", ");
			sb.append(this.possibleValues.get(2));
			break;
		default:
			sb.append(this.possibleValues.get(0));
			sb.append(", ..., ");
			sb.append(this.possibleValues.get(this.possibleValues.size() - 1));
		}
		return sb.toString();
	}

	@Override
	public Control createEditControl(final Composite parent,
			final IDimensionListener dimensionListener) {
		final ItemList valueList = new ItemList(parent, SWT.NONE);
		valueList.setMargin(5);
		valueList.setSpacing(5);
		this.refreshEditControl(valueList);
		valueList.addListener(new ItemListAdapter() {
			@Override
			public void itemClicked(String key, int i) {
				if (key.equals("add")) {
					try {
						RenameDialog renameDialog = new RenameDialog(parent
								.getShell(), "");
						renameDialog.create();
						if (renameDialog.open() == Window.OK) {
							NominalDimension.this.possibleValues
									.add(renameDialog.getCaption());
							if (dimensionListener != null) {
								dimensionListener
										.dimensionChanged(NominalDimension.this);
							}
							NominalDimension.this.refreshEditControl(valueList);
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
							RenameDialog renameDialog = new RenameDialog(parent
									.getShell(),
									NominalDimension.this.possibleValues
											.get(idx));
							renameDialog.create();
							if (renameDialog.open() == Window.OK) {
								NominalDimension.this.possibleValues.set(idx,
										renameDialog.getCaption());
								if (dimensionListener != null) {
									dimensionListener
											.dimensionChanged(NominalDimension.this);
								}
								NominalDimension.this
										.refreshEditControl(valueList);
							}
							break;
						case 2:
							NominalDimension.this.possibleValues.remove(idx);
							if (dimensionListener != null) {
								dimensionListener
										.dimensionChanged(NominalDimension.this);
							}
							NominalDimension.this.refreshEditControl(valueList);
							break;
						}
					} catch (Exception e) {
						LOGGER.error("Error renaming nominal value", e);
					}
				}
			}
		});
		return valueList;
	}

	private void refreshEditControl(Control control) {
		if (!ItemList.class.isInstance(control)) {
			throw new RuntimeException(Control.class.getSimpleName()
					+ "'s type is " + control.getClass().getSimpleName()
					+ " instead of " + ItemList.class.getSimpleName());
		}
		ItemList itemList = (ItemList) control;
		itemList.clear();
		for (int i = 0; i < this.possibleValues.size(); i++) {
			itemList.addItem(i + "", this.possibleValues.get(i),
					ButtonOption.DEFAULT, ButtonSize.EXTRA_SMALL,
					ButtonStyle.HORIZONTAL, Arrays.asList(
							"<small class=\"no_click\">↩</small>",
							"<small class=\"no_click\">⌫</small>"));
		}
		itemList.addItem("add", "Add Nominal", ButtonOption.DEFAULT,
				ButtonSize.EXTRA_SMALL, ButtonStyle.HORIZONTAL, null);
	}

	@Override
	public Control createValueEditControl(Composite parent,
			final IDimensionValueListener dimensionValueListener) {
		final Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (dimensionValueListener != null) {
					dimensionValueListener.dimensionValueChanged(
							NominalDimension.this, combo.getText());
				}
			}
		});
		return combo;
	}

	@Override
	public void setValueEditControlValue(Control control, String value) {
		if (!Combo.class.isInstance(control)) {
			throw new RuntimeException(Control.class.getSimpleName()
					+ "'s type is " + control.getClass().getSimpleName()
					+ " instead of " + Combo.class.getSimpleName());
		}
		Combo combo = (Combo) control;
		combo.add(IScale.UNSET_LABEL);
		for (String possibleValue : this.possibleValues) {
			combo.add(possibleValue);
		}
		combo.select(this.possibleValues.indexOf(value) + 1);
	}

	@Override
	public String getValueEditControlValue(Control control) {
		if (!Combo.class.isInstance(control)) {
			throw new RuntimeException(Control.class.getSimpleName()
					+ "'s type is " + control.getClass().getSimpleName()
					+ " instead of " + Combo.class.getSimpleName());
		}
		Combo combo = (Combo) control;
		return combo.getText().equals(IScale.UNSET_LABEL) ? null : combo
				.getText();
	}
}
