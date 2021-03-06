package de.fu_berlin.imp.apiua.groundedtheory.services;

import java.util.List;
import java.util.Set;

import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;

// TODO rewrite everything to URI
public interface ICodeServiceListener {
	public void codesAdded(List<ICode> codes);

	public void codesAssigned(List<ICode> codes, List<URI> uris);

	public void codeRenamed(ICode code, String oldCaption, String newCaption);

	public void codeRecolored(ICode code, RGB oldColor, RGB newColor);

	public void codesRemoved(List<ICode> removedCodes, List<URI> uris);

	public void codeMoved(ICode code, ICode oldParentCode, ICode newParentCode);

	public void codeDeleted(ICode code);

	public void relationsAdded(Set<IRelation> relations);

	public void relationsDeleted(Set<IRelation> relations);

	public void relationsRenamed(Set<IRelation> relations);

	public void relationInstancesAdded(Set<IRelationInstance> relations);

	public void relationInstancesDeleted(Set<IRelationInstance> relations);

	public void memoAdded(URI uri);

	public void memoModified(URI uri);

	public void memoRemoved(URI uri);

	public void episodeAdded(IEpisode episode);

	public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode);

	public void episodesDeleted(Set<IEpisode> deletedEpisodes);

	public void dimensionChanged(URI uri, IDimension oldDimension,
			IDimension newDimension);

	public void dimensionValueChanged(URI uri, String oldValue, String value);

	public void propertiesChanged(URI uri, List<URI> addedProperties,
			List<URI> removedProperties);

	public void axialCodingModelAdded(URI uri);

	public void axialCodingModelUpdated(URI uri);

	public void axialCodingModelRemoved(URI uri);
}
