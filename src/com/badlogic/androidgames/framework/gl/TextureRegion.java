package com.badlogic.androidgames.framework.gl;

public class TextureRegion {
	public final float u1, v1;
	public final float u2, v2;
	public Texture texture;
	
	public TextureRegion(Texture texture, float x, float y, float width, float height) {
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
}
