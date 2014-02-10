package de.fu_berlin.imp.seqan.usability_analyzer.uri.viewers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.model.IUri;

public class UriLabelProvider
		extends
		de.fu_berlin.imp.seqan.usability_analyzer.core.services.IUriPresenterService.UriLabelProvider {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(UriLabelProvider.class);

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	@Override
	public String getText(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		if (locatable instanceof IUri) {
			IUri uri_ = (IUri) locatable;
			StringBuilder text = new StringBuilder();
			text.append(uri_.getUri().toString());
			if (uri_.getTitle() != null) {
				text.append(" - ");
				text.append(uri_.getTitle());
			}
			return text.toString();
		}
		return "";
	}

	public StyledString getStyledText(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		if (locatable instanceof IUri) {
			IUri uri_ = (IUri) locatable;
			StyledString text = new StyledString();
			text.append(uri_.getUri().toString());
			if (uri_.getTitle() != null) {
				text.append("  ");
				text.append(uri_.getTitle(), StyledString.DECORATIONS_STYLER);
			}
			return text;
		}
		return new StyledString();
	}

	@Override
	public Image getImage(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		if (locatable instanceof IUri) {
			try {
				if (this.codeService.getCodes(uri).size() > 0) {
					return this.codeService.isMemo(uri) ? ImageManager.URI_CODED_MEMO
							: ImageManager.URI_CODED;
				} else {
					return (this.codeService.isMemo(uri) ? ImageManager.URI_MEMO
							: ImageManager.URI);
				}
			} catch (CodeServiceException e) {
				return ImageManager.URI;
			}
		}
		return super.getImage(uri);
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
		return super.fillInformation(uri, composite);
	}

}