package de.fu_berlin.imp.seqan.usability_analyzer.diff.services;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;

public class CompilationServiceAdapter implements ICompilationServiceListener {

	@Override
	public void compilationStateChanged(ICompilable[] compilables, Boolean state) {
		return;
	}

	@Override
	public void compilerOutputChanged(ICompilable compilable, String html) {
		return;
	}

	@Override
	public void executionOutputChanged(ICompilable compilable, String html) {
		return;
	}

}
