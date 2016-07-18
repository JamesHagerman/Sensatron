/**
 * Blink
 * 
 * Simple "blinkenlights" program for the TotalControl (p9813) library.
 * Flashes one random LED at a time.
 */

/*
 * You'll need to use one of these commands to get the correct kernel drivers 
 * inserted into the kernel for the D2XX bitbanging the TotalControl library
 * requires....
 *
 * Note: Run the commands AFTER you plug in the FTDI... otherwise the FTDI
 * driver will be loaded
 * 
 * unload (to use the bitbang driver for the TCL lights):
 *   modprobe -r ftdi_sio
 *   modprobe -r usbserial
 * load (to use the normal FTDI serial driver):
 *   modprobe ftdi_sio
 *   modprobe usbserial
 */

import TotalControl.*;

// The TotalControl processing library doesn't define the FTDI pins so we do:
short TC_FTDI_TX  = 0x01;  /* Avail on all FTDI adapters,  strand 0 default */
short TC_FTDI_RX  = 0x02;  /* Avail on all FTDI adapters,  strand 1 default */
short TC_FTDI_RTS = 0x04;  /* Avail on FTDI-branded cable, strand 2 default */
short TC_FTDI_CTS = 0x08;  /* Avail on all FTDI adapters,  clock default    */
short TC_FTDI_DTR = 0x10;  /* Avail on third-party cables, strand 2 default */
short TC_FTDI_DSR = 0x20;  /* Avail on full breakout board */
short TC_FTDI_DCD = 0x40;  /* Avail on full breakout board */
short TC_FTDI_RI  = 0x80;  /* Avail on full breakout board */

TotalControl tc;
int          STRANDS        = 1,
             STRAND_LENGTH = 50;
int[]        p               = new int[STRANDS * STRAND_LENGTH];
int[] remap;
/* Could also use PImage and loadPixels()/updatePixels()...many options! */

void setup() 
{
  
  // Override the default pin outs:
  // This is clock. We don't want to override it:
  //tc.setStrandPin(x,TC_FTDI_CTS);
  tc.setStrandPin(0,TC_FTDI_TX); // default
  tc.setStrandPin(1,TC_FTDI_RX); // default
  tc.setStrandPin(2,TC_FTDI_DTR); // spliting dtr and rts
  
  // Custom lines for the ftdi breakout
  tc.setStrandPin(3,TC_FTDI_RTS);
  tc.setStrandPin(4,TC_FTDI_RI);
  tc.setStrandPin(5,TC_FTDI_DSR);
  tc.setStrandPin(6,TC_FTDI_DCD);
  
  int status = tc.open(STRANDS, STRAND_LENGTH);
  if(status != 0) {
    tc.printError(status);
    exit();
  }
  
  // This will build the Remapping array that will
  // wrap the LED strands back on themselves for 
  // the higher density the Sensatron loves so much:
  remap = new int[STRANDS * STRAND_LENGTH];
  buildRemapArray();
}

void draw()
{
  // Set some random pixel to full white:
  int x = (int)random(STRANDS * STRAND_LENGTH);
  p[x]  = 0x00ffffff;
  
  // Draw the p array to the lights using the remap function:
  tc.refresh(p, remap); 
  
  // Set the random pixel back to black for the next pass:
  p[x]  = 0;
}

void exit()
{
  tc.close();
}


public void buildRemapArray() {
  println("Building remap array...");
  // Linear mapping - No fold back:
  //for (int i = 0; i < STRANDS * STRAND_LENGTH; i++) {
  //  remap[i] = i;
  //}

  // All 0 mapping - Only touch the... uh... first led? I think?
  //for (int i = 0; i < STRANDS * STRAND_LENGTH; i++) {
  //  remap[i] = 0;
  //}

  // Working radial remap:
  int index = 0;
  for(int i=0; i<STRANDS; i++) {
    println("Setting wand: " + i);
    for(int j=0;j<STRAND_LENGTH;j++) {
      if(j%2==0) { // even led's (0,2,4,6...)
        remap[j-(j/2) + (STRAND_LENGTH * i)] = index;
//          if (i == 1) {
//            println("index " + index + " is: " + remap[index]);
//          }
      } else { // odd led's (1,3,5,7...)
         remap[(STRAND_LENGTH * (i+1)) - (j-(j/2))] = index;
//          if (i == 1) {
//            println("index " + index + " is: " + remap[index]);
//          }
      }
      index += 1;
    }
  }
  
  println("Done building remap array.");
}