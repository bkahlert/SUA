package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.FontUtils;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class ShowArtefactIDDialog extends TitleAreaDialog {

	public static final int COPY_AND_CLOSE_ID = IDialogConstants.OK_ID + 1;
	public static final String COPY_AND_CLOSE_STRING = "Copy and Close";

	private ICodeable codeable;

	public ShowArtefactIDDialog(Shell parentShell, ICodeable codeable) {
		super(parentShell);
		this.codeable = codeable;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		this.createButton(parent, IDialogConstants.CLOSE_ID,
				IDialogConstants.CLOSE_LABEL, true);
		this.createButton(parent, COPY_AND_CLOSE_ID, COPY_AND_CLOSE_STRING,
				true);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		this.setTitle("Artefact ID");
		this.setMessage(
				"Press \"Copy and Close\" if you want to copy the artefact ID to your clipboard",
				IMessageProvider.INFORMATION);
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1)
				.create());

		Image image = null;
		String label = "?";

		Label intro = new Label(composite, SWT.NONE);
		intro.setText("The ID of the artefact");
		intro.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		FontUtils.changeFontSizeBy(intro, -1);

		ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
				.getWorkbench().getService(ILabelProviderService.class);
		if (labelProviderService != null) {
			ILabelProvider labelProvider = labelProviderService
					.getLabelProvider(this.codeable);
			if (labelProvider != null) {
				image = labelProvider.getImage(this.codeable);
				label = labelProvider.getText(this.codeable);
			}
		}

		SimpleIllustratedComposite artefact = new SimpleIllustratedComposite(
				composite, SWT.NONE, new IllustratedText(image, label));
		artefact.setSpacing(3);
		artefact.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		artefact.changeFontSizeBy(artefact, 2);

		Label is = new Label(composite, SWT.NONE);
		is.setText("is");
		is.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		FontUtils.changeFontSizeBy(is, -1);

		Label uriLabel = new Label(composite, SWT.NONE);
		uriLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		uriLabel.setText(this.codeable.getUri().toString());
		FontUtils.changeFontSizeBy(uriLabel, 2);

		parent.pack();
		return composite;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		this.close();
	}
}
