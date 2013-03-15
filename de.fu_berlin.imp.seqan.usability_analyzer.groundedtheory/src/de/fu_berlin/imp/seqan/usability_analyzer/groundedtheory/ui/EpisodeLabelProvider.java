package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.NoCodesNode;

public final class EpisodeLabelProvider extends LabelProvider {

	private static final Logger LOGGER = Logger
			.getLogger(EpisodeLabelProvider.class);

	ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);
	ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private HashMap<Image, Image> annotatedImages = new HashMap<Image, Image>();

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
	public String getText(Object element) {
		if (ICode.class.isInstance(element)) {
			ICode code = (ICode) element;
			return code.getCaption();
		}
		if (ICodeInstance.class.isInstance(element)) {
			ICodeInstance codeInstance = (ICodeInstance) element;
			ICodeable codedObject = this.codeService
					.getCodedObject(codeInstance.getId());
			if (codedObject != null) {
				ILabelProvider labelProvider = this.labelProviderService
						.getLabelProvider(codedObject);
				return (labelProvider != null) ? labelProvider
						.getText(codedObject) : "[UNKNOWN ORIGIN]";
			} else {
				return codeInstance.getId().toString();
			}
		}
		if (NoCodesNode.class.isInstance(element)) {
			return "no code";
		}
		if (element instanceof IEpisode) {
			IEpisode episode = (IEpisode) element;
			String name = (episode != null) ? episode.getCaption() : "";
			if (name.isEmpty()) {
				List<ICode> codes;
				try {
					codes = this.codeService.getCodes(episode);
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
		return "";
	}

	@Override
	public Image getImage(Object element) {
		if (ICode.class.isInstance(element)) {
			return this.codeService.isMemo((ICode) element) ? ImageManager.CODE_MEMO
					: ImageManager.CODE;
		}
		if (ICodeInstance.class.isInstance(element)) {
			ICodeInstance codeInstance = (ICodeInstance) element;
			ICodeable codedObject = this.codeService
					.getCodedObject(codeInstance.getId());

			Image image;
			if (codedObject != null) {
				ILabelProvider labelProvider = this.labelProviderService
						.getLabelProvider(codedObject);
				image = (labelProvider != null) ? labelProvider
						.getImage(codedObject) : null;
			} else {
				image = PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
			}
			return (this.codeService.isMemo(codeInstance)) ? this
					.getMemoAnnotatedImage(image) : image;
		}
		if (element instanceof IEpisode) {
			IEpisode episode = (IEpisode) element;
			Image overlay;
			try {
				overlay = (this.codeService.getCodes(episode).size() > 0) ? (this.codeService
						.isMemo(episode) ? ImageManager.EPISODE_CODED_MEMO
						: ImageManager.EPISODE_CODED) : (this.codeService
						.isMemo(episode) ? ImageManager.EPISODE_MEMO
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
		return super.getImage(element);
	}
}