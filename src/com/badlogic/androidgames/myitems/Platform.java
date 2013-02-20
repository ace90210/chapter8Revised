package com.badlogic.androidgames.myitems;

import com.badlogic.androidgames.framework.GameObject;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.gl.Texture;

import java.util.List;
import java.util.ArrayList;

public class Platform extends GameObject {
	private static int TILE_WIDTH = 16;
	public TextureRegion tileRegion;
	public List<GameObject> tiles;
	
	public Platform(Texture texture, float x, float y, float width, float height) {
		super(x, y, width, height);
		
		int remainder = (int)width % TILE_WIDTH;
		tiles = new ArrayList<GameObject>();
		
		if(width >= 16) {
			tileRegion = new TextureRegion(texture, 33, 155, 16, 8);
			int numTiles = (int)width / TILE_WIDTH;
					
			for(int i = 0; i < numTiles; i++) {
				//position y - half full height and count down from there by i x tile height
				tiles.add(new GameObject((position.x + width / 2) - (i * TILE_WIDTH) - (TILE_WIDTH / 2), position.y, TILE_WIDTH, height));
			}		
			tiles.add(new GameObject((position.x - width / 2) + remainder, position.y, TILE_WIDTH, height));
		}
	}
	
	public boolean onPlatform(PlayerV2 p) {
		if(p.position.x + p.bounds.width / 4 > bounds.lowerLeft.x && p.position.x - p.bounds.width / 4 < bounds.lowerLeft.x + bounds.width) {
			if(p.position.y >= (position.y - bounds.height / 2) + p.bounds.height / 2) {
				if(p.position.y - p.bounds.height / 2 < (position.y + bounds.height / 2)) {
					return true;
				}
			}
		}
		return false;
	}
}
