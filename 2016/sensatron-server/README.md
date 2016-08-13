# Running the server:

```
mvn jetty:run
```

Then go to `http://localhost:8080` in your browser to see the control site!

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
