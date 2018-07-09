# Raspberry Pi Sensatron Server Image Directions

Instead of using the Nuc, the Raspberry Pi 3 is plenty fast enough for the task of getting colors to the lights.

We've already started the move towards sending color over network protocols, so this will be the next major step.

## Advantages of the new Raspberry Pi

The old Raspberry Pis were just too slow. The new ones, while they still have the chance of an SD card going bad, are cheaper, and faster than most other options out there.

And they're easy to image as well!

The following directions are the rough steps to building a Raspberry Pi image based off of the standard Raspbian Linux distro provided for the Pi. 

At the end of these steps, you should have a pretty minimal but pretty standard Rasperry Pi image that can be flashed to new SD cards and just WORK as replacement Sensatron Color Servers.

## Create `rpi-minimal-spi.img`

This first chunk of directions will get you a tiny install image that's easy to back up.

The final image will be called: `rpi-minimal-spi.img`

### Download Raspbian Stretch Lite:
https://www.raspberrypi.org/downloads/raspbian/

### Write to sd card:

```
sudo dd bs=4M if=2017-11-29-raspbian-stretch-lite.img of=/dev/sdb status=progress conv=fsync
```

### Mount first partition, edit boot command to remove auto partition resize:

```
sudo mkdir /mnt/tempmount
sudo mount /dev/sdb1 /mnt/tempmount
cd /mnt/tempmount 
sudo vim cmdline.txt
```

Remove this: `init=/usr/lib/raspi-config/init_resize.sh`

### Edit config.txt in that same directory to enable spi:

```
sudo vim config.txt
```

uncomment: `dtparam=spi=on`

### Unmount the first partition

```
sudo umount /mnt/tempmount
```

### Create an image file from the modified SD card

```
sudo dd bs=4M if=/dev/sdb of=rpi-minimal-spi.img status=progress conv=fsync
truncate --reference 2017-11-29-raspbian-stretch-lite.img rpi-minimal-spi.img
```

## Boot `rpi-minimal-spi.img` -> Configure -> Create `rpi-configured.img` 

### Boot the SD card and login

```
username: pi
password: raspberry
```

### Run `raspi-config` and configure most of the settings:

```
sudo raspi-config
```

>```
>2. Network Options:
>    -> Set up WiFi (SSID and Passphrase... Temporary; We need internet)
>    -> Enable predictible network interface names
>4. Localization Options:
>    -> Change Keyboard Layout: 
>        -> Generic 104-key ** NOT Intl**
>        -> Other
>        -> English (US)
>        -> English (US)
>        -> The default for the keyboard layout
>        -> No compose key
>5. Interfacing Options
>    -> P2 SSH (Enable ssh...)
>        -> Yes
>    -> P4 SPI (SPI should have already been enabled... but just check!)
>        -> Yes
>8. Update
>    -> Update the raspi-config just in case... If nothing else, a wifi test!
>```

### Update Aptitude and Upgrade the system

```
sudo apt update && sudo apt upgrade
```

### Install the obvious stuff

```
sudo apt install vim git
```

### Configure `top` to look good:

```
top
zxcbmms1<enter>W
```

### Install WiringPi (If you need it...)

```
cd
git clone git://git.drogon.net/wiringPi
cd wiringPi
./build
```

### Remove any wifi settings

They are somewhere in `/etc/wpa_supplicant`

### Shutdown and clear history

```
sudo shutdown -h now & history -c
```

### Create an image of the configured install

*NOTE: Make sure you removed your WiFi credentials!*

```
sudo dd bs=4M if=/dev/sdb of=rpi-configured.img status=progress conv=fsync
sudo truncate --reference 2017-11-29-raspbian-stretch-lite.img rpi-configured.img
```

You should now have a nice, configured `rpi-configured.img` file that can be modified for whatever custom builds you want to make

## Boot `rpi-configured.img` -> Focus the image -> Create `<custom name>.img` file

Now to the actual WORK part of this: Getting it to do something useful.

### Run `raspi-config` and configure most of the settings:

```
sudo raspi-config
```

>```
>2. Network Options:
>    -> Set a hostname
>    -> Set up WiFi (SSID and Passphrase... Temporary; We need internet)
>```

### Generate SSH Key:

**Keep in mind how this will be saved into the images...**

```
ssh-keygen
```

### Configure git

**Keep in mind how this will be saved into the images...**

Put `~/.ssh/id_rsa.pub` into GitHub then configure the git client:

```
git config --global user.email "james.hagerman@gmail.com"
git config --global user.name "JamesHagerman"
```

### Sensatron specific stuff

Next, get the Sensatron specific stuff installed:


#### Grab useful code repos

```
git clone https://github.com/JamesHagerman/Sensatron.git
git clone https://github.com/JamesHagerman/rpi-tcl-server-p9813.git
git clone git@github.com:JamesHagerman/BBB-BM-Lights-2015.git
```

#### Maybe install Java?

```
 sudo apt-get update && sudo apt-get install default-jdk
```

#### FTDI and P9813, drivers and libraries

Install the FTDI driver, build the p9813 C library, build the p9813 Java library, and install the libraries

```
cd Sensatron/hardware-interface/
tar -zxvf FTDI_Hack.tar.gz
git clone git@github.com:PaintYourDragon/p9813.git
./orange_box_install.sh
```

#### Build the Color Server

**TBD**



### Remove anything you don't want in the images:

1. Git settings
2. Maybe: `~/.ssh/id_rsa` `~/.ssh/id_rsa.pub`
3. Any code you don't want to share
4. Terminal history?
5. WiFi passwords


### Finalize your SD card image:

```
sudo dd bs=4M if=/dev/sdb of=sensatron-color-server.img status=progress conv=fsync
truncate --reference 2017-11-29-raspbian-stretch-lite.img sensatron-color-server.img
```

## How to clone an SD card to a `.img` file:

Really, you only need to let `dd` copy about as much as the end of the userland partition. You can then use truncate to shorten everything up to a reasonable size.

```
sudo dd bs=4M if=/dev/sdb of=pi-sd-backup.img status=progress conv=fsync
truncate --reference 2017-11-29-raspbian-stretch-lite.img pi-sd-backup.img
```

Once you've backed it up, it's a good idea USING it to make sure it works...

```
sudo dd bs=4M if=pi-sd-backup.img of=/dev/sdb status=progress conv=fsync
```





