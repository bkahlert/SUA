package de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.widgets;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.bkahlert.devel.nebula.widgets.RoundedComposite;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
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
	private Link lblUrlvalue;
	private Label lblIpvalue;
	private Label lblPropxyipvalue;
	private Label lblActionvalue;
	private Label lblParamvalue;
	private Label lblBoundsvalue;
	private Label lblDatevalue;
	private Label lblTimepassedvalue;

	private static final String timeDifferenceFormat = new SUACorePreferenceUtil()
			.getTimeDifferenceFormat();

	public DoclogDetailDialog(Shell parentShell, DoclogTimeline doclogTimeline,
			DoclogRecord currentDoclogRecord) {
		super(parentShell);
		this.doclogTimeline = doclogTimeline;
		this.currentDoclogRecord = currentDoclogRecord;
	}

	@Override
	protected Point getInitialLocation(Point initialSize) {
		return new Point(100, 70);
	}

	@Override
	public void create() {
		super.create();
		loadDoclogRecord(currentDoclogRecord);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.marginWidth = borderWidth;
		gl_composite.marginHeight = borderWidth;
		gl_composite.horizontalSpacing = borderWidth;
		composite.setLayout(gl_composite);

		Composite metaComposite = new RoundedComposite(composite, SWT.BORDER);
		metaComposite.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_LIST_BACKGROUND));
		metaComposite.setLayout(new GridLayout(2, false));
		metaComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1));

		Label lblUrl = new Label(metaComposite, SWT.NONE);
		lblUrl.setText("URL");

		lblUrlvalue = new Link(metaComposite, SWT.NONE);
		lblUrlvalue.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_LIST_BACKGROUND));
		lblUrlvalue.setText("urlValue");
		lblUrlvalue.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				// TODO: Click funktioniert nicht
				openURL(currentDoclogRecord.getUrl());
			}
		});

		Label lblIp = new Label(metaComposite, SWT.NONE);
		lblIp.setText("IP");

		lblIpvalue = new Label(metaComposite, SWT.NONE);
		lblIpvalue.setText("ipValue");

		Label lblProxyip = new Label(metaComposite, SWT.NONE);
		lblProxyip.setText("Proxy IP");

		lblPropxyipvalue = new Label(metaComposite, SWT.NONE);
		lblPropxyipvalue.setText("proxyipValue");

		Label lblAction = new Label(metaComposite, SWT.NONE);
		lblAction.setText("Action");

		lblActionvalue = new Label(metaComposite, SWT.NONE);
		lblActionvalue.setText("actionValue");

		Label lblParam = new Label(metaComposite, SWT.NONE);
		lblParam.setText("Param");

		lblParamvalue = new Label(metaComposite, SWT.NONE);
		lblParamvalue.setText("paramValue");

		Label lblBounds = new Label(metaComposite, SWT.NONE);
		lblBounds.setText("Bounds");

		lblBoundsvalue = new Label(metaComposite, SWT.NONE);
		lblBoundsvalue.setText("boundsValue");

		Label lblDate = new Label(metaComposite, SWT.NONE);
		lblDate.setText("Date");

		lblDatevalue = new Label(metaComposite, SWT.NONE);
		lblDatevalue.setText("dateValue");

		Label lblTimePassed = new Label(metaComposite, SWT.NONE);
		lblTimePassed.setText("Time Passed");

		lblTimepassedvalue = new Label(metaComposite, SWT.NONE);
		lblTimepassedvalue.setText("timePassedValue");

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

		this.lblUrlvalue
				.setText(doclogRecord.getShortUrl() != null ? doclogRecord
						.getShortUrl() : "-");
		this.lblIpvalue.setText(doclogRecord.getIp() != null ? doclogRecord
				.getIp() : "-");
		this.lblPropxyipvalue
				.setText(doclogRecord.getProxyIp() != null ? doclogRecord
						.getProxyIp() : "-");
		this.lblActionvalue
				.setText(doclogRecord.getAction() != null ? doclogRecord
						.getAction().toString() : "-");
		this.lblParamvalue
				.setText(doclogRecord.getActionParameter() != null ? doclogRecord
						.getActionParameter() : "-");
		this.lblBoundsvalue
				.setText((doclogRecord.getScrollPosition() != null ? doclogRecord
						.getScrollPosition().x
						+ ","
						+ doclogRecord.getScrollPosition().y : "unknown")
						+ " - "
						+ (doclogRecord.getWindowDimensions() != null ? +doclogRecord
								.getWindowDimensions().x
								+ ","
								+ doclogRecord.getWindowDimensions().y
								: "unknown"));
		this.lblDatevalue
				.setText((doclogRecord.getDateRange() != null && doclogRecord
						.getDateRange().getStartDate() != null) ? doclogRecord
						.getDateRange().getStartDate().toISO8601() : "-");

		Long milliSecondsPassed = doclogRecord.getDateRange() != null ? doclogRecord
				.getDateRange().getDifference() : null;
		this.lblTimepassedvalue
				.setText((milliSecondsPassed != null) ? DurationFormatUtils
						.formatDuration(milliSecondsPassed,
								timeDifferenceFormat, true) : "unknown");

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
		if (doclogRecord.getAction() == DoclogAction.BLUR
				|| doclogRecord.getAction() == DoclogAction.UNLOAD) {
			GC gc = new GC(image);
			gc.setBackground(background);
			gc.setAlpha(70);
			gc.fillRectangle(image.getBounds());
			gc.dispose();
		}

		imageLabel.setImage(image);
		imageLabel.getParent().setBackground(background);

		this.doclogTimeline.center(currentDoclogRecord.getDateRange());

		((Composite) this.getContents()).layout();
		Shell shell = this.getShell();
		shell.pack();
		// Rectangle bounds = shell.getBounds();
		// int width = bounds.width + 2 * borderWidth;
		// int height = bounds.height + 2 * borderWidth;
		// shell.setBounds(bounds.x, bounds.y, width, height);
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
						openURL(currentDoclogRecord.getUrl());
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

	public static void openURL(String url) {
		if (url != null) {
			org.eclipse.swt.program.Program.launch(url);
		}
	}

}
