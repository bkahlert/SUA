package de.fu_berlin.imp.seqan.usability_analyzer.diff.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;

/**
 * Handles references to all used images throughout this plug-in.
 */
public class ImageManager {

	/*
	 * overlays
	 */
	//	public static final ImageDescriptor OVERLAY_CODED = getImageDescriptor("icons/ovr16/coded.png"); //$NON-NLS-1$

	/*
	 * wizard banners
	 */
	//	public static final ImageDescriptor WIZBAN_ADD_CODE = getImageDescriptor("icons/wizban/add_code_wiz.gif"); //$NON-NLS-1$

	/*
	 * tool bar
	 */
	//	public static final Image ETOOL_STATISTIC = getImage("icons/etool16/statistic_misc.png"); //$NON-NLS-1$
	//	public static final Image DTOOL_STATISTIC = getImage("icons/dtool16/statistic_misc.png"); //$NON-NLS-1$

	/*
	 * local tool bar
	 */
	//	public static final Image ELCL_SPACER = getImage("icons/elcl16/spacer.png"); //$NON-NLS-1$
	//	public static final Image DLCL_CREATE_CODE = getImage("icons/dlcl16/spacer.png"); //$NON-NLS-1$

	/*
	 * objects
	 */
	public static final Image DIFFFILELIST = getImage("icons/obj16/difffilelist_obj.png"); //$NON-NLS-1$

	public static final Image DIFFFILE = getImage("icons/obj16/difffile_obj.png"); //$NON-NLS-1$
	public static final Image DIFFFILE_CODED = new DecorationOverlayIcon(
			DIFFFILE,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image DIFFFILE_MEMO = new DecorationOverlayIcon(
			DIFFFILE,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
			IDecoration.TOP_RIGHT).createImage();
	public static final Image DIFFFILE_CODED_MEMO = new DecorationOverlayIcon(
			DIFFFILE_MEMO,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();

	public static final Image DIFFFILE_PARTIALLY_CODED = new DecorationOverlayIcon(
			DIFFFILE,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image DIFFFILE_PARTIALLY_CODED_MEMO = new DecorationOverlayIcon(
			DIFFFILE_MEMO,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();

	public static final Image DIFFFILERECORD = getImage("icons/obj16/difffilerecord_obj.png"); //$NON-NLS-1$
	public static final Image DIFFFILERECORD_CODED = new DecorationOverlayIcon(
			DIFFFILERECORD,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image DIFFFILERECORD_MEMO = new DecorationOverlayIcon(
			DIFFFILERECORD,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
			IDecoration.TOP_RIGHT).createImage();
	public static final Image DIFFFILERECORD_CODED_MEMO = new DecorationOverlayIcon(
			DIFFFILERECORD_MEMO,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();

	public static final Image DIFFFILERECORD_PARTIALLY_CODED = new DecorationOverlayIcon(
			DIFFFILERECORD,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image DIFFFILERECORD_PARTIALLY_CODED_MEMO = new DecorationOverlayIcon(
			DIFFFILERECORD_MEMO,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();

	public static final Image DIFFFILERECORDSEGMENT = getImage("icons/obj16/difffilerecordsegment_obj.png"); //$NON-NLS-1$
	public static final Image DIFFFILERECORDSEGMENT_CODED = new DecorationOverlayIcon(
			DIFFFILERECORDSEGMENT,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image DIFFFILERECORDSEGMENT_MEMO = new DecorationOverlayIcon(
			DIFFFILERECORDSEGMENT,
			de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
			IDecoration.TOP_RIGHT).createImage();
	public static final Image DIFFFILERECORDSEGMENT_CODED_MEMO = new DecorationOverlayIcon(
			DIFFFILERECORDSEGMENT_MEMO,
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
