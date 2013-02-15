package com.badlogic.androidgames.framework.gl;

public class TextureRegion {
	public float u1, v1;
	public float u2, v2;
	public Texture texture;
	
	public float x, y, width, height;
	
	public TextureRegion(Texture texture, float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.u1 = x / texture.width;
		this.v1 = y / texture.height;
		this.u2 = this.u1 + width / texture.width;
		this.v2 = this.v1 + height / texture.height;
		this.texture = texture;
	}
	
	 public TextureRegion(float texWidth, float texHeight, float x, float y, float width, float height)  {
	      this.u1 = x / texWidth;                         // Calculate U1
	      this.v1 = y / texHeight;                        // Calculate V1
	      this.u2 = this.u1 + ( width / texWidth );       // Calculate U2
	      this.v2 = this.v1 + ( height / texHeight );     // Calculate V2
	 }
	 
	 public void setRegion(float newHeight) {
		this.height = newHeight;
		this.u1 = x / texture.width;
		this.v1 = y / texture.height;
		this.u2 = this.u1 + width / texture.width;
		this.v2 = this.v1 + height / texture.height;
	 }
}
