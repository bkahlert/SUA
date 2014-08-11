package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.PaintUtils;
import com.bkahlert.nebula.utils.Stylers;
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
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisodes;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.ICodeInstance;

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
	private final HashMap<Image, Image> annotatedImages = new HashMap<Image, Image>();

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
			Image image = new Image(Display.getCurrent(), 16, 16);
			GC gc = new GC(image);
			drawCodeImage(code, gc, image.getBounds());
			gc.dispose();
			codeImages.put(code, image);
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

	protected Image getMemoAnnotatedImage(Image image) {
		if (image == null) {
			return null;
		}

		if (!this.annotatedImages.containsKey(image)) {
			Image annotatedImage = new DecorationOverlayIcon(image,
					ImageManager.OVERLAY_MEMO, IDecoration.TOP_RIGHT)
					.createImage();
			this.annotatedImages.put(image, annotatedImage);
		}

		return this.annotatedImages.get(image);
	}

	@Override
	public void dispose() {
		for (Image annotatedImage : this.annotatedImages.values()) {
			if (annotatedImage != null && !annotatedImage.isDisposed()) {
				annotatedImage.dispose();
			}
		}
		super.dispose();
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
			return new StyledString(code.getCaption(), styler);
		}
		if (ICodeInstance.class.isInstance(locatable)) {
			ICodeInstance codeInstance = (ICodeInstance) locatable;
			ILabelProvider labelProvider = this.labelProviderService
					.getLabelProvider(codeInstance.getId());
			return (labelProvider != null) ? new StyledString(
					labelProvider.getText(codeInstance.getId()), styler)
					: new StyledString("unknown origin",
							Stylers.ATTENTION_STYLER);
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
				try {
					codes = this.codeService.getCodes(episode.getUri());
					List<String> codeNames = new ArrayList<String>();
					for (ICode code : codes) {
						codeNames.add(code.getCaption());
					}
					name = "[" + StringUtils.join(codeNames, ", ") + "]";
				} catch (CodeServiceException e) {
					LOGGER.warn("Could not find the episode's codes", e);
				}
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

	@Override
	public Image getImage(URI uri) throws Exception {
		Class<? extends ILocatable> type = LocatorService.INSTANCE.getType(uri);
		if (type == ICode.class) {
			Image image;
			try {
				image = this.codeService.getDimension(uri) != null ? ((this.codeService
						.getCodes(uri).size() > 0) ? (this.codeService
						.isMemo(uri) ? ImageManager.CODE_DIMENSIONALIZED_CODED_MEMO
						: ImageManager.CODE_DIMENSIONALIZED_CODED)
						: (this.codeService.isMemo(uri) ? ImageManager.CODE_DIMENSIONALIZED_MEMO
								: ImageManager.CODE_DIMENSIONALIZED))
						: ((this.codeService.getCodes(uri).size() > 0) ? (this.codeService
								.isMemo(uri) ? ImageManager.CODE_CODED_MEMO
								: ImageManager.CODE_CODED) : (this.codeService
								.isMemo(uri) ? ImageManager.CODE_MEMO
								: ImageManager.CODE));
			} catch (CodeServiceException e) {
				image = ImageManager.CODE;
			}
			return image;
		}
		if (type == ICodeInstance.class) {
			ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null)
					.get();
			ICodeInstance codeInstance = (ICodeInstance) locatable;
			ILabelProvider labelProvider = this.labelProviderService
					.getLabelProvider(codeInstance.getId());
			Image image = (labelProvider != null) ? labelProvider
					.getImage(codeInstance.getId()) : null;
			return (this.codeService.isMemo(uri)) ? this
					.getMemoAnnotatedImage(image) : image;
		}
		if (type == IEpisodes.class) {
			Image image;
			try {
				image = (this.codeService.getCodes(uri).size() > 0) ? (this.codeService
						.isMemo(uri) ? ImageManager.EPISODE_CODED_MEMO
						: ImageManager.EPISODE_CODED) : (this.codeService
						.isMemo(uri) ? ImageManager.EPISODE_MEMO
						: ImageManager.EPISODE);
			} catch (CodeServiceException e) {
				image = ImageManager.EPISODE;
			}
			return image;
		}
		if (type == IEpisode.class) {
			Image image;
			try {
				image = (this.codeService.getCodes(uri).size() > 0) ? (this.codeService
						.isMemo(uri) ? ImageManager.EPISODE_CODED_MEMO
						: ImageManager.EPISODE_CODED) : (this.codeService
						.isMemo(uri) ? ImageManager.EPISODE_MEMO
						: ImageManager.EPISODE);
			} catch (CodeServiceException e) {
				image = ImageManager.EPISODE;
			}
			return image;
			// Image image = new Image(Display.getCurrent(),
			// new Rectangle(0, 0, 16, 16));
			// GC gc = new GC(image);
			// PaintUtils
			// .drawRoundedRectangle(
			// gc,
			// overlay.getBounds(),
			// new Color(Display.getDefault(), episode
			// .getColor()));
			// gc.copyArea(overlay, 0, 0);
			// return image;
		}

		ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null).get();
		if (locatable == null) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
		}

		ILabelProvider labelProvider = this.labelProviderService
				.getLabelProvider(uri);
		return (labelProvider != null) ? labelProvider.getImage(uri) : null;
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