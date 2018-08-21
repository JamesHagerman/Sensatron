class LightDisplay {
  PGraphics lightDisplay;
  int SPACING = 5;
  
  LightDisplay() {
    lightDisplay = createGraphics(width, height, P3D);
    lightDisplay.smooth();
    lightDisplay.lights();
  }
 
  void drawLights() {
    int centerX = lightDisplay.width/2;
    int centerY = lightDisplay.height/2;
    
    lightDisplay.beginDraw();
    lightDisplay.background(100);
    lightDisplay.pushMatrix();
    //  rotateZ(radians(180));
    lightDisplay.translate(0, 0, -100);
    lightDisplay.rotateX(radians(45));
  
    for (int strand = 0; strand < STRANDS; strand++) {
      double theta = strand * dRad - (PI/2) + PI;
      for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
        int c = lights[strand][lightNum];
  //      c = 255;
        lightDisplay.fill(c);
  //      noStroke();
        int y = (int) ((lightNum+3) * SPACING * Math.sin(theta));
        int x = (int) ((lightNum+3) * SPACING * Math.cos(theta));
        x = centerX - x;
        y = centerY - y;
        lightDisplay.ellipse(x, y, 5, 5);
      }
      // Draw the wand labels
      lightDisplay.fill(255);
      int y = (int) ((STRAND_LENGTH+3) * SPACING * Math.sin(theta));
      int x = (int) ((STRAND_LENGTH+3) * SPACING * Math.cos(theta));
      x = centerX - x;
      y = centerY - y;
      lightDisplay.text(strand, x, y, 10);
    }
  
    lightDisplay.popMatrix();
    lightDisplay.endDraw();
    image(lightDisplay, 0, 0);
    
    // THIS NEEDS TO BE MOVED ELSEWHERE:
//    // Move this information to the physical lights:
//    mapDrawingToLights();
//    sendLights();
  } 
  
}
