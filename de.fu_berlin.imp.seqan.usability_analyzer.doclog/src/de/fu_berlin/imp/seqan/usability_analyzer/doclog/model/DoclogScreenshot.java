package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.utils.ImageUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public class DoclogScreenshot implements HasDateRange, HasIdentifier {

	@SuppressWarnings("unused")
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

	public static final com.bkahlert.nebula.screenshots.IScreenshotTaker.Format Format = com.bkahlert.nebula.screenshots.IScreenshotTaker.Format.PNG;
	public static final String OLD_RELFILE = "%s-%d,%d-%d,%d";
	public static final String NEW_RELFILE = "%s-%d,%d-%d,%d-%s-%s";
	public static final int MAX_FILENAME_LENGTH = 255;

	private final DoclogRecord doclogRecord;

	private final String old_filename;
	private final String new_filename;
	private ImageData imageData;
	private Status status;

	public DoclogScreenshot(DoclogRecord doclogRecord)
			throws UnsupportedEncodingException {
		this.doclogRecord = doclogRecord;

		String url = URLEncoder.encode(this.doclogRecord.getUrl(), "UTF-8");
		int windowWidth = this.doclogRecord.getWindowDimensions().x;
		int windowHeight = this.doclogRecord.getWindowDimensions().y;
		int scrollX = this.doclogRecord.getScrollPosition().x;
		int scrollY = this.doclogRecord.getScrollPosition().y;
		String action = this.doclogRecord.getAction().toString();
		String param = this.doclogRecord.getActionParameter() != null ? URLEncoder
				.encode(this.doclogRecord.getActionParameter(), "UTF-8") : "";
		String oldRelFile = String.format(OLD_RELFILE, url, windowWidth,
				windowHeight, scrollX, scrollY);
		String newRelFile = String.format(NEW_RELFILE, url, windowWidth,
				windowHeight, scrollX, scrollY, action, param);

		// Use md5 if filename to long
		if (oldRelFile.length() > MAX_FILENAME_LENGTH - 1
				- Format.getName().length()) {
			oldRelFile = DigestUtils.md5Hex(oldRelFile);
		}
		if (newRelFile.length() > MAX_FILENAME_LENGTH - 1
				- Format.getName().length()) {
			newRelFile = DigestUtils.md5Hex(newRelFile);
		}
		this.old_filename = oldRelFile + "." + Format;
		this.new_filename = newRelFile + "." + Format;
	}

	@Override
	public IIdentifier getIdentifier() {
		return this.doclogRecord.getIdentifier();
	}

	public DoclogRecord getDoclogRecord() {
		return this.doclogRecord;
	}

	public File getFile() throws IOException {
		IBaseDataContainer baseDataContainer = this.doclogRecord.getDoclog()
				.getBaseDataContainer();
		File file = baseDataContainer.getStaticFile("screenshots",
				this.new_filename);
		if (file == null) {
			file = baseDataContainer.getStaticFile("screenshots",
					this.old_filename);
		}
		return file;
	}

	public ImageData calculateImageData() {
		try {
			Image image = new Image(Display.getDefault(), this.getFile()
					.getAbsolutePath());
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
			imageSize = ImageUtils.getImageDimensions(this.getFile());
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

	public void setFile(File tempScreenshotFile) throws IOException {
		if (tempScreenshotFile == null) {
			return;
		}

		IBaseDataContainer baseDataContainer = this.doclogRecord.getDoclog()
				.getBaseDataContainer();
		baseDataContainer.putFile("screenshots", this.new_filename,
				tempScreenshotFile);
		if (this.imageData != null) {
			this.imageData = this.calculateImageData();
		}
		this.status = this.calculateStatus();
	}

	public ImageData getImageData() {
		if (this.imageData == null) {
			this.imageData = this.calculateImageData();
		}
		return this.imageData;
	}

	public Dimension getImageSize() throws IOException {
		return this.calculateImageSize();
	}

	public Status getStatus() {
		if (this.status == null) {
			this.status = this.calculateStatus();
		}
		return this.status;
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		if (this.getDoclogRecord() != null) {
			return this.getDoclogRecord().getDateRange();
		} else {
			return null;
		}
	}

}
