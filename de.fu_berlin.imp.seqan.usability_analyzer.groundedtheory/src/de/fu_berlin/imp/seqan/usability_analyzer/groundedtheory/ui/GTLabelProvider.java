package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui;

import java.net.URI;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.colors.ColorUtils;
import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.PaintUtils;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.Stylers;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IUriPresenterService.StyledUriInformationLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisodes;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.NoCodesNode;

public final class GTLabelProvider extends StyledUriInformationLabelProvider {

	public static class CodeColors {
		private final RGB backgroundRGB;
		private Color backgroundColor = null;
		private Color borderColor = null;

		public CodeColors(RGB backgroundRGB) {
			this.backgroundRGB = backgroundRGB;

			if (backgroundRGB == null) {
				this.backgroundColor = EpisodeRenderer.DEFAULT_BACKGROUND_COLOR;
			} else {
				this.backgroundColor = new Color(Display.getCurrent(),
						backgroundRGB.toClassicRGB());
			}

			this.borderColor = new Color(Display.getDefault(), ColorUtils
					.scaleLightnessBy(new RGB(this.backgroundColor.getRGB()),
							0.85f).toClassicRGB());
		}

		public RGB getBackgroundRGB() {
			return this.backgroundRGB;
		}

		public Color getBackgroundColor() {
			return this.backgroundColor;
		}

		public Color getBorderColor() {
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

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
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
	 * Returns the image that shows the color of the given {@link ICode}.
	 * 
	 * @param code
	 * @return
	 */
	public static Image getCodeImage(ICode code) {
		if (!codeImages.containsKey(code)) {
			Image image = new Image(Display.getCurrent(), 16, 16);
			GTLabelProvider.CodeColors info = new GTLabelProvider.CodeColors(
					code.getColor());
			GC gc = new GC(image);
			gc.setAlpha(128);
			PaintUtils.drawRoundedRectangle(gc, image.getBounds(),
					info.getBackgroundColor(), info.getBorderColor());
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
	public static URI getCodeImageURI(ICode code) {
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
		if (NoCodesNode.Uri.equals(uri)) {
			return new StyledString("no code", Stylers.MINOR_STYLER);
		}

		ILocatable locatable = this.locatorService.resolve(uri, null).get();

		if (ICode.class.isInstance(locatable)) {
			ICode code = (ICode) locatable;
			return new StyledString(code.getCaption());
		}
		if (ICodeInstance.class.isInstance(locatable)) {
			ICodeInstance codeInstance = (ICodeInstance) locatable;
			ILabelProvider labelProvider = this.labelProviderService
					.getLabelProvider(codeInstance.getId());
			return (labelProvider != null) ? new StyledString(
					labelProvider.getText(codeInstance.getId()))
					: new StyledString("unknown origin",
							Stylers.ATTENTION_STYLER);
		}
		if (IEpisodes.class.isInstance(locatable)) {
			return new StyledString(URIUtils.getIdentifier(uri).toString());
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
			return new StyledString(name);
		}

		ILabelProvider labelProvider = this.labelProviderService
				.getLabelProvider(uri);
		return (labelProvider != null) ? new StyledString(
				labelProvider.getText(uri)) : new StyledString(
				"label provider missing", Stylers.ATTENTION_STYLER);
	}

	@Override
	public Image getImage(URI uri) throws Exception {
		Class<? extends ILocatable> type = this.locatorService.getType(uri);
		if (type == ICode.class) {
			return this.codeService.isMemo(uri) ? ImageManager.CODE_MEMO
					: ImageManager.CODE;
		}
		if (type == ICodeInstance.class) {
			ILocatable locatable = this.locatorService.resolve(uri, null).get();
			ICodeInstance codeInstance = (ICodeInstance) locatable;
			ILabelProvider labelProvider = this.labelProviderService
					.getLabelProvider(codeInstance.getId());
			Image image = (labelProvider != null) ? labelProvider
					.getImage(codeInstance.getId()) : null;
			return (this.codeService.isMemo(uri)) ? this
					.getMemoAnnotatedImage(image) : image;
		}
		if (type == IEpisodes.class) {
			Image overlay;
			try {
				overlay = (this.codeService.getCodes(uri).size() > 0) ? (this.codeService
						.isMemo(uri) ? ImageManager.EPISODE_CODED_MEMO
						: ImageManager.EPISODE_CODED) : (this.codeService
						.isMemo(uri) ? ImageManager.EPISODE_MEMO
						: ImageManager.EPISODE);
			} catch (CodeServiceException e) {
				overlay = ImageManager.EPISODE;
			}
			return overlay;
		}
		if (type == IEpisode.class) {
			Image overlay;
			try {
				overlay = (this.codeService.getCodes(uri).size() > 0) ? (this.codeService
						.isMemo(uri) ? ImageManager.EPISODE_CODED_MEMO
						: ImageManager.EPISODE_CODED) : (this.codeService
						.isMemo(uri) ? ImageManager.EPISODE_MEMO
						: ImageManager.EPISODE);
			} catch (CodeServiceException e) {
				overlay = ImageManager.EPISODE;
			}
			return overlay;
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

		ILabelProvider labelProvider = this.labelProviderService
				.getLabelProvider(uri);
		return (labelProvider != null) ? labelProvider.getImage(uri) : null;
	}

	@Override
	public boolean hasInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		return locatable instanceof ICode || locatable instanceof ICodeInstance
				|| locatable instanceof IEpisode;
	}

	@Override
	public List<IllustratedText> getMetaInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();

		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		if (locatable instanceof ICode) {
			metaEntries.add(new IllustratedText(ImageManager.CODE, ICode.class
					.getSimpleName().substring(1)));
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
		ILocatable locatable = this.locatorService.resolve(uri, null).get();

		List<IDetailEntry> detailEntries = new ArrayList<IDetailEntry>();
		if (locatable instanceof ICode) {
			ICode code = (ICode) locatable;
			detailEntries.add(new DetailEntry("URI", code.getUri().toString()));
			detailEntries.add(new DetailEntry("Caption", code.getCaption()));
			detailEntries.add(new DetailEntry("Color", code.getColor()
					.toHexString()));
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