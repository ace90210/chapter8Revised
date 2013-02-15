package com.badlogic.androidgames.myitems;

import com.badlogic.androidgames.framework.gl.Animation;
import com.badlogic.androidgames.framework.gl.TextureRegion;

public class AnimatedObject extends Animation {
	public float walkingTime = 0;
	TextureRegion currKeyFrame;
	TextureRegion standingKeyFrame;
	TextureRegion shootingKeyFrame;
	
	public static enum AnimationState { WALKING_RIGHT, WALKING_LEFT, STANDING, SHOOTING};
	
	public AnimatedObject(float frameDuration, TextureRegion ... keyFrames) {
		super(frameDuration, keyFrames);
		standingKeyFrame = keyFrames[0];
		if(keyFrames.length > 1) {
			shootingKeyFrame = keyFrames[1];
		} else {
			shootingKeyFrame = keyFrames[0];
		}
	}
	
	public TextureRegion getKeyFrame(float stateTime,  AnimationState state) {
		int frameNumber = (int)(stateTime / this.frameDuration);
		switch(state) 
		{
			case STANDING: 	  	 {
								  	 currKeyFrame = standingKeyFrame;
							  	 } break;
			case SHOOTING: 	  	 {
							  	      currKeyFrame = shootingKeyFrame;
							     } break;
			case WALKING_LEFT:
			case WALKING_RIGHT:  {
								     currKeyFrame = keyFrames[frameNumber % keyFrames.length];
							     } break;
								  
		};
		return currKeyFrame;
	}
}