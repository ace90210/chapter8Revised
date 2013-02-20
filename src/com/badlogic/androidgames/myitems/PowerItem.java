package com.badlogic.androidgames.myitems;

import java.util.List;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.math.Vector2;
import com.badlogic.androidgames.framework.GameObject;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.myitems.Weapon.MODE;

public class PowerItem extends DynamicGameObject {
	public float fallSpeed, lifeLimit, life;
	public int value;
	public MODE mode;
	public TextureRegion region;
	public boolean alive;
	
	public PowerItem(TextureRegion region, float x, float y, float width, float height, float life, MODE mode) {
		super(x, y, width, height);
		this.region = region;
		this.alive = false;
		this.fallSpeed = -200;
		this.mode = mode;
		this.life = life;
		lifeLimit = life;
	}
	
	public void update(float deltaTime, List<GameObject> ... objects) {
		if(alive) {
			life -= deltaTime;
			if(life < 0) {
				alive = false;
			}		
			if(!onFloor(objects)) {
				position.add(0, fallSpeed * deltaTime);
				bounds.lowerLeft.add(0, fallSpeed * deltaTime);
			}
		}
	}
	
	public void setPosition(Vector2 pos) {
		life = lifeLimit;
		position.set(pos.x, pos.y);
		updateBounds(pos.x, pos.y, bounds.width, bounds.height);
	}
	
	public boolean onFloor(List<GameObject> ... objects) {
		boolean onFloor = false;
		if(bounds.lowerLeft.y <= 0) {
			position.y = bounds.height / 2;
			onFloor = true;
		}
		for(List<GameObject> objectList: objects) {
			for(GameObject object: objectList) {
				if(position.x - bounds.width / 4 < object.bounds.lowerLeft.x + object.bounds.width 		&&
				   position.x + bounds.width / 4 > object.bounds.lowerLeft.x 							&&
				   position.y - bounds.height / 2 > object.bounds.lowerLeft.y 							&&
				   position.y + bounds.height / 2 <= (object.position.y + object.bounds.height / 2) + bounds.height) {
				   onFloor = true;
				}			
			}	
		}
		return onFloor;
	}
}
