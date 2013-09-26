package de.fu_berlin.imp.seqan.usability_analyzer.survey.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.widgets.composer.ComposerReadOnly;

public class CdView extends ViewPart {

	private ComposerReadOnly view = null;

	public CdView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.view = new ComposerReadOnly(parent, SWT.NONE);

	}

	@Override
	public void setFocus() {
		this.view.setFocus();
	}

}
