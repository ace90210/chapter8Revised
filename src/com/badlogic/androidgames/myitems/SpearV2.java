package com.badlogic.androidgames.myitems;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.gl.Texture;
import com.badlogic.androidgames.framework.gl.Animation;

public class SpearV2  extends DynamicGameObject {	
	private float speed, movingTime;
	private float realHeight;	
	private Animation anim;
	private float life, heightLimit;
	public float activeTime;
	public boolean alive, stick;
	public TextureRegion region, stickRegion;
	
	//life > 0 = sticky
	public SpearV2(float x, float y, float width, float height, float speed, float life, Texture texture) {
		super(x, y, width, height);
		this.alive = false;
		if(life > 0) {
			this.anim =   new AnimatedObject( 0.01f,
					  new TextureRegion(texture, 262,   247, 9, 245),
					  new TextureRegion(texture, 280,  247, 9, 245),
					  new TextureRegion(texture, 298,  247, 9, 245),
					  new TextureRegion(texture, 316, 247, 9, 245),
					  new TextureRegion(texture, 334,  247, 9, 245),
					  new TextureRegion(texture, 352, 247, 9, 245),
					  new TextureRegion(texture, 369,  247, 9, 245));
		
			this.region =  new TextureRegion(texture, 369,  247, 9, 245);
			this.stickRegion = new TextureRegion(texture, 453,  0, 9, 245);
		} else {
			this.anim =   new AnimatedObject( 0.01f,
						  new TextureRegion(texture, 261,   0, 9, 245),
						  new TextureRegion(texture, 279,  0, 9, 245),
						  new TextureRegion(texture, 297,  0, 9, 245),
						  new TextureRegion(texture, 315, 0, 9, 245),
						  new TextureRegion(texture, 333,  0, 9, 245),
						  new TextureRegion(texture, 351, 0, 9, 245),
						  new TextureRegion(texture, 368,  0, 9, 245));
			
			this.region =  new TextureRegion(texture, 368,  0, 9, 245);
		}
		this.realHeight = height;
		this.heightLimit = 460;
		this.speed = speed;
		this.movingTime = 0;
		this.life = life;
		this.stick = false;
	}
	
	public void setHeightLimit(float limit) {
		this.heightLimit = limit;
	}
	
	public void update(float deltaTime) {			
		movingTime += deltaTime;			
		
		if(stick) {
			activeTime -= deltaTime;
			region = stickRegion;
			if(activeTime <= 0) {
				stick = false;
				alive = false;
			}
		} else {
			if(position.y + (bounds.height / 2) >= heightLimit) {
				position.y = heightLimit - (bounds.height / 2);
				kill();
			}
			region = anim.getKeyFrame(movingTime, Animation.ANIMATION_LOOPING);
			realHeight += speed * deltaTime * 2;
			position.y += speed * deltaTime;
			updateBounds(position.x, position.y, bounds.width, realHeight );
		}
	}
	
	public void shoot(float x, float y) {	
		stick = false;
		alive = true;
		this.realHeight = 64;
		this.position.set(x, y);
		updateBounds(position.x, position.y, bounds.width, realHeight );	
	}
	
	public void kill() {
		activeTime = life;
		stick = true;
		if(activeTime <= 0) {
			stick = false;
			alive = false;
		}
	}
}
