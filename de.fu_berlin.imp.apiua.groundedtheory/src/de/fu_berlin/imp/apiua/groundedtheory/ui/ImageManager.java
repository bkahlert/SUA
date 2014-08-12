package de.fu_berlin.imp.apiua.groundedtheory.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.fu_berlin.imp.apiua.groundedtheory.Activator;

/**
 * Handles references to all used images throughout this plug-in.
 */
public class ImageManager {

	/*
	 * overlays
	 */
	public static final ImageDescriptor OVERLAY_CODED = getImageDescriptor("icons/ovr16/coded.png"); //$NON-NLS-1$
	public static final Image OVERLAY_CODED_IMG = OVERLAY_CODED.createImage(); //$NON-NLS-1$
	public static final ImageDescriptor OVERLAY_PARTIALLY_CODED = getImageDescriptor("icons/ovr16/partially_coded.png"); //$NON-NLS-1$
	public static final Image OVERLAY_PARTIALLY_CODED_IMG = OVERLAY_PARTIALLY_CODED
			.createImage(); //$NON-NLS-1$
	public static final ImageDescriptor OVERLAY_DIMENSIONALIZED = getImageDescriptor("icons/ovr16/dimensionalized.png"); //$NON-NLS-1$
	public static final ImageDescriptor OVERLAY_INDIRECTLYDIMENSIONALIZED = getImageDescriptor("icons/ovr16/indirectly-dimensionalized.png"); //$NON-NLS-1$

	public static final Image OVERLAY_DIMENSIONALIZED_IMG = OVERLAY_DIMENSIONALIZED
			.createImage(); //$NON-NLS-1$
	public static final Image OVERLAY_INDIRECTLYDIMENSIONALIZED_IMG = OVERLAY_INDIRECTLYDIMENSIONALIZED
			.createImage(); //$NON-NLS-1$
	public static final ImageDescriptor OVERLAY_MEMO = getImageDescriptor("icons/ovr16/memo.png"); //$NON-NLS-1$
	public static final Image OVERLAY_MEMO_IMG = OVERLAY_MEMO.createImage(); //$NON-NLS-1$

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
	public static final Image ELCL_PIN = getImage("icons/elcl16/pin_tsk.png"); //$NON-NLS-1$
	public static final Image DLCL_PIN = getImage("icons/dlcl16/pin_tsk.png"); //$NON-NLS-1$
	public static final Image ELCL_SHOW_INSTANCES = getImage("icons/elcl16/show_instances_tsk.png"); //$NON-NLS-1$
	public static final Image DLCL_SHOW_INSTANCES = getImage("icons/dlcl16/show_instances_tsk.png"); //$NON-NLS-1$
	public static final Image ELCL_HIDE_INSTANCES = getImage("icons/elcl16/hide_instances_tsk.png"); //$NON-NLS-1$
	public static final Image DLCL_HIDE_INSTANCES = getImage("icons/dlcl16/hide_instances_tsk.png"); //$NON-NLS-1$

	/*
	 * objects
	 */
	public static final Image MEMO = OVERLAY_MEMO.createImage();
	public static final Image CODE = getImage("icons/obj16/code_obj.png");
	public static final Image CODE_DIMENSIONALIZED = new DecorationOverlayIcon(
			CODE, OVERLAY_DIMENSIONALIZED, IDecoration.TOP_RIGHT).createImage();
	public static final Image CODE_INDIRECTLYDIMENSIONALIZED = new DecorationOverlayIcon(
			CODE, OVERLAY_INDIRECTLYDIMENSIONALIZED, IDecoration.TOP_RIGHT)
			.createImage();
	public static final Image CODE_CODED = new DecorationOverlayIcon(CODE,
			OVERLAY_CODED, IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image CODE_DIMENSIONALIZED_CODED = new DecorationOverlayIcon(
			CODE_DIMENSIONALIZED, OVERLAY_CODED, IDecoration.BOTTOM_RIGHT)
			.createImage();
	public static final Image CODE_INDIRECTLYDIMENSIONALIZED_CODED = new DecorationOverlayIcon(
			CODE_INDIRECTLYDIMENSIONALIZED, OVERLAY_CODED,
			IDecoration.BOTTOM_RIGHT).createImage();

	public static final Image CODE_MEMO = new DecorationOverlayIcon(CODE,
			OVERLAY_MEMO, IDecoration.TOP_RIGHT).createImage();
	public static final Image CODE_DIMENSIONALIZED_MEMO = new DecorationOverlayIcon(
			CODE_DIMENSIONALIZED, OVERLAY_MEMO, IDecoration.TOP_RIGHT)
			.createImage();
	public static final Image CODE_INDIRECTLYDIMENSIONALIZED_MEMO = new DecorationOverlayIcon(
			CODE_INDIRECTLYDIMENSIONALIZED, OVERLAY_MEMO, IDecoration.TOP_RIGHT)
			.createImage();
	public static final Image CODE_CODED_MEMO = new DecorationOverlayIcon(
			CODE_CODED, OVERLAY_MEMO, IDecoration.TOP_RIGHT).createImage();
	public static final Image CODE_DIMENSIONALIZED_CODED_MEMO = new DecorationOverlayIcon(
			CODE_DIMENSIONALIZED_CODED, OVERLAY_MEMO, IDecoration.TOP_RIGHT)
			.createImage();
	public static final Image CODE_INDIRECTLYDIMENSIONALIZED_CODED_MEMO = new DecorationOverlayIcon(
			CODE_INDIRECTLYDIMENSIONALIZED_CODED, OVERLAY_MEMO,
			IDecoration.TOP_RIGHT).createImage();

	public static final Image EPISODE = getImage("icons/obj16/episode_obj.png");
	public static final Image EPISODE_CODED = new DecorationOverlayIcon(
			EPISODE, OVERLAY_CODED, IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image EPISODE_MEMO = new DecorationOverlayIcon(EPISODE,
			OVERLAY_MEMO, IDecoration.TOP_RIGHT).createImage();
	public static final Image EPISODE_CODED_MEMO = new DecorationOverlayIcon(
			EPISODE_CODED, OVERLAY_MEMO, IDecoration.TOP_RIGHT).createImage();

	public static Image applyCodedOverlay(Image image) {
		return new DecorationOverlayIcon(image, OVERLAY_CODED,
				IDecoration.BOTTOM_RIGHT).createImage();
	}

	public static Image applyMemoOverlay(Image image) {
		return new DecorationOverlayIcon(image, OVERLAY_MEMO,
				IDecoration.TOP_RIGHT).createImage();
	}

	public static Image applyCodedMemoOverlay(Image image) {
		return new DecorationOverlayIcon(image, new ImageDescriptor[] { null,
				OVERLAY_MEMO, null, OVERLAY_CODED }).createImage();
	}

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
