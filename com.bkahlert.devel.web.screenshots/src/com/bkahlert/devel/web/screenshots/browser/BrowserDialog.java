package com.bkahlert.devel.web.screenshots.browser;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.AuthenticationEvent;
import org.eclipse.swt.browser.AuthenticationListener;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class BrowserDialog extends Dialog {

	public static final int COMPLETE_CALL_DELAY = 800;
	public static final String JS_SCROLLTO = "javascript:$(document).scrollTo(%d, %d, { duration: 0 });";

	private Browser browser;
	private int timeout;
	private volatile boolean completed = false;

	private String url;
	private Point windowDimensions;
	private Point scrollPosition;

	private ArrayList<FutureTask<?>> finishedTasks;

	private ProgressListener progressListener = new ProgressAdapter() {
		@Override
		public void completed(ProgressEvent event) {
			complete();
		}
	};
	private Thread timeoutThread = new Thread(new Runnable() {
		@Override
		public void run() {
			try {
				synchronized (this) {
					wait(timeout);
				}
			} catch (InterruptedException e) {
				return;
			}

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if (!browser.isDisposed())
						browser.removeProgressListener(progressListener);
					complete();
				}
			});
		}
	});

	public BrowserDialog(Shell parentShell, String url, Point windowDimensions,
			Point scrollPosition, int timeout) {
		super(parentShell);
		this.url = url;
		this.windowDimensions = windowDimensions;
		this.scrollPosition = scrollPosition;

		this.timeout = timeout;

		this.finishedTasks = new ArrayList<FutureTask<?>>();
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new FillLayout());
		this.browser = new Browser(composite, SWT.BORDER);
		this.browser.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				cancel();
			}
		});
		this.browser.addAuthenticationListener(new AuthenticationListener() {
			@Override
			public void authenticate(AuthenticationEvent event) {
				event.user = "###";
				event.password = "###";
			}
		});
		this.browser.addProgressListener(progressListener);
		this.timeoutThread.start();
		this.browser.setUrl(this.url);
		return composite;
	}

	protected void complete() {
		if (completed)
			return;
		completed = true;
		if (browser != null && !browser.isDisposed()) {
			if (scrollPosition != null)
				browser.execute(String.format(JS_SCROLLTO, scrollPosition.y,
						scrollPosition.x));
			/*
			 * Scrolling needs some time. We therefore have to wait in a non-UI
			 * thread and then return.
			 */
			new Thread(new Runnable() {
				@Override
				public void run() {
					synchronized (this) {
						try {
							wait(COMPLETE_CALL_DELAY);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								for (FutureTask<?> futureTask : finishedTasks) {
									futureTask.run();
								}
							}
						});
					}
				}
			}).start();
		} else {
			cancel();
		}
	}

	protected void cancel() {
		for (FutureTask<?> futureTask : finishedTasks) {
			if (!futureTask.isCancelled())
				futureTask.cancel(true);
		}
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return parent;
	}

	protected Point getInitialSize() {
		Rectangle trim = this.getShell().computeTrim(0, 0,
				this.windowDimensions.x, this.windowDimensions.y);
		return new Point(trim.width, trim.height);
	}

	public Browser getBrowser() {
		return this.browser;
	}

	public <V extends Object> Future<V> addFinishedCallable(Callable<V> callable) {
		FutureTask<V> futureTask = new FutureTask<V>(callable);
		this.finishedTasks.add(futureTask);
		return futureTask;
	}
}
