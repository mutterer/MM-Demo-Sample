package fr.cnrs.mutterer.mm2ds;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import org.micromanager.data.Image;
import org.micromanager.data.Metadata;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorContext;

import org.micromanager.PropertyMap;
import org.micromanager.PropertyMaps;
import org.micromanager.Studio;

import com.google.common.eventbus.Subscribe;
import ij.gui.Arrow;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.micromanager.data.Metadata.Builder;
import org.micromanager.events.PixelSizeChangedEvent;

import org.micromanager.events.StagePositionChangedEvent;

public class DemoSampleProcessor implements Processor {

    private final Studio studio_;
    String camera_;
    static ImagePlus imp;
    static ImageProcessor ip, ipObj;
    private static double pixSizeUm;
    private static double x, y, z, exp;
    private static double where;
    private static PolygonRoi objectPath;
    private static boolean stageWarning;

    public DemoSampleProcessor(Studio studio, String camera) {
        studio_ = studio;
        camera_ = camera;
        String url = getClass().getClassLoader().getResource("MM_COS_clathrin-mt-actin-dapi.zip").toString();
        imp = IJ.openImage(url);
        int[] xpoints = {680, 88, 736, 1048, 1070, 1114, 1178, 1260, 1596, 1650, 1370, 1290, 1210, 1132, 742};
        int[] ypoints = {466, 1074, 1186, 918, 1150, 1210, 1464, 1542, 1634, 1580, 1216, 922, 502, 562, 530};
        imp.setRoi(new PolygonRoi(xpoints, ypoints, 15, Roi.POLYGON));
        IJ.run(imp, "Fit Spline", "");
        IJ.run(imp, "Interpolate", "interval=4 smooth");
        objectPath = (PolygonRoi) imp.getRoi();
        ip = imp.getProcessor();
        studio_.events().registerForEvents(this);
        try {
            z = studio.getCMMCore().getPosition();
            x = studio.getCMMCore().getXPosition();
            y = studio.getCMMCore().getYPosition();
            exp = studio.getCMMCore().getExposure();
        } catch (Exception e) {
            e.printStackTrace();
        }
        pixSizeUm = studio.getCMMCore().getPixelSizeUm();

    }

    @Subscribe
    public void onPixelSizeChanged(PixelSizeChangedEvent event) {
        pixSizeUm = event.getNewPixelSizeUm();
    }

    @Subscribe
    public void onStagePositionChanged(StagePositionChangedEvent event) {
        z = event.getPos();
    }

    /**
     * Process one image.
     */
    @Override
    public void processImage(Image image, ProcessorContext context) {
        // to allow processing old data, we do not check for the camera when no 
        // camera was selected
        if (!camera_.isEmpty()) {
            String imageCam = image.getMetadata().getCamera();
            if (imageCam == null || !imageCam.equals(camera_)) {
                // Image is for the wrong camera; just pass it along unmodified.
                context.outputImage(image);
                return;
            }
        }
        context.outputImage(transformImage(studio_, image));
    }

    /**
     * Executes image transformation First mirror the image if requested, than
     * rotate as requested
     *
     * @param studio
     * @param image Image to be transformed.
     * @param isMirrored Whether or not to mirror the image.
     * @param rotation Degrees to rotate by (R0, R90, R180, R270)
     * @return - Transformed Image, otherwise a copy of the input
     */
    public static Image transformImage(Studio studio, Image image) {

        ImageProcessor proc = studio.data().ij().createProcessor(image);
        where += 1;
        try {
            z = studio.getCMMCore().getPosition();
            x = studio.getCMMCore().getXPosition();
            y = studio.getCMMCore().getYPosition();
            exp = studio.getCMMCore().getExposure();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int channel = -1;
        try {
            String ch = studio.getCMMCore().getCurrentConfig("Channel");

            if (ch.startsWith("DAPI")) {
                channel = 3;
            } else if (ch.startsWith("FITC")) {
                channel = 2;
            } else if (ch.startsWith("Rhodamine")) {
                channel = 1;
            } else if (ch.startsWith("Cy5")) {
                channel = 4;
            } else if (ch.startsWith("Mi")) {
                channel = 5;
            } else {
                channel = -1;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        imp.setC(channel);
        ip = imp.getChannelProcessor();
        ipObj = addEffect(studio, ip);

        int fieldSize = (int) (512 * pixSizeUm);

        ipObj.setRoi((int) (ip.getWidth() / 2 + (int) x - fieldSize / 2), (int) (ip.getHeight() / 2 + (int) y - fieldSize / 2), fieldSize, fieldSize);
        ipObj = ipObj.crop();
        stageWarning = (ipObj.getWidth() != ipObj.getHeight()) ? true : false;
        ipObj = ipObj.resize(512, 512);

        
        if (stageWarning) {
            ipObj.setColor(0xffffff);
            ipObj.setFont(new Font("Arial", Font.PLAIN, 30));
            ipObj.drawString("Stage Limit Reached!", 100, 256);
        } else if (pixSizeUm < 1.0) {
            ipObj.blurGaussian(0.5);
        }

        // blur image when away from z0
        double defocusFactor = ij.Prefs.get("demosample.defocus", 0.5);
        if (defocusFactor > 0) {
            ipObj.blurGaussian(Math.abs(z * defocusFactor));
        }

        // adjust range as per camera exposure value
        double exposureFactor = ij.Prefs.get("demosample.exposure", 0.1);
        if (exposureFactor > 0) {
            ipObj.multiply(exp * exposureFactor);
        }

        // add small amount of camera noise
        double noiseValue = ij.Prefs.get("demosample.noise", 2.0);
        if (noiseValue > 0) {
            ipObj.noise(noiseValue);
        }

        // emulate lights off if shutter closed
        try {
            if ((!studio.getCMMCore().getAutoShutter()
                    && !studio.getCMMCore().getShutterOpen()) || channel < 0) {
                ipObj.and(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert some metadata to indicate what we did to the image.
        PropertyMap.Builder builder;
        PropertyMap userData = image.getMetadata().getUserData();
        if (userData != null) {
            builder = userData.copyBuilder();
        } else {
            builder = PropertyMaps.builder();
        }
        Metadata newMetadata = image.getMetadata().copyBuilderPreservingUUID().userData(builder.build()).build();
        Image result = studio.data().ij().createImage(ipObj, image.getCoords(), newMetadata);
        Builder mb = result.getMetadata().copyBuilderWithNewUUID();
        mb.pixelSizeUm(pixSizeUm);
        return result.copyWithMetadata(mb.build());
    }

    private static ImageProcessor addEffect(Studio studio, ImageProcessor ip) {
        // only one effect so far: 
        // 1. add an object moving accross the image. 
        ipObj = ip.duplicate();
        try {
            if (1 == (int) Double.parseDouble((String) studio.getCMMCore().getProperty("Camera", "TestProperty2"))) {
                where = (System.currentTimeMillis() % 30000) * objectPath.getNCoordinates() / 30000;
                ipObj.setColor(0xffffff);
                Arrow a = new Arrow(
                        objectPath.getXCoordinates()[((int) where) % objectPath.getNCoordinates()] + (int) objectPath.getXBase(),
                        objectPath.getYCoordinates()[((int) where) % objectPath.getNCoordinates()] + (int) objectPath.getYBase(),
                        objectPath.getXCoordinates()[((int) where + 2) % objectPath.getNCoordinates()] + (int) objectPath.getXBase(),
                        objectPath.getYCoordinates()[((int) where + 2) % objectPath.getNCoordinates()] + (int) objectPath.getYBase()
                );
                a.drawPixels(ipObj);
            }
        } catch (Exception ex) {
            Logger.getLogger(DemoSampleProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ipObj;
    }
    
}
