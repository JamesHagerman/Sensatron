package org.aardvark.processing.sensatron;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;

import org.aardvark.processing.PAppletAware;

import processing.core.PApplet;

public abstract class DisplayControlWidget<D extends LightDisplay> implements PAppletAware  {

	public static final int WIDTH = 200;
	public static final int HEIGHT = 200;
	
	protected int x, y;
	protected boolean minimized;
	protected transient PApplet parent;
	protected D display;
	private String name;

	public DisplayControlWidget(int x, int y, D display, PApplet parent) {
		this.x = x;
		this.y = y;
		this.display = display;
		this.parent = parent;
	}

	public final void draw() {
		if (isMinimized()) {
			drawMinimized();
		} else {
			drawMaximized();
		}
	}
	
	protected void drawMinimized() {
		parent.stroke(0, 0);
		parent.fill(0);
		parent.text(name == null ? display.getClass().getSimpleName() : name);
		parent.stroke(0);
		parent.fill(0, 0);
		parent.rect(x, y, 80, 20);
	}

	public boolean isMinimized() {
		return minimized;
	}

	public void setMinimized(boolean minimized) {
		this.minimized = minimized;
	}
	
	public void minimize() {
		setMinimized(true);
	}
	
	public void restore() {
		setMinimized(false);
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	protected abstract void drawMaximized();

	public abstract void keyEvent(KeyEvent event);

	public abstract void mouseEvent(MouseEvent event);
	
	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public D getDisplay() {
		return display;
	}
	
	public void setDisplay(D lightDisplay) {
		this.display = lightDisplay;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PApplet getParent() {
		return parent;
	}

	public void setParent(PApplet parent) {
		this.parent = parent;
		for (Field f : this.getClass().getDeclaredFields()) {
			if (PAppletAware.class.isAssignableFrom(f.getType())) {
				try {
					((PAppletAware) f.get(this)).setParent(parent);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

}