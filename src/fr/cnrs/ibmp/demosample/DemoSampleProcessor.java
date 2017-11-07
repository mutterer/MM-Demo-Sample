
package fr.cnrs.ibmp.demosample;

import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.MMStudio;
import org.micromanager.acquisition.TaggedImageQueue;
import org.micromanager.api.DataProcessor;
import org.micromanager.utils.ImageUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import mmcorej.TaggedImage;

public class DemoSampleProcessor extends DataProcessor<TaggedImage> {

	String camera_;
	static ImageProcessor ip_;
	static ImagePlus imp_;
	DemoSampleControls controls_;

	/**
	 * Polls for tagged images, and processes them if they are from the selected
	 * camera.
	 * 
	 */
	@Override
	public void process() {
		try {
			TaggedImage nextImage = poll();
			if (nextImage != TaggedImageQueue.POISON) {
				try {
					String camera = nextImage.tags.getString("Core-Camera");
					if (!camera.equals(camera_)) {
						if (nextImage.tags.has("Camera")) {
							camera = nextImage.tags.getString("Camera");
						}
					}
					if (!camera.equals(camera_)) {
						produce(nextImage);
						return;

					}

					produce(proccessTaggedImage(nextImage));

				} catch (JSONException ex) {
					produce(TaggedImageQueue.POISON);
					ReportingUtils.logError(ex);
				} catch (MMScriptException ex) {
					produce(TaggedImageQueue.POISON);
					ReportingUtils.logError(ex);
				}
			} else {
				// Must produce Poison (sentinel) image to terminate tagged
				// image pipeline
				produce(nextImage);
			}
		} catch (Exception ex) {
			ReportingUtils.logError(ex);
		}
	}

	public static TaggedImage proccessTaggedImage(TaggedImage nextImage)
			throws JSONException, MMScriptException {

		int width = MDUtils.getWidth(nextImage.tags);
		int height = MDUtils.getHeight(nextImage.tags);

		String type = MDUtils.getPixelType(nextImage.tags);
		int ijType = ImagePlus.GRAY8;
		if (type.equals("GRAY16")) {
			ijType = ImagePlus.GRAY16;
		}

		ImageProcessor proc = ImageUtils.makeProcessor(ijType, width, height, nextImage.pix);

		JSONObject newTags = nextImage.tags;

		if ((width != proc.getWidth()) || (height != proc.getHeight())) {
			throw new MMScriptException("Demo Sample, Size not matching");
		}
		MDUtils.setWidth(newTags, proc.getWidth());
		MDUtils.setHeight(newTags, proc.getHeight());

		double y = 0;
		double x = 0;
		double z = 0;
		double exp = 0;

		try {
			x = MMStudio.getInstance().getCore().getXPosition();
			y = MMStudio.getInstance().getCore().getYPosition();
			z = MMStudio.getInstance().getCore().getPosition();
			exp = MMStudio.getInstance().getCore().getExposure();
		} catch (Exception e) {
			e.printStackTrace();
		}
		x = Math.max(2, Math.min(510, 256 + x));
		y = Math.max(2, Math.min(510, 256 + y));

		int channel = -1;
		try {
			String ch = MMStudio.getInstance().getCore().getCurrentConfig("Channel");
			if (ch.startsWith("DA"))
				channel = 3;
			else if (ch.startsWith("FI"))
				channel = 2;
			else if (ch.startsWith("Rh"))
				channel = 1;
			else if (ch.startsWith("Cy"))

				channel = 4;
			else
				channel = -1;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Roi roi = new Roi(x, y, width, height);
		imp_.setC(channel);
		ip_ = imp_.getChannelProcessor();
		proc = ip_;
		proc.setRoi(roi);
		proc = proc.crop();

		int obj = 1;

		try {
			String ch = MMStudio.getInstance().getCore().getCurrentConfig("Objective");
			if (ch.startsWith("10"))
				obj = 1;
			else if (ch.startsWith("20"))
				obj = 2;
			else if (ch.startsWith("40"))
				obj = 4;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (obj > 1) {
			int w = width / obj;
			int h = height / obj;
			proc.setRoi((width - w) / 2, (height - h) / 2, w, h);
			proc = proc.crop();
			proc = proc.resize(width, height);

		}
		double defocusFactor = ij.Prefs.get("demosample.defocus", 0.5);
		if (defocusFactor>0) proc.blurGaussian(Math.abs(z*defocusFactor));

		double exposureFactor = ij.Prefs.get("demosample.exposure", 0.1);
		if (exposureFactor>0) proc.multiply(exp*exposureFactor);
		
		double noiseValue = ij.Prefs.get("demosample.noise", 20.0);
		if (noiseValue>0) proc.noise(noiseValue); // lire plutot d'une pref imagej

		try {
			if ((!MMStudio.getInstance().getCore().getAutoShutter()
					&& !MMStudio.getInstance().getCore().getShutterOpen()) || channel < 0)
				proc.and(0); // all pixels to zero if shutter closed !
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (type.equals("GRAY8")) {
			proc = proc.convertToByte(false);
		}

		return new TaggedImage(proc.getPixels(), newTags);
	}

	/**
	 * Update which camera is to be processed.
	 * 
	 * @param camera
	 *            - Camera name
	 */
	public void setCamera(String camera) {
		camera_ = camera;
	}

	/**
	 * Update source image.
	 * 
	 * @param image
	 */
	public void setSourceImage(ImagePlus imp) {
		imp_ = imp;
	}

	/**
	 * Generate the configuration UI for this processor.
	 */
	@Override
	public void makeConfigurationGUI() {
		if (controls_ == null) {
			controls_ = new DemoSampleControls(this);
			MMStudio.getInstance().addMMBackgroundListener(controls_);
		} else {
			controls_.updateCameras();
		}
		controls_.setVisible(true);
	}

	/**
	 * Dispose of the GUI generated by this processor.
	 */
	@Override
	public void dispose() {
		// Ensure that the controls actually exist first.
		if (controls_ != null) {
			controls_.dispose();
			// And make certain we don't think the controls are still valid.
			controls_ = null;
		}
	}

}
