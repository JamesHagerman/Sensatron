# 2018 Sensatron - Raspberry PI 

Stuff I'm sending to playa:

- 2x Lasagnatron replacements (power supplies and cables)
- 2x Raspberry Pi 3 B+'s in cases (one red, one blue)
- 2x 32GB SD cards flashed with working systems already flashed to them
- 2x Apple 12W USB Power adapters
- 4x USB Micro cables (2x for power the Pi's, 2x for data into the board on the replacement Lasagnatrons
- 1x Mono USB sound card (for last ditch effort to get a raspberry pi to boot
- 6+ - TCL power+data Tee's
- 12+ - TCL data cable extensions
- 12+ - Power cable extensions

Stuff I expect to be provided by someone else, on playa:

- 2x long USB extension cables to go from Nuc/Raspberry Pi "Brains" up to the Lasaganatrons
- 6+ TCL Power+Data Tee's
- Some amount of TCL strands
- Power! Both to the Brains in the cockpit and to the Lasagnatrons near the lights!

## Lasagnatron Replacements

The original Lasagnatron and the two new replacements should be entirely interchangeable.

The replacements have 7x TCL data pigtails and 7x power plugs. The final and extra should not be used this year. They are for later.

*Note: Keep in mind that we have no done a clean load test on these new Lasagnatron replacements with all LEDs hooked up and on full white! Keep an eye on the power supply and how much heat things are giving off for the first Full Light Night!*

*Note: One of the supplies says 30A instead of 40A. I do not know why. The seller said they're both 40A. You may want to be careful with that 30A one and use it on Luminous instead of Sensi just in case...*

## Brain Replacements

The existing Nuc and the two new Raspberry Pi's should be mostly interchangeable except for these differences:

1. The WiFi access points are different (I don't remember if it's the Red or Blue one that's Luminous...):
  - Nuc -> AP Name: `Sensatron`, Security: I have no memory of this. Sorry...
  - Raspberry Pi #1 -> AP Name: `Sensatron`, Security: none! No password! Just connect
  - Raspberry Pi #2 -> AP Name: `Luminous`, Security: none! No password! Just connect
2. The Raspberry Pi's do NOT need an external WiFi adapter!
3. The Raspberry Pi's have the same code on them! But the Nuc has different code on it! I do not know how they differ. Sorry

The brains are the same in these ways:

1. They *NEED USB SOUND CARDS FOR THE CODE TO WORK!!* I am sending along my last, shitty USB Sound Card as a last ditch effort if the other 2x sound cards disappear.
2. They depend on having Airplane mode on!
3. They should let anyone connected to the WiFi access points to go to random urls in the browser and see the Control Interface.
2. They should *ALL* be shut down cleanly via the "Shutdown" button in the `/admin.html` page. You risk corrupting the drives otherwise
3. *THE BRAINS SHOULD BE POWERED ON BEFORE THE LASAGNATRONS!* Weird shit may happen otherwise...

## About the SD Card

Plugging the SD card into another computer should provides access to a partition on the SD card named "CODE". It contains three things:

1. Instructions similar to these about how to update the code
2. Configuration files that should NOT BE TOUCHED BEFORE THE BURN unless I say so
3. A directory named `Sensatron` that contains a complete copy of the GitHub repo we've been using

### Configuration files

The SD card's `CODE` partition contains most of the configuration files for the Sensatron LED server.

Specifically, the `start-server.sh` file will actually start the Maven/Java server code itself. This is called by the systemd `sensatron` service.

This SD card image will try to start the audio reactive Sensatron animation server as well as provide a WiFi Access point named something like `Sensatron` or `Luminous`. (There is no password on the WiFi because that has given us problems in the past).

Once someone is connected to the WiFi access point, they should be able to go to ANY URL in their devices web browser to access the LED Interface.

Once you have accessed the LED Interface, you can access the admin panel by adding `/admin.html` to the end of whatever URL you used to access the LED Interface itself.

### Updating code on this SD card

In short, *DO NOT TOUCH* anything other than the `Sensatron/` directory!

You can get the latest version of the `Sensatron` code in one of two ways:

#### Easy Update

1. Grab the zip file from this url: (https://github.com/JamesHagerman/Sensatron/archive/master.zip)[https://github.com/JamesHagerman/Sensatron/archive/master.zip]

2. Extract it so it is a single directory named `Sensatron`

3. Overwrite the contents of the `Sensatron` directory on this SD card with your unzipped content

#### Difficult Update

1. On a machine with Git installed (either the Raspberry Pi itself, or the machine the SD card is inserted into), do a `git pull` inside of the `Sensatron/` directory to get the latest code from the `master` branch

2. Restart the Sensatron Raspberry Pi

Enjoy!
