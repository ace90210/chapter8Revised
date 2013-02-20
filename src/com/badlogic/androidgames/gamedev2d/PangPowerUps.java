package com.badlogic.androidgames.gamedev2d;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

import com.badlogic.androidgames.framework.Game;
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
import com.badlogic.androidgames.myitems.TouchControlsV2;
import com.badlogic.androidgames.myitems.SpearV2;
import com.badlogic.androidgames.myitems.TouchControlsV2.ButtonList;
import com.badlogic.androidgames.myitems.Weapon;
import com.badlogic.androidgames.myitems.PowerItem;
import com.badlogic.androidgames.myitems.Ladder;
import com.badlogic.androidgames.myitems.Platform;
import com.badlogic.androidgames.myitems.TouchElementV2;
import com.badlogic.androidgames.myitems.Weapon.MODE;
import com.badlogic.androidgames.framework.impl.MultiTouchHandler;


public class PangPowerUps extends GLGame {
	public Screen getStartScreen()
	{
		return new PangPowerUpsScreen(this);
	}
	
	class PangPowerUpsScreen extends Screen implements OnKeyListener {
		Camera2D camera;
		MultiTouchHandler mTouch;
		final float WORLD_WIDTH = 820.0f;
		final float WORLD_HEIGHT = 460.0f;

		final float SPEAR_REGION_MAX = 240.0f;
		final float BALL_RADIUS = 16.0f;
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

		SoundPool soundPool;
		int metalPop = -1, pop = -1, fire = -1, stickSound = -1;
				
		PlayerV2 player;
		PowerItem single, doub, stick;
		Weapon weapon;
		//SpearV2 spearSticky;		
		//SpearV2 spear;
		
		boolean paused = false;
		
		PlayerState playerState;
		PlayerV2 savedPlayerState;
		TextureRegion currKeyFrame;		
		Texture background;
		TextureRegion backgroundRegion;
		List<DynamicGameObject> balls;
		List<GameObject> platforms;
		List<Ladder> ladders;
		Texture textureSet;
		
		TextureRegion ballRegion, spearRegion;
		Vector2 gravity = new Vector2(0, -400);
		
		TouchControlsV2 controls;
		SpriteBatcher batcher;
		
		public PangPowerUpsScreen(Game game) {
			super(game);
			glGraphics = ((GLGame)game).getGLGraphics();
			glGraphics.getView().setOnKeyListener(this);
			glGraphics.getView().setFocusable(true);
			glGraphics.getView().requestFocus();
			gl = glGraphics.getGL();
			
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			soundPool = new SoundPool(20,AudioManager.STREAM_MUSIC, 1);
			
			try
			{
				AssetManager assetManager = getAssets();
				AssetFileDescriptor descriptor = assetManager.openFd("metal1.wav");
				metalPop = soundPool.load(descriptor, 1);
				descriptor= assetManager.openFd("pop.wav");
				pop = soundPool.load(descriptor, 1);
				descriptor= assetManager.openFd("fire.wav");
				fire = soundPool.load(descriptor, 1);
				descriptor= assetManager.openFd("stick.wav");
				stickSound = soundPool.load(descriptor, 1);
				
			}
			catch(IOException e)
			{
				
			}
			
			mTouch = new MultiTouchHandler(glGraphics.getView(), 1, 1);
			
			camera = new Camera2D(glGraphics, WORLD_WIDTH, WORLD_HEIGHT);
			playerState = PlayerState.STANDING;
			balls = new ArrayList<DynamicGameObject>();
			ladders  = new ArrayList<Ladder>();
			platforms = new ArrayList<GameObject>();
			
			balls.add(new DynamicGameObject(WORLD_WIDTH / 4, WORLD_HEIGHT - 32.0f, BALL_RADIUS * 2));
			
			for(DynamicGameObject ball: balls) {
				ball.velocity.x = 100;
			}
			
			batcher = new SpriteBatcher(gl, SPRITE_LIMIT);	
			glText = new GLText( gl, ((GLGame)game).getAssets() );
		}
		
		@Override
		public void resume() {
			 glText.load( "ARIAL.TTF", 18, 2, 2 ); 
				textureSet = new Texture((GLGame)game, "BasicPangSet2.png");
				if(savedPlayerState == null) {
					player = new PlayerV2(camera.position.x, 35, 64, 70, 3, textureSet);
					player.setGravity(0, -400);
				} else {
					player = savedPlayerState;
				}
							
				ballRegion = new TextureRegion(textureSet, 0, 0, 128, 128);
								
				weapon = new Weapon(textureSet, 2);				
				TextureRegion singleRegion = new TextureRegion(textureSet, 50, 133, 8, 16);
				single = new PowerItem(singleRegion, 100, 400, 24, 48, 3, MODE.SINGLE);
				
				TextureRegion doubleRegion = new TextureRegion(textureSet, 50, 133, 16, 16);
				doub = new PowerItem(doubleRegion, 100, 400, 48, 48, 3, MODE.DOUBLE);
				
				TextureRegion stickyRegion = new TextureRegion(textureSet, 31, 133, 13, 16);
				stick = new PowerItem(stickyRegion, 100, 400, 40, 48, 3, MODE.STICKY);
				
				ladders.add(new Ladder(textureSet, 250, 100, 50, 200));
				ladders.add(new Ladder(textureSet, 450, 145, 50, 300));
				
				platforms.add(new Platform(textureSet, 300, 288, 300, 16));	
				
				background = new Texture(((GLGame)game), "background.png");
				backgroundRegion = new TextureRegion(background, 0, 0, 512, 256);
				controls = new TouchControlsV2(textureSet, WORLD_WIDTH, WORLD_HEIGHT);
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
					case KeyEvent.KEYCODE_DPAD_CENTER: 	{	//x
																										
														}
													break;
						case KeyEvent.KEYCODE_BACK:  {
														//Circle Pressed
														weapon.mode = Weapon.MODE.STICKY;	
													 }
													break;
					case KeyEvent.KEYCODE_BUTTON_X:	{
															
													}
													break;
					case KeyEvent.KEYCODE_BUTTON_Y: {
															//Triangle pressed		
															weapon.mode = Weapon.MODE.DOUBLE;	
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
																weapon.mode = Weapon.MODE.SINGLE;	
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
			boolean moveLeft = false, moveRight = false, moveUp = false, moveDown= false;
			for(int i = 0; i < 10; i++) {
				int x = mTouch.getTouchX(i);
				int y = glGraphics.getHeight() - mTouch.getTouchY(i);
				for(int j = 0; j < controls.touchElements.size(); j++) {
					TouchElementV2 element = controls.touchElements.get(j);
					if(x > element.bounds.lowerLeft.x && x < element.bounds.lowerLeft.x + element.bounds.width &&
					   y > element.bounds.lowerLeft.y && y < element.bounds.lowerLeft.y + element.bounds.height &&
					   element.ready()
							  ) {
						controls.activeButtons[j] = true;
						element.use(x, y);
						
						//if hit joystick work out directions
						if(j == 0) {
							float DEADZONE = 20;
							
							//left, right
							if(controls.getElement(ButtonList.STICK).position.x + DEADZONE < x) {
								moveRight = true;
							} else if(controls.getElement(ButtonList.STICK).position.x - DEADZONE > x) {
								moveLeft = true;
							}
							
							//up, down
							if(controls.getElement(ButtonList.STICK).position.y + DEADZONE < y) {
								moveUp = true;
							} else if(controls.getElement(ButtonList.STICK).position.y - DEADZONE > y) {
								moveDown = true;
							}
						}
					} 
				}	
			}
			if(controls.activeButtons[3] || keyPressed(KeyEvent.KEYCODE_BUTTON_START)) {
				pressedKeys[KeyEvent.KEYCODE_BUTTON_START] = false;
				startButtonAction();			
			} 
			
			if(!paused) {			
				playerState = PlayerState.STANDING;

				//move player
				if((keyPressed(KeyEvent.KEYCODE_DPAD_RIGHT) || moveRight) && player.position.x + player.bounds.width / 2 < WORLD_WIDTH) 	{
					player.movePlayer(200, 0, deltaTime);
					playerState = PlayerState.WALKING_RIGHT;
				} else if((keyPressed(KeyEvent.KEYCODE_DPAD_LEFT) || moveLeft) && player.position.x - player.bounds.width / 2 > 0) 	{
					player.movePlayer(-200, 0, deltaTime);
					playerState = PlayerState.WALKING_LEFT;
				} else if((keyPressed(KeyEvent.KEYCODE_DPAD_UP) || moveUp)) {
					for(int i =0; i < ladders.size(); i++) {
						Ladder ladder = (Ladder)ladders.get(i);
						if(ladder.upValid(player)) {
							playerState = PlayerState.CLIMBING;
							player.movePlayer(0, 200, deltaTime);
							break;
						}
					}
				} else if((keyPressed(KeyEvent.KEYCODE_DPAD_DOWN) || moveDown)) {
					for(int i =0; i < ladders.size(); i++) {
						Ladder ladder = (Ladder)ladders.get(i);
						if(ladder.downValid(player)) {
							playerState = PlayerState.CLIMBING;					
							player.movePlayer(0, -200, deltaTime);
							break;
						}
					}
				} else{ 
					if((weapon.spear.alive && weapon.spear.position.y < 80) || (weapon.stickySpear.position.y < 80 && weapon.stickySpear.alive) || (weapon.spear2.position.y < 80 && weapon.spear2.alive)) {
						
						playerState = PlayerState.SHOOTING;
					} 				
				}			
				
				if((keyPressed(KeyEvent.KEYCODE_DPAD_CENTER) || controls.activeButtons[1])) {
					//reset button to off
					pressedKeys[KeyEvent.KEYCODE_DPAD_CENTER] = false;					
					//spawn new spear
					if(weapon.shoot(player.position.x, player.position.y)){	
						if(fire != -1) {
							soundPool.play(fire, 0.5f, 0.5f, 0, 0, 1.0f);
						}
						playerState = PlayerState.SHOOTING;
					}
				} 
				if((keyPressed(KeyEvent.KEYCODE_BUTTON_X) || controls.activeButtons[2])) {
					pressedKeys[KeyEvent.KEYCODE_BUTTON_X] = false;
					//square pressed		
					if(player.onFloor(deltaTime, ladders, platforms)){
						player.setVelocity(0, 450);
					}
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
						if(weapon.checkCollisions(ball.boundingCircle)) {							
							if(pop != -1) {
								soundPool.play(pop, 1.0f, 1.0f, 0, 0, 1.0f);
							}
							//if ball smaller than smallest ball limit destroy
							if(ball.boundingCircle.radius < SMALLEST_BALL) {
								balls.remove(ball);
							} else {
								Random rand = new Random();								
								int prize = rand.nextInt(2)+1;
								
								switch(prize) {
									case 0: {
										if(!single.alive) {
											single.setPosition(ball.position);
											single.alive = true;
										}
									} break;
									case 1: {
										if(!doub.alive){
											doub.setPosition(ball.position);
											doub.alive = true;
										}										
									} break;
									case 2: {
										if(!stick.alive) {
											stick.setPosition(ball.position);
											stick.alive = true;
										}										
									} break;
								}
								
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
						
						for(int j = 0; j < platforms.size(); j++) {
							Platform platform = (Platform)platforms.get(j);
						
							if(OverlapTester.overlapCircleRectangle(ball.boundingCircle, platform.bounds)) {
								if(ball.position.x + ball.boundingCircle.radius / 2 > platform.position.x - platform.bounds.width / 2 && ball.position.x - ball.boundingCircle.radius / 2 < platform.position.x + platform.bounds.width / 2) {
									ball.velocity.y = -ball.velocity.y;
									if(ball.position.y < platform.position.y) {
										ball.position.y = platform.position.y - platform.bounds.height / 2 - ball.bounds.width / 2;
									}
									else {
										ball.position.y = platform.position.y + platform.bounds.height / 2 +  ball.bounds.width / 2;
									} 
								}
								else {
									ball.velocity.x = -ball.velocity.x;
									if(ball.position.x < platform.position.x) {
										ball.position.x = platform.position.x - platform.bounds.width / 2 -  ball.bounds.width / 2;
									}
									else {
										ball.position.x = platform.position.x + platform.bounds.width / 2 +  ball.bounds.width / 2;
									} 
								}
							}
						}
						
						//apply changes to ball
						ball.position.add(ball.velocity.x * deltaTime, ball.velocity.y * deltaTime);
						ball.boundingCircle.center.set(ball.position.x, ball.position.y);						
					}	
				}	
				
				//update spear
				if(weapon.alive) {					
					weapon.update(deltaTime, platforms);					
				}
				if(single.alive) {
					if(OverlapTester.overlapRectangle(player.bounds, single.bounds)) {
						weapon.mode = MODE.SINGLE;
						single.alive = false;
					}
					single.update(deltaTime, platforms);
				}
				if(doub.alive) {
					if(OverlapTester.overlapRectangle(player.bounds, doub.bounds)) {
						weapon.mode = MODE.DOUBLE;
						doub.alive = false;
					}
					doub.update(deltaTime, platforms);
				}
				if(stick.alive) {
					if(OverlapTester.overlapRectangle(player.bounds, stick.bounds)) {
						weapon.mode = MODE.STICKY;
						stick.alive = false;
					}
					stick.update(deltaTime, platforms);
				}
				
				player.update(deltaTime, playerState, ladders, platforms);
			}		
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
			for(int i = 0; i < platforms.size(); i++) {
				Platform platform = (Platform)platforms.get(i);
				for(GameObject tile: platform.tiles) {
					batcher.drawSprite(tile.position.x, tile.position.y, tile.bounds.width, tile.bounds.height, platform.tileRegion);
				}		
			}
			
			for(int i = 0; i < ladders.size(); i++) {
				Ladder ladder = (Ladder)ladders.get(i);
				for(GameObject tile: ladder.tiles) {
					batcher.drawSprite(tile.position.x, tile.position.y, tile.bounds.width, tile.bounds.height, ladder.tileRegion);
				}		
			}
			batcher.endBatch();
			
			
			if(single.alive) {
				batcher.beginBatch(textureSet);
				if(single.life < 1 && single.life * 4 % 2 > 1) {
					gl.glColor4f(0.4f, 0.4f, 0.5f, 1.0f);
				}
				batcher.drawSprite(single.position.x, single.position.y, single.bounds.width, single.bounds.height, single.region);
				batcher.endBatch();
				gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);	
			}
			if(doub.alive) {
				batcher.beginBatch(textureSet);
				if(doub.life < 1 && doub.life * 4 % 2 > 1) {
					gl.glColor4f(0.4f, 0.4f, 0.5f, 1.0f);
				}
				batcher.drawSprite(doub.position.x, doub.position.y, doub.bounds.width, doub.bounds.height, doub.region);
				batcher.endBatch();
				gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);	
			}
			if(stick.alive) {
				batcher.beginBatch(textureSet);
				if(stick.life < 1 && stick.life * 4 % 2 > 1) {
					gl.glColor4f(0.4f, 0.4f, 0.5f, 1.0f);
				}
				batcher.drawSprite(stick.position.x, stick.position.y, stick.bounds.width, stick.bounds.height, stick.region);
				batcher.endBatch();
				gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);	
			}
			
			
			

			if(weapon.spear.alive) {
				//draw spear 1
				batcher.beginBatch(textureSet);	
				if(weapon.spear.stick && weapon.spear.activeTime < 1) {
					if(weapon.spear.activeTime * 4 % 2 > 1) {
						gl.glColor4f(0.4f, 0.4f, 0.5f, 1.0f);
					}
				}
				batcher.drawSprite(weapon.spear.position.x, weapon.spear.position.y, weapon.spear.bounds.width, weapon.spear.bounds.height, weapon.spear.region);
				batcher.endBatch();
				gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);			
				
			}
			if(weapon.spear2.alive) {
				//draw spear 2
				batcher.beginBatch(textureSet);	
				if(weapon.spear2.stick && weapon.spear2.activeTime < 1) {
					if(weapon.spear2.activeTime * 4 % 2 > 1) {
						gl.glColor4f(0.4f, 0.4f, 0.5f, 1.0f);
					}
				}
				batcher.drawSprite(weapon.spear2.position.x, weapon.spear2.position.y, weapon.spear2.bounds.width, weapon.spear2.bounds.height, weapon.spear2.region);
				batcher.endBatch();
				gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			}
			if(weapon.stickySpear.alive) {
				//draw sticky spear
				batcher.beginBatch(textureSet);	
				if(weapon.stickySpear.stick && weapon.stickySpear.activeTime < 1) {
					if(weapon.stickySpear.activeTime * 4 % 2 > 1) {
						gl.glColor4f(0.4f, 0.4f, 0.5f, 1.0f);
					}
				}
				batcher.drawSprite(weapon.stickySpear.position.x, weapon.stickySpear.position.y, weapon.stickySpear.bounds.width, weapon.stickySpear.bounds.height, weapon.stickySpear.region);
				batcher.endBatch();
				gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			}
			
			
			//draw player
			batcher.beginBatch(textureSet);
			if(player.hurt() && player.getImmuneTime() * 10 % 2 > 1) {
				gl.glColor4f(1.0f, 0.2f, 0.2f, 1.0f);
			}
			batcher.drawSprite(player.position.x, player.position.y, player.bounds.width, player.bounds.height, player.getKeyFrame());	
			batcher.endBatch();
			if(player.hurt()) {
				gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			}	
			
			
			batcher.beginBatch(textureSet);
			for(int i = 0; i < balls.size(); i++) {
				DynamicGameObject ball = balls.get(i);
				batcher.drawSprite(ball.position.x, ball.position.y, ball.boundingCircle.radius * 2, ball.boundingCircle.radius * 2, ballRegion);
			}
			for(TouchElementV2 element :controls.touchElements) {
				batcher.drawSprite(element.position.x, element.position.y, element.width, element.height, element.region);
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
	        glText.drawC( "player.LL: " +  player.bounds.lowerLeft.y + " plat.tr : " +  (platforms.get(0).position.y + platforms.get(0).bounds.height / 2), camera.position.x, camera.position.y + (camera.frustumHeight / 2) * camera.zoom - (20 * camera.zoom) ); 
	        glText.end();                                   
		}

		@Override
		public void dispose() {
			
		}
		
		
	}
}
