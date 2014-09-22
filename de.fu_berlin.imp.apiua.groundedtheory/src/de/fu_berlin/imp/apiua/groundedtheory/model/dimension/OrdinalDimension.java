package de.fu_berlin.imp.apiua.groundedtheory.model.dimension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.nebula.widgets.scale.IScale;
import com.bkahlert.nebula.widgets.scale.OrdinalScale;
import com.bkahlert.nebula.widgets.scale.OrdinalScale.EditType;
import com.bkahlert.nebula.widgets.scale.OrdinalScale.IOrdinalScaleListener;

/**
 * A {@link IDimension} with nominals.
 * 
 * @author bkahlert
 * 
 */
public class OrdinalDimension implements IDimension {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(OrdinalDimension.class);

	private List<String> ordinals;

	public OrdinalDimension(List<String> ordinals) {
		this.ordinals = ordinals != null ? new ArrayList<String>(ordinals)
				: new ArrayList<String>();
	}

	public OrdinalDimension(String... ordinals) {
		this(ordinals != null ? Arrays.asList(ordinals) : null);
	}

	public OrdinalDimension() {
		this(new ArrayList<String>());
	}

	@Override
	public boolean isLegal(String value) {
		return value == null || this.ordinals.contains(value);
	}

	@Override
	public String represent() {
		StringBuilder sb = new StringBuilder("");
		switch (this.ordinals.size()) {
		case 0:
			sb.append("-");
			break;
		case 1:
			sb.append(this.ordinals.get(0));
			break;
		case 2:
			sb.append(this.ordinals.get(0));
			sb.append(", ");
			sb.append(this.ordinals.get(1));
			break;
		case 3:
			sb.append(this.ordinals.get(0));
			sb.append(", ");
			sb.append(this.ordinals.get(1));
			sb.append(", ");
			sb.append(this.ordinals.get(2));
			break;
		default:
			sb.append(this.ordinals.get(0));
			sb.append(" ... ");
			sb.append(this.ordinals.get(this.ordinals.size() - 1));
		}
		return sb.toString();
	}

	@Override
	public Control createEditControl(final Composite parent,
			final IDimensionListener dimensionListener) {
		final OrdinalScale ordinalScale = new OrdinalScale(parent, SWT.NONE,
				EditType.CHANGE_ORDER);
		ordinalScale.setOrdinals(this.ordinals.toArray(new String[0]));
		ordinalScale.setMargin(5);
		ordinalScale.addListener(new IOrdinalScaleListener() {
			@Override
			public void valueChanged(String oldValue, String newValue) {
			}

			@Override
			public void ordinalRenamed(String oldName, String newName) {
				if (dimensionListener != null) {
					OrdinalDimension.this.ordinals = Arrays.asList(ordinalScale
							.getOrdinals());
					dimensionListener.dimensionChanged(OrdinalDimension.this);
				}
			}

			@Override
			public void ordinalRemoved(String ordinal) {
				if (dimensionListener != null) {
					OrdinalDimension.this.ordinals = Arrays.asList(ordinalScale
							.getOrdinals());
					dimensionListener.dimensionChanged(OrdinalDimension.this);
				}
			}

			@Override
			public void ordinalAdded(String newOrdinal) {
				if (dimensionListener != null) {
					OrdinalDimension.this.ordinals = Arrays.asList(ordinalScale
							.getOrdinals());
					dimensionListener.dimensionChanged(OrdinalDimension.this);
				}
			}

			@Override
			public void orderChanged(String[] oldOrdinals, String[] newOrdinals) {
				if (dimensionListener != null) {
					OrdinalDimension.this.ordinals = Arrays.asList(ordinalScale
							.getOrdinals());
					dimensionListener.dimensionChanged(OrdinalDimension.this);
				}
			}
		});
		return ordinalScale;
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
							OrdinalDimension.this, combo.getText());
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
		for (String ordinal : this.ordinals) {
			combo.add(ordinal);
		}
		combo.select(this.ordinals.indexOf(value) + 1);
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
