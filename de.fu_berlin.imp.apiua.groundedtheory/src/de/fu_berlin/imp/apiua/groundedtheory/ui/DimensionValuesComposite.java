package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.SWTUtils;
import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;

/**
 * This control shows the {@link IDimension} values of the loaded {@link URI}.
 * In contrast to a {@link DimensionValueComposite} a
 * {@link DimensionValuesComposite} not only supports {@link ICodeInstance}s. If
 * a non {@link ICodeInstance} is passed, all corresponding
 * {@link ICodeInstance} are loaded.
 * 
 * @author bkahlert
 * 
 */
public class DimensionValuesComposite extends Composite {

	public static interface IDimensionValueListener {
		public void dimensionValueChanged(ICodeInstance codeInstance,
				IDimension dimension, String newValue);
	}

	private static final Logger LOGGER = Logger
			.getLogger(DimensionValuesComposite.class);

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private final Map<ICodeInstance, DimensionValueComposite> dimensionValueComposites = new HashMap<ICodeInstance, DimensionValueComposite>();
	private final IDimensionValueListener dimensionValueListener;

	public DimensionValuesComposite(Composite parent, int style,
			IDimensionValueListener dimensionValueListener) {
		super(parent, style);
		this.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		this.dimensionValueListener = dimensionValueListener;
	}

	public void load(URI uri, ICodeInstance highlight)
			throws CodeServiceException {
		SWTUtils.clearControl(this);
		this.dimensionValueComposites.clear();
		if (uri == null) {
			return;
		}

		List<ICodeInstance> codeInstances = new ArrayList<ICodeInstance>();
		if (LocatorService.INSTANCE.getType(uri) == ICodeInstance.class) {
			try {
				final ICodeInstance codeInstance = LocatorService.INSTANCE
						.resolve(uri, ICodeInstance.class, null).get();
				if (codeInstance != null) {
					codeInstances.add(codeInstance);
					if (CODE_SERVICE.getAllInstances(codeInstance.getId()).size() > 1) {
						Button button = new Button(this, SWT.PUSH);
						button.setLayoutData(GridDataFactory.swtDefaults()
								.create());
						button.setText("Show all dimension values...");
						button.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									DimensionValuesComposite.this.load(
											codeInstance.getId(), codeInstance);
								} catch (Exception e1) {
									LOGGER.error("Error loading "
											+ codeInstance.getId(), e1);
								}
							}
						});

						Label label = new Label(this, SWT.WRAP);
						label.setLayoutData(GridDataFactory.fillDefaults()
								.grab(true, false).create());
						label.setText("Currently the association's single dimension value is loaded. To display the dimension values of all the phaenomenons associations, click on the button to the left.");
					}
				} else {
					LOGGER.error("Error resolving " + ICodeInstance.class);
				}
			} catch (Exception e) {
				LOGGER.error("Error resolving " + ICodeInstance.class, e);
			}
		} else {
			codeInstances.addAll(CODE_SERVICE.getAllInstances(uri));
		}

		for (final ICodeInstance codeInstance : codeInstances) {
			Composite parent = this;
			if (codeInstances.size() > 0 /* 1 */) {
				Group group = new Group(this, SWT.BORDER);
				group.setLayoutData(GridDataFactory.fillDefaults()
						.grab(true, true).span(2, 1).create());
				group.setLayout(new FillLayout());
				parent = group;
			}

			DimensionValueComposite dimensionValueComposite = new DimensionValueComposite(
					parent, SWT.NONE, new IDimension.IDimensionValueListener() {
						@Override
						public void dimensionValueChanged(IDimension dimension,
								String newValue) {
							if (DimensionValuesComposite.this.dimensionValueListener != null) {
								DimensionValuesComposite.this.dimensionValueListener
										.dimensionValueChanged(codeInstance,
												dimension, newValue);
							}
						}
					});
			this.dimensionValueComposites.put(codeInstance,
					dimensionValueComposite);
			dimensionValueComposite.load(codeInstance);

			if (codeInstance.equals(highlight)) {
				Group oldGroup = (Group) parent;
				oldGroup.setBackground(RGB.WARNING_COLOR);
			}
		}

		DimensionValuesComposite.this.layout();
	}

	public void load(URI uri) throws CodeServiceException {
		this.load(uri, null);
	}

	public void save() throws CodeStoreWriteException {
		for (ICodeInstance codeInstance : this.dimensionValueComposites
				.keySet()) {
			this.save(codeInstance);
		}
	}

	public void save(ICodeInstance codeInstance) throws CodeStoreWriteException {
		if (this.dimensionValueComposites.containsKey(codeInstance)) {
			this.dimensionValueComposites.get(codeInstance).save();
		}
	}

}
