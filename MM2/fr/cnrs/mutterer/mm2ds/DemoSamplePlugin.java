package fr.cnrs.mutterer.mm2ds;


import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.data.ProcessorPlugin;
import org.micromanager.data.ProcessorFactory;

import org.micromanager.PropertyMap;
import org.micromanager.Studio;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

@Plugin(type = ProcessorPlugin.class)
public class DemoSamplePlugin implements ProcessorPlugin, SciJavaPlugin {
   private Studio studio_;

   @Override
   public void setContext(Studio studio) {
      studio_ = studio;
   }

   @Override
   public ProcessorConfigurator createConfigurator(PropertyMap settings) {
      return new DemoSampleConfigurator(studio_, settings);
   }

   @Override
   public ProcessorFactory createFactory(PropertyMap settings) {
      return new DemoSampleFactory(settings, studio_);
   }

   @Override
   public String getName() {
      return "Demo Sample";
   }

   @Override
   public String getHelpText() {
      return "Replaces images coming from the selected camera by demo sample";
   }

   @Override
   public String getVersion() {
      return "Version 2.0.1";
   }

   @Override
   public String getCopyright() {
      return "CC BY 4.0 Jerome Mutterer";
   }

}
