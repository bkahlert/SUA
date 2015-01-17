package de.fu_berlin.imp.apiua.groundedtheory.ui.information;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.InformationControl;
import com.bkahlert.nebula.information.extender.EditorInformationControlExtender;
import com.bkahlert.nebula.widgets.editor.Editor;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.ui.Utils.AnkerLabelProvider;

public class MemoInformationControlExtender extends
		EditorInformationControlExtender<URI> {

	private static final Logger LOGGER = Logger
			.getLogger(MemoInformationControlExtender.class);

	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	public MemoInformationControlExtender() {
		super(GridDataFactory.fillDefaults().grab(true, true).minSize(450, 300));
	}

	@Override
	public Class<URI> getInformationClass() {
		return URI.class;
	}

	@Override
	public void extend(InformationControl<URI> informationControl,
			Composite parent) {
		super.extend(informationControl, parent);
		for (Editor<URI> editor : this.editors.values()) {
			editor.addAnkerLabelProvider(AnkerLabelProvider.INSTANCE);
		}
	}

	@Override
	public String getTitle(URI objectToLoad, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public String getHtml(URI objectToLoad, IProgressMonitor monitor) {
		return MemoInformationControlExtender.this.codeService
				.loadMemo(objectToLoad);
	}

	@Override
	public void setHtml(URI loadedObject, String html, IProgressMonitor monitor) {
		try {
			MemoInformationControlExtender.this.codeService.setMemo(
					loadedObject, html);
		} catch (CodeServiceException e) {
			LOGGER.error("Error saving memo for " + loadedObject, e);
		}
	}
}
