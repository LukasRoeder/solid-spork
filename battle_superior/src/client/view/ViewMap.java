package client.view;

import java.util.HashMap;
import java.util.Map;

import server.Tile;
import utils.SupUtils.Direction;
import utils.SupUtils.TileType;

public class ViewMap {
	
	//The Tiles.
	// key = x-Coordinate, so one column is all tiles within one column
	private Map<Integer, Map<Integer, ViewTile>> columns = new HashMap<Integer, Map<Integer, ViewTile>>();
	
	//is true if we are moving and false if we are not.
	Boolean moving;
	Boolean initialized = false;
	
	//x position of the player in the viewMap
	private int playerX;
	//y position of the player in the viewMap
	private int playerY;
	
	public ViewMap(){
		playerX = 0;
		playerY = 0;
	}
	
//	public void update(int[] surroundings, int direction){
//		
//		moving = testDirection(direction);
//		if (moving){
//			movePlayer(direction);
//			updateMap(surroundings);
//		}
//	}
	
	public void update(Tile[] surroundings, Direction direction){
		
		moving = testDirection(direction);
		if (moving){
			movePlayer(direction);
			updateMap(surroundings);
		}
	}
	
	//This updates the map
	private void updateMap(Tile[] surroundings) {
		
		
		//builds the tiles for the 4 surrounding directions
		buildTile(playerX, playerY, surroundings[0].getType());
		buildTile(playerX, playerY-1, surroundings[1].getType());
		buildTile(playerX+1, playerY, surroundings[2].getType());
		buildTile(playerX, playerY+1, surroundings[3].getType());
		buildTile(playerX-1, playerY, surroundings[4].getType());
		
	}
	
	//actually builds the tiles and puts them into the map
	private void buildTile(int x, int y, TileType type){
		System.out.println("Building Tile: " + x + "," + y + ":" + type);
		ViewTile tile = new ViewTile();
		tile.setX(x);
		tile.setY(x);
		tile.setType(type);
		//checks if the column already exists. 
		if(!columns.containsKey(x)){
			//we create a new column if it doesnt already exist
			Map<Integer, ViewTile> newColumn = new HashMap<Integer,ViewTile>();
			//puts the new column into the map
			columns.put(x, newColumn);
		}
		//puts the tile into the map
		columns.get(x).put(y, tile);
	}

	//moves the player
	private void movePlayer(Direction direction){
			switch(direction){
			case NORTH: playerY--; break;
			case EAST: playerX++; break;
			case SOUTH: playerY++; break;
			case WEST: playerX--; break;
			}
	}
	
	private boolean testDirection(Direction direction){
		switch (direction){
		case STAY : return true;
		case NORTH : if(columns.get(playerX).get(playerY - 1).getType() == TileType.EMPTY){return false;}else{return true;}
		case EAST : if(columns.get(playerX + 1).get(playerY).getType() == TileType.EMPTY){return false;}else{return true;}
		case SOUTH : if(columns.get(playerX).get(playerY + 1).getType() == TileType.EMPTY){return false;}else{return true;}
		case WEST : if(columns.get(playerX - 1).get(playerY).getType() == TileType.EMPTY){return false;}else{return true;}
		}
		return false;
	}

	public Map<Integer, Map<Integer, ViewTile>> getColumns() {
		return columns;
	}

	public int getPlayerX() {
		return playerX;
	}

	public int getPlayerY() {
		return playerY;
	}
}
