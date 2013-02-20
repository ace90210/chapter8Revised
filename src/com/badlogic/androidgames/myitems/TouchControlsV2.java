package com.badlogic.androidgames.myitems;

import java.util.List;
import java.util.ArrayList;

import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.gl.Texture;

public class TouchControlsV2 {
	public List<TouchElementV2> touchElements;
	public enum ButtonList {STICK, A, B, START };
	private TextureRegion aRegion, bRegion, startRegion, stickRegion;
	public boolean[] activeButtons;
	
	public TouchControlsV2(Texture texture, float worldWidth, float worldHeight) {
		touchElements = new ArrayList<TouchElementV2>();
		
		aRegion = new TextureRegion(texture, 128, 0, 64, 64); 
		bRegion = new TextureRegion(texture, 192, 0, 64, 64);
		startRegion = new TextureRegion(texture, 194, 69, 63, 26);
		stickRegion = new TextureRegion(texture, 194, 130, 62, 63); 		
		
		touchElements.add(new TouchElementV2(100, 100,  160,  160, stickRegion));			//joystick	
		touchElements.get(0).setBothBoundsBuffers(15);		
		
		touchElements.add(new TouchElementV2(worldWidth - 210, 42, 70, 70, aRegion));		//A
		touchElements.get(1).setBothBoundsBuffers(10);
		touchElements.get(1).setWait(250);
		
		touchElements.add(new TouchElementV2(worldWidth - 92, 42, 70, 70, bRegion));		//B
		touchElements.get(2).setBothBoundsBuffers(10);
		touchElements.get(2).setWait(250);
		
		touchElements.add(new TouchElementV2(worldWidth - 64, worldHeight - 42, 128, 50, startRegion));	//Start
		touchElements.get(3).setBoundsWidthBuffer(20);
		touchElements.get(3).setBoundsHeightBuffer(15);
		touchElements.get(3).setWait(300);	
		
		activeButtons = new boolean[touchElements.size()];
		resetActiveButtons();
	}

	public TouchElementV2 getElement(ButtonList button) {
		TouchElementV2 element;
		switch(button) {
			case STICK:	element = touchElements.get(0); break;			
			case A:		element = touchElements.get(1); break;
			case B:		element = touchElements.get(2); break;
			default: 	element = touchElements.get(3); break;
		}
		return element;
	}
	
	public void resetActiveButtons() {
		for(int i = 0; i < activeButtons.length; i++) {
			activeButtons[i] = false;
		}
	}
}
