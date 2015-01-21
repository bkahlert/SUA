package de.fu_berlin.imp.apiua.survey.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.apiua.core.model.HtmlManager;
import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.IData;
import de.fu_berlin.imp.apiua.core.model.data.IDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.impl.AggregatedBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.model.identifier.Token;
import de.fu_berlin.imp.apiua.survey.GroupDiscussionLocatorProvider;
import de.fu_berlin.imp.apiua.survey.model.cd.CDDocument;
import de.fu_berlin.imp.apiua.survey.model.cd.CDDocumentManager;
import de.fu_berlin.imp.apiua.survey.model.csv.CSVSurveyManager;
import de.fu_berlin.imp.apiua.survey.model.csv.CSVSurveyRecord;
import de.fu_berlin.imp.apiua.survey.model.groupdiscussion.GroupDiscussionDocument;

/**
 * Encapsulates multiple {@link IBaseDataContainer}s containing surveys.
 * <p>
 * Each survey is managed by a {@link XMLSurveyManager}.
 *
 * @author bkahlert
 *
 */
public class SurveyContainer extends AggregatedBaseDataContainer {

	private static final Logger LOGGER = Logger
			.getLogger(SurveyContainer.class);

	private List<? extends IBaseDataContainer> baseContainers;

	private List<CSVSurveyManager> cSVSurveyManagers;
	private List<CDDocumentManager> cdDocumentManagers;
	private List<HtmlManager> groupDiscussionManagers;

	public SurveyContainer(List<? extends IBaseDataContainer> baseContainers) {
		super(baseContainers);
		this.baseContainers = baseContainers;
	}

	public IData getSurveyData(IBaseDataContainer baseDataContainer) {
		IData surveyData = null;
		if (baseDataContainer.getInfo() == null) {
			return null;
		}

		Map<String, String> props = baseDataContainer.getInfo()
				.getUnknownProperties();
		for (String key : props.keySet()) {
			if (key.equals("surveyLocation")) {
				surveyData = this.getResource(props.get(key));
				break;
			}
		}
		return surveyData;
	}

	public void scan(SubMonitor subMonitor) {
		SubMonitor monitor = SubMonitor.convert(subMonitor,
				this.baseContainers.size() * 3);

		List<CSVSurveyManager> cSVSurveyManagers = new ArrayList<CSVSurveyManager>();
		for (IBaseDataContainer baseDataContainer : this.baseContainers) {
			IData surveyData = this.getSurveyData(baseDataContainer);
			if (surveyData == null) {
				LOGGER.error("Could not load survey data");
			} else {
				CSVSurveyManager cSVSurveyManager = new CSVSurveyManager(
						surveyData);
				cSVSurveyManager.scanRecords(monitor.newChild(1));
				cSVSurveyManagers.add(cSVSurveyManager);
			}
		}
		this.cSVSurveyManagers = cSVSurveyManagers;

		List<CDDocumentManager> cdDocumentManagers = new ArrayList<CDDocumentManager>();
		for (IBaseDataContainer baseDataContainer : this.baseContainers) {
			IDataContainer cdContainer = baseDataContainer
					.getSubContainer("cd");
			if (cdContainer == null) {
				LOGGER.warn("Could not load survey's CD data");
			} else {
				CDDocumentManager cdDocumentManager = new CDDocumentManager(
						cdContainer, "en");
				cdDocumentManager.scan(monitor.newChild(1));
				cdDocumentManagers.add(cdDocumentManager);
			}
		}
		this.cdDocumentManagers = cdDocumentManagers;

		List<HtmlManager> groupDiscussionManagers = new ArrayList<HtmlManager>();
		for (IBaseDataContainer baseDataContainer : this.baseContainers) {
			IDataContainer groupDiscussionsContainer = baseDataContainer
					.getSubContainer("group-discussions");
			if (groupDiscussionsContainer != null) {
				HtmlManager cdDocumentManager = new HtmlManager(
						groupDiscussionsContainer,
						"apiua://"
								+ GroupDiscussionLocatorProvider.GROUP_DISCUSSION_NAMESPACE);
				cdDocumentManager.scan(monitor.newChild(1));
				groupDiscussionManagers.add(cdDocumentManager);
			}
		}
		this.groupDiscussionManagers = groupDiscussionManagers;

		monitor.done();
	}

	/**
	 * TODO aggregate in case of multiple hits
	 *
	 * @param identifier
	 * @return
	 */
	public CSVSurveyRecord getSurveyRecord(IIdentifier identifier) {
		for (CSVSurveyManager cSVSurveyManager : this.cSVSurveyManagers) {
			CSVSurveyRecord cSVSurveyRecord = cSVSurveyManager
					.getSurveyRecord(identifier);
			if (cSVSurveyRecord != null) {
				return cSVSurveyRecord;
			}
		}
		return null;
	}

	public List<Token> getTokens() {
		List<Token> tokens = new ArrayList<Token>();
		for (CSVSurveyManager cSVSurveyManager : this.cSVSurveyManagers) {
			List<Token> currentTokens = cSVSurveyManager.getTokens();
			if (currentTokens != null) {
				tokens.addAll(currentTokens);
			}
		}
		return tokens;
	}

	public Collection<CDDocument> getCDDocuments() {
		Collection<CDDocument> cdDocuments = new ArrayList<CDDocument>();
		for (CDDocumentManager manager : this.cdDocumentManagers) {
			cdDocuments.addAll(manager.getDocuments());
		}
		return cdDocuments;
	}

	public Collection<GroupDiscussionDocument> getGroupDiscussionDocuments() {
		Collection<GroupDiscussionDocument> groupDiscussionDocuments = new ArrayList<>();
		for (HtmlManager manager : this.groupDiscussionManagers) {
			groupDiscussionDocuments.addAll(manager.getDocuments().stream()
					.map(html -> new GroupDiscussionDocument(html))
					.collect(Collectors.toList()));
		}
		return groupDiscussionDocuments;
	}
}
