package fr.cnrs.mutterer.mm2ds;

import org.micromanager.PropertyMap;
import org.micromanager.PropertyMaps;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.propertymap.MutablePropertyMapView;

import java.awt.Toolkit;

import javax.swing.*;

import mmcorej.StrVector;

public class DemoSampleConfigurator extends JFrame implements ProcessorConfigurator {

	private static final String DEFAULT_CAMERA = "Default camera for demo sample";
	private final int frameXPos_ = 300;
	private final int frameYPos_ = 300;
	private final Studio studio_;
	private final MutablePropertyMapView defaults_;
	private final String selectedCamera_;
	private javax.swing.JComboBox cameraComboBox_;

	public DemoSampleConfigurator(Studio studio, PropertyMap settings) {
		studio_ = studio;
		defaults_ = studio_.profile().getSettings(this.getClass());
		initComponents();
		selectedCamera_ = settings.getString("camera",
				defaults_.getString(DEFAULT_CAMERA, studio_.core().getCameraDevice()));
     super.setIconImage(Toolkit.getDefaultToolkit().getImage(
              getClass().getResource("/org/micromanager/icons/microscope.gif")));
      super.setLocation(frameXPos_, frameYPos_);
		updateCameras();
	}

	@Override
	public void showGUI() {
		setVisible(true);
	}

	@Override
	public void cleanup() {
		dispose();
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
			StrVector cameras = studio_.core().getAllowedPropertyValues("Core", "Camera");
			for (String camera : cameras) {
				cameraComboBox_.addItem(camera);
			}
		} catch (Exception ex) {
			studio_.logs().logError(ex, "Error updating valid cameras in demo sample");
		}
		cameraComboBox_.setSelectedItem(selectedCamera_);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	@SuppressWarnings("unchecked")
	private void initComponents() {
		cameraComboBox_ = new javax.swing.JComboBox();
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Demo Sample");
		setMinimumSize(new java.awt.Dimension(200, 50));
		setResizable(false);

		cameraComboBox_.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cameraComboBox_ActionPerformed(evt);
			}
		});
		add(cameraComboBox_);
		pack();
	}

	private void cameraComboBox_ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cameraComboBox_ActionPerformed
		String camera = (String) cameraComboBox_.getSelectedItem();
		if (camera != null) {
			defaults_.putString(DEFAULT_CAMERA, camera);
		}
		studio_.data().notifyPipelineChanged();
	}

	public String getCamera() {
		return (String) cameraComboBox_.getSelectedItem();
	}

	@Override
	public PropertyMap getSettings() {
		PropertyMap.Builder builder = PropertyMaps.builder();
		builder.putString("camera", getCamera());
		return builder.build();
	}
}