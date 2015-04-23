package de.fu_berlin.imp.apiua.groundedtheory.dialogs;

import java.util.List;

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
import de.fu_berlin.imp.apiua.groundedtheory.preferences.SUAGTPreferenceUtil;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer.Filterable;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer.QuickSelectionMode;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer.ShowAllInstances;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer.ShowInstances;

public class SelectCodeDialog extends TitleAreaDialog {

	private CodeViewer codeViewer;

	private URI selected;

	public SelectCodeDialog(Shell parentShell, URI selected) {
		super(parentShell);
		this.selected = selected;
	}

	@Override
	public void create() {
		super.create();
		this.setTitle("Select Code");
		// this.setMessage("Please enter the new caption",
		// IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new FillLayout());

		this.codeViewer = new CodeViewer(container, SWT.NONE,
				ShowInstances.OFF, ShowAllInstances.OFF, null, Filterable.ON,
				QuickSelectionMode.ON);
		// this.codeViewer.setLayoutData(GridDataFactory.fillDefaults()
		// .grab(true, true).create());
		this.codeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						validate(event);
					}
				});
		this.codeViewer.getViewer().expandAll();
		this.codeViewer.setSelection(new StructuredSelection(
				new SUAGTPreferenceUtil().getLastUsedCodes()));
		this.codeViewer.setFocus();
		return area;
	}

	private void validate(SelectionChangedEvent event) {
		List<URI> uris = SelectionUtils.getAdaptableObjects(
				event.getSelection(), URI.class);
		SelectCodeDialog.this.selected = uris.size() > 0 ? uris.get(0) : null;
		// Button ok = getButton(IDialogConstants.OK_ID);
		// ok.setEnabled(this.selected != null);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	public URI getUri() {
		return this.selected;
	}
}