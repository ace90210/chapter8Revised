package com.badlogic.androidgames.gamedev2d;

import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.myitems.GLText;
import com.badlogic.androidgames.myitems.TexturedObject;
import com.badlogic.androidgames.framework.SpatialHashGrid;
import com.badlogic.androidgames.framework.GameObject;
import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.impl.GLGame;
import com.badlogic.androidgames.framework.impl.GLGraphics;
import com.badlogic.androidgames.framework.gl.Texture;
import com.badlogic.androidgames.framework.gl.Vertices;
import com.badlogic.androidgames.framework.math.OverlapTester;

public class MyGameTest extends GLGame {
	public Screen getStartScreen() {
		return new MyGameScreen(this);
	}
	
	class MyGameScreen extends Screen implements OnKeyListener {
		final float WORLD_WIDTH = 854.0f;
		final float WORLD_HEIGHT = 480.0f;
		final float BAT_WIDTH = 32.0f;
		final float BAT_HEIGHT = 128.0f;
		final float BALL_WIDTH = 32.0f;
		
		
		float ballSpeedX, ballSpeedY, playerSpeed;
		
		GLGraphics glGraphics;
		GL10 gl;
		TexturedObject p1, p2, ball;
		SpatialHashGrid grid;
		GLText glText;
		
		public MyGameScreen(Game game) {
			super(game);
			glGraphics = ((GLGame)game).getGLGraphics();
			glGraphics.getView().setOnKeyListener(this);
			glGraphics.getView().setFocusable(true);
			glGraphics.getView().requestFocus();
			gl = glGraphics.getGL();
			
			ballSpeedX = 100.0f;
			ballSpeedY = 100.0f;
			playerSpeed = 20.0f;
			
			grid = new SpatialHashGrid(WORLD_WIDTH, WORLD_HEIGHT, BAT_HEIGHT * 2);
			glText = new GLText( gl, ((GLGame)game).getAssets() );

		    glText.load( "ARIAL.TTF", 14, 2, 2 ); 
			
			//make player 1 (bat 1)
			Texture player1Texture = new Texture((GLGame)game, "bat1.png");
			float x = 20.0f;			
			float y = WORLD_HEIGHT / 2;
			float width = BAT_WIDTH;
			float height = BAT_HEIGHT;
			
			Vertices player1Verts = new Vertices(gl, 4, 12, true, true);
			float[] points = new float[] { -width / 2, 	-height / 2,	 1, 1, 1, 1, 0, 1,
										    width / 2, 	-height / 2,	 1, 1, 1, 1, 1, 1,
										    width / 2,    height / 2,  1, 1, 1, 1, 1, 0,
										   -width / 2,   height / 2,  1, 1, 1, 1, 0, 0 };
			player1Verts.setVertices(points, 0, points.length);			
			player1Verts.setIndices(new short[] { 0, 1, 2, 2, 3, 0 }, 0, 6);	
			
			p1 = new TexturedObject( x, y, width, height, player1Texture, player1Verts);
			GameObject object = new GameObject(x, y, width, height);
			grid.insertDynamicObject(object);
			
			//make player 2 (bat 2)
			Texture player2Texture = new Texture((GLGame)game, "bat2.png");
			
			x = WORLD_WIDTH - x;				
			
			Vertices player2Verts = new Vertices(gl, 4, 12, true, true);
			points = new float[] { -width / 2, 	-height / 2,	 1, 1, 1, 1, 0, 1,
								    width / 2, 	-height / 2,	 1, 1, 1, 1, 1, 1,
								    width / 2,   height / 2,  1, 1, 1, 1, 1, 0,
								   -width / 2,   height / 2,  1, 1, 1, 1, 0, 0 };
			player2Verts.setVertices(points, 0, points.length);
			player2Verts.setIndices(new short[] { 0, 1, 2, 2, 3, 0 }, 0, 6);	
			
			p2 = new TexturedObject( x, y, width, height, player2Texture, player2Verts);
			object = new GameObject(x, y, width, height);
			grid.insertDynamicObject(object);
			
			//make ball
			Texture ballTexture = new Texture((GLGame)game, "ball.png");
			x = WORLD_WIDTH / 2;	
			y = WORLD_HEIGHT / 2;
			width = BALL_WIDTH;
			height = BALL_WIDTH;
			
			
			Vertices ballVerts = new Vertices(gl, 4, 12, true, true);			
			points = new float[] { -width / 2, 	-height / 2,	 1, 1, 1, 1, 0, 1,
								    width / 2, 	-height / 2,	 1, 1, 1, 1, 1, 1,
								    width / 2,   height / 2,  1, 1, 1, 1, 1, 0,
								   -width / 2,   height / 2,  1, 1, 1, 1, 0, 0 };
			ballVerts.setVertices(points, 0, points.length);
			ballVerts.setIndices(new short[] { 0, 1, 2, 2, 3, 0 }, 0, 6);	
			
			ball = new TexturedObject( x, y, width, height, ballTexture, ballVerts);
			
			ball.velocity.x = ballSpeedX;
			ball.velocity.y = ballSpeedY;
		}
		
		@Override
		public void update(float deltaTime) {
			p1.position.add(p1.velocity.x * deltaTime, p1.velocity.y * deltaTime);
			p1.bounds.lowerLeft.add(p1.velocity.x * deltaTime, p1.velocity.y * deltaTime);
			
			p2.position.add(p2.velocity.x * deltaTime, p2.velocity.y * deltaTime);
			p2.bounds.lowerLeft.add(p2.velocity.x * deltaTime, p2.velocity.y * deltaTime);
			
			ball.position.add( ball.velocity.x * deltaTime, ball.velocity.y * deltaTime );	
			ball.bounds.lowerLeft.add( ball.velocity.x * deltaTime, ball.velocity.y * deltaTime );

			
			List<GameObject> colliders = grid.getPotentialColliders(ball);
			
			int len = colliders.size();
			for(int i = 0; i < len; i++) {
				GameObject collider = colliders.get(i);
				if(OverlapTester.overlapRectangle(ball.bounds, collider.bounds)) {
					ball.velocity.x = -ball.velocity.x;
				}
			}
			
			
			//if out of bounds invert x or y accordingly
			if(ball.position.x < BALL_WIDTH / 2 || ball.position.x + BALL_WIDTH / 2 > WORLD_WIDTH) {
				ball.velocity.x = -ball.velocity.x;
			}
			if(ball.position.y < BALL_WIDTH / 2 || ball.position.y + BALL_WIDTH / 2> WORLD_HEIGHT) {
				ball.velocity.y = -ball.velocity.y;
			}
		}
		
		@Override
		public void present(float deltaTime) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			
			glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         // Begin Text Rendering (Set Color WHITE)
	        glText.draw( "Line Test", 350, 50 );              // Draw Test String
	        glText.end();                                   // End Text Rendering
	        
			p1.bind();
			gl.glLoadIdentity();
			gl.glTranslatef(p1.position.x, p1.position.y, 0);
			p1.vertices.drawOptimal(GL10.GL_TRIANGLES, 0, 6);
			p1.unbind();
			
			p2.bind();
			gl.glLoadIdentity();
			gl.glTranslatef(p2.position.x, p2.position.y, 0);
			p2.vertices.drawOptimal(GL10.GL_TRIANGLES, 0, 6);
			p2.unbind();
			
			ball.bind();
			gl.glLoadIdentity();
			gl.glTranslatef(ball.position.x, ball.position.y, 0);
			ball.vertices.drawOptimal(GL10.GL_TRIANGLES, 0, 6);
			ball.unbind();		
		}
		
		@Override
		public void resume() {
			gl.glViewport(0, 0, glGraphics.getWidth(), glGraphics.getHeight());
			gl.glClearColor(0, 0, 0, 1);
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glOrthof(0, WORLD_WIDTH, 0, WORLD_HEIGHT, 1, -1);
			gl.glEnable(GL10.GL_TEXTURE_2D);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		}

		@Override
		public void pause() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
		
		private void updateSpatialGrid() {
			grid.clearDynamicCells(p1);
			grid.clearDynamicCells(p2);
			GameObject object = new GameObject(p1.position.x, p1.position.y, p1.bounds.width,  p1.bounds.height);
			grid.insertDynamicObject(object);
			object = new GameObject(p2.position.x, p2.position.y, p2.bounds.width,  p2.bounds.height);
			grid.insertDynamicObject(object);
		}
		
		public boolean onKey(View v, int keyCode, KeyEvent event) 
		{
				boolean validKey = true;
				if(event.getAction() == KeyEvent.ACTION_DOWN) {
					
					switch(event.getKeyCode())
					{
						case KeyEvent.KEYCODE_DPAD_CENTER: {
																if(p2.position.y > BALL_WIDTH)
																{
																	p2.velocity.y = -playerSpeed;
																	updateSpatialGrid();
																}	
														   }																						
														break;
						case KeyEvent.KEYCODE_BACK:  {
														if(event.isAltPressed())
														{							
					        								playerSpeed *= 1.05f;			        					
														}
														else
														{
															validKey = false;
														}
													}
														break;
						case KeyEvent.KEYCODE_BUTTON_X:	{
															playerSpeed *= 0.95f;
														}
														break;
						case KeyEvent.KEYCODE_BUTTON_Y: {
									        				if(p2.position.y + BALL_WIDTH < WORLD_HEIGHT )
															{
									        					p2.velocity.y = playerSpeed;
									        					updateSpatialGrid();
															}
														}													
														break;
						case KeyEvent.KEYCODE_DPAD_UP:  {
															if(p1.position.y + BALL_WIDTH < WORLD_HEIGHT )
															{
									        					p1.velocity.y = playerSpeed;
									        					updateSpatialGrid();
															}
														}
														break;
						case KeyEvent.KEYCODE_DPAD_DOWN: {
															if(p1.position.y > BALL_WIDTH)
															{
																p1.velocity.y = -playerSpeed;
																updateSpatialGrid();
															}	
														 }
														
														break;
						case KeyEvent.KEYCODE_DPAD_RIGHT: 	{
																ball.velocity.x *= 1.05f;
																ball.velocity.y *= 1.05f;
															}
															
														break;
						case KeyEvent.KEYCODE_DPAD_LEFT:  	{
																ball.velocity.x *= 0.95f;
																ball.velocity.y *= 0.95f;
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
				else if(event.getAction() == KeyEvent.ACTION_UP) {
					p1.velocity.y = 0.0f;
					p2.velocity.y = 0.0f;	
					
					if(!event.isAltPressed()) {
						validKey = false;
					}
				}
				return validKey;
			}
	}
}
