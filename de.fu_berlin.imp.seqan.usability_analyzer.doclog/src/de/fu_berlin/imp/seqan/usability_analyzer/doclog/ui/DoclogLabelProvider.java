package de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.MathUtils;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.nebula.widgets.image.Image.FILL_MODE;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IUriPresenterService.StyledUriInformationLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class DoclogLabelProvider extends StyledUriInformationLabelProvider {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(DoclogLabelProvider.class);

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	@Override
	public StyledString getStyledText(URI uri) throws Exception {
		if (this.locatorService.getType(uri) == Doclog.class) {
			return new StyledString(URIUtils.getIdentifier(uri).toString());
		}
		if (this.locatorService.getType(uri) == DoclogRecord.class) {
			DoclogRecord doclogRecord = this.locatorService.resolve(uri,
					DoclogRecord.class, null).get();
			String url = doclogRecord.getUrl();
			if (url != null) {
				url = url.replaceAll(".*://", "");
			}
			Integer scrollY = doclogRecord.getScrollPosition() != null ? doclogRecord
					.getScrollPosition().y : null;
			if (url != null) {
				return new StyledString(scrollY != null ? url + " ⇅" + scrollY
						: url);
			}
		}
		return new StyledString("ERROR", Stylers.ATTENTION_STYLER);
	}

	private final Map<String, Image> cachedImages = new HashMap<String, Image>();

	@Override
	public Image getImage(URI uri) throws Exception {
		if (this.locatorService.getType(uri) == Doclog.class) {
			try {
				return (this.codeService.getCodes(uri).size() > 0) ? (this.codeService
						.isMemo(uri) ? ImageManager.DOCLOGFILE_CODED_MEMO
						: ImageManager.DOCLOGFILE_CODED) : (this.codeService
						.isMemo(uri) ? ImageManager.DOCLOGFILE_MEMO
						: ImageManager.DOCLOGFILE);
			} catch (CodeServiceException e) {
				return ImageManager.DOCLOGFILE;
			}
		}
		if (this.locatorService.getType(uri) == DoclogRecord.class) {
			String key = null;
			Image image = null;
			DoclogRecord doclogRecord = this.locatorService.resolve(uri,
					DoclogRecord.class, null).get();
			if (doclogRecord != null) {
				key = doclogRecord.getAction().name();
				switch (doclogRecord.getAction()) {
				case BLUR:
					image = ImageManager.DOCLOGACTION_BLUR;
					break;
				case FOCUS:
					image = ImageManager.DOCLOGACTION_FOCUS;
					break;
				case LINK:
					image = ImageManager.DOCLOGACTION_LINK;
					break;
				case READY:
					image = ImageManager.DOCLOGACTION_READY;
					break;
				case RESIZE:
					image = ImageManager.DOCLOGACTION_RESIZE;
					break;
				case SCROLL:
					image = ImageManager.DOCLOGACTION_SCROLL;
					break;
				case SURVEY:
					image = ImageManager.DOCLOGACTION_SURVEY;
					break;
				case TYPING:
					image = ImageManager.DOCLOGACTION_TYPING;
					break;
				case UNLOAD:
					image = ImageManager.DOCLOGACTION_UNLOAD;
					break;
				default:
					image = ImageManager.DOCLOGACTION_UNKNOWN;
					break;
				}
			} else {
				image = ImageManager.DOCLOGRECORD;
			}

			try {
				if (this.codeService.getCodes(uri).size() > 0) {
					if (this.codeService.isMemo(uri)) {
						if (key != null) {
							key += ",coded,memo";
						}
						if (this.cachedImages.containsKey(key)) {
							return this.cachedImages.get(key);
						}
						image = de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager
								.applyCodedMemoOverlay(image);
					} else {
						if (key != null) {
							key += ",coded";
						}
						if (this.cachedImages.containsKey(key)) {
							return this.cachedImages.get(key);
						}
						image = de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager
								.applyCodedOverlay(image);
					}
				} else {
					if (this.codeService.isMemo(uri)) {
						if (key != null) {
							key += ",memo";
						}
						if (this.cachedImages.containsKey(key)) {
							return this.cachedImages.get(key);
						}
						image = de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager
								.applyMemoOverlay(image);
					}
				}
			} catch (CodeServiceException e) {
				return image;
			}

			if (key != null) {
				this.cachedImages.put(key, image);
				return image;
			}
		}
		return PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
	}

	@Override
	public boolean hasInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		if (locatable instanceof DoclogRecord) {
			return true;
		}
		return false;
	}

	@Override
	public List<IllustratedText> getMetaInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		if (locatable instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) locatable;
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
	public List<IDetailEntry> getDetailInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		List<IDetailEntry> detailEntries = new ArrayList<IDetailEntry>();
		if (locatable instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) locatable;
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

			TimeZoneDateRange range = doclogRecord.getDateRange();
			detailEntries.add(new DetailEntry("Time Passed",
					(range != null) ? range.formatDuration() : "?"));
		}
		return detailEntries;
	}

	private final Map<Composite, com.bkahlert.nebula.widgets.image.Image> images = new HashMap<Composite, com.bkahlert.nebula.widgets.image.Image>();

	@Override
	public Control fillInformation(URI uri, final Composite composite)
			throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		if (locatable instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) locatable;

			ImageData imageData = doclogRecord.getScreenshot().getImageData();
			if (imageData == null) {
				return null;
			}

			Image originalImage = new Image(Display.getCurrent(), imageData);
			Image image = new Image(Display.getCurrent(), imageData.width - 2,
					imageData.height - 2);
			GC originalGC = new GC(originalImage);
			originalGC.copyArea(image, 1, 1);
			originalGC.dispose();
			originalImage.dispose();

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
				gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
				gc.setAlpha(70);
				gc.fillRectangle(image.getBounds());
				gc.dispose();
				break;
			case UNLOAD:
				drawOverlay(doclogRecord, image,
						ImageManager.CLOSE_DETAIL_OVERLAY);
				break;
			case UNKNOWN:
				drawOverlay(
						doclogRecord,
						PlatformUI.getWorkbench().getSharedImages()
								.getImage(ISharedImages.IMG_OBJS_WARN_TSK),
						ImageManager.CREATE_DETAIL_OVERLAY);
			default:
			}

			Rectangle monitorBounds = Display.getCurrent().getPrimaryMonitor()
					.getBounds();
			Point maxSize = new Point(
					(int) Math.round(monitorBounds.width * 0.5),
					(int) Math.round(monitorBounds.height * 0.45));
			Point size = Geometry.getSize(image.getBounds());
			if (size.x > maxSize.x || size.y > maxSize.y) {
				size = ImageUtils.resizeWithinArea(
						Geometry.getSize(image.getBounds()), maxSize);
			}

			if (!this.images.containsKey(composite)) {
				com.bkahlert.nebula.widgets.image.Image img = new com.bkahlert.nebula.widgets.image.Image(
						composite, SWT.NONE, size, FILL_MODE.INNER_FILL);
				this.images.put(composite, img);
				img.setBackground(Display.getCurrent().getSystemColor(
						SWT.COLOR_INFO_BACKGROUND));
				img.limitToOriginalSize();
			}
			this.images.get(composite).load(image, new Runnable() {
				@Override
				public void run() {
					composite.getShell().layout();
				}
			});
			image.dispose();
			return this.images.get(composite);
		}
		return null;
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

	@Override
	public void fill(URI uri, ToolBarManager toolBarManager) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		if (locatable instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) locatable;

			if (doclogRecord.getUrl() != null) {
				final String url = doclogRecord.getUrl();
				Action openUrl = new Action() {
					@Override
					public String getText() {
						return "Open URL";
					}

					@Override
					public void run() {
						org.eclipse.swt.program.Program.launch(url);
					}
				};
				toolBarManager.add(openUrl);
			}
		}
	}

}
