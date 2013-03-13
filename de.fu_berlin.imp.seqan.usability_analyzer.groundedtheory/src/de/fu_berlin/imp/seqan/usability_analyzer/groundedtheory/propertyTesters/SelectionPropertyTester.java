package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.propertyTesters;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

public class SelectionPropertyTester extends PropertyTester {

	// ugly workaround: since in normal JUnit test the Eclipse workbench is not
	// started we cannot use the SelectionUtils
	@SuppressWarnings("unchecked")
	private static <T> List<T> getAdaptableObjects(ISelection selection,
			Class<? extends T> clazz) {
		try {
			return SelectionUtils.getAdaptableObjects(selection, clazz);
		} catch (Exception e) {
			IStructuredSelection s = (IStructuredSelection) selection;
			List<T> items = new LinkedList<T>();
			for (Object item : s.toArray()) {
				if (clazz.isInstance(item)) {
					items.add((T) item);
				}
			}
			return items;
		}
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof ISelection) {
			if ("containsSingleKey".equals(property)) {
				Map<IIdentifier, List<HasIdentifier>> identifiers = new HashMap<IIdentifier, List<HasIdentifier>>();
				List<HasIdentifier> hasIdentifiers = getAdaptableObjects(
						(ISelection) receiver, HasIdentifier.class);
				for (HasIdentifier hasIdentifier : hasIdentifiers) {
					IIdentifier identifier = hasIdentifier.getIdentifier();
					if (identifier != null) {
						if (!identifiers.containsKey(identifier)) {
							identifiers.put(identifier,
									new LinkedList<HasIdentifier>());
						}
						identifiers.get(identifier).add(hasIdentifier);
					}
				}

				if (identifiers.size() != 1) {
					return false;
				} else {
					return true;
				}
			}
		}
		return false;
	}
}
