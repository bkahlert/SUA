package de.fu_berlin.imp.apiua.survey;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ImageManager {

	private static final Image CDDOCUMENT_BASE = getImage("icons/obj16/cddocument_obj.png");
	public static final Image CDDOCUMENT = CDDOCUMENT_BASE;
	public static final Image CDDOCUMENT_CODED = new DecorationOverlayIcon(
			CDDOCUMENT_BASE,
			new ImageDescriptor[] {
					null,
					null,
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image CDDOCUMENT_MEMO = new DecorationOverlayIcon(
			CDDOCUMENT_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					null, null, null }).createImage();
	public static final Image CDDOCUMENT_CODED_MEMO = new DecorationOverlayIcon(
			CDDOCUMENT_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image CDDOCUMENT_PARTIALLY_CODED = new DecorationOverlayIcon(
			CDDOCUMENT_BASE,
			new ImageDescriptor[] {
					null,
					null,
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();
	public static final Image CDDOCUMENT_PARTIALLY_CODED_MEMO = new DecorationOverlayIcon(
			CDDOCUMENT_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();

	private static final Image CDDOCUMENTFIELD_BASE = getImage("icons/obj16/cddocumentfield_obj.png");
	public static final Image CDDOCUMENTFIELD = CDDOCUMENTFIELD_BASE;
	public static final Image CDDOCUMENTFIELD_CODED = new DecorationOverlayIcon(
			CDDOCUMENTFIELD_BASE,
			new ImageDescriptor[] {
					null,
					null,
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image CDDOCUMENTFIELD_MEMO = new DecorationOverlayIcon(
			CDDOCUMENTFIELD_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					null, null, null }).createImage();
	public static final Image CDDOCUMENTFIELD_CODED_MEMO = new DecorationOverlayIcon(
			CDDOCUMENTFIELD_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_CODED,
					null }).createImage();
	public static final Image CDDOCUMENTFIELD_PARTIALLY_CODED = new DecorationOverlayIcon(
			CDDOCUMENTFIELD_BASE,
			new ImageDescriptor[] {
					null,
					null,
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();
	public static final Image CDDOCUMENTFIELD_PARTIALLY_CODED_MEMO = new DecorationOverlayIcon(
			CDDOCUMENTFIELD_BASE,
			new ImageDescriptor[] {
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_MEMO,
					null,
					de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager.OVERLAY_PARTIALLY_CODED,
					null }).createImage();

	public static Image getImage(String path) {
		return new Image(Display.getDefault(), getImageDescriptor(path)
				.getImageData());
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
				path);
	}

}
