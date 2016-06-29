package org.aardvark.processing;

import java.awt.event.MouseEvent;

import processing.core.PApplet;

public class Handle implements PAppletAware {

	transient PApplet parent;

	protected int x, y;
	protected int boxx, boxy;
	protected int position;
	int maxPosition;
	int width;
	int height;
	boolean over;
	boolean press;
	boolean moved;
	int fillColor;

	public Handle(PApplet parent, int posX, int posY, int position, int maxPosition, int width, int height)
	{
		this.parent = parent;
		x = posX;
		y = posY;
		this.maxPosition = maxPosition;
		this.width = width;
		this.height = height;
		setPosition(position);
		fillColor = parent.color(255);
	}
	
	public int getPosition() {
		return position;
	}

	public void update() 
	{
		boxx = x+position;
		boxy = y - height/2;

		if(press) {
			position = lock(parent.mouseX-parent.width/2-width/2, 0, maxPosition-width-1);
		}
	}

	public void draw() 
	{
//		parent.line(x, y, x+position, y);
		parent.fill(fillColor);
		parent.stroke(0);
		parent.rect(boxx, boxy, width, height);
		if(over || press) {
			parent.line(boxx, boxy, boxx+width, boxy+height);
			parent.line(boxx, boxy+height, boxx+width, boxy);
		}

	}

	boolean overRect(int x, int y, int width, int height) 
	{
		if (parent.mouseX >= x && parent.mouseX <= x+width && 
				parent.mouseY >= y && parent.mouseY <= y+height) {
			return true;
		} else {
			return false;
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
			press = false;
			over = isOver(event);
			break;
			
		case MouseEvent.MOUSE_MOVED:
			over = isOver(event);
			break;
			
		case MouseEvent.MOUSE_DRAGGED:
			if(press) {
				setPosition(event.getPoint().x - x);
				moved = true;
			}
			break;
			
		}
//		if (Math.random() < .1d)
//			PApplet.println(event);
	}

	void setPosition(int position) {
		this.position = lock(position, 0, maxPosition);
		boxx = x+this.position-width/2;
		boxy = y - height/2;
	}
	
	static int lock(int val, int minv, int maxv) 
	{ 
		return  PApplet.min(PApplet.max(val, minv), maxv); 
	} 
	
	boolean lock(int minPosition, int maxPosition) {
		if (position < minPosition) {
			setPosition(minPosition);
			return true;
		} else if (position > maxPosition) {
			setPosition(maxPosition);
			return true;
		}
		return false;
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
