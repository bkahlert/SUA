package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

public enum DoclogAction {
	READY, UNLOAD, SCROLL, LINK, SURVEY;

	public static DoclogAction getByString(String doclogActionString) {
		for (DoclogAction doclogAction : DoclogAction.values()) {
			if (doclogAction.toString().equalsIgnoreCase(doclogActionString))
				return doclogAction;
		}
		return null;
	}
}
