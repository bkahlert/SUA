package de.fu_berlin.imp.apiua.uri.viewers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IUriPresenterService.StyledUriInformationLabelProvider;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.uri.ImageManager;
import de.fu_berlin.imp.apiua.uri.model.IUri;

public class UriLabelProvider extends StyledUriInformationLabelProvider {

	private static final Logger LOGGER = Logger
			.getLogger(UriLabelProvider.class);

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	@Override
	public StyledString getStyledText(URI uri) throws Exception {
		int maxUriLength = 20;

		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		if (locatable instanceof IUri) {
			IUri uri_ = (IUri) locatable;
			StyledString text = new StyledString();
			String uriString = uri_.getUri().toString().length() > maxUriLength ? uri_
					.getUri().toString().substring(0, maxUriLength)
					+ "..."
					: uri_.getUri().toString();
			if (uri_.getTitle() != null) {
				text.append(uri_.getTitle());
				text.append("  ");
				text.append(uriString, Stylers.MINOR_STYLER);
			} else {
				text.append(uriString);
			}
			return text;
		}
		return new StyledString();
	}

	@Override
	public Image getImage(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		if (locatable instanceof IUri) {
			if (this.codeService.getExplicitCodes(uri).size() > 0) {
				return this.codeService.isMemo(uri) ? ImageManager.URI_CODED_MEMO
						: ImageManager.URI_CODED;
			} else {
				return (this.codeService.isMemo(uri) ? ImageManager.URI_MEMO
						: ImageManager.URI);
			}
		}
		return super.getImage(uri);
	}

	@Override
	public boolean hasInformation(URI uri) throws Exception {
		ILocatable locatable;
		try {
			locatable = this.locatorService.resolve(uri, null).get();
		} catch (Exception e) {
			LOGGER.error("Error checking information for " + uri);
			return false;
		}

		return locatable instanceof IUri;
	}

	@Override
	public List<IllustratedText> getMetaInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();

		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		if (locatable instanceof IUri) {
			metaEntries.add(new IllustratedText(ImageManager.URI, IUri.class
					.getSimpleName()));
		}
		return metaEntries;
	}

	@Override
	public List<IDetailEntry> getDetailInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();

		List<IDetailEntry> detailEntries = new ArrayList<IDetailEntry>();
		if (locatable instanceof IUri) {
			IUri uri_ = (IUri) locatable;
			detailEntries.add(new DetailEntry("Title",
					uri_.getTitle() != null ? uri_.getTitle() : "-"));
			detailEntries.add(new DetailEntry("URI", uri_.getUri() + ""));
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