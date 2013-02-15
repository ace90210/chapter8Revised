package com.badlogic.androidgames.gamedev2d;

import java.util.List;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.util.FloatMath;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.SpatialHashGrid;
import com.badlogic.androidgames.framework.GameObject;
import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.impl.GLGame;
import com.badlogic.androidgames.framework.impl.GLGraphics;
import com.badlogic.androidgames.framework.Input.TouchEvent;
import com.badlogic.androidgames.framework.gl.Texture;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.gl.SpriteBatcher;
import com.badlogic.androidgames.framework.gl.Camera2D;
import com.badlogic.androidgames.framework.math.OverlapTester;
import com.badlogic.androidgames.framework.math.Vector2;
import com.badlogic.androidgames.myitems.GLText;

public class PangBounceTest extends GLGame {
	public Screen getStartScreen()
	{
		return new PangBounceScreen(this);
	}
	
	class PangBounceScreen extends Screen {
		Camera2D camera;
		final float WORLD_WIDTH = 820.0f;
		final float WORLD_HEIGHT = 460.0f;
		GLGraphics glGraphics;
		GL10 gl;
		GLText glText;
		

		DynamicGameObject ball;
		Texture texture;
		
		TextureRegion ballRegion;

		SpriteBatcher batcher;
		
		Vector2 touchPos = new Vector2();
		Vector2 gravity = new Vector2(0, -600);

		public PangBounceScreen(Game game) {
				super(game);
				glGraphics = ((GLGame)game).getGLGraphics();
				gl = glGraphics.getGL();
				
				camera = new Camera2D(glGraphics, WORLD_WIDTH, WORLD_HEIGHT);
				
				ball = new DynamicGameObject(WORLD_WIDTH / 2, WORLD_HEIGHT - 32.0f, 64.0f, 64.0f);
				ball.velocity.x = 200;
				
				batcher = new SpriteBatcher(gl, 100);	
				glText = new GLText( gl, ((GLGame)game).getAssets() );

			    glText.load( "ARIAL.TTF", 18, 2, 2 ); 
		}
		
		@Override
		public void update(float deltaTime) {
			game.getInput().getTouchEvents();
			game.getInput().getKeyEvents();		
			
			if(ball.position.x < 32.0f) {
				ball.velocity.x = -ball.velocity.x;
				ball.position.x = 32.0f;
			}else if(ball.position.x > WORLD_WIDTH - 32.0f) {
				ball.velocity.x = -ball.velocity.x;
				ball.position.x = WORLD_WIDTH - 32.0f;
			}
			
			if(ball.position.y < 32) {
				ball.velocity.y = -ball.velocity.y;
			} else {			
				ball.velocity.add(gravity.x * deltaTime, gravity.y * deltaTime);
			}
			
			ball.position.add(ball.velocity.x * deltaTime, ball.velocity.y * deltaTime);
			ball.bounds.lowerLeft.add(ball.velocity.x * deltaTime, ball.velocity.y * deltaTime);				
		}
		
		
		@Override
		public void present(float deltaTime) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.setViewportAndMatrices();
			
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnable(GL10.GL_TEXTURE_2D);		  
			
			batcher.beginBatch(texture);
			batcher.drawSprite(ball.position.x, ball.position.y, 64.0f, 64.0f, ballRegion);
			batcher.endBatch();
			
			glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         // Begin Text Rendering (Set Color WHITE)
			glText.setScale(camera.zoom);
	        glText.drawC( "Bounce Test", camera.position.x, camera.position.y + (camera.frustumHeight / 2) * camera.zoom - (20 * camera.zoom) );              // Draw Test String
	        glText.end();                                   // End Text Rendering      
		}

		
		@Override
		public void resume() {
			texture = new Texture(((GLGame)game), "ballPang.png");
			ballRegion = new TextureRegion(texture, 0, 0, 256, 256);
		}

		@Override
		public void pause() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
	}
}
