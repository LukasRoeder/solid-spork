package server;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static utils.SupUtils.*;

public class Tile implements Serializable{

	private static final long serialVersionUID = 8231368211537585542L;
	//attributes
	private TileType type;
	private Map<Integer, Player> players = new ConcurrentHashMap<Integer, Player>();

	//constructor
	public Tile(TileType typeIn){
		setType(typeIn);
	}
	
	//sets the type of a field
	public void setType(TileType typeIn){
		type = typeIn;
	}
	public TileType getType(){
		return type;
	}
	public void moveInto(Player player){
		players.put(player.getId(), player);
	}
	public void moveOutOf(int id){
		players.remove(id);
	}
	public Map<Integer, Player> getPlayers() {
		return players;
	}

	public void removeAllPlayers() {
		players.clear();
	}

	
//	switch (x){
//	case 0: type = "field"; break;
//	case 1: type = "forest"; break;
//	case 2: type = "mountain"; break;
//	case 3: type = "sea"; break;
//	case 4: type = "path"; break;
//	}
}
