package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.pages;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.utils.FontUtils;

public abstract class ORWizardPage extends WizardPage {

	private final Font font;
	private final int numAlternatives;

	public ORWizardPage(String pageName, int numAlternatives) {
		super(pageName);
		this.numAlternatives = numAlternatives;
		this.font = new Font(Display.getCurrent(),
				FontUtils.getResizedFontData(Display.getCurrent()
						.getSystemFont().getFontData(), 2));
	}

	@Override
	public final void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		this.setControl(composite);

		composite.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Font oldFont = e.gc.getFont();
				e.gc.setFont(ORWizardPage.this.font);
				e.gc.drawText("OR", (int) (e.width / 2 - e.gc.getFontMetrics()
						.getAverageCharWidth() * 1.25),
						(int) (e.height / 2 - ORWizardPage.this.font
								.getFontData()[0].getHeight() / 3.0));
				e.gc.setFont(oldFont);
			}
		});

		composite.setLayout(GridLayoutFactory.fillDefaults()
				.numColumns(this.numAlternatives).margins(10, 0).spacing(50, 0)
				.equalWidth(true).create());

		Composite[] contentComposites = new Composite[this.numAlternatives];
		for (int i = 0, m = this.numAlternatives; i < m; i++) {
			contentComposites[i] = new Composite(composite, SWT.NONE);
			contentComposites[i].setLayoutData(new GridData(SWT.FILL, SWT.FILL,
					true, true));
		}

		this.fillContent(contentComposites);
	}

	public abstract void fillContent(Composite... contentComposites);

	@Override
	public void dispose() {
		this.font.dispose();
		super.dispose();
	}
}