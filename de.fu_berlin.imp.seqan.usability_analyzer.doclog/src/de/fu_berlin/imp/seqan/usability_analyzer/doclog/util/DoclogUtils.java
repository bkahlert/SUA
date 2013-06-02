package de.fu_berlin.imp.seqan.usability_analyzer.doclog.util;

import java.util.Iterator;

public class DoclogUtils {

	public static Iterable<String> getPossibleFieldNames(
			final String typingParam) {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					private int processed = 0;
					private String nextFieldName = null;

					{
						this.calcNextFieldName();
					}

					private void calcNextFieldName() {
						int nextHyphen = typingParam.indexOf("-",
								this.processed);
						if (nextHyphen < 0) {
							this.nextFieldName = null;
							this.processed = typingParam.length();
						} else {
							this.nextFieldName = typingParam.substring(0,
									nextHyphen);
							this.processed = nextHyphen + 1;
						}
					}

					@Override
					public void remove() {
					}

					@Override
					public String next() {
						String rt = this.nextFieldName;
						this.calcNextFieldName();
						return rt;
					}

					@Override
					public boolean hasNext() {
						return this.nextFieldName != null;
					}

				};
			}
		};
	}

	public static String getFieldContent(String fieldName, String typingParam) {
		return typingParam.substring(fieldName.length() + 1);
	}
}
