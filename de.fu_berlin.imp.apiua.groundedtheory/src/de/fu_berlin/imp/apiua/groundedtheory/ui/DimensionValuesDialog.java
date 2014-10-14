package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.apiua.groundedtheory.ui.DimensionValuesComposite.IDimensionValueListener;

public class DimensionValuesDialog extends TitleAreaDialog {

	private static final Logger LOGGER = Logger
			.getLogger(DimensionValuesDialog.class);

	private DimensionValuesComposite dimensionValuesComposite;
	private final ICodeInstance codeInstance;

	private final List<ICodeInstance> dirty = new LinkedList<ICodeInstance>();
	private final IDimensionValueListener dimensionValueListener = new IDimensionValueListener() {
		@Override
		public void dimensionValueChanged(ICodeInstance codeInstance,
				IDimension dimension, String newValue) {
			if (!DimensionValuesDialog.this.dirty.contains(codeInstance)) {
				DimensionValuesDialog.this.dirty.add(codeInstance);
			}
		}
	};

	public DimensionValuesDialog(Shell parentShell, ICodeInstance uri) {
		super(parentShell);
		this.codeInstance = uri;
	}

	@Override
	public void create() {
		super.create();
		this.setTitle("Set Dimension Values");
		this.setMessage("Please set the new dimension values",
				IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new FillLayout());

		this.dimensionValuesComposite = new DimensionValuesComposite(container,
				SWT.NONE, this.dimensionValueListener);
		try {
			this.dimensionValuesComposite.load(this.codeInstance.getId(),
					this.codeInstance);
		} catch (CodeServiceException e) {
			LOGGER.error(
					"Error opening "
							+ DimensionValuesDialog.class.getSimpleName(), e);
			super.cancelPressed();
		}
		return area;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		for (ICodeInstance dirtyCodeInstance : this.dirty) {
			try {
				this.dimensionValuesComposite.save(dirtyCodeInstance);
			} catch (CodeStoreWriteException e) {
				LOGGER.error(e);
			}
		}
		super.okPressed();
	}

}