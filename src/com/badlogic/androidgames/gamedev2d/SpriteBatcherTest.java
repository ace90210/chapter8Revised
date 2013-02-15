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

public class SpriteBatcherTest extends GLGame {
	public Screen getStartScreen()
	{
		return new SpriteBatcherScreen(this);
	}
	
	class SpriteBatcherScreen extends Screen {
		Camera2D camera;
		final int NUM_TARGETS = 20;
		final float WORLD_WIDTH = 820.0f;
		final float WORLD_HEIGHT = 460.0f;
		GLGraphics glGraphics;
		GL10 gl;
		Cannon cannon;
		DynamicGameObject ball;
		List<GameObject> targets;
		SpatialHashGrid grid;
		GLText glText;
		
		Texture texture;
		
		TextureRegion cannonRegion;
		TextureRegion ballRegion;
		TextureRegion bobRegion;
		SpriteBatcher batcher;
		
		Vector2 touchPos = new Vector2();
		Vector2 gravity = new Vector2(0, -600);
		
		public SpriteBatcherScreen(Game game) {
			super(game);
			glGraphics = ((GLGame)game).getGLGraphics();
			gl = glGraphics.getGL();
			
			camera = new Camera2D(glGraphics, WORLD_WIDTH, WORLD_HEIGHT);
			
			cannon = new Cannon(0, 0, 128, 64.0f);
			ball = new DynamicGameObject(0, 0, 16.0f, 16.0f);
			targets = new ArrayList<GameObject>(NUM_TARGETS);
			grid = new SpatialHashGrid(WORLD_WIDTH, WORLD_HEIGHT, 64);
			for(int i = 0; i < NUM_TARGETS; i++) {
				GameObject target = new GameObject((float)Math.random() * WORLD_WIDTH,
												   (float)Math.random() * WORLD_HEIGHT,
												   50.0f, 50.0f);
				grid.insertStaticObject(target);
				targets.add(target);
			}
			
			batcher = new SpriteBatcher(gl, 100);	
			glText = new GLText( gl, ((GLGame)game).getAssets() );

		    glText.load( "ARIAL.TTF", 18, 2, 2 ); 
		}
		
		@Override
		public void update(float deltaTime) {
			List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
			game.getInput().getKeyEvents();
			
			int len = touchEvents.size();
			for(int i = 0; i < len; i++) {
				TouchEvent event = touchEvents.get(i);
				
				camera.touchToWorld(touchPos.set(event.x, event.y));
				
				cannon.angle = touchPos.sub(cannon.position).angle();
				
				if(event.type == TouchEvent.TOUCH_UP) {
					float radians = cannon.angle * Vector2.TO_RADIANS;
					float ballSpeed = touchPos.len() * 2;
					ball.position.set(cannon.position);
					
					ball.velocity.x = FloatMath.cos(radians) * ballSpeed;
					ball.velocity.y = FloatMath.sin(radians) * ballSpeed;
					
					ball.bounds.lowerLeft.set(ball.position.x - 8.0f, ball.position.y - 8.0f);
				}
			}
			

			
			ball.velocity.add(gravity.x * deltaTime, gravity.y * deltaTime);
			ball.position.add(ball.velocity.x * deltaTime, ball.velocity.y * deltaTime);
			ball.bounds.lowerLeft.add(ball.velocity.x * deltaTime, ball.velocity.y * deltaTime);
			
			List<GameObject> colliders = grid.getPotentialColliders(ball);
			len = colliders.size();
			
			for(int i = 0; i < len; i++) {
				GameObject collider = colliders.get(i);
				if(OverlapTester.overlapRectangle(ball.bounds, collider.bounds)) {
					grid.removeObject(collider);
					targets.remove(collider);
				}
			}
			
			if(ball.position.y > 0) {
				camera.position.set(ball.position);
				camera.zoom = 1 + ball.position.y / WORLD_HEIGHT;
			}
			else {
				camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
				camera.zoom = 1;
			}
		}
		
		@Override
		public void present(float deltaTime) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.setViewportAndMatrices();
			
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnable(GL10.GL_TEXTURE_2D);
			
			glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         // Begin Text Rendering (Set Color WHITE)
			glText.setScale(camera.zoom);
	        glText.drawC( "Line Test", camera.position.x, camera.position.y + (camera.frustumHeight / 2) * camera.zoom - (20 * camera.zoom) );              // Draw Test String
	        glText.end();                                   // End Text Rendering        
			
			batcher.beginBatch(texture);
			
			int len = targets.size();
			for(int i = 0; i < len; i++) {
				GameObject target = targets.get(i);
				batcher.drawSprite(target.position.x, target.position.y, 50.0f, 50.0f, bobRegion);
			}

			batcher.drawSprite(ball.position.x, ball.position.y, 16.0f, 16.0f, ballRegion);
			batcher.drawSprite(cannon.position.x, cannon.position.y, 128, 64.0f, cannon.angle, cannonRegion);
			batcher.endBatch();
		}

		@Override
		public void pause() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void resume() {
			texture = new Texture(((GLGame)game), "atlas.png");
			cannonRegion = new TextureRegion(texture, 0, 0, 64, 32);
			ballRegion = new TextureRegion(texture, 0, 32, 16, 16);
			bobRegion = new TextureRegion(texture, 32, 32, 32, 32);
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
	}
}
