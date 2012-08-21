package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class ViewerUtils {

	/**
	 * Returns all {@link Item}s who's {@link Item#getData()} object is of the
	 * given type.
	 * 
	 * @param items
	 * @param clazz
	 * @return
	 */
	public static List<Item> getItemWithDataType(Item[] items, Class<?> clazz) {
		if (items == null)
			return null;

		List<Item> itemsWithDataType = new ArrayList<Item>();
		for (Item item : getAllItems(items)) {
			if (clazz.isInstance(item.getData())) {
				itemsWithDataType.add(item);
			}
		}

		return itemsWithDataType;
	}

	/**
	 * Returns a list that does not only contain the {@link Item}s themselves
	 * but also their child, children's children, etc.
	 * 
	 * @param items
	 * @return
	 */
	public static List<Item> getAllItems(Item[] items) {
		List<Item> allItems = new ArrayList<Item>();
		for (Item item : items) {
			allItems.add(item);
			if (item instanceof TreeItem) {
				TreeItem treeItem = (TreeItem) item;
				allItems.addAll(listTreeItems(treeItem));
			}
		}
		return allItems;
	}

	/**
	 * Returns a list of all elements contained in the {@link TreeItem}.
	 * <p>
	 * Example:
	 * 
	 * <code>a</code> is root and has children <code>b</code> and <code>e</code>. <code>b</code> has the children <code>c</code> and <code>d</code>.
	 * 
	 * The resulting list contains the elements <code>b</code>, <code>c</code>,
	 * <code>d</code> and <code>e</code> whereas <code>a</code> was the
	 * argument.
	 * 
	 * @param treeItem
	 * @return
	 */
	public static List<TreeItem> listTreeItems(TreeItem treeItem) {
		List<TreeItem> treeItems = new ArrayList<TreeItem>();
		for (TreeItem child : treeItem.getItems()) {
			treeItems.add(child);
			treeItems.addAll(listTreeItems(child));
		}
		return treeItems;
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

	public static Rectangle getBounds(ViewerColumn column) {
		int index = getIndex(column);
		Control control = column.getViewer().getControl();

		int x = 0;
		int w;
		if (control instanceof Table) {
			for (int i = 0; i < index; i++) {
				x += getColumn((Table) control, i).getWidth();
			}
			w = getColumn((Table) control, index).getWidth();
		} else {
			for (int i = 0; i < index; i++) {
				x += getColumn((Tree) control, i).getWidth();
			}
			w = getColumn((Tree) control, index).getWidth();
			;
		}
		return new Rectangle(x, 0, w, control.getBounds().height);
	}

	public static int getIndex(ViewerColumn viewerColumn) {
		Control control = viewerColumn.getViewer().getControl();
		Item column = viewerColumn instanceof TableViewerColumn ? ((TableViewerColumn) viewerColumn)
				.getColumn() : ((TreeViewerColumn) viewerColumn).getColumn();
		Item[] columns = control instanceof Table ? ((Table) control)
				.getColumns() : ((Tree) control).getColumns();
		for (int i = 0, m = columns.length; i < m; i++) {
			if (columns[i] == column) {
				int[] order = control instanceof Table ? ((Table) control)
						.getColumnOrder() : ((Tree) control).getColumnOrder();
				for (int j = 0, n = order.length; j < n; j++) {
					if (order[j] == i)
						return j;
				}
			}
		}
		return -1;
	}

	private static Item getColumn(Control control, int index) {
		int[] order = control instanceof Table ? ((Table) control)
				.getColumnOrder() : ((Tree) control).getColumnOrder();
		for (int j = 0, n = order.length; j < n; j++) {
			if (order[j] == index) {
				Item[] columns = control instanceof Table ? ((Table) control)
						.getColumns() : ((Tree) control).getColumns();
				return columns[j];
			}
		}
		return null;
	}

	public static TableColumn getColumn(Table table, int index) {
		return (TableColumn) getColumn((Control) table, index);
	}

	public static TreeColumn getColumn(Tree tree, int index) {
		return (TreeColumn) getColumn((Control) tree, index);
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
