# Running the server:

```
mvn jetty:run
```

Then go to `http://localhost:8080` in your browser to see the control site!

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
