package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.devel.nebula.utils.ImageUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasFingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;

public class DoclogScreenshot implements HasDateRange, HasID, HasFingerprint {

	private static final Logger LOGGER = Logger
			.getLogger(DoclogScreenshot.class);

	public static enum Status implements Comparable<Status> {
		OK, DIRTY, MISSING, ERROR;

		public RGB getRGB() {
			SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();
			RGB rgb = null;
			switch (this) {
			case OK:
				rgb = preferenceUtil.getColorOk();
				break;
			case DIRTY:
				rgb = preferenceUtil.getColorDirty();
				break;
			case MISSING:
				rgb = preferenceUtil.getColorMissing();
				break;
			default:
				rgb = preferenceUtil.getColorError();
				break;
			}
			return rgb;
		}
	}

	public static final String FORMAT = "png";
	public static final String RELPATH = "/../screenshots/";
	public static final String RELFILE = "%s-%d,%d-%d,%d";
	public static final int MAX_FILENAME_LENGTH = 255;

	private DoclogRecord doclogRecord;

	private String filename;
	private ImageData imageData;
	private Status status;

	public DoclogScreenshot(DoclogRecord doclogRecord)
			throws UnsupportedEncodingException {
		this.doclogRecord = doclogRecord;

		this.filename = this.calculateFilename();
	}

	@Override
	public ID getID() {
		return this.doclogRecord.getID();
	}

	@Override
	public Fingerprint getFingerprint() {
		return this.doclogRecord.getFingerprint();
	}

	public DoclogRecord getDoclogRecord() {
		return this.doclogRecord;
	}

	public String calculateFilename() {
		String url;
		try {
			url = URLEncoder.encode(this.doclogRecord.getUrl(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Error calculating the screenshots filename", e);
			return null;
		}
		int windowWidth = this.doclogRecord.getWindowDimensions().x;
		int windowHeight = this.doclogRecord.getWindowDimensions().y;
		int scrollX = this.doclogRecord.getScrollPosition().x;
		int scrollY = this.doclogRecord.getScrollPosition().y;
		String relFile = String.format(RELFILE, url, windowWidth, windowHeight,
				scrollX, scrollY);

		// Use md5 if filename to long
		if (relFile.length() > MAX_FILENAME_LENGTH - 1 - FORMAT.length())
			relFile = DigestUtils.md5Hex(relFile);

		return this.doclogRecord.getDoclogPath().getAbsoluteFile().getParent()
				+ RELPATH + relFile + "." + FORMAT;
	}

	public ImageData calculateImageData() {
		try {
			Image image = new Image(Display.getDefault(), this.filename);
			ImageData imageData = image.getImageData();
			image.dispose();
			return imageData;
		} catch (Exception e) {
			return null;
		}
	}

	public Dimension calculateImageSize() throws IOException {
		Dimension imageSize = null;
		if (this.imageData != null) {
			imageSize = new Dimension(this.imageData.width,
					this.imageData.height);
		} else {
			imageSize = ImageUtils.getImageDimensions(this.getFilename());
		}
		return imageSize;
	}

	public Status calculateStatus() {
		try {
			Dimension imageSize = this.calculateImageSize();
			if (imageSize != null) {
				Point windowDimensions = this.doclogRecord
						.getWindowDimensions();

				boolean correctWidth = imageSize.width == windowDimensions.x;
				boolean correctHeight = imageSize.height == windowDimensions.y;

				/*
				 * We are not 100% interested in the navigation frame of the
				 * seqan.de documentation. If this computer can't produce the
				 * screenshots the screenshots height demands that's ok.
				 */
				if (this.doclogRecord.getUrl().contains("/INDEX_")
						&& Activator.getDefault() != null) {
					int maxHeight = Activator.getDefault().getMaxCaptureArea().height;

					if (windowDimensions.y > maxHeight
							&& imageSize.height >= maxHeight) {
						correctHeight = true;
					}
				}

				if (correctWidth && correctHeight) {
					return Status.OK;
				} else {
					return Status.DIRTY;
				}
			} else {
				return Status.MISSING;
			}
		} catch (Exception e) {
			return Status.ERROR;
		}
	}

	public String getFilename() {
		return this.filename;
	}

	public File getFile() throws IOException {
		return new File(this.filename);
	}

	public void setFile(File tempScreenshotFile) throws IOException {
		if (tempScreenshotFile == null)
			return;

		File screenshotFile = this.getFile();
		FileUtils.copyFile(tempScreenshotFile, screenshotFile);
		if (this.imageData != null) {
			this.imageData = this.calculateImageData();
		}
		this.status = this.calculateStatus();
	}

	public ImageData getImageData() {
		if (this.imageData == null)
			this.imageData = this.calculateImageData();
		return this.imageData;
	}

	public Dimension getImageSize() throws IOException {
		return this.calculateImageSize();
	}

	public Status getStatus() {
		if (this.status == null)
			this.status = this.calculateStatus();
		return this.status;
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		if (this.getDoclogRecord() != null)
			return this.getDoclogRecord().getDateRange();
		else
			return null;
	}

}
