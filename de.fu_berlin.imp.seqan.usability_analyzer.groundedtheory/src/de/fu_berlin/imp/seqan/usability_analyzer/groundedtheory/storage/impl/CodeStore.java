package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.data.TreeNode;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.NoNullSet;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Code;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
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

	@XStreamAlias("episodes")
	private Set<IEpisode> episodes;

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
		if (codeStoreFile == null || !codeStoreFile.exists()) {
			throw new CodeStoreReadException(new FileNotFoundException(
					codeStoreFile.getAbsolutePath()));
		}

		try {
			CodeStore codeStore = (CodeStore) xstream.fromXML(codeStoreFile);
			codeStore.setCodeStoreFile(codeStoreFile);
			if (codeStore.createdIds == null) {
				codeStore.createdIds = new TreeSet<Long>();
			}
			if (codeStore.codeTrees == null) {
				codeStore.codeTrees = new LinkedList<TreeNode<ICode>>();
			}
			if (codeStore.codeInstances == null) {
				codeStore.codeInstances = new HashSet<ICodeInstance>();
			}
			if (codeStore.memos == null) {
				codeStore.memos = new HashMap<Object, String>();
			}
			if (codeStore.episodes == null) {
				codeStore.episodes = new NoNullSet<IEpisode>();
			}

			return codeStore;
		} catch (ArrayIndexOutOfBoundsException e) {
			return new CodeStore(codeStoreFile);
		} catch (Exception e) {
			logger.error(e);
			throw new CodeStoreReadException(e);
		}
	}

	private CodeStore(File codeStoreFile) {
		this.codeStoreFile = codeStoreFile;
		this.createdIds = new TreeSet<Long>();
		this.codeTrees = new LinkedList<TreeNode<ICode>>();
		this.codeInstances = new HashSet<ICodeInstance>();
		this.episodes = new NoNullSet<IEpisode>();
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

	@Override
	public ICode[] getCodes() {
		List<ICode> codes = new ArrayList<ICode>();
		for (TreeNode<ICode> codeTree : this.codeTrees) {
			for (Iterator<ICode> iterator = codeTree.bfs(); iterator.hasNext();) {
				ICode code = iterator.next();
				codes.add(code);
			}
		}
		return codes.toArray(new ICode[0]);
	}

	@Override
	public boolean codeExists(ICode code) {
		for (TreeNode<ICode> codeTree : this.codeTrees) {
			if (codeTree.find(code).size() > 0) {
				return true;
			}
		}
		return false;
	}

	private void setCodeStoreFile(File codeStoreFile) {
		this.codeStoreFile = codeStoreFile;
	}

	public File getCodeStoreFile() {
		return this.codeStoreFile;
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
		List<TreeNode<ICode>> treeNodes = this.find(code);
		assert treeNodes.size() < 2;
		return treeNodes.size() == 0 ? null : treeNodes.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<ICodeInstance> loadInstances() {
		return (Set<ICodeInstance>) this.codeInstances.clone();
	}

	@Override
	public ICode createCode(String caption, RGB color)
			throws CodeStoreFullException {
		Long id = Long.MAX_VALUE;
		ArrayList<Long> ids = new ArrayList<Long>(this.codeTrees.size());
		for (TreeNode<ICode> codeTree : this.codeTrees) {
			for (ICode code : codeTree) {
				if (code.getId() == Long.MAX_VALUE) {
					throw new CodeStoreFullException();
				}
				ids.add(code.getId());
			}
		}
		ids.addAll(this.createdIds);
		id = Code.calculateId(ids);
		this.createdIds.add(id);

		ICode code = new Code(id, caption, color, new TimeZoneDate());
		this.codeTrees.add(new TreeNode<ICode>(code));
		return code;
	}

	@Override
	public ICodeInstance[] createCodeInstances(ICode[] codes,
			ILocatable[] locatables) throws InvalidParameterException,
			CodeStoreReadException, DuplicateCodeInstanceException {
		List<ICodeInstance> duplicateCodeInstances = new LinkedList<ICodeInstance>();
		List<ICodeInstance> generatedCodeInstances = new LinkedList<ICodeInstance>();
		for (ICode code : codes) {
			if (this.assertiveFind(code) != null) {
				for (ILocatable locatable : locatables) {
					ICodeInstance codeInstance = new CodeInstance(code,
							locatable.getUri(), new TimeZoneDate(new Date(),
									TimeZone.getDefault()));
					if (this.codeInstances.contains(codeInstance)) {
						duplicateCodeInstances.add(codeInstance);
					} else {
						generatedCodeInstances.add(codeInstance);
					}
				}
			} else {
				throw new InvalidParameterException(
						"Could not find a matching "
								+ ICode.class.getSimpleName() + " for " + code);
			}

			if (duplicateCodeInstances.size() > 0) {
				throw new DuplicateCodeInstanceException(duplicateCodeInstances);
			}
		}

		return generatedCodeInstances.toArray(new ICodeInstance[0]);
	}

	@Override
	public void addAndSaveCode(ICode code) throws CodeStoreWriteException,
			CodeStoreReadException {
		this.codeTrees.add(new TreeNode<ICode>(code));
		this.save();
	}

	@Override
	public void addAndSaveCodeInstances(ICodeInstance[] codeInstances)
			throws CodeStoreWriteException {
		List<ICodeInstance> abandondedCodeInstances = new LinkedList<ICodeInstance>();
		for (ICodeInstance codeInstance : codeInstances) {
			if (!this.codeExists(codeInstance.getCode())) {
				abandondedCodeInstances.add(codeInstance);
			}
		}
		if (abandondedCodeInstances.size() > 0) {
			throw new CodeStoreWriteAbandonedCodeInstancesException(
					abandondedCodeInstances);
		}

		for (ICodeInstance codeInstance : codeInstances) {
			this.codeInstances.add(codeInstance);
		}

		this.save();
	}

	@Override
	public void removeAndSaveCode(ICode code) throws CodeStoreWriteException,
			CodeHasChildCodesException, CodeDoesNotExistException {
		this.removeAndSaveCode(code, false);
	}

	@Override
	public void removeAndSaveCode(ICode code, boolean deleteInstance)
			throws CodeStoreWriteException, CodeHasChildCodesException,
			CodeDoesNotExistException {

		List<ICodeInstance> abandoned = new LinkedList<ICodeInstance>();
		for (ICodeInstance instance : this.codeInstances) {
			if (instance.getCode().equals(code)) {
				abandoned.add(instance);
			}
		}
		if (deleteInstance) {
			for (ICodeInstance instance : abandoned) {
				this.codeInstances.remove(instance);
				this.setMemo(instance, null);
			}
		} else if (abandoned.size() > 0) {
			throw new CodeStoreWriteAbandonedCodeInstancesException(abandoned);
		}

		List<TreeNode<ICode>> codeNodes = this.find(code);
		assert codeNodes.size() < 2;
		if (codeNodes.size() == 0) {
			throw new CodeDoesNotExistException(code);
		}

		if (codeNodes.get(0).hasChildren()) {
			throw new CodeHasChildCodesException();
		}

		if (this.codeTrees.contains(codeNodes.get(0))) {
			this.codeTrees.remove(codeNodes.get(0));
		} else {
			codeNodes.get(0).removeFromParent();
		}

		this.setMemo(code, null);

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
		List<TreeNode<ICode>> foundNodes = this.find(code);
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
		TreeNode<ICode> childNode = this.assertiveFind(code);

		if (childNode == null) {
			throw new CodeDoesNotExistException(code);
		}

		TreeNode<ICode> parentNode = this.assertiveFind(parentCode);
		TreeNode<ICode> oldParentNode = childNode.getParent();

		if (childNode == parentNode) {
			throw new CodeStoreIntegrityProtectionException("Child node"
					+ childNode + " can't be his own parent node");
		}
		if (childNode.isAncestorOf(childNode)) {
			throw new CodeStoreIntegrityProtectionException("Node" + childNode
					+ " can't be made a child node of its current child node "
					+ parentCode);
		}

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

		this.save();
		return (oldParentNode != null) ? oldParentNode.getData() : null;
	}

	@Override
	public List<ICode> getChildren(ICode code) {
		List<ICode> childCodes = new ArrayList<ICode>();
		for (TreeNode<ICode> codeTree : this.codeTrees) {
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
	public List<ICode> getSubCodes(ICode code) {
		List<ICode> subCodes = new ArrayList<ICode>();
		for (TreeNode<ICode> codeTree : this.codeTrees) {
			List<TreeNode<ICode>> foundNodes = codeTree.find(code);
			assert foundNodes.size() < 2;
			if (foundNodes.size() == 1) {
				for (TreeNode<ICode> subCode : foundNodes.get(0).children()) {
					subCodes.add(subCode.getData());
				}
			}
		}
		return subCodes;
	}

	@Override
	public void save() throws CodeStoreWriteException {
		try {
			xstream.toXML(this, new OutputStreamWriter(new FileOutputStream(
					this.codeStoreFile), "UTF-8"));
		} catch (IOException e) {
			throw new CodeStoreWriteException(e);
		}
	}

	@Override
	public void deleteCodeInstance(ICodeInstance codeInstance)
			throws CodeInstanceDoesNotExistException, CodeStoreWriteException {
		if (!this.codeInstances.contains(codeInstance)) {
			throw new CodeInstanceDoesNotExistException();
		}
		this.codeInstances.remove(codeInstance);
		this.save();
	}

	@Override
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

	/**
	 * Returns the location of a memo {@link File} for a given basename.
	 * 
	 * @param basename
	 * @return
	 */
	protected File getMemoLocation(String basename) {
		File file = new File(this.codeStoreFile.getParentFile(),
				DigestUtils.md5Hex(basename) + ".memo.html");
		if (file.exists()) {
			return file;
		} else {
			return new File(this.codeStoreFile.getParentFile(), basename
					+ ".memo.html");
		}

	}

	/**
	 * Returns the basename for the given {@link ICode} for use in conjunction
	 * {@link #getMemoLocation(String)}.
	 * 
	 * @param code
	 * @return
	 */
	protected static String getMemoBasename(ICode code) {
		if (code == null) {
			throw new InvalidParameterException();
		}
		return "code_" + new Long(code.getId()).toString();
	}

	/**
	 * Returns the basename for the given {@link ICodeInstance} for use in
	 * conjunction {@link #getMemoLocation(String)}.
	 * 
	 * @param codeInstance
	 * @return
	 */
	protected static String getMemoBasename(ICodeInstance codeInstance) {
		if (codeInstance == null) {
			throw new InvalidParameterException();
		}
		try {
			return "codeInstance_"
					+ new Long(codeInstance.getCode().getId()).toString()
					+ "_"
					+ URLEncoder.encode(codeInstance.getId().toString(),
							"UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Returns the basename for the given {@link ILocatable} for use in
	 * conjunction {@link #getMemoLocation(String)}.
	 * 
	 * @param locatable
	 * @return
	 */
	protected static String getMemoBasename(ILocatable locatable) {
		if (locatable == null) {
			throw new InvalidParameterException();
		}
		try {
			return "codeInstance_"
					+ URLEncoder.encode(locatable.getUri().toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Loads the memo saved for the given basename.
	 * 
	 * @param basename
	 * @return null if no memo exists
	 * @throws IOException
	 */
	protected String loadMemo(String basename) throws IOException {
		File memoFile = this.getMemoLocation(basename);
		if (memoFile.exists()) {
			try {
				return FileUtils.readFileToString(memoFile, "UTF-8");
			} catch (FileNotFoundException e) {
				return this.loadMemo(DigestUtils.md5Hex(basename));
			}
		} else {
			return null;
		}
	}

	/**
	 * Saves the memo to a given basename.
	 * 
	 * @param basename
	 * @param memo
	 *            if null or empty the memo is removed
	 * @throws IOException
	 */
	protected void saveMemo(String basename, String memo) throws IOException {
		File memoFile = this.getMemoLocation(basename);
		if ((memo == null || memo.trim().equals("")) && memoFile.exists()) {
			memoFile.delete();
		} else {
			try {
				FileUtils.writeStringToFile(memoFile, memo, "UTF-8");
			} catch (FileNotFoundException e) {
				this.saveMemo(DigestUtils.md5Hex(basename), memo);
			}
		}
	}

	@Override
	public String getMemo(ICode code) {
		String memo = null;
		try {
			memo = this.loadMemo(getMemoBasename(code));
		} catch (IOException e) {
			logger.error("Error reading memo for " + code);
		}
		if (memo == null) {
			return this.memos != null ? this.memos.get(code) : null;
		} else {
			return memo;
		}
	}

	@Override
	public String getMemo(ICodeInstance codeInstance) {
		String memo = null;
		try {
			memo = this.loadMemo(getMemoBasename(codeInstance));
		} catch (IOException e) {
			logger.error("Error reading memo for " + codeInstance);
		}
		if (memo == null) {
			return this.memos != null ? this.memos.get(codeInstance) : null;
		} else {
			return memo;
		}
	}

	@Override
	public String getMemo(ILocatable locatable) {
		String memo = null;
		try {
			memo = this.loadMemo(getMemoBasename(locatable));
		} catch (IOException e) {
			logger.error("Error reading memo for " + locatable);
		}
		if (memo == null) {
			return this.memos != null ? this.memos.get(locatable.getUri())
					: null;
		} else {
			return memo;
		}
	};

	@Override
	public void setMemo(ICode code, String html) throws CodeStoreWriteException {
		try {
			this.saveMemo(getMemoBasename(code), html);
		} catch (IOException e) {
			throw new CodeStoreWriteException(e);
		}
		if (this.memos != null) {
			this.memos.remove(code);
		}
		this.save();
	}

	@Override
	public void setMemo(ICodeInstance codeInstance, String html)
			throws CodeStoreWriteException {
		try {
			this.saveMemo(getMemoBasename(codeInstance), html);
		} catch (IOException e) {
			throw new CodeStoreWriteException(e);
		}
		if (this.memos != null) {
			this.memos.remove(codeInstance);
		}
		this.save();
	}

	@Override
	public void setMemo(ILocatable locatable, String html)
			throws CodeStoreWriteException {
		try {
			this.saveMemo(getMemoBasename(locatable), html);
		} catch (IOException e) {
			throw new CodeStoreWriteException(e);
		}
		if (this.memos != null) {
			this.memos.remove(locatable.getUri());
		}
		this.save();
	}

	@Override
	public Set<IEpisode> getEpisodes() {
		return this.episodes;
	}
}
