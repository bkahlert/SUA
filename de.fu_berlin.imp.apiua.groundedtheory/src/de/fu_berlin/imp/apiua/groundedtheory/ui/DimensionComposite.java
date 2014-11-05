package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.SWTUtils;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension.IDimensionListener;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;

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

	private final List<Pair<Class<? extends IDimension>, String>> availableDimensionTypes;
	private final Map<Class<? extends IDimension>, Integer> dimensionTypeToIndex;
	private final Map<String, Integer> dimensionTypeNameToIndex;

	private Class<? extends IDimension> getDimensionByName(String name) {
		for (Pair<Class<? extends IDimension>, String> t : DimensionComposite.this.availableDimensionTypes) {
			if (t.getSecond().equals(name)) {
				return t.getFirst();
			}
		}
		throw new RuntimeException("Invalid name for a dimension: " + name);
	}

	private URI loaded = null;

	private final Combo typeCombo;
	private final Composite editComposite;

	private Class<? extends IDimension> dimensionType = null;
	private IDimension dimension = null;

	public DimensionComposite(
			Composite parent,
			int style,
			List<Pair<Class<? extends IDimension>, String>> availableDimensionTypes) {
		super(parent, style);
		this.availableDimensionTypes = availableDimensionTypes;
		this.dimensionTypeToIndex = new HashMap<Class<? extends IDimension>, Integer>();
		this.dimensionTypeNameToIndex = new HashMap<String, Integer>();
		for (int i = 0; i < availableDimensionTypes.size(); i++) {
			Pair<Class<? extends IDimension>, String> x = availableDimensionTypes
					.get(i);
			this.dimensionTypeToIndex.put(x.getFirst(), i);
			this.dimensionTypeNameToIndex.put(x.getSecond(), i);
		}

		this.setLayout(GridLayoutFactory.fillDefaults().spacing(5, 5).create());

		this.typeCombo = new Combo(this, SWT.DROP_DOWN | SWT.BORDER
				| SWT.READ_ONLY);
		this.typeCombo.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());
		for (Pair<Class<? extends IDimension>, String> dimensionType : availableDimensionTypes) {
			this.typeCombo.add(dimensionType.getSecond());
		}
		this.typeCombo.select(0);
		this.typeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DimensionComposite.this.dimensionType = DimensionComposite.this
						.getDimensionByName(DimensionComposite.this.typeCombo
								.getText());
				if (DimensionComposite.this.dimensionType == null) {
					DimensionComposite.this.dimension = null;
				} else if (DimensionComposite.this.dimensionType
						.isInstance(DimensionComposite.this.dimension)) {
					// do nothing
				} else {
					try {
						DimensionComposite.this.dimension = DimensionComposite.this.dimensionType
								.newInstance();
					} catch (Exception e1) {
						LOGGER.error("Error creating "
								+ DimensionComposite.this.dimensionType
								+ " for " + DimensionComposite.this.loaded);
					}
				}
				try {
					DimensionComposite.this.save();
				} catch (CodeStoreWriteException e1) {
					LOGGER.error(e);
				}
				DimensionComposite.this.refresh();
			}
		});

		this.editComposite = new Composite(this, SWT.NONE);
		this.editComposite.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());
		this.editComposite.setLayout(new FillLayout());
	}

	@Override
	public void dispose() {
		try {
			this.save();
		} catch (CodeStoreWriteException e) {
			LOGGER.error(e);
		}
	}

	public void load(URI uri) throws CodeStoreWriteException {
		if (ObjectUtils.equals(this.loaded, uri)) {
			return;
		}

		this.save();

		if (uri != null && LocatorService.INSTANCE.getType(uri) == ICode.class) {
			ICode code = null;
			try {
				code = LocatorService.INSTANCE.resolve(uri, ICode.class, null)
						.get();
			} catch (Exception e) {
				LOGGER.error("Error", e);
			}

			if (code == null) {
				this.load(null);
				return;
			}

			// TODO layout dimension value

			if (code != null) {
				this.dimension = CODE_SERVICE.getDimension(code.getUri());
				if (this.dimension == null) {
					this.dimensionType = null;
				} else {
					this.dimensionType = this.dimension.getClass();
				}

				this.loaded = uri;
			} else {
				this.dimensionType = null;
				this.dimension = null;
				this.loaded = null;
			}
		} else {
			this.dimensionType = null;
			this.dimension = null;
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

		if (this.dimensionType == null ^ this.dimension == null) {
			LOGGER.error("Implementation error");
		} else {
			CODE_SERVICE.setDimension(code, null);
			CODE_SERVICE.setDimension(code, this.dimension);
		}
	}

	private void refresh() {
		if (this.dimensionType == null ^ this.dimension == null) {
			LOGGER.error("Implementation error");
		} else {
			SWTUtils.clearControl(this.editComposite);

			int index = this.dimensionTypeToIndex.get(this.dimensionType);
			this.typeCombo.select(index);
			this.typeCombo.setEnabled(this.loaded != null);
			if (this.dimension != null) {
				this.dimension.createEditControl(this.editComposite,
						new IDimensionListener() {
							@Override
							public void dimensionChanged(IDimension dimension) {
								try {
									DimensionComposite.this.save();
								} catch (CodeStoreWriteException e) {
									LOGGER.error(e);
								}
							}
						});
			}
		}
		this.layout(false, true);
	}

	@Override
	public boolean setFocus() {
		return this.typeCombo.setFocus();
	}
}