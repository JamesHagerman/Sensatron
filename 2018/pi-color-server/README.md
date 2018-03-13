# Sensatron Pi Color Server

This is a simple server that will accept socket connections, take packet data, and turn it into LED Colors.

This was built off of some old Raspberry Pi SPI RGB LED work. It supported the following LED formats:

- ws2801
- Total Control Lighting (TCL) strands via the SPI pins on the Pi's GPIO header

Due to our existing hardware design, I will be updating this server to also support:

- Total Control Lighting strands via the p9813 FTDI based driver from PaintYourDragon

## Build

As this runs on the Pi (and, really, only the Pi) the Makefile actually copies the files TO the Pi and then compiles them there. Maybe there's a better way to do this...

### Install WiringPi libraries on the Pi

```
cd
git clone git://git.drogon.net/wiringPi
cd wiringPi
./build
```

### Install FTDI and P9813, drivers and libraries

Install the FTDI driver, build the p9813 C library, build the p9813 Java library, and install the libraries

```
cd Sensatron/hardware-interface/
tar -zxvf FTDI_Hack.tar.gz
git clone git@github.com:PaintYourDragon/p9813.git
./orange_box_install.sh
```

### Configure and Run `make`

You'll have to modify the Makefile to point at your Pi. *Note: It's a good idea to add your dev machines public key to `~/.ssh/authorized_keys` to make the thing stop asking for passwords.

```
cd pi-color-server
make
```


## Existing project notes

This project is a socket server that takes space delimitated strings of 24 bit hex colors and 
dumps those colors out over the raspberry pi's SPI bus to the Total Control Lighting addressable 
LED chains.

Also included is a Processing app that will connect to the raspberry pi and cycle through HSB
colors as quickly as it can based on the Hue cycling from 0-360 degrees.

This project depends on the wiringPi libraries by Gordon Drogon. Thanks Gordon!

Some of the code was also ripped from the Total Control Lighting Arduino TCL library. Since TCL 
doesn't tell it's customers what chipset is being used in their products, this was the only sure
way to get the code working correctly.


## Requirements

Install WiringPi first:

```
git clone git://git.drogon.net/wiringPi
./build
```

## Build


Build on the Pi itself:

```
make
```

Transfer all files to the Pi using SCP THEN build on the pi via SSH:

```
make -f Makefile.scp
```

## Running it


TCL Mode:

```
./socket_server 
```

WS2801 Mode:

```
./socket_server 
```
