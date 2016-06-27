# List of the hardware and who has it

We seem to have a bit of a time keeping track of all this things related and required hardware. So let's at least make a list of where it all is now

## James has

- Power supply lasanga pan with the spidery cables to send power to all of the light strands
  - This INCLUDES the little FTDI Breakout board that takes in USB and bitbangs the colors out to the lights.
  - We will probably want to rebuild this thing to fit the car a bit better including the Intel NUC mini-PC.
  - I will find the power supply specs so we know how many more lights we can support without adding a second supply.
  - The spider needs to be rebuilt to support the TCL T-connectors instead of the hackjob we did on playa last year.
- 2x TCL lights
  - 1x working and is being used for testing
  - 1x NEEDS A NEW INPUT CONNECTOR!!!!
- 1x FTDI-to-TCL-Light-strand connector
  - This is used for testing the [FTDI D2XX driver](http://www.ftdichip.com/Drivers/D2XX.htm)
  - I will use this to test the Processing-to-Lights interface as it is similar to what is embedded in the power supply pan
- 1x 5v, 2Amp wallwort power supply that can run two light strands
- 1x 5v, 1Amp wallwart power supply that can run one light strand
- The old Arduino rig which consists of:
  - 1x Arduino UNO clone
  - 1x TCL Lighting shield
  - 1x Sparkfun audio input shield rewired to work in tandem with the TCL shield (code exists for this somewhere. Ask james)
  - I have an SD Card shield floating around but it's slow and may not work with the rest of the shields without considerable work...

## MISSING (Maybe in Jason's storage) parts include

- The T-connectors
- The Intel NUC board
- Perhaps connectors to repair the rest of the lights
- Any new lights...
