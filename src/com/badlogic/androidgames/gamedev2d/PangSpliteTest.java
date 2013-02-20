package com.badlogic.androidgames.gamedev2d;

import java.util.List;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.util.FloatMath;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

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

public class PangSpliteTest extends GLGame {
	public Screen getStartScreen()
	{
		return new PangSpliteScreen(this);
	}
	
	class PangSpliteScreen extends Screen implements OnKeyListener {
		Camera2D camera;
		final float WORLD_WIDTH = 820.0f;
		final int BALL_LIMIT = 200;
		final int SPRITE_LIMIT = 4000;
		final float BUMP = 400;
		final float WORLD_HEIGHT = 460.0f;
		final float BALL_RADIUS = 32.0f;
		
		GLGraphics glGraphics;
		GL10 gl;
		GLText glText;
		

		List<DynamicGameObject> balls;
		Texture texture;
		
		TextureRegion ballRegion;

		SpriteBatcher batcher;
		
		Vector2 touchPos = new Vector2();
		Vector2 gravity = new Vector2(0, -600);

		public PangSpliteScreen(Game game) {
				super(game);
				glGraphics = ((GLGame)game).getGLGraphics();
				glGraphics.getView().setOnKeyListener(this);
				glGraphics.getView().setFocusable(true);
				glGraphics.getView().requestFocus();
				gl = glGraphics.getGL();
				
				camera = new Camera2D(glGraphics, WORLD_WIDTH, WORLD_HEIGHT);
				balls = new ArrayList<DynamicGameObject>();
				
				balls.add(new DynamicGameObject(WORLD_WIDTH / 2, WORLD_HEIGHT - 32.0f, BALL_RADIUS * 2, BALL_RADIUS * 2));
				
				for(DynamicGameObject ball: balls) {
					ball.velocity.x = 200;
				}
				
				batcher = new SpriteBatcher(gl, SPRITE_LIMIT);	
				glText = new GLText( gl, ((GLGame)game).getAssets() );

			    glText.load( "ARIAL.TTF", 18, 2, 2 ); 
		}
		
		@Override
		public void update(float deltaTime) {
			game.getInput().getTouchEvents();
			game.getInput().getKeyEvents();		
			
			synchronized(balls){
				for(DynamicGameObject ball: balls) {
					if(ball.position.x < ball.bounds.width / 2) {
						ball.velocity.x = -ball.velocity.x;
						ball.position.x = ball.bounds.width / 2;
					}else if(ball.position.x > WORLD_WIDTH - ball.bounds.width / 2) {
						ball.velocity.x = -ball.velocity.x;
						ball.position.x = WORLD_WIDTH - ball.bounds.width / 2;
					}
					
					if(ball.position.y < ball.bounds.height / 2) {					
						ball.velocity.y = (-1 * ball.velocity.y) - (ball.bounds.width / 2 - ball.position.y);
						ball.position.y = ball.bounds.width / 2;
					} else {			
						ball.velocity.add(gravity.x * deltaTime, gravity.y * deltaTime);
					}
					
					ball.position.add(ball.velocity.x * deltaTime, ball.velocity.y * deltaTime);
					ball.bounds.lowerLeft.add(ball.velocity.x * deltaTime, ball.velocity.y * deltaTime);
				}		
			}
		}
		
		
		@Override
		public void present(float deltaTime) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.setViewportAndMatrices();
			
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnable(GL10.GL_TEXTURE_2D);		  
			
			batcher.beginBatch(texture);
			
			int len = balls.size();
			for(int i = 0; i < len; i++) {
				DynamicGameObject ball = balls.get(i);
				batcher.drawSprite(ball.position.x, ball.position.y, ball.bounds.width, ball.bounds.height, ballRegion);
			}
			batcher.endBatch();
			
			glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         // Begin Text Rendering (Set Color WHITE)
			glText.setScale(camera.zoom);
	        glText.drawC( "Splite Test", camera.position.x, camera.position.y + (camera.frustumHeight / 2) * camera.zoom - (20 * camera.zoom) );              // Draw Test String
	        glText.drawC( "size: " + len, camera.position.x, camera.position.y + (camera.frustumHeight / 2) * camera.zoom - (20 * camera.zoom) - 25);              // Draw Test String
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
		
		public boolean onKey(View v, int keyCode, KeyEvent event) 
		{
				boolean validKey = true;
				if(event.getAction() == KeyEvent.ACTION_DOWN) {
					
					switch(event.getKeyCode())
					{
						case KeyEvent.KEYCODE_DPAD_CENTER: 	{
																synchronized(balls) {
																	if(balls.size() < BALL_LIMIT) {
																		balls.add(new DynamicGameObject(WORLD_WIDTH / 2, WORLD_HEIGHT - 32.0f, BALL_RADIUS * 2, BALL_RADIUS * 2));
																		balls.get(balls.size() - 1).velocity.x = 150;	
																	}
																}
															}
														break;
							case KeyEvent.KEYCODE_BACK:  {
															synchronized(balls) {
																if(balls.size() * 2 < SPRITE_LIMIT) {
																	int len = balls.size();
																	for(int i = 0; i < len; i++) {
																		DynamicGameObject ball = balls.get(i);
																		ball.bounds.height /= 2; 
																		ball.bounds.width /= 2;
																		ball.velocity.y = BUMP;
																		balls.add(new DynamicGameObject(ball.position.x, ball.position.y, ball.bounds.width, ball.bounds.height));
																		balls.get(balls.size() - 1).velocity.x = -ball.velocity.x;
																		balls.get(balls.size() - 1).velocity.y = BUMP;

																	}
																}
															}
														 }
														break;
						case KeyEvent.KEYCODE_BUTTON_X:	{
															
														}
														break;
						case KeyEvent.KEYCODE_BUTTON_Y: {
							
														}													
														break;
						case KeyEvent.KEYCODE_DPAD_UP:  {
															
														}
														break;
						case KeyEvent.KEYCODE_DPAD_DOWN: {
															
														 }
														
														break;
						case KeyEvent.KEYCODE_DPAD_RIGHT: 	{
																
															}
															
														break;
						case KeyEvent.KEYCODE_DPAD_LEFT:  	{
																
															}
														break;
						case KeyEvent.KEYCODE_BUTTON_SELECT: 
														//builder.append("\"Select\" key was pressed");
														break;
						case KeyEvent.KEYCODE_BUTTON_START: 
														//builder.append("\"Start\" key was pressed");
														break;
						case KeyEvent.KEYCODE_BUTTON_L1: 
														//builder.append("\"L1\" key was pressed");
														break;
						case KeyEvent.KEYCODE_BUTTON_R1: 
														//builder.append("\"R1\" key was pressed");
														break;
					}
				}
				
				if(!event.isAltPressed()) {
					validKey = false;
				}
				return validKey;
			}
	}
}
