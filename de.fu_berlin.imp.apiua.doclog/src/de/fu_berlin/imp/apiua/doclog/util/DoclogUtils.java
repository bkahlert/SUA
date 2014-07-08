package de.fu_berlin.imp.apiua.doclog.util;

import java.util.Iterator;

import com.bkahlert.nebula.screenshots.webpage.IFormContainingWebpage.IFieldFill;
import com.bkahlert.nebula.widgets.browser.extended.ISelector;

public class DoclogUtils {

	private static class FieldFill implements IFieldFill {
		private ISelector selector;
		private String fieldValue;

		public FieldFill(String fieldIdOrName, String fieldValue) {
			this.selector = new ISelector.FieldSelector(fieldIdOrName);
			this.fieldValue = fieldValue;
		}

		@Override
		public ISelector getFieldSelector() {
			return this.selector;
		}

		@Override
		public String getFieldValue() {
			return this.fieldValue;
		}
	}

	public static Iterable<IFieldFill> getPossibleFields(
			final String typingParam) {
		return new Iterable<IFieldFill>() {
			@Override
			public Iterator<IFieldFill> iterator() {
				return new Iterator<IFieldFill>() {
					private int processed = 0;
					private IFieldFill nextFieldFill = null;

					{
						this.calcNextFieldName();
					}

					private void calcNextFieldName() {
						int nextHyphen = typingParam.indexOf("-",
								this.processed);
						if (nextHyphen < 0) {
							this.nextFieldFill = null;
							this.processed = typingParam.length();
						} else {
							String fieldName = typingParam.substring(0,
									nextHyphen);
							String fieldValue = typingParam.substring(fieldName
									.length() + 1);
							this.nextFieldFill = new FieldFill(fieldName,
									fieldValue);
							this.processed = nextHyphen + 1;
						}
					}

					@Override
					public void remove() {
					}

					@Override
					public IFieldFill next() {
						IFieldFill next = this.nextFieldFill;
						this.calcNextFieldName();
						return next;
					}

					@Override
					public boolean hasNext() {
						return this.nextFieldFill != null;
					}

				};
			}
		};
	}

	public static String getFieldContent(String fieldName, String typingParam) {
		return typingParam.substring(fieldName.length() + 1);
	}
}
