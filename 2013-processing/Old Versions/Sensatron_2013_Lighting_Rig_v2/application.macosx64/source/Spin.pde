class Spin extends SensatronRoutine {
//  int w = 0;
//  int h = 0;
//  
//  void reinit() {
//    w = pg.width;
//    h = pg.height;
//  }
  
  int size;
  Spin() {
     size = 5;
  }
  
  Spin(int circleSize) {
     size = circleSize;
  }

  void draw() {
    draw(mouseX, mouseY);
  }
  
  void draw(int inputX, int inputY) {
    pg.beginDraw();
    pg.noStroke();
    pg.colorMode(HSB, 255);
    pg.background(255,255,0);
    pg.pushMatrix();
    pg.imageMode(CENTER);
    pg.translate(width/2, height/2);
    
    pg.rotate(radians(inputX));
    pg.fill(inputY, 255, 255); // HSB colors
    pg.rect(-width/2, -size/2, width, size);
    
    pg.rotate(radians(-inputX*2));
    pg.fill(inputY, 255, 255); // HSB colors
    pg.rect(-width/2, -size/2, width, size);
    
    pg.popMatrix();
    pg.endDraw();
  }
}
