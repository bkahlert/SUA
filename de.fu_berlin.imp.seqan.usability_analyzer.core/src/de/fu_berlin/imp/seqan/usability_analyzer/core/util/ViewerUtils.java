package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Item;

public class ViewerUtils {

	/**
	 * Returns all {@link Item}s who's {@link Item#getData()} object is of the
	 * given type.
	 * 
	 * @param items
	 * @param clazz
	 * @return
	 */
	public static <T extends Item> List<T> getItemWithDataType(T[] items,
			Class<?> clazz) {
		if (items == null)
			return null;

		List<T> itemsWithDataType = new ArrayList<T>();
		for (T item : items) {
			if (clazz.isInstance(item.getData())) {
				itemsWithDataType.add(item);
			}
		}

		return itemsWithDataType;
	}

	/**
	 * Merges an array of {@link TreePath}s to one {@link TreePath}.
	 * 
	 * Example: {@link TreePath}s
	 * 
	 * <pre>
	 * A<br/>
	 * | -B
	 * </pre>
	 * 
	 * and
	 * 
	 * <pre>
	 * C<br/>
	 * | -D
	 * </pre>
	 * 
	 * become
	 * 
	 * <pre>
	 * A<br/>
	 * | -B<br/>
	 *    | -C<br/>
	 *       | -D
	 * </pre>
	 * 
	 * @param treePaths
	 * @return
	 */
	public static TreePath merge(TreePath... treePaths) {
		ArrayList<Object> segments = new ArrayList<Object>();
		for (TreePath treePath : treePaths) {
			for (int i = 0; i < treePath.getSegmentCount(); i++) {
				segments.add(treePath.getSegment(i));
			}
		}
		return new TreePath(segments.toArray());
	}

	public static void refresh(final Viewer viewer) {
		if (viewer != null) {
			ExecutorUtil.syncExec(new Runnable() {
				@Override
				public void run() {
					viewer.refresh();
				}
			});

		}
	}

}
