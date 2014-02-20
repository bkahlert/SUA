package de.fu_berlin.imp.seqan.usability_analyzer.doclog.jobs;

import java.util.concurrent.ExecutionException;

import org.eclipse.swt.widgets.Shell;

import com.bkahlert.nebula.screenshots.impl.webpage.FormContainingWebpageScreenshotRenderer;
import com.bkahlert.nebula.widgets.browser.extended.IJQueryBrowser;

public class DoclogWebpageScreenshotRenderer extends
		FormContainingWebpageScreenshotRenderer<DoclogWebpage> {

	public DoclogWebpageScreenshotRenderer(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void loadingWebpageFinished(DoclogWebpage webpage,
			IJQueryBrowser browser) {
		super.loadingWebpageFinished(webpage, browser);

		/*
		 * FIXME The navigation of the old SeqAn documentation triggers its
		 * search result update using an onkeyup attribute on the search input
		 * field that - for unknown reason - can not be triggered using jQuery.
		 */
		if (webpage.getUri().toString().contains("seqan/dev/INDEX_Page.html")) {
			try {
				/*
				 * Take the typed in input and do the search manually without
				 * using updateSearch in dddoc.js
				 */
				browser.run(
						"var text=$('#search').val();"
								+ "var s='';count=1;if(text.length>=2){if(text.length<3)reg=new RegExp('^('+text.toLowerCase()+')','gi');else reg=new RegExp('('+text.toLowerCase()+')','gi');for(i=0;i<DB.length-1;++i){entry=DB[i];key=entry[0];if(key.match(reg)){displaytext=entry[0].replace(reg,'<b>$1</b>');s+='<div><nobr><a target=_parent '+entry[2]+displaytext+' '+entry[1]+'</a></nobr></div>';++count;if(count>=MAX_RESULT)break}}}"
								+ "$('#result').html(s);").get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
