package de.fu_berlin.imp.seqan.usability_analyzer.entity.editors;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IAnnotationModelFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

public class CodeAnnotationModelFactory implements IAnnotationModelFactory {

	@Override
	public IAnnotationModel createAnnotationModel(IPath location) {
		IFile file = FileBuffers.getWorkspaceFileAtLocation(location);
		AnnotationModel model = null;
		if (file != null)
			model = new ResourceMarkerAnnotationModel(file);
		model = new AnnotationModel();

		model.addAnnotation(
				new Annotation(
						"de.fu_berlin.imp.seqan.usability_analyzer.entity.markers.code",
						false, "CLONE"), new Position(20, 5));
		return model;
	}

}
