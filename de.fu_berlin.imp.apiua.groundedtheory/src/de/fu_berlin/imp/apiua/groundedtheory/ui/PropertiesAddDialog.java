package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer.Filterable;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer.QuickSelectionMode;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer.ShowAllInstances;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer.ShowInstances;

class PropertiesAddDialog extends TitleAreaDialog {

	private CodeViewer codeViewer;

	private List<URI> uris;

	public PropertiesAddDialog(Shell parentShell, List<URI> uris) {
		super(parentShell);
		this.uris = uris != null ? uris : new LinkedList<URI>();
	}

	@Override
	public void create() {
		super.create();
		this.setTitle("Add Property");
		this.setMessage("Please select the properties you want to add",
				IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new FillLayout());

		this.codeViewer = new CodeViewer(container, SWT.NONE,
				ShowInstances.OFF, ShowAllInstances.OFF, null, Filterable.ON, QuickSelectionMode.ON);
		this.codeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						PropertiesAddDialog.this.uris = SelectionUtils
								.getAdaptableObjects(event.getSelection(),
										URI.class);
					}
				});
		this.codeViewer.getViewer().expandAll();
		this.codeViewer.setSelection(new StructuredSelection(this.uris));
		this.codeViewer.setFocus();

		return area;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	public List<URI> getProperties() {
		return this.uris;
	}
}