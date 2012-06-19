package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.sourceProviders;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class SourceProvider extends AbstractSourceProvider {

	/**
	 * Corresponds to a serviceProvider variable as defined in Extension
	 * org.eclipse.ui.services.
	 */
	public static final String CODESERVICE = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.CodeService";

	private ICodeService codeService;

	public SourceProvider() {
		// TODO
		// this.codeService = (ICodeService) PlatformUI.getWorkbench()
		// .getActiveWorkbenchWindow().getService(ICodeService.class);
		// this.fireSourceChanged(ISources.WORKBENCH, CODESERVICE,
		// this.codeService);
	}

	public void dispose() {
	}

	public String[] getProvidedSourceNames() {
		return new String[] { CODESERVICE };
	}

	public Map<Object, Object> getCurrentState() {
		Map<Object, Object> map = new HashMap<Object, Object>(1);
		map.put(CODESERVICE, this.codeService);
		return map;
	}

}
