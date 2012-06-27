package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;
import de.fu_berlin.inf.nebula.explanation.SimpleExplanationComposite;
import de.fu_berlin.inf.nebula.explanation.SimpleExplanationComposite.SimpleExplanation;

public class MemoComposer extends Composite {

	private static final Logger LOGGER = Logger.getLogger(MemoComposer.class);

	private SimpleExplanationComposite header;
	private Text text;
	private long autosaveAfterMilliseconds;

	private ICodeService codeService = null;

	private ICode code = null;
	private ICodeInstance codeInstance = null;
	private ICodeable codeable = null;

	public MemoComposer(Composite parent, int style,
			long autosaveAfterMilliseconds) {
		super(parent, style & ~SWT.BORDER);
		this.setLayout(GridLayoutFactory.fillDefaults().create());

		this.header = new SimpleExplanationComposite(this, SWT.NONE);
		this.header.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		this.header.setSpacing(2);

		this.text = new Text(this, style & SWT.BORDER | SWT.MULTI);
		this.text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.text.addListener(SWT.KeyUp, new Listener() {
			public void handleEvent(Event event) {
				if ((event.stateMask == SWT.CTRL || event.stateMask == SWT.COMMAND)
						&& event.keyCode == 'a') {
					((Text) event.widget).selectAll();
				}
			}
		});

		this.autosaveAfterMilliseconds = autosaveAfterMilliseconds;
		this.codeService = (ICodeService) PlatformUI.getWorkbench().getService(
				ICodeService.class);

		// auto-save feature
		if (this.autosaveAfterMilliseconds > 0) {
			final AtomicReference<Runnable> runnable = new AtomicReference<Runnable>();
			runnable.set(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						LOGGER.error("Error auto-saving memo", e);
					}
					if (MemoComposer.this.text != null
							&& !MemoComposer.this.text.isDisposed()) {
						save();
						LOGGER.debug("Successfully auto-saved memo");
						new Thread(runnable.get()).start();
					}
				}
			});
			new Thread(runnable.get()).start();
		}
	}

	public void load(ICode code) {
		this.save();
		this.code = code;
		this.codeInstance = null;
		this.codeable = null;
		this.load();
	}

	public void load(ICodeInstance codeInstance) {
		this.save();
		this.code = null;
		this.codeInstance = codeInstance;
		this.codeable = null;
		this.load();
	}

	public void load(ICodeable codeable) {
		this.save();
		this.code = null;
		this.codeInstance = null;
		this.codeable = codeable;
		this.load();
	}

	private void load() {
		Image icon = null;
		String caption = null;
		String html = null;
		if (this.code != null) {
			icon = ImageManager.CODE;
			caption = this.code.getCaption();
			html = this.codeService.loadMemo(code);
		} else if (this.codeInstance != null) {
			ICodeable coded = this.codeService.getCodedObject(this.codeInstance
					.getId());
			ILabelProvider lp = this.codeService.getLabelProvider(coded
					.getCodeInstanceID());
			icon = lp.getImage(coded);
			caption = lp.getText(coded) + " (coded with "
					+ this.codeInstance.getCode().getCaption() + ")";
			html = this.codeService.loadMemo(codeInstance);
		} else if (this.codeable != null) {
			ILabelProvider lp = this.codeService.getLabelProvider(this.codeable
					.getCodeInstanceID());
			icon = lp.getImage(this.codeable);
			caption = lp.getText(this.codeable);
			html = this.codeService.loadMemo(codeable);
		} else
			return;

		this.header.setExplanation(new SimpleExplanation(icon, caption));
		this.text.setText(html != null ? html : "");
		this.layout();
	}

	private void save() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				String html = text.getText();
				if (code != null) {
					try {
						codeService.setMemo(code, html);
					} catch (CodeServiceException e) {
						LOGGER.error("Can't save memo for " + code, e);
					}
					return;
				}
				if (codeInstance != null) {
					try {
						codeService.setMemo(codeInstance, html);
					} catch (CodeServiceException e) {
						LOGGER.error("Can't save memo for " + codeInstance, e);
					}
					return;
				}
				if (codeable != null) {
					try {
						codeService.setMemo(codeable, html);
					} catch (CodeServiceException e) {
						LOGGER.error("Can't save memo for " + codeable, e);
					}
					return;
				}
			}
		});
	}

}
