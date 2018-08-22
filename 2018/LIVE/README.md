# 2018 Sensatron - Raspberry PI 

Stuff I'm sending to playa:

- 2x Lasagnatron replacements (power supplies and cables)
- 2x Raspberry Pi 3 B+'s in cases (one red, one blue)
- 2x 32GB SD cards flashed with working systems already flashed to them
- 1x Mono USB sound card (for last ditch effort to get a raspberry pi to boot. See note [USB Sound Card Bug](#usb-soundcard-bug) below!)
- 6+ - TCL power+data Tee's
- 12+ - TCL data cable extensions
- 12+ - Power cable extensions


## USB Sound Card Bug

The Sensatron code WILL NOT WORK without a USB sound card plugged into one of the USB ports!

## WiFi Access Point names and settings

About WiFi: One of them provides a WiFi access point named Sensatron, the other provides a WiFi access point named Luminous. No password on either because that was a pain in the butt in the past.

## About the SD Card

Plugging the SD card into another computer should provides access to a partition on the SD card named "CODE". It contains three things:

1. Configuration files that should NOT BE TOUCHED BEFORE THE BURN unless I say so
2. A directory named Sensatron that contains a complete copy of the GitHub repo we've been using
3. Instructions on how to update the code if we need to update the server, interface, built in animations, or some other thing

## `CODE` parition

The CODE partition of this SD card contains most of the configuration and source code for running the Sensatron LED server.

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
