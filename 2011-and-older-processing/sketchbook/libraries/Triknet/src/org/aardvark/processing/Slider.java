package org.aardvark.processing;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.aardvark.processing.delegate.ActionDelegate;

import processing.core.PApplet;

public class Slider implements PAppletAware {
	
	protected transient PApplet parent;
	protected Handle[] handles;
	protected int x;
	protected int y;
	protected int length;
	protected int[] positions;
	protected String label;
	protected char shortcutKey;
	protected boolean selected;
	protected int selectedHandle;
	protected ActionDelegate actionDelegate;
	protected boolean showValues;
	
	public Slider(PApplet parent, int x, int y, int length, int numHandles) {
		this(parent, x, y, length, numHandles, 6, 10);
	}
	
	public Slider(PApplet parent, int x, int y, int length, int numHandles, int handleWidth, int handleLength) {
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.length = length;
		handles = new Handle[numHandles];
		positions = new int[numHandles];
		int initPos = 0;
		if (numHandles == 1) {
			handles[0] = new Handle(parent, x, y, length/2, length, handleWidth, handleLength);
		} else {
			for (int i = 0; i < numHandles; i++) {
				handles[i] = new Handle(parent, x, y, initPos, length, handleWidth, handleLength);
				initPos += length / (numHandles-1);
			}
		}
	}
	
	public void setHandlePosition(int handleNum, int position) {
		handles[handleNum].setPosition(position);
	}
	
	public void setHandlePosition(int handleNum, double position) {
		handles[handleNum].setPosition((int) (position * length));
	}
	
	public int[] getPositions() {
		return positions;
	}
	
	public float[] getNormalizedPositions() {
		float[] result = new float[positions.length];
		for (int i = 0; i < positions.length; i++) {
			result[i] = positions[i] / (float) length;
		}
		return result;
	}
	
	public void draw() {
	    if (label != null) {
	    	parent.fill(0);
	    	int y = this.y;
	    	if (handles.length > 0) y += handles[0].height/2;
	    	parent.text(label, this.x+2, y+12);
	    }
		parent.line(x, y, x+length, y);
		for (int i = 0; i < handles.length; i++) {
			Handle h = handles[i];
			h.draw();
			positions[i] = h.position;
			if (showValues) {
				parent.fill(0);
				parent.text(Integer.toString(h.position), x + h.position - 5, y - h.height/2 - 1);
			}
		}
	}
	
	
	public void mouseEvent(MouseEvent event) {
		switch (event.getID()) {
		case MouseEvent.MOUSE_PRESSED:
			if (selected) {
				selected = false;
				handles[selectedHandle].press = false;
			}
		}
		for (int i = 0; i < handles.length; i++) {
			Handle h = handles[i];
			h.mouseEvent(event);
			if (h.moved) {
				constrainHandle(i);
				h.moved = false;
				if (actionDelegate != null) actionDelegate.doAction(event);
			}
		}
	}

	private void constrainHandle(int handleNum) {
		int minPos = 0;
		int maxPos = length;
		if (handleNum > 0)
			minPos = handles[handleNum-1].position + handles[handleNum-1].width/2;
		if (handleNum+1 < handles.length)
			maxPos = handles[handleNum+1].position - handles[handleNum+1].width/2;
		handles[handleNum].lock(minPos, maxPos);
	}
	
	public void keyEvent(KeyEvent event) {
		if (event.getID() == KeyEvent.KEY_TYPED) {
			if (selected && event.getKeyChar() != '\t') {
				selected = false;
				handles[selectedHandle].press = false;
			} else if (event.getKeyChar() == shortcutKey) {
				selected = true;
				handles[selectedHandle].press = true;
			}
		}
		if (selected && event.getID() == KeyEvent.KEY_PRESSED) {
			int moveScale = event.isControlDown() ? 5 : 1;
			switch (event.getKeyCode()) {
			case KeyEvent.VK_TAB:
				handles[selectedHandle].press = false;
				if (event.isShiftDown()) {
					selectedHandle--;
					if (selectedHandle < 0) selectedHandle = handles.length - 1;
				} else {
					selectedHandle++;
					selectedHandle %= handles.length;
				}
				handles[selectedHandle].press = true;
				break;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_KP_UP:
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_KP_RIGHT:
				setHandlePosition(selectedHandle, getPositions()[selectedHandle] + moveScale);
				constrainHandle(selectedHandle);
				if (actionDelegate != null) actionDelegate.doAction(event);
				break;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_KP_DOWN:
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_KP_LEFT:
				setHandlePosition(selectedHandle, getPositions()[selectedHandle] - moveScale);
				constrainHandle(selectedHandle);
				if (actionDelegate != null) actionDelegate.doAction(event);
				break;
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_ENTER:
				selected = false;
				handles[selectedHandle].press = false;
				break;
			}
		}
	}
	
	public void setHandleColor(int handleNum, int color) {
		if (handleNum > handles.length) return;
		handles[handleNum].setFillColor(color);
	}
	
	public int getLength() {
		return length;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public char getShortcutKey() {
		return shortcutKey;
	}

	public void setShortcutKey(char shortcutKey) {
		this.shortcutKey = shortcutKey;
	}

	public ActionDelegate getActionDelegate() {
		return actionDelegate;
	}

	public void setActionDelegate(ActionDelegate actionDelegate) {
		this.actionDelegate = actionDelegate;
	}

	public boolean isShowValues() {
		return showValues;
	}

	public void setShowValues(boolean showValues) {
		this.showValues = showValues;
	}

	public void setParent(PApplet parent) {
		this.parent = parent;
		for (Handle h : handles) {
			h.setParent(parent);
		}
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
}
