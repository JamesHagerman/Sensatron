# Raspberry Pi Sensatron Server Image Directions

Instead of using the Nuc, the Raspberry Pi 3 is plenty fast enough for the task of getting colors to the lights.

We've already started the move towards sending color over network protocols, so this will be the next major step.

## Advantages of the new Raspberry Pi

The old Raspberry Pis were just too slow. The new ones, while they still have the chance of an SD card going bad, are cheaper, and faster than most other options out there.

And they're easy to image as well!

The following directions are the rough steps to building a Raspberry Pi image based off of the standard Raspbian Linux distro provided for the Pi. 

At the end of these steps, you should have a pretty minimal but pretty standard Rasperry Pi image that can be flashed to new SD cards and just WORK as replacement Sensatron Color Servers.

## Getting the basic image configured

We have to get some of the basics out of the way first. This first chunk of directions will build you a niiiiice Raspberry Pi image for lots of other projects besides just Sensatron stuff...

1. Download Raspbian Stretch Lite:
https://www.raspberrypi.org/downloads/raspbian/

2. Write to sd card:

```
sudo dd bs=4M if=2017-11-29-raspbian-stretch-lite.img of=/dev/sdb status=progress conv=fsync
```

3. Mount first partition, edit boot command to remove auto partition resize:

```
sudo mkdir /mnt/tempmount
sudo mount /dev/sdb1 /mnt/tempmount
cd /mnt/tempmount 
sudo vim cmdline.txt
```

Remove this: `init=/usr/lib/raspi-config/init_resize.sh`

4. Edit config.txt in that same directory to enable spi:

```
sudo vim config.txt
```

uncomment: `dtparam=spi=on`

5. Unmount the first partition and **consider making a backup of this**

```
sudo umount /mnt/tempmount
```

On the backup machine:
```
sudo dd bs=4M if=/dev/sdb of=rpi-minimal-spi.img status=progress conv=fsync
truncate --reference 2017-11-29-raspbian-stretch-lite.img rpi-minimal-spi.img
```

6. Boot the SD card in a pi
7. Login:

```
username: pi
password: raspberry
```

8. Run `raspi-config` and change some stuff:

`sudo raspi-config`

```
2. Network Options:
    -> Set a Hostname (sensatron-pi)
    -> Set up WiFi (SSID and Passphrase... Temporary; We need internet)
    -> Enable predictible network interface names
4. Localization Options:
    -> Change Keyboard Layout: 
        -> Generic 104-key ** NOT Intl**
        -> Other
        -> English (US)
        -> English (US)
        -> The default for the keyboard layout
        -> No compose key
5. Interfacing Options
    -> P2 SSH (Enable ssh...)
        -> Yes
    -> P4 SPI (SPI should have already been enabled... but just check!)
        -> Yes
8. Update
    -> Update the raspi-config just in case... If nothing else, a wifi test!
```

9. Maybe shut down and image the SD card again...... lol

On the backup machine:
```
sudo dd bs=4M if=/dev/sdb of=rpi-configured.img status=progress conv=fsync
sudo truncate --reference 2017-11-29-raspbian-stretch-lite.img rpi-configured.img
```

You should now have a nice, configured `rpi-configured.img` file that can be modified for whatever custom builds you want to make

## Focusing the image

Now to the actual WORK part of this: Getting it to do something useful.

1. Make sure you run `ssh-keygen` so you can put a key in GitHub. **Keep in mind how this will be saved into the images...**

2. Install obvious stuff

```
sudo apt install vim git
```

3. Install WiringPi

```
git clone git://git.drogon.net/wiringPi
cd wiringPi
./build
```

4. Configure `top` to look good:

```
top
zxcbmms1<enter>W
```

### Sensatron specific stuff

Next, get the Sensatron specific stuff installed:


1. Grab useful code repos:

```
git clone https://github.com/JamesHagerman/Sensatron.git
git clone https://github.com/JamesHagerman/rpi-tcl-server-p9813.git
```

2. Install the FTDI driver:

```
cd Sensatron/hardware-interface/
tar -zxvf FTDI_Hack.tar.gz

```




## Clone SD card to .img file:

Really, you only need to let `dd` copy about as much as the end of the userland partition. You can then use truncate to shorten everything up to a reasonable size.

```
sudo dd bs=4M if=/dev/sdb of=rpi-minimal-spi.img status=progress conv=fsync
truncate --reference 2017-11-29-raspbian-stretch-lite.img rpi-minimal-spi.img
```

Once you've backed it up, it's a good idea USING it to make sure it works...

```
sudo dd bs=4M if=rpi-minimal-spi.img of=/dev/sdb status=progress conv=fsync
```





