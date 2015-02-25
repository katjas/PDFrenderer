package com.sun.pdfview;

/**
 * Since there is no context that is passed between the various classes that
 * perform the pdf parsing and rendering, we introduce this class to at least
 * globally configure PDFRenderer.
 *
 * Typically you would configure the global instance before using any other
 * PDFRenderer API.
 */
public class Configuration {
	private static Configuration INSTANCE;

	public static synchronized Configuration getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Configuration();
		}
		return INSTANCE;
	}

	/** whether greyscale images will be converted to ARGB */
	private boolean convertGreyscaleImagesToArgb = true;
	/** threshold in pixels after which images are rendered in chunks (disabled by default) */
	private int thresholdForBandedImageRendering = 0;

	/**
	 * Enables or disables the conversion of greyscale images to ARGB.
	 * Disabling this may have a lower memory overhead with high resolution
	 * (e.g. scanned) images. Note that this has to be called before
	 * {@link #getImage()} is called to have an effect.
	 *
	 * Enabled by default.
	 * @param aFlag whether greyscale images shall be converted to ARGB.
	 */
	public void setConvertGreyscaleImagesToArgb(boolean aFlag) {
		convertGreyscaleImagesToArgb = aFlag;
	}

	/**
	 * Returns <code>true</code> if greyscale images will be converted to ARGB
	 */
	public boolean getConvertGreyscaleImagesToArgb() {
		return convertGreyscaleImagesToArgb;
	}

	/**
	 * If an image is higher than the given size (in pixels) then
	 * the image will be rendered in chunks, rather than as one big image.
	 * This may lead to lower memory consumption for e.g. scanned PDFs with
	 * large images.
	 *
	 * Set to a value <= 0 to disable banded image rendering.
	 * Defaults to 0 (off)
	 *
	 * @param aSize the height threshold at which to enable banded image rendering
	 */
	public void setThresholdForBandedImageRendering(int aSize) {
		thresholdForBandedImageRendering = aSize;
	}

	/**
	 * Returns the image height threshold at which to enable banded image rendering.
	 * @return the threshold value, or a value <= 0 if banded rendering is disabled
	 */
	public int getThresholdForBandedImageRendering() {
		return thresholdForBandedImageRendering;
	}
}
