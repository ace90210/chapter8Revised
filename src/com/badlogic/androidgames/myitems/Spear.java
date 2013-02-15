package com.badlogic.androidgames.myitems;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.gl.Texture;
import com.badlogic.androidgames.framework.gl.Animation;

public class Spear  extends DynamicGameObject {
	//private static float SPEAR_REGION_MAX = 240.0f;
	private float speed, movingTime;
	private float realHeight;	
	private Animation anim;
	
	public boolean alive;
	public TextureRegion region;
	
	public Spear(float x, float y, float width, float height, float speed, Texture texture) {
		super(x, y, width, height);
		this.alive = false;
		this.anim =   new AnimatedObject( 0.01f,
					  new TextureRegion(texture, 261,   0, 9, 245),
					  new TextureRegion(texture, 279,  0, 9, 245),
					  new TextureRegion(texture, 297,  0, 9, 245),
					  new TextureRegion(texture, 315, 0, 9, 245),
					  new TextureRegion(texture, 333,  0, 9, 245),
					  new TextureRegion(texture, 351, 0, 9, 245),
					  new TextureRegion(texture, 368,  0, 9, 245));
		
		this.region =  new TextureRegion(texture, 368,  0, 9, 245);
		this.realHeight = height;
		this.speed = speed;
		this.movingTime = 0;
	}
	
	public void update(float deltaTime) {			
		movingTime += deltaTime;		
		
		region = anim.getKeyFrame(movingTime, Animation.ANIMATION_LOOPING);
		realHeight += speed * deltaTime * 2;
		position.y += speed * deltaTime;
		updateBounds(position.x, position.y, bounds.width, realHeight );		
	}
	
	public void shoot(float x, float y) {
		alive = true;
		this.realHeight = 64;
		this.position.set(x, y);
		updateBounds(position.x, position.y, bounds.width, realHeight );	
	}
}
