package org.aardvark.processing;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.aardvark.processing.delegate.ActionDelegate;

import processing.core.PApplet;

public class ACheckbox implements PAppletAware {
	transient PApplet parent;

	private int boxx, boxy;
	private int width;
	private int height;
	private boolean over;
	private boolean press;
	private boolean checked;
	private char shortcutKey;
	private String label;
	private ActionDelegate actionDelegate;

	public ACheckbox(PApplet parent, int posX, int posY, int width, int height)
	{
		this.parent = parent;
		boxx = posX;
		boxy = posY;
		this.width = width;
		this.height = height;
	}

	public ACheckbox(PApplet parent, int posX, int posY, int width, int height, String label) {
		this(parent, posX, posY, width, height);
		this.label = label;
	}
	
	public ACheckbox(PApplet parent, int posX, int posY, int width, int height, String label, char shortcutKey) {
		this(parent, posX, posY, width, height, label);
		this.shortcutKey = shortcutKey;
	}
	
	public void draw() 
	{
//		parent.line(x, y, x+position, y);
		if (press) {
			parent.fill(150);
		} else if (over) {
			parent.fill(200);
		} else {
			parent.fill(255);
		}
		parent.stroke(0);
		parent.rect(boxx, boxy, width, height);
		if(checked) {
			parent.line(boxx, boxy, boxx+width, boxy+height);
			parent.line(boxx, boxy+height, boxx+width, boxy);
		}
	    if (label != null) {
	    	parent.fill(0);
	    	parent.text(label, boxx+width + 2, boxy+height-1);
	    }
	}

	public boolean isOver(MouseEvent event) {
		int x = event.getPoint().x;
		int y = event.getPoint().y;
		return ((x > boxx && x < boxx + width)
				&& (y > boxy && y < boxy + height));
	}
	
	public void mouseEvent(MouseEvent event) {
		switch (event.getID()) {
		case MouseEvent.MOUSE_PRESSED:
			if (isOver(event)) {
				press = true;
			}
			break;

		case MouseEvent.MOUSE_RELEASED:
			if (press) {
				if (isOver(event)) {
					checked = !checked;
					if (actionDelegate != null) {
						actionDelegate.doAction(event);
					}
				}
				press = false;
				over = isOver(event);
			}
			break;
			
		case MouseEvent.MOUSE_MOVED:
			over = isOver(event);
			break;
			
		}
	}
	
	  public void keyEvent(KeyEvent event) {
		  if (event.getID() == KeyEvent.KEY_TYPED 
				  && event.getKeyChar() == shortcutKey) {
			  checked = !checked;
			  if (actionDelegate != null) {
				  actionDelegate.doAction(event);
			  }
		  }
	  }
	  
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public char getShortcutKey() {
		return shortcutKey;
	}

	public void setShortcutKey(char shortcutKey) {
		this.shortcutKey = shortcutKey;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setActionDelegate(ActionDelegate actionDelegate) {
		this.actionDelegate = actionDelegate;
	}

	public void setParent(PApplet parent) {
		this.parent = parent;
	}

}
