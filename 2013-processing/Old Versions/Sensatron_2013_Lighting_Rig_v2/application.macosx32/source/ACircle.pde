class ACircle extends SensatronRoutine {
//  int w = 0;
//  int h = 0;
//  
//  void reinit() {
//    w = pg.width;
//    h = pg.height;
//  }
  
  int diameter;
  ACircle() {
     diameter = 5;
  }
  
  ACircle(int circleSize) {
     diameter = circleSize;
  }

  void draw() {
    draw(mouseX, mouseY);
  }
  
  void draw(int inputX, int inputY) {
    pg.beginDraw();
    pg.colorMode(HSB, 255);
    pg.background(inputY, inputX, 255);
    pg.noStroke();
    pg.fill(0, 255, 0);
//    pg.ellipse(inputX,inputY, diameter, diameter);
    pg.endDraw();
  }
}
