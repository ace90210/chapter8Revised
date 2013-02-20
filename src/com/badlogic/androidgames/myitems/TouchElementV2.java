package com.badlogic.androidgames.myitems;

import com.badlogic.androidgames.framework.math.Vector2;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.math.Rectangle;

public class TouchElementV2 {
	public Vector2 position;
	public float width, height;
	public TextureRegion region;
	public Rectangle bounds;
	private Vector2 basePosition;
	private Rectangle baseBounds;
	double waitTime, lastUsed;
	
	TouchElementV2(float x, float y, float width, float height, TextureRegion region) {
		this.region = region;
		this.position = new Vector2(x, y);
		this.width = width;
		this.height = height;		
		this.waitTime = 0;
		this.lastUsed = System.nanoTime() / 1000000;
		
		//correct for inverted texture direction
		if(width < 0) {
			width = -width;
		}
		if(height < 0) {
			height = -height;
		}
		this.bounds = new Rectangle(x - width / 2,y - height / 2, width, height);
		this.baseBounds = this.bounds;
		this.basePosition = this.position;
	}
	
	public void setWait(int wait) {
		this.waitTime = wait;
	}
	
	public void setBoundsWidthBuffer(int buffer) {
		this.bounds.lowerLeft.x = (this.basePosition.x - this.baseBounds.width / 2) - buffer;
		this.bounds.width = this.baseBounds.width + 2 * buffer; 
	}
	
	public void setBoundsHeightBuffer(int buffer) {
		this.bounds.lowerLeft.y = (this.basePosition.y - this.baseBounds.height / 2) - buffer;
		this.bounds.height = this.baseBounds.height + 2 * buffer; 
	}
	
	public void setBothBoundsBuffers(int buffer) {
		this.bounds.lowerLeft.x = (this.basePosition.x - this.baseBounds.width / 2) - buffer;
		this.bounds.width = this.baseBounds.width + 2 * buffer;
		this.bounds.lowerLeft.y = (this.basePosition.y - this.baseBounds.height / 2) - buffer;
		this.bounds.height = this.baseBounds.height + 2 * buffer; 
	}
	
	public void setBoundsBufferUp(int buffer) {
		this.bounds.height = this.baseBounds.height + buffer; 
	}
	
	public void setBoundsBufferDown(int buffer) {
		this.bounds.height = this.baseBounds.height + buffer; 
		this.bounds.lowerLeft.y = (this.basePosition.y - this.baseBounds.height / 2) - buffer;
	}
	
	public void setBoundsBufferRight(int buffer) {
		this.bounds.width = this.baseBounds.width + buffer; 
	}
	
	public void setBoundsBufferLeft(int buffer) {
		this.bounds.width = this.baseBounds.width + buffer; 
		this.bounds.lowerLeft.x = (this.basePosition.x - this.baseBounds.width / 2) - buffer;
	}
	
	
	public boolean ready() {
		double now = System.nanoTime() / 1000000;
		if(now - lastUsed > waitTime){
			return true;
		}
		return false;
	}
	
	public void use(float x, float y) {
		lastUsed = System.nanoTime() / 1000000;
	}
}
