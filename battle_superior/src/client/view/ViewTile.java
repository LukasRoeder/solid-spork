package client.view;

import utils.SupUtils.TileType;

public class ViewTile {
	
	private TileType type;
	private int x;
	private int y;
	
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	//sets the type of a field
	public void setType(TileType typeIn){
		type = typeIn;
	}
	public TileType getType(){
		return type;
	}
	
//case 0: return "nothing";
//case 1: return "field"; 
//case 2: return "forest"; 
//case 3: return "mountain";
//case 4: return "sea"; 
//case 5: return "path";
//case 6: return "petting zoo";
//}
}
