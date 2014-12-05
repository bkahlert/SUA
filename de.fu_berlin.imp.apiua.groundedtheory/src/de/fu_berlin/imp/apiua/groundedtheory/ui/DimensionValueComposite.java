package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
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

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IteratorUtils;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.Triple;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
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

	private ICodeInstance loaded = null;
	private final List<Triple<Integer, ICode, IDimension>> dimensions = new LinkedList<Triple<Integer, ICode, IDimension>>();

	private final List<Control> labels = new LinkedList<Control>();
	private final List<Control> valueEditors = new LinkedList<Control>();

	private final IDimensionValueListener dimensionValueListener;

	private final CodeServiceAdapter codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void dimensionValueChanged(URI uri, String oldValue, String value) {
			try {
				ExecUtils
						.syncExec(() -> DimensionValueComposite.this.refresh());
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}

		@Override
		public void dimensionChanged(URI uri, IDimension oldDimension,
				IDimension newDimension) {
			try {
				ExecUtils
						.syncExec(() -> DimensionValueComposite.this.refresh());
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}

		@Override
		public void propertiesChanged(URI uri,
				java.util.List<URI> addedProperties,
				java.util.List<URI> removedProperties) {
			try {
				ExecUtils
						.syncExec(() -> DimensionValueComposite.this.refresh());
			} catch (Exception e) {
				LOGGER.error(e);
			}
		};
	};

	public DimensionValueComposite(Composite parent, int style,
			IDimensionValueListener dimensionValueListener) {
		super(parent, SWT.NONE);

		this.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.margins(0, 0).numColumns(2).create());

		CODE_SERVICE.addCodeServiceListener(this.codeServiceListener);
		this.dimensionValueListener = dimensionValueListener;
		this.refresh();
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

	public void load(ICodeInstance codeInstance) throws CodeServiceException {
		if (ObjectUtils.equals(this.loaded, codeInstance)) {
			return;
		}

		this.loaded = codeInstance;

		this.dimensions.clear();
		for (Pair<Integer, ICode> property : IteratorUtils.dfs(codeInstance
				.getCode(), property1 -> CODE_SERVICE.getProperties(property1)
				.toArray(new ICode[0]))) {
			IDimension dimension = CODE_SERVICE.getDimension(property
					.getSecond().getUri());
			this.dimensions.add(new Triple<Integer, ICode, IDimension>(property
					.getFirst(), property.getSecond(), dimension));
		}

		try {
			ExecUtils.syncExec(() -> DimensionValueComposite.this.refresh());
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	public void save() throws CodeStoreWriteException {
		ICodeInstance codeInstance = this.loaded;
		if (codeInstance == null) {
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
						CODE_SERVICE.setDimensionValue(codeInstance.getUri(),
								dimension.getSecond(), dimensionValue);
					} catch (IllegalDimensionValueException e) {
						LOGGER.error(
								"Error saving value " + dimensionValue
										+ " for " + codeInstance + "'s "
										+ dimension.getSecond() + " dimension",
								e);
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

		if (this.isDisposed()) {
			return;
		}

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
						this, SWT.CENTER | (i == 0 ? SWT.BOLD : SWT.NONE),
						labelContent);
				label.setSpacing(0);
				label.setLayoutData(GridDataFactory.swtDefaults()
						.align(SWT.FILL, SWT.CENTER).grab(true, false).create());
				this.labels.add(label);

				Control value;
				if (dim != null) {
					value = dim
							.createValueEditControl(
									this,
									(dimension1, newValue) -> {
										if (DimensionValueComposite.this.dimensionValueListener != null) {
											DimensionValueComposite.this.dimensionValueListener
													.dimensionValueChanged(
															dimension1,
															newValue);
										}
									});
					dim.setValueEditControlValue(value, CODE_SERVICE
							.getDimensionValue(this.loaded.getUri(), code));
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

		this.layout(true, true);
	}
}