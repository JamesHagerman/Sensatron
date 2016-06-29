package org.aardvark.processing.sensatron;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import processing.core.PApplet;

public class EmptyControlWidget extends DisplayControlWidget<LightDisplay> {

	public EmptyControlWidget(int x, int y, LightDisplay display, PApplet parent) {
		super(x, y, display, parent);
	}

	@Override
	protected void drawMaximized() {}

	@Override
	public void keyEvent(KeyEvent event) {}

	@Override
	public void mouseEvent(MouseEvent event) {}

}
