#include <SPI.h>
#include <TCL.h>
#include <math.h>

int i;

int board = 1; // 0 = mini; 1 = fullsized

const int LEDS = 600; // 50 LED Total Control Lighting Strand
int wand_length = 49; // zero index

byte current_color[LEDS][3]; // This will store the RGB values of various colors
int remap_lookup[LEDS];

int btn = 9; // button definition for mini
int button_state;
int went_low = 0;

int program_select = 0;
int sparkle_speed = 50;

boolean fade_on = false;
double fade_angle = 0.0;
int blink_speed = 5;
int red, blue, green;
int brightness = 255;
byte fade_color[3]; // holds the current rgb byte values for the fading color cyclers

int bouncer_count = 0;
int sparkle_count = 0;

void setup() {
  Serial.begin(9600);
  Serial.println("LED sequencing by James Hagerman");
  Serial.print("Program changed to ");
  Serial.println(program_select);
  
  TCL.begin();
  if (board == 1) {
    TCL.setupDeveloperShield();
  }
  
  pinMode(btn, INPUT);
  
  // Build lookup table for remapping pixel location based on strand index
  for (i = 0; i < LEDS; i++) {
    // One to One lookup:
    //remap_lookup[i] = i;
    
    // No fucking idea:
    //remap_lookup[i] = i+(i*wand_count);
    
    // Double back stuff:	
    if( i%2 == 0 ) { // even led's (0,2,4,6,...)
      //remap_lookup[i] = 0;
      //remap_lookup[i] = j-(j/2) + (wand_length * i);
      remap_lookup[i] = (i-(i/2));
    } else { // odd led's
      //remap_lookup[i] = -1;
      //remap_lookup[i] = (wand_length * (i+1)) - (j-(j/2));
      remap_lookup[i] = (wand_length) - ((i-(i/2)) - 1);
    }		
  }

  update_strand(); // Update the colors along the strand of LEDs (should be black here...)
}

void update_strand() {
  TCL.sendEmptyFrame();
  for(int i=0; i<=LEDS; i++) {
    TCL.sendColor(0x00,0x00,0x00);
    //TCL.sendColor(current_color[remap_lookup[i]][0],current_color[remap_lookup[i]][1],current_color[remap_lookup[i]][2]);
  }
  TCL.sendEmptyFrame();
}

void update_strand_red() {
  TCL.sendEmptyFrame();
  for(int i=0; i<=LEDS; i++) {
    TCL.sendColor(0xff,0x00,0x00);
    //TCL.sendColor(current_color[remap_lookup[i]][0],current_color[remap_lookup[i]][1],current_color[remap_lookup[i]][2]);
  }
  TCL.sendEmptyFrame();
}

void loop() {
  
  if (fade_on) {
//    Serial.println("fade on");
    red = fade_color[0];
    blue = fade_color[1];
    green = fade_color[2];
  } else {
    red = random(0,255);
    blue = random(0,255);
    green = random(0,255); 
  }
  
  // Select which lighting program to run:
  if (program_select == 0) {
    fade_on = true;
    sparkle_program(); 
  } else if (program_select == 1) {
    fade_on = true;
    bounce_program();
  } else if (program_select == 2) {
    fade_on = true;
    fill_program();
  } else if (program_select == 3) {
    fade_on = false;
    sparkle_program();
  } else if (program_select == 4) {
    fade_on = false;
    bounce_program();
  } else if (program_select == 5) {
    fade_on = false;
    fill_program();
  } else if (program_select == 6) {
    fade_on = false;
    test_program();
  } else if (program_select == 7) {
    fade_on = true;
    test_program();
  }
  
  //update_strand();
  
  if (board == 1) {
    blink_speed = analogRead(TCL_POT2);
    sparkle_speed = analogRead(TCL_POT1);
    brightness = 255 * (analogRead(TCL_POT3)/1023.0); // between 0.0 and 255.0 
    sparkle_count = 9 * analogRead(TCL_POT4)/1023.0; // between 0 and 9
  }
  
  updateBaseFadeAngle();  // Update base phase fade_angle  
  buildFadeColors(); 			// Use updated base phase angle to calculate new RGB values:
  
  buttonRead();
  
}

void sparkle_program() {
  
  int val1 = random(0,LEDS);
  int val2 = random(0,LEDS);
  int val3 = random(0,LEDS);
  int val4 = random(0,LEDS);
  int val5 = random(0,LEDS);
  int val6 = random(0,LEDS);
  int val7 = random(0,LEDS);
  int val8 = random(0,LEDS);
  int val9 = random(0,LEDS);
  int val10 = random(0,LEDS);
  
  Serial.println(sparkle_count);
  
  TCL.sendEmptyFrame();
  for(int i=0; i<=LEDS; i++) {
    if (i == val1 && sparkle_count >= 0) {
      TCL.sendColor((byte)red, (byte)blue, (byte)green); 
    } else if (i == val2 && sparkle_count >= 1) {
      TCL.sendColor((byte)red, (byte)blue, (byte)green); 
    } else if (i == val3 && sparkle_count >= 2) {
      TCL.sendColor((byte)red, (byte)blue, (byte)green); 
    } else if (i == val4 && sparkle_count >= 3) {
      TCL.sendColor((byte)red, (byte)blue, (byte)green); 
    } else if (i == val5 && sparkle_count >= 4) {
      TCL.sendColor((byte)red, (byte)blue, (byte)green);
    } else if (i == val6 && sparkle_count >= 5) {
      TCL.sendColor((byte)red, (byte)blue, (byte)green); 
    } else if (i == val7 && sparkle_count >= 6) {
      TCL.sendColor((byte)red, (byte)blue, (byte)green); 
    } else if (i == val8 && sparkle_count >= 7) {
      TCL.sendColor((byte)red, (byte)blue, (byte)green); 
    } else if (i == val9 && sparkle_count >= 8) {
      TCL.sendColor((byte)red, (byte)blue, (byte)green); 
    } else if (i == val10 && sparkle_count >= 9) {
      TCL.sendColor((byte)red, (byte)blue, (byte)green);  
    } else {
      TCL.sendColor(0x00,0x00,0x00);
    }
  }
  TCL.sendEmptyFrame();
//  Serial.println("");
  
  delay(sparkle_speed);
  
  TCL.sendEmptyFrame();
  for(int i=0; i<=LEDS; i++) {
      TCL.sendColor(0x00,0x00,0x00);
  }
  TCL.sendEmptyFrame();
  
//  delay(200);
}

void fill_program() {
  
  TCL.sendEmptyFrame();
  for(i=0; i<=LEDS; i++) {
      TCL.sendColor((byte)red, (byte)blue, (byte)green);    
    //TCL.sendColor(current_color[remap_lookup[i]][0],current_color[remap_lookup[i]][1],current_color[remap_lookup[i]][2]);
  }
  TCL.sendEmptyFrame();
  
  if (fade_on == false) {
    delay(sparkle_speed);
  }
  
//  TCL.sendEmptyFrame();
//  for(int i=0; i<=LEDS; i++) {
//      TCL.sendColor(0x00,0x00,0x00);
//  }
//  TCL.sendEmptyFrame();
  
}

void bounce_program() {
  bouncer_count += 1;
  if (bouncer_count >= LEDS) {
    bouncer_count = 0;
  }
  
  TCL.sendEmptyFrame();
  for(i=0; i<=LEDS; i++) {
    if (i == bouncer_count) {
      TCL.sendColor((byte)red, (byte)blue, (byte)green);    
    } else {
      TCL.sendColor(0x00,0x00,0x00);   
    }
       
    //TCL.sendColor(current_color[remap_lookup[i]][0],current_color[remap_lookup[i]][1],current_color[remap_lookup[i]][2]);
  }
  TCL.sendEmptyFrame();
  
  delay(sparkle_speed);
}

void test_program() {
  update_strand_red();
}


void buttonRead() {
   // read the state of the switch into a local variable:
   int button_state;
   
   if (board == 0) {
     button_state = digitalRead(btn);
   } else if (board == 1) {
     button_state = digitalRead(TCL_MOMENTARY1);
   }
//  Serial.println(button_state);
  
  if (board == 0) {
    if (button_state == LOW && went_low != 2) {
      went_low = 1;
    }
    
    if (button_state == HIGH) {
      went_low = 0;
    }
  } else if (board == 1) {
    if (button_state == HIGH && went_low != 2) {
      went_low = 1;
    }
    
    if (button_state == LOW) {
      went_low = 0;
    }
  }
  
  if (went_low == 1) {
//    Serial.println("Went low");
    program_select += 1;
    if (program_select > 5) {
      program_select = 0;
    }
    Serial.print("Program changed to ");
    Serial.println(program_select);
    went_low = 2;
  } 
}

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
}
