package de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.Relation;

public class CodeStoreWriteAbandonedRelationsException extends
		CodeStoreWriteException {

	private static final long serialVersionUID = 4695434392874929969L;

	private Set<IRelation> relations;

	public CodeStoreWriteAbandonedRelationsException(Set<IRelation> relations) {
		super("Saving the given " + ICode.class.getSimpleName()
				+ " would lead to " + Relation.class.getSimpleName()
				+ " with an invalid " + ICode.class + " reference.\nAffected "
				+ Relation.class.getSimpleName() + "s:\n- "
				+ StringUtils.join(relations, "\n- "));
		this.relations = relations;
	}

	public Set<IRelation> getAffectedRelations() {
		return this.relations;
	}
}
