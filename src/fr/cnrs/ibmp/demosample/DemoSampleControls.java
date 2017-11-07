
package fr.cnrs.ibmp.demosample;

import java.awt.FlowLayout;
import java.io.InputStream;
import java.util.prefs.Preferences;

import org.micromanager.MMStudio;
import org.micromanager.api.DataProcessor;
import org.micromanager.utils.MMFrame;
import org.micromanager.utils.ReportingUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import mmcorej.StrVector;
import mmcorej.TaggedImage;

/**
 *
 Modified from NewImageFlipper by Arthur Edelstein and Nico Stuurman
 */
@SuppressWarnings("serial")
public class DemoSampleControls extends MMFrame {

	private final DemoSampleProcessor processor_;
	private final String selectedCamera_;
	private final String SELECTEDCAMERA = "SelectedCamera";
	private final Preferences prefs_;
	private final int frameXPos_ = 300;
	private final int frameYPos_ = 300;
	private ImagePlus imp;

	/**
	 * Creates form DemoSampleControls
	 *
	 * @param processor
	 */
	public DemoSampleControls(DemoSampleProcessor processor) {
		processor_ = processor;

		prefs_ = getPrefsNode();

		initComponents();

		selectedCamera_ = prefs_.get(SELECTEDCAMERA, MMStudio.getInstance().getCore().getCameraDevice());

		this.loadAndRestorePosition(frameXPos_, frameYPos_);

		updateCameras();
		setBackground(MMStudio.getInstance().getBackgroundColor());

		processor_.setCamera((String) cameraComboBox_.getSelectedItem());

		//ImagePlus imp = IJ.openImage(IJ.getDirectory("imagej") + "sample.tif");
		// open from jar file, taken from ij jar file demo by Rasband.
		InputStream is = getClass().getResourceAsStream("sample.tif");
        if (is!=null) {
            Opener opener = new Opener();
            imp = opener.openTiff(is, "DemoSample");
            // if (imp!=null) imp.show();
        }

		processor_.setSourceImage(imp);
	}

	public DataProcessor<TaggedImage> getProcessor() {
		return processor_;
	}

	/**
	 * updates the content of the camera selection drop down box
	 *
	 * Shows all available cameras and sets the currently selected camera as the
	 * selected item in the drop down box
	 */
	final public void updateCameras() {
		cameraComboBox_.removeAllItems();
		try {
			StrVector cameras = MMStudio.getInstance().getCore().getAllowedPropertyValues("Core", "Camera");
			for (String camera : cameras) {
				cameraComboBox_.addItem(camera);
			}
		} catch (Exception ex) {
			ReportingUtils.logError(ex);
		}
		cameraComboBox_.setSelectedItem(selectedCamera_);
	}

	private void initComponents() {

		cameraComboBox_ = new javax.swing.JComboBox();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Demo Sample");
//		setBounds(new java.awt.Rectangle(300, 300, 150, 150));
//		setMinimumSize(new java.awt.Dimension(200, 200));
//		setResizable(false);
		setLayout(new FlowLayout());
		add(cameraComboBox_);
		cameraComboBox_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cameraComboBox_ActionPerformed(evt);
			}
		});

		pack();
	}


	private void cameraComboBox_ActionPerformed(java.awt.event.ActionEvent evt) {
		String camera = (String) cameraComboBox_.getSelectedItem();
		if (camera != null) {
			if (processor_ != null) {
				processor_.setCamera(camera);
			}
			prefs_.put(SELECTEDCAMERA, camera);
		}
	}

	private javax.swing.JComboBox cameraComboBox_;


	public String getCamera() {
		return (String) cameraComboBox_.getSelectedItem();
	}

}
