package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.DecorationOverlayIcon.ImageOverlay;
import com.bkahlert.nebula.utils.DecorationOverlayIcon.ImageOverlay.Quadrant;
import com.bkahlert.nebula.utils.DecorationOverlayIcon.ImageOverlayImpl;
import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.PaintUtils;
import com.bkahlert.nebula.utils.PartRenamer;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.utils.Triple;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IImportanceService;
import de.fu_berlin.imp.apiua.core.services.IImportanceService.Importance;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.IUriPresenterService.StyledUriInformationLabelProvider;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisodes;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;

public final class GTLabelProvider extends StyledUriInformationLabelProvider {

	public static final int HIGH_BACKGROUND_ALPHA = 255;
	public static final int HIGH_BORDER_ALPHA = 255;

	public static final int LOW_BACKGROUND_ALPHA = 0;
	public static final int LOW_BORDER_ALPHA = 120;

	public static final int DEFAULT_BACKGROUND_ALPHA = 70;
	public static final int DEFAULT_BORDER_ALPHA = 100;

	public static class CodeColors {
		private final RGB backgroundRGB;
		private final RGB borderRGB;
		private Color backgroundColor = null;
		private Color borderColor = null;

		public CodeColors(RGB backgroundRGB) {
			this.backgroundRGB = backgroundRGB != null ? backgroundRGB
					: new RGB(EpisodeRenderer.DEFAULT_BACKGROUND_COLOR.getRGB());
			this.borderRGB = ColorUtils.scaleLightnessBy(this.backgroundRGB,
					.85f);

			this.backgroundColor = new Color(Display.getCurrent(),
					backgroundRGB.toClassicRGB());

			this.borderColor = new Color(Display.getDefault(),
					this.borderRGB.toClassicRGB());
		}

		public RGB getBackgroundRGB() {
			return this.backgroundRGB;
		}

		public RGB getBorderRGB() {
			return this.borderRGB;
		}

		public Color getBackgroundColor() {
			if (this.backgroundColor == null) {
				this.backgroundColor = new Color(Display.getCurrent(),
						this.backgroundRGB.toClassicRGB());
			}
			return this.backgroundColor;
		}

		public Color getBorderColor() {
			if (this.borderColor == null) {
				this.borderColor = new Color(Display.getCurrent(),
						this.borderRGB.toClassicRGB());
			}
			return this.borderColor;
		}

		public void dispose() {
			if (EpisodeRenderer.DEFAULT_BACKGROUND_COLOR != this.backgroundColor
					&& this.backgroundColor != null
					&& !this.backgroundColor.isDisposed()) {
				this.backgroundColor.dispose();
			}
			if (this.borderColor != null && !this.borderColor.isDisposed()) {
				this.borderColor.dispose();
			}
		}
	}

	private static final Logger LOGGER = Logger
			.getLogger(GTLabelProvider.class);

	private static final IImportanceService IMPORTANCE_SERVICE = (IImportanceService) PlatformUI
			.getWorkbench().getService(IImportanceService.class);

	private final ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	/**
	 * Caches image that shows the color of a {@link ICode}.
	 */
	private static final Map<ICode, Image> codeImages = new HashMap<ICode, Image>();

	/**
	 * Caches color and importance of {@link ICode} when image was created for.
	 * This is used to re-create the image if the color or importance changed.
	 */
	private static final Map<ICode, RGB> codeColors = new HashMap<ICode, RGB>();
	private static final Map<ICode, Importance> codeImportances = new HashMap<ICode, IImportanceService.Importance>();

	/**
	 * Returns the {@link ICode}s effective color ... the color also reflecting
	 * the {@link ICode}'s {@link Importance}.
	 * 
	 * @param code
	 * @return
	 */
	public static RGB getCodeColor(ICode code) {
		Importance importance = IMPORTANCE_SERVICE.getImportance(code.getUri());
		RGB color = code.getColor();
		switch (importance) {
		case HIGH:
			return new RGB(color.getRed(), color.getGreen(), color.getBlue(),
					HIGH_BACKGROUND_ALPHA / 255f);
		case LOW:
			return new RGB(color.getRed(), color.getGreen(), color.getBlue(),
					LOW_BACKGROUND_ALPHA / 255f);
		default:
			return new RGB(color.getRed(), color.getGreen(), color.getBlue(),
					DEFAULT_BACKGROUND_ALPHA / 255f);
		}
	}

	public static void drawCodeImage(ICode code, GC gc, Rectangle bounds) {
		Importance importance = IMPORTANCE_SERVICE.getImportance(code.getUri());

		GTLabelProvider.CodeColors info = new GTLabelProvider.CodeColors(
				code.getColor());

		int oldAlpha = gc.getAlpha();
		int backgroundAlpha;
		int borderAlpha;
		switch (importance) {
		case HIGH:
			backgroundAlpha = HIGH_BACKGROUND_ALPHA;
			borderAlpha = HIGH_BORDER_ALPHA;
			break;
		case LOW:
			backgroundAlpha = LOW_BACKGROUND_ALPHA;
			borderAlpha = LOW_BORDER_ALPHA;
			break;
		default:
			backgroundAlpha = DEFAULT_BACKGROUND_ALPHA;
			borderAlpha = DEFAULT_BORDER_ALPHA;
			break;
		}
		gc.setAlpha(backgroundAlpha);
		PaintUtils.drawRoundedRectangle(gc, bounds, info.getBackgroundColor());
		gc.setAlpha(borderAlpha);
		PaintUtils.drawRoundedBorder(gc, bounds, info.getBorderColor());
		gc.setAlpha(oldAlpha);
	}

	/**
	 * Returns the image that shows the color of the given {@link ICode}.
	 * 
	 * @param code
	 * @return
	 */
	public static Image getCodeImage(ICode code) {
		RGB color = code.getColor();
		Importance importance = IMPORTANCE_SERVICE.getImportance(code.getUri());
		if (codeColors.containsKey(code) || codeImportances.containsKey(code)) {
			if (!codeColors.get(code).equals(color)
					|| !codeImportances.get(code).equals(importance)) {
				// we don't dispose the outdated image since it could still be
				// used. codeImages.get(code).dispose();
				codeImages.remove(code);
			}
		}
		codeColors.put(code, color);
		codeImportances.put(code, importance);

		if (!codeImages.containsKey(code)) {
			int width = 16;
			int height = 16;
			Image image = new Image(Display.getCurrent(), width, height);
			GC gc = new GC(image);
			drawCodeImage(code, gc, image.getBounds());
			gc.dispose();

			// adds transparency
			ImageData imageData = image.getImageData();
			image.dispose();
			Ellipse2D.Double ellipse = new Ellipse2D.Double(0.0, 0.0, width,
					height);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int alpha = 0;
					if (ellipse.contains(x + 0.5, y + 0.5)) {
						alpha = 255;
					}
					imageData.setAlpha(x, y, alpha);
				}
			}
			codeImages.put(code, new Image(Display.getCurrent(), imageData));
		}
		return codeImages.get(code);
	}

	/**
	 * Returns the {@link URI} point to the image that shows the color of the
	 * given {@link ICode}.
	 * 
	 * @param code
	 * @return
	 */
	public static java.net.URI getCodeImageURI(ICode code) {
		return ImageUtils.createUriFromImage(getCodeImage(code));
	}

	@Override
	public StyledString getStyledText(URI uri) throws Exception {
		ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null).get();
		if (locatable == null) {
			return new StyledString(uri.toString(), Stylers.ATTENTION_STYLER);
		}

		Importance importance = IMPORTANCE_SERVICE.getImportance(uri);
		Styler styler;
		switch (importance) {
		case HIGH:
			styler = Stylers.IMPORTANCE_HIGH_STYLER;
			break;
		case LOW:
			styler = Stylers.IMPORTANCE_LOW_STYLER;
			break;
		default:
			styler = null;
			break;
		}

		if (ICode.class.isInstance(locatable)) {
			ICode code = (ICode) locatable;
			StyledString string = new StyledString(code.getCaption(), styler);
			IDimension dimension = this.codeService.getDimension(uri);
			if (dimension != null) {
				string.append(" (" + dimension.represent() + ")",
						Stylers.MINOR_STYLER);
			}
			List<ICode> properties = this.codeService.getProperties(code);
			if (properties.size() > 0) {
				StringBuilder sb = new StringBuilder(" (");
				switch (properties.size()) {
				case 1:
					sb.append(properties.get(0));
					break;
				case 2:
					sb.append(properties.get(0));
					sb.append(", ");
					sb.append(properties.get(1));
					break;
				default:
					sb.append(properties.size());
					sb.append(" properties");
				}
				sb.append(")");
				string.append(sb.toString(), Stylers.COUNTER_STYLER);
			}
			return string;
		}
		if (ICodeInstance.class.isInstance(locatable)) {
			ICodeInstance codeInstance = (ICodeInstance) locatable;
			ILabelProvider labelProvider = this.labelProviderService
					.getLabelProvider(codeInstance.getId());
			if (labelProvider != null) {
				StyledString string = new StyledString(
						labelProvider.getText(codeInstance.getId()), styler);
				List<Triple<URI, IDimension, String>> dimensionValues = this.codeService
						.getDimensionValues(codeInstance);
				if (!dimensionValues.isEmpty()) {
					StringBuilder sb = new StringBuilder(" (");
					List<String> dimensionValueStrings = new LinkedList<String>();
					for (Triple<URI, IDimension, String> dimensionValue : dimensionValues) {
						ICode code = LocatorService.INSTANCE.resolve(
								dimensionValue.getFirst(), ICode.class, null)
								.get();
						// only show dimension name if the owner is not the
						// used code
						if (!dimensionValue.getFirst().equals(
								codeInstance.getCode().getUri())) {
							String owner = code != null ? code.getCaption()
									: "ERROR";
							dimensionValueStrings.add(owner);
							dimensionValueStrings.add(" = ");
						}
						dimensionValueStrings.add(dimensionValue.getThird());
					}
					sb.append(StringUtils.join(dimensionValueStrings, ", "));
					sb.append(")");
					string.append(sb.toString());
				}
				return string;
			} else {
				return new StyledString("unknown origin",
						Stylers.ATTENTION_STYLER);
			}
		}
		if (IEpisodes.class.isInstance(locatable)) {
			return new StyledString(URIUtils.getIdentifier(uri).toString(),
					styler);
		}
		if (IEpisode.class.isInstance(locatable)) {
			IEpisode episode = (IEpisode) locatable;
			String name = (episode != null) ? episode.getCaption() : "";
			if (name.isEmpty()) {
				List<ICode> codes;
				codes = this.codeService.getCodes(episode.getUri());
				List<String> codeNames = new ArrayList<String>();
				for (ICode code : codes) {
					codeNames.add(code.getCaption());
				}
				name = "[" + StringUtils.join(codeNames, ", ") + "]";
			}
			return new StyledString(name, styler);
		}
		if (IAxialCodingModel.class.isInstance(locatable)) {
			IAxialCodingModel axialCodingModel = (IAxialCodingModel) locatable;
			String name = (axialCodingModel != null) ? axialCodingModel
					.getTitle() : null;
			if (name == null) {
				name = uri.toString();
			}
			return new StyledString(name, styler);
		}

		ILabelProvider labelProvider = this.labelProviderService
				.getLabelProvider(uri);
		return (labelProvider != null) ? new StyledString(
				labelProvider.getText(uri), styler) : new StyledString(
				"label provider missing", Stylers.ATTENTION_STYLER);
	}

	private boolean isCoded(URI uri) throws CodeServiceException {
		return this.codeService.getCodes(uri).size() > 0;
	}

	private boolean hasMemo(URI uri) {
		return this.codeService.isMemo(uri);
	}

	private boolean isDimensionalized(URI uri) {
		return this.codeService.getDimension(uri) != null;
	}

	private boolean canHaveDimensionValue(URI uri) throws CodeServiceException {
		for (ICode code : this.codeService.getCodes(uri)) {
			if (this.codeService.getDimension(code.getUri()) != null) {
				return true;
			}
		}
		return false;
	}

	private boolean hasProperties(URI uri) throws InterruptedException,
			ExecutionException {
		ICode code = LocatorService.INSTANCE.resolve(uri, ICode.class, null)
				.get();
		return code != null ? this.codeService.getProperties(code).size() > 0
				: false;
	}

	@Override
	public Image getImage(URI uri) throws Exception {
		Class<? extends ILocatable> type = LocatorService.INSTANCE.getType(uri);

		Image image = null;
		if (type == ICode.class) {
			image = ImageManager.CODE;
		}
		if (type == ICodeInstance.class) {
			ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null)
					.get();
			ICodeInstance codeInstance = (ICodeInstance) locatable;
			ILabelProvider labelProvider = this.labelProviderService
					.getLabelProvider(codeInstance.getId());
			image = (labelProvider != null) ? labelProvider
					.getImage(codeInstance.getId()) : null;
		}
		if (type == IEpisodes.class) {
			image = ImageManager.EPISODE;
		}
		if (type == IEpisode.class) {
			image = ImageManager.EPISODE;
		}

		List<ImageOverlay> overlays = new LinkedList<ImageOverlay>();
		try {
			if (this.isCoded(uri)) {
				overlays.add(ImageManager.OVERLAY_CODED);
			}
			if (this.hasMemo(uri)) {
				overlays.add(ImageManager.OVERLAY_MEMO);
			}
			if (this.isDimensionalized(uri)) {
				overlays.add(ImageManager.OVERLAY_DIMENSIONALIZED);
			}
			if (this.canHaveDimensionValue(uri)) {
				overlays.add(ImageManager.OVERLAY_HAS_DIMENSION_VALUE);
			}
			if (this.hasProperties(uri)) {
				overlays.add(ImageManager.OVERLAY_HAS_PROPERTIES);
			}
		} catch (CodeServiceException e) {
			LOGGER.error("Error creating appropriate image for " + uri, e);
			overlays.add(new ImageOverlayImpl(ImageDescriptor
					.createFromImage(PartRenamer.ERROR), Quadrant.BottomRight));
		}

		if (image == null) {
			image = PartRenamer.ERROR;
		}

		ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null).get();
		if (locatable == null) {
			image = PartRenamer.ERROR;
		}

		return ImageManager.getImage(image, overlays);
	}

	@Override
	public boolean hasInformation(URI uri) throws Exception {
		ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null).get();
		return locatable instanceof ICode || locatable instanceof ICodeInstance
				|| locatable instanceof IEpisode;
	}

	@Override
	public List<IllustratedText> getMetaInformation(URI uri) throws Exception {
		ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null).get();

		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		if (locatable instanceof ICode) {
			ICode code = (ICode) locatable;
			metaEntries.add(new IllustratedText(getCodeImage(code), code
					.getClass().getSimpleName()
					+ " \""
					+ code.getCaption()
					+ "\""));
		}
		if (locatable instanceof ICodeInstance) {
			metaEntries.add(new IllustratedText(ICodeInstance.class
					.getSimpleName().substring(1)));
		}
		if (locatable instanceof IEpisode) {
			metaEntries.add(new IllustratedText(ImageManager.EPISODE,
					IEpisode.class.getSimpleName().substring(1)));
		}
		return metaEntries;
	}

	@Override
	public List<IDetailEntry> getDetailInformation(URI uri) throws Exception {
		ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null).get();

		List<IDetailEntry> detailEntries = new ArrayList<IDetailEntry>();
		if (locatable instanceof ICode) {
			ICode code = (ICode) locatable;
			detailEntries.add(new DetailEntry("URI", code.getUri().toString()));
			detailEntries.add(new DetailEntry("Created", code.getCreation()
					.toISO8601()));
		}
		if (locatable instanceof IEpisode) {
			IEpisode episode = (IEpisode) locatable;
			detailEntries.add(new DetailEntry("Owner",
					episode.getIdentifier() != null ? episode.getIdentifier()
							.getIdentifier() : ""));
			detailEntries.add(new DetailEntry("Caption",
					episode.getCaption() != null ? episode.getCaption() : "-"));
			detailEntries.add(new DetailEntry("Creation", (episode
					.getCreation() != null) ? episode.getCreation().toISO8601()
					: "-"));

			detailEntries.add(new DetailEntry("Start",
					(episode.getDateRange() != null && episode.getDateRange()
							.getStartDate() != null) ? episode.getDateRange()
							.getStartDate().toISO8601() : "-"));

			detailEntries.add(new DetailEntry("End",
					(episode.getDateRange() != null && episode.getDateRange()
							.getEndDate() != null) ? episode.getDateRange()
							.getEndDate().toISO8601() : "-"));

			TimeZoneDateRange range = episode.getDateRange();
			detailEntries.add(new DetailEntry("Span", (range != null) ? range
					.formatDuration() : "?"));
		}
		return detailEntries;
	}

	@Override
	public Control fillInformation(URI uri, Composite composite)
			throws Exception {
		return null;
	}

	@Override
	public void fill(URI object, ToolBarManager toolBarManager)
			throws Exception {
	}
}