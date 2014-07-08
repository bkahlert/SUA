package de.fu_berlin.imp.apiua.groundedtheory.viewer;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.FontUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.viewer.jointjs.JointJSLabelProvider;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IImportanceService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.IImportanceService.Importance;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.LabelProvider;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.ui.GTLabelProvider;
import de.fu_berlin.imp.apiua.groundedtheory.ui.GTLabelProvider.CodeColors;

public class AxialCodingLabelProvider extends LabelProvider implements
		JointJSLabelProvider {

	private static final Logger LOGGER = Logger
			.getLogger(AxialCodingLabelProvider.class);

	private static final IImportanceService IMPORTANCE_SERVICE = (IImportanceService) PlatformUI
			.getWorkbench().getService(IImportanceService.class);
	private static final ILabelProviderService LABEL_PROVIDER_SERVICE = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	private CodeColors getCodeColors(URI uri) {
		if (LocatorService.INSTANCE.getType(uri) == ICode.class) {
			try {
				ICode code = LocatorService.INSTANCE.resolve(uri, ICode.class,
						null).get();
				return new CodeColors(code.getColor());
			} catch (Exception e) {
				LOGGER.error("Error getting color for " + uri, e);
			}
		}
		return null;
	}

	private int[] getAlpha(URI uri) {
		Importance importance = IMPORTANCE_SERVICE.getImportance(uri);

		int backgroundAlpha;
		int borderAlpha;
		switch (importance) {
		case HIGH:
			backgroundAlpha = GTLabelProvider.HIGH_BACKGROUND_ALPHA;
			borderAlpha = GTLabelProvider.HIGH_BORDER_ALPHA;
			break;
		case LOW:
			backgroundAlpha = GTLabelProvider.LOW_BACKGROUND_ALPHA;
			borderAlpha = GTLabelProvider.LOW_BORDER_ALPHA;
			break;
		default:
			backgroundAlpha = GTLabelProvider.DEFAULT_BACKGROUND_ALPHA;
			borderAlpha = GTLabelProvider.DEFAULT_BORDER_ALPHA;
			break;
		}
		return new int[] { backgroundAlpha, borderAlpha };
	}

	@Override
	public String getText(URI element) throws Exception {
		ILabelProvider lp = LABEL_PROVIDER_SERVICE.getLabelProvider(element);
		return lp.getText(element);
	}

	@Override
	public String getContent(Object element) {
		return "";
	}

	@Override
	public RGB getColor(Object element) {
		return RGB.BLACK;
	}

	@Override
	public RGB getBackgroundColor(Object element) {
		if (element instanceof URI) {
			URI uri = (URI) element;
			if (LocatorService.INSTANCE.getType(uri) == ICode.class) {
				RGB rgb = this.getCodeColors(uri).getBackgroundRGB();
				rgb.setAlpha(this.getAlpha(uri)[0]);
				return rgb;
			}
		}
		return null;
	}

	@Override
	public RGB getBorderColor(Object element) {
		if (element instanceof URI) {
			URI uri = (URI) element;
			if (LocatorService.INSTANCE.getType(uri) == ICode.class) {
				RGB rgb = this.getCodeColors((URI) element).getBorderRGB();
				rgb.setAlpha(this.getAlpha(uri)[1]);
				return rgb;
			}
		}
		return null;
	}

	@Override
	public Point getSize(Object element) {
		String text = this.getText(element);
		Point size = null;
		try {
			size = FontUtils.calcSize(text).get();
			size.x += 60;
			size.y += 15;
		} catch (Exception e) {
			LOGGER.error("Error calculing size for " + element);
		}
		return size != null ? size : new Point(180, 50);
	}

}
