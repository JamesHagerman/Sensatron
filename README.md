# SENSATRON

This is the repo that will end up being the one-stop-shop for all code related to the Sensatron art car.

## 2016-Current Code

This code exists in the `2016/` directory. Look at the `README.md` file in there to figure out how to run it.

This version runs on an Intel Nuc minicomputer as a Java web server that hosts a control interface via WiFi. Anyone connected to the wifi can control the car's LEDs. This version is sound reactive.

The WiFi Access Point is hosted on the Nuc directly using `hostapd` while `dnsmasq` routes all users connecting to the AP to the Sensatron control interface web site. The Java app is hosted via `jetty` and traffic is routed to it via an `nginx` proxy. 

In 2017, an Android tablet traveled to Playa and ran a fluid simulator that streamed colors to the Sensatron via HTTP connections.

## 2014-2015 Code

Exists in `2014-15-beaglebone-black/` but actually lives in this repo here: (https://github.com/JamesHagerman/BBB-BM-Lights-2015)[https://github.com/JamesHagerman/BBB-BM-Lights-2015]. This ran on the Beaglebone Black touchscreen computer James built. It runs GLSL Shaders to generate colors. Compiling it is a pain. We tried getting audio working in 2015, but Linux audio driver issues and last minute hacks kept that from happening.

## 2013 Code

Exists in `2013-processing/`. This was just a Processing project that ran on James' laptop on the car the entire night. It had a 9DoF motion sensor locked in a yellow "football" hooked up via bluetooth that people could use to control the lights directly.

This was the first year we used the `p9813` FTDI libary to control the TCL Lights. That library is here: (https://github.com/PaintYourDragon/p9813)[https://github.com/PaintYourDragon/p9813]

## 2012 Code

Exists in `2012-arduino/`. This was an Arduino sketch that ran great with one strand of LEDs but ran out of memory on playa when tested against 600 LEDs. Oops. Memory and Power issues that year were a pain in the butt.

## 2011 and Earlier

James wasn't involved in this work. It was a Processing sketch that interfaced with a different kind of LED pixels. Not sure if this code works at all anymore. It was sound reactive.