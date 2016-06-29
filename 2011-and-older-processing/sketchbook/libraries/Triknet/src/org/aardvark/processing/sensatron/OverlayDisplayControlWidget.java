package org.aardvark.processing.sensatron;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;

import javax.swing.JFileChooser;

import org.aardvark.processing.ACheckbox;
import org.aardvark.processing.PButton;
import org.aardvark.processing.delegate.ActionDelegate;
import org.aardvark.processing.sensatron.OverlayDisplay.OverlayMode;

import processing.core.PApplet;

public class OverlayDisplayControlWidget extends DisplayControlWidget<OverlayDisplay> {

	ACheckbox[] modeCheckboxes = new ACheckbox[OverlayDisplay.OverlayMode.values().length];
	PButton loadBaseButton;
	PButton loadOverlayButton;
	ACheckbox invertBaseCheckbox;
	ACheckbox invertOverlayCheckbox;
	
	JFileChooser fileChooser = new JFileChooser("/");
	
	public OverlayDisplayControlWidget(int x, int y, OverlayDisplay display, PApplet parent) {
		super(x, y, display, parent);
		int i = 0;
		for (OverlayMode mode : OverlayMode.values()) {
			final int index = i;
			final String modeName = mode.name();
			modeCheckboxes[i] = new ACheckbox(parent, x + 10, y + 10 + (i * 15), 10, 10, mode.toString());
			if (display != null && mode == display.getMode()) {
				modeCheckboxes[i].setChecked(true);
			}
			modeCheckboxes[i].setActionDelegate(new ActionDelegate() {
				public void doAction(InputEvent event) {
					getDisplay().setMode(OverlayMode.valueOf(modeName));
					modeChecked(index);
				}
			});
			i++;
		}
		loadBaseButton = new PButton(parent, x + 110, y + 10, 70, 20, "Load Base", 
				new ActionDelegate() {
			public void doAction(InputEvent event) {
				loadBaseDisplay();
			}
		});
		loadOverlayButton = new PButton(parent, x + 110, y + 40, 80, 20, "Load Overlay", 
				new ActionDelegate() {
			public void doAction(InputEvent event) {
				loadOverlayDisplay();
			}
		});
		invertBaseCheckbox = new ACheckbox(parent, x + 10, y + 180, 10, 10, "Invert");
		invertOverlayCheckbox = new ACheckbox(parent, x + 110, y + 180, 10, 10, "Invert");
		invertBaseCheckbox.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				getDisplay().setInvertBase(invertBaseCheckbox.isChecked());
			}
		});
		invertOverlayCheckbox.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				getDisplay().setInvertOverlay(invertOverlayCheckbox.isChecked());
			}
		});
	}
	
	public LightDisplay loadDisplay(StringBuffer nameBuffer) {
		int returnVal = -1;
		try {
//			choosingFile = true;
			Field choosingFileFlag = parent.getClass().getField("choosingFile");
			choosingFileFlag.setBoolean(parent, true);
			returnVal = fileChooser.showOpenDialog(parent); 
//			choosingFile = false;
			choosingFileFlag.setBoolean(parent, false);
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (NoSuchFieldException e1) {
			e1.printStackTrace();
		}
		if (returnVal == JFileChooser.APPROVE_OPTION) { 
			File file = fileChooser.getSelectedFile();
			if (file.isFile()) {
				ObjectInputStream is = null;
				try {
					is = new ObjectInputStream(new FileInputStream(file));
					LightDisplay newDisplay = (LightDisplay) is.readObject();
					nameBuffer.append(file.getName());
					return newDisplay;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return null;
	}
	
	public void loadBaseDisplay() {
		StringBuffer name = new StringBuffer();
		LightDisplay newBase = loadDisplay(name);
		if (newBase != null) {
			display.setBase(newBase);
			display.setBaseName(name.toString());
		}
//		DisplayControlWidget<? extends LightDisplay> newWidget = DisplayControlWidgetFactory.createWidget(newDisplay, parent, x + 10, y + 150);
//		if (newWidget != null) {
//		}

	}
	
	public void loadOverlayDisplay() {
		StringBuffer name = new StringBuffer();
		LightDisplay newOverlay = loadDisplay(name);
		if (newOverlay != null) {
			display.setOverlay(newOverlay);
			display.setOverlayName(name.toString());
		}
//		DisplayControlWidget<? extends LightDisplay> newWidget = DisplayControlWidgetFactory.createWidget(newDisplay, parent, x + 10, y + 150);
//		if (newWidget != null) {
//		}
	}
	
	@Override
	protected void drawMaximized() {
	    parent.stroke(0);
	    parent.fill(0, 0);
	    parent.rect(x, y, WIDTH, HEIGHT);

		for (ACheckbox checkbox : modeCheckboxes) {
			checkbox.draw();
		}
		loadBaseButton.draw();
		loadOverlayButton.draw();
		invertBaseCheckbox.draw();
		invertOverlayCheckbox.draw();
		
		parent.stroke(0, 0);
		parent.fill(0);
		parent.text("Base Display:", x + 10, y + 140);
		parent.text(display.getBaseName(), x + 10, y + 160);
		parent.text("Overlay Display:", x + 110, y + 140);
		parent.text(display.getOverlayName(), x + 110, y + 160);
	}
	
	private void modeChecked(int idx) {
		for (int i = 0; i < modeCheckboxes.length; i++) {
			if (i == idx) {
				modeCheckboxes[i].setChecked(true);
			} else {
				modeCheckboxes[i].setChecked(false);
			}
		}
	}

	@Override
	public void mouseEvent(MouseEvent event) {
		for (ACheckbox checkbox : modeCheckboxes) {
			checkbox.mouseEvent(event);
		}
		loadBaseButton.mouseEvent(event);
		loadOverlayButton.mouseEvent(event);
		invertBaseCheckbox.mouseEvent(event);
		invertOverlayCheckbox.mouseEvent(event);
	}

	@Override
	public void keyEvent(KeyEvent event) {
		for (ACheckbox checkbox : modeCheckboxes) {
			checkbox.keyEvent(event);
		}
		loadBaseButton.keyEvent(event);
		loadOverlayButton.keyEvent(event);
		invertBaseCheckbox.keyEvent(event);
		invertOverlayCheckbox.keyEvent(event);
	}

}
