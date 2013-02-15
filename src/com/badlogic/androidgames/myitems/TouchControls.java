package com.badlogic.androidgames.myitems;

import java.util.List;
import java.util.ArrayList;

import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.gl.Texture;

public class TouchControls {
	public List<TouchElement> touchElements;
	public enum ButtonList {UP, DOWN, LEFT, RIGHT, A, B, START };
	private TextureRegion aRegion, bRegion, startRegion, upRegion, leftRegion;
	public boolean[] activeButtons;
	
	public TouchControls(Texture texture, float worldWidth, float worldHeight) {
		touchElements = new ArrayList<TouchElement>();
		
		aRegion = new TextureRegion(texture, 128, 0, 64, 64); 
		bRegion = new TextureRegion(texture, 192, 0, 64, 64);
		startRegion = new TextureRegion(texture, 194, 69, 63, 26);
		upRegion = new TextureRegion(texture, 135, 85, 22, 28); 
		leftRegion = new TextureRegion(texture, 158, 91, 28, 22);
		
		
		touchElements.add(new TouchElement(110, 142,  48,  64, upRegion));				//UP	
		touchElements.get(0).setBoundsBufferUp(30);
		touchElements.add(new TouchElement(110, 40,  48,  -64, upRegion));				//DOWN
		touchElements.get(1).setBoundsBufferDown(30);
		
		touchElements.add(new TouchElement(60,  94,  64,  48, leftRegion));				//LEFT
		touchElements.get(2).setBoundsBufferLeft(30);
		touchElements.add(new TouchElement(160,  94,  -64,  48, leftRegion));			//RIGHT
		touchElements.get(3).setBoundsBufferRight(30);
		
		touchElements.add(new TouchElement(worldWidth - 192, 42, 70, 70, aRegion));		//A
		touchElements.get(4).setBothBoundsBuffers(10);
		touchElements.get(4).setWait(250);
		
		touchElements.add(new TouchElement(worldWidth - 92, 42, 70, 70, bRegion));		//B
		touchElements.get(5).setBothBoundsBuffers(10);
		touchElements.get(5).setWait(250);
		
		touchElements.add(new TouchElement(worldWidth / 2, 42, 128, 50, startRegion));	//Start
		touchElements.get(6).setBoundsWidthBuffer(20);
		touchElements.get(6).setBoundsHeightBuffer(15);
		touchElements.get(6).setWait(300);	
		
		activeButtons = new boolean[touchElements.size()];
		resetActiveButtons();
	}

	public TouchElement getElement(ButtonList button) {
		TouchElement element;
		switch(button) {
			case UP: 	element = touchElements.get(0); break;
			case DOWN:	element = touchElements.get(1); break;
			case LEFT:	element = touchElements.get(2); break;
			case RIGHT:	element = touchElements.get(3); break;
			case A:		element = touchElements.get(4); break;
			case B:		element = touchElements.get(5); break;
			default: 	element = touchElements.get(6); break;
		}
		return element;
	}
	
	public void resetActiveButtons() {
		for(int i = 0; i < activeButtons.length; i++) {
			activeButtons[i] = false;
		}
	}
}
