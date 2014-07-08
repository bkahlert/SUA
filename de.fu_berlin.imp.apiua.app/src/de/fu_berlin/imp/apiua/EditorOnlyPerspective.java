package de.fu_berlin.imp.apiua;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class EditorOnlyPerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		@SuppressWarnings("unused")
		String editorArea = layout.getEditorArea();
		this.addFastViews(layout);
		this.addViewShortcuts(layout);
		this.addPerspectiveShortcuts(layout);
		layout.addView(
				"de.fu_berlin.imp.apiua.groundedtheory.views.EditorOnlyMemoView",
				IPageLayout.RIGHT, 0.76f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(
				"de.fu_berlin.imp.apiua.diff.views.EditorOnlyCompilerOutputView",
				IPageLayout.BOTTOM,
				0.5f,
				"de.fu_berlin.imp.apiua.groundedtheory.views.EditorOnlyMemoView");
		layout.addView(
				"de.fu_berlin.imp.apiua.diff.views.EditorOnlyExecutionOutputView",
				IPageLayout.BOTTOM,
				0.5f,
				"de.fu_berlin.imp.apiua.diff.views.EditorOnlyCompilerOutputView");
	}

	/**
	 * Add fast views to the perspective.
	 */
	private void addFastViews(IPageLayout layout) {
	}

	/**
	 * Add view shortcuts to the perspective.
	 */
	private void addViewShortcuts(IPageLayout layout) {
	}

	/**
	 * Add perspective shortcuts to the perspective.
	 */
	private void addPerspectiveShortcuts(IPageLayout layout) {
	}

}
