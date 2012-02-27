package de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.widgets;

import java.util.ArrayList;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class StringListWidget extends Composite {

	private Composite textContainer;

	public StringListWidget(Composite composite, int style) {
		super(composite, style);

		this.setLayout(GridLayoutFactory.fillDefaults().margins(10, 10)
				.create());

		this.textContainer = new Composite(this, SWT.NONE);
		this.textContainer.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());
		this.textContainer.setLayout(GridLayoutFactory.fillDefaults()
				.numColumns(2).create());

		Button add = new Button(this, SWT.PUSH);
		add.setText("Add");
		add.setLayoutData(GridDataFactory.fillDefaults().grab(false, false)
				.create());
		add.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addText("");
			}
		});
	}

	public void clearTexts() {
		for (Control control : this.textContainer.getChildren()) {
			if (!control.isDisposed()) {
				control.dispose();
			}
		}
	}

	public void setTexts(String[] texts) {
		for (String text : texts) {
			addText(text);
		}
	}

	public void addText(String string) {
		final Text text = new Text(this.textContainer, SWT.BORDER);
		text.setText(string);
		text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
				.create());

		final Button button = new Button(this.textContainer, SWT.PUSH);
		button.setText("Remove");
		button.setLayoutData(GridDataFactory.fillDefaults().create());
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!text.isDisposed())
					text.dispose();
				if (!button.isDisposed())
					button.dispose();
				layout();
				update();
			}
		});

		this.layout();
		update();
	}

	public String[] getTexts() {
		ArrayList<String> texts = new ArrayList<String>();
		for (Control control : this.textContainer.getChildren()) {
			if (!control.isDisposed() && control instanceof Text) {
				texts.add(((Text) control).getText());
			}
		}
		return texts.toArray(new String[0]);
	}
}
