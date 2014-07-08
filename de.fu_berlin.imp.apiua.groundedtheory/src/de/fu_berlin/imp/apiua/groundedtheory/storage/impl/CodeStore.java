package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;

import com.bkahlert.nebula.data.TreeNode;
import com.bkahlert.nebula.utils.CalendarUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.util.NoNullSet;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.Code;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.storage.ICodeStore;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeDoesNotExistException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeHasChildCodesException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeInstanceDoesNotExistException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreFullException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteAbandonedCodeInstancesException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;

@XStreamAlias("codeStore")
class CodeStore implements ICodeStore {

	private static final Logger logger = Logger.getLogger(CodeStore.class);

	public static class URIConverter implements Converter {

		@Override
		public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz) {
			return clazz.equals(URI.class);
		}

		@Override
		public void marshal(Object value, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			URI uri = (URI) value;
			writer.setValue(uri.getRawURI().toString());
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			return new URI(reader.getValue());
		}

	}

	@XStreamOmitField
	private File codeStoreFile;

	@XStreamAlias("createdIDs")
	private Set<Long> createdIds = null;

	@XStreamAlias("createdCodeInstanceIDs")
	private Set<Long> createdCodeInstanceIds = null;

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
		xstream.processAnnotations(CodeInstance.class);
		xstream.processAnnotations(CodeStore.class);
		xstream.registerConverter(new URIConverter());
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
			if (codeStore.createdCodeInstanceIds == null) {
				codeStore.createdCodeInstanceIds = new HashSet<Long>();
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

			sanityCheckCodeIds(codeStore);
			sanityCheckCodeInstanceIds(codeStore);

			return codeStore;
		} catch (ArrayIndexOutOfBoundsException e) {
			return new CodeStore(codeStoreFile);
		} catch (Exception e) {
			logger.error(e);
			throw new CodeStoreReadException(e);
		}
	}

	private static Set<Long> sanityCheckCodeIds(CodeStore codeStore)
			throws CodeStoreReadException {
		Set<Long> codeIds = new HashSet<Long>();
		for (ICode code : codeStore.getCodes()) {
			if (!codeIds.contains(code.getId())) {
				codeIds.add(code.getId());
			} else {
				throw new CodeStoreReadException(new Throwable("Duplicate "
						+ ICode.class.getSimpleName() + " ID found: "
						+ code.getId()));
			}
		}
		if (!codeStore.createdIds.containsAll(codeIds)) {
			logger.error(CodeStore.class.getSimpleName()
					+ " contains "
					+ ICode.class.getSimpleName()
					+ " whose IDs are not part of the set of created IDs. There must be an implementation error. Unknown ID will be automatically added now.");
			codeStore.createdIds.addAll(codeIds);
		}
		return codeIds;
	}

	private static void sanityCheckCodeInstanceIds(CodeStore codeStore)
			throws CodeStoreReadException {
		Set<Long> codeInstanceIds = new HashSet<Long>();
		for (ICodeInstance codeInstance : codeStore.codeInstances) {
			if (!codeInstanceIds.contains(codeInstance.getCodeInstanceID())) {
				codeInstanceIds.add(codeInstance.getCodeInstanceID());
			} else {
				throw new CodeStoreReadException(new Throwable("Duplicate "
						+ ICodeInstance.class.getSimpleName() + " ID found: "
						+ codeInstance.getId()));
			}
		}
		if (!codeStore.createdCodeInstanceIds.containsAll(codeInstanceIds)) {
			logger.error(CodeStore.class.getSimpleName()
					+ " contains "
					+ ICodeInstance.class.getSimpleName()
					+ " whose IDs are not part of the set of created IDs. There must be an implementation error. Unknown ID will be automatically added now.");
			codeStore.createdCodeInstanceIds.addAll(codeInstanceIds);
		}
	}

	private CodeStore(File codeStoreFile) {
		this.codeStoreFile = codeStoreFile;
		this.createdIds = new TreeSet<Long>();
		this.createdCodeInstanceIds = new HashSet<Long>();
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
	public ICodeInstance getCodeInstance(long id) {
		for (ICodeInstance codeInstance : this.codeInstances) {
			if (codeInstance.getCodeInstanceID() == id) {
				return codeInstance;
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
	public Set<ICodeInstance> getInstances() {
		return (Set<ICodeInstance>) this.codeInstances.clone();
	}

	@Override
	public ICode createCode(String caption, RGB color)
			throws CodeStoreFullException {
		for (TreeNode<ICode> codeTree : this.codeTrees) {
			for (ICode code : codeTree) {
				if (code.getId() == Long.MAX_VALUE) {
					throw new CodeStoreFullException();
				}
			}
		}
		long id = Code.calculateId(this.createdIds);
		this.createdIds.add(id);

		ICode code = new Code(id, caption, color, new TimeZoneDate());
		this.codeTrees.add(new TreeNode<ICode>(code));
		return code;
	}

	@Override
	public ICodeInstance[] createCodeInstances(ICode[] codes, URI[] uris)
			throws InvalidParameterException, CodeStoreReadException,
			DuplicateCodeInstanceException, CodeStoreFullException {
		for (ICode code : codes) {
			Assert.isNotNull(code);
		}
		for (URI uri : uris) {
			Assert.isNotNull(uri);
		}

		for (ICodeInstance codeInstance : this.codeInstances) {
			if (codeInstance.getCodeInstanceID() == Long.MAX_VALUE) {
				throw new CodeStoreFullException();
			}
		}

		List<ICodeInstance> duplicateCodeInstances = new LinkedList<ICodeInstance>();
		List<ICodeInstance> generatedCodeInstances = new LinkedList<ICodeInstance>();
		for (ICode code : codes) {
			if (this.assertiveFind(code) != null) {
				for (URI uri : uris) {
					long codeInstanceID = Code
							.calculateId(this.createdCodeInstanceIds);
					this.createdIds.add(codeInstanceID);

					ICodeInstance codeInstance = new CodeInstance(
							codeInstanceID, code, uri, new TimeZoneDate(
									new Date(), TimeZone.getDefault()));

					boolean successful = true;
					for (ICodeInstance existing : this.codeInstances) {
						if (existing.getCode().equals(codeInstance.getCode())
								&& existing.getId()
										.equals(codeInstance.getId())) {
							duplicateCodeInstances.add(existing);
							duplicateCodeInstances.add(codeInstance);
							successful = false;
						}
					}

					if (successful) {
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
		this.createdIds.add(code.getId());
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
			this.createdCodeInstanceIds.add(codeInstance.getCodeInstanceID());
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

		if (LocatorService.INSTANCE != null) {
			LocatorService.INSTANCE.uncache(code.getUri());
		}

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
		TreeNode<ICode> futureChildNode = this.assertiveFind(code);

		if (futureChildNode == null) {
			throw new CodeDoesNotExistException(code);
		}

		TreeNode<ICode> futureParentNode = this.assertiveFind(parentCode);
		TreeNode<ICode> currentParentNode = futureChildNode.getParent();

		if (futureChildNode == futureParentNode) {
			throw new CodeStoreIntegrityProtectionException("Child node"
					+ futureChildNode + " can't be his own parent node");
		}
		if (futureChildNode.isAncestorOf(futureParentNode)) {
			throw new CodeStoreIntegrityProtectionException("Node"
					+ futureChildNode
					+ " can't be made a child node of its current child node "
					+ parentCode);
		}

		// TODO: Komplexe Schleife

		// remove from old parent
		if (currentParentNode != null) {
			futureChildNode.removeFromParent();
		} else if (this.codeTrees.contains(futureChildNode)) {
			this.codeTrees.remove(futureChildNode);
		} else {
			assert false;
		}

		// add to new parent
		if (futureParentNode != null) {
			futureParentNode.add(futureChildNode);
		} else {
			this.codeTrees.add(futureChildNode);
		}

		this.save();
		return (currentParentNode != null) ? currentParentNode.getData() : null;
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
	public File getBackupFile() {
		int tries = 0;
		File backupFile;
		do {
			String absPath = this.codeStoreFile.getAbsolutePath();
			String extension = FilenameUtils.getExtension(absPath);
			String date = CalendarUtils.toISO8601FileSystemCompatible(Calendar
					.getInstance());
			backupFile = new File(absPath.substring(0, absPath.length()
					- extension.length())
					+ date + "." + (tries == 0 ? "" : tries + ".") + extension);
			tries++;
		} while (backupFile.exists());
		return backupFile;
	}

	@Override
	public void save() throws CodeStoreWriteException {
		try {
			File backupFile = this.getBackupFile();
			FileUtils.moveFile(this.codeStoreFile, backupFile);
			xstream.toXML(this, new OutputStreamWriter(new FileOutputStream(
					this.codeStoreFile), "UTF-8"));

			if (FileUtils.readFileToString(backupFile).equals(
					FileUtils.readFileToString(this.codeStoreFile))) {
				backupFile.delete();
			}
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
	 * @param uri
	 * @return
	 */
	protected static String getMemoBasename(URI uri) {
		if (uri == null) {
			throw new InvalidParameterException();
		}
		try {
			return "codeInstance_" + URLEncoder.encode(uri.toString(), "UTF-8");
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
	public String getMemo(URI uri) {
		String memo = null;
		try {
			memo = this.loadMemo(getMemoBasename(uri));
		} catch (IOException e) {
			logger.error("Error reading memo for " + uri);
		}
		if (memo == null) {
			return this.memos != null ? this.memos.get(uri) : null;
		} else {
			return memo;
		}
	};

	// TODO remove
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

	// TODO remove
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
	public void setMemo(URI uri, String html) throws CodeStoreWriteException {
		try {
			this.saveMemo(getMemoBasename(uri), html);
		} catch (IOException e) {
			throw new CodeStoreWriteException(e);
		}
		if (this.memos != null) {
			this.memos.remove(uri);
		}
		this.save();
	}

	private String getRawBasename(String type, URI uri)
			throws UnsupportedEncodingException {
		return type + "." + URLEncoder.encode(uri.toString(), "UTF-8");
	}

	private URI getRawUri(String filename) throws UnsupportedEncodingException {
		int prefixLength = filename.indexOf('.') + 1;
		return new URI(URLDecoder.decode(filename.substring(prefixLength),
				"UTF-8"));
	}

	private File getRawFile(String type, URI uri)
			throws UnsupportedEncodingException {
		String filename = this.getRawBasename(type, uri);
		File file = new File(this.codeStoreFile.getParentFile(), filename);
		return file;
	}

	@Override
	public List<URI> getRaw(final String type) throws CodeStoreReadException {
		final String prefix = type + ".";

		List<URI> uris = new LinkedList<URI>();
		try {
			for (File file : this.getRawFile(type, new URI("")).getParentFile()
					.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File arg0, String arg1) {
							return arg1.startsWith(prefix);
						}
					})) {
				URI uri = this.getRawUri(file.getName());
				uris.add(uri);
			}
		} catch (UnsupportedEncodingException e) {
			throw new CodeStoreReadException(e);
		}
		return uris;
	}

	@Override
	public String getRaw(String type, URI uri) throws CodeStoreReadException {
		try {
			File rawFile = this.getRawFile(type, uri);
			try {
				if (rawFile.exists()) {
					return FileUtils.readFileToString(rawFile, "UTF-8");
				}
			} catch (IOException e) {
				throw new CodeStoreReadException(e);
			}
		} catch (UnsupportedEncodingException e) {
			throw new CodeStoreReadException(e);
		}

		return null;
	}

	@Override
	public void setRaw(String type, URI uri, String content)
			throws CodeStoreWriteException {
		Assert.isNotNull(type);
		Assert.isLegal(!type.contains("."));
		Assert.isNotNull(uri);

		try {
			File rawFile = this.getRawFile(type, uri);
			try {
				if ((content == null || content.trim().equals(""))
						&& rawFile.exists()) {
					rawFile.delete();
				} else {
					try {
						FileUtils.writeStringToFile(rawFile, content, "UTF-8");
					} catch (FileNotFoundException e) {
						throw new CodeStoreWriteException(e);
					}
				}
			} catch (IOException e) {
				throw new CodeStoreWriteException(e);
			}
		} catch (UnsupportedEncodingException e) {
			throw new CodeStoreWriteException(e);
		}
	}

	@Override
	public Set<IEpisode> getEpisodes() {
		return this.episodes;
	}

	@Override
	public String toString() {
		ICode[] codes = this.getCodes();

		StringBuilder sb = new StringBuilder("Code Store - #codes: "
				+ codes.length + ", #instances: " + this.codeInstances.size());
		sb.append("\n");
		sb.append("- Codes IDs:");
		for (ICode code : codes) {
			sb.append(" " + code.getId());
		}
		sb.append("\n");
		sb.append("- Instance IDs:");
		for (ICodeInstance codeInstance : this.codeInstances) {
			sb.append(" " + codeInstance.getCodeInstanceID());
		}

		return sb.toString();
	}
}
