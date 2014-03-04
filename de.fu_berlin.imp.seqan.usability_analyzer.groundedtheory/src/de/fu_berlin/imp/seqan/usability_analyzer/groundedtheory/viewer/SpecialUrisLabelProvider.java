package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IUriPresenterService.StyledUriInformationLabelProvider;

public final class SpecialUrisLabelProvider extends
		StyledUriInformationLabelProvider {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(SpecialUrisLabelProvider.class);

	@Override
	public StyledString getStyledText(URI uri) throws Exception {
		if (uri.toString().startsWith(SpecialUris.PARENT_CODE.toString())) {
			return new StyledString("parent code", Stylers.COUNTER_STYLER);
		}
		if (uri.toString().startsWith(SpecialUris.NO_PARENT_CODE.toString())) {
			return new StyledString("no parent code", Stylers.COUNTER_STYLER);
		}
		if (uri.toString().startsWith(SpecialUris.CODES.toString())) {
			return new StyledString("child codes", Stylers.COUNTER_STYLER);
		}
		if (uri.toString().startsWith(SpecialUris.NO_CODES.toString())) {
			return new StyledString("no child codes", Stylers.COUNTER_STYLER);
		}
		if (uri.toString().startsWith(SpecialUris.PHENOMENONS.toString())) {
			return new StyledString("phenomenons", Stylers.COUNTER_STYLER);
		}
		if (uri.toString().startsWith(SpecialUris.NO_PHENOMENONS.toString())) {
			return new StyledString("no phenomenons", Stylers.COUNTER_STYLER);
		}
		return new StyledString("ERROR", Stylers.ATTENTION_STYLER);
	}

	@Override
	public Image getImage(URI uri) throws Exception {
		return null;
	}

	@Override
	public boolean hasInformation(URI uri) throws Exception {
		return false;
	}

	@Override
	public List<IllustratedText> getMetaInformation(URI object)
			throws Exception {
		return null;
	}

	@Override
	public List<de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService.IInformationLabelProvider.IDetailEntry> getDetailInformation(
			URI object) throws Exception {
		return null;
	}

	@Override
	public Control fillInformation(URI object, Composite composite)
			throws Exception {
		return null;
	}

	@Override
	public void fill(URI object, ToolBarManager toolBarManager)
			throws Exception {
		return;
	}

}