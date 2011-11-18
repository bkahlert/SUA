package de.fu_berlin.imp.seqan.usability_analyzer.doclog.widgets;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;

public class DoclogDetailDialog extends Dialog {

	private Point windowSize;
	private DoclogRecord doclogRecord;

	public DoclogDetailDialog(Shell parentShell, Point windowSize,
			DoclogRecord doclogRecord) {
		super(parentShell);
		this.windowSize = windowSize;
		this.doclogRecord = doclogRecord;
	}

	protected Control createDialogArea(Composite parent) {
		getShell().addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				close();
			}
		});

		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new FillLayout());
		final Image screenshot = new Image(Display.getCurrent(), doclogRecord
				.getScreenshot().getImageData());
		Label label = new Label(composite, SWT.NONE);
		label.setImage(screenshot);
		composite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (screenshot != null && !screenshot.isDisposed())
					screenshot.dispose();
			}
		});
		return composite;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return parent;
	}

	protected Point getInitialSize() {
		return this.windowSize;
	}
}
