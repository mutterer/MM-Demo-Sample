# MM-Demo-Sample

This is the repository for a Micro-Manager 1.4 On-The-Fly_Processor plugin. This processor is supposed to work with the Demo configuration file, and replace images from the demo camera by portions of a built-in multichannel fluorescence image. It adds some noise to the image, blurs if you defocus, scales the pixel values with exposure, blanks with shutter and moves with the stage (to the max extent of the built-in Image). It has 4 channels, DAPI, FITC, Rhodamine and the MM logo in the Cy5 channel, so you can do MDA, or demonstrate most features of MM. I built this for a MM training.

# Included image disclaimer
I'm using a modified version of this image as a demo sample: https://commons.wikimedia.org/wiki/File:HeLa-I.jpg
This image is from the National Institutes of Health (NIH). I hope it's OK to use it, but I'm happy to replace it, would someone be wanting to provide a nice 3 channels image of something like 2048x2048.

# Installation
In a fresh installation of MM1.4, put the jar file inside mmplugins/On-The-Fly_Processors
