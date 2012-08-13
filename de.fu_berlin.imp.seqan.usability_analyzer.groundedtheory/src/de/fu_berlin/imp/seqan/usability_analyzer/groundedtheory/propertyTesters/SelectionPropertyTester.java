package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.propertyTesters;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasFingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;

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
				if (clazz.isInstance(item))
					items.add((T) item);
			}
			return items;
		}
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof ISelection) {
			if ("containsSingleKey".equals(property)) {
				Map<ID, List<HasID>> ids = new HashMap<ID, List<HasID>>();
				List<HasID> hasIDs = getAdaptableObjects((ISelection) receiver,
						HasID.class);
				for (HasID hasID : hasIDs) {
					ID id = hasID.getID();
					if (id != null) {
						if (!ids.containsKey(id))
							ids.put(id, new LinkedList<HasID>());
						ids.get(id).add(hasID);
					}
				}

				Map<Fingerprint, List<HasFingerprint>> fingerprints = new HashMap<Fingerprint, List<HasFingerprint>>();
				List<HasFingerprint> hasFingerprints = getAdaptableObjects(
						(ISelection) receiver, HasFingerprint.class);
				for (HasFingerprint hasFingerprint : hasFingerprints) {
					Fingerprint fingerprint = hasFingerprint.getFingerprint();
					if (fingerprint != null) {
						if (!fingerprints.containsKey(fingerprint))
							fingerprints.put(fingerprint,
									new LinkedList<HasFingerprint>());
						fingerprints.get(fingerprint).add(hasFingerprint);
					}
				}

				if (ids.size() > 1)
					return false;
				if (ids.size() == 1) {
					OuterLoop: for (HasFingerprint hasFingerprint : hasFingerprints) {
						if (!(hasFingerprint instanceof HasID && ((HasID) hasFingerprint)
								.getID() != null)) {
							for (HasID hasID : hasIDs) {
								if (hasID instanceof HasFingerprint) {
									Fingerprint idsFingerprint = ((HasFingerprint) hasID)
											.getFingerprint();
									if (idsFingerprint.equals(hasFingerprint
											.getFingerprint()))
										continue OuterLoop;
								}
							}
						} else {
							ID fingerprintsID = ((HasID) hasFingerprint)
									.getID();
							for (HasID hasID : hasIDs) {
								if (hasID.getID().equals(fingerprintsID))
									continue OuterLoop;
							}
						}
						return false;
					}
					return true;
				}
				if (ids.size() == 0) {
					if (fingerprints.size() == 1)
						return true;
					return false;
				}
			}
		}
		return false;
	}
}
