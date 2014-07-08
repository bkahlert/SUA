package de.fu_berlin.imp.apiua.core.model.data.impl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.iterators.ReverseListIterator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.IData;
import de.fu_berlin.imp.apiua.core.model.data.IDataContainer;

public class StringData implements IData {

	private IBaseDataContainer baseDataContainer;
	private IDataContainer dataContainer;
	private final String name;
	private final String content;

	public StringData(IBaseDataContainer baseDataContainer,
			IDataContainer parentDataContainer, String name, String content) {
		this(name, content);

		Assert.isNotNull(baseDataContainer);
		Assert.isNotNull(parentDataContainer);

		IDataContainer tmp = parentDataContainer;
		while (tmp.getParentDataContainer() != null) {
			tmp = tmp.getParentDataContainer();
		}
		Assert.isTrue(tmp == baseDataContainer);

		this.baseDataContainer = baseDataContainer;
		this.dataContainer = parentDataContainer;
	}

	public StringData(String name, String content) {
		this.name = name;
		this.content = content;
	}

	@Override
	public IBaseDataContainer getBaseDataContainer() {
		return this.baseDataContainer;
	}

	@Override
	public IDataContainer getParentDataContainer() {
		return this.dataContainer;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String read() {
		return this.content;
	}

	@Override
	public String read(long from, long to) {
		return this.content.substring((int) from, (int) to);
	}

	@Override
	public String readFirstLine() {
		return this.content.split("\n", 1)[0];
	}

	@Override
	public String readLastLines(int numLines) {
		String[] lines = this.content.split("\n");
		Object[] lastLines = ArrayUtils.subarray(lines, lines.length - 1
				- numLines, lines.length - 1);
		return StringUtils.join(lastLines, "\n");
	}

	@Override
	public Iterator<String> iterator() {
		return new Iterator<String>() {

			private String[] lines = null;
			private int i = 0;

			{
				this.lines = StringData.this.content.split("\n");
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String next() {
				String rt = this.lines[this.i];
				this.i++;
				return rt;
			}

			@Override
			public boolean hasNext() {
				return this.lines.length >= this.i + 1;
			}
		};
	}

	@Override
	public long getLength() {
		return this.content.length();
	}

	// TODO
	@Override
	public File getStaticFile() throws IOException {
		List<String> path = new LinkedList<String>();
		path.add(this.getName());
		IDataContainer container = this.getParentDataContainer();
		while (container != null) {
			path.add(container.getName());
			container = container.getParentDataContainer();
		}
		// remove base
		path.remove(path.size() - 1);

		String scope = path.remove(path.size() - 1);
		String name = StringUtils.join(new ReverseListIterator(path),
				File.separator);
		return this.getBaseDataContainer().getStaticFile(scope, name);
	}

	@Override
	public File getFile() throws IOException {
		return this.getStaticFile();
	}

	@Override
	public String toString() {
		return this.content;
	}
}
