class CircleAnimation extends SensatronRoutine {
  int diameter; // Size of the largest single circle ever drawn
  int[] xpos;
  int[] ypos;
  
  CircleAnimation() {
     diameter = 100;
     reinit();
  }
  
  CircleAnimation(int circleSize) {
     diameter = circleSize;
     reinit();
  }

  void reinit() {
    // Declare two arrays with 50 elements.
    xpos = new int[100]; 
    ypos = new int[100];
    
    // Initialize all elements of each array to zero.
    for (int i = 0; i < xpos.length; i ++ ) {
      xpos[i] = 0; 
      ypos[i] = 0;
    }
  }

  void draw() {
    draw(mouseX, mouseY);
  }
  
  void draw(int inputX, int inputY) {
    pg.beginDraw();
    pg.colorMode(HSB, 255);
    pg.background(inputY, 255, 100);
    
    // Shift array values
    for (int i = 0; i < xpos.length-1; i ++ ) {
      // Shift all elements down one spot. 
      // xpos[0] = xpos[1], xpos[1] = xpos = [2], and so on. Stop at the second to last element.
      xpos[i] = xpos[i+1];
      ypos[i] = ypos[i+1];
    }
  //  delay(mouseY);
    
    // New location
    xpos[xpos.length-1] = inputX; // Update the last spot in the array with the mouse location.
    ypos[ypos.length-1] = 0;
    
    // Draw everything
    pg.pushMatrix();
//    rotateX(radians(-45));
//    rotateY(radians(45));
    pg.translate(pg.width/2,pg.height/2); // for P3D renderer
//    translate(width/2,height/2);
  
//    for (int i = 0; i < xpos.length; i ++ ) {
//       // Draw an ellipse for each element in the arrays. 
//       // Color and size are tied to the loop's counter: i.
//      
//  //    rotateZ(radians(i*90)); // for P3D renderer
//      rotate(radians(30));
//      noStroke();
//  //    translate(0,0,-10);
//      fill(255-i*2, 0+i*2.5, 255);
//      ellipse(xpos[i],ypos[i],i,i);
//    }
    for (int i = xpos.length -1; i > 0 ; i-- ) {
       // Draw an ellipse for each element in the arrays. 
       // Color and size are tied to the loop's counter: i.
      
//      rotateZ(radians(i*90)); // for P3D renderer
      pg.rotate(radians(30));
      pg.noStroke();
//      translate(0,0,-10);
      
      pg.fill(255-i*2, 255, 255, inputY); // HSB colors
      
//      fill(255-i*2, 0+i*2.5, 255-i*2, mouseY); // rgb colors
      pg.ellipse(xpos[i],ypos[i],i,i);
    }
    pg.popMatrix();
    pg.endDraw();
  }
}
