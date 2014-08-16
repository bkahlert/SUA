package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.bkahlert.nebula.utils.DecorationOverlayIcon;
import com.bkahlert.nebula.utils.DecorationOverlayIcon.ImageOverlay;
import com.bkahlert.nebula.utils.DecorationOverlayIcon.ImageOverlay.Quadrant;
import com.bkahlert.nebula.utils.DecorationOverlayIcon.ImageOverlayImpl;
import com.bkahlert.nebula.utils.Pair;

import de.fu_berlin.imp.apiua.groundedtheory.Activator;

/**
 * Handles references to all used images throughout this plug-in.
 */
public class ImageManager {

	/*
	 * overlays
	 */
	public static final ImageOverlay OVERLAY_CODED = getImageDescriptor(
			"icons/ovr16/coded.png", Quadrant.BottomRight); //$NON-NLS-1$
	public static final ImageOverlay OVERLAY_PARTIALLY_CODED = getImageDescriptor(
			"icons/ovr16/partially_coded.png", Quadrant.BottomRight); //$NON-NLS-1$
	public static final ImageOverlay OVERLAY_DIMENSIONALIZED = getImageDescriptor(
			"icons/ovr16/dimensionalized.png", Quadrant.TopLeft); //$NON-NLS-1$
	public static final ImageOverlay OVERLAY_HAS_DIMENSION_VALUE = getImageDescriptor(
			"icons/ovr16/has_dimension_value.png", Quadrant.TopLeft); //$NON-NLS-1$
	public static final ImageOverlay OVERLAY_HAS_PROPERTIES = getImageDescriptor(
			"icons/ovr16/has_properties.png", Quadrant.BottomLeft); //$NON-NLS-1$
	public static final ImageOverlay OVERLAY_MEMO = getImageDescriptor(
			"icons/ovr16/memo.png", Quadrant.TopRight); //$NON-NLS-1$

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
	public static final Image MEMO = OVERLAY_MEMO.getImageDescriptor()
			.createImage();
	public static final Image CODE = getImage("icons/obj16/code_obj.png");

	public static final Image EPISODE = getImage("icons/obj16/episode_obj.png");

	private static Map<Pair<Image, List<ImageOverlay>>, Image> overlayed = new HashMap<Pair<Image, List<ImageOverlay>>, Image>();

	/**
	 * The returned image must *not* be disposed!
	 * 
	 * @param image
	 * @param overlays
	 * @return
	 */
	public static Image getImage(Image image, List<ImageOverlay> overlays) {
		Assert.isNotNull(image);
		Assert.isNotNull(overlays);
		Pair<Image, List<ImageOverlay>> key = new Pair<Image, List<ImageOverlay>>(
				image, overlays);
		if (overlayed.containsKey(key)) {
			return overlayed.get(key);
		}
		Image icon = new DecorationOverlayIcon(image.getImageData(),
				overlays.toArray(new ImageOverlay[0])).createImage();
		overlayed.put(key, icon);
		return icon;
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

	public static ImageOverlay getImageDescriptor(final String path,
			final Quadrant quadrant) {
		return new ImageOverlayImpl(getImageDescriptor(path), quadrant);
	}

}
