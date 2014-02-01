package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.gt.DiffLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecordSegment;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecords;

public class DiffRecordUtils {
	public static Logger LOGGER = Logger.getLogger(DiffRecordUtils.class);

	private static enum PARSE_STATE {
		READING_CONTENT, EXPECTING_OLD_LINE, EXPECTING_NEW_LINE;
	}

	/**
	 * Light-weight descriptor for {@link DiffRecord}s
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class DiffRecordDescriptor {
		String commandLine;
		String metaOldLine;
		String metaNewLine;
		long contentStart;
		long contentEnd;

		public DiffRecordDescriptor(String commandLine, String metaOldLine,
				String metaNewLine, long contentStart, long contentEnd) {
			this.commandLine = commandLine;
			this.metaOldLine = metaOldLine;
			this.metaNewLine = metaNewLine;
			this.contentStart = contentStart;
			this.contentEnd = contentEnd;
		}
	}

	public static List<DiffRecordDescriptor> readDescriptors(IData data,
			IProgressMonitor progressMonitor) {
		SubMonitor monitor = SubMonitor.convert(progressMonitor);
		monitor.beginTask(
				"Detecting " + DiffRecord.class.getSimpleName() + "s",
				(int) (data.getLength() / 1000));

		PARSE_STATE state = PARSE_STATE.READING_CONTENT;
		String commandLine = null;
		String metaOldLine = null;
		String metaNewLine = null;
		long contentStart = 0l;
		long contentEnd = 0l;

		LinkedList<DiffRecordDescriptor> descriptors = new LinkedList<DiffRecordDescriptor>();
		Integer newLineLength = null;
		try {
			for (String line : data) {
				if (line.equals("RESET")) {
					break;
				}

				long lineLength = line.getBytes().length;
				if (newLineLength == null) {
					try {
						newLineLength = FileUtils.getNewlineLengthAt(data,
								contentEnd + lineLength);
					} catch (Exception e) {
						continue;
					}
				}

				// On empty lines Windows only uses \r instead of \r\n
				lineLength += line.isEmpty() ? 1 : newLineLength;

				String[] x = line.split(" ");
				if (state == PARSE_STATE.READING_CONTENT
						&& x.length > 0
						&& (x[0].equals("diff") || x[0].equals("Files") || x[0]
								.equals("Binary"))) {
					// create record if new record is found
					if (commandLine != null) {
						descriptors.add(new DiffRecordDescriptor(commandLine,
								metaOldLine, metaNewLine, contentStart,
								contentEnd - newLineLength));
						commandLine = null;
						contentStart = contentEnd;
					}

					if (x[0].equals("diff")) {
						commandLine = line;
						state = PARSE_STATE.EXPECTING_OLD_LINE;
					}

					contentStart += lineLength;
				} else {
					if (state == PARSE_STATE.EXPECTING_OLD_LINE) {
						metaOldLine = line;
						state = PARSE_STATE.EXPECTING_NEW_LINE;
					} else if (state == PARSE_STATE.EXPECTING_NEW_LINE) {
						metaNewLine = line;
						state = PARSE_STATE.READING_CONTENT;
					}
				}

				contentEnd += lineLength;

				monitor.worked((int) (lineLength / 1000));
				if (progressMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
		} catch (Exception e) {
			monitor.beginTask("Aborting " + Diff.class.getSimpleName()
					+ " parsing", 1);
			LOGGER.error("Could not open " + Diff.class.getSimpleName(), e);
			monitor.done();
			return null;
		}

		// create record if EOF
		if (commandLine != null) {
			descriptors.add(new DiffRecordDescriptor(commandLine, metaOldLine,
					metaNewLine, contentStart, contentEnd - newLineLength));
		}

		monitor.done();
		return descriptors;
	}

	public static DiffRecords createRecordsFromDescriptors(Diff diff,
			ITrunk trunk, ISourceStore sourceCache,
			List<DiffRecordDescriptor> descriptors,
			IProgressMonitor progressMonitor) {
		SubMonitor monitor = SubMonitor.convert(progressMonitor);
		monitor.beginTask("Creating " + DiffRecord.class.getSimpleName() + "s",
				descriptors.size());

		DiffRecords diffRecords = new DiffRecords(diff, trunk, sourceCache);

		for (DiffRecordDescriptor descriptor : descriptors) {
			diffRecords.createAndAddRecord(descriptor.commandLine,
					descriptor.metaOldLine, descriptor.metaNewLine,
					descriptor.contentStart, descriptor.contentEnd);
			monitor.worked(1);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
		monitor.done();

		return diffRecords;
	}

	public static DiffRecords createRecordsFromZip(Diff diff, ITrunk trunk,
			ISourceStore sourceCache, IProgressMonitor progressMonitor) {
		SubMonitor monitor = SubMonitor.convert(progressMonitor);

		TimeZone timeZone = DiffDataUtils.getDate(diff, null).getTimeZone();

		DiffRecords diffRecords = new DiffRecords(diff, trunk, sourceCache);

		try {
			File file = diff.getStaticFile();
			if (file.length() > 22) { // otherwise empty
				ZipFile zipFile = new ZipFile(file);
				Enumeration<?> enu = zipFile.entries();
				while (enu.hasMoreElements()) {
					monitor.setWorkRemaining(10);

					ZipEntry zipEntry = (ZipEntry) enu.nextElement();
					File createdSourceFile = sourceCache.getSourceFile(
							diff.getIdentifier(), diff.getRevision(),
							zipEntry.getName());
					if (createdSourceFile != null
							&& createdSourceFile.length() == zipEntry.getSize()) {
						LOGGER.info("Skipping "
								+ zipEntry.getName()
								+ " because it was already successfully uncompressed.");
					} else {
						File dest = File.createTempFile(
								"SUA-",
								"-"
										+ zipEntry.getName().replaceAll(
												"[\\\\/]", "---"));

						InputStream is = zipFile.getInputStream(zipEntry);
						FileOutputStream fos = new FileOutputStream(dest);
						byte[] bytes = new byte[10240];
						int length;
						while ((length = is.read(bytes)) >= 0) {
							fos.write(bytes, 0, length);
						}
						is.close();
						fos.close();

						if (zipEntry.getTime() == -1) {
							LOGGER.error("Zipped entry has no date");
						}

						sourceCache.setSourceFile(diff.getIdentifier(),
								diff.getRevision(), zipEntry.getName(), dest);

						LOGGER.info("Successfully uncompressed "
								+ zipEntry.getName() + " (size: "
								+ zipEntry.getSize() + ")");
					}
					TimeZoneDate date = new TimeZoneDate(new Date(
							zipEntry.getTime()), timeZone);
					diffRecords.createAndAddRecord(zipEntry.getName(), date);
				}
				zipFile.close();
				monitor.worked(1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		monitor.done();

		return diffRecords;
	}

	public static DiffRecords readRecords(Diff diff, ITrunk trunk,
			ISourceStore sourceCache, IProgressMonitor progressMonitor) {
		SubMonitor monitor = SubMonitor.convert(progressMonitor);
		monitor.beginTask("Processing " + diff.getName(), 3);
		final long start = System.currentTimeMillis();

		DiffRecords diffRecords = null;
		if (diff.getName().endsWith(".diff")) {
			List<DiffRecordDescriptor> descriptors = readDescriptors(diff,
					monitor.newChild(1));
			diffRecords = createRecordsFromDescriptors(diff, trunk,
					sourceCache, descriptors, monitor.newChild(2));

			LOGGER.info("Parsed " + Diff.class.getSimpleName() + " \""
					+ diff.getName() + "\": " + diffRecords.size() + " "
					+ DiffRecord.class.getSimpleName() + "s found within "
					+ (System.currentTimeMillis() - start) + "ms.");
		} else {
			diffRecords = createRecordsFromZip(diff, trunk, sourceCache,
					monitor.newChild(2));

			LOGGER.info("Extracted " + Diff.class.getSimpleName() + " \""
					+ diff.getName() + "\": " + diffRecords.size() + " "
					+ DiffRecord.class.getSimpleName() + "s done within "
					+ (System.currentTimeMillis() - start) + "ms.");
		}

		monitor.done();

		return diffRecords;
	}

	/**
	 * Looks for {@link DiffRecordSegment}s and returns a list of the
	 * corresponding {@link DiffRecord}s.
	 * 
	 * @param uris
	 * @return
	 */
	public static List<URI> getRecordsFromSegments(URI[] uris) {
		List<URI> diffRecords = new LinkedList<URI>();
		for (URI uri : uris) {
			if (!DiffLocatorProvider.DIFF_NAMESPACE.equals(URIUtils
					.getResource(uri))) {
				continue;
			}
			if (URIUtils.getIdentifier(uri) == null) {
				continue;
			}
			if (URIUtils.getTrail(uri).size() == 0) {
				continue;
			}

			int hashIndex = uri.toString().indexOf('#');
			if (hashIndex != -1) {
				String base = uri.toString().substring(0, hashIndex);
				try {
					diffRecords.add(new URI(base));
				} catch (URISyntaxException e) {
					LOGGER.fatal("Implementation error", e);
				}
			} else {
				diffRecords.add(uri);
			}
		}
		return diffRecords;
	}
}
