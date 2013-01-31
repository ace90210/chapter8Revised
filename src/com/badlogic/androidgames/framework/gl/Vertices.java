package com.badlogic.androidgames.framework.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.badlogic.androidgames.framework.impl.GLGraphics;

public class Vertices {
	final GL10 gl;
	final boolean hasColor;
	final boolean hasTexCoords;
	final int vertexSize;
	final IntBuffer vertices;
	final ShortBuffer indices;
	final int[] tmpBuffer;
	
	
	public Vertices(GL10 gl, int maxVertices, int maxIndices, boolean hasColor, boolean hasTexCoords)
	{
		this.gl = gl;
		this.hasColor = hasColor;
		this.hasTexCoords = hasTexCoords;
		this.vertexSize = (2 + (hasColor?4:0) + (hasTexCoords?2:0)) * 4;
		
		ByteBuffer buffer = ByteBuffer.allocateDirect(maxVertices * vertexSize);
		buffer.order(ByteOrder.nativeOrder());
		vertices = buffer.asIntBuffer();
		this.tmpBuffer = new int[maxVertices * vertexSize / 4];
		
		if(maxIndices > 0)
		{
			buffer = ByteBuffer.allocateDirect(maxIndices * Short.SIZE / 8);
			buffer.order(ByteOrder.nativeOrder());
			indices = buffer.asShortBuffer();
		}
		else
		{
			indices = null;
		}
	}
	
	public void setVertices(float[] vertices, int offset, int length)
	{
		this.vertices.clear();
		int len = offset + length;
		for(int i = offset, j = 0; i < len; i++, j++) {
			tmpBuffer[j] = Float.floatToRawIntBits(vertices[i]);
		}
		this.vertices.put(tmpBuffer, offset, length);
		this.vertices.flip();
	}
	
	public void setIndices(short[] indices, int offset, int length)
	{
		this.indices.clear();
		this.indices.put(indices, offset, length);
		this.indices.flip();
	}
	
	public void bind()
	{		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		vertices.position(0);
		gl.glVertexPointer(2, GL10.GL_FLOAT, vertexSize, vertices);
		
		if(hasColor)
		{
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			vertices.position(2);
			gl.glColorPointer(4, GL10.GL_FLOAT, vertexSize, vertices);
		}
		if(hasTexCoords)
		{
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			vertices.position(hasColor?6:2);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, vertexSize, vertices);
		}
	}
	
	public void unbind()
	{
		if(hasTexCoords)
		{
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}
		
		if(hasColor)
		{
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		}
	}
	
	public void draw(int primativeType, int offset, int numVertices)
	{		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		vertices.position(0);
		gl.glVertexPointer(2, GL10.GL_FLOAT, vertexSize, vertices);
		
		if(hasColor)
		{
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			vertices.position(2);
			gl.glColorPointer(4, GL10.GL_FLOAT, vertexSize, vertices);
		}
		if(hasTexCoords)
		{
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			vertices.position(hasColor?6:2);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, vertexSize, vertices);
		}
		
		if(indices != null)
		{
			indices.position(offset);
			gl.glDrawElements(primativeType, numVertices,  GL10.GL_UNSIGNED_SHORT,  indices);
		}
		else
		{
			gl.glDrawArrays(primativeType, offset, numVertices);
		}
		
		if(hasTexCoords)
		{
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}
		
		if(hasColor)
		{
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		}
	}
	
	public void drawOptimal(int primativeType, int offset, int numVertices)
	{				
		if(indices != null)
		{
			indices.position(offset);
			gl.glDrawElements(primativeType, numVertices,  GL10.GL_UNSIGNED_SHORT,  indices);
		}
		else
		{
			gl.glDrawArrays(primativeType, offset, numVertices);
		}
	}
}
