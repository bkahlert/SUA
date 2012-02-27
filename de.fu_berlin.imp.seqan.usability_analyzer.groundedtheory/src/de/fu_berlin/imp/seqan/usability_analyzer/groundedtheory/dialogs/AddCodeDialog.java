package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.dialogs;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.inf.nebula.explanation.note.SimpleNoteComposite;

@Deprecated
// TODO: delete
public class AddCodeDialog extends Dialog {

	private Text codeCaptionText;
	private String codeCaption;
	private ICodeable[] codeables;

	public AddCodeDialog(Shell parentShell, List<ICodeable> codeables) {
		super(parentShell);
		this.codeables = codeables.toArray(new ICodeable[0]);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(GridLayoutFactory.swtDefaults().numColumns(2)
				.create());

		SimpleNoteComposite noteComposite = new SimpleNoteComposite(composite,
				SWT.BORDER, SWT.ICON_INFORMATION);
		noteComposite.setLayoutData(GridDataFactory.fillDefaults().span(2, 1)
				.create());
		noteComposite.setText("Add a new code to the following entries:\n"
				+ StringUtils.join(codeables, "\n"));

		Label codeCaptionLabel = new Label(composite, SWT.NONE);
		codeCaptionLabel.setLayoutData(GridDataFactory.swtDefaults().create());
		codeCaptionLabel.setText("Code");
		this.codeCaptionText = new Text(composite, SWT.BORDER);
		this.codeCaptionText.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());

		parent.pack();
		return composite;
	}

	@Override
	protected void okPressed() {
		this.codeCaption = this.codeCaptionText.getText();
		super.okPressed();
	}

	public String getCodeCaption() {
		return codeCaption;
	}
}
