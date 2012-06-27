package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui;

import org.eclipse.jface.resource.ImageDescriptor;
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

	/*
	 * wizard banners
	 */
	public static final ImageDescriptor WIZBAN_CREATE_CODE = getImageDescriptor("icons/wizban/create_code_wiz.gif"); //$NON-NLS-1$
	public static final ImageDescriptor WIZBAN_ADD_CODE = getImageDescriptor("icons/wizban/add_code_wiz.gif"); //$NON-NLS-1$
	public static final ImageDescriptor WIZBAN_EDIT_CODE = getImageDescriptor("icons/wizban/edit_code_wiz.gif"); //$NON-NLS-1$

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

	//	public static final Image ICON_BUDDY = getImage("icons/obj16/buddy_obj.png"); //$NON-NLS-1$
	//	public static final Image ICON_BUDDY_OFFLINE = getImage("icons/obj16/buddy_offline_obj.png"); //$NON-NLS-1$
	// public static final Image ICON_BUDDY_AWAY = new DecorationOverlayIcon(
	// ICON_BUDDY, OVERLAY_AWAY, IDecoration.TOP_RIGHT).createImage();
	//	public static final Image ICON_BUDDY_SAROS = getImage("icons/obj16/buddy_saros_obj.png"); //$NON-NLS-1$
	//	public static Image ICON_UPNP = getImage("icons/obj16/upnp_obj.png"); //$NON-NLS-1$
	//
	// public static final Image ICON_BUDDY_SAROS_FOLLOWMODE = new
	// DecorationOverlayIcon(
	// ICON_BUDDY_SAROS, OVERLAY_FOLLOWMODE, IDecoration.TOP_LEFT)
	// .createImage();
	// public static final Image ICON_BUDDY_SAROS_FOLLOWMODE_READONLY = new
	// DecorationOverlayIcon(
	// ICON_BUDDY_SAROS_FOLLOWMODE, OVERLAY_READONLY,
	// IDecoration.BOTTOM_RIGHT).createImage();
	// public static final Image ICON_BUDDY_SAROS_FOLLOWMODE_READONLY_AWAY = new
	// DecorationOverlayIcon(
	// ICON_BUDDY_SAROS_FOLLOWMODE_READONLY, OVERLAY_AWAY,
	// IDecoration.TOP_RIGHT).createImage();
	// public static final Image ICON_BUDDY_SAROS_FOLLOWMODE_AWAY = new
	// DecorationOverlayIcon(
	// ICON_BUDDY_SAROS_FOLLOWMODE, OVERLAY_AWAY, IDecoration.TOP_RIGHT)
	// .createImage();
	//
	// public static final Image ICON_BUDDY_SAROS_READONLY = new
	// DecorationOverlayIcon(
	// ICON_BUDDY_SAROS, OVERLAY_READONLY, IDecoration.BOTTOM_RIGHT)
	// .createImage();
	// public static final Image ICON_BUDDY_SAROS_READONLY_AWAY = new
	// DecorationOverlayIcon(
	// ICON_BUDDY_SAROS_READONLY, OVERLAY_AWAY, IDecoration.TOP_RIGHT)
	// .createImage();
	//
	// public static final Image ICON_BUDDY_SAROS_AWAY = new
	// DecorationOverlayIcon(
	// ICON_BUDDY_SAROS, OVERLAY_AWAY, IDecoration.TOP_RIGHT)
	// .createImage();

	/*
	 * Getting Started
	 */
	//	public static final ImageDescriptor WIZBAN_GETTING_STARTED_STEP0 = getImageDescriptor("icons/wizban/getting_started_step0_wiz.gif"); //$NON-NLS-1$
	//	public static final ImageDescriptor WIZBAN_GETTING_STARTED_STEP1 = getImageDescriptor("icons/wizban/getting_started_step1_wiz.gif"); //$NON-NLS-1$
	//	public static final ImageDescriptor WIZBAN_GETTING_STARTED_STEP2 = getImageDescriptor("icons/wizban/getting_started_step2_wiz.gif"); //$NON-NLS-1$
	//	public static final ImageDescriptor WIZBAN_GETTING_STARTED_STEP3 = getImageDescriptor("icons/wizban/getting_started_step3_wiz.gif"); //$NON-NLS-1$
	//	public static final ImageDescriptor WIZBAN_GETTING_STARTED_STEP4 = getImageDescriptor("icons/wizban/getting_started_step4_wiz.gif"); //$NON-NLS-1$
	//
	//	public static final ImageDescriptor IMAGE_GETTING_STARTED_STEP0 = getImageDescriptor("assets/images/getting_started/step0.png"); //$NON-NLS-1$
	//	public static final ImageDescriptor IMAGE_GETTING_STARTED_STEP1 = getImageDescriptor("assets/images/getting_started/step1.png"); //$NON-NLS-1$
	//	public static final ImageDescriptor IMAGE_GETTING_STARTED_STEP2 = getImageDescriptor("assets/images/getting_started/step2.png"); //$NON-NLS-1$
	//	public static final ImageDescriptor IMAGE_GETTING_STARTED_STEP3 = getImageDescriptor("assets/images/getting_started/step3.png"); //$NON-NLS-1$
	//	public static final ImageDescriptor IMAGE_GETTING_STARTED_STEP4_CONFIG = getImageDescriptor("assets/images/getting_started/step4_config.png"); //$NON-NLS-1$
	//	public static final ImageDescriptor IMAGE_GETTING_STARTED_STEP4_NOCONFIG = getImageDescriptor("assets/images/getting_started/step4_noconfig.png"); //$NON-NLS-1$

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
