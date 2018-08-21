PGraphics outputPg;
PGraphics maskPg;
PGraphics bottomLeds;
PGraphics topLeds;
PGraphics mask;

int centerX;
int centerY;
  
void setup() {
  size(500,500,P3D);
  smooth();
  
  // Build the graphics objects:
  outputPg = createGraphics(width,height,P3D);
  maskPg = createGraphics(width,height,P3D);
  bottomLeds = createGraphics(width,height,P3D);
  topLeds = createGraphics(width,height,P3D);
  outputPg.smooth();
  maskPg.smooth();
  bottomLeds.smooth();
  topLeds.smooth();
  
  // Define some useful variables:
  centerX = width/2;
  centerY = height/2;
  
  // Build a mask for the top and bottom leds:
  mask = createGraphics(width,height,P2D);
  buildMask();
}

void draw() {
//  image(mask,0,0);
  background(0);
  updateAll(mouseX, mouseY);
  joinTopBottom();
  image(outputPg,0,0);
}

void joinTopBottom() {
  maskPg = topLeds;
  maskPg.mask(mask);
  outputPg.beginDraw();
  outputPg.image(bottomLeds,0,0);
  outputPg.image(maskPg, 0, 0);
  outputPg.endDraw();
}

void buildMask() {
  mask.beginDraw();
  mask.background(255);
  mask.noStroke();
  mask.fill(0);
  mask.ellipse(centerX, centerY, centerX, centerY);
  mask.endDraw();
}

void updateAll(int inputX, int inputY) {
  updateTop(inputX, inputY);
  updateBottom(inputX, inputY);
}

void updateTop(int inputX, int inputY) {
  topLeds.beginDraw();
  topLeds.background(0,255,0);
  
  topLeds.noStroke();
  topLeds.fill(255,0,0);
  topLeds.ellipse(inputX, inputY, 20, 20);
  
  topLeds.endDraw();  
  
}

void updateBottom(int inputX, int inputY) {
  bottomLeds.beginDraw();
  bottomLeds.background(0);
  
  bottomLeds.noStroke();
  bottomLeds.fill(getRandomColor());
  bottomLeds.ellipse(inputX, inputY, 20, 20);
  
  bottomLeds.endDraw();  
}


color getRandomColor() {
  return color((int)random(255), (int)random(255), (int)random(255));
}
