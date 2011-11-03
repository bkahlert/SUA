package de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer;

import java.util.Comparator;

import org.apache.commons.collections.map.MultiKeyMap;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

/**
 * Generic {@link ViewerComparator} for use in conjunction with
 * {@link ColumnViewer}s like {@link TableViewer}s or {@link TreeViewer}s.
 * 
 * @author bkahlert
 */
public class GenericColumnViewerComparator extends ViewerComparator {

	/**
	 * This map uses 2 keys.
	 * <ol>
	 * <li>number of the column</li>
	 * <li>class the comparator is responsible for and may expect</li>
	 * </ol>
	 */
	private MultiKeyMap comparators = new MultiKeyMap();

	protected int propertyIndex;
	protected static final int DESCENDING = 1;
	protected int direction = DESCENDING;

	public GenericColumnViewerComparator() {
		this.propertyIndex = -1;
		direction = DESCENDING;
	}

	public int getDirection() {
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = 0;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int rc = 0;

		if (propertyIndex < 0)
			return rc;

		if (e1.getClass().equals(e2.getClass())
				&& this.comparators.containsKey(propertyIndex, e1.getClass())) {
			@SuppressWarnings("unchecked")
			Comparator<Object> customComparator = (Comparator<Object>) this.comparators
					.get(propertyIndex, e1.getClass());
			rc = customComparator.compare(e1, e2);
		} else {
			ColumnViewer columnViewer = (ColumnViewer) viewer;
			ColumnLabelProvider columnLabelProvider = (ColumnLabelProvider) columnViewer
					.getLabelProvider(propertyIndex);
			String str1 = columnLabelProvider.getText(e1);
			String str2 = columnLabelProvider.getText(e2);
			try {
				rc = new Integer(Integer.parseInt(str1)).compareTo(Integer
						.parseInt(str2));
			} catch (Exception e) {
				rc = str1.compareTo(str2);
			}
		}
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}

	public void setComparator(int colNumber, Comparator<Object> comparator,
			Class<?>[] comparatorClasses) {
		for (Class<?> comparatorClass : comparatorClasses) {
			comparators.put(colNumber, comparatorClass, comparator);
		}
	}
}