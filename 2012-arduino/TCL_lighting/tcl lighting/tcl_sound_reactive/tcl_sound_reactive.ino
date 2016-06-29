#include <SPI.h>
#include <TCL.h>
#include <math.h>

const int WAND_COUNT = 2; 
const int WAND_LENGTH = 25; // 50 LEDs per strand
const int LEDS = WAND_COUNT * WAND_LENGTH; // total led count on all strands

int programId = -1;

// This is the array that gets sent to all strands:
int current_color[LEDS]; // This array stores an integer for each LED with its current color
byte real_current_color[LEDS][3];
byte double_back[LEDS][3];
byte fade_color[3];

// OSC setup:
double angle = 0.0;
unsigned long phase_time = 0; // between 0 and 5 seconds...

// Audio detection stuff:
int audioPinL = A4;
int audioPinR = A5;
int audioValue; // value of the audio pin
double audioCurve[WAND_LENGTH]; // Adjustment table for audio spectrum

/*
  Coordinate system setup
  For each strand we need to convert to the double back array layout
  We need a radius (r) to store where on the strand we should draw
    This should be between 0 and STRAND_LENGTH
*/
int r;

/*
  We need an angle (t) to store where on the circle we should draw
    this should be between 0 and STRAND_COUNT
*/
int t;

/*
  Function work:
  r = 3      circle radius 1
  r = t      spiral
  r = sin(t) offset circle radius 1
*/
//               r             t        z
byte coord[WAND_COUNT][WAND_LENGTH][3]; // array for holding coordinates and colors of pixels


/*
  animations on strand
*/


/*
  effect picker
*/
int effect_count = 2; // knob makes 512 or so effects... probably 255 really...
 


void setup() {
  int i;
  unsigned long time;
  
  // Turn on Serial port:
  Serial.begin(9600);
  Serial.println("");
  Serial.println("LED sequencing written by James Hagerman");
  
  // Turn on TCL library:
  TCL.begin();

  // Black out all of the LEDs on boot:
  TCL.sendEmptyFrame();
  for(i=0;i<LEDS;i++) {
    TCL.sendColor(0x00,0x00,0x00);
  }
  TCL.sendEmptyFrame();
  // End Blackout block
  
  time = millis(); // Find the current time
  buildAudioSpread(); // build the audio value lookup table
}
// END SETUP



/*
  This version of the update_strand function uses one of the following two arrays as colors on the strand.
  real_current_color  - the values that would get spit to a single string laid out lengthwise in one piece
  double_back         - calculated inside setDoubleBack() from the values in real_current_color
  
  This must be modified to support a radial coordinate system and multiple wands.
*/
void update_strand_real() {
  int i;
  int color;
  
  // call double-back code:
  boolean double_back_on = true;
  if(double_back_on) {
    setDoubleBack();
  }
  
  TCL.sendEmptyFrame();
  for(i=0;i<LEDS;i++) {
    if(double_back_on) {
      TCL.sendColor(double_back[i][0],double_back[i][1],double_back[i][2]);
    } else {
      TCL.sendColor(real_current_color[i][0],real_current_color[i][1],real_current_color[i][2]);
    }
  }
  TCL.sendEmptyFrame();  
}


/*
  We are doubling back a single strand on each wand to double up led's.
  This function should take a wands array and modify it for this double back.
  
  Currently it's only modifying the existing output array for one strand (double_back is being spit to the strand).
*/
void setDoubleBack() {
  int i, j, adj_index, even_loc, odd_loc;
//  for(i=0;i<WAND_LENGTH;i++) {
//    if(i%2==0) { // even led's (0,2,4,6...)
//      even_loc = i-(i/2);
//      double_back[even_loc][0] = real_current_color[i][0];
//      double_back[even_loc][1] = real_current_color[i][1];
//      double_back[even_loc][2] = real_current_color[i][2];
//    } else { // odd led's (1,3,5,7...)
//      odd_loc = LEDS-(i-(i/2));
//      double_back[odd_loc][0] = real_current_color[i][0];
//      double_back[odd_loc][1] = real_current_color[i][1];
//      double_back[odd_loc][2] = real_current_color[i][2];
//    }
//  }
  
  for(i=0; i<WAND_COUNT; i++) {
    for(j=0;j<WAND_LENGTH;j++) {
      adj_index = (WAND_LENGTH*i) + j; // replaces j in final location caluculation for double_back array
      if(j%2==0) { // even led's (0,2,4,6...)
        even_loc = adj_index-(adj_index/2);
        double_back[even_loc][0] = real_current_color[j][0];
        double_back[even_loc][1] = real_current_color[j][1];
        double_back[even_loc][2] = real_current_color[j][2];
      } else { // odd led's (1,3,5,7...)
        odd_loc = (WAND_LENGTH*(i+1))-(adj_index-(adj_index/2));
        double_back[odd_loc][0] = real_current_color[j][0];
        double_back[odd_loc][1] = real_current_color[j][1];
        double_back[odd_loc][2] = real_current_color[j][2];
      }
    }
  }
}


/* 
  Audio is read at values between 0 and 1023. This needs to be spread out over a good range of the LED strand to get a reactive curve
  Linear is no good but it's a start.
*/
void buildAudioSpread() {
  int i;
  double adjustment_function;
  for(i=0;i<WAND_LENGTH;i++) {
//    adjustment_function = (i+1)*(1023.0/STRAND_LENGTH);
    //adjustment_function = pow(i+1, 2)/2.44379; // x^2/2.4: f(0) = 0, f(50) = 1023
    // But really, the analog input doesn't hit 1023 and stick with our current draw at 5 volts . so lets calc for 1020 max input (1023-3)...
    adjustment_function = pow(i+1, 2)/(pow(WAND_LENGTH, 2)/(1023-3)); // x^2/2.4: f(0) = 0, f(50) = 1023
    Serial.print("Setting led ");
    Serial.print(i);
    Serial.print(" to ");
    Serial.println(adjustment_function);
    audioCurve[i] = adjustment_function;
  }
}


void buildCoords() {
  int i, j;
  for(i=0;i<WAND_COUNT;i++) { // for each wand
//    Serial.print("wand: ");
//    Serial.println(i);
    for(j=0;j<WAND_LENGTH;j++) { // for each LED
//      Serial.print(" led: ");
//      Serial.print(j);

      // Set all LEDs to fade color
      coord[i][j][0] = fade_color[0];
      coord[i][j][1] = fade_color[1];
      coord[i][j][2] = fade_color[2];
      
      // Set a circle at 5 leds out from the center to fade:
      if (j == 5) {
      
      // Set a circle between 5 and 10 leds out from the center to fade:  
      //if (j > 5 && j <= 10) {
        coord[i][j][0] = fade_color[0];
        coord[i][j][1] = fade_color[1];
        coord[i][j][2] = fade_color[2];
      } else {
        coord[i][j][0] = 0x00;
        coord[i][j][1] = 0x00;
        coord[i][j][2] = 0x00;
      }
      
//      Serial.print(" idx: ");
//      Serial.println((STRAND_LENGTH*i) + j);
    }    
  }
  
  // Dump coordinate system array into real draw array
  for(i=0;i<WAND_COUNT;i++) { // for each wand
    for(j=0;j<WAND_LENGTH;j++) { // for each LED
      real_current_color[(WAND_LENGTH*i) + j][0] = coord[i][j][0];
      real_current_color[(WAND_LENGTH*i) + j][1] = coord[i][j][1];
      real_current_color[(WAND_LENGTH*i) + j][2] = coord[i][j][2];
    }    
  }
  
}


void loop() {
  int i;
  unsigned long time;
  time=millis();

  int blink_speed = (analogRead(TCL_POT2) + analogRead(TCL_POT2)) >> 1;
  int brightness = 255 * (analogRead(TCL_POT3)/1023.0); // between 0.0 and 255.0 
  int programPick = 10 * (analogRead(TCL_POT1)/1023.0);
  
  double audioCutoff = 1023.0 * (analogRead(TCL_POT4)/1023.0);
  
  /*
    Get audio input value.
    
    This needs to be expanded to support the shield and the bands.
  */
  audioValue = analogRead(audioPinL);
//  Serial.print("Audio value: "); 
//  Serial.println(audioCutoff);

  
  // Update base phase angle
  angle += blink_speed/1023.0 * 100.0;
  if(angle > 360) {
    angle = angle - 360;
  }
  if(angle < 0) {
    angle = angle + 360; 
  }
  //Serial.println(angle);
  
  // Use updated base phase angle to calculate new RGB values:
  double r, g, b;
  r = ((sin((angle + 120)*(M_PI/180)) + 1)/2.0)*brightness;
  g = ((sin((angle )*(M_PI/180)) + 1)/2.0)*brightness;
  b = ((sin((angle + 240)*(M_PI/180)) + 1)/2.0)*brightness;
//  Serial.print(angle);
//  Serial.print(", ");
//  Serial.print(r);
//  Serial.print(", ");
//  Serial.print(g);
//  Serial.print(", ");
//  Serial.print(b);
//  Serial.print("\n");

  fade_color[0] = r;
  fade_color[1] = g;
  fade_color[2] = b;
  
  
  // Select which program to run. Fading between programs would be cool...
  if (programPick < 5) {
    if (programId == 1 || programId == -1) {
      Serial.println("Switching to polar coordinate system");
      programId = 0;
    }
    
    // Do coordinate drawing
    buildCoords();
    
  } else {
    if (programId == 0 || programId == -1) {
      Serial.println("Switching to simple fade system");
      programId = 1;
    }
    
    // All led's 
//   Serial.println(audioCutoff);
    double cutOffValue = audioCutoff; // audioValue
    for(i=0; i<LEDS; i++) {
  //    Serial.print(i);
  //    Serial.print(": ");
  //    Serial.println(i%2);
     
      // Set color down the strand:
      if(audioCurve[i] <= cutOffValue) { // audioValue
          // add white runner
  //      if(audioCurve[i-1] > cutOffValue  || audioCurve[i+2] > cutOffValue) { // audioValue
  //        real_current_color[i][0] = 0xff;
  //        real_current_color[i][1] = 0xff;
  //        real_current_color[i][2] = 0xff;
  //      } else {
          real_current_color[i][0] = fade_color[0];
          real_current_color[i][1] = fade_color[1];
          real_current_color[i][2] = fade_color[2];  
  //      }
        
        // Set all leds to white
  //      real_current_color[i][0] = 0xff;
  //      real_current_color[i][1] = 0xff;
  //      real_current_color[i][2] = 0xff;
      } else {
        // Set the overage leds to black
  //      real_current_color[i][0] = 0x00;
  //      real_current_color[i][1] = 0x00;
  //      real_current_color[i][2] = 0x00;
        
        // Fade out the overage leds - YEAH GLITCH FEATURES!
  //      real_current_color[i][0] = real_current_color[i][0]-0x01;
  //      real_current_color[i][1] = real_current_color[i][1]-0x01;
  //      real_current_color[i][2] = real_current_color[i][2]-0x01;
        
        // Bit shift to zero
  //      real_current_color[i][0] = real_current_color[i][0] >> 1;
  //      real_current_color[i][1] = real_current_color[i][1] >> 1;
  //      real_current_color[i][2] = real_current_color[i][2] >> 1;
        
        // Zero cross check fade
        byte fade_amount = 0x01;
        if(real_current_color[i][0] == 0 || real_current_color[i][0] > 250) {
          real_current_color[i][0] = 0x00;
        } else {
          real_current_color[i][0] = real_current_color[i][0]-fade_amount;
        }
        if(real_current_color[i][1] == 0 || real_current_color[i][0] > 250) {
          real_current_color[i][1] = 0x00; 
        } else {
          real_current_color[i][1] = real_current_color[i][1]-fade_amount;
        }
        if(real_current_color[i][2] == 0 || real_current_color[i][0] > 250) {
          real_current_color[i][2] = 0x00; 
        } else {
          real_current_color[i][2] = real_current_color[i][2]-fade_amount;
        }
  
      }
    }
  } // end program pick
  
  update_strand_real();
}
