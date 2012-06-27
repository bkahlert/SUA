package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.j4me.collections.TreeNode;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Code;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeStore;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeHasChildCodesException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeInstanceDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreFullException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteAbandonedCodeInstancesException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteException;

@XStreamAlias("codeStore")
class CodeStore implements ICodeStore {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CodeStore.class);

	@XStreamOmitField
	private File codeStoreFile;

	@XStreamAlias("createdIDs")
	private Set<Long> createdIds = null;

	@XStreamAlias("codeTrees")
	private LinkedList<TreeNode<ICode>> codeTrees = null;

	@XStreamAlias("instances")
	private HashSet<ICodeInstance> codeInstances = null;

	@XStreamAlias("memos")
	private HashMap<Object, String> memos = null;

	private static XStream xstream;

	static {
		xstream = new XStream();
		xstream.alias("codes", Code.class);
		xstream.alias("instance", CodeInstance.class);
		xstream.processAnnotations(CodeStore.class);
	}

	public static ICodeStore create(File codeStoreFile) {
		return new CodeStore(codeStoreFile);
	}

	public static ICodeStore load(File codeStoreFile)
			throws CodeStoreReadException {
		if (codeStoreFile == null || !codeStoreFile.exists())
			throw new CodeStoreReadException(new FileNotFoundException(
					codeStoreFile.getAbsolutePath()));

		try {
			CodeStore codeStore = (CodeStore) xstream.fromXML(codeStoreFile);
			codeStore.setCodeStoreFile(codeStoreFile);
			if (codeStore.createdIds == null)
				codeStore.createdIds = new TreeSet<Long>();
			if (codeStore.codeTrees == null)
				codeStore.codeTrees = new LinkedList<TreeNode<ICode>>();
			if (codeStore.codeInstances == null)
				codeStore.codeInstances = new HashSet<ICodeInstance>();
			if (codeStore.memos == null)
				codeStore.memos = new HashMap<Object, String>();
			return codeStore;
		} catch (ArrayIndexOutOfBoundsException e) {
			return new CodeStore(codeStoreFile);
		} catch (Exception e) {
			throw new CodeStoreReadException(e);
		}
	}

	private CodeStore(File codeStoreFile) {
		this.codeStoreFile = codeStoreFile;
		this.createdIds = new TreeSet<Long>();
		this.codeTrees = new LinkedList<TreeNode<ICode>>();
		this.codeInstances = new HashSet<ICodeInstance>();
	}

	@Override
	public ICode getCode(long id) {
		for (TreeNode<ICode> codeTree : this.codeTrees) {
			for (ICode code : codeTree) {
				if (code.getId() == id) {
					return code;
				}
			}
		}
		return null;
	}

	public boolean codeExists(ICode code) {
		for (TreeNode<ICode> codeTree : codeTrees) {
			if (codeTree.find(code).size() > 0)
				return true;
		}
		return false;
	}

	private void setCodeStoreFile(File codeStoreFile) {
		this.codeStoreFile = codeStoreFile;
	}

	@Override
	public List<ICode> getTopLevelCodes() {
		List<ICode> topLevelCodes = new ArrayList<ICode>();
		for (TreeNode<ICode> codeTree : this.codeTrees) {
			topLevelCodes.add(codeTree.getData());
		}
		return topLevelCodes;
	}

	/**
	 * Returns all {@link TreeNode}s that are describe the given {@link ICode}
	 * 
	 * @param code
	 * @return
	 */
	protected List<TreeNode<ICode>> find(ICode code) {
		List<TreeNode<ICode>> treeNodes = new ArrayList<TreeNode<ICode>>();
		for (TreeNode<ICode> codeTree : this.codeTrees) {
			treeNodes.addAll(codeTree.find(code));
		}
		return treeNodes;
	}

	/**
	 * Returns the {@link TreeNode} that describes the given {@link ICode}.
	 * <p>
	 * In contrast to {@link #find(ICode)} this method checks via
	 * <code>assert</code> if no more than one {@link TreeNode} is found.
	 * 
	 * @param code
	 * @return
	 */
	protected TreeNode<ICode> assertiveFind(ICode code) {
		List<TreeNode<ICode>> treeNodes = find(code);
		assert treeNodes.size() < 2;
		return treeNodes.size() == 0 ? null : treeNodes.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<ICodeInstance> loadInstances() {
		return (Set<ICodeInstance>) this.codeInstances.clone();
	}

	public ICode createCode(String caption) throws CodeStoreFullException {
		Long id = Long.MAX_VALUE;
		ArrayList<Long> ids = new ArrayList<Long>(codeTrees.size());
		for (TreeNode<ICode> codeTree : codeTrees) {
			for (ICode code : codeTree) {
				if (code.getId() == Long.MAX_VALUE)
					throw new CodeStoreFullException();
				ids.add(code.getId());
			}
		}
		ids.addAll(createdIds);
		id = Code.calculateId(ids);
		createdIds.add(id);

		ICode code = new Code(id, caption, new TimeZoneDate());
		codeTrees.add(new TreeNode<ICode>(code));
		return code;
	}

	@Override
	public ICodeInstance createCodeInstance(ICode code, ICodeable codeable)
			throws InvalidParameterException, CodeStoreReadException,
			DuplicateCodeInstanceException {
		for (TreeNode<ICode> codeTree : codeTrees) {
			if (codeTree.find(code).size() > 0) {
				ICodeInstance codeInstance = new CodeInstance(code,
						codeable.getCodeInstanceID(), new TimeZoneDate(
								new Date(), TimeZone.getDefault()));
				if (codeInstances.contains(codeInstance)) {
					throw new DuplicateCodeInstanceException();
				} else {
					return codeInstance;
				}
			}
		}
		throw new InvalidParameterException("Could not find a matching "
				+ ICode.class.getSimpleName() + " for " + code);
	}

	@Override
	public void addAndSaveCode(ICode code) throws CodeStoreWriteException,
			CodeStoreReadException {
		this.codeTrees.add(new TreeNode<ICode>(code));
		this.save();
	}

	@Override
	public void addAndSaveCodeInstance(ICodeInstance codeInstance)
			throws CodeStoreWriteException {
		if (!this.codeExists(codeInstance.getCode()))
			throw new CodeStoreWriteAbandonedCodeInstancesException(
					Arrays.asList(codeInstance));
		;
		this.codeInstances.add(codeInstance);
		this.save();
	}

	public void removeAndSaveCode(ICode code) throws CodeStoreWriteException,
			CodeHasChildCodesException, CodeDoesNotExistException {
		removeAndSaveCode(code, false);
	}

	@Override
	public void removeAndSaveCode(ICode code, boolean deleteInstance)
			throws CodeStoreWriteException, CodeHasChildCodesException,
			CodeDoesNotExistException {

		List<ICodeInstance> abandoned = new LinkedList<ICodeInstance>();
		for (ICodeInstance instance : this.codeInstances)
			if (instance.getCode().equals(code))
				abandoned.add(instance);
		if (deleteInstance) {
			for (ICodeInstance instance : abandoned) {
				this.codeInstances.remove(instance);
			}
		} else if (abandoned.size() > 0) {
			throw new CodeStoreWriteAbandonedCodeInstancesException(abandoned);
		}

		List<TreeNode<ICode>> codeNodes = this.find(code);
		assert codeNodes.size() < 2;
		if (codeNodes.size() == 0)
			throw new CodeDoesNotExistException(code);

		if (codeNodes.get(0).hasChildren())
			throw new CodeHasChildCodesException();

		if (this.codeTrees.contains(codeNodes.get(0)))
			this.codeTrees.remove(codeNodes.get(0));
		else
			codeNodes.get(0).removeFromParent();

		this.save();
	}

	@Override
	public void removeAndSaveCodeInstance(ICodeInstance codeInstance)
			throws CodeStoreWriteException, CodeStoreReadException {
		this.codeInstances.remove(codeInstance);
		this.save();
	}

	@Override
	public ICode getParent(ICode code) {
		List<TreeNode<ICode>> foundNodes = find(code);
		assert foundNodes.size() < 2;
		if (foundNodes.size() == 1) {
			TreeNode<ICode> parent = foundNodes.get(0).getParent();
			return parent != null ? parent.getData() : null;
		}
		return null;
	}

	@Override
	public ICode setParent(ICode code, ICode parentCode)
			throws CodeDoesNotExistException, CodeStoreWriteException {
		TreeNode<ICode> childNode = assertiveFind(code);

		if (childNode == null)
			throw new CodeDoesNotExistException(code);

		TreeNode<ICode> parentNode = assertiveFind(parentCode);
		TreeNode<ICode> oldParentNode = childNode.getParent();

		if (childNode == parentNode)
			throw new CodeStoreIntegrityProtectionException("Child node"
					+ childNode + " can't be his own parent node");
		if (childNode.isAncestorOf(childNode))
			throw new CodeStoreIntegrityProtectionException("Node" + childNode
					+ " can't be made a child node of its current child node "
					+ parentCode);

		// TODO: Komplexe Schleife

		// remove from old parent
		if (oldParentNode != null) {
			childNode.removeFromParent();
		} else if (this.codeTrees.contains(childNode)) {
			this.codeTrees.remove(childNode);
		} else {
			assert false;
		}

		// add to new parent
		if (parentNode != null) {
			parentNode.add(childNode);
		} else {
			this.codeTrees.add(childNode);
		}

		save();
		return (oldParentNode != null) ? oldParentNode.getData() : null;
	}

	@Override
	public List<ICode> getChildren(ICode code) {
		List<ICode> childCodes = new ArrayList<ICode>();
		for (TreeNode<ICode> codeTree : codeTrees) {
			List<TreeNode<ICode>> foundNodes = codeTree.find(code);
			assert foundNodes.size() < 2;
			if (foundNodes.size() == 1) {
				for (TreeNode<ICode> childNode : foundNodes.get(0).children()) {
					childCodes.add(childNode.getData());
				}
			}
		}
		return childCodes;
	}

	@Override
	// TODO test
	public List<ICode> getSubCodes(ICode code) {
		List<ICode> subCodes = new ArrayList<ICode>();
		for (TreeNode<ICode> codeTree : codeTrees) {
			List<TreeNode<ICode>> foundNodes = codeTree.find(code);
			assert foundNodes.size() < 2;
			if (foundNodes.size() == 1) {
				for (ICode subCode : foundNodes.get(0)) {
					subCodes.add(subCode);
				}
			}
		}
		return subCodes;
	}

	@Override
	public void save() throws CodeStoreWriteException {
		try {
			xstream.toXML(this, new FileWriter(codeStoreFile));
		} catch (IOException e) {
			throw new CodeStoreWriteException(e);
		}
	}

	@Override
	public void deleteCodeInstance(ICodeInstance codeInstance)
			throws CodeInstanceDoesNotExistException, CodeStoreWriteException {
		if (!this.codeInstances.contains(codeInstance))
			throw new CodeInstanceDoesNotExistException();
		this.codeInstances.remove(codeInstance);
		this.save();
	}

	public void deleteCodeInstances(ICode code) throws CodeStoreReadException,
			CodeStoreWriteException {
		for (Iterator<ICodeInstance> iter = this.codeInstances.iterator(); iter
				.hasNext();) {
			if (iter.next().getCode().equals(code)) {
				iter.remove();
			}
		}
		this.save();
	}

	@Override
	public String getMemo(ICode code) {
		return this.memos.get(code);
	}

	@Override
	public String getMemo(ICodeInstance codeInstance) {
		return this.memos.get(codeInstance);
	}

	public String getMemo(ICodeable codeable) {
		return this.memos.get(codeable.getCodeInstanceID());
	};

	@Override
	public void setMemo(ICode code, String html) throws CodeStoreWriteException {
		this.memos.put(code, html);
		this.save();
	}

	@Override
	public void setMemo(ICodeInstance codeInstance, String html)
			throws CodeStoreWriteException {
		this.memos.put(codeInstance, html);
		this.save();
	}

	public void setMemo(ICodeable codeable, String html)
			throws CodeStoreWriteException {
		this.memos.put(codeable.getCodeInstanceID(), html);
		this.save();
	};
}
