package de.fu_berlin.imp.apiua.groundedtheory.services;

import java.util.List;
import java.util.Set;

import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;

public class CodeServiceAdapter implements ICodeServiceListener {

	@Override
	public void codesAdded(List<ICode> codes) {
	}

	@Override
	public void codesAssigned(List<ICode> codes, List<URI> uris) {
	}

	@Override
	public void codeRenamed(ICode code, String oldCaption, String newCaption) {
	}

	@Override
	public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
	}

	@Override
	public void codesRemoved(List<ICode> codes, List<URI> uris) {
	}

	@Override
	public void codeMoved(ICode code, ICode oldParentCode, ICode newParentCode) {
	}

	@Override
	public void codeDeleted(ICode code) {
	}

	@Override
	public void relationsAdded(List<URI> uris) {
	}

	@Override
	public void relationsDeleted(Set<URI> uris) {
	}

	@Override
	public void memoAdded(URI uri) {
	}

	@Override
	public void memoModified(URI uri) {
	}

	@Override
	public void memoRemoved(URI uri) {
	}

	@Override
	public void episodeAdded(IEpisode episode) {
	}

	@Override
	public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
	}

	@Override
	public void episodesDeleted(Set<IEpisode> episodes) {
	}

	@Override
	public void dimensionChanged(URI uri, IDimension oldDimension,
			IDimension newDimension) {
	}

	@Override
	public void dimensionValueChanged(URI uri, String oldValue, String value) {
	}

	@Override
	public void propertiesChanged(URI uri, List<URI> addedProperties,
			List<URI> removedProperties) {
	}

	@Override
	public void axialCodingModelAdded(URI uri) {
	}

	@Override
	public void axialCodingModelUpdated(URI uri) {
	}

	@Override
	public void axialCodingModelRemoved(URI uri) {
	}

}
