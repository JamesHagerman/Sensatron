/*
  SpiralControl.pde
  by James Hagerman
  on December 10, 2013
 */

class SpiralControl {
  // Place to draw this specific layout shape:
  PGraphics lightDisplay;
  
	// Lighting hardware settings:
	int strands = 3; // Number of physical wands
	int strandLength = 50; // Number of lights per wand
	int ledCount = strands * strandLength; // Total number of lights (150)

	// SpiralControl layout shape paramaters:
	// Separate the lights into multiple display layers:
	int layers = 6;
	// Each layer of the pattern will have some number of lights. 
	int[] layerLights = {10, 25, 25, 25, 25, 40};

	// Color storage is based on layout shape:
	int[][] lights;
  int[] remapArray;

  int grabSpacing = 5; // This spacing is for PULLING data from images not putting lights on the screen
  int displaySpacing = 5;

  
  SpiralControl() {
  	// Verify that the layout shape is valid:
  	int maximumLayerSize = validateLayout();
  	if (maximumLayerSize == -1) {
  		println("The SpiralControl system doesn't have a valid shape layout defined!");
  		exit();
  	}

  	lights = new int[layers][maximumLayerSize];

    lightDisplay = createGraphics(width, height, P3D);
    lightDisplay.smooth();
    lightDisplay.lights();

    // Build the radial remapping array we'll need for the spoke pattern:
    buildRemapArray();
  }

	// This method will validate the initial layout shape parameters: 
  int validateLayout() {
  	int layerCountCheck = 0;
  	int maximumOnLayer = 0;
  	for (int i = 0; i < layers; i++) {
  		int thisLayerCount = layerLights[i];
  		layerCountCheck += thisLayerCount;
  		if (thisLayerCount > maximumOnLayer) {
  			maximumOnLayer = thisLayerCount;
  		}
  	}
  	if (layerCountCheck == ledCount) {
  		return maximumOnLayer;
  	}
  	return -1;
  }

  void buildRemapArray() {
    println("Building SpiralControl remap array...");
    remapArray = new int[ledCount];

    // No map:
    for (int i = 0; i < ledCount; i++) {
      remapArray[i] = i;
    }

    // Only the colors of the first light:
    // for (int i = 0; i < ledCount; i++) {
    //   remapArray[i] = 0;
    // }

    // Working radial remap:
    // int index = 0;
    // for(int i=0; i<strands; i++) {
    //   println("Setting wand: " + i);
    //   for(int j=0;j<strandLength;j++) {
    //     if(j%2==0) { // even led's (0,2,4,6...)
    //       remapArray[j-(j/2) + (strandLength * i)] = index;
    //       // if (i == 1) {
    //       //   println("index " + index + " is: " + remapArray[index]);
    //       // }
    //     } else { // odd led's (1,3,5,7...)
    //       remapArray[(strandLength * (i+1)) - (j-(j/2))] = index;
    //       // if (i == 1) {
    //       //   println("index " + index + " is: " + remapArray[index]);
    //       // }
    //     }
    //     index += 1;
    //   }
    // }
    
    println("Done building SpiralControl remap array.");
  }

  // This method strips raw color data from a given PImage and plops it DIRECTLY into the radial lights array:
  void stripRawColors(PImage toLoad) {
    toLoad.loadPixels();
    int centerX = toLoad.width/2;
    int centerY = toLoad.height/2;
    for (int strand = 0; strand < strands; strand++) {
    	dRad = (Math.PI*2)/strands;
      double theta = strand * dRad - (PI/2) + PI;
      for (int lightNum = 0; lightNum < strandLength; lightNum++) {
        int y = (int) ((lightNum+3) * grabSpacing * Math.sin(theta));
        int x = (int) ((lightNum+3) * grabSpacing * Math.cos(theta));
        
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

  // This method draws the raw data to the screen:
  // void drawRawData() {
  //   if (cam.available()) {
  //     cam.read();
  //     cam.loadPixels();
  //   }
  //   image(cam, width-cam.width, 100); 
  //   color c = cam.pixels[1*cam.width+1]; //pixels[y*cam.width+x]
  //   // color c = cam.get(60,90);
  //   noStroke();
  //   fill(c);
  //   rect(width-30, height-30, 20, 20);  
  // }
  
  int[] mapRadialArrayToLights() {
    int[] toRet = new int[totalPixels];
    int lightIndex = 0;
    for (int layer = 0; layer < layers; layer++) {
      for (int lightNum = 0; lightNum < layerLights[layer]; lightNum++) {
        toRet[lightIndex] = lights[layer][lightNum];
        lightIndex++;
      }
    }
    return toRet;
  }

  void drawLights() {
    int centerX = lightDisplay.width/2;
    int centerY = lightDisplay.height/2;
    
    lightDisplay.beginDraw();
    lightDisplay.background(100);
    lightDisplay.pushMatrix();
    // rotateZ(radians(180));
    lightDisplay.translate(0, 0, -100);
    // lightDisplay.rotateX(radians(45));

    // For each layer...
    for (int layer = 0; layer < layers; layer++) {

    	int layerDistance = (layer+1) * 50;

    	// Iterate over each light in that layer:
    	for (int lightNum = 0; lightNum < layerLights[layer]-1; lightNum++) {
    		// Pull that ligths expected color:
    		// lightDisplay.fill(lights[layers][lightNum]);
    		lightDisplay.fill(200);

    		// Do some angle shit:

				// double theta = layer * dRad - (PI/2) + PI;
				float spacing = 5*lightNum;

    		int y = (int) (layerDistance * Math.sin(radians(spacing)));
    		int x = (int) (layerDistance * Math.cos(radians(spacing)));

    		// Actual pixel position from center:
    		x = centerX - x;
        y = centerY - y;
    		lightDisplay.ellipse(x, y, 5, 5);
    	}
    }

    lightDisplay.popMatrix();
    lightDisplay.endDraw();
    image(lightDisplay, 0, 0);
  }
  
  
  
  // These are really more like animations but maybe we can use them as a tool to help us write animations later:
  void randomizeAllLights() {
    for (int strand = 0; strand < strands; strand++) {
      for (int lightNum = 0; lightNum < strandLength; lightNum++) {
        lights[strand][lightNum] = getRandomColor();
      }
    }
  }
  
  void setAllLights(color c) {
    for (int strand = 0; strand < strands; strand++) {
      for (int lightNum = 0; lightNum < strandLength; lightNum++) {
        lights[strand][lightNum] = c;
      }
    }
  }
  
  void setOneLight(int strand, int lightNum, color c) {
    lights[strand][lightNum] = c;
  }
  
  color getRandomColor() {
    return color((int)random(255), (int)random(255), (int)random(255));
  }
}
