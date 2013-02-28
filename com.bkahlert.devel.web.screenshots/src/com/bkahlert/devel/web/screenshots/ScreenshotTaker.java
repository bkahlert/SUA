package com.bkahlert.devel.web.screenshots;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.web.screenshots.browser.BrowserDialog;
import com.bkahlert.devel.web.screenshots.browser.BrowserException;

public class ScreenshotTaker {
	private static final Logger LOGGER = Logger.getLogger(ScreenshotTaker.class
			.getName());
	public static final String SCREENSHOT_FILE_PREFIX = "screenshot";

	private Robot robot;

	public ScreenshotTaker() throws PartInitException, AWTException {
		this.robot = new Robot();
	}

	/**
	 * Determines the maximum takeable screenshot size of this computer.
	 * 
	 * @UI
	 * @return
	 */
	public org.eclipse.swt.graphics.Rectangle getMaxCaptureArea() {
		try {
			BrowserDialog dialog = new BrowserDialog(null, "about:blank",
					new Point(Integer.MAX_VALUE, Integer.MAX_VALUE), null, 10);
			dialog.setBlockOnOpen(false);
			dialog.open();
			org.eclipse.swt.graphics.Rectangle maxCaptureArea = getCaptureArea(dialog);
			dialog.close();
			return maxCaptureArea;
		} catch (BrowserException e) {
			LOGGER.log(
					Level.WARNING,
					"Could not determine correct maximum capture area for screenshots",
					e);
		}
		return Display.getDefault().getBounds();
	}

	/**
	 * Creates and returns a screenshot for the given {@link ScreenshotInfo}.
	 * <p>
	 * Note: The temporary file gets deleted when the virtual machine
	 * terminates. In order to persist the file you have to copy it.
	 * 
	 * @param screenshot
	 * @param format
	 * @return
	 * @blocking
	 */
	public File takeScreenshot(final ScreenshotInfo screenshot,
			final String format, final int timeout) {
		if (Display.getCurrent() != null)
			throw new SWTException(
					"This method is long running and must *not* be opened in the UI Thread.");

		final AtomicReference<BrowserDialog> dialogReference = new AtomicReference<BrowserDialog>();
		final AtomicReference<Future<File>> screenshotFutureReference = new AtomicReference<Future<File>>();

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell();
				try {
					final BrowserDialog dialog = new BrowserDialog(shell,
							screenshot.getUrl(), screenshot
									.getWindowDimensions(), screenshot
									.getScrollPosition(), timeout);
					dialogReference.set(dialog);
					Future<File> screenshotFuture = dialog
							.addFinishedCallable(new Callable<File>() {
								@Override
								public File call() throws Exception {
									BufferedImage image = captureScreen(dialog);
									return imageToTempFile(image, format);
								}
							});
					screenshotFutureReference.set(screenshotFuture);
					dialog.setBlockOnOpen(false);
					dialog.open();
				} catch (BrowserException e) {
					LOGGER.log(Level.WARNING, "Could not take screenshot of "
							+ screenshot.getUrl(), e);
				}

			}
		});

		if (dialogReference.get() == null)
			return null;

		File screenshotFile = null;
		try {
			screenshotFile = screenshotFutureReference.get().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				dialogReference.get().close();
			}
		});

		return screenshotFile;
	}

	/**
	 * Calculates the {@link Dialog}'s client area relative to the screen.
	 * 
	 * @return
	 */
	protected org.eclipse.swt.graphics.Rectangle getCaptureArea(Dialog dialog) {
		// window bounds
		org.eclipse.swt.graphics.Rectangle area = dialog.getShell().getBounds();

		// window bounds + trim (= 2 borders)
		org.eclipse.swt.graphics.Rectangle trimArea = dialog.getShell()
				.computeTrim(area.x, area.y, area.width, area.height);

		// subtract what SWT wanted to add to remove the border
		area.x -= (trimArea.x - area.x);
		area.y -= (trimArea.y - area.y);
		area.width -= (trimArea.width - area.width);
		area.height -= (trimArea.height - area.height);
		return area;
	}

	/**
	 * Makes a screenshot of the given coordinates.
	 * 
	 * @param dialog
	 * @return
	 */
	protected BufferedImage captureScreen(Dialog dialog) {
		org.eclipse.swt.graphics.Rectangle area = getCaptureArea(dialog);

		BufferedImage bufferedImage = robot.createScreenCapture(new Rectangle(
				area.x, area.y, area.width, area.height));

		return bufferedImage;
	}

	/**
	 * Writes the given image to a temp file and returns it.
	 * <p>
	 * Note: The temporary file gets deleted when the virtual machine
	 * terminates.
	 * 
	 * @param bufferedImage
	 * @param format
	 * @return
	 * @throws IOException
	 */
	protected File imageToTempFile(BufferedImage bufferedImage, String format)
			throws IOException {
		File tempFile = File.createTempFile(SCREENSHOT_FILE_PREFIX, "."
				+ format);
		tempFile.deleteOnExit();
		ImageIO.write(bufferedImage, format, tempFile);
		return tempFile;
	}
}
