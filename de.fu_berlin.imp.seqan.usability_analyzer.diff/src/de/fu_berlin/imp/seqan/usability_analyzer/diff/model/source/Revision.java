package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.source;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataContainer;

public class Revision implements IRevision {

	private ISourceData[] sourceDatas;

	public Revision(ISourceData... sourceFiles) {
		this.sourceDatas = sourceFiles;
	}

	public Revision(IDataContainer subContainer) {
		List<ISourceData> sourceDatas = new ArrayList<ISourceData>();
		for (Iterator<IData> it = subContainer.listDatasDeep(); it.hasNext();) {
			SourceData sourceData = new SourceData(it.next());
			sourceDatas.add(sourceData);
		}
		this.sourceDatas = sourceDatas.toArray(new ISourceData[0]);
	}

	@Override
	public int size() {
		return this.sourceDatas.length;
	}

	@Override
	public Iterator<ISourceData> iterator() {
		return new Iterator<ISourceData>() {
			private int i = 0;

			@Override
			public void remove() {
				throw new NotImplementedException();
			}

			@Override
			public ISourceData next() {
				return Revision.this.sourceDatas.length > this.i++ ? Revision.this.sourceDatas[this.i - 1]
						: null;
			}

			@Override
			public boolean hasNext() {
				return Revision.this.sourceDatas.length > this.i;
			}
		};
	}
}
