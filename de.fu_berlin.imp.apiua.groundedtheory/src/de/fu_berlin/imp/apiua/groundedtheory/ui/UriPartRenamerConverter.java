package de.fu_berlin.imp.apiua.groundedtheory.ui;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.PartRenamer;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;

/**
 * Used in conjunction with the {@link PartRenamer} this class enables views to
 * correctly set they part name based on the displayed URIs.
 *
 * @author bkahlert
 *
 */
public class UriPartRenamerConverter implements
		IConverter<URI, Pair<String, Image>> {

	private static final Logger LOGGER = Logger
			.getLogger(UriPartRenamerConverter.class);

	private static final ILabelProviderService LABEL_PROVIDER_SERVICE = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	@Override
	public Pair<String, Image> convert(URI uri) {
		ILocatable locatable = null;
		if (uri != null) {
			try {
				locatable = LocatorService.INSTANCE.resolve(uri, null).get();
			} catch (Exception e) {
				LOGGER.error("Error retrieving part info for " + uri);
			}
		}

		String title = null;
		Image image = null;
		if (locatable instanceof ICode) {
			ICode code = (ICode) locatable;
			title = code.getCaption();
			try {
				image = LABEL_PROVIDER_SERVICE.getLabelProvider(uri).getImage(
						uri);
			} catch (Exception e) {
				image = PartRenamer.ERROR;
			}
		} else if (locatable instanceof ICodeInstance) {
			ICodeInstance codeInstance = (ICodeInstance) locatable;
			ILabelProvider lp = LABEL_PROVIDER_SERVICE
					.getLabelProvider(codeInstance.getUri());
			if (lp != null) {
				title = "→ " + codeInstance.getCode().getCaption();
				try {
					image = lp.getImage(uri);
				} catch (Exception e) {
					image = PartRenamer.ERROR;
				}
			} else {
				// TODO check
				return null;
			}
		} else if (locatable instanceof IRelation) {
			@SuppressWarnings("unused")
			IRelation relation = (IRelation) locatable;
			try {
				title = LABEL_PROVIDER_SERVICE.getLabelProvider(uri).getText(
						uri);
				image = LABEL_PROVIDER_SERVICE.getLabelProvider(uri).getImage(
						uri);
			} catch (Exception e) {
				title = "ERROR";
				image = PartRenamer.ERROR;
			}
		} else if (locatable instanceof IRelationInstance) {
			IRelationInstance relationInstance = (IRelationInstance) locatable;
			ILabelProvider lp = LABEL_PROVIDER_SERVICE
					.getLabelProvider(relationInstance.getUri());
			if (lp != null) {
				title = "→ "
						+ LABEL_PROVIDER_SERVICE.getText(relationInstance
								.getRelation().getUri());
				try {
					image = lp.getImage(uri);
				} catch (Exception e) {
					image = PartRenamer.ERROR;
				}
			} else {
				// TODO check
				return null;
			}
		} else {
			if (locatable != null) {
				ILabelProvider lp = LABEL_PROVIDER_SERVICE
						.getLabelProvider(uri);
				if (lp == null) {
					title = "UNKNOWN";
					image = null;
				} else {
					try {
						title = lp.getText(uri);
					} catch (Exception e) {
						title = "ERROR";
					}
					try {
						image = lp.getImage(uri);
					} catch (Exception e) {
						image = PartRenamer.ERROR;
					}
				}
			} else {
				title = "INVALID";
				image = PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
			}
		}

		return new Pair<String, Image>(title, image);
	}
}