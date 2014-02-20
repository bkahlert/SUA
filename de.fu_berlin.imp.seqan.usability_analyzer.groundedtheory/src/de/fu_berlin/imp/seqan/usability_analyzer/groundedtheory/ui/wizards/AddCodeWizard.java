package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;
import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.pages.AddCodeWizardPage;

public class AddCodeWizard extends Wizard {
	private static final Logger LOGGER = Logger.getLogger(AddCodeWizard.class);

	public static final String TITLE = "Add Code";
	public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_ADD_CODE;

	private ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	protected final AddCodeWizardPage addCodeWizardPage;

	protected List<ICode> affectedCodes;

	private List<URI> uris;

	public AddCodeWizard(List<URI> uris, RGB initialRGB) {
		this.setWindowTitle(TITLE);
		this.setDefaultPageImageDescriptor(IMAGE);
		this.setNeedsProgressMonitor(false);
		this.uris = uris;
		this.addCodeWizardPage = new AddCodeWizardPage(initialRGB);
	}

	@Override
	public void addPages() {
		this.addPage(this.addCodeWizardPage);
	}

	@Override
	public boolean performFinish() {
		this.affectedCodes = new LinkedList<ICode>();
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		if (this.addCodeWizardPage.getCreateCode()) {
			String codeCaption = this.addCodeWizardPage.getNewCodeCaption();
			RGB rgb = this.addCodeWizardPage.getNewCodeRGB();
			ICode createdCode = null;
			for (URI uri : this.uris) {
				try {
					if (createdCode == null) {
						createdCode = codeService
								.addCode(codeCaption, rgb, uri);
						this.affectedCodes.add(createdCode);
					} else {
						codeService.addCode(createdCode, uri);
					}
					LOGGER.info("Code " + createdCode + " added to " + uri);
				} catch (CodeServiceException e) {
					LOGGER.error("Code " + codeCaption
							+ " couldn't be added to " + uri, e);
				}
			}
		} else {
			URI[] uris = this.addCodeWizardPage.getExistingCodes().toArray(
					new URI[0]);

			try {
				ILocatable[] locatables = this.locatorService.resolve(uris,
						null).get();
				List<ICode> codes = ArrayUtils.getAdaptableObjects(locatables,
						ICode.class);
				if (codes.size() != locatables.length) {
					throw new RuntimeException("Implementation Error");
				}

				try {
					codeService.addCodes(codes, this.uris);
					LOGGER.info(ICode.class.getSimpleName() + "s \""
							+ StringUtils.join(codes, "\", \"")
							+ "\" added to \""
							+ StringUtils.join(this.uris, "\", \"") + "\"");
				} catch (CodeServiceException e) {
					LOGGER.error(
							ICode.class.getSimpleName() + "s \""
									+ StringUtils.join(codes, "\", \"")
									+ "\" could not be added to \""
									+ StringUtils.join(this.uris, "\", \"")
									+ "\"", e);
				}
				this.affectedCodes.addAll(codes);
			} catch (Exception e) {
				LOGGER.error("Error while adding codes " + uris + " to "
						+ this.uris, e);
			}
		}

		return true;
	}

	public List<ICode> getAffectedCodes() {
		return this.affectedCodes;
	}
}
