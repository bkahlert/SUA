package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.nebula.utils.FontUtils;

public abstract class ORWizardPage extends WizardPage {

	private int numAlternatives;

	public ORWizardPage(String pageName, int numAlternatives) {
		super(pageName);
		this.numAlternatives = numAlternatives;
	}

	public final void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);

		composite.setLayout(new GridLayout(2 * numAlternatives - 1, false));

		Composite[] contentComposites = new Composite[numAlternatives];
		for (int i = 0, m = numAlternatives; i < m; i++) {
			contentComposites[i] = new Composite(composite, SWT.NONE);
			contentComposites[i].setLayoutData(new GridData(SWT.FILL, SWT.FILL,
					true, true));
			if (i + 1 < m) {
				Label separator = new Label(composite, SWT.NONE);
				separator.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
						false, true));
				separator.setText("OR");
				FontUtils.changeFontSizeBy(separator, 10);
			}
		}

		fillContent(contentComposites);
	}

	public abstract void fillContent(Composite... contentComposites);
}