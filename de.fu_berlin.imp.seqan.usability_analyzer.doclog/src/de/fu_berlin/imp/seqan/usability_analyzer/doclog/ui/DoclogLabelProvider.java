package de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.MathUtils;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService.InformationLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class DoclogLabelProvider extends InformationLabelProvider {

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private Label imageLabel = null;

	@Override
	public String getText(Object element) {
		if (element instanceof Doclog) {
			Doclog doclog = (Doclog) element;
			return doclog.getIdentifier().toString();
		}
		if (element instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) element;
			String url = doclogRecord.getUrl();
			if (url != null) {
				url = url.replaceAll(".*://", "");
			}
			Integer scrollY = doclogRecord.getScrollPosition() != null ? doclogRecord
					.getScrollPosition().y : null;
			return url != null ? (scrollY != null ? url + " ⇅" + scrollY : url)
					: "ERROR";
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof Doclog) {
			Doclog doclog = (Doclog) element;
			try {
				return (this.codeService.getCodes(doclog).size() > 0) ? (this.codeService
						.isMemo(doclog) ? ImageManager.DOCLOGFILE_CODED_MEMO
						: ImageManager.DOCLOGFILE_CODED) : (this.codeService
						.isMemo(doclog) ? ImageManager.DOCLOGFILE_MEMO
						: ImageManager.DOCLOGFILE);
			} catch (CodeServiceException e) {
				return ImageManager.DOCLOGFILE;
			}
		}
		if (element instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) element;
			try {
				return (this.codeService.getCodes(doclogRecord).size() > 0) ? (this.codeService
						.isMemo(doclogRecord) ? ImageManager.DOCLOGRECORD_CODED_MEMO
						: ImageManager.DOCLOGRECORD_CODED)
						: (this.codeService.isMemo(doclogRecord) ? ImageManager.DOCLOGRECORD_MEMO
								: ImageManager.DOCLOGRECORD);
			} catch (CodeServiceException e) {
				return ImageManager.DOCLOGRECORD;
			}
		}
		return super.getImage(element);
	}

	@Override
	public boolean hasInformation(Object element) {
		if (element instanceof DoclogRecord) {
			return true;
		}
		return super.hasInformation(element);
	}

	@Override
	public List<IllustratedText> getMetaInformation(Object element) {
		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		if (element instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) element;
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
		}
		return metaEntries;
	}

	@Override
	public List<IDetailEntry> getDetailInformation(Object element) {
		List<IDetailEntry> detailEntries = new ArrayList<IDetailEntry>();
		if (element instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) element;
			detailEntries.add(new DetailEntry("URL",
					doclogRecord.getShortUrl() != null ? doclogRecord
							.getShortUrl() : "-"));
			detailEntries.add(new DetailEntry("IP",
					doclogRecord.getIp() != null ? doclogRecord.getIp() : "-"));
			detailEntries.add(new DetailEntry("Proxy IP", doclogRecord
					.getProxyIp() != null ? doclogRecord.getProxyIp() : "-"));
			detailEntries.add(new DetailEntry("Action", doclogRecord
					.getAction() != null ? doclogRecord.getAction().toString()
					: "-"));
			detailEntries.add(new DetailEntry("Action Parameter", doclogRecord
					.getActionParameter() != null ? doclogRecord
					.getActionParameter() : "-"));
			detailEntries.add(new DetailEntry("Scroll Position", doclogRecord
					.getScrollPosition() != null ? doclogRecord
					.getScrollPosition().x
					+ ", "
					+ doclogRecord.getScrollPosition().y : "-"));
			detailEntries.add(new DetailEntry("Window Size", doclogRecord
					.getWindowDimensions() != null ? doclogRecord
					.getWindowDimensions().x
					+ ", "
					+ doclogRecord.getWindowDimensions().y : "-"));
			detailEntries.add(new DetailEntry("Date", (doclogRecord
					.getDateRange() != null && doclogRecord.getDateRange()
					.getStartDate() != null) ? doclogRecord.getDateRange()
					.getStartDate().toISO8601() : "-"));

			Long milliSecondsPassed = doclogRecord.getDateRange() != null ? doclogRecord
					.getDateRange().getDifference() : null;
			detailEntries.add(new DetailEntry("Time Passed",
					(milliSecondsPassed != null) ? DurationFormatUtils
							.formatDuration(milliSecondsPassed,
									new SUACorePreferenceUtil()
											.getTimeDifferenceFormat(), true)
							: "unknown"));
		}
		return detailEntries;
	}

	@Override
	public Control fillInformation(Object element, Composite composite) {
		if (element instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) element;
			this.imageLabel = new Label(composite, SWT.NONE);

			this.disposeImage();

			Color background = this.getBackground(doclogRecord);
			ImageData imageData = doclogRecord.getScreenshot().getImageData();
			if (imageData == null) {
				return null;
			}

			Image image = new Image(Display.getCurrent(), imageData);
			switch (doclogRecord.getAction()) {
			case READY:
				drawOverlay(doclogRecord, image,
						ImageManager.CREATE_DETAIL_OVERLAY);
				break;
			case SCROLL:
				drawScrollDirectionOverlay(doclogRecord, image);
				break;
			case BLUR:
				GC gc = new GC(image);
				gc.setBackground(background);
				gc.setAlpha(70);
				gc.fillRectangle(image.getBounds());
				gc.dispose();
				break;
			case UNLOAD:
				drawOverlay(doclogRecord, image,
						ImageManager.CLOSE_DETAIL_OVERLAY);
				break;
			}

			this.imageLabel.setImage(image);
			this.imageLabel.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					DoclogLabelProvider.this.disposeImage();
				}
			});
			return this.imageLabel;
		}
		return null;
	}

	private void disposeImage() {
		if (this.imageLabel != null && this.imageLabel.getImage() != null
				&& !this.imageLabel.getImage().isDisposed()) {
			this.imageLabel.getImage().dispose();
		}
	}

	private static void drawScrollDirectionOverlay(DoclogRecord doclogRecord,
			Image image) {
		DoclogRecord prevDoclogRecord = doclogRecord.getDoclog()
				.getPrevDoclogRecord(doclogRecord);
		if (prevDoclogRecord == null) {
			return;
		}

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
