package de.fu_berlin.imp.apiua.diff.services;

import de.fu_berlin.imp.apiua.diff.model.ICompilable;

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
