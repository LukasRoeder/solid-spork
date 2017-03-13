package server;

import java.io.Serializable;

public class SavedWorld implements Serializable{

	private static final long serialVersionUID = 1135247586600189911L;
	
	private int width;
	private int height;
	private Tile[][] map;
	
	public SavedWorld(int widthIn, int heightIn, Tile[][] mapIn) {
		width = widthIn;
		height = heightIn;
		map = mapIn;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public Tile[][] getMap() {
		return map;
	}
	public void setMap(Tile[][] map) {
		this.map = map;
	}

	
}
