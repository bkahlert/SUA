package de.fu_berlin.imp.apiua.uri;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ImageManager {

	public static final ImageDescriptor WIZBAN_CREATE_URI = getImageDescriptor("icons/wizban/create_uri_wiz.gif"); //$NON-NLS-1$
	public static final ImageDescriptor WIZBAN_EDIT_URI = getImageDescriptor("icons/wizban/edit_uri_wiz.gif"); //$NON-NLS-1$

	private static final Image URI_BASE = getImage("icons/obj16/uri_obj.png");
	public static final Image URI = URI_BASE;
	public static final Image URI_CODED = new DecorationOverlayIcon(
			URI_BASE,
			new ImageDescriptor[] {
					null,
					null,
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image URI_MEMO = new DecorationOverlayIcon(
			URI_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					null, null, null }).createImage();
	public static final Image URI_CODED_MEMO = new DecorationOverlayIcon(
			URI_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image URI_PARTIALLY_CODED = new DecorationOverlayIcon(
			URI_BASE,
			new ImageDescriptor[] {
					null,
					null,
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();
	public static final Image URI_PARTIALLY_CODED_MEMO = new DecorationOverlayIcon(
			URI_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();

	/**
	 * Returns an image from the file at the given plug-in relative path.
	 * 
	 * @param path
	 * @return image; the returned image <b>MUST be disposed after usage</b> to
	 *         free up memory
	 */
	public static Image getImage(String path) {
		return new Image(Display.getDefault(), getImageDescriptor(path)
				.getImageData());
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
				path);
	}

}
