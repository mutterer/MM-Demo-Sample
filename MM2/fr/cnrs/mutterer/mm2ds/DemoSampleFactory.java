package fr.cnrs.mutterer.mm2ds;

import ij.IJ;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorFactory;

import org.micromanager.PropertyMap;
import org.micromanager.Studio;

public class DemoSampleFactory implements ProcessorFactory {
   private final PropertyMap settings_;
   private final Studio studio_;

   public DemoSampleFactory(PropertyMap settings, Studio studio) {
      settings_ = settings;
      studio_ = studio;
   }

   @Override
   public Processor createProcessor() {
      return new DemoSampleProcessor(studio_, settings_.getString("camera", ""));
   }
}
