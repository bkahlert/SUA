package de.fu_berlin.imp.seqan.usability_analyzer.diff.services.impl;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationService;

public class CompilationService implements ICompilationService {

	@Override
	public Boolean compiles(ICompilable compilable) {
		return true;
		// TODO label provider anpassen
		// TODO persistence implementieren
	}

}
