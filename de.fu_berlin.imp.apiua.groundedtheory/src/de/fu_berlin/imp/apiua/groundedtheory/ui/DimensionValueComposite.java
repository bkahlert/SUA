package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.IteratorUtils;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.Triple;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
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
	private static final ILabelProviderService LABEL_PROVIDER_SERVICE = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	private static final String EXPLANATION = "Code the selected object with a dimensionalized code to set a dimension value.";

	private URI loaded = null;
	private final List<Triple<Integer, ICode, IDimension>> dimensions = new LinkedList<Triple<Integer, ICode, IDimension>>();

	private final List<Control> labels = new LinkedList<Control>();
	private final List<Combo> values = new LinkedList<Combo>();

	private final CodeServiceAdapter codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void dimensionValueChanged(URI uri, String oldValue, String value) {
			DimensionValueComposite.this.refresh();
		}

		@Override
		public void dimensionChanged(URI uri, IDimension oldDimension,
				IDimension newDimension) {
			DimensionValueComposite.this.refresh();
		}

		@Override
		public void propertiesChanged(URI uri,
				java.util.List<URI> addedProperties,
				java.util.List<URI> removedProperties) {
			DimensionValueComposite.this.refresh();
		};
	};

	public DimensionValueComposite(Composite parent, int style) {
		super(parent, SWT.NONE);

		this.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.margins(0, 0).numColumns(2).create());

		CODE_SERVICE.addCodeServiceListener(this.codeServiceListener);

		try {
			this.load(null);
		} catch (CodeServiceException e) {
			LOGGER.error(e);
		}
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
			for (Pair<Integer, ICode> property : IteratorUtils.bfs(code,
					new IConverter<ICode, ICode[]>() {
						@Override
						public ICode[] convert(ICode property) {
							return CODE_SERVICE.getProperties(property)
									.toArray(new ICode[0]);
						}
					})) {
				IDimension dimension = CODE_SERVICE.getDimension(property
						.getSecond().getUri());
				this.dimensions.add(new Triple<Integer, ICode, IDimension>(
						property.getFirst(), property.getSecond(), dimension));
			}
		}

		this.refresh();
	}

	private void save() throws CodeStoreWriteException {
		URI uri = this.loaded;
		if (uri == null) {
			return;
		}

		for (Triple<Integer, ICode, IDimension> dimension : this.dimensions) {
			if (dimension.getThird() == null) {
				continue;
			}
			for (Combo value : this.values) {
				if (value.getData() == dimension.getSecond()) {
					try {
						String dimensionValue = value.getText();
						CODE_SERVICE.setDimensionValue(uri, dimension
								.getSecond(),
								!dimensionValue.equals(UNSET) ? dimensionValue
										: null);
					} catch (IllegalDimensionValueException e) {
						LOGGER.error("Error saving value " + value.getText()
								+ " for " + uri + "'s " + dimension.getSecond()
								+ " dimension", e);
					}
					break;
				}
			}
		}
	}

	private void refresh() {
		for (Control label : this.labels) {
			label.dispose();
		}
		this.labels.clear();
		for (Combo value : this.values) {
			value.dispose();
		}
		this.values.clear();

		if (this.dimensions.size() == 0) {
			Label label = new Label(this, SWT.WRAP);
			label.setText(EXPLANATION);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
					2));
			this.labels.add(label);
		} else {
			for (Triple<Integer, ICode, IDimension> dimension : this.dimensions) {
				int depth = dimension.getFirst();
				ICode code = dimension.getSecond();
				IDimension dim = dimension.getThird();

				Image image = null;
				try {
					image = LABEL_PROVIDER_SERVICE.getLabelProvider(
							code.getUri()).getImage(code.getUri());
				} catch (Exception e2) {
					LOGGER.error(e2);
				}

				IllustratedText labelContent = new SimpleIllustratedComposite.IllustratedText(
						image, depth + " - " + code.getCaption());
				SimpleIllustratedComposite label = new SimpleIllustratedComposite(
						this, SWT.CENTER, labelContent);
				label.setSpacing(0);
				label.setLayoutData(GridDataFactory.swtDefaults()
						.align(SWT.FILL, SWT.CENTER).grab(true, false).create());
				this.labels.add(label);

				Combo value = new Combo(this, SWT.READ_ONLY);
				value.setLayoutData(GridDataFactory.swtDefaults()
						.align(SWT.FILL, SWT.CENTER).grab(true, false).create());
				value.setData(code);
				if (dim instanceof NominalDimension) {
					List<String> possibleValues = ((NominalDimension) dim)
							.getPossibleValues();
					value.add(UNSET);
					for (String possibleValue : possibleValues) {
						value.add(possibleValue);
					}
					value.select(possibleValues.indexOf(CODE_SERVICE
							.getDimensionValue(this.loaded, code)) + 1);
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
		}

		this.layout();
	}
}