TCL Lighting Rig
================

This project started off as a way to control the addressable RGB LED strands used on the Sensatron art car in 2013 (http://postnuclearfamily.com/?page_id=55). It was a heavy modification from the previous year (2012) where the same lights were controlled using an Arduino. The light are Total Control Lighting strands from CoolNeon (http://www.coolneon.com/).

After having multiple memory and speed issues with the Arduino based system, I sat down and rewrote most of the code in Processing. The hardware interface is a SparkFun FTDI breakout board using the bitbang driver to shove the data out to the lights through 6 parallel data lines. The driver and library used to control the lights was provided by PaintYourDragon (https://github.com/PaintYourDragon/p9813).

Because the FTDI bitbang driver is not able to be used at the same time as the FTDI serial driver, no FTDI based Arduinos can be used at the same time as this code.

Some of the code layout ideas were suggested to me while helping solder lights for the Disorient LED wall Pyramid art frontage that was on playa in 2013. That code was written by the same people that did the Bay Lights Bridge project and while the hardware interface is quite different, the code structure really helped me get the animation code working in time for the Burn.

This code has since moved away from being tied to the Sensatron and has started taking a new form that will allow it to be used for many other TCL lighting applications where a PC can be hooked up to the lights at all times.

Some Features:
==============

A few different types of input are provided for the animations to use:

A bluetooth connected gyro "football" was the first addition. While it wasn't working the entire Burn because of battery issues, it was still a major hit with the people that got to play with it. Keeping the bluetooth connection open, however, was dificult due to the limitations of Bluetooth connections under OS X.

Raw color data from a web cam can be dumped to the lights as well. By using CamTwist (http://camtwiststudio.com/), any video that can be displayed on the computer's normal monitor can also be used as a color source.

After hacking at the Kinect Processing library originally written by Daniel Shiffman (http://shiffman.net/p5/kinect/), I was able to modify this code to be able to support multiple Kinects at the same time.

Along with multiple Kinect support, the KinectManager class also includes blob/flob tracking using the Processing library written by Andre Sier (http://s373.net/code/flob/). This library filters the incoming depth data from each Kinect and parses out the locations of any moving objects seen by any of the Kinects. The incoming depth images could easily be scaled down for faster flob detection.



The Future:
===========

I plan to continue modifying this project as time moves on to support different forms of input, different position mappings, and different animation control.

For now, it's doing just as much as it needs though...
