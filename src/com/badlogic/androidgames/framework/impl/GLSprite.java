package com.badlogic.androidgames.framework.impl;

import javax.microedition.khronos.opengles.GL10;

import com.badlogic.androidgames.framework.gl.Vertices;
import com.badlogic.androidgames.framework.impl.GLGraphics;

public class GLSprite {
	 GL10 gl;
     Vertices spriteVertices;
     float height;
     float width;
     
     public GLSprite(GL10 gl, float locX, float locY, float height, float width)
     {
    	 this.height = height;
    	 this.width = width;
         spriteVertices = new Vertices(gl, 4, 12, false, true);
         spriteVertices.setVertices(new float[] { 
												 locX,         locY,		  0, 1,
												 locX + width, locY, 		  1, 1,
												 locX + width, locY + height, 1, 0,
												 locX,         locY + height, 0, 0
			
											 }, 0, 16);
         spriteVertices.setIndices(new short[] { 0, 1, 2, 2, 3, 0 }, 0, 6);
     }
     
     
     public void movePosition(float x, float y)
     {
		 gl.glClearColor(1, 0, 0, 1);
		 gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
	 	 gl.glMatrixMode(GL10.GL_PROJECTION);
		 gl.glLoadIdentity();
		 gl.glOrthof(0, 320, 0, 480, 1, -1);
		 gl.glEnable(GL10.GL_TEXTURE_2D);

			
		 gl.glMatrixMode(GL10.GL_MODELVIEW);
    	 gl.glLoadIdentity();
		 gl.glTranslatef(x, y, 0);
     }    
}
