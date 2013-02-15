package com.badlogic.androidgames.gamedev2d;

import java.util.List;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.util.FloatMath;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.annotation.TargetApi;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.impl.KeyboardHandler;
import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.Pool;
import com.badlogic.androidgames.framework.SpatialHashGrid;
import com.badlogic.androidgames.framework.GameObject;
import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.impl.GLGame;
import com.badlogic.androidgames.framework.impl.GLGraphics;
import com.badlogic.androidgames.framework.gl.Texture;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.gl.SpriteBatcher;
import com.badlogic.androidgames.framework.gl.Camera2D;
import com.badlogic.androidgames.myitems.PlayerV2;
import com.badlogic.androidgames.framework.math.OverlapTester;
import com.badlogic.androidgames.framework.math.Vector2;
import com.badlogic.androidgames.myitems.GLText;
import com.badlogic.androidgames.myitems.PlayerV2.PlayerState;
import com.badlogic.androidgames.myitems.TouchControls;
import com.badlogic.androidgames.myitems.Spear;
import com.badlogic.androidgames.myitems.TouchElement;
import com.badlogic.androidgames.framework.impl.MultiTouchHandler;


public class PangV2 extends GLGame {
	public Screen getStartScreen()
	{
		return new PangV2Screen(this);
	}
	
	class PangV2Screen extends Screen implements OnKeyListener {
		Camera2D camera;
		MultiTouchHandler mTouch;
		final float WORLD_WIDTH = 820.0f;
		final float WORLD_HEIGHT = 460.0f;

		final float SPEAR_REGION_MAX = 240.0f;
		final float BALL_RADIUS = 32.0f;
		final float SMALLEST_BALL = 16.0f;
		final float BUMP = 0.7f;
		final float BOUNCE_HEIGHT = 500.0f;
		final int BALL_LIMIT = 20;
		final int SPRITE_LIMIT = 100;
		

		float[] x = new float[10];
		float[] y = new float[10];
		boolean[] touched = new boolean[10];
		int[] id = new int[10];
		
		
		float SPEAR_SPEED = 150.0f;		
		
		boolean[] pressedKeys = new boolean[128];
	    List<KeyEvent> keyEventsBuffer = new ArrayList<KeyEvent>(); 
	    
		GLGraphics glGraphics;
		GL10 gl;
		GLText glText;

		PlayerV2 player;
		Spear spear;		
		
		boolean paused = false;
		
		PlayerState playerState;
		PlayerV2 savedPlayerState;
		TextureRegion currKeyFrame;		
		Texture background;
		TextureRegion backgroundRegion;
		List<DynamicGameObject> balls;
		Texture textureSet;
		
		TextureRegion ballRegion, spearRegion;
		Vector2 gravity = new Vector2(0, -400);
		
		TouchControls controls;
		SpriteBatcher batcher;
		
		public PangV2Screen(Game game) {
			super(game);
			glGraphics = ((GLGame)game).getGLGraphics();
			glGraphics.getView().setOnKeyListener(this);
			glGraphics.getView().setFocusable(true);
			glGraphics.getView().requestFocus();
			gl = glGraphics.getGL();
			
			mTouch = new MultiTouchHandler(glGraphics.getView(), 1, 1);
			
			camera = new Camera2D(glGraphics, WORLD_WIDTH, WORLD_HEIGHT);
			playerState = PlayerState.STANDING;
			balls = new ArrayList<DynamicGameObject>();
			
			balls.add(new DynamicGameObject(WORLD_WIDTH / 2, WORLD_HEIGHT - 32.0f, BALL_RADIUS * 2));
			
			for(DynamicGameObject ball: balls) {
				ball.velocity.x = 150;
			}
			
			batcher = new SpriteBatcher(gl, SPRITE_LIMIT);	
			glText = new GLText( gl, ((GLGame)game).getAssets() );
		}
		
		@Override
		public void resume() {
			 glText.load( "ARIAL.TTF", 18, 2, 2 ); 
				textureSet = new Texture((GLGame)game, "BasicPangSet2.png");
				if(savedPlayerState == null) {
					player = new PlayerV2(camera.position.x, 50, 64, 70, 3, textureSet);
					player.updateBounds(camera.position.x, 64, player.bounds.width, player.bounds.height);
				} else {
					player = savedPlayerState;
				}
							
				ballRegion = new TextureRegion(textureSet, 0, 0, 128, 128);
				
				spear = new Spear(camera.position.x, 0, 20, 64, 200, textureSet);	
				
				background = new Texture(((GLGame)game), "background.png");
				backgroundRegion = new TextureRegion(background, 0, 0, 512, 256);
				controls = new TouchControls(textureSet, WORLD_WIDTH, WORLD_HEIGHT);
		}
		
		@Override
		public void pause() {
			paused = true;
			savedPlayerState = player;
			if(isFinishing())
			{
				background.dispose();
				textureSet.dispose();
			}
		}

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			boolean validKey = true;
			
			if(event.getAction() == KeyEvent.ACTION_DOWN) {
				if(keyCode > 0 && keyCode < 127) {
                    pressedKeys[keyCode] = true;
				}
				switch(event.getKeyCode())
				{
					case KeyEvent.KEYCODE_DPAD_CENTER: 	{
															//X Pressed													
														}
													break;
						case KeyEvent.KEYCODE_BACK:  {
														//Circle Pressed														
													 }
													break;
					case KeyEvent.KEYCODE_BUTTON_X:	{
															//square pressed															
													}
													break;
					case KeyEvent.KEYCODE_BUTTON_Y: {
															//Triangle pressed									
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
															{
																
															}
													break;
					case KeyEvent.KEYCODE_BUTTON_START: 
														
													break;
					case KeyEvent.KEYCODE_BUTTON_L1: 
													
													break;
					case KeyEvent.KEYCODE_BUTTON_R1: 
													
													break;
				}
			}else if(event.getAction() == KeyEvent.ACTION_UP) {
				if(keyCode > 0 && keyCode < 127) {
                    pressedKeys[keyCode] = false;
				}
			}
			//catch circle button (prevent back key + alt quiting)
			if(!event.isAltPressed()) {
				validKey = false;
			}
			return validKey;
		}
		
		public void startButtonAction() {
	    	//if end of game reset else toggle pause
			player.velocity.set(0, 0);
			if(balls.size() == 0 || !player.alive()) {
				paused = false; 
				//spawn new ball
				synchronized(balls) {
					balls.clear();
					if(balls.size() < BALL_LIMIT && !paused) {
						balls.add(new DynamicGameObject(WORLD_WIDTH / 2, WORLD_HEIGHT - 32.0f, BALL_RADIUS * 2));
						balls.get(balls.size() - 1).velocity.x = 150;																		
					}
					player.reset(WORLD_WIDTH / 2, 50, 3);																	
				}
			} else {															
				if(paused) {
					paused = false;
				} else {
					paused = true;
				}
			}
	    }

	    public boolean keyPressed(int keyCode) {
	        if (keyCode < 0 || keyCode > 127)
	            return false;
	        return pressedKeys[keyCode];
	    }	   
	    
		@Override
		public void update(float deltaTime) {
			game.getInput().getKeyEvents();		
			controls.resetActiveButtons();
			for(int i = 0; i < 10; i++) {
				int x = mTouch.getTouchX(i);
				int y = glGraphics.getHeight() - mTouch.getTouchY(i);
				for(int j = 0; j < controls.touchElements.size(); j++) {
					TouchElement element = controls.touchElements.get(j);
					if(x > element.bounds.lowerLeft.x && x < element.bounds.lowerLeft.x + element.bounds.width &&
					   y > element.bounds.lowerLeft.y && y < element.bounds.lowerLeft.y + element.bounds.height &&
					   element.ready()
							  ) {
						controls.activeButtons[j] = true;
						element.use();
					}
				}	
			}
			if(controls.activeButtons[6] || keyPressed(KeyEvent.KEYCODE_BUTTON_START)) {
				pressedKeys[KeyEvent.KEYCODE_BUTTON_START] = false;
				startButtonAction();			
			} 
			
			if(!paused) {
					
				
				//move player
				if((keyPressed(KeyEvent.KEYCODE_DPAD_RIGHT) || controls.activeButtons[3]) && player.position.x + player.bounds.width / 2 < WORLD_WIDTH) 	{
					player.velocity.x = 4;
					playerState = PlayerState.WALKING_RIGHT;
				} else if((keyPressed(KeyEvent.KEYCODE_DPAD_LEFT) || controls.activeButtons[2]) && player.position.x - player.bounds.width / 2 > 0) 	{
					player.velocity.x = -4;
					playerState = PlayerState.WALKING_LEFT;
				} else { 
					player.velocity.x = 0;
					if(spear.alive && spear.position.y < 80) {
						playerState = PlayerState.SHOOTING;
					} else {
						playerState = PlayerState.STANDING;
					}					
				}
				
								
				
				if((keyPressed(KeyEvent.KEYCODE_DPAD_CENTER) || controls.activeButtons[4]) && !spear.alive) {
					//reset button to off
					pressedKeys[KeyEvent.KEYCODE_DPAD_CENTER] = false;
					//spawn new spear
					spear.shoot(player.position.x, player.bounds.lowerLeft.y);	
					playerState = PlayerState.SHOOTING;
				} 		
				
				//update all balls
				synchronized(balls) {
					for(int i = 0; i < balls.size(); i++) {
						DynamicGameObject ball = balls.get(i);
						
						//check player collision
						if(OverlapTester.overlapCircleRectangle(ball.boundingCircle, player.bounds)) {
							player.hit();
						}
						
						//check spear collisions
						if(spear.alive && OverlapTester.overlapCircleRectangle(ball.boundingCircle, spear.bounds)) {							
							spear.alive = false;
							
							//if ball smaller than smallest ball limit destroy
							if(ball.boundingCircle.radius < SMALLEST_BALL) {
								balls.remove(ball);
							} else {
								ball.boundingCircle.radius /= 2; 
								ball.velocity.y = BUMP * (WORLD_HEIGHT - ball.position.y);
								balls.add(new DynamicGameObject(ball.position.x, ball.position.y, ball.boundingCircle.radius));
								balls.get(balls.size() - 1).velocity.x = -ball.velocity.x;
								balls.get(balls.size() - 1).velocity.y = BUMP * (WORLD_HEIGHT - ball.position.y);
							}
						}
							
						//check ball in x screen bounds
						if(ball.position.x < ball.boundingCircle.radius) {
							ball.velocity.x = -ball.velocity.x;
							ball.position.x = ball.boundingCircle.radius;
						}else if(ball.position.x > WORLD_WIDTH - ball.boundingCircle.radius) {
							ball.velocity.x = -ball.velocity.x;
							ball.position.x = WORLD_WIDTH - ball.boundingCircle.radius;
						}
						
						//bounce ball when hit floor
						if(ball.position.y < ball.boundingCircle.radius) {					
							ball.velocity.y = (BOUNCE_HEIGHT) - (ball.boundingCircle.radius - ball.position.y);
							ball.position.y = ball.boundingCircle.radius;
						} else {			
							ball.velocity.add(gravity.x * deltaTime, gravity.y * deltaTime);
						}
						
						//apply changes to ball
						ball.position.add(ball.velocity.x * deltaTime, ball.velocity.y * deltaTime);
						ball.boundingCircle.center.set(ball.position.x, ball.position.y);						
					}	
				}				
			}
			
			//update spear
			if(spear.alive) {
				spear.update(deltaTime);		
				if(spear.position.y > camera.position.y) {
					spear.alive = false;
				}
			}
			player.update(deltaTime, playerState);
		}

		@Override
		public void present(float deltaTime) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.setViewportAndMatrices(0.0f, 0.0f, 0.6f);
			
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnable(GL10.GL_TEXTURE_2D);		  
			
			
			batcher.beginBatch(background);
			batcher.drawSprite(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, WORLD_WIDTH, WORLD_HEIGHT, backgroundRegion);
			batcher.endBatch();
			
			batcher.beginBatch(textureSet);
			if(player.hurt() && player.getImmuneTime() * 10 % 2 > 1) {
				gl.glColor4f(1.0f, 0.2f, 0.2f, 1.0f);
			}
			batcher.drawSprite(player.position.x, player.position.y, 90, 110, player.getKeyFrame());	
			//if(spearAlive) {
			//	batcher.drawSprite(spear.position.x, spear.position.y, 20, spearHeight, spearRegion);
			//}			
			batcher.endBatch();
			if(player.hurt()) {
				gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			}
			
			
			batcher.beginBatch(textureSet);
			for(int i = 0; i < balls.size(); i++) {
				DynamicGameObject ball = balls.get(i);
				batcher.drawSprite(ball.position.x, ball.position.y, ball.boundingCircle.radius * 2, ball.boundingCircle.radius * 2, ballRegion);
			}
			for(TouchElement element :controls.touchElements) {
				batcher.drawSprite(element.position.x, element.position.y, element.width, element.height, element.region);
			}
			if(spear.alive) {
				batcher.drawSprite(spear.position.x, spear.position.y, spear.bounds.width, spear.bounds.height, spear.region);
			}
			
			batcher.endBatch();
			
			if(!player.alive()) {
				paused = true;
				player.reset();
				
				glText.begin( 1.0f, 0.0f, 0.0f, 1.0f );         
				glText.setScale(camera.zoom * 3);
		        glText.drawC( "You Died ha!", camera.position.x, camera.position.y + 10);              
		        glText.end(); 
		        
		        glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         
				glText.setScale(camera.zoom);
				glText.drawC( "Press start to try again", camera.position.x, camera.position.y - 30);              
				glText.end(); 
			} else if(balls.size() == 0) {
				paused = true;
				player.reset();
				
				glText.begin( 0.0f, 1.0f, 0.0f, 1.0f );         
				glText.setScale(camera.zoom * 3);
		        glText.drawC( "You WON!!!", camera.position.x, camera.position.y + 10);             
		        glText.end(); 
		        
		        glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         
				glText.setScale(camera.zoom);
				glText.drawC( "Press start to try again", camera.position.x, camera.position.y - 30);             
				glText.end();  
			} else if(paused) { 
				glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );       
				glText.setScale(camera.zoom * 3);
		        glText.drawC( "game paused", camera.position.x, camera.position.y );              
		        glText.end(); 
			} 
			glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         
			glText.setScale(camera.zoom);
	        glText.drawC( "Immune: " +  (int)player.getImmuneTime() + "   Lives left: " +  player.life , camera.position.x, camera.position.y + (camera.frustumHeight / 2) * camera.zoom - (20 * camera.zoom) ); 
	        glText.end();                                   
		}

		@Override
		public void dispose() {
			
		}
		
		
	}
}
