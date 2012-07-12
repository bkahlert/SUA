package de.fu_berlin.imp.seqan.usability_analyzer.doclog.mapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.JAXBUtils;

@XmlRootElement(namespace = "de.fu_berlin.imp.seqan.usability_analyzer.srv")
public class DoclogKeyMap {

	private static final Logger LOGGER = Logger.getLogger(DoclogKeyMap.class);
	private static ConcurrentMap<File, ReentrantReadWriteLock> LOCKS = new ConcurrentHashMap<File, ReentrantReadWriteLock>();

	private static ReentrantReadWriteLock getLock(File file) {
		LOCKS.putIfAbsent(file, new ReentrantReadWriteLock());
		return LOCKS.get(file);
	}

	private static ReadLock getReadLock(File file) {
		return getLock(file).readLock();
	}

	private static WriteLock getWriteLock(File file) {
		return getLock(file).writeLock();
	}

	public static class DoclogKeyMapWrapper {
		public List<DoclogKeyMapWrapperEntry> entry = new ArrayList<DoclogKeyMapWrapperEntry>();

		public DoclogKeyMapWrapper(ConcurrentHashMap<Fingerprint, ID> v) {
			for (Map.Entry<Fingerprint, ID> e : v.entrySet())
				entry.add(new DoclogKeyMapWrapperEntry(e));
		}

		public DoclogKeyMapWrapper() {
		}

		public ConcurrentHashMap<Fingerprint, ID> getDoclogKeyMap() {
			ConcurrentHashMap<Fingerprint, ID> map = new ConcurrentHashMap<Fingerprint, ID>();
			for (DoclogKeyMapWrapperEntry e : entry)
				map.put(e.key, e.value);
			return map;
		}
	}

	public static class DoclogKeyMapWrapperEntry {
		@XmlAttribute
		public Fingerprint key;

		@XmlAttribute
		public ID value;

		public DoclogKeyMapWrapperEntry() {
		}

		public DoclogKeyMapWrapperEntry(Entry<Fingerprint, ID> e) {
			key = e.getKey();
			value = e.getValue();
		}
	}

	public static DoclogKeyMap load(File file) throws FileNotFoundException {
		try {
			getReadLock(file).lock();
			return JAXBUtils.unmarshall(DoclogKeyMap.class, file);
		} catch (JAXBException e) {
			LOGGER.error(e);
			return null;
		} finally {
			getReadLock(file).unlock();
		}
	}

	public void save(File file) throws IOException {
		try {
			getWriteLock(file).lock();
			JAXBUtils.marshall(this, file);
		} catch (JAXBException e) {
			LOGGER.error(e);
		} finally {
			getWriteLock(file).unlock();
		}
	}

	private ConcurrentHashMap<Fingerprint, ID> map = new ConcurrentHashMap<Fingerprint, ID>();

	public DoclogKeyMapWrapper getMappings() {
		return new DoclogKeyMapWrapper(map);
	}

	public void setMappings(DoclogKeyMapWrapper map) {
		this.map = map.getDoclogKeyMap();
	}

	synchronized public void associate(Fingerprint fingerprint, ID id)
			throws FinterprintAlreadyMappedException {
		ID linkedID = this.map.putIfAbsent(fingerprint, id);
		if (linkedID != null) {
			if (linkedID.equals(id)) {
				return;
			} else {
				throw new FinterprintAlreadyMappedException(fingerprint,
						linkedID, id);
			}
		}
	}

	synchronized public ID getID(Fingerprint fingerprint) {
		return this.map.get(fingerprint);
	}

	synchronized public List<Fingerprint> getFingerprints(ID id) {
		List<Fingerprint> fingerprints = new LinkedList<Fingerprint>();
		for (Entry<Fingerprint, ID> entry : this.map.entrySet()) {
			if (entry.getValue().equals(id))
				fingerprints.add(entry.getKey());
		}
		return fingerprints;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DoclogKeyMap other = (DoclogKeyMap) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}

}
