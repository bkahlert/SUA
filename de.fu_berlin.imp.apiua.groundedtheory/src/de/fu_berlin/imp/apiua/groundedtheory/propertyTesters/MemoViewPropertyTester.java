package de.fu_berlin.imp.apiua.groundedtheory.propertyTesters;

import org.eclipse.core.expressions.PropertyTester;

import de.fu_berlin.imp.apiua.groundedtheory.views.AbstractMemoView;

public class MemoViewPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof AbstractMemoView) {
			AbstractMemoView memoView = (AbstractMemoView) receiver;
			if ("canNavigateBack".equals(property)) {
				return memoView.canNavigateBack();
			} else if ("canNavigateForward".equals(property)) {
				return memoView.canNavigateForward();
			}
		}
		return false;
	}

}
