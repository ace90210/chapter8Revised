package com.badlogic.androidgames.myitems;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.gl.Texture;
import com.badlogic.androidgames.framework.gl.Vertices;

public class TexturedObject extends DynamicGameObject {
	Texture texture;
	public Vertices vertices;
	
	public TexturedObject(float x, float y, float width, float height, Texture texture, Vertices vertices) {
		super(x, y, width, height);
		this.texture = texture;
		this.vertices = vertices;
	}
	
	public void bind() {
		texture.bind();
		vertices.bind();
	}
	
	public void unbind() {
		vertices.unbind();
	}
}
