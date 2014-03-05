package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import org.eclipse.jface.viewers.IContentProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;

/**
 * This empty class is only used to denote the non existence of any
 * {@link ICode}s. This is useful for {@link IContentProvider}s that want to
 * explicitly mark the miss of {@link ICode}s.
 * 
 * @author bkahlert
 * 
 */
public final class NoCodesNode {
	public static URI Uri = new URI("null");
}
