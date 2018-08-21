package org.aardvark.processing;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.aardvark.processing.delegate.ActionDelegate;
import org.aardvark.processing.delegate.KeyEventDelegate;
import org.aardvark.processing.delegate.MouseEventDelegate;

import processing.core.PApplet;

public class PButton implements PAppletAware {
	  int x;
	  int y;
	  int width;
	  int height;
	  String label;
	  transient PApplet parent;
	  MouseEventDelegate mouseDelegate;
	  KeyEventDelegate keyDelegate;
	  ActionDelegate actionDelegate;
	  int fillColor;
	  
	  public PButton(PApplet parent, int x, int y, int width, int height, String label, ActionDelegate action) {
	    this.x = x;
	    this.y = y;
	    this.width = width;
	    this.height = height;
	    this.parent = parent;
	    this.label = label;
	    this.actionDelegate = action;
	    fillColor = parent.color(255);
	  }
	  
	  public PButton(PApplet parent, int x, int y, int width, int height, String label, ActionDelegate action, char shortcut) {
		  this(parent, x, y, width, height, label, action);
		  setKeyboardShortcut(shortcut);
	  }
	  
	  public void setKeyboardShortcut(final char key) {
		  keyDelegate = new KeyEventDelegate() {

			  public void keyEvent(KeyEvent event) {
				  if (event.getID() == KeyEvent.KEY_TYPED 
						  && event.getKeyChar() == key) {
					  doAction(event);
				  }
			  }

		  };
	  }
	  
	  public void mouseEvent(MouseEvent event) {
		  if (mouseDelegate != null) {
			  mouseDelegate.mouseEvent(event);
		  } else {
			  if (event.getID() == MouseEvent.MOUSE_CLICKED) {
				  int x = event.getPoint().x;
				  int y = event.getPoint().y;
				  if ((x > this.x && x < this.x + this.width)
						  && (y > this.y && y < this.y + this.height)) {
					  doAction(event);
				  }
			  }
		  }
	  }

	private void doAction(InputEvent event) {
		if (actionDelegate != null) {
			  actionDelegate.doAction(event);
		  } else {
			  System.err.println("action is null");
		  }
	}
	  
	  public void keyEvent(KeyEvent event) {
		  if (keyDelegate != null) {
			  keyDelegate.keyEvent(event);
		  }
	  }
	  
	  public void draw() {
	    parent.fill(fillColor);
	    parent.stroke(0);
	    parent.rect(x, y, width, height);
	    if (label != null) {
	    	if (Math.max(parent.red(fillColor), Math.max(parent.green(fillColor), parent.blue(fillColor))) < 127) {
	    		parent.fill(255);
	    	} else {
	    		parent.fill(0);
	    	}
	    	parent.text(label, x+2, y+height-4);
	    }
	  }

	public void setMouseDelegate(MouseEventDelegate mouseDelegate) {
		this.mouseDelegate = mouseDelegate;
	}

	public void setKeyDelegate(KeyEventDelegate keyDelegate) {
		this.keyDelegate = keyDelegate;
	}

	public void setActionDelegate(ActionDelegate actionDelegate) {
		this.actionDelegate = actionDelegate;
	}

	public int getFillColor() {
		return fillColor;
	}

	public void setFillColor(int fillColor) {
		this.fillColor = fillColor;
	}

	public void setParent(PApplet parent) {
		this.parent = parent;
	}
	  
}
