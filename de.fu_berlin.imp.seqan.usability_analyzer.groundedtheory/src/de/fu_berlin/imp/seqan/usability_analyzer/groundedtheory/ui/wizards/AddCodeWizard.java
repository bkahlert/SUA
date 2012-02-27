package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.pages.AddCodeWizardPage;

public class AddCodeWizard extends Wizard {
	private static final Logger LOGGER = Logger.getLogger(AddCodeWizard.class);

	public static final String TITLE = "Add Code";
	public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_ADD_CODE;

	protected final AddCodeWizardPage addCodeWizardPage = new AddCodeWizardPage();

	protected List<ICode> affectedCodes;

	private List<ICodeable> codeables;

	public AddCodeWizard(List<ICodeable> codeables) {
		this.setWindowTitle(TITLE);
		this.setDefaultPageImageDescriptor(IMAGE);
		this.setNeedsProgressMonitor(false);
		this.codeables = codeables;
	}

	@Override
	public void addPages() {
		this.addPage(addCodeWizardPage);
	}

	@Override
	public boolean performFinish() {
		affectedCodes = new LinkedList<ICode>();
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);
		if (this.addCodeWizardPage.getCreateCode()) {
			String codeCaption = this.addCodeWizardPage.getNewCodeCaption();
			ICode createdCode = null;
			for (ICodeable codeable : codeables) {
				try {
					if (createdCode == null) {
						createdCode = codeService
								.addCode(codeCaption, codeable);
						affectedCodes.add(createdCode);
					} else {
						codeService.addCode(createdCode, codeable);
					}
					LOGGER.info("Code " + createdCode + " added to " + codeable);
				} catch (CodeServiceException e) {
					LOGGER.error("Code " + codeCaption
							+ " couldn't be added to " + codeable);
				}
			}
		} else {
			for (ICode code : this.addCodeWizardPage.getExistingCodes()) {
				for (ICodeable codeable : codeables) {
					try {
						codeService.addCode(code, codeable);
						LOGGER.info("Code " + code + " added to " + codeable);
					} catch (CodeServiceException e) {
						LOGGER.error("Code " + code + " couldn't be added to "
								+ codeable);
					}
				}
				affectedCodes.add(code);
			}
		}

		return true;
	}

	public List<ICode> getAffectedCodes() {
		return affectedCodes;
	}
}
