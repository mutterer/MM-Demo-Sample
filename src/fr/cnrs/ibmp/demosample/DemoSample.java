
package fr.cnrs.ibmp.demosample;

import org.micromanager.api.MMProcessorPlugin;

public class DemoSample implements MMProcessorPlugin {
   public static String menuName = "Demo Sample";
   public static String tooltipDescription = "demo sample";
  
   public static Class<?> getProcessorClass() {
      return DemoSampleProcessor.class;
   }

   public void configurationChanged() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public String getDescription() {
      return "hacks demo sample into images coming from the selected camera";
   }

   @Override
   public String getInfo() {
      return "Not supported yet.";
   }

   @Override
   public String getVersion() {
      return "Version 0.1";
   }

   @Override
   public String getCopyright() {
      return "Copyright CNRS, 2017";
   }

}
