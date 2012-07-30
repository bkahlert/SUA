package de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.widgets;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;

public class DoclogDetailDialog extends Dialog {

	private static final Logger LOGGER = Logger
			.getLogger(DoclogDetailDialog.class.getSimpleName());

	private static final int borderWidth = 5;

	private static final Color COLOR_HIGHLIGHT = new Color(
			Display.getDefault(), new RGB(216, 255, 38));
	private static final Color COLOR_STANDARD = new Color(Display.getDefault(),
			new RGB(75, 131, 179));

	private DoclogTimeline doclogTimeline;
	private DoclogRecord currentDoclogRecord;

	private Label imageLabel;

	public DoclogDetailDialog(Shell parentShell, DoclogTimeline doclogTimeline,
			DoclogRecord currentDoclogRecord) {
		super(parentShell);
		this.doclogTimeline = doclogTimeline;
		this.currentDoclogRecord = currentDoclogRecord;
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new FillLayout());

		imageLabel = new Label(composite, SWT.NONE);
		imageLabel.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				close();
			}
		});
		composite.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_RIGHT) {
					currentDoclogRecord = doclogTimeline.getDoclogFile()
							.getNextDoclogRecord(currentDoclogRecord);
					loadDoclogRecord(currentDoclogRecord);
				} else if (e.keyCode == SWT.ARROW_LEFT) {
					currentDoclogRecord = doclogTimeline.getDoclogFile()
							.getPrevDoclogRecord(currentDoclogRecord);
					loadDoclogRecord(currentDoclogRecord);
				} else if (e.keyCode == SWT.ESC || e.keyCode == SWT.CR) {
					close();
				}
			}
		});

		composite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				disposeImage();
			}
		});

		loadDoclogRecord(currentDoclogRecord);

		return composite;
	}

	private void loadDoclogRecord(DoclogRecord doclogRecord) {
		if (doclogRecord == null) {
			LOGGER.info("User tried to move beyond the available "
					+ DoclogRecord.class.getSimpleName()
					+ "s while browsing through "
					+ doclogTimeline.getDoclogFile());
			return;
		}

		disposeImage();

		boolean isIntersected = false;
		if (doclogTimeline.getHighlightedDateRanges() != null)
			for (TimeZoneDateRange t : doclogTimeline
					.getHighlightedDateRanges()) {
				if (t.isIntersected(doclogRecord.getDateRange())) {
					isIntersected = true;
					break;
				}
			}
		imageLabel.setImage(new Image(Display.getCurrent(), doclogRecord
				.getScreenshot().getImageData()));
		imageLabel.getParent().setBackground(
				isIntersected ? COLOR_HIGHLIGHT : COLOR_STANDARD);

		this.doclogTimeline.center(currentDoclogRecord.getDateRange());

		Shell shell = this.getShell();
		shell.pack();
		Rectangle bounds = shell.getBounds();
		int width = bounds.width + 2 * borderWidth;
		int height = bounds.height + 2 * borderWidth;
		shell.setBounds(bounds.x, bounds.y, width, height);
	}

	private void disposeImage() {
		if (imageLabel != null && imageLabel.getImage() != null
				&& !imageLabel.getImage().isDisposed())
			imageLabel.getImage().dispose();
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return parent;
	}
}
