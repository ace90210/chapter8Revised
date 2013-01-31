package com.badlogic.androidgames.gamedev2d;

import javax.microedition.khronos.opengles.GL10;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.GameObject;
import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.gl.Camera2D;
import com.badlogic.androidgames.framework.gl.SpriteBatcher;
import com.badlogic.androidgames.framework.gl.Texture;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.impl.GLGame;
import com.badlogic.androidgames.framework.impl.GLGraphics;
import com.badlogic.androidgames.myitems.GLText;

public class FontTest extends GLGame {
	public Screen getStartScreen()
	{
		return new FontScreen(this);
	}
	
	class FontScreen extends Screen {
		Camera2D camera;
		final float WORLD_WIDTH = 240.0f;
		final float WORLD_HEIGHT = 410.0f;
		GLGraphics glGraphics;
		GL10 gl;
		GLText glText;
		DynamicGameObject ball;
		Texture texture;
		TextureRegion ballRegion;
		
		
		SpriteBatcher batcher;
		
		public FontScreen(Game game) {
			super(game);
			ball = new DynamicGameObject(0, 0, 0.2f, 0.2f);
			glGraphics = ((GLGame)game).getGLGraphics();
			gl = glGraphics.getGL();			
			camera = new Camera2D(glGraphics, WORLD_WIDTH, WORLD_HEIGHT);
			batcher = new SpriteBatcher(gl, 100);	
			
			texture = new Texture(((GLGame)game), "atlas.png");
			
			ballRegion = new TextureRegion(texture, 0, 32, 16, 16);
			
			
			glText = new GLText( gl, ((GLGame)game).getAssets() );

		     glText.load( "ARIAL.TTF", 14, 2, 2 );  
		}

		@Override
		public void update(float deltaTime) {
		 
			
		}

		@Override
		public void present(float deltaTime) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.setViewportAndMatrices();
			
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
			gl.glEnable(GL10.GL_TEXTURE_2D);		
			
		    gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );        
	                   
	
  	      // TEST: render some strings with the font
	        glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         // Begin Text Rendering (Set Color WHITE)
	        glText.draw( "Test String :)", 0, 0 );          // Draw Test String
	        glText.draw( "Line 1", 50, 50 );                // Draw Test String
	        glText.draw( "Line 2", 100, 100 );              // Draw Test String
	        glText.end();                                   // End Text Rendering
	
	        //glText.drawTexture( (int)WORLD_WIDTH, (int)WORLD_HEIGHT ); 
	        
	        glText.begin( 0.0f, 0.0f, 1.0f, 1.0f );         // Begin Text Rendering (Set Color BLUE)
	        glText.draw( "More Lines...", 50, 150 );        // Draw Test String
	        glText.draw( "The End.", 50, 150 + glText.getCharHeight() );  // Draw Test String
	        glText.end();   
	        
	        batcher.beginBatch(texture);			
			batcher.drawSprite(ball.position.x, ball.position.y, 50f, 50.0f, ballRegion);
			batcher.endBatch();
			
		}

		@Override
		public void pause() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void resume() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
	}
}
