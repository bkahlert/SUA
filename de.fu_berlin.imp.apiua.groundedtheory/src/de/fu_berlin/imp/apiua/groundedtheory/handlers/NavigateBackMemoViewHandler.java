package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.fu_berlin.imp.apiua.groundedtheory.views.AbstractMemoView;

public class NavigateBackMemoViewHandler extends
		AbstractNavigationMemoViewHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractMemoView memoView = this.getMemoView();
		if (memoView != null) {
			memoView.navigateBack();
		}
		return null;
	}

}