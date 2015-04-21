package de.fu_berlin.imp.apiua.groundedtheory.services;

import java.util.List;
import java.util.Set;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
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
	public void codesRecolored(List<ICode> codes) {
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
	public void relationsAdded(Set<IRelation> relations) {
	}

	@Override
	public void relationsUpdated(Set<IRelation> relations) {
	}

	@Override
	public void relationsDeleted(Set<IRelation> relations) {
	}

	@Override
	public void relationInstancesAdded(Set<IRelationInstance> relations) {
	}

	@Override
	public void relationInstancesDeleted(Set<IRelationInstance> relations) {
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
