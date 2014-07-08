package de.fu_berlin.imp.apiua.diff.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.fu_berlin.imp.apiua.diff.Activator;

/**
 * Handles references to all used images throughout this plug-in.
 */
public class ImageManager {

	/*
	 * views
	 */
	public static final Image COMPILEROUTPUT_MISC = getImage("icons/view16/compileroutput_misc.png"); //$NON-NLS-1$

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
	public static final Image DIFFS = getImage("icons/obj16/difffilelist_obj.png"); //$NON-NLS-1$

	/*
	 * DIFF
	 */
	public static final Image DIFF_BASE = getImage("icons/obj16/difffile_obj.png"); //$NON-NLS-1$
	public static final Image DIFF = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					null, null }).createImage();
	public static final Image DIFF_CODED = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFF_MEMO = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					null, null, null }).createImage();
	public static final Image DIFF_CODED_MEMO = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFF_PARTIALLY_CODED = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();
	public static final Image DIFF_PARTIALLY_CODED_MEMO = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();

	public static final Image DIFF_WORKING = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					null, null }).createImage();
	public static final Image DIFF_CODED_WORKING = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFF_MEMO_WORKING = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					null, null }).createImage();
	public static final Image DIFF_CODED_MEMO_WORKING = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFF_PARTIALLY_CODED_WORKING = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();
	public static final Image DIFF_PARTIALLY_CODED_MEMO_WORKING = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();

	public static final Image DIFF_NOTWORKING = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					null, null }).createImage();
	public static final Image DIFF_CODED_NOTWORKING = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFF_MEMO_NOTWORKING = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					null, null }).createImage();
	public static final Image DIFF_CODED_MEMO_NOTWORKING = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFF_PARTIALLY_CODED_NOTWORKING = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();
	public static final Image DIFF_PARTIALLY_CODED_MEMO_NOTWORKING = new DecorationOverlayIcon(
			DIFF_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();

	/*
	 * DIFF RECORD
	 */
	public static final Image DIFFRECORD_BASE = getImage("icons/obj16/difffilerecord_obj.png"); //$NON-NLS-1$
	public static final Image DIFFRECORD = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					null, null }).createImage();
	public static final Image DIFFRECORD_CODED = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFFRECORD_MEMO = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					null, null }).createImage();
	public static final Image DIFFRECORD_CODED_MEMO = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFFRECORD_PARTIALLY_CODED = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();
	public static final Image DIFFRECORD_PARTIALLY_CODED_MEMO = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();

	public static final Image DIFFRECORD_WORKING = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					null, null }).createImage();
	public static final Image DIFFRECORD_CODED_WORKING = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFFRECORD_MEMO_WORKING = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					null, null }).createImage();
	public static final Image DIFFRECORD_CODED_MEMO_WORKING = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFFRECORD_PARTIALLY_CODED_WORKING = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();
	public static final Image DIFFRECORD_PARTIALLY_CODED_MEMO_WORKING = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();

	public static final Image DIFFRECORD_NOTWORKING = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					null, null }).createImage();
	public static final Image DIFFRECORD_CODED_NOTWORKING = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFFRECORD_MEMO_NOTWORKING = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					null, null }).createImage();
	public static final Image DIFFRECORD_CODED_MEMO_NOTWORKING = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFFRECORD_PARTIALLY_CODED_NOTWORKING = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();
	public static final Image DIFFRECORD_PARTIALLY_CODED_MEMO_NOTWORKING = new DecorationOverlayIcon(
			DIFFRECORD_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();

	/*
	 * DIFF RECORD SEGMENT
	 */
	public static final Image DIFFRECORDSEGMENT_BASE = getImage("icons/obj16/difffilerecordsegment_obj.png"); //$NON-NLS-1$
	public static final Image DIFFRECORDSEGMENT = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					null, null }).createImage();
	public static final Image DIFFRECORDSEGMENT_CODED = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFFRECORDSEGMENT_MEMO = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					null, null }).createImage();
	public static final Image DIFFRECORDSEGMENT_CODED_MEMO = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFFRECORDSEGMENT_PARTIALLY_CODED = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();
	public static final Image DIFFRECORDSEGMENT_PARTIALLY_CODED_MEMO = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_DIRTY,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();

	public static final Image DIFFRECORDSEGMENT_WORKING = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					null, null }).createImage();
	public static final Image DIFFRECORDSEGMENT_CODED_WORKING = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFFRECORDSEGMENT_MEMO_WORKING = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					null, null }).createImage();
	public static final Image DIFFRECORDSEGMENT_CODED_MEMO_WORKING = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFFRECORDSEGMENT_PARTIALLY_CODED_WORKING = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();
	public static final Image DIFFRECORDSEGMENT_PARTIALLY_CODED_MEMO_WORKING = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_OK,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();

	public static final Image DIFFRECORDSEGMENT_NOTWORKING = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					null, null }).createImage();
	public static final Image DIFFRECORDSEGMENT_CODED_NOTWORKING = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFFRECORDSEGMENT_MEMO_NOTWORKING = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					null, null }).createImage();
	public static final Image DIFFRECORDSEGMENT_CODED_MEMO_NOTWORKING = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image DIFFRECORDSEGMENT_PARTIALLY_CODED_NOTWORKING = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					null,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();
	public static final Image DIFFRECORDSEGMENT_PARTIALLY_CODED_MEMO_NOTWORKING = new DecorationOverlayIcon(
			DIFFRECORDSEGMENT_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					de.fu_berlin.imp.apiua.core.ui.ImageManager.OVERLAY_ERROR,
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
