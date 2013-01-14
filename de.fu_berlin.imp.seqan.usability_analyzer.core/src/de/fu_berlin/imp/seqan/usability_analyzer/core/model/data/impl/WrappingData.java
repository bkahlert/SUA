package de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataContainer;

/**
 * This class can be used to extend an {@link IData} instance and add
 * functionality to it without having to know the {@link IData}'s
 * implementation.
 * 
 * @author bkahlert
 * 
 */
public abstract class WrappingData implements IData {

	private IData data;

	public WrappingData(IData wrappedData) {
		this.data = wrappedData;
	}

	protected IData getData() {
		return this.data;
	}

	@Override
	public Iterator<String> iterator() {
		return this.data.iterator();
	}

	@Override
	public IBaseDataContainer getBaseDataContainer() {
		return this.data.getBaseDataContainer();
	}

	@Override
	public IDataContainer getParentDataContainer() {
		return this.data.getParentDataContainer();
	}

	@Override
	public String getName() {
		return this.data.getName();
	}

	@Override
	public String read() {
		return this.data.read();
	}

	@Override
	public String read(long from, long to) {
		return this.data.read(from, to);
	}

	@Override
	public String readFirstLine() {
		return this.data.readFirstLine();
	}

	@Override
	public String readLastLines(int numLines) {
		return this.data.readLastLines(numLines);
	}

	@Override
	public long getLength() {
		return this.data.getLength();
	}

	@Override
	public File getStaticFile() throws IOException {
		return this.data.getStaticFile();
	}

}
