package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecordSegment;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecordList;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class DiffDataResourceUtils {
	public static Logger LOGGER = Logger.getLogger(DiffDataResourceUtils.class);

	private static class DiffFileRecordDescriptor {
		String commandLine;
		String metaOldLine;
		String metaNewLine;
		private long contentStart;
		private long contentEnd;

		public DiffFileRecordDescriptor(String commandLine, String metaOldLine,
				String metaNewLine, long contentStart, long contentEnd) {
			this.commandLine = commandLine;
			this.metaOldLine = metaOldLine;
			this.metaNewLine = metaNewLine;
			this.contentStart = contentStart;
			this.contentEnd = contentEnd;
		}
	}

	public static DiffRecordList readRecords(Diff diff, ITrunk trunk,
			ISourceStore sourceCache, IProgressMonitor progressMonitor) {

		String commandLine = null;
		String metaOldLine = null;
		String metaNewLine = null;
		long contentStart = 0l;
		long contentEnd = 0l;

		progressMonitor.beginTask("Processing " + diff.getName(), 3);
		final long start = System.currentTimeMillis();

		SubProgressMonitor detectionMonitor = new SubProgressMonitor(
				progressMonitor, 1);
		detectionMonitor.beginTask(
				"Detecting " + DiffRecord.class.getSimpleName() + "s",
				(int) (diff.getLength() / 1000));

		LinkedList<DiffFileRecordDescriptor> descriptors = new LinkedList<DiffFileRecordDescriptor>();
		Integer newLineLength = null;
		try {
			for (String line : diff) {
				if (line.equals("RESET"))
					break;

				long lineLength = line.getBytes().length;
				if (newLineLength == null) {
					try {
						newLineLength = de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils
								.getNewlineLengthAt(diff, contentEnd
										+ lineLength);
					} catch (Exception e) {
						continue;
					}
				}

				// On empty lines Windows only uses \r instead of \r\n
				lineLength += line.isEmpty() ? 1 : newLineLength;

				String[] x = line.split(" ");
				if (x.length > 0
						&& (x[0].equals("diff") || x[0].equals("Files") || x[0]
								.equals("Binary"))) {
					// create record if new record is found
					if (commandLine != null) {
						descriptors.add(new DiffFileRecordDescriptor(
								commandLine, metaOldLine, metaNewLine,
								contentStart, contentEnd - newLineLength));
						commandLine = null;
						contentStart = contentEnd;
					}

					if (x[0].equals("diff")) {
						commandLine = line;
					}

					contentStart += lineLength;
				} else {
					if (x.length > 0) {
						if (x[0].equals("---"))
							metaOldLine = line;
						else if (x[0].equals("+++"))
							metaNewLine = line;
					}
				}

				contentEnd += lineLength;

				detectionMonitor.worked((int) (lineLength / 1000));
				if (progressMonitor.isCanceled())
					throw new OperationCanceledException();
			}
		} catch (Exception e) {
			detectionMonitor.beginTask(
					"Aborting " + Diff.class.getSimpleName() + " parsing",
					1);
			LOGGER.error("Could not open " + Diff.class.getSimpleName(), e);
			detectionMonitor.done();
			return null;
		}

		// create record if EOF
		if (commandLine != null) {
			descriptors.add(new DiffFileRecordDescriptor(commandLine,
					metaOldLine, metaNewLine, contentStart, contentEnd
							- newLineLength));
		}

		detectionMonitor.done();

		SubProgressMonitor creationMonitor = new SubProgressMonitor(
				progressMonitor, 2);
		creationMonitor.beginTask(
				"Creating " + DiffRecord.class.getSimpleName() + "s",
				descriptors.size());

		DiffRecordList diffRecords = new DiffRecordList(diff, trunk,
				sourceCache);

		for (DiffFileRecordDescriptor descriptor : descriptors) {
			diffRecords.createAndAddRecord(descriptor.commandLine,
					descriptor.metaOldLine, descriptor.metaNewLine,
					descriptor.contentStart, descriptor.contentEnd);
			creationMonitor.worked(1);
			if (progressMonitor.isCanceled())
				throw new OperationCanceledException();
		}

		creationMonitor.done();

		LOGGER.info("Parsed " + Diff.class.getSimpleName() + " \""
				+ diff.getName() + "\": " + diffRecords.size() + " "
				+ DiffRecord.class.getSimpleName() + "s found within "
				+ (System.currentTimeMillis() - start) + "ms.");
		progressMonitor.done();

		return diffRecords;
	}

	/**
	 * Looks for {@link DiffRecordSegment}s and returns a list of the
	 * corresponding {@link DiffRecord}s.
	 * 
	 * @param codeables
	 * @return
	 */
	public static List<DiffRecord> getRecordsFromSegments(
			List<ICodeable> codeables) {
		List<DiffRecord> diffRecords = new LinkedList<DiffRecord>();
		for (ICodeable codeable : codeables) {
			if (codeable instanceof DiffRecordSegment) {
				DiffRecordSegment segment = (DiffRecordSegment) codeable;
				diffRecords.add(segment.getDiffFileRecord());
			}
		}
		return diffRecords;
	}
}
