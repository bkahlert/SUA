package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.PartRenamer;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.utils.selection.retriever.ISelectionRetriever;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.NominalDimension;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.OrdinalDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.apiua.groundedtheory.ui.DimensionComposite;
import de.fu_berlin.imp.apiua.groundedtheory.ui.DimensionValuesComposite;
import de.fu_berlin.imp.apiua.groundedtheory.ui.DimensionValuesComposite.IDimensionValueListener;
import de.fu_berlin.imp.apiua.groundedtheory.ui.PropertiesComposite;
import de.fu_berlin.imp.apiua.groundedtheory.ui.UriPartRenamerConverter;

public class DimensionView extends ViewPart {

	private static final Logger LOGGER = Logger.getLogger(DimensionView.class);

	public static final String ID = "de.fu_berlin.imp.apiua.groundedtheory.views.DimensionView";

	@SuppressWarnings("unused")
	private static final ILabelProviderService LABEL_PROVIDER_SERVICE = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	private final ISelectionRetriever<URI> uriRetriever = SelectionRetrieverFactory
			.getSelectionRetriever(URI.class);

	private final ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part == DimensionView.this) {
				return;
			}
			List<URI> uris = DimensionView.this.uriRetriever.getSelection();
			try {
				if (uris.size() > 0) {
					DimensionView.this.load(uris.get(0));
				} else {
					DimensionView.this.load(null);
				}
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
	};

	private final List<Pair<Class<? extends IDimension>, String>> availableDimensionTypes;

	private final PartRenamer<URI> partRenamer;
	private Composite parent;
	private DimensionComposite dimensionComposite;
	private DimensionValuesComposite dimensionValuesComposite;
	private PropertiesComposite propertiesComposite;

	public DimensionView() {
		this.availableDimensionTypes = new LinkedList<Pair<Class<? extends IDimension>, String>>();
		this.availableDimensionTypes
				.add(new Pair<Class<? extends IDimension>, String>(null, "None"));
		this.availableDimensionTypes
				.add(new Pair<Class<? extends IDimension>, String>(
						NominalDimension.class, "Nominal"));
		this.availableDimensionTypes
				.add(new Pair<Class<? extends IDimension>, String>(
						OrdinalDimension.class, "Ordinal"));
		this.partRenamer = new PartRenamer<URI>(this,
				new UriPartRenamerConverter());
		SelectionUtils.getSelectionService().addSelectionListener(
				this.selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removeSelectionListener(
				this.selectionListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		parent.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
				.spacing(5, 5).margins(5, 5).equalWidth(true).create());

		Group dimensionGroup = new Group(parent, SWT.BORDER);
		dimensionGroup.setText("Dimension");
		dimensionGroup.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());
		dimensionGroup.setLayout(new FillLayout());
		this.dimensionComposite = new DimensionComposite(dimensionGroup,
				SWT.NONE, this.availableDimensionTypes);

		Group propertiesGroup = new Group(parent, SWT.BORDER);
		propertiesGroup.setText("Properties");
		propertiesGroup.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());
		propertiesGroup.setLayout(new FillLayout());
		this.propertiesComposite = new PropertiesComposite(propertiesGroup,
				SWT.NONE);

		Group dimensionValueGroup = new Group(parent, SWT.BORDER);
		dimensionValueGroup.setText("Dimension Values");
		dimensionValueGroup.setLayoutData(GridDataFactory.fillDefaults()
				.span(2, 1).grab(true, true).create());
		dimensionValueGroup.setLayout(new FillLayout());
		this.dimensionValuesComposite = new DimensionValuesComposite(
				dimensionValueGroup, SWT.NONE, new IDimensionValueListener() {
					@Override
					public void dimensionValueChanged(
							ICodeInstance codeInstance, IDimension dimension,
							String newValue) {
						try {
							DimensionView.this.dimensionValuesComposite
									.save(codeInstance);
						} catch (CodeStoreWriteException e) {
							LOGGER.error(e);
						}
					}
				});

		// new ContextMenu(this.episodeViewer.getViewer(), this.getSite()) {
		// @Override
		// protected String getDefaultCommandID() {
		// return null;
		// }
		// };

	}

	private void load(URI uri) throws CodeStoreWriteException,
			CodeServiceException {
		this.partRenamer.apply(uri);

		boolean load = true;
		try {
			this.save();
		} catch (CodeStoreWriteException e) {
			MessageBox box = new MessageBox(this.getSite().getShell(),
					SWT.ICON_WARNING | SWT.YES | SWT.NO);
			box.setText("Cyclic Dependency");
			box.setMessage("Saving the changes would result in a cyclic dependency. Do you want to discard your changes?");
			load = box.open() == SWT.YES;
			LOGGER.error(e);
		}

		if (load) {
			this.dimensionComposite.load(uri);
			this.dimensionValuesComposite.load(uri);
			this.propertiesComposite.load(uri);
		}

		this.parent.layout(true, true);
	}

	private void save() throws CodeStoreWriteException {
		// saves automatically this.dimensionComposite.save();
		this.dimensionValuesComposite.save();
		this.propertiesComposite.save();
	}

	@Override
	public void setFocus() {
		this.dimensionComposite.setFocus();
	}

}
