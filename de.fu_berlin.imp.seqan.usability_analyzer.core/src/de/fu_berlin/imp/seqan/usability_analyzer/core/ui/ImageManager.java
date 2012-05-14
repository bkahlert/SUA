package de.fu_berlin.imp.seqan.usability_analyzer.core.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.fu_berlin.imp.seqan.usability_analyzer.core.Activator;

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
	public static final Image ETOOL_STATISTIC = getImage("icons/etool16/worksession_start_tsk.png"); //$NON-NLS-1$
	public static final Image DTOOL_STATISTIC = getImage("icons/dtool16/worksession_start_tsk.png"); //$NON-NLS-1$

	/*
	 * local tool bar
	 */
	//	public static final Image ELCL_SPACER = getImage("icons/elcl16/spacer.png"); //$NON-NLS-1$
	//	public static final Image DLCL_SPACER = getImage("icons/dlcl16/spacer.png"); //$NON-NLS-1$

	/*
	 * objects
	 */
	//	public static final Image DIFFFILELIST = getImage("icons/obj16/difffilelist_obj.png"); //$NON-NLS-1$

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
