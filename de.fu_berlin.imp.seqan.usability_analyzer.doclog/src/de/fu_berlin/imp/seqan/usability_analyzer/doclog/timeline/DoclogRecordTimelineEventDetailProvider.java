package de.fu_berlin.imp.seqan.usability_analyzer.doclog.timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.bkahlert.devel.nebula.utils.MathUtils;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.DefaultTimelineEventDetailProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineEventDetailProvider;

public class DoclogRecordTimelineEventDetailProvider extends
		DefaultTimelineEventDetailProvider<DoclogRecord> implements
		ITimelineEventDetailProvider<DoclogRecord> {

	private Label imageLabel = null;

	@Override
	public Class<DoclogRecord> getType() {
		return DoclogRecord.class;
	}

	@Override
	public List<IllustratedText> getMetaInformation(DoclogRecord doclogRecord) {
		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		metaEntries
				.add(new IllustratedText(
						de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.ImageManager.DOCLOGRECORD,
						DoclogRecord.class.getSimpleName()));

		String detailText = null;
		Image detailIcon = null;
		switch (doclogRecord.getAction()) {
		case READY:
			detailText = doclogRecord.getAction().toString();
			detailIcon = ImageManager.CREATED;
		case RESIZE:
			break;
		case SCROLL:
			DoclogRecord prevDoclogRecord = doclogRecord.getDoclog()
					.getPrevDoclogRecord(doclogRecord);

			if (prevDoclogRecord == null) {
				detailText = "0px";
				detailIcon = ImageManager.SCROLL;
			} else if (prevDoclogRecord.getScrollPosition().y < doclogRecord
					.getScrollPosition().y) {
				detailText = "+"
						+ (doclogRecord.getScrollPosition().y - prevDoclogRecord
								.getScrollPosition().y) + "px";
				detailIcon = ImageManager.SCROLL_DOWN;
			} else if (prevDoclogRecord.getScrollPosition().y > doclogRecord
					.getScrollPosition().y) {
				detailText = "-"
						+ (prevDoclogRecord.getScrollPosition().y - doclogRecord
								.getScrollPosition().y) + "px";
				detailIcon = ImageManager.SCROLL_UP;
			} else if (prevDoclogRecord.getScrollPosition().y == doclogRecord
					.getScrollPosition().y) {
				detailText = "0px";
				detailIcon = ImageManager.SCROLL;
			}
			break;
		case TYPING:
			detailText = doclogRecord.getAction().toString();
			break;
		case BLUR:
			detailText = doclogRecord.getAction().toString();
			break;
		case FOCUS:
			detailText = doclogRecord.getAction().toString();
			break;
		case LINK:
			detailText = doclogRecord.getActionParameter();
			break;
		case SURVEY:
			detailText = doclogRecord.getAction().toString();
			break;
		case UNLOAD:
			detailText = doclogRecord.getAction().toString();
			detailIcon = ImageManager.CLOSE;
			break;
		default:
			break;
		}

		if (detailText != null || detailIcon != null) {
			metaEntries.add(new IllustratedText(detailIcon, detailText));
		}

		return metaEntries;
	}

	@Override
	public List<Entry<String, String>> getDetailInformation(
			DoclogRecord doclogRecord) {
		List<Entry<String, String>> detailEntries = new ArrayList<Entry<String, String>>();
		detailEntries.add(new DetailEntry("URL",
				doclogRecord.getShortUrl() != null ? doclogRecord.getShortUrl()
						: "-"));
		detailEntries.add(new DetailEntry("IP",
				doclogRecord.getIp() != null ? doclogRecord.getIp() : "-"));
		detailEntries.add(new DetailEntry("Proxy IP",
				doclogRecord.getProxyIp() != null ? doclogRecord.getProxyIp()
						: "-"));
		detailEntries.add(new DetailEntry("Action",
				doclogRecord.getAction() != null ? doclogRecord.getAction()
						.toString() : "-"));
		detailEntries.add(new DetailEntry("Action Parameter", doclogRecord
				.getActionParameter() != null ? doclogRecord
				.getActionParameter() : "-"));
		detailEntries.add(new DetailEntry("Scroll Position",
				doclogRecord.getScrollPosition() != null ? doclogRecord
						.getScrollPosition().x
						+ ", "
						+ doclogRecord.getScrollPosition().y : "-"));
		detailEntries.add(new DetailEntry("Window Size", doclogRecord
				.getWindowDimensions() != null ? doclogRecord
				.getWindowDimensions().x
				+ ", "
				+ doclogRecord.getWindowDimensions().y : "-"));
		detailEntries.add(new DetailEntry("Date",
				(doclogRecord.getDateRange() != null && doclogRecord
						.getDateRange().getStartDate() != null) ? doclogRecord
						.getDateRange().getStartDate().toISO8601() : "-"));

		Long milliSecondsPassed = doclogRecord.getDateRange() != null ? doclogRecord
				.getDateRange().getDifference() : null;
		detailEntries.add(new DetailEntry("Time Passed",
				(milliSecondsPassed != null) ? DurationFormatUtils
						.formatDuration(milliSecondsPassed,
								new SUACorePreferenceUtil()
										.getTimeDifferenceFormat(), true)
						: "unknown"));
		return detailEntries;
	}

	@Override
	public void fillCustomComposite(Composite parent,
			DoclogRecord doclogRecord, ITimeline timeline) {
		imageLabel = new Label(parent, SWT.NONE);

		disposeImage();

		Color background = getBackground(doclogRecord, timeline);
		ImageData imageData = doclogRecord.getScreenshot().getImageData();
		if (imageData == null)
			return;

		Image image = new Image(Display.getCurrent(), imageData);
		switch (doclogRecord.getAction()) {
		case READY:
			DoclogRecordTimelineEventDetailProvider.drawOverlay(doclogRecord,
					image, ImageManager.CREATE_DETAIL_OVERLAY);
			break;
		case SCROLL:
			DoclogRecordTimelineEventDetailProvider.drawScrollDirectionOverlay(
					doclogRecord, image);
			break;
		case BLUR:
			GC gc = new GC(image);
			gc.setBackground(background);
			gc.setAlpha(70);
			gc.fillRectangle(image.getBounds());
			gc.dispose();
			break;
		case UNLOAD:
			DoclogRecordTimelineEventDetailProvider.drawOverlay(doclogRecord,
					image, ImageManager.CLOSE_DETAIL_OVERLAY);
			break;
		}

		imageLabel.setImage(image);
		imageLabel.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				disposeImage();
			}
		});
	}

	private void disposeImage() {
		if (imageLabel != null && imageLabel.getImage() != null
				&& !imageLabel.getImage().isDisposed())
			imageLabel.getImage().dispose();
	}

	public static void drawScrollDirectionOverlay(DoclogRecord doclogRecord,
			Image image) {
		DoclogRecord prevDoclogRecord = doclogRecord.getDoclog()
				.getPrevDoclogRecord(doclogRecord);
		if (prevDoclogRecord == null)
			return;

		Rectangle arrowBounds = ImageManager.ARROW_TOP_DETAIL_OVERLAY
				.getBounds();

		Rectangle resizedArrowBounds = MathUtils.resizeRectangle(new Point(
				arrowBounds.width, arrowBounds.height),
				new Rectangle(0, 0, (int) (image.getBounds().width * 0.75),
						(int) (image.getBounds().height * 0.75)));

		double direction = MathUtils.calcDegreeBetweenPoints(
				prevDoclogRecord.getScrollPosition(),
				doclogRecord.getScrollPosition());

		if (Double.isNaN(direction)) {
			drawOverlay(doclogRecord, image,
					ImageManager.ARROW_TOP_DOWN_DETAIL_OVERLAY);
		} else {
			GC gc = new GC(image);
			Transform transform = new Transform(Display.getCurrent());
			transform.translate(image.getBounds().width / 2,
					image.getBounds().height / 2);
			transform.rotate((float) direction - 90); // -90 because the initial
														// graphic direction is
														// incorrect
			gc.setTransform(transform);
			gc.setAlpha(70);
			gc.drawImage(ImageManager.ARROW_TOP_DETAIL_OVERLAY, 0, 0,
					arrowBounds.width, arrowBounds.height,
					(-resizedArrowBounds.width) / 2,
					(-resizedArrowBounds.height) / 2, resizedArrowBounds.width,
					resizedArrowBounds.height);
			transform.dispose();
			gc.dispose();
		}
	}

	private static void drawOverlay(DoclogRecord doclogRecord, Image image,
			Image overlay) {
		Rectangle overlayBounds = overlay.getBounds();
		Rectangle resizedOverlayBounds = MathUtils.resizeRectangle(new Point(
				overlayBounds.width, overlayBounds.height),
				new Rectangle(0, 0, (int) (image.getBounds().width * 0.75),
						(int) (image.getBounds().height * 0.75)));

		GC gc = new GC(image);
		gc.setAlpha(70);
		gc.drawImage(overlay, 0, 0, overlayBounds.width, overlayBounds.height,
				(image.getBounds().width - resizedOverlayBounds.width) / 2,
				(image.getBounds().height - resizedOverlayBounds.height) / 2,
				resizedOverlayBounds.width, resizedOverlayBounds.height);
		gc.dispose();
	}
}
