package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IllegalDimensionValueException;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.NominalDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;

/**
 * Displays and provides editing capabilities for {@link IDimension} for the
 * given objects.
 * 
 * @author bkahlert
 * 
 */
public class DimensionValueComposite extends Composite {

	private static final String UNSET = "[unset]";

	private static final Logger LOGGER = Logger
			.getLogger(DimensionValueComposite.class);

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private URI loaded = null;
	private final Map<ICode, IDimension> dimensions = new HashMap<ICode, IDimension>();

	private final List<Label> labels = new LinkedList<Label>();
	private final List<Combo> values = new LinkedList<Combo>();

	private final CodeServiceAdapter codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void dimensionValueChanged(URI uri, String oldValue, String value) {
			// TODO Auto-generated method stub

		}

		@Override
		public void dimensionChanged(URI uri, IDimension oldDimension,
				IDimension newDimension) {
			// TODO Auto-generated method stub

		}
	};

	public DimensionValueComposite(Composite parent, int style) {
		super(parent, style);

		this.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		CODE_SERVICE.addCodeServiceListener(this.codeServiceListener);

	}

	@Override
	public void dispose() {
		CODE_SERVICE.removeCodeServiceListener(this.codeServiceListener);
		try {
			this.save();
		} catch (CodeStoreWriteException e) {
			LOGGER.error(e);
		}
		super.dispose();
	}

	public void load(URI uri) throws CodeServiceException {
		try {
			this.save();
		} catch (CodeStoreWriteException e) {
			throw new CodeServiceException(e);
		}

		this.loaded = uri;
		this.dimensions.clear();
		for (ICode code : CODE_SERVICE.getCodes(uri)) {
			IDimension dimension = CODE_SERVICE.getDimension(code.getUri());
			this.dimensions.put(code, dimension);
		}

		this.refresh();
	}

	private void save() throws CodeStoreWriteException {
		URI uri = this.loaded;
		if (uri == null) {
			return;
		}

		for (Entry<ICode, IDimension> dimension : this.dimensions.entrySet()) {
			if (dimension.getValue() == null) {
				continue;
			}
			for (Combo value : this.values) {
				if (value.getData() == dimension.getKey()) {
					try {
						String dimensionValue = value.getText();
						CODE_SERVICE.setDimensionValue(uri, dimension.getKey(),
								!dimensionValue.equals(UNSET) ? dimensionValue
										: null);
					} catch (IllegalDimensionValueException e) {
						LOGGER.error("Error saving value " + value.getText()
								+ " for " + uri + "'s " + dimension.getKey()
								+ " dimension", e);
					}
					break;
				}
			}
		}
	}

	private void refresh() {
		for (Label label : this.labels) {
			label.dispose();
		}
		this.labels.clear();
		for (Combo value : this.values) {
			value.dispose();
		}
		this.values.clear();

		for (Entry<ICode, IDimension> dimension : this.dimensions.entrySet()) {
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData(GridDataFactory.swtDefaults()
					.align(SWT.RIGHT, SWT.CENTER).create());
			label.setText(dimension.getKey().getCaption());
			this.labels.add(label);

			Combo value = new Combo(this, SWT.READ_ONLY);
			value.setLayoutData(GridDataFactory.swtDefaults()
					.align(SWT.FILL, SWT.CENTER).grab(true, true).create());
			value.setData(dimension.getKey());
			if (dimension.getValue() instanceof NominalDimension) {
				List<String> possibleValues = ((NominalDimension) dimension
						.getValue()).getPossibleValues();
				value.add(UNSET);
				for (String possibleValue : possibleValues) {
					value.add(possibleValue);
				}
				value.select(possibleValues.indexOf(CODE_SERVICE
						.getDimensionValue(this.loaded, dimension.getKey())) + 1);
			} else {
				value.setEnabled(false);
			}
			value.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						DimensionValueComposite.this.save();
					} catch (CodeStoreWriteException e1) {
						LOGGER.error(e1);
					}
				}
			});
			this.values.add(value);
		}

		this.getParent().layout();
	}

}