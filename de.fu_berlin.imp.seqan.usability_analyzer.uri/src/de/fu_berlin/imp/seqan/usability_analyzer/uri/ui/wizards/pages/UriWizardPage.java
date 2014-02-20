package de.fu_berlin.imp.seqan.usability_analyzer.uri.ui.wizards.pages;

import java.net.URISyntaxException;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.nebula.widgets.decoration.EmptyText;

import de.fu_berlin.imp.seqan.usability_analyzer.uri.model.IUri;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.model.Uri;

public class UriWizardPage extends WizardPage {

	private Text uriAddress;
	private Text uriTitle;

	private IUri editUri;

	public UriWizardPage() {
		super(UriWizardPage.class.getName());
		this.setTitle("Create URI");
		this.setDescription("Please specify the URI to be added.");
		this.editUri = null;
	}

	public UriWizardPage(IUri editUri) {
		this();
		this.setTitle("Edit URI");
		this.setDescription("Please specify the new URI.");
		this.editUri = editUri;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		this.setControl(composite);

		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
				.margins(10, 0).create());

		this.uriAddress = new Text(composite, SWT.BORDER);
		this.uriAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		if (this.editUri != null) {
			try {
				this.uriAddress.setText(this.editUri.getUri().toString());
			} catch (NullPointerException e) {
				this.uriAddress.setText("");
			}
		} else {
			new EmptyText(this.uriAddress, "http://www.example.com");
		}
		this.uriAddress.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				de.fu_berlin.imp.seqan.usability_analyzer.uri.ui.wizards.pages.UriWizardPage.this
						.updateCompletion();
			}
		});

		this.uriTitle = new Text(composite, SWT.BORDER);
		this.uriTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		if (this.editUri != null) {
			try {
				this.uriTitle.setText(this.editUri.getTitle().toString());
			} catch (NullPointerException e) {
				this.uriTitle.setText("");
			}
		} else {
			new EmptyText(this.uriTitle, "Optional title");
		}
		this.uriTitle.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				de.fu_berlin.imp.seqan.usability_analyzer.uri.ui.wizards.pages.UriWizardPage.this
						.updateCompletion();
			}
		});

	}

	private void updateCompletion() {
		if (!this.uriAddress.getText().trim().isEmpty()) {
			this.setMessage("");
			this.setErrorMessage(null);
			this.setPageComplete(true);
		} else {
			this.setErrorMessage("The name must not be empty!");
			this.setPageComplete(false);
		}
	}

	public IUri getURI() {
		try {
			return new Uri(this.uriTitle.getText().isEmpty() ? null
					: this.uriTitle.getText(), this.uriAddress.getText());
		} catch (URISyntaxException e) {
			return null;
		}
	}

}