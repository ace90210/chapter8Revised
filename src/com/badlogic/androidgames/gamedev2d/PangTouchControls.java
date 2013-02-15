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
import com.badlogic.androidgames.framework.Pool.PoolObjectFactory;
import com.badlogic.androidgames.framework.gl.Texture;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.gl.SpriteBatcher;
import com.badlogic.androidgames.framework.gl.Camera2D;
import com.badlogic.androidgames.myitems.Player;
import com.badlogic.androidgames.framework.math.OverlapTester;
import com.badlogic.androidgames.framework.math.Rectangle;
import com.badlogic.androidgames.framework.math.Vector2;
import com.badlogic.androidgames.myitems.GLText;
import com.badlogic.androidgames.myitems.AnimatedObject.AnimationState;
import com.badlogic.androidgames.myitems.TouchControls;
import com.badlogic.androidgames.myitems.TouchElement;
import com.badlogic.androidgames.framework.impl.MultiTouchHandler;

@TargetApi(5)
public class PangTouchControls extends GLGame {
	public Screen getStartScreen()
	{
		return new PangTouchControlsScreen(this);
	}
	
	class PangTouchControlsScreen extends Screen implements OnKeyListener {
		Camera2D camera;
		MultiTouchHandler mTouch;
		final float WORLD_WIDTH = 820.0f;
		final float WORLD_HEIGHT = 460.0f;
		final float PLAYER_COL_BUFFERW = 40.0f;
		final float PLAYER_COL_BUFFERH = 70.0f;	
		final float SPEAR_REGION_MAX = 240.0f;
		final float BALL_RADIUS = 32.0f;
		final float SMALLEST_BALL = 16.0f;
		final int BALL_LIMIT = 20;
		final int SPRITE_LIMIT = 100;
		final float BUMP = 0.7f;
		final float BOUNCE_HEIGHT = 500.0f;
		final int TOUCH_BUFFER = 20;

		float[] x = new float[10];
		float[] y = new float[10];
		boolean[] touched = new boolean[10];
		int[] id = new int[10];
		
		
		float SPEAR_SPEED = 150.0f;
		float spearHeight;
		float direction = 0;		//players direction for rendering sprite
		
		
		boolean[] pressedKeys = new boolean[128];
	    List<KeyEvent> keyEventsBuffer = new ArrayList<KeyEvent>(); 
	    
		GLGraphics glGraphics;
		GL10 gl;
		GLText glText;
		boolean alive = true;

		Player player;
		DynamicGameObject spear;		
		
		boolean spearAlive = false;
		boolean paused = false;
		
		AnimationState playerState;
		Player savedPlayerState;
		TextureRegion currKeyFrame;
		TouchControls controls;
		Texture texture;
		Texture background;
		TextureRegion backgroundRegion;
		List<DynamicGameObject> balls;
		Texture textureSet;
		
		TextureRegion ballRegion, spearRegion;
		Vector2 gravity = new Vector2(0, -400);
		
		SpriteBatcher batcher;

		public PangTouchControlsScreen(Game game) {
				super(game);
				glGraphics = ((GLGame)game).getGLGraphics();
				glGraphics.getView().setOnKeyListener(this);
				glGraphics.getView().setFocusable(true);
				glGraphics.getView().requestFocus();
				gl = glGraphics.getGL();
				
				mTouch = new MultiTouchHandler(glGraphics.getView(), 1, 1);
				
				camera = new Camera2D(glGraphics, WORLD_WIDTH, WORLD_HEIGHT);
				playerState = AnimationState.STANDING;
				balls = new ArrayList<DynamicGameObject>();
				
				balls.add(new DynamicGameObject(WORLD_WIDTH / 2, WORLD_HEIGHT - 32.0f, BALL_RADIUS * 2));
				
				for(DynamicGameObject ball: balls) {
					ball.velocity.x = 150;
				}
				 
				
				
				spear = new DynamicGameObject(camera.position.x, 64, 20, 64);	
				spear.updateBounds(spear.position.x, spear.position.y, 10, spearHeight);
				
				spearHeight = 64;
				batcher = new SpriteBatcher(gl, SPRITE_LIMIT);	
				glText = new GLText( gl, ((GLGame)game).getAssets() );
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
			if(controls.activeButtons[6]) {
				startButtonAction();			
			} 
			
			if(!paused) {
				if(controls.activeButtons[4]) {
					//spawn new spear
					if(!spearAlive  && !paused) {
						spear.position.x = player.position.x;
						spear.position.y = 0;																	
						spearHeight = 64;
						spear.updateBounds(spear.position.x, spear.position.y, 10, spearHeight );
						spearRegion.setRegion(64);
						spearAlive = true;
						playerState = AnimationState.SHOOTING;
					}
				}
				
				//move player
				if((isKeyPressed(KeyEvent.KEYCODE_DPAD_RIGHT) || controls.activeButtons[3]) && player.position.x + player.bounds.width / 2 < WORLD_WIDTH) 	{
					player.velocity.x = 250;
					playerState = AnimationState.WALKING_RIGHT;
				} else if((isKeyPressed(KeyEvent.KEYCODE_DPAD_LEFT) || controls.activeButtons[2]) && player.position.x - player.bounds.width / 2 > 0) 	{
					player.velocity.x = -250;
					playerState = AnimationState.WALKING_LEFT;
				} else {
					player.velocity.x = 0;
					playerState = AnimationState.STANDING;
				}
				player.position.add(player.velocity.x * deltaTime, player.velocity.y * deltaTime);	
				player.bounds.lowerLeft.add(player.velocity.x * deltaTime, player.velocity.y * deltaTime);
				currKeyFrame = player.getKeyFrame(deltaTime, playerState);
				
				//if spear in use update spear
				if(spearAlive) {
					if(spear.position.y + spearHeight / 2 < WORLD_HEIGHT) {
						if(spearRegion.height < SPEAR_REGION_MAX) {
							spearRegion.setRegion(spearRegion.height + SPEAR_SPEED * deltaTime * 2);
						}				
						spearHeight += SPEAR_SPEED * deltaTime * 2;
						spear.position.y += SPEAR_SPEED * deltaTime;
						spear.updateBounds(spear.position.x, spear.position.y, 10, spearHeight );
						
					}
					else {
						spearRegion.setRegion(64);
						spearAlive = false;
					}
				}
				
				//update all balls
				synchronized(balls) {
					for(int i = 0; i < balls.size(); i++) {
						DynamicGameObject ball = balls.get(i);
						
						//check player collision
						if(OverlapTester.overlapCircleRectangle(ball.boundingCircle, player.bounds)) {
							alive= false;
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
						
						//check collisions with spear
						if(OverlapTester.overlapCircleRectangle(ball.boundingCircle, spear.bounds) && spearAlive) {
							spearHeight = 64;
							spear.updateBounds(spear.position.x, spear.position.y, 10, spearHeight );
							spearRegion.setRegion(64);
							spearAlive = false;
							
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
					}	
				}
			}
		}
		
		
		@Override
		public void present(float deltaTime) {
			
			if(playerState == AnimationState.WALKING_LEFT) {
				 direction = -(player.bounds.width + PLAYER_COL_BUFFERW);
			} else {
				 direction = player.bounds.width + PLAYER_COL_BUFFERW;
			}
			 
			 
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.setViewportAndMatrices(0.0f, 0.0f, 0.6f);
			
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnable(GL10.GL_TEXTURE_2D);		  
			
			
			batcher.beginBatch(background);
			batcher.drawSprite(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, WORLD_WIDTH, WORLD_HEIGHT, backgroundRegion);
			batcher.endBatch();
			
			batcher.beginBatch(texture);
			batcher.drawSprite(player.position.x, player.position.y, direction, player.bounds.height + PLAYER_COL_BUFFERH, currKeyFrame);	
			if(spearAlive) {
				batcher.drawSprite(spear.position.x, spear.position.y, 20, spearHeight, spearRegion);
			}
			batcher.endBatch();
						
			batcher.beginBatch(textureSet);
			for(int i = 0; i < balls.size(); i++) {
				DynamicGameObject ball = balls.get(i);
				batcher.drawSprite(ball.position.x, ball.position.y, ball.boundingCircle.radius * 2, ball.boundingCircle.radius * 2, ballRegion);
			}
			for(TouchElement element :controls.touchElements) {
				batcher.drawSprite(element.position.x, element.position.y, element.width, element.height, element.region);
			}
			batcher.endBatch();
			
			if(!alive) {
				paused = true;
				glText.begin( 1.0f, 0.0f, 0.0f, 1.0f );         // Begin Text Rendering (Set Color WHITE)
				glText.setScale(camera.zoom * 3);
		        glText.drawC( "You Died ha!", camera.position.x, camera.position.y + 10);              // Draw Test String
		        glText.end(); 
		        
		        glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         // Begin Text Rendering (Set Color WHITE)
				glText.setScale(camera.zoom);
				glText.drawC( "Press start to try again", camera.position.x, camera.position.y - 30);              // Draw Test String
				glText.end(); 
			} else if(balls.size() == 0) {
				paused = true;
				glText.begin( 0.0f, 1.0f, 0.0f, 1.0f );         // Begin Text Rendering (Set Color WHITE)
				glText.setScale(camera.zoom * 3);
		        glText.drawC( "You WON!!!", camera.position.x, camera.position.y + 10);              // Draw Test String
		        glText.end(); 
		        
		        glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         // Begin Text Rendering (Set Color WHITE)
				glText.setScale(camera.zoom);
				glText.drawC( "Press start to try again", camera.position.x, camera.position.y - 30);              // Draw Test String
				glText.end();  
			} else if(paused) { 
				glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         // Begin Text Rendering (Set Color WHITE)
				glText.setScale(camera.zoom * 3);
		        glText.drawC( "game paused", camera.position.x, camera.position.y );              // Draw Test String
		        glText.end(); 
			} 
			glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         // Begin Text Rendering (Set Color WHITE)
			glText.setScale(camera.zoom);
	        glText.drawC( "X: " +  mTouch.getTouchX(0) + "   Y: " +  mTouch.getTouchY(0) , camera.position.x, camera.position.y + (camera.frustumHeight / 2) * camera.zoom - (20 * camera.zoom) );              // Draw Test String
	        glText.end();                                   // End Text Rendering      
		}

		
		@Override
		public void resume() {
		    glText.load( "ARIAL.TTF", 18, 2, 2 ); 
			texture = new Texture((GLGame)game, "walkingSad.png");
			if(savedPlayerState == null) {
				player = new Player(camera.position.x, 64, 64, 128, texture);
				player.updateBounds(camera.position.x, 64, player.bounds.width - PLAYER_COL_BUFFERW, player.bounds.height - PLAYER_COL_BUFFERH);
			} else {
				player = savedPlayerState;
			}
			spearRegion = new TextureRegion(texture, 484, 12, 20, 64);
			
			background = new Texture(((GLGame)game), "background.png");
			backgroundRegion = new TextureRegion(background, 0, 0, 512, 256);
			textureSet = new Texture(((GLGame)game), "BasicPangSet.png");
			ballRegion = new TextureRegion(textureSet, 0, 0, 128, 128);
			controls = new TouchControls(textureSet, WORLD_WIDTH, WORLD_HEIGHT);
		}

		@Override
		public void pause() {
			paused = true;
			savedPlayerState = player;
			if(isFinishing())
			{
				texture.dispose();
				textureSet.dispose();
			}
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
		}
		
	    public boolean isKeyPressed(int keyCode) {
	        if (keyCode < 0 || keyCode > 127)
	            return false;
	        return pressedKeys[keyCode];
	    }
	    
	    public void startButtonAction() {
	    	//if end of game reset else toggle pause
			if(balls.size() == 0 || !alive) {
				paused = false; 
				//spawn new ball
				synchronized(balls) {
					balls.clear();
					if(balls.size() < BALL_LIMIT && !paused) {
						balls.add(new DynamicGameObject(WORLD_WIDTH / 2, WORLD_HEIGHT - 32.0f, BALL_RADIUS * 2));
						balls.get(balls.size() - 1).velocity.x = 150;																		
					}
					alive = true;																	
				}
			} else {															
				if(paused) {
					paused = false;
				} else {
					paused = true;
				}
			}
	    }
	    
		
		public boolean onKey(View v, int keyCode, KeyEvent event) 
		{
				boolean validKey = true;
				
				if(event.getAction() == KeyEvent.ACTION_DOWN) {
					if(keyCode > 0 && keyCode < 127) {
	                    pressedKeys[keyCode] = true;
					}
					switch(event.getKeyCode())
					{
						case KeyEvent.KEYCODE_DPAD_CENTER: 	{
																//spawn new spear
																if(!spearAlive  && !paused) {
																	spear.position.x = player.position.x;
																	spear.position.y = 0;																	
																	spearHeight = 64;
																	spear.updateBounds(spear.position.x, spear.position.y, 10, spearHeight );
																	spearRegion.setRegion(64);
																	spearAlive = true;
																	playerState = AnimationState.SHOOTING;
																}
															}
														break;
							case KeyEvent.KEYCODE_BACK:  {
															//splite all balls
															synchronized(balls) {
																if(balls.size() * 2 < SPRITE_LIMIT && !paused) {
																	int len = balls.size();
																	for(int i = 0; i < len; i++) {
																		DynamicGameObject ball = balls.get(i);
																		ball.boundingCircle.radius /= 2; 
																		ball.velocity.y = BUMP * (WORLD_HEIGHT - ball.position.y);
																		balls.add(new DynamicGameObject(ball.position.x, ball.position.y, ball.boundingCircle.radius));
																		balls.get(balls.size() - 1).velocity.x = -ball.velocity.x;
																		balls.get(balls.size() - 1).velocity.y = BUMP * (WORLD_HEIGHT - ball.position.y);
							
																	}
																}
															}
														 }
														break;
						case KeyEvent.KEYCODE_BUTTON_X:	{
																//spawn new ball
																synchronized(balls) {
																	if(balls.size() < BALL_LIMIT && !paused) {
																		balls.add(new DynamicGameObject(WORLD_WIDTH / 2, WORLD_HEIGHT - 32.0f, BALL_RADIUS * 2));
																		balls.get(balls.size() - 1).velocity.x = 150;																		
																	}
																	alive = true;																	
																}
														}
														break;
						case KeyEvent.KEYCODE_BUTTON_Y: {
																if(balls.size() > 1 && !paused) {
																	alive = true;
																	DynamicGameObject ball = balls.get(0);
																	balls.clear();
																	balls.add(ball);
																}														
														}													
														break;
						case KeyEvent.KEYCODE_DPAD_UP:  {
															if(!paused) {
																SPEAR_SPEED += 10.0f;
															}
														}
														break;
						case KeyEvent.KEYCODE_DPAD_DOWN: {
															if(!paused) {
																SPEAR_SPEED -= 10.0f;
															}															
														 }
														
														break;
						case KeyEvent.KEYCODE_DPAD_RIGHT: 	{
																//player.velocity.x = 250;
																//playerState = AnimationState.WALKING_RIGHT;
															}
															
														break;
						case KeyEvent.KEYCODE_DPAD_LEFT:  	{
																//player.velocity.x = -250;
																//playerState = AnimationState.WALKING_LEFT;
															}
														break;
						case KeyEvent.KEYCODE_BUTTON_SELECT: 
																{
																	paused = false; 
																	//spawn new ball
																	synchronized(balls) {
																		balls.clear();
																		if(balls.size() < BALL_LIMIT && !paused) {
																			balls.add(new DynamicGameObject(WORLD_WIDTH / 2, WORLD_HEIGHT - 32.0f, BALL_RADIUS * 2));
																			balls.get(balls.size() - 1).velocity.x = 150;																		
																		}
																		alive = true;																	
																	}
																}
														break;
						case KeyEvent.KEYCODE_BUTTON_START: 
															startButtonAction();
														break;
						case KeyEvent.KEYCODE_BUTTON_L1: 
														//builder.append("\"L1\" key was pressed");
														break;
						case KeyEvent.KEYCODE_BUTTON_R1: 
														//builder.append("\"R1\" key was pressed");
														break;
					}
				}else if(event.getAction() == KeyEvent.ACTION_UP) {
					if(keyCode > 0 && keyCode < 127) {
	                    pressedKeys[keyCode] = false;
					}
				}
				if(!event.isAltPressed()) {
					validKey = false;
				}
				return validKey;
			}
	}		
}
