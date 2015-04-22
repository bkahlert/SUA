package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.core.services.IImportanceService;
import de.fu_berlin.imp.apiua.core.services.IImportanceService.Importance;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.storage.ICodeStore;
import de.fu_berlin.imp.apiua.groundedtheory.ui.GTLabelProvider;

public class CodeStoreLatexCommandCreator {
	private static String COLOR_NAME = "codecolor";
	private static String COLOR_BORDER_ALPHA = "codeborderalpha";
	private static String COLOR_BACKGROUND_ALPHA = "codebackgroundalpha";

	public static String createMappingCommand(String commandName,
			Map<String, String> mappings, String defaultExpr) {
		StringBuilder sb = new StringBuilder();
		sb.append("\\makeatletter\n");
		sb.append("\\newcommand{\\" + commandName + "}[1]{%\n");
		int i = 0;
		for (Entry<String, String> mapping : mappings.entrySet()) {
			if (i > 0) {
				sb.append("\\else");
			}
			sb.append("\\ifnum\\pdf@strcmp{#1}{" + mapping.getKey() + "}=0 "
					+ mapping.getValue() + "%\n");
			i++;
		}
		sb.append("\\else " + defaultExpr + "%\n");
		sb.append(StringUtils.repeat("\\fi", mappings.size()));
		sb.append("}\n");
		sb.append("\\makeatother");
		return sb.toString();
	}

	public static String createNamesCommand(ICodeStore codeStore) {
		Map<String, String> mappings = new HashMap<>();
		for (ICode code : codeStore.getCodeHierarchyView().getCodes()) {
			mappings.put(code.getUri().toString(), code.getCaption());
		}
		return createMappingCommand("codename", mappings, "UNKNOWN CODE");
	}

	public static String createColorsCommand(ICodeStore codeStore) {
		Map<String, String> mappings = new HashMap<>();
		for (ICode code : codeStore.getCodeHierarchyView().getCodes()) {
			mappings.put(code.getUri().toString(), "\\definecolor{"
					+ COLOR_NAME + "}{HTML}{"
					+ code.getColor().toHexString().substring(1, 7) + "}");
		}
		return createMappingCommand("codecolor", mappings, "\\definecolor{"
				+ COLOR_NAME + "}{HTML}{999999}");
	}

	public static String createBorderAlphasCommand(ICodeStore codeStore) {
		int highAlpha = (int) Math
				.round((double) GTLabelProvider.HIGH_BORDER_ALPHA / 255.0 * 100.0);
		int lowAlpha = (int) Math
				.round((double) GTLabelProvider.LOW_BORDER_ALPHA / 255.0 * 100.0);
		int defaultAlpha = (int) Math
				.round((double) GTLabelProvider.DEFAULT_BORDER_ALPHA / 255.0 * 100.0);

		IImportanceService importanceService = (IImportanceService) PlatformUI
				.getWorkbench().getService(IImportanceService.class);
		Map<String, String> mappings = new HashMap<>();
		for (ICode code : codeStore.getCodeHierarchyView().getCodes()) {
			Importance importance = importanceService.getImportance(code
					.getUri());
			int alpha;
			switch (importance) {
			case HIGH:
				alpha = highAlpha;
				break;
			case LOW:
				alpha = lowAlpha;
				break;
			default:
				alpha = defaultAlpha;
				break;
			}
			mappings.put(code.getUri().toString(), "\\gdef\\"
					+ COLOR_BORDER_ALPHA + "value{" + alpha + "}");
		}
		return createMappingCommand(COLOR_BORDER_ALPHA, mappings, "\\gdef\\"
				+ COLOR_BORDER_ALPHA + "value{" + defaultAlpha + "}");
	}

	public static String createBackgroundAlphasCommand(ICodeStore codeStore) {
		int highAlpha = (int) Math
				.round((double) GTLabelProvider.HIGH_BACKGROUND_ALPHA / 255.0 * 100.0);
		int lowAlpha = (int) Math
				.round((double) GTLabelProvider.LOW_BACKGROUND_ALPHA / 255.0 * 100.0);
		int defaultAlpha = (int) Math
				.round((double) GTLabelProvider.DEFAULT_BACKGROUND_ALPHA / 255.0 * 100.0);

		IImportanceService importanceService = (IImportanceService) PlatformUI
				.getWorkbench().getService(IImportanceService.class);
		Map<String, String> mappings = new HashMap<>();
		for (ICode code : codeStore.getCodeHierarchyView().getCodes()) {
			Importance importance = importanceService.getImportance(code
					.getUri());
			int alpha;
			switch (importance) {
			case HIGH:
				alpha = highAlpha;
				break;
			case LOW:
				alpha = lowAlpha;
				break;
			default:
				alpha = defaultAlpha;
				break;
			}
			mappings.put(code.getUri().toString(), "\\gdef\\"
					+ COLOR_BACKGROUND_ALPHA + "value{" + alpha + "}");
		}
		return createMappingCommand(COLOR_BACKGROUND_ALPHA, mappings,
				"\\gdef\\" + COLOR_BACKGROUND_ALPHA + "value{" + defaultAlpha
						+ "}");
	}

	public static String createAllCommands(ICodeStore codeStore) {
		StringBuilder sb = new StringBuilder();
		sb.append(createNamesCommand(codeStore));
		sb.append("\n\n");
		sb.append(createColorsCommand(codeStore));
		sb.append("\n\n");
		sb.append(createBorderAlphasCommand(codeStore));
		sb.append("\n\n");
		sb.append(createBackgroundAlphasCommand(codeStore));
		return sb.toString();
	}
}
