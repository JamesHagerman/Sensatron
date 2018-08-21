# Sensatron Raspberry PI

The CODE partition of this SD card contains most of the configuration and source code for running the Sensatron LED server.

This SD card image will try to start the audio reactive Sensatron animation server as well as provide a WiFi Access point named something like `Sensatron` or `Luminous`. (There is no password on the WiFi because that has given us problems in the past).

Once someone is connected to the WiFi access point, they should be able to go to ANY URL in their devices web browser to access the LED Interface.

Once you have accessed the LED Interface, you can access the admin panel by adding `/admin.html` to the end of whatever URL you used to access the LED Interface itself.

## Updating code on this SD card

In short, *DO NOT TOUCH* anything other than the `Sensatron/` directory!

You can get the latest version of the `Sensatron` code in one of two ways:

## Easy Update

1. Grab the zip file from this url: (https://github.com/JamesHagerman/Sensatron/archive/master.zip)[https://github.com/JamesHagerman/Sensatron/archive/master.zip]

2. Extract it so it is a single directory named `Sensatron`

3. Overwrite the contents of the `Sensatron` directory on this SD card with your unzipped content

## Difficult Update

1. On a machine with Git installed (either the Raspberry Pi itself, or the machine the SD card is inserted into), do a `git pull` inside of the `Sensatron/` directory to get the latest code from the `master` branch

2. Restart the Sensatron Raspberry Pi

Enjoy!
