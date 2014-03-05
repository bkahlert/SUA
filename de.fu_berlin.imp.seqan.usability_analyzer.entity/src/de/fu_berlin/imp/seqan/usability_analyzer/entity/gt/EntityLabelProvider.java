package de.fu_berlin.imp.seqan.usability_analyzer.entity.gt;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IUriPresenterService.StyledUriInformationLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

// TODO implement UriLabelProvider
public class EntityLabelProvider extends StyledUriInformationLabelProvider {

	private static final Logger LOGGER = Logger
			.getLogger(EntityLabelProvider.class);

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	@Override
	public StyledString getStyledText(URI uri) throws Exception {
		return new StyledString(URIUtils.getIdentifier(uri).toString());
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

	@Override
	public boolean hasInformation(URI uri) {
		ILocatable locatable;
		try {
			locatable = this.locatorService.resolve(uri, null).get();
		} catch (Exception e) {
			LOGGER.error("Error checking information for " + uri);
			return false;
		}

		return locatable instanceof Entity;
	}

	@Override
	public List<IllustratedText> getMetaInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();

		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		if (locatable instanceof Entity) {
			metaEntries.add(new IllustratedText(ImageManager.ENTITY,
					Entity.class.getSimpleName()));
		}
		return metaEntries;
	}

	@Override
	public List<IDetailEntry> getDetailInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();

		List<IDetailEntry> detailEntries = new ArrayList<IDetailEntry>();
		if (locatable instanceof IDiff) {
			Entity entity = (Entity) locatable;
			detailEntries
					.add(new DetailEntry("URI",
							entity.getUri() != null ? entity.getUri()
									.toString() : "-"));
			detailEntries.add(new DetailEntry("Internal ID", entity
					.getInternalId() != null ? entity.getInternalId() : "-"));
			detailEntries.add(new DetailEntry("Identifier", entity
					.getIdentifier() != null ? entity.getIdentifier()
					.toString() : "-"));
			detailEntries.add(new DetailEntry("ID",
					entity.getId() != null ? entity.getId().toString() : "-"));
			detailEntries.add(new DetailEntry("Fingerprints", entity
					.getFingerprints() != null ? StringUtils.join(
					entity.getFingerprints(), ", ") : "-"));
			detailEntries.add(new DetailEntry("Token",
					entity.getToken() != null ? entity.getToken().toString()
							: "-"));
			detailEntries.add(new DetailEntry("Earliest Date", entity
					.getEarliestEntryDate() != null ? entity
					.getEarliestEntryDate().toISO8601() : "-"));
			detailEntries.add(new DetailEntry("Latest Date", entity
					.getLatestEntryDate() != null ? entity.getLatestEntryDate()
					.toISO8601() : "-"));
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