package de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

import org.eclipse.jface.viewers.ColumnViewer;

import com.bkahlert.nebula.utils.TypedEditorSupport;

public abstract class URIEditingSupport extends TypedEditorSupport<URI> {

	public URIEditingSupport(ColumnViewer viewer) {
		super(viewer, URI.class);
	}

}
