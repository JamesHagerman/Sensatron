# List of the hardware and who has it

We seem to have a bit of a time keeping track of all this things related and required hardware. So let's at least make a list of where it all is now

## James has

- Power supply lasanga pan with the spidery cables to send power to all of the light strands
  - This INCLUDES the little FTDI Breakout board that takes in USB and bitbangs the colors out to the lights.
  - We will probably want to rebuild this thing to fit the car a bit better including the Intel NUC mini-PC.
  - I will find the power supply specs so we know how many more lights we can support without adding a second supply.
  - The spider needs to be rebuilt to support the TCL T-connectors instead of the hackjob we did on playa last year.
- Existing "Active" 32 foot USB cable [Sabrent 32-foot USB 2.0 Active Extension Cable (CB-USBXT)](https://www.amazon.com/Sabrent-32-foot-Active-Extension-CB-USBXT/dp/B002SB7K3E)
- Existing, yellowish touchscreen Sensatron computer!
  - 1x Power supply
  - 1x USB Hub
  - 1x USB Sound card that was never stable enough to deploy on playa.
- Existing backup Sensatron computer
  - 1x Power supply
  - 1x Plastic case
- 2x TCL lights
  - 1x working and is being used for testing
  - 1x NEEDS A NEW INPUT CONNECTOR!!!!
- 1x FTDI-to-TCL-Light-strand connector
  - This is used for testing the [FTDI D2XX driver](http://www.ftdichip.com/Drivers/D2XX.htm)
  - I will use this to test the Processing-to-Lights interface as it is similar to what is embedded in the power supply pan
- 1x 5v, 2Amp wallwort power supply that can run two light strands
- 1x 5v, 1Amp wallwart power supply that can run one light strand
- 1x remains of the yellow football!
  - Honestly, not sure of the status on this. If we are using mobile devices instead, this is probably useless except for being a robust, "let Tomato toss it around for a while" solution.
  - Probably still has it's bluetooth link
  - Might still have it's LiPo battery charger
  - Might still have it's Arudino mini
  - I know I still have it's 9DoF sensor...
- The old Arduino rig which consists of:
  - 1x Arduino UNO clone
  - 1x TCL Lighting shield
  - 1x Sparkfun audio input shield rewired to work in tandem with the TCL shield (code exists for this somewhere. Ask james)
  - I have an SD Card shield floating around but it's slow and may not work with the rest of the shields without considerable work...

## MISSING parts

Most of these are most likely in Jason's storage unit. Ones that MIGHT be on James' porch will be mentioned.

- The T-connectors
- The Intel NUC board
- Original 25 foot "Active" USB extension cable
- Original 25 foot TCL Lighting cable (This is the only one that James might have in this list)
- Perhaps connectors to repair the rest of the lights
- Any new lights...
