package de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;

/**
 * Instances of this class provide detailed information about instances of a
 * given class.
 * 
 * @author bkahlert
 * 
 * @param <DataType>
 *            The type this provider can provide detailed information of.
 */
public interface ITimelineEventDetailProvider<DataType> {
	public static interface IDetailEntry extends Map.Entry<String, String> {

	}

	public static class DetailEntry implements IDetailEntry {
		private final String key;
		private String value;

		public DetailEntry(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String setValue(String value) {
			String old = this.value;
			this.value = value;
			return old;
		}
	}

	public Class<DataType> getType();

	public List<IllustratedText> getMetaInformation(DataType data);

	public List<Map.Entry<String, String>> getDetailInformation(DataType data);

	public void fillCustomComposite(Composite parent, DataType doclogRecord,
			ITimeline timeline);

	public Color getBackground(DataType data, ITimeline timeline);
}
