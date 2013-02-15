package com.badlogic.androidgames.framework;

import com.badlogic.androidgames.framework.math.Rectangle;
import com.badlogic.androidgames.framework.math.Circle;
import com.badlogic.androidgames.framework.math.Vector2;

public class GameObject {
	public final Vector2 position;
	public Rectangle bounds;
	public final Circle boundingCircle;
	
	public GameObject(float x, float y, float width, float height) 	{
		this.position = new Vector2(x, y);
		this.bounds = new Rectangle(x-width/2, y-height/2, width, height);
		this.boundingCircle = null;
	}
	
	public GameObject(float x, float y, float radius) 	{
		this.position = new Vector2(x, y);
		this.boundingCircle = new Circle(x-radius, y-radius, radius);
		this.bounds = null;
	}
	
	public void updateBounds(float x, float y, float width, float height) {
		this.bounds = new Rectangle(x-width/2, y-height/2, width, height);
	}
}
