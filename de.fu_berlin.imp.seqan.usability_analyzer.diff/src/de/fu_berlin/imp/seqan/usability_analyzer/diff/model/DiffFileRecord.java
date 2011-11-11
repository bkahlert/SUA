package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.diff.DiffMeta;

public class DiffFileRecord implements HasDateRange {

	private DiffFile diffFile;

	private String commandLine;
	private DiffMeta meta;
	private String content;

	public DiffFileRecord(DiffFile diffFile, String commandLine,
			List<String> lines) {
		this.diffFile = diffFile;
		this.commandLine = commandLine;

		this.meta = new DiffMeta(lines.get(0), lines.get(1));

		String[] content = Arrays.copyOfRange(lines.toArray(new String[0]), 2,
				lines.size());
		this.content = StringUtils.join(content, "\n");
	}

	public DiffFile getDiffFile() {
		return diffFile;
	}

	public String getCommandLine() {
		return commandLine;
	}

	public String getFilename() {
		return this.meta.getToFileName();
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.getFilename();
	}

	@Override
	public DateRange getDateRange() {
		return this.meta.getDateRange();
	}
}
