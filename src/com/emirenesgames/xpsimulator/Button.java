package com.emirenesgames.xpsimulator;

import java.awt.Rectangle;

public class Button extends Rectangle {
	
	public Button(int x,int y,int width,int height) {
		super(x,y,width,height);
	}
	
	private boolean hide = false;
	
	public void show() {
		hide = false;
	}
	
	public void hide() {
		hide = true;
	}
	
	public void setEnable(boolean bool) {
		hide = bool;
	}
	
	public boolean getEnable() {
		return hide;
	}

	@Override
	public boolean intersects(Rectangle r) {
		if(!hide) {
			return super.intersects(r);
		}
		return false;
	}
	
	

}
