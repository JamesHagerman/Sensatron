class RawConversion {
  int SPACING = 5; // This spacing is for PULLING data from images not putting lights on the screen
  
  RawConversion() {
    
  }
  
  // This method strips raw color data from a given PImage and plops it DIRECTLY into the radial lights array:
  void stripRawColors(PImage toLoad) {
    toLoad.loadPixels();
    int centerX = toLoad.width/2;
    int centerY = toLoad.height/2;
    for (int strand = 0; strand < STRANDS; strand++) {
      double theta = strand * dRad - (PI/2) + PI;
      for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
        int y = (int) ((lightNum+3) * SPACING * Math.sin(theta));
        int x = (int) ((lightNum+3) * SPACING * Math.cos(theta));
        
        x = (int)map(x, 0, 600, 0, toLoad.width);
        y = (int)map(y, 0, 600, 0, toLoad.height);
        x = centerX - x;
        y = centerY - y;
        fill(toLoad.pixels[y*toLoad.width+x]);
        ellipse(500+x, 0+toLoad.height+y, 5, 5); // 
        lights[strand][lightNum] = toLoad.pixels[y*toLoad.width+x];
      }
    }
  }
  
//  // This method draws the raw data to the screen:
//  void drawRawData() {
//    if (cam.available()) {
//      cam.read();
//      cam.loadPixels();
//    }
//    image(cam, width-cam.width, 100); 
//    color c = cam.pixels[1*cam.width+1]; //pixels[y*cam.width+x]
//  //  color c = cam.get(60,90);
//    noStroke();
//    fill(c);
//    rect(width-30, height-30, 20, 20);  
//  }


}
