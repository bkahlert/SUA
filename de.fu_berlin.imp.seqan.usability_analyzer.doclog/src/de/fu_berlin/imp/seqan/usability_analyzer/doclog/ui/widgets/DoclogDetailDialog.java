package de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
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
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogAction;
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

	@Override
	public void create() {
		super.create();
		loadDoclogRecord(currentDoclogRecord);
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
					nextScreenshot();
				} else if (e.keyCode == SWT.ARROW_LEFT) {
					prevScreenshot();
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

		Color background = isIntersected ? COLOR_HIGHLIGHT : COLOR_STANDARD;
		Image image = new Image(Display.getCurrent(), doclogRecord
				.getScreenshot().getImageData());
		if (doclogRecord.getAction() == DoclogAction.BLUR) {
			GC gc = new GC(image);
			gc.setBackground(background);
			gc.setAlpha(70);
			gc.fillRectangle(image.getBounds());
			gc.dispose();
		}

		GC gc = new GC(image);
		gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		gc.setAlpha(200);
		List<String> description = new ArrayList<String>();
		description.add(doclogRecord.getUrl());
		description.add(doclogRecord.getAction().toString());
		description.add(doclogRecord.getActionParameter());
		gc.drawText(StringUtils.join(description, "\n"), 10, 10, false);
		gc.dispose();

		imageLabel.setImage(image);
		imageLabel.getParent().setBackground(background);

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
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.BACK_ID,
				IDialogConstants.BACK_LABEL, false).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						prevScreenshot();
					}
				});
		createButton(parent, IDialogConstants.NEXT_ID,
				IDialogConstants.NEXT_LABEL, false).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						nextScreenshot();
					}
				});
		createButton(parent, IDialogConstants.OPEN_ID, "Open URL", false)
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						openURL();
					}
				});
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	public void nextScreenshot() {
		DoclogRecord doclogRecord = currentDoclogRecord.getDoclog()
				.getNextDoclogRecord(currentDoclogRecord);
		if (doclogRecord != null) {
			currentDoclogRecord = doclogRecord;
			loadDoclogRecord(currentDoclogRecord);
		}
	}

	public void prevScreenshot() {
		DoclogRecord doclogRecord = currentDoclogRecord.getDoclog()
				.getPrevDoclogRecord(currentDoclogRecord);
		if (doclogRecord != null) {
			currentDoclogRecord = doclogRecord;
			loadDoclogRecord(currentDoclogRecord);
		}
	}

	public void openURL() {
		if (currentDoclogRecord != null) {
			org.eclipse.swt.program.Program
					.launch(currentDoclogRecord.getUrl());
		}
	}
}
