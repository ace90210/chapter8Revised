package com.badlogic.androidgames.myitems;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.gl.Texture;
import com.badlogic.androidgames.myitems.AnimatedObject;

public class Player extends DynamicGameObject {
	public float walkingTime = 0;
	private AnimatedObject player;
	
	public Player(float x, float y, float width, float height, Texture texture) {
		super(x, y, width, height);
		player = new AnimatedObject( 0.1f,
				  new TextureRegion(texture, 10,   168, 54, 74),
				  new TextureRegion(texture, 64,   168, 54, 74),
				  new TextureRegion(texture, 116,   168, 54, 74),
				  new TextureRegion(texture, 178,   168, 54, 74),
				  new TextureRegion(texture, 254,   168, 54, 74),
				  new TextureRegion(texture, 314,   168, 54, 74),
				  new TextureRegion(texture, 367,   168, 54, 74));
		player.standingKeyFrame = new TextureRegion(texture, 7, 12, 42, 75);
		player.shootingKeyFrame = new TextureRegion(texture, 438, 12, 42, 75);
	}
	
	public TextureRegion getKeyFrame(float deltaTime, AnimatedObject.AnimationState state) {
		walkingTime += deltaTime;
		return player.getKeyFrame(walkingTime, state);
	}
}
