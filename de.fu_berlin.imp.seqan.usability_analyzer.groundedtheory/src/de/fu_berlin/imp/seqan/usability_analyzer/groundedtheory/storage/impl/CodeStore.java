package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.log4j.Logger;

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
	private Set<Long> createdIds = new TreeSet<Long>();

	@XStreamAlias("codes")
	private HashSet<ICode> codes = null;

	@XStreamAlias("instances")
	private HashSet<ICodeInstance> codeInstances = null;

	private static XStream xstream;

	static {
		xstream = new XStream();
		xstream.alias("code", Code.class);
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
			return codeStore;
		} catch (ArrayIndexOutOfBoundsException e) {
			return new CodeStore(codeStoreFile);
		} catch (Exception e) {
			throw new CodeStoreReadException(e);
		}
	}

	private CodeStore(File codeStoreFile) {
		this.codeStoreFile = codeStoreFile;
		this.codes = new HashSet<ICode>();
		this.codeInstances = new HashSet<ICodeInstance>();
	}

	@Override
	public ICode getCode(long id) {
		for (ICode code : this.codes) {
			if (code.getId() == id) {
				return code;
			}
		}
		return null;
	}

	private void setCodeStoreFile(File codeStoreFile) {
		this.codeStoreFile = codeStoreFile;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<ICode> getTopLevelCodes() {
		return (Set<ICode>) this.codes.clone();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<ICodeInstance> loadInstances() {
		return (Set<ICodeInstance>) this.codeInstances.clone();
	}

	public ICode createCode(String caption) throws CodeStoreFullException {
		Long id = Long.MAX_VALUE;
		ArrayList<Long> ids = new ArrayList<Long>(codes.size());
		for (ICode code : codes) {
			if (code.getId() == Long.MAX_VALUE)
				throw new CodeStoreFullException();
			ids.add(code.getId());
		}
		ids.addAll(createdIds);
		id = Code.calculateId(ids);
		createdIds.add(id);
		return new Code(id, caption);
	}

	@Override
	public ICodeInstance createCodeInstance(ICode code, ICodeable codeable)
			throws InvalidParameterException, CodeStoreReadException,
			DuplicateCodeInstanceException {
		for (ICode currentCode : codes) {
			if (currentCode.equals(code)) {
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
		this.codes.add(code);
		this.save();
	}

	@Override
	public void addAndSaveCodeInstance(ICodeInstance codeInstance)
			throws CodeStoreWriteException {
		if (!this.codes.contains(codeInstance.getCode()))
			throw new CodeStoreWriteAbandonedCodeInstancesException(
					Arrays.asList(codeInstance));
		;
		this.codeInstances.add(codeInstance);
		this.save();
	}

	@Override
	public void removeAndSaveCode(ICode code) throws CodeStoreWriteException,
			CodeStoreReadException {
		List<ICodeInstance> abandoned = new LinkedList<ICodeInstance>();
		for (ICodeInstance instance : this.codeInstances)
			if (instance.getCode().equals(code))
				abandoned.add(instance);
		if (abandoned.size() > 0)
			throw new CodeStoreWriteAbandonedCodeInstancesException(abandoned);

		this.codes.remove(code);
		this.save();
	}

	@Override
	public void removeAndSaveCodeInstance(ICodeInstance codeInstance)
			throws CodeStoreWriteException, CodeStoreReadException {
		this.codeInstances.remove(codeInstance);
		this.save();
	}

	@Override
	public void save() throws CodeStoreWriteException {
		try {
			xstream.toXML(this, new FileWriter(codeStoreFile));
			System.err.println(xstream.toXML(this));
		} catch (IOException e) {
			throw new CodeStoreWriteException(e);
		}
	}

	@Override
	public void deleteCodeInstance(ICodeInstance codeInstance)
			throws CodeInstanceDoesNotExistException, CodeStoreWriteException,
			CodeStoreReadException {
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
	public void deleteCode(ICode code) throws CodeStoreReadException,
			CodeStoreWriteException, CodeDoesNotExistException {
		if (!codes.contains(code))
			throw new CodeDoesNotExistException();
		deleteCodeInstances(code);
		this.codes.remove(code);
		this.save();
	}
}
