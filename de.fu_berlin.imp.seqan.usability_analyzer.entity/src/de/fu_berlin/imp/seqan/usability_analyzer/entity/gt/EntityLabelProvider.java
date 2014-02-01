package de.fu_berlin.imp.seqan.usability_analyzer.entity.gt;

import java.net.URI;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IUriPresenterService.UriLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

// TODO implement UriLabelProvider
public class EntityLabelProvider extends UriLabelProvider {

	private static final Logger LOGGER = Logger
			.getLogger(EntityLabelProvider.class);

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	@Override
	public String getText(URI uri) throws Exception {
		return URIUtils.getIdentifier(uri).toString();
	}

	@Override
	public Image getImage(URI uri) throws Exception {
		try {
			if (this.codeService.getCodes(uri).size() > 0) {
				if (this.codeService.isMemo(uri)) {
					return ImageManager.ENTITY_CODED_MEMO;
				} else {
					return ImageManager.ENTITY_CODED;
				}
			} else {
				if (this.codeService.isMemo(uri)) {
					return ImageManager.ENTITY_MEMO;
				} else {
					return ImageManager.ENTITY;
				}
			}
		} catch (CodeServiceException e) {
			LOGGER.error("Can't access " + ICodeService.class.getSimpleName());
		}
		return ImageManager.ENTITY;
	}

}