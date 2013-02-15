package com.badlogic.androidgames.myitems;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.gl.Texture;
import com.badlogic.androidgames.framework.gl.Animation;

public class PlayerV2 extends DynamicGameObject {
	private static int IMMUNE_LIMIT = 3; //seconds to remain immune after hit
	private float walkingTime, immuneTime;
	private Animation walkingLeft, walkingRight, climbing;
	private TextureRegion standing, shooting, hurt, currentState;
	public int life;	
	private boolean immune;
	
	public static enum PlayerState { WALKING_RIGHT, WALKING_LEFT, STANDING, SHOOTING, HURT, CLIMBING};
	private PlayerState playerState, lastState;
	
	public PlayerV2(float x, float y, float width, float height, int life, Texture texture) {
		super(x, y, width, height);
		walkingLeft = new AnimatedObject( 0.1f,
					  new TextureRegion(texture, 0,   349, 26, 33),
					  new TextureRegion(texture, 36,  349, 26, 33),
					  new TextureRegion(texture, 68,  349, 26, 33),
					  new TextureRegion(texture, 101, 349, 26, 33),
					  new TextureRegion(texture, 98,  317, 26, 33));
		walkingRight = new AnimatedObject( 0.1f,
					  new TextureRegion(texture, 0,   248, 26, 33),
					  new TextureRegion(texture, 34,  248, 26, 33),
					  new TextureRegion(texture, 69,  248, 26, 33),
					  new TextureRegion(texture, 103, 247, 26, 33),
					  new TextureRegion(texture, 1,   282, 26, 33));
		climbing =    new AnimatedObject( 0.2f,
					  new TextureRegion(texture, 163, 248, 26, 33),
					  new TextureRegion(texture, 69,  281, 26, 33),
					  new TextureRegion(texture, 1,   316, 26, 33));
		
		standing = new TextureRegion(texture, 68, 350, 26, 33);
		shooting = new TextureRegion(texture, 33, 280, 26, 33);
		hurt = new TextureRegion(texture, 196, 251, 26, 33);
		
		walkingTime = 0;
		immune = false;
		immuneTime = IMMUNE_LIMIT;
		this.currentState = standing;
		this.playerState = PlayerState.STANDING;
		this.lastState = playerState;
		this.life = life;
	}
	
	public void update(float deltaTime, PlayerState state) {
		walkingTime += deltaTime;
		if(immune){
			immuneTime -= deltaTime;
			if(immuneTime <= 0) {
				immune = false;
				immuneTime = IMMUNE_LIMIT;
				playerState = state;
			}
		} else {			
			playerState = state;				
		}
		
		position.add(velocity);
		bounds.lowerLeft.add(velocity);	
		switch(state){
			case SHOOTING: currentState = shooting; break;
			case HURT: currentState = hurt; break;
			case WALKING_LEFT: 	{
									this.lastState = PlayerState.WALKING_LEFT;
									currentState = walkingLeft.getKeyFrame(walkingTime, Animation.ANIMATION_LOOPING);
								} break;
			case WALKING_RIGHT: {
									this.lastState = PlayerState.WALKING_RIGHT;
									currentState = walkingRight.getKeyFrame(walkingTime, Animation.ANIMATION_LOOPING);
								} break;
			case CLIMBING: 		{
									currentState = climbing.getKeyFrame(walkingTime, Animation.ANIMATION_LOOPING);
								} break;
			default: {
						if(lastState == PlayerState.WALKING_LEFT) {
							currentState = walkingLeft.getKeyFrame(0, Animation.ANIMATION_LOOPING);
						} else {
							currentState = walkingRight.getKeyFrame(0, Animation.ANIMATION_LOOPING);
						}						
					}
		}
		
	}
	
	public TextureRegion getKeyFrame() {
		return currentState;
	}
	
	public boolean hit(){
		if(!immune){			
			immune = true;
			playerState = PlayerState.HURT;
			life--;
		}		
		return alive();
	}
	
	public boolean alive(){
		if(life <= 0) {
			return false;
		}
		return true;
	}
	
	public float getImmuneTime() {
		return immuneTime;
	}
	
	public void reset(float x, float y, int life) {
		this.position.set(x, y);
		this.updateBounds(x, y, this.bounds.width, this.bounds.height);
		this.velocity.set(0, 0);
		walkingTime = 0;
		immune = false;
		immuneTime = IMMUNE_LIMIT;
		this.currentState = standing;
		this.playerState = PlayerState.STANDING;
		this.life = life;
		
	}
	
	public void reset() {
		walkingTime = 0;
		this.velocity.set(0, 0);
		immune = false;
		immuneTime = IMMUNE_LIMIT;
		this.currentState = standing;
		this.playerState = PlayerState.STANDING;
	}
	
	public boolean hurt() {
		if( playerState == PlayerState.HURT ) {
			return true;
		}
		return false;
	}
}
