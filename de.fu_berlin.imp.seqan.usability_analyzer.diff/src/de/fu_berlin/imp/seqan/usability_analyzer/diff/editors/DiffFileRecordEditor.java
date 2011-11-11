package de.fu_berlin.imp.seqan.usability_analyzer.diff.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;

public class DiffFileRecordEditor extends EditorPart {

	public static String ID = "de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileRecordEditor";
	private DiffFileRecord diffFileRecord;
	private DiffFileRecordEditorInput input;

	public DiffFileRecordEditor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (!(input instanceof DiffFileRecordEditorInput)) {
			throw new RuntimeException(
					"This editor only accepts input of type "
							+ DiffFileRecordEditorInput.class.getSimpleName());
		}

		DiffFileRecordEditorInput new_name = (DiffFileRecordEditorInput) input;
		this.input = (DiffFileRecordEditorInput) input;
		setSite(site);
		setInput(input);

		this.diffFileRecord = this.input.getDiffFileRecord();
		setPartName(this.diffFileRecord.getFilename() + " - r"
				+ this.diffFileRecord.getDiffFile().getRevision());
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);
		Label label1 = new Label(parent, SWT.NONE);
		label1.setText("First Name");
		Text text = new Text(parent, SWT.BORDER);
		text.setText(this.diffFileRecord.getFilename());
		text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		new Label(parent, SWT.NONE).setText("Last Name");
		Text lastName = new Text(parent, SWT.BORDER);
		lastName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		lastName.setText(this.diffFileRecord.getContent());
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {

		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
	}

}
