# Power and LED signal distribution system

This directory holds all of the stuff needed to send power and LED control signals to the pixel strands. Mostly, it's a collection of strange and hard to find versions of the `p9813` library and FTDI drivers.

It's also supposed to be where I document how the power board works.

## Install directions

Each OS/Architecture uses a different version of this silly FTDI driver. The `orange_box_install.sh` script should help explain how to install the FTDI drivers and other stuff.

... Check it before you run it...

### x86_64 Linux

The Intel Nuc and x86_64 Ubuntu development machines.

### ARM

Either Hard Float or Soft Float. Check with `dpkg --print-architecture`

#### HF

The Raspberry Pi 3 is Hard Float. 

#### SF

I think the BBB was using this at some point...?

## Historical name

Because this thing started off it's life living in a plastic, orange tool box, that's what I called it for a while.

It's currently living in a lasagna pan, so Jason has been calling it `Lasagnatron`

The next iteration will be a little more reasonable hopefully...

## Electrical design

The `orange_box`/`Lasagnatron` hardware itself consists of three main parts:

1. A 5volt power supply
2. A Sparkfun FTDI breakout board
3. The board itself that wires everything together

The current iteration needs more than that to hook up to the lights though:

1. TCL Connectors to get data from the board to the LED strands
2. TCL "T"-connectors to inject more power between every strand
3. Raw power taps off of the PCB to inject power into the "T"'s from the common power supply.

And it also needs some other stuff to hook up to the control computer itself:

1. A USB Micro cable
2. A powered USB extension cable
3. A computer that can run the custom FTDI Bitbang driver instead of the normal FTDI UART serial driver

### PCB Design

The first iteration of this thing was a hand soldered hack. It still works but will be difficult to replicate when it dies.

So, I threw together an actual PCB design for the board using Upverter:
(https://upverter.com/JamesHagerman/8e72ed68639d07fa/Lasagnatron-2018/)[https://upverter.com/JamesHagerman/8e72ed68639d07fa/Lasagnatron-2018/]

And had some built via OSHPark:
https://oshpark.com/shared_projects/DYC0Qxst

I've assmbled one of the boards, and am waiting on LEDs to finish testing it.


## Future designs

In the future, the design needs to made more simple if it can be. Here are a few notes about that:

### Ditch FTDI

In the future, the best thing we can do is remove the FTDI requirement from the project. The chips are expensive, the driver is a pain in the ass, and it limits us to 8 strands of LEDs (and even that requires a fair amount of custom hacking. Otherwise, the limit is 7 strands and a clock.)

So, doing away with the FTDI means two things:
1. Having to build a new computer-to-board interface
2. Having to write a new libarary that wraps that interface into Java

### Ditch USB cable for WiFi links.

Use TCP links for everything. WiFi is everywhere and the server already supports TCP connections. So do most devices.

Instead of the Nuc's Java app loading the custom `p9813` library, it could just send raw TCP packets to a variety of hosts connected to it's WiFi access point. That would mean that the lights connect to the Nuc, instead of the Nuc connecting to the Lights.

More importantly, that would mean connecting multiple LED devices to a common Java server REGARDLESS of what kind of LEDs are used at the end of the day. 

So, necklaces? Shoes? Other art cars? More LEDs on the Sensatron?

They'd all become fair game.

And that's good!


## TCL Pinout

I can never seem to find this so I am putting it here again...

Looking at female threaded output, with referencing tab pointing up, the pins are as follows:

```
Female threads, female pin sockets, male connector:
    |
GND   SCK

MOSI  5v
```
