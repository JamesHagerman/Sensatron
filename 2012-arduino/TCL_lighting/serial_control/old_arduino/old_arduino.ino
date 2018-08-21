#include <SPI.h>
#include <TCL.h>
#include <math.h>

int i, j;

const int WAND_COUNT = 1;
const int WAND_LENGTH = 25 ; // 50 LEDs per strand
const int LEDS = WAND_COUNT * WAND_LENGTH; // total led count on all strands

// Cutoff Lookup tables
double linearCurve[WAND_LENGTH]; // Adjustment table for linear lookup
double audioCurve[WAND_LENGTH]; // Adjustment table for audio spectrum

// Arrays to hold all color values:
//byte current_color[LEDS][3]; // This array stores LED colors, three bytes each for rgb
byte current_red[LEDS];
byte current_green[LEDS];
byte current_blue[LEDS];

//byte double_back[LEDS][3]; // This array stores the position for the leds in the doubled back wands
byte double_back_red[LEDS];
byte double_back_green[LEDS];
byte double_back_blue[LEDS];

/*
  Coordinate system setup
  For each strand we need to convert to the double back array layout
  We need a radius to store where on the strand we should draw
    This should be between 0 and STRAND_LENGTH

  We need an angle to store where on the circle we should draw
    this should be between 0 and STRAND_COUNT
    
  Function work:
  r = 3      circle radius 1
  r = t      spiral
  r = sin(t) offset circle radius 1
*/
//byte coord[WAND_COUNT][WAND_LENGTH][3]; // array for holding coordinates and colors of pixels
byte coord_red[WAND_COUNT][WAND_LENGTH];
byte coord_green[WAND_COUNT][WAND_LENGTH];
byte coord_blue[WAND_COUNT][WAND_LENGTH];

int coord_radius;
double coord_angle_size = 360.00 / WAND_COUNT; // Number of degrees between each wand
double coord_angle = 0.0; // Angle to increment by coord_angle_size for each wand so math can be done

// Coord drawing variables
double anim_angle = 0.0;		// cycles from 0-360 degrees at a variable rate. Use this for animations
double radiusCutoff = 0.0;	// draw cut off for some functions

// OSC setup:
unsigned long time;
double fade_angle = 0.0;
unsigned long phase_time = 0; // between 0 and 5 seconds...
double r, g, b; // temp storage for cacluating rgb fade values;
byte fade_color[3]; // holds the current rgb byte values for the fading color cyclers

// Analog inputs:
int programPick;    		// TCL_POT1 - AN1 - between 0-10
int blink_speed;    		// TCL_POT2 - AN2 - between 0-1024
int brightness;     		// TCL_POT3 - AN3 - between 0-255
double programControl; 	// TCL_POT4 - AN4 - between 0-1023.0

// Audio detection stuff:
// Spectrum analyzer shield pins
int spectrumStrobe = 8;
int spectrumReset = 9;
int spectrumAnalogL = 4;  //4 for left channel, 5 for right.
int spectrumAnalogR = 5;  //4 for left channel, 5 for right.
int spectrumL[7]; 	// array to hold spectrum values for the left channel
int spectrumR[7]; 	// array to hold spectrum values for the right channel
byte band;	// variable to hold which band we're looking at
// int audioPinL = A4;
// int audioPinR = A5;
int audioValueL; // Left audio value
int audioValueR; // Right audio value
int divisor; // divide single to adjust for low volumes


/*
	Do some setup stuff...:
*/
void setup() {
  
  // Turn on Serial port:
  Serial.begin(9600);
  Serial.println("");
  Serial.println("LED sequencing written by James Hagerman");
  
  // Turn on TCL library:
  TCL.begin();
  
  // Setup TCL board (may not be needed)
//  TCL.setupDeveloperShield();

  // Black out all of the LEDs on boot:
  // TCL.sendEmptyFrame();
  // for(i=0;i<LEDS;i++) {
  //   TCL.sendColor(0x00,0x00,0x00);
  // }
  // TCL.sendEmptyFrame();
  // End Blackout block
	
	// Initialize the spectrum spread board:
	setupSpectrumBoard();
  
  time = millis(); // Find the current time
  buildLinearSpread(); // build the linear value lookup table
  buildAudioSpread(); // build the audio value lookup table
}

// Spectrum board setup:
void setupSpectrumBoard() {
	//Setup pins to drive the spectrum analyzer. 
  pinMode(spectrumReset, OUTPUT);
  pinMode(spectrumStrobe, OUTPUT);

  //Init spectrum analyzer
  digitalWrite(spectrumStrobe,LOW);
    delay(1);
  digitalWrite(spectrumReset,HIGH);
    delay(1);
  digitalWrite(spectrumStrobe,HIGH);
    delay(1);
  digitalWrite(spectrumStrobe,LOW);
    delay(1);
  digitalWrite(spectrumReset,LOW);
    delay(5);
  // Reading the analyzer now will read the lowest frequency.
	
}

// Read 7 band equalizer on each channel
void readSpectrum() {
  // band 0 = Lowest Frequencies.
  for(band=0;band <7; band++) {
    // spectrumL[band] = (analogRead(spectrumAnalogL) + analogRead(spectrumAnalogL) ) >>1; //Read twice and take the average by dividing by 2
    // spectrumR[band] = (analogRead(spectrumAnalogR) + analogRead(spectrumAnalogR) ) >>1; //Read twice and take the average by dividing by 2
		spectrumL[band] = analogRead(spectrumAnalogL);
    spectrumR[band] = analogRead(spectrumAnalogR);

    digitalWrite(spectrumStrobe,HIGH);
    digitalWrite(spectrumStrobe,LOW);     
  }
}

// Audio is read at values between 0 and 1023. This needs to be spread out over a good range of the LED strand to get a reactive curve
void buildAudioSpread() {
  int i;
  double adjustment_function;
  for(i=0;i<WAND_LENGTH;i++) {
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

// Linear lookup table for led positions
void buildLinearSpread() {
  int i;
  double adjustment_function;
  for(i=0;i<WAND_LENGTH;i++) {
    adjustment_function = (i+1)*(1023.0/WAND_LENGTH);
    Serial.print("Setting led ");
    Serial.print(i);
    Serial.print(" to ");
    Serial.println(adjustment_function);
    linearCurve[i] = adjustment_function;
  }
}

/**** END SETUP ****/

/*
  We are doubling back a single strand on each wand to double up led's.
  This function should take a wands array and modify it for this double back.
*/
void setDoubleBack() {
  int i, j, max_wand_index, even_loc, odd_loc;
		// This is old code. Don't use it.
//  for(i=0;i<WAND_LENGTH;i++) {
//    if(i%2==0) { // even led's (0,2,4,6...)
//      even_loc = i-(i/2);
//      double_back[even_loc][0] = current_color[i][0];
//      double_back[even_loc][1] = current_color[i][1];
//      double_back[even_loc][2] = current_color[i][2];
//    } else { // odd led's (1,3,5,7...)
//      odd_loc = LEDS-(i-(i/2));
//      double_back[odd_loc][0] = current_color[i][0];
//      double_back[odd_loc][1] = current_color[i][1];
//      double_back[odd_loc][2] = current_color[i][2];
//    }
//  }
  
  for(i=0; i<WAND_COUNT; i++) {
    for(j=0;j<WAND_LENGTH;j++) {
      if(j%2==0) { // even led's (0,2,4,6...)
			  even_loc = j-(j/2) + (WAND_LENGTH * i);
        // double_back[even_loc][0] = coord[i][j][0];
        // double_back[even_loc][1] = coord[i][j][1];
        // double_back[even_loc][2] = coord[i][j][2];
				double_back_red[even_loc] = coord_red[i][j];
        double_back_green[even_loc] = coord_green[i][j];
        double_back_blue[even_loc] = coord_blue[i][j];

				// Debug: set all even leds to blue
        // double_back[even_loc][0] = 0x00;
        // double_back[even_loc][1] = 0x00;
        // double_back[even_loc][2] = 0xff;

      } else { // odd led's (1,3,5,7...)
        odd_loc = (WAND_LENGTH * (i+1)) - (j-(j/2));
        // double_back[odd_loc][0] = coord[i][j][0];
        // double_back[odd_loc][1] = coord[i][j][1];
        // double_back[odd_loc][2] = coord[i][j][2];

				double_back_red[odd_loc] = coord_red[i][j];
        double_back_green[odd_loc] = coord_green[i][j];
        double_back_blue[odd_loc] = coord_blue[i][j];
				
				// Debug: set all odd leds to red
				// double_back[odd_loc][0] = 0xff;
				// double_back[odd_loc][1] = 0x00;
				// double_back[odd_loc][2] = 0x00;

      }
    }
  }
}

/*
  This version of the update_strand function uses one of the following two arrays as colors on the strand.
  current_color  - the values that would get spit to a single string laid out lengthwise in one piece
  double_back         - calculated inside setDoubleBack() from the values in current_color
  
  This must be modified to support a radial coordinate system and multiple wands.
*/
void update_strand_real() {
  int i;
  int color;
  setDoubleBack(); // Double back will use coord[][][] and build the real array into double_back[]
  
  TCL.sendEmptyFrame();
  for(i=0;i<LEDS;i++) {
		//TCL.sendColor(double_back[i][0],double_back[i][1],double_back[i][2]);
		TCL.sendColor(double_back_red[i],double_back_green[i],double_back_blue[i]);
		
  // old:  
	// TCL.sendColor(current_color[i][0],current_color[i][1],current_color[i][2]);
  }
  for(i=0;i<LEDS;i++) {
	//TCL.sendColor(double_back[i][0],double_back[i][1],double_back[i][2]);
	TCL.sendColor(double_back_red[i],double_back_green[i],double_back_blue[i]);
	
  // old:  
// TCL.sendColor(current_color[i][0],current_color[i][1],current_color[i][2]);
  }
  for(i=0;i<LEDS;i++) {
	//TCL.sendColor(double_back[i][0],double_back[i][1],double_back[i][2]);
	TCL.sendColor(double_back_red[i],double_back_green[i],double_back_blue[i]);
	
  // old:  
// TCL.sendColor(current_color[i][0],current_color[i][1],current_color[i][2]);
  }
  TCL.sendEmptyFrame();  
}


/****
	Animation functions:
*****/
// Update the animation angle
void updateAnimationAngle () {
	anim_angle += (programControl/1023.0 * 20.0) + 0.5; // shift programControl from 0-1023.0 range into 0-100.0 range
	if(anim_angle > 360) {
    anim_angle = anim_angle - 360;
  }
  if(anim_angle < 0) {
    anim_angle = anim_angle + 360; 
  }
	// Serial.print("coord_angle: ");
	// Serial.println(coord_angle);
}

// Update the coordinate angle system
void updateCoordAngle() {
	coord_angle += coord_angle_size;
	if(coord_angle >= 360) {
    coord_angle = coord_angle - 360;
  }
  if(coord_angle < 0) {
    coord_angle = coord_angle + 360; 
  }
}


/*******
	Color generation functions:
********/
// Update the color cycle angle
void updateBaseFadeAngle() {
	fade_angle += (blink_speed/1023.0 * 100.0)+0.1;
  if(fade_angle > 360) {
    fade_angle = fade_angle - 360;
  }
  if(fade_angle < 0) {
    fade_angle = fade_angle + 360; 
  }
  //Serial.println(fade_angle);
}

// convert HSB to RGB:
int rgb_colors[3]; // holder for rgb color from hsb conversion
int hue;
int saturation;
//int brightness; // defined above, set using AN3
const byte dim_curve[] = {
    0,   1,   1,   2,   2,   2,   2,   2,   2,   3,   3,   3,   3,   3,   3,   3,
    3,   3,   3,   3,   3,   3,   3,   4,   4,   4,   4,   4,   4,   4,   4,   4,
    4,   4,   4,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   6,   6,   6,
    6,   6,   6,   6,   6,   7,   7,   7,   7,   7,   7,   7,   8,   8,   8,   8,
    8,   8,   9,   9,   9,   9,   9,   9,   10,  10,  10,  10,  10,  11,  11,  11,
    11,  11,  12,  12,  12,  12,  12,  13,  13,  13,  13,  14,  14,  14,  14,  15,
    15,  15,  16,  16,  16,  16,  17,  17,  17,  18,  18,  18,  19,  19,  19,  20,
    20,  20,  21,  21,  22,  22,  22,  23,  23,  24,  24,  25,  25,  25,  26,  26,
    27,  27,  28,  28,  29,  29,  30,  30,  31,  32,  32,  33,  33,  34,  35,  35,
    36,  36,  37,  38,  38,  39,  40,  40,  41,  42,  43,  43,  44,  45,  46,  47,
    48,  48,  49,  50,  51,  52,  53,  54,  55,  56,  57,  58,  59,  60,  61,  62,
    63,  64,  65,  66,  68,  69,  70,  71,  73,  74,  75,  76,  78,  79,  81,  82,
    83,  85,  86,  88,  90,  91,  93,  94,  96,  98,  99,  101, 103, 105, 107, 109,
    110, 112, 114, 116, 118, 121, 123, 125, 127, 129, 132, 134, 136, 139, 141, 144,
    146, 149, 151, 154, 157, 159, 162, 165, 168, 171, 174, 177, 180, 183, 186, 190,
    193, 196, 200, 203, 207, 211, 214, 218, 222, 226, 230, 234, 238, 242, 248, 255,
};
void getRGB(int hue, int sat, int val, int colors[3]) { 
  /* convert hue, saturation and brightness ( HSB/HSV ) to RGB
     The dim_curve is used only on brightness/value and on saturation (inverted).
     This looks the most natural.      
  */

  val = dim_curve[val];
  sat = 255-dim_curve[255-sat];

  int r;
  int g;
  int b;
  int base;

  if (sat == 0) { // Acromatic color (gray). Hue doesn't mind.
    colors[0]=val;
    colors[1]=val;
    colors[2]=val;  
  } else  { 

    base = ((255 - sat) * val)>>8;

    switch(hue/60) {
    case 0:
        r = val;
        g = (((val-base)*hue)/60)+base;
        b = base;
    break;

    case 1:
        r = (((val-base)*(60-(hue%60)))/60)+base;
        g = val;
        b = base;
    break;

    case 2:
        r = base;
        g = val;
        b = (((val-base)*(hue%60))/60)+base;
    break;

    case 3:
        r = base;
        g = (((val-base)*(60-(hue%60)))/60)+base;
        b = val;
    break;

    case 4:
        r = (((val-base)*(hue%60))/60)+base;
        g = base;
        b = val;
    break;

    case 5:
        r = val;
        g = base;
        b = (((val-base)*(60-(hue%60)))/60)+base;
    break;
    }

    colors[0]=r;
    colors[1]=g;
    colors[2]=b; 
  }   
}

// Calculate colors from the fade angle
void buildFadeColors() {
	
	// HSB conversion for better color
	hue = fade_angle;
	saturation = 255; 
	// brightness = 255; // use the analong input to control brightness.
	getRGB(hue, saturation,brightness,rgb_colors);
	
	fade_color[0] = rgb_colors[0];
	fade_color[1] = rgb_colors[1];
	fade_color[2] = rgb_colors[2];
	
	// RGB cycling (looks meh and too white)
	// old, clunky functions:
	  // r = ((sin((fade_angle + 120)*(M_PI/180)) + 1)/2.0)*brightness;
	  // g = ((sin((fade_angle )*(M_PI/180)) + 1)/2.0)*brightness;
	  // b = ((sin((fade_angle + 240)*(M_PI/180)) + 1)/2.0)*brightness;
	
	// Convert to degrees with phase
	// r = (fade_angle      ) * (M_PI/180);
	// g = (fade_angle + 120) * (M_PI/180);
	// b = (fade_angle + 240) * (M_PI/180);
	// 
	// // Range to 0.0 - 1.0
	// r = (sin(r) + 1) / 2.0;
	// g = (sin(g) + 1) / 2.0;
	// b = (sin(b) + 1) / 2.0;
	// 
	// // Multiply by brightness
	// r = r * brightness;
	// g = g * brightness;
	// b = b * brightness;
	
	//  Serial.print(fade_angle);
	//  Serial.print(", ");
	//  Serial.print(r);
	//  Serial.print(", ");
	//  Serial.print(g);
	//  Serial.print(", ");
	//  Serial.print(b);
	//  Serial.print("\n");
	
	// fade_color[0] = r;
	//   fade_color[1] = g;
	//   fade_color[2] = b;
}

byte colors[3]; // array to hold colors for fadeout stuff
void fadeOut(byte colors[3]) {
	// Zero cross check fade
	byte red, green, blue;
	
	byte fade_amount = 1;
	
	red = colors[0];
	green = colors[1];
	blue = colors[2];
	
	if(red == 0) {
	  red = 0;
	} else {
	  red = red-fade_amount;
	}
	if(green == 0) {
	  green = 0; 
	} else {
	  green = green-fade_amount;
	}
	if(blue == 0) {
	  blue = 0; 
	} else {
	  blue = blue-fade_amount;
	}
	
	colors[0] = red;
	colors[1] = green;
	colors[2] = blue;
	
}


/***********
	Programs: 
************/

// Draw audio levels at some distance out
void drawAudioLevelsLR() {
	//int circle_size = programControl/1023.0 * (WAND_LENGTH-1);
	
	int band_select = programControl/1023.0 * 7;
	// Serial.print("band_select: ");
	// Serial.println(band_select);
	
	audioValueL = spectrumL[band_select];
	audioValueR = spectrumR[band_select];
	

	for(i=0;i<WAND_COUNT;i++) { // for each wand
		for(j=0;j<WAND_LENGTH;j++) { // for each LED
			
			if (i < WAND_COUNT/2) { // first half of the wands
				
				if (linearCurve[j] <= audioValueL) {
				  // coord[i][j][0] = fade_color[0];
				  // coord[i][j][1] = fade_color[1];
				  // 	        coord[i][j][2] = fade_color[2];
					coord_red[i][j] = fade_color[0];
				  coord_green[i][j] = fade_color[1];
	        coord_blue[i][j] = fade_color[2];

	      } else {
					// colors[0] = coord[i][j][0];
					// colors[1] = coord[i][j][1];
					// colors[2] = coord[i][j][2];
					// 
					// fadeOut(colors);
					// 
					// coord[i][j][0] = colors[0];
					// coord[i][j][1] = colors[1];
					// coord[i][j][2] = colors[2];

	        // coord[i][j][0] = 0x00;
	        // coord[i][j][1] = 0x00;
	        // coord[i][j][2] = 0x00;
					coord_red[i][j] = 0x00;
	        coord_green[i][j] = 0x00;
	        coord_blue[i][j] = 0x00;
	
					// coord[i][j][0] = 0xff;
					// coord[i][j][1] = 0xff;
					// coord[i][j][2] = 0xff;
	      }
	
			} else if (i >= WAND_COUNT/2) { // second half of the wands
				if (linearCurve[j] <= audioValueR) {
				  // coord[i][j][0] = fade_color[0];
				  // coord[i][j][1] = fade_color[1];
				  // 	        coord[i][j][2] = fade_color[2];
					coord_red[i][j] = fade_color[0];
				  coord_blue[i][j] = fade_color[1];
	        coord_green[i][j] = fade_color[2];

	      } else {
					// colors[0] = coord[i][j][0];
					// colors[1] = coord[i][j][1];
					// colors[2] = coord[i][j][2];
					// 
					// fadeOut(colors);
					// 
					// coord[i][j][0] = colors[0];
					// coord[i][j][1] = colors[1];
					// coord[i][j][2] = colors[2];

	        // coord[i][j][0] = 0x00;
	        // coord[i][j][1] = 0x00;
	        // coord[i][j][2] = 0x00;
	
					coord_red[i][j] = 0x00;
	        coord_green[i][j] = 0x00;
	        coord_blue[i][j] = 0x00;
	
					// coord[i][j][0] = 0xff;
					// coord[i][j][1] = 0xff;
					// coord[i][j][2] = 0xff;
	      }
			}
			
    } 

   	// Update the coordinate systems angle for the next
		updateCoordAngle();
		
  }
}

// Draw audio levels at some distance out
void drawAudioLevelsL() {
	int circle_size = programControl/1023.0 * (WAND_LENGTH-1);
	// divisor = programControl/1023.0 * 100.0;
	// Serial.print("divisor: ");
	// Serial.println(divisor);
	for(i=0;i<WAND_COUNT;i++) { // for each wand
		audioValueL = spectrumL[i];
		for(j=0;j<WAND_LENGTH;j++) { // for each LED
			if (linearCurve[j] <= audioValueL) {
			  coord_red[i][j] = fade_color[0];
			  coord_green[i][j] = fade_color[1];
			  coord_blue[i][j] = fade_color[2];
				// coord[i][j][0] = 0xff;
				// coord[i][j][1] = 0xff;
				// coord[i][j][2] = 0xff;
				
				// coord_red[i][j] = 0xff;
				//         coord_green[i][j] = 0xff;
				//         coord_blue[i][j] = 0xff;

      } else {
				// colors[0] = coord[i][j][0];
				// colors[1] = coord[i][j][1];
				// colors[2] = coord[i][j][2];
				
				// colors[0] = coord_red[i][j];
				// colors[1] = coord_green[i][j];
				// colors[2] = coord_blue[i][j];
				// 
				// fadeOut(colors);
				// 
				// // coord[i][j][0] = colors[0];
				// // coord[i][j][1] = colors[1];
				// // coord[i][j][2] = colors[2];
				// coord_red[i][j] = colors[0];
				// coord_green[i][j] = colors[1];
				// coord_blue[i][j] = colors[2];

        coord_red[i][j] = 0x00;
        coord_blue[i][j] = 0x00;
        coord_green[i][j] = 0x00;
				// coord[i][j][0] = 0xff;
				// coord[i][j][1] = 0xff;
				// coord[i][j][2] = 0xff;
      }
    } 

   	// Update the coordinate systems angle for the next
		updateCoordAngle();
		
  }
}
// 
// // Draw a circle at some distance out controlled by programControl
// void drawCircle() {
// 	int circle_size = programControl/1023.0 * (WAND_LENGTH-1);
// 	
// 	for(i=0;i<WAND_COUNT;i++) { // for each wand
// 		for(j=0;j<WAND_LENGTH;j++) { // for each LED
//       
//       // Set a circle some distance out from the center to fade:
//       if (j == circle_size) {
// 			  // coord[i][j][0] = fade_color[0];
// 			  // coord[i][j][1] = fade_color[1];
// 			  //         coord[i][j][2] = fade_color[2];
// 
// 				coord_red[i][j] = fade_color[0];
// 			  coord_green[i][j] = fade_color[1];
//         coord_blue[i][j] = fade_color[2];
// 				
//       } else {
// 				// colors[0] = coord[i][j][0];
// 				// colors[1] = coord[i][j][1];
// 				// colors[2] = coord[i][j][2];
// 				
// 				colors[0] = coord_red[i][j];
// 				colors[1] = coord_green[i][j];
// 				colors[2] = coord_blue[i][j];
// 				
// 				fadeOut(colors);
// 				
// 				// coord[i][j][0] = colors[0];
// 				// coord[i][j][1] = colors[1];
// 				// coord[i][j][2] = colors[2];
// 				coord_red[i][j] = colors[0];
// 				coord_green[i][j] = colors[1];
// 				coord_blue[i][j] = colors[2];
// 				
//         // coord[i][j][0] = 0x00;
//         // coord[i][j][1] = 0x00;
//         // coord[i][j][2] = 0x00;
// 				// coord[i][j][0] = 0xff;
// 				// coord[i][j][1] = 0xff;
// 				// coord[i][j][2] = 0xff;
//       }
//     } 
// 
//    	// Update the coordinate systems angle for the next
// 		updateCoordAngle();
// 		
//   }
// }

// The new coordinate system program
void buildCoords() {
	
	// Update animation angle for this frame:
	updateAnimationAngle();
	
	// Calculate the cutoff for this frame
	radiusCutoff = anim_angle*(M_PI/180); 				// don't forget to convert to degrees!
	radiusCutoff = (sin(radiusCutoff) + 1) / 2.0; // shift to 0.0-1.0;
	radiusCutoff = radiusCutoff * 1024;						// shift to 0-1024.0
	
	// Serial.print("sin(");
	// Serial.print(coord_angle);
	// Serial.print(") = ");
	// Serial.println(radiusCutoff);
	// delay(100);
	
  for(i=0;i<WAND_COUNT;i++) { // for each wand
   // Serial.print("wand: ");
   //  Serial.println(i);

			// Serial.print("coord_angle: ");
			// 			Serial.println(coord_angle);
		
		for(j=0;j<WAND_LENGTH;j++) { // for each LED
//      Serial.print(" led: ");
//      Serial.print(j);

      // Set all LEDs to fade color
      // coord[i][j][0] = fade_color[0];
      // coord[i][j][1] = fade_color[1];
      // coord[i][j][2] = fade_color[2];
      
      // Set a circle at 5 leds out from the center to fade:
      //if (j == 5) {
      
      // Set a circle between 5 and 10 leds out from the center to fade:  
      // if (j > 5 && j <= 10) {
	
			// Set all leds above cutoff to fade
      if (linearCurve[j] <= radiusCutoff ) {
			  // coord[i][j][0] = fade_color[0];
			  //         coord[i][j][1] = fade_color[1];
			  //         coord[i][j][2] = fade_color[2];

				coord_red[i][j] = fade_color[0];
        coord_green[i][j] = fade_color[1];
        coord_blue[i][j] = fade_color[2];
				
      } else {
				// colors[0] = coord[i][j][0];
				// colors[1] = coord[i][j][1];
				// colors[2] = coord[i][j][2];
				
				colors[0] = coord_red[i][j];
				colors[1] = coord_green[i][j];
				colors[2] = coord_blue[i][j];
				
				fadeOut(colors);
				
				// coord[i][j][0] = colors[0];
				// coord[i][j][1] = colors[1];
				// coord[i][j][2] = colors[2];
				coord_red[i][j] = colors[0];
				coord_green[i][j] = colors[1];
				coord_blue[i][j] = colors[2];
				
        // coord[i][j][0] = 0x00;
        //         coord[i][j][1] = 0x00;
        //         coord[i][j][2] = 0x00;
				// coord_red[i][j] = 0xff;
				// coord_green[i][j] = 0xff;
				// coord_blue[i][j] = 0xff;
      }
      
//      Serial.print(" idx: ");
//      Serial.println((STRAND_LENGTH*i) + j);
    } 

   	// Update the coordinate systems angle for the next
		updateCoordAngle();
		
  }
  
}

/*
	Main loop:
*/
void loop() {
	// Set variable values
	time = millis();
	blink_speed = analogRead(TCL_POT2);
	brightness = 255 * (analogRead(TCL_POT3)/1023.0); // between 0.0 and 255.0 
	programPick = 10 * (analogRead(TCL_POT1)/1023.0);
	programControl = 1023.0 * (analogRead(TCL_POT4)/1023.0);
	//Serial.println(programControl);

	// Read the values of all bands of the spectrum on both channels
	readSpectrum();
	
	// Update color angles
	updateBaseFadeAngle();  // Update base phase fade_angle  
	buildFadeColors(); 			// Use updated base phase angle to calculate new RGB values:
	
	// Select which program to run. Fading between programs would be cool...
	switch ( programPick ) {
		case 0:
			drawAudioLevelsL();
			break;
		case 1:
			drawAudioLevelsLR();
			break;
		case 2:
			//drawCircle();
			break;
		default:
			buildCoords();
			break;
	}
	
	update_strand_real(); // draw the colors to all of the strands
}
