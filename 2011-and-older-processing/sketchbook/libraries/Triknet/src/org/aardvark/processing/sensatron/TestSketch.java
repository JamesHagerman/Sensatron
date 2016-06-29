package org.aardvark.processing.sensatron;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import processing.core.PApplet;
import processing.core.PImage;

public class TestSketch extends PApplet {
	 
	public void setup() {
	try { 
	  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
	} catch (Exception e) { 
	  e.printStackTrace();  
	 
	} 
	 
	// create a file chooser 
	final JFileChooser fc = new JFileChooser(); 
	 
	// in response to a button click: 
	int returnVal = fc.showOpenDialog(this); 
	 
	if (returnVal == JFileChooser.APPROVE_OPTION) { 
	  File file = fc.getSelectedFile(); 
	  // see if it's an image 
	  // (better to write a function and check for all supported extensions) 
	  if (file.getName().endsWith("jpg")) { 
	    // load the image using the given file path
	    PImage img = loadImage(file.getPath()); 
	    if (img != null) { 
	      // size the window and show the image 
	      size(img.width,img.height); 
	      image(img,0,0); 
	    } 
	  } else { 
	    // just print the contents to the console 
	    // note: loadStrings can take a Java File Object too 
	    String lines[] = loadStrings(file); 
	    for (int i = 0; i < lines.length; i++) { 
	      println(lines[i]);  
	    } 
	  } 
	} else { 
	  println("Open command cancelled by user."); 
	}
	}
}
