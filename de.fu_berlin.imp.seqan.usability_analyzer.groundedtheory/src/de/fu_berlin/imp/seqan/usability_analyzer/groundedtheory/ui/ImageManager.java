package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.Activator;

/**
 * Handles references to all used images throughout this plug-in.
 */
public class ImageManager {

	/*
	 * overlays
	 */
	public static final ImageDescriptor OVERLAY_CODED = getImageDescriptor("icons/ovr16/coded.png"); //$NON-NLS-1$
	public static final ImageDescriptor OVERLAY_PARTIALLY_CODED = getImageDescriptor("icons/ovr16/partially_coded.png"); //$NON-NLS-1$
	public static final ImageDescriptor OVERLAY_MEMO = getImageDescriptor("icons/ovr16/memo.png"); //$NON-NLS-1$

	/*
	 * wizard banners
	 */
	public static final ImageDescriptor WIZBAN_CREATE_CODE = getImageDescriptor("icons/wizban/create_code_wiz.gif"); //$NON-NLS-1$
	public static final ImageDescriptor WIZBAN_ADD_CODE = getImageDescriptor("icons/wizban/add_code_wiz.gif"); //$NON-NLS-1$
	public static final ImageDescriptor WIZBAN_EDIT_CODE = getImageDescriptor("icons/wizban/edit_code_wiz.gif"); //$NON-NLS-1$
	public static final ImageDescriptor WIZBAN_CREATE_EPISODE = getImageDescriptor("icons/wizban/create_episode_wiz.gif"); //$NON-NLS-1$

	/*
	 * tool bar
	 */
	public static final Image ETOOL_CREATE_CODE = getImage("icons/etool16/create_code_tsk.png"); //$NON-NLS-1$
	public static final Image DTOOL_CREATE_CODE = getImage("icons/dtool16/create_code_tsk.png"); //$NON-NLS-1$
	public static final Image ETOOL_ADD_CODE = getImage("icons/etool16/add_code_tsk.png"); //$NON-NLS-1$
	public static final Image DTOOL_ADD_CODE = getImage("icons/dtool16/add_code_tsk.png"); //$NON-NLS-1$

	/*
	 * local tool bar
	 */
	//	public static final Image ELCL_CREATE_CODE = getImage("icons/elcl16/create_code_tsk.png"); //$NON-NLS-1$
	//	public static final Image DLCL_CREATE_CODE = getImage("icons/dlcl16/create_code_tsk.png"); //$NON-NLS-1$
	//	public static final Image ELCL_ADD_CODE = getImage("icons/elcl16/add_code_tsk.png"); //$NON-NLS-1$
	//	public static final Image DLCL_ADD_CODE = getImage("icons/dlcl16/add_code_tsk.png"); //$NON-NLS-1$

	/*
	 * objects
	 */
	public static final Image CODE = getImage("icons/obj16/code_obj.png"); //$NON-NLS-1$
	public static final Image CODE_MEMO = new DecorationOverlayIcon(CODE,
			OVERLAY_MEMO, IDecoration.TOP_RIGHT).createImage();

	public static final Image EPISODE = getImage("icons/obj16/episode_obj.png");
	public static final Image EPISODE_CODED = new DecorationOverlayIcon(
			EPISODE, OVERLAY_CODED, IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image EPISODE_MEMO = new DecorationOverlayIcon(EPISODE,
			OVERLAY_MEMO, IDecoration.TOP_RIGHT).createImage();
	public static final Image EPISODE_CODED_MEMO = new DecorationOverlayIcon(
			EPISODE_CODED, OVERLAY_MEMO, IDecoration.TOP_RIGHT).createImage();

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
