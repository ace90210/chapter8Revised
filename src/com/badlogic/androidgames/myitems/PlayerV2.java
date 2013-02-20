package com.badlogic.androidgames.myitems;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.GameObject;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.gl.Texture;
import com.badlogic.androidgames.framework.gl.Animation;
import com.badlogic.androidgames.framework.math.OverlapTester;
import com.badlogic.androidgames.framework.math.Vector2;

import java.util.List;
import java.util.ArrayList;

public class PlayerV2 extends DynamicGameObject {
	private static int IMMUNE_LIMIT = 3; //seconds to remain immune after hit
	private float walkingTime, immuneTime;
	private Vector2 gravity;
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
		this.gravity = new Vector2(0, 0);
		this.playerState = PlayerState.STANDING;
		this.lastState = PlayerState.STANDING;
		this.life = life;
	}
	
	public void update(float deltaTime, PlayerState state, List<Ladder> ladders, List<GameObject> ... objects) {
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
			
		if(velocity.y <= 0) {
			velocity.y = 0;
		} else {
			position.add((velocity.x -gravity.x) * deltaTime, (velocity.y - gravity.y) * deltaTime);
			bounds.lowerLeft.add((velocity.x - gravity.x) * deltaTime, (velocity.y - gravity.y) * deltaTime);
			velocity.add(gravity.x * 3 * deltaTime, gravity.y * 3 * deltaTime);
		}
		
		boolean onFloor = onFloor(deltaTime, ladders, objects);
		if(bounds.lowerLeft.y <= 0) {
			position.y = bounds.height / 2;
			bounds.lowerLeft.y = 0;
			velocity.y = 0;
			onFloor = true;
		}
		
		if(!onFloor || velocity.y > 0) {
			position.add(gravity.x * deltaTime, gravity.y * deltaTime);		
			bounds.lowerLeft.add(gravity.x * deltaTime, gravity.y * deltaTime);
		} 
	
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
	
	public void setVelocity(float x, float y) {
		velocity.set(x, y);
	}
	
	public boolean onFloor(float deltaTime,  List<Ladder> ladders, List<GameObject> ... objects) {
		boolean onFloor = false;
		if(bounds.lowerLeft.y <= 0) {
			onFloor = true;
		}
		for(Ladder ladder: ladders) {
			if(position.x - bounds.width / 4 < ladder.bounds.lowerLeft.x + ladder.bounds.width 		&&
			   position.x + bounds.width / 4 > ladder.bounds.lowerLeft.x 							&&
			   position.y - bounds.height / 2 > ladder.bounds.lowerLeft.y 							&&
			   position.y + bounds.height / 2 < (ladder.position.y + ladder.bounds.height / 2) + bounds.height) {					
			   onFloor = true;
			}			
		}	
		for(List<GameObject> objectList: objects) {
			for(GameObject object: objectList) {
				if(position.x - bounds.width / 4 < object.bounds.lowerLeft.x + object.bounds.width 		&&
				   position.x + bounds.width / 4 > object.bounds.lowerLeft.x 							&&
				   position.y >= object.bounds.lowerLeft.y + object.bounds.height	&&
				   position.y + bounds.height / 2 <= (object.position.y + object.bounds.height / 2) + bounds.height) {	
					//if on ladder onFloor will be true
					if(!onFloor) {
						position.y = (object.position.y + object.bounds.height / 2) + bounds.height / 2;		//update position and bounds
						bounds.lowerLeft.set(position.x - bounds.width / 2, position.y - bounds.height / 2);
					}
				   
				   onFloor = true;
				}			
			}	
		}
		return onFloor;
	}
	
	public void setGravity(float x, float y) {
		this.gravity.x = x;
		this.gravity.y = y;
	}
	
	public void movePlayer(float x, float y, float deltaTime) {
		position.add(x * deltaTime, y * deltaTime);
		bounds.lowerLeft.add(x * deltaTime, y * deltaTime);
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
