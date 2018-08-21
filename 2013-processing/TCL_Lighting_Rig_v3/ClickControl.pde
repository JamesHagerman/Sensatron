/*
  ClickControl.pde
  by James Hagerman
  on December 10, 2013
 */

class ClickControl {
  // Place to draw this specific layout shape:
  PGraphics lightDisplay;
  
	// Lighting hardware settings:
	int strands = 1; // Number of physical wands
	int strandLength = 150; // Number of lights per wand
	int totalPixels = strands * strandLength; // Total number of lights (150)

	// Color Data storage:
	int[] lights;
  int[] remapArray;

  // Physical location storage:
  PVector[] physicalLocations;
  Table locationsTable;
  String locationFile = "locations.csv"; // located at: data/locations.csv

  // Mouse input management:
	int currentLight = 0; // which light index the mouse will affect

  ClickControl() {
  	// Verify that the layout shape is valid:
  	if (!validateLayout()) {
  		println("The ClickControl system doesn't have a valid shape layout defined!");
  		exit();
  	}

  	lights = new int[totalPixels];

  	// Load the mapping locations from the CSV file:
  	loadCSV();

    lightDisplay = createGraphics(width, height, P3D);
    lightDisplay.smooth();
    lightDisplay.lights();

    // Build the radial remapping array we'll need for the spoke pattern:
    buildRemapArray();
  }

  void loadCSV() {
  	physicalLocations = new PVector[totalPixels];
  	locationsTable = loadTable(locationFile, "header");

  	int rowCount = locationsTable.getRowCount();
  	println("Loaded " + rowCount + " rows from the provided CSV, data/" + locationFile);
  	
  	if (rowCount < totalPixels) {
  		println("Not enough pixel locations were defined. Using 0,0 for the missing ones...");
  	} else if (rowCount > totalPixels) {
  		println("More pixel locations were defined then available lights. Ignoring extra locations...");
  	}

  	int x,y;
  	for (int i = 0; i < totalPixels; i++) {
  		if (i < rowCount) {
  			x = locationsTable.getInt(i,"x");
  			y = locationsTable.getInt(i,"y");	
  		} else {
  			x = 0;
  			y = 0;
  		}

  		// println(x+","+y);
  		physicalLocations[i] = new PVector(x, y);
  	}
  	saveLocationTable();
  }

  void handleMouse() {
  	if (mouseButton == LEFT) {
	    println("Left Click. Setting " + mouseX+","+mouseY + " as the coordinate for light " + currentLight);
	    physicalLocations[currentLight].set((int)mouseX, (int)mouseY);
	    saveLocationTable();
	    currentLight += 1;
	    if (currentLight > totalPixels-1) {
	  		currentLight = totalPixels-1;
	  		println("Oops! No more lights to set!");
	  	}
	  } else if (mouseButton == RIGHT) {
	  	currentLight -= 1;
	  	if (currentLight < 0) {
	  		currentLight = 0;
	  		println("Oops! You are at the beginning of the lights list!");
	  	}
	    println("Right click. Backing up the light index to " + currentLight);
	  } else {
	    println("not a left or right click... what happened!?");
	  }
  }

  void saveLocationTable() {
  	locationsTable.clearRows();
  	for (int i = 0; i < totalPixels; i++) {
  		TableRow newRow = locationsTable.addRow();
  		newRow.setInt("x", (int) physicalLocations[i].x);
  		newRow.setInt("y", (int) physicalLocations[i].y);
  	}
  	saveTable(locationsTable, "data/"+locationFile);
  }

	// This method will validate the initial layout shape parameters: 
  boolean validateLayout() {
  	return true;
  }

  

  void buildRemapArray() {
    println("Building ClickControl remap array...");
    remapArray = new int[totalPixels];
    // No map:
    for (int i = 0; i < totalPixels; i++) {
      remapArray[i] = i;
    }
    println("Done building ClickControl remap array.");
  }

  // This method strips raw color data from a given PImage and plops it DIRECTLY into the radial lights array:
  void stripRawColors(PImage toLoad) {
    toLoad.loadPixels();

    // ToDo: Add a map function to be able to scale the values...
    for (int i = 0; i < totalPixels; i++) {
    	int y = (int) physicalLocations[i].y;
    	int x = (int) physicalLocations[i].x;
    	lights[i] = toLoad.pixels[y*toLoad.width+x];
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
  
  int[] mapDataToLights() {
  	// No mapping for this Control object:
    return lights;
  }

  void drawLights(PImage animationFrame) {
    lightDisplay.beginDraw();
    lightDisplay.background(animationFrame);
    lightDisplay.pushMatrix();
    // rotateZ(radians(180));
    // lightDisplay.translate(0, 0, -100);
    // lightDisplay.rotateX(radians(45));

    for (int i = 0; i < totalPixels; i++) {
    	lightDisplay.fill(lights[i]);
    	// lightDisplay.fill(200);
    	int y = (int) physicalLocations[i].y;
    	int x = (int) physicalLocations[i].x;
    	lightDisplay.ellipse(x, y, 5, 5);
    }

    lightDisplay.popMatrix();
    lightDisplay.endDraw();
    image(lightDisplay, 0, 0);
  }
  
  
  
  // These are really more like animations but maybe we can use them as a tool to help us write animations later:
  void randomizeAllLights() {
    // for (int strand = 0; strand < strands; strand++) {
    //   for (int lightNum = 0; lightNum < strandLength; lightNum++) {
    //     lights[strand][lightNum] = getRandomColor();
    //   }
    // }
  }
  
  void setAllLights(color c) {
    // for (int strand = 0; strand < strands; strand++) {
    //   for (int lightNum = 0; lightNum < strandLength; lightNum++) {
    //     lights[strand][lightNum] = c;
    //   }
    // }
  }
  
  void setOneLight(int strand, int lightNum, color c) {
    // lights[strand][lightNum] = c;
  }
  
  color getRandomColor() {
    return color((int)random(255), (int)random(255), (int)random(255));
  }

}
