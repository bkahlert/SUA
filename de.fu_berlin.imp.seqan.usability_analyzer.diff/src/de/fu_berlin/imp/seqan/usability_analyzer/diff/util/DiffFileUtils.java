package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecordList;

public class DiffFileUtils {
	public static Logger logger = Logger.getLogger(DiffFileUtils.class);

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

	public static DiffFileRecordList readRecords(DiffFile diffFile,
			SourceOrigin sourceOrigin, SourceCache sourceCache,
			IProgressMonitor progressMonitor) {

		String commandLine = null;
		String metaOldLine = null;
		String metaNewLine = null;
		long contentStart = 0l;
		long contentEnd = 0l;

		progressMonitor.beginTask("Processing " + diffFile.getName(), 3);
		final long start = System.currentTimeMillis();

		SubProgressMonitor detectionMonitor = new SubProgressMonitor(
				progressMonitor, 1);
		detectionMonitor.beginTask(
				"Detecting " + DiffFileRecord.class.getSimpleName() + "s",
				(int) (diffFile.length() / 1000));

		LinkedList<DiffFileRecordDescriptor> descriptors = new LinkedList<DiffFileRecordDescriptor>();
		FileInputStream fstream = null;
		Byte newLineLength = null;
		try {
			fstream = new FileInputStream(diffFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;

			while ((strLine = br.readLine()) != null) {
				if (strLine.equals("RESET"))
					break;

				long lineLength = strLine.getBytes().length;
				if (newLineLength == null)
					newLineLength = de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils
							.getNewlineLengthAt(diffFile, contentEnd
									+ lineLength);

				// On empty lines Windows only uses \r instead of \r\n
				lineLength += strLine.isEmpty() ? 1 : newLineLength;

				String[] x = strLine.split(" ");
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
						commandLine = strLine;
					}

					contentStart += lineLength;
				} else {
					if (x.length > 0) {
						if (x[0].equals("---"))
							metaOldLine = strLine;
						else if (x[0].equals("+++"))
							metaNewLine = strLine;
					}
				}

				contentEnd += lineLength;

				detectionMonitor.worked((int) (lineLength / 1000));
				if (progressMonitor.isCanceled())
					throw new OperationCanceledException();
			}
		} catch (IOException e) {
			detectionMonitor.beginTask(
					"Aborting " + DiffFile.class.getSimpleName() + " parsing",
					1);
			logger.error("Could not open doclog file", e);
			detectionMonitor.done();
			return null;
		} finally {
			if (fstream != null)
				try {
					fstream.close();
				} catch (IOException e) {
				}
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
				"Creating " + DiffFileRecord.class.getSimpleName() + "s",
				descriptors.size());

		DiffFileRecordList diffFileRecords = new DiffFileRecordList(diffFile,
				sourceOrigin, sourceCache);

		for (DiffFileRecordDescriptor descriptor : descriptors) {
			diffFileRecords.createAndAddRecord(descriptor.commandLine,
					descriptor.metaOldLine, descriptor.metaNewLine,
					descriptor.contentStart, descriptor.contentEnd);
			creationMonitor.worked(1);
			if (progressMonitor.isCanceled())
				throw new OperationCanceledException();
		}

		creationMonitor.done();

		logger.info(diffFileRecords.size() + " "
				+ DiffFileRecord.class.getSimpleName() + "s parsed in "
				+ diffFile.getName() + " within "
				+ (System.currentTimeMillis() - start) + "ms.");
		progressMonitor.done();

		return diffFileRecords;
	}
}
