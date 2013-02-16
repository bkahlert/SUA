package de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;

/**
 * Handles references to all used images throughout this plug-in.
 */
public class ImageManager {

	/*
	 * images
	 */
	public static final Image ARROW_TOP_DETAIL_OVERLAY = getImage("images/detail-overlays/arrow-top.png"); //$NON-NLS-1$
	public static final Image ARROW_TOP_DOWN_DETAIL_OVERLAY = getImage("images/detail-overlays/arrow-top-down.png"); //$NON-NLS-1$
	public static final Image CREATE_DETAIL_OVERLAY = getImage("images/detail-overlays/create.png"); //$NON-NLS-1$
	public static final Image CLOSE_DETAIL_OVERLAY = getImage("images/detail-overlays/close.png"); //$NON-NLS-1$

	public static final Image SCROLL_UP = getImage("images/scroll-up.png"); //$NON-NLS-1$
	public static final Image SCROLL_DOWN = getImage("images/scroll-down.png"); //$NON-NLS-1$
	public static final Image SCROLL = getImage("images/scroll.png"); //$NON-NLS-1$

	public static final Image CREATED = getImage("images/created-big.png"); //$NON-NLS-1$
	public static final Image CLOSE = getImage("images/close.png"); //$NON-NLS-1$

	/*
	 * objects
	 */
	public static final Image DOCLOGFILELIST = getImage("icons/obj16/doclogfilelist_obj.png"); //$NON-NLS-1$

	public static final Image DOCLOGFILE = getImage("icons/obj16/doclogfile_obj.png"); //$NON-NLS-1$
	public static final Image DOCLOGFILE_CODED = new DecorationOverlayIcon(
			DOCLOGFILE,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image DOCLOGFILE_MEMO = new DecorationOverlayIcon(
			DOCLOGFILE,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
			IDecoration.TOP_RIGHT).createImage();
	public static final Image DOCLOGFILE_CODED_MEMO = new DecorationOverlayIcon(
			DOCLOGFILE_MEMO,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();

	public static final Image DOCLOGRECORD = getImage("icons/obj16/doclogrecord_obj.png"); //$NON-NLS-1$
	public static final Image DOCLOGRECORD_CODED = new DecorationOverlayIcon(
			DOCLOGRECORD,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image DOCLOGRECORD_MEMO = new DecorationOverlayIcon(
			DOCLOGRECORD,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
			IDecoration.TOP_RIGHT).createImage();
	public static final Image DOCLOGRECORD_CODED_MEMO = new DecorationOverlayIcon(
			DOCLOGRECORD_MEMO,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();

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
