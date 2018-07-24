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
>7. Advanced Options
>   -> Memory Split
>        -> 256 (If you're doing any graphics ON HDMI)
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

### Flash your nice new card image:

```
sudo dd bs=4M if=sensatron-color-server.img of=/dev/sdb status=progress conv=fsync
```

## Managing image size

Between Java being huge and needing to have a third partition for software updates, it's a good idea to have better control over the size of our images.

This is what `fdisk -l` spit out for the img file itself:

```
fdisk -l sensatron-color-server-v2.img
Disk sensatron-color-server-v2.img: 1.7 GiB, 1858076672 bytes, 3629056 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes
Disklabel type: dos
Disk identifier: 0x37665771

Device                         Boot Start     End Sectors  Size Id Type
sensatron-color-server-v2.img1       8192   93236   85045 41.5M  c W95 FAT32 (LBA)
sensatron-color-server-v2.img2      94208 3629055 3534848  1.7G 83 Linux
```

After flashing that to the SD card, we can use `fdisk -l` to determine the sector size of what is now on the SD card:

```
$ fdisk -l /dev/sdb
sudo fdisk -l /dev/sdb
Disk /dev/sdb: 29.8 GiB, 32010928128 bytes, 62521344 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes
Disklabel type: dos
Disk identifier: 0x37665771

Device     Boot Start     End Sectors  Size Id Type
/dev/sdb1        8192   93236   85045 41.5M  c W95 FAT32 (LBA)
/dev/sdb2       94208 3629055 3534848  1.7G 83 Linux
```

Since it's 512, you then know how to tell `dd` exactly what you're trying to clone. In this case, we're trying to copy until the end of the disk... i.e. the last partition:

```
sudo dd if=/dev/sdb of=sensatron-color-server-v2-test.img bs=512 count=3629055 status=progress conv=fsync
```

Running that will give us an sd card image that looks very much like the original image. In this case though, the original image had one extra 512 byte sector in it... that was apparently not a part of the second partition...

```
fdisk -l sensatron-color-server-v2-test.img 
Disk sensatron-color-server-v2-test.img: 1.7 GiB, 1858076160 bytes, 3629055 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes
Disklabel type: dos
Disk identifier: 0x37665771

Device                              Boot Start     End Sectors  Size Id Type
sensatron-color-server-v2-test.img1       8192   93236   85045 41.5M  c W95 FAT32 (LBA)
sensatron-color-server-v2-test.img2      94208 3629055 3534848  1.7G 83 Linux
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





