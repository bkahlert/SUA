package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.AggregatedBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil.ParametrizedCallable;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.util.DoclogCache;

public class DoclogDataDirectory extends AggregatedBaseDataContainer {

	private static final Logger LOGGER = Logger
			.getLogger(DoclogDataDirectory.class);

	public static final int DOCLOG_CACHE_SIZE = 10;

	private static final ExecutorService LOADER_POOL = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(1);

	private static Map<Object, IData> readDoclogFileMappings(
			DoclogDataDirectory directory) {
		Map<Object, IData> rawDataResource = new HashMap<Object, IData>();
		for (IData doclogDataResource : directory.getDoclogDirectory()
				.getResources()) {
			if (!Doclog.ID_PATTERN.matcher(
					doclogDataResource.getName()).matches())
				continue;
			ID id = Doclog.getID(doclogDataResource);
			rawDataResource.put(id, doclogDataResource);
		}
		for (IData doclogDataResource : directory.getDoclogDirectory()
				.getResources()) {
			if (!Doclog.FINGERPRINT_PATTERN.matcher(
					doclogDataResource.getName()).matches())
				continue;
			Fingerprint fingerprint = Doclog
					.getFingerprint(doclogDataResource);
			rawDataResource.put(fingerprint, doclogDataResource);
		}
		return rawDataResource;
	}

	private Map<Object, IData> datas;
	private Map<Object, TimeZoneDateRange> fileDateRanges;
	private Map<Object, Token> fileToken;

	private final IDataContainer doclogDirectory;
	private final IData mappingFile;
	private DoclogCache doclogCache;

	public DoclogDataDirectory(
			List<? extends IBaseDataContainer> dataResourceContainers) {
		super(dataResourceContainers);
		this.doclogDirectory = this.getSubContainer("doclog");
		this.mappingFile = this.getResource("mapping.xml");
		this.doclogCache = new DoclogCache(this, DOCLOG_CACHE_SIZE);
	}

	public DoclogDataDirectory(IBaseDataContainer dataResourceContainer) {
		this(Arrays.asList(dataResourceContainer));
	}

	public IDataContainer getDoclogDirectory() {
		return doclogDirectory;
	}

	public IData getMappingFile() {
		return mappingFile;
	}

	public void scan(final SubMonitor monitor) {
		this.datas = readDoclogFileMappings(this);
		this.fileDateRanges = new HashMap<Object, TimeZoneDateRange>(
				this.datas.size());
		this.fileToken = new HashMap<Object, Token>(this.datas.size());

		long size = 0;
		for (Object key : this.datas.keySet())
			size += this.datas.get(key).getLength();
		monitor.beginTask("Loading " + this.getName(), (int) (size / 1000l));

		// force class loading since DoclogRecord is used in the Callable
		DoclogAction.class.getClass();
		DoclogRecord.class.getClass();
		List<Future<Integer>> futures = ExecutorUtil.nonUIAsyncExec(
				LOADER_POOL, this.datas.keySet(),
				new ParametrizedCallable<Object, Integer>() {
					@Override
					public Integer call(Object key) throws Exception {
						final IData data = datas.get(key);
						try {
							TimeZoneDateRange dateRange = Doclog
									.getDateRange(data);
							Token token = Doclog.getToken(data);
							synchronized (fileDateRanges) {
								fileDateRanges.put(key, dateRange);
							}
							synchronized (fileToken) {
								fileToken.put(key, token);
							}
						} catch (Exception e) {
							LOGGER.error(e);
						}
						return (int) (data.getLength() / 1000l);
					}
				});
		for (Future<Integer> future : futures) {
			try {
				int worked = future.get();
				monitor.worked(worked);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			} catch (ExecutionException e) {
				LOGGER.error(e);
			}
		}
	}

	/**
	 * Returns a list of all {@link ID}s occurring in the managed
	 * {@link Doclog}s
	 * 
	 * @return
	 */
	public List<ID> getIDs() {
		List<ID> doclogIDs = new ArrayList<ID>();
		for (Object key : this.datas.keySet()) {
			if (key instanceof ID)
				doclogIDs.add((ID) key);
		}
		return doclogIDs;
	}

	/**
	 * Returns the {@link ID} that is associated to a {@link Doclog}
	 * containing the specified {@link Token}.
	 * 
	 * @param token
	 * @return
	 */
	public ID getID(Token token) {
		for (Object key : this.fileToken.keySet()) {
			if (key instanceof ID && this.fileToken.get(key) != null
					&& this.fileToken.get(key).equals(token)) {
				return (ID) key;
			}
		}
		return null;
	}

	/**
	 * Returns a list of all {@link Fingerprint}s occurring in the managed
	 * {@link Doclog}s
	 * 
	 * @return
	 */
	public List<Fingerprint> getFingerprints() {
		List<Fingerprint> doclogFingerprints = new ArrayList<Fingerprint>();
		for (Object key : this.datas.keySet()) {
			if (key instanceof Fingerprint)
				doclogFingerprints.add((Fingerprint) key);
		}
		return doclogFingerprints;
	}

	/**
	 * Returns a list of the {@link Fingerprint}s that are associated to a
	 * {@link Doclog} containing the specified {@link Token}.
	 * 
	 * @param token
	 * @return
	 */
	public List<Fingerprint> getFingerprints(Token token) {
		LinkedList<Fingerprint> fingerprints = new LinkedList<Fingerprint>();
		for (Object key : this.fileToken.keySet()) {
			if (key instanceof Fingerprint && this.fileToken.get(key) != null
					&& this.fileToken.get(key).equals(token)) {
				fingerprints.add((Fingerprint) key);
			}
		}
		return fingerprints;
	}

	public Token getToken(Object key) {
		for (Object currentKey : this.fileToken.keySet()) {
			if (currentKey.equals(key)) {
				return this.fileToken.get(key);
			}
		}
		return null;
	}

	public TimeZoneDateRange getDateRange(Object key) {
		return this.fileDateRanges.get(key);
	}

	/**
	 * Returns a list of all keys (of types {@link ID} and {@link Fingerprint}
	 * occurring in the managed {@link Doclog}s
	 * 
	 * @return
	 */
	public Set<Object> getKeys() {
		return this.datas.keySet();
	}

	/**
	 * Returns a {@link File}Êthat can be parsed as a {@link ID} based
	 * {@link Doclog}.
	 * 
	 * @param id
	 * @return
	 */
	public IData getFile(ID id) {
		return this.datas.get(id);
	}

	/**
	 * Returns a {@link File}Êthat can be parsed as a {@link Fingerprint} based
	 * {@link Doclog}.
	 * 
	 * @param fingerprint
	 * @return
	 */
	public IData getFile(Fingerprint fingerprint) {
		return this.datas.get(fingerprint);
	}

	/**
	 * Returns the {@link Doclog} associated with a given {@link ID}
	 * 
	 * @param id
	 * @param progressMonitor
	 * @return
	 */
	public Doclog getDoclogFile(ID id,
			IProgressMonitor progressMonitor) {
		return this.getDoclogFile((Object) id, progressMonitor);
	}

	/**
	 * Returns the {@link Doclog} associated with a given
	 * {@link Fingerprint}
	 * 
	 * @param fingerprint
	 * @param progressMonitor
	 * @return
	 */
	public Doclog getDoclogFile(Fingerprint fingerprint,
			IProgressMonitor progressMonitor) {
		return this.getDoclogFile((Object) fingerprint, progressMonitor);
	}

	/**
	 * Returns the {@link Doclog} associated with a given key using
	 * an internal {@link DoclogCache}.
	 * 
	 * @param key
	 * @param progressMonitor
	 * @return
	 */
	public Doclog getDoclogFile(Object key,
			IProgressMonitor progressMonitor) {
		return this.doclogCache.getPayload(key, progressMonitor);
	}

	/**
	 * Returns the {@link Doclog} associated with a given key.
	 * <p>
	 * In contrast to {@link #getDoclogFile(Object, IProgressMonitor)} this
	 * method always creates the needed objects anew without using any cache.
	 * 
	 * @param key
	 * @param progressMonitor
	 * @return
	 */
	public Doclog createDoclogFile(Object key,
			IProgressMonitor progressMonitor) {
		progressMonitor.beginTask(
				"Parsing " + Doclog.class.getSimpleName(), 2);
		IData data = this.datas.get(key);
		if (data == null)
			return null;

		TimeZoneDateRange dateRange = this.fileDateRanges.get(key);
		Token token = this.fileToken.get(key);
		progressMonitor.worked(1);
		Doclog doclog = new Doclog(data,
				key, dateRange, token);
		progressMonitor.worked(1);
		progressMonitor.done();
		return doclog;
	}
}
