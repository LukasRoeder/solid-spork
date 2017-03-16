package server;

import java.io.Serializable;
import java.util.Map;
import java.util.Random;
import static utils.SupUtils.*;

public class World implements Serializable {
	
	private static final long serialVersionUID = -926081498054600591L;
	
	//attributes
	private int width;
	private int height;
	
	//loaded static variables
	private static int loadedWidth;
	private static int loadedHeight;
	private static Tile[][] loadedMap;
	
	private Map<Integer, Player> everyPlayerMap;
//	private int numPlayers;
	
	private Tile[][] map;
	private Random random = new Random();
	
	//constuctor of the world
	public World(int width, int height, Map<Integer, Player> playerMapIn){
		System.out.println("(World) new Constructor");
		this.width = width;
		this.height = height;
		everyPlayerMap = playerMapIn;
//		this.numPlayers = everyPlayerMap.size();

		map = new Tile[width][height];
		
		createTiles();	
	}
	
	public World(Map<Integer, Player> playerMapIn){
		System.out.println("(World) loaded Constructor");
		width = loadedWidth;
		height = loadedHeight;
		everyPlayerMap = playerMapIn;
		
		map = loadedMap;
	}
	
	//Creates the tiles of the game map
	private void createTiles(){
		for (int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				map[i][j] = new Tile(getRandomTileType());
			}
		}
	}
	
	public TileType getRandomTileType(){
		int tmp = random.nextInt(4) + 1;
		TileType type;
		
		switch(tmp){
		case 0: type = TileType.EMPTY; break;
		case 1: type = TileType.FIELD; break;
		case 2: type = TileType.FORREST; break;
		case 3: type = TileType.MOUNTAIN; break;
		case 4: type = TileType.PATH; break;
		case 5: type = TileType.SEA; break;
			default: type = TileType.PETTING_ZOO;
		}
		return type;
	}
	
	//getting the surrounding tile types, north is 'under' the player, the map is build    S
	//																					 W   O
	//																					   N
	public Tile getNorth(int x, int y){
		if (y-1 < 0){
			return new Tile(TileType.EMPTY);
		} 
		else{
			return map[x][y-1];
		}
	}
	
	public Tile getSouth(int x, int y){
		if (y+1 >= height){
			return new Tile(TileType.EMPTY);
		} 
		else{
			return map[x][y+1];
		}
	}
	
	public Tile getEast(int x, int y){
		if (x+1 >= width){
			return new Tile(TileType.EMPTY);
		}
		else{
			return map[x+1][y];
		}
	}
	
	public Tile getWest(int x, int y){
		if (x-1 < 0){
			return new Tile(TileType.EMPTY);
		}
		else{
			return map[x-1][y];
		}
	}
	
	public Tile getCurField(int x, int y){
		return map[x][y];
	}
	
	/** returns the surrounding Tiles at the postion x, y 
	 * @param x X Coordinate
	 * @param y Y Coordinate
	 * @return Tile
	 */
	public Tile[] getSurroundings(int x, int y){
		Tile[] tmp = new Tile[5];
		tmp[0] = getCurField(x,y);
		tmp[1] = getNorth(x,y);
		tmp[2] = getEast(x,y);
		tmp[3] = getSouth(x,y);
		tmp[4] = getWest(x,y);		
		return tmp;
	}

	
	//this is used at gamestart to spawn the players into the world at a random possition, also sets their state to 1
	public void spawnPlayers(){
		Random random = new Random();
		for (Player player : everyPlayerMap.values()) {
			int xOut = random.nextInt(width);
			int yOut = random.nextInt(height);
			player.setState(PlayerState.TURNBASED);
		    player.setHp(100);
		    player.setXPos(xOut);
		    player.setYPos(yOut);
		    putIntoTile(player, xOut, yOut);
		}
	}
	
	public void putIntoTile(Player player, int x, int y){
		map[x][y].moveInto(player);
	}
	
	public void removeFromTile(Player player){
//		System.out.println("Player to remove from Tile: " + player);
		map[player.getXPos()][player.getYPos()].moveOutOf(player.getId());
	}
	
	public Map<Integer, Player> getPlayersFromTile(int x, int y){
		return map[x][y].getPlayers();
	}
	
	//loads the world
	//TODO needs testing as soon as we can save a map
	public static String loadFromFile (SavedWorld savedWorld) throws ClassNotFoundException{
		
		int tmpWidth = savedWorld.getWidth();
		int tmpHeight = savedWorld.getHeight();
		Tile[][] tmpMap = savedWorld.getMap();
		
		String tmpError = "SuccessfullLoad";
		
		boolean corrupted = false;
		
		for (Tile[] row : tmpMap){
			for (Tile tile : row){
				if (!tile.getPlayers().isEmpty()){
					corrupted = true;
				}
			}
		}
		
		if (tmpWidth <= 0 || tmpHeight <= 0 || corrupted){
			tmpError = "CorruptedWorld";
			System.out.println("(World.loadfromFile) World could not be loaded");
		}
		
		System.out.println("loaded width: " + loadedWidth + "");
		loadedWidth = tmpWidth;
		loadedHeight = tmpHeight;
		loadedMap = tmpMap;
		
		return tmpError;
		
		
	}

	public Tile[][] getMap() {
		
		Tile[][] tmpMap = map;
		
		for (Tile[] row : tmpMap){
			for(Tile tile : row){
				tile.removeAllPlayers();
			}
		}
		return tmpMap;
	}
	
	
	
	
}
