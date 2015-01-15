package de.fu_berlin.imp.apiua.survey;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.CalendarUtils;
import com.bkahlert.nebula.utils.StringUtils;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.services.IUriPresenterService.StyledUriInformationLabelProvider;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.survey.model.DateId;
import de.fu_berlin.imp.apiua.survey.model.cd.CDDocument;
import de.fu_berlin.imp.apiua.survey.model.cd.CDDocumentField;
import de.fu_berlin.imp.apiua.survey.model.groupdiscussion.GroupDiscussionDocument;
import de.fu_berlin.imp.apiua.survey.model.groupdiscussion.GroupDiscussionDocumentField;

public class SurveyLabelProvider extends StyledUriInformationLabelProvider {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(SurveyLabelProvider.class);

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	@Override
	public StyledString getStyledText(URI uri) throws Exception {
		Class<? extends ILocatable> type = this.locatorService.getType(uri);
		if (type == CDDocument.class) {
			List<String> trail = URIUtils.getTrail(uri);
			IIdentifier identifier = new DateId(trail.get(0));
			return new StyledString(CDDocument.getIdentifierHash(identifier));
		}
		if (type == CDDocumentField.class) {
			List<String> trail = URIUtils.getTrail(uri);
			IIdentifier identifier = new DateId(trail.get(0));
			return new StyledString(CDDocument.getIdentifierHash(identifier)
					+ " - " + trail.get(1));
		}
		if (type == GroupDiscussionDocument.class) {
			String name = URLDecoder.decode(uri.getSegments().get(1), "UTF-8");
			name = FilenameUtils.getBaseName(name);
			return new StyledString(name);
		}
		if (type == GroupDiscussionDocumentField.class) {
			GroupDiscussionDocumentField discussionDocumentField = this.locatorService
					.resolve(uri, GroupDiscussionDocumentField.class, null)
					.get();
			String excerpt = StringUtils.shorten(
					discussionDocumentField.getValue(), 50);
			return new StyledString(excerpt);
		}
		return new StyledString(uri.toString(), Stylers.ATTENTION_STYLER);
	}

	// private boolean hasCodedChildren(IDiff diff) {
	// for (IDiffRecord diffRecord : diff.getDiffFileRecords()) {
	// try {
	// if (this.codeService.getCodes(diffRecord).size() > 0) {
	// return true;
	// }
	// } catch (CodeServiceException e) {
	// }
	// }
	// return false;
	// }

	@Override
	public Image getImage(URI uri) throws Exception {
		Class<? extends ILocatable> type = this.locatorService.getType(uri);
		if (type == CDDocument.class) {
			if (this.codeService.getCodes(uri).size() > 0) {
				return this.codeService.isMemo(uri) ? ImageManager.CDDOCUMENT_CODED_MEMO
						: ImageManager.CDDOCUMENT_CODED;
			} else {
				return (this.codeService.isMemo(uri) ? ImageManager.CDDOCUMENT_MEMO
						: ImageManager.CDDOCUMENT);
			}
		}
		if (type == CDDocumentField.class) {
			if (this.codeService.getCodes(uri).size() > 0) {
				return this.codeService.isMemo(uri) ? ImageManager.CDDOCUMENTFIELD_CODED_MEMO
						: ImageManager.CDDOCUMENTFIELD_CODED;
			} else {
				return (this.codeService.isMemo(uri) ? ImageManager.CDDOCUMENTFIELD_MEMO
						: ImageManager.CDDOCUMENTFIELD);
			}
		}
		if (type == GroupDiscussionDocument.class) {
			return ImageManager.GROUP_DISCUSSION;
		}
		if (type == GroupDiscussionDocumentField.class) {
			return ImageManager.GROUP_DISCUSSION;
		}
		return PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
	}

	@Override
	public boolean hasInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		return locatable instanceof CDDocument
				|| locatable instanceof CDDocumentField
				|| locatable instanceof GroupDiscussionDocument
				|| locatable instanceof GroupDiscussionDocumentField;
	}

	@Override
	public List<IllustratedText> getMetaInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		if (locatable instanceof CDDocument) {
			metaEntries.add(new IllustratedText(this.getImage(uri),
					CDDocument.class.getSimpleName()));
		}
		if (locatable instanceof CDDocumentField) {
			metaEntries.add(new IllustratedText(this.getImage(uri),
					CDDocumentField.class.getSimpleName()));
		}
		if (locatable instanceof GroupDiscussionDocument) {
			metaEntries.add(new IllustratedText(this.getImage(uri),
					GroupDiscussionDocument.class.getSimpleName()));
		}
		if (locatable instanceof GroupDiscussionDocumentField) {
			metaEntries.add(new IllustratedText(this.getImage(uri),
					GroupDiscussionDocumentField.class.getSimpleName()));
		}
		return metaEntries;
	}

	@Override
	public List<IDetailEntry> getDetailInformation(URI uri) throws Exception {
		ILocatable locatable = this.locatorService.resolve(uri, null).get();
		List<IDetailEntry> detailEntries = new ArrayList<IDetailEntry>();
		if (locatable instanceof CDDocument) {
			CDDocument cdDocument = (CDDocument) locatable;
			detailEntries.add(new DetailEntry("Identifier", cdDocument
					.getIdentifier() != null ? cdDocument.getIdentifier()
					.toString() : "-"));
			detailEntries.add(new DetailEntry("Size", Integer
					.toString(cdDocument.getSize())));
			detailEntries.add(new DetailEntry("Source Exists", CalendarUtils
					.toISO8601(cdDocument.getCompleted())));

			for (CDDocumentField field : cdDocument) {
				detailEntries.add(new DetailEntry(field.getKey(), field
						.getAnswer()));
			}
		}
		if (locatable instanceof CDDocumentField) {
			CDDocumentField field = (CDDocumentField) locatable;
			detailEntries.add(new DetailEntry("Identifier", field
					.getCdDocument().getIdentifier() != null ? field
					.getCdDocument().getIdentifier().toString() : "-"));
			detailEntries.add(new DetailEntry("Key", field.getKey()));
			detailEntries.add(new DetailEntry("Question", field.getQuestion()));
			detailEntries.add(new DetailEntry("Answer", field.getAnswer()));
		}
		if (locatable instanceof GroupDiscussionDocument) {
			GroupDiscussionDocument document = (GroupDiscussionDocument) locatable;
			detailEntries.add(new DetailEntry("CSS Query", document
					.getCssQuery()));
		}
		if (locatable instanceof GroupDiscussionDocumentField) {
			GroupDiscussionDocumentField field = (GroupDiscussionDocumentField) locatable;
			detailEntries.add(new DetailEntry("Content", field.getValue()));
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
