package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IUriPresenterService.UriLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.NoCodesNode;

public final class GTLabelProvider extends UriLabelProvider {

	private static final Logger LOGGER = Logger
			.getLogger(GTLabelProvider.class);

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private final ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private final HashMap<Image, Image> annotatedImages = new HashMap<Image, Image>();

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
	public String getText(URI uri) throws Exception {
		if (NoCodesNode.Uri.equals(uri)) {
			return "no code";
		}

		ILocatable locatable = this.locatorService.resolve(uri, null).get();

		if (ICode.class.isInstance(locatable)) {
			ICode code = (ICode) locatable;
			return code.getCaption();
		}
		if (ICodeInstance.class.isInstance(locatable)) {
			ICodeInstance codeInstance = (ICodeInstance) locatable;
			ILabelProvider labelProvider = this.labelProviderService
					.getLabelProvider(codeInstance.getId());
			return (labelProvider != null) ? labelProvider.getText(codeInstance
					.getId()) : "[UNKNOWN ORIGIN]";
		}
		if (locatable instanceof IEpisode) {
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
			return name;
		}

		ILabelProvider labelProvider = this.labelProviderService
				.getLabelProvider(uri);
		return (labelProvider != null) ? labelProvider.getText(uri) : "-";
	}

	@Override
	public Image getImage(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		if (ICode.class.isInstance(locatable)) {
			return this.codeService.isMemo(uri) ? ImageManager.CODE_MEMO
					: ImageManager.CODE;
		}
		if (ICodeInstance.class.isInstance(locatable)) {
			ICodeInstance codeInstance = (ICodeInstance) locatable;
			ILabelProvider labelProvider = this.labelProviderService
					.getLabelProvider(codeInstance.getId());
			Image image = (labelProvider != null) ? labelProvider
					.getImage(codeInstance.getId()) : null;
			return (this.codeService.isMemo(uri)) ? this
					.getMemoAnnotatedImage(image) : image;
		}
		if (locatable instanceof IEpisode) {
			IEpisode episode = (IEpisode) locatable;
			Image overlay;
			try {
				overlay = (this.codeService.getCodes(episode.getUri()).size() > 0) ? (this.codeService
						.isMemo(episode.getUri()) ? ImageManager.EPISODE_CODED_MEMO
						: ImageManager.EPISODE_CODED)
						: (this.codeService.isMemo(episode.getUri()) ? ImageManager.EPISODE_MEMO
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

			Long milliSecondsPassed = episode.getDateRange() != null ? episode
					.getDateRange().getDifference() : null;
			detailEntries.add(new DetailEntry("Span",
					(milliSecondsPassed != null) ? DurationFormatUtils
							.formatDuration(milliSecondsPassed,
									new SUACorePreferenceUtil()
											.getTimeDifferenceFormat(), true)
							: "unknown"));
		}
		return detailEntries;
	}

	@Override
	public Control fillInformation(URI uri, Composite composite)
			throws Exception {
		return super.fillInformation(uri, composite);
	}
}