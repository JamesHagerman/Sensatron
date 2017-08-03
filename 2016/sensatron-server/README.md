# Running the server:

```
mvn jetty:run
```

Then go to `http://localhost:8080` in your browser to see the control site!


## Going offline:

Because Maven manages dependencies on it's own by downloading them from the internet when it needs them, to run this thing on Playa means you'll have to tell Maven to download all of it's dependencies beforehand.

You can do that by making sure you're online, changing to the `2016/sensatron-server/` directory and running:

```
mvn dependency:go-offline
```

That will tell Maven to go off and download everything it thinks it needs.

## Building a .war file

This will spit out a deployable `.war` file to be run inside Tomcat:

```
mvn package
```

## Getting Audio passthrough working

You've gotta modify the `sound.properties` file inside the Java directory structure. Sun got all bitchy about their pulseaudio support or something.

Modify this file (Might be somewhere else on your box...):
`/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/sound.properties`

So it looks something like this:

```
#javax.sound.sampled.Clip=org.classpath.icedtea.pulseaudio.PulseAudioMixerProvider
#javax.sound.sampled.Port=org.classpath.icedtea.pulseaudio.PulseAudioMixerProvider
#javax.sound.sampled.SourceDataLine=org.classpath.icedtea.pulseaudio.PulseAudioMixerProvider
#javax.sound.sampled.TargetDataLine=org.classpath.icedtea.pulseaudio.PulseAudioMixerProvider

javax.sound.sampled.Clip=com.sun.media.sound.DirectAudioDeviceProvider
javax.sound.sampled.Port=com.sun.media.sound.PortMixerProvider
javax.sound.sampled.SourceDataLine=com.sun.media.sound.DirectAudioDeviceProvider
javax.sound.sampled.TargetDataLine=com.sun.media.sound.DirectAudioDeviceProvider
```

And that should let Java and Minim just use the working PulseAudio version instead of mucking around with stupid libraries. Woo!

## Getting lights working:

Before starting the server, make sure you've installed the correct libraries.
These are pretty much all in the `orange_box.tar.gz` archive as raw source code.
You will have to go through and get everything installed for your architecture.

If you're on an x86_64 Linux machine, you can try running the
`orange_box_install.sh` script and see how far that gets you.

Once those libraries are all installed, make sure your `LD_LIBRARY_PATH` is set
to point to wherever `libTotalControl.so` or `TotalControl.dll` was installed so
that the server can actually find the low level library it needs to communicate
with the lights.

After that's set up, you'll have to add a udev rule for your current user to
allow access to the usb subsystem:

Inside `/etc/udev/rules/95-ftdi.rules` place the following text:

```
SUBSYSTEM=="usb", ATTRS{idVendor}=="0403", ATTRS{idProduct}=="6001", MODE="0666"
```

Once that's there, you'll have to reload the udev rules using these commands:

```
sudo udevadm control --reload-rules
sudo udevadm trigger
```

Then make sure you're using the right drivers:

```
modprobe -r ftdi_sio
modprobe -r usbserial
```

And then start the server!

```
mvn jetty:run
```

Then go to `http://localhost:8080` in your browser to see the control site!


## System services:

We have written a systemd/systemctl service that starts up on boot. It lives here:

`/etc/systemd/system/sensatron.service`

When that service starts up, it runs `start-server.sh` which actually configures the correct environment variables and ends up running:

`mvn jetty:run >> ~sensatron/run.log &`

Other than that, we run `hostapd`, `dnsmasq`, and `nginx`:

- `hostapd` to set up an access point named `Sensatron` at `10.42.0.1`
- `dnsmasq` to route all DNS requests to the Nuc (once clients are connected)
- `nginx` to host a proxy that points at the Maven server, `sensatron-server`


## Controlling the sensatron.service

The service starts on boot. If you stop it, starting the service with the following DOES NOT SEEM TO WORK!!!:

```
sudo systemctl start sensatron.service
```

So use:

```
~sensatron/dev/Sensatron/2016/LIVE/start-server.sh
```


Stop the service with:

```
sudo systemctl stop sensatron.service
```

## Getting back into the Nuc to work on the projcet

After hooking up a screen and keyboard to the Nuc, you'll end up at a desktop.

### To use the on screen color display

0. Unplug the USB cable that goes to the lights/lasagna pan, whatever
1. Stop the service
2. Start processing (and close the nagging Welcome screen)
3. Load the `LEDDisplay` Processing script from: `~/dev/Sensatron/2016/LEDDisplay`
4. Run `LEDDisplay`
5. Start 
6. `tail -f ~sensatron/run.log`


### See if the server is actually still running

`ps -axww | grep sensatron`

### Kill an unruly server

This will kill processing as well...

`killall java`



