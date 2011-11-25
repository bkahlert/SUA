package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecordList;

public class DiffFileUtils {
	public static Logger logger = Logger.getLogger(DiffFileUtils.class);

	private static class DiffFileRecordDescriptor {
		String commandLine;
		ArrayList<String> content;

		public DiffFileRecordDescriptor(String commandLine,
				ArrayList<String> content) {
			this.commandLine = commandLine;
			this.content = content;
		}
	}

	public static DiffFileRecordList readRecords(DiffFile diffFile,
			SourceOrigin sourceOrigin, SourceCache sourceCache,
			IProgressMonitor progressMonitor) {

		LinkedList<DiffFileRecordDescriptor> descriptors = new LinkedList<DiffFileRecordDescriptor>();
		List<String> lines = null;
		try {
			lines = FileUtils.readLines(diffFile);
		} catch (IOException e) {
			progressMonitor.beginTask(
					"Aborting " + DiffFile.class.getSimpleName() + " parsing",
					1);
			logger.error("Could not open doclog file", e);
			progressMonitor.done();
			return null;
		}

		String commandLine = null;
		ArrayList<String> content = null;
		String line;
		for (int i = 0, m = lines.size(); i < m; i++) {
			line = lines.get(i);
			if (line.equals("RESET"))
				break;

			String[] x = line.split(" ");
			if (x.length > 0
					&& (x[0].equals("diff") || x[0].equals("Files") || x[0]
							.equals("Binary"))) {
				// create record if new record is found
				if (commandLine != null) {
					descriptors.add(new DiffFileRecordDescriptor(commandLine,
							content));
					commandLine = null;
					content = null;
				}

				if (x[0].equals("diff")) {
					commandLine = line;
					content = new ArrayList<String>();
				}
			} else {
				content.add(line);
			}
			progressMonitor.worked(1);
		}

		// create record if EOF
		if (commandLine != null) {
			descriptors.add(new DiffFileRecordDescriptor(commandLine, content));
		}

		DiffFileRecordList diffFileRecords = new DiffFileRecordList(diffFile,
				sourceOrigin, sourceCache);

		progressMonitor.beginTask(
				"Parsing " + DiffFileRecord.class.getSimpleName() + "s",
				descriptors.size());
		for (DiffFileRecordDescriptor descriptor : descriptors) {
			diffFileRecords.createAndAddRecord(descriptor.commandLine,
					descriptor.content);
		}
		progressMonitor.done();

		return diffFileRecords;
	}
}
