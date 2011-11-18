package de.fu_berlin.imp.seqan.usability_analyzer.doclog.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot;
import de.fu_berlin.inf.nebula.utils.ImageUtils;
import de.fu_berlin.inf.nebula.widgets.RoundedComposite;

public class DoclogScreenshotDisplay extends RoundedComposite {

	private LocalResourceManager resources = new LocalResourceManager(
			JFaceResources.getResources(), this);

	private Label imageLabel;

	private Label urlLabel;
	private Label actionLabel;
	private Label windowDimensionsLabel;
	private Label actualDimensionsLabel;
	private Label scrollPositionLabel;

	public DoclogScreenshotDisplay(Composite parent, int style) {
		super(parent, style);

		this.setLayout(GridLayoutFactory.fillDefaults().numColumns(3)
				.margins(5, 5).create());

		this.imageLabel = new Label(this, SWT.NONE);
		this.imageLabel.setLayoutData(GridDataFactory.fillDefaults().span(1, 5)
				.create());

		createKeyLabel("URL:");
		this.urlLabel = createValueLabel();

		createKeyLabel("Action:");
		this.actionLabel = createValueLabel();

		createKeyLabel("Size:");
		this.windowDimensionsLabel = createValueLabel();

		createKeyLabel("Actual size:");
		this.actualDimensionsLabel = createValueLabel();

		createKeyLabel("Scroll:");
		this.scrollPositionLabel = createValueLabel();

		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				disposeScreenshot();
				resources.dispose();
			}
		});
	}

	private Label createKeyLabel(String text) {
		Label label = new Label(this, SWT.NONE);
		label.setText(text);
		label.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.BEGINNING, SWT.BEGINNING).create());
		return label;
	}

	private Label createValueLabel() {
		Label label = new Label(this, SWT.WRAP);
		label.setLayoutData(GridDataFactory.swtDefaults().grab(true, false)
				.align(SWT.BEGINNING, SWT.BEGINNING).create());
		return label;
	}

	public void setScreenshot(DoclogScreenshot doclogScreenshot,
			int screenshotWidth) {
		updateScreenshot(doclogScreenshot, screenshotWidth);
		updateUrl(doclogScreenshot);
		updateAction(doclogScreenshot);
		updateWindowDimensions(doclogScreenshot);
		updateActualDimensions(doclogScreenshot);
		updateScrollPosition(doclogScreenshot);

		updateColors(doclogScreenshot);
	}

	private void updateScreenshot(DoclogScreenshot doclogScreenshot,
			int screenshotWidth) {
		disposeScreenshot();

		ImageData imageData = doclogScreenshot.getImageData();
		if (imageData != null) {
			this.imageLabel.setImage(createScaledImage(imageData,
					Math.min(screenshotWidth, imageData.width)));
		} else {
			this.imageLabel.setText("NO SCREENSHOT");
		}
	}

	private void disposeScreenshot() {
		if (this.imageLabel != null && !this.imageLabel.isDisposed()
				&& this.imageLabel.getImage() != null
				&& !this.imageLabel.getImage().isDisposed()) {
			this.imageLabel.getImage().dispose();
			this.imageLabel.setImage(null);
		}
	}

	private void updateUrl(DoclogScreenshot doclogScreenshot) {
		this.urlLabel.setText(doclogScreenshot.getDoclogRecord().getUrl());
	}

	private void updateAction(DoclogScreenshot doclogScreenshot) {
		String action = doclogScreenshot.getDoclogRecord().getAction()
				.toString();
		String actionParameter = doclogScreenshot.getDoclogRecord()
				.getActionParameter();
		this.actionLabel
				.setText(action
						+ ((actionParameter != null) ? "(" + actionParameter
								+ ")" : ""));
	}

	private void updateWindowDimensions(DoclogScreenshot doclogScreenshot) {
		Point windowDimensions = doclogScreenshot.getDoclogRecord()
				.getWindowDimensions();
		this.windowDimensionsLabel.setText(windowDimensions.x + " x "
				+ windowDimensions.y);
	}

	private void updateActualDimensions(DoclogScreenshot doclogScreenshot) {
		ImageData imageData = doclogScreenshot.getImageData();
		if (imageData != null) {
			this.actualDimensionsLabel.setText(imageData.width + " x "
					+ imageData.height);
		} else {
			this.actualDimensionsLabel.setText("-");
		}
	}

	private void updateScrollPosition(DoclogScreenshot doclogScreenshot) {
		Point scrollPosition = doclogScreenshot.getDoclogRecord()
				.getScrollPosition();
		this.scrollPositionLabel.setText(scrollPosition.x + " x "
				+ scrollPosition.y);
	}

	private void updateColors(DoclogScreenshot doclogScreenshot) {
		RGB backgroundRgb = doclogScreenshot.getStatus().getRGB();
		this.setBackground(resources.createColor(backgroundRgb));
	}

	protected Image createScaledImage(ImageData imageData, int newWidth) {
		int height = (int) Math.round(imageData.height
				* ((double) newWidth / imageData.width));
		Image scaledImage = ImageUtils.resize(imageData, newWidth, height);
		return scaledImage;
	}

	@Override
	public boolean setFocus() {
		System.err.println("FOCUS");
		return super.setFocus();
	}
}
