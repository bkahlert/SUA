package de.fu_berlin.imp.apiua.core.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.Stylers;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;

public class LabelProviderService implements ILabelProviderService {

	private static final Logger LOGGER = Logger
			.getLogger(LabelProviderService.class);

	public static final Image ERROR = PlatformUI.getWorkbench()
			.getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);

	private List<ILabelProviderFactory> labelProviderFactories = new ArrayList<ILabelProviderFactory>();

	@Override
	public void addLabelProviderFactory(
			ILabelProviderFactory labelProviderFactory) {
		Assert.isNotNull(labelProviderFactory);
		this.labelProviderFactories.add(labelProviderFactory);
	}

	@Override
	public void removeLabelProviderFactory(
			ILabelProviderFactory labelProviderFactory) {
		Assert.isNotNull(labelProviderFactory);
		this.labelProviderFactories.remove(labelProviderFactory);
	}

	@Override
	public ILabelProvider getLabelProvider(URI uri) {
		for (ILabelProviderFactory labelProviderFactory : this.labelProviderFactories) {
			ILabelProvider labelProvider = labelProviderFactory.createFor(uri);
			if (labelProvider != null) {
				return labelProvider;
			}
		}
		return null;
	}

	@Override
	public StyledString getStyledText(URI uri) {
		ILabelProvider lp = this.getLabelProvider(uri);
		StyledString styledText = null;
		if (lp != null) {
			try {
				if (lp instanceof StyledLabelProvider) {
					styledText = ((StyledLabelProvider) lp).getStyledText(uri);
				} else {
					String text = lp.getText(uri);
					styledText = text != null ? new StyledString(text) : null;
				}
			} catch (Exception e) {
				LOGGER.error("Error retrieving styled text for " + uri, e);
			}
		}
		return styledText != null ? styledText : new StyledString(
				uri.toString(), Stylers.ATTENTION_STYLER);
	}

	@Override
	public String getText(URI uri) {
		ILabelProvider lp = this.getLabelProvider(uri);
		String text = null;
		if (lp != null) {
			try {
				text = lp.getText(uri);
			} catch (Exception e) {
				LOGGER.error("Error retrieving text for " + uri, e);
			}
		}
		return text != null ? text : uri.toString();
	}

	@Override
	public Image getImage(URI uri) {
		ILabelProvider lp = this.getLabelProvider(uri);
		Image image = null;
		if (lp != null) {
			try {
				image = lp.getImage(uri);
			} catch (Exception e) {
				LOGGER.error("Error retrieving image for " + uri, e);
			}
		}
		return image != null ? image : ERROR;
	}

}
