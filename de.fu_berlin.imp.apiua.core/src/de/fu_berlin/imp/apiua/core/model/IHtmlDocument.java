package de.fu_berlin.imp.apiua.core.model;

import java.io.File;

public interface IHtmlDocument extends IDocument {

	public String getCssQuery();

	public File getMarkedUpFile();

}
