package org.aardvark.processing;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.aardvark.processing.sensatron.LightDisplay;

public abstract class DisplayControlWidget<D extends LightDisplay> {
	
	
	
	public abstract void draw();
	public abstract void keyEvent(KeyEvent event);
	public abstract void mouseEvent(MouseEvent event);
	
}
