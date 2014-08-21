package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
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
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension.IDimensionValueListener;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IllegalDimensionValueException;
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

	private static final Logger LOGGER = Logger
			.getLogger(DimensionValueComposite.class);

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private static final String EXPLANATION = "Code the selected object with a dimensionalized code to set a dimension value.";

	private static final String DATA_CODE = "code";

	private URI loaded = null;
	private final List<Triple<Integer, ICode, IDimension>> dimensions = new LinkedList<Triple<Integer, ICode, IDimension>>();

	private final List<Control> labels = new LinkedList<Control>();
	private final List<Control> valueEditors = new LinkedList<Control>();

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
			for (Pair<Integer, ICode> property : IteratorUtils.dfs(code,
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
			for (Control valueEditor : this.valueEditors) {
				if (valueEditor.getData(DATA_CODE) == dimension.getSecond()) {
					String dimensionValue = dimension.getThird()
							.getValueEditControlValue(valueEditor);
					try {
						CODE_SERVICE.setDimensionValue(uri,
								dimension.getSecond(), dimensionValue);
					} catch (IllegalDimensionValueException e) {
						LOGGER.error("Error saving value " + dimensionValue
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
		for (Control valueEditor : this.valueEditors) {
			valueEditor.dispose();
		}
		this.valueEditors.clear();

		if (this.dimensions.size() == 0) {
			Label label = new Label(this, SWT.WRAP);
			label.setText(EXPLANATION);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
					2));
			this.labels.add(label);
		} else {
			int depths[] = new int[this.dimensions.size()];
			for (int i = 0, m = this.dimensions.size(); i < m; i++) {
				depths[i] = this.dimensions.get(i).getFirst();
			}
			for (int i = 0, m = this.dimensions.size(); i < m; i++) {
				Triple<Integer, ICode, IDimension> dimension = this.dimensions
						.get(i);
				boolean isLast = this.dimensions.size() == i + 1;
				int depth = dimension.getFirst();
				ICode code = dimension.getSecond();
				IDimension dim = dimension.getThird();

				Image image = GTLabelProvider.getCodeImage(code);

				String prefix = "";
				if (depth > 0) {
					prefix += "";
					for (int k = 0; k < depth - 1; k++) {
						prefix += "  ";
					}

					if (!isLast && depths[i + 1] == depth) {
						prefix += "┣━ ";
					} else {
						prefix += "┗━ ";
					}

					for (int k = i + 1; k < depths.length; k++) {
						if (depths[k] == depth) {
							StringBuilder sb = new StringBuilder(prefix);
							sb.setCharAt(depth - 1, '┣');
							prefix = sb.toString();
						}
						if (depths[k] == depth - 1) {
							StringBuilder sb = new StringBuilder(prefix);
							sb.setCharAt(depth - 2, '┃');
							prefix = sb.toString();
						}
					}
				}

				IllustratedText labelContent = new SimpleIllustratedComposite.IllustratedText(
						image, prefix + code.getCaption());
				SimpleIllustratedComposite label = new SimpleIllustratedComposite(
						this, SWT.CENTER, labelContent);
				label.setSpacing(0);
				label.setLayoutData(GridDataFactory.swtDefaults()
						.align(SWT.FILL, SWT.CENTER).grab(true, false).create());
				this.labels.add(label);

				Control value;
				if (dim != null) {
					value = dim.createValueEditControl(this,
							new IDimensionValueListener() {
								@Override
								public void dimensionValueChanged(
										IDimension dimension, String newValue) {
									try {
										DimensionValueComposite.this.save();
									} catch (CodeStoreWriteException e) {
										LOGGER.error(e);
									}
								}
							});
					dim.setValueEditControlValue(value,
							CODE_SERVICE.getDimensionValue(this.loaded, code));
				} else {
					value = new Label(this, SWT.WRAP);
					((Label) value).setText("-");
				}
				value.setLayoutData(GridDataFactory.swtDefaults()
						.align(SWT.FILL, SWT.CENTER).grab(true, false).create());
				value.setData(DATA_CODE, code);
				if (dim != null) {

				} else {
				}
				this.valueEditors.add(value);
			}
		}

		this.layout();
	}
}