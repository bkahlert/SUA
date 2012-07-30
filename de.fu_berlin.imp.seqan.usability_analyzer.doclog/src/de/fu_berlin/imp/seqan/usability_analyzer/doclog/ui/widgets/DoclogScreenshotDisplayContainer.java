package de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.nebula.widgets.RoundedComposite;

public class DoclogScreenshotDisplayContainer extends RoundedComposite {

	public DoclogScreenshotDisplayContainer(Composite parent, int style,
			String caption) {
		super(parent, style);

		this.setLayout(GridLayoutFactory.fillDefaults().numColumns(1)
				.margins(5, 5).create());

		Label captionLabel = new Label(this, SWT.NONE);
		captionLabel.setText(caption);
		captionLabel.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.BEGINNING, SWT.BEGINNING).create());
	}
}
