class MultiSpin extends AnimationRoutine {
//  int w = 0;
//  int h = 0;
//  
//  void reinit() {
//    w = pg.width;
//    h = pg.height;
//  }
  
  int size;
  int changeTimeoutCounter = 0;
  int changeTimeout = 50;
  color backgroundColor;
  
  MultiSpin() {
     size = 5;
  }
  
  MultiSpin(int circleSize) {
     size = circleSize;
  }

  void draw() {
    draw(mouseX, mouseY);
  }
  
  void draw(int inputX, int inputY) {
    pg.beginDraw();
    pg.noStroke();
    pg.colorMode(HSB, 255);
    pg.background(backgroundColor);
    pg.pushMatrix();
    pg.imageMode(CENTER);
    pg.translate(width/2, height/2);
    
    pg.rotate(radians(inputX));

    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    
    pg.popMatrix();
    pg.endDraw();
    
//    changeTimeoutCounter += 1;
//    if (changeTimeoutCounter > changeTimeout) {
//      changeColors();
//      changeTimeoutCounter = 0;
//    }
    // change every 30 degrees
    if (inputX%30 == 1) {
      changeColors();
//      changeTimeoutCounter = 0;
    }
  }
  
  void changeColors() {
    colorMode(HSB, 255);
    backgroundColor = color((int)random(255), 255, 150); 
    colorMode(RGB, 255);
  }
  
  
  void drawBar() {
    int colors = 100;
    for (int i = 0; i < colors; i++) {
      pg.fill((255/colors)*i, 255, 255); // HSB colors
      pg.rect(((width/2)/colors)*i, -size/2, (width/2)/colors, size);
    }
  }
}
