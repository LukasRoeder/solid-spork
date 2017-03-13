package server;

import static utils.SupUtils.*;

import java.io.Serializable;

public class Player implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1273586939530666525L;
		//attributes
		private static int ID_SRC = 0;
		private int id;
		private String playerName;
		private int hp;
		private int xPos;
		private int yPos;
		private boolean ready = false;
		//state of the Client. 0 = Lobby, 1 = turn-based game, 2 = battle, 3 = dead		
		private PlayerState playerState;
		private boolean isAlive = true;

		//constructor
		public Player(String name){
			playerName = name;
			id = ID_SRC++; // use static variable to let the IDs go up per player.
		}
				
		
		//Getter and Setter Methods
		public String getPlayerName() {
			return playerName;
		}

		public void setPlayerName(String playerName) {
			this.playerName = playerName;
		}

		public int getHp() {
			return hp;
		}

		public void setHp(int hp) {
			this.hp = hp;
		}

		public int getXPos() {
			return xPos;
		}

		public void setXPos(int xPos) {
			this.xPos = xPos;
		}

		public int getYPos() {
			return yPos;
		}

		public void setYPos(int yPos) {
			this.yPos = yPos;
		}

		public int getId() {
			return id;
		}
		
		//TODO: maybe make boolean input
		public void setReady(){
			ready = true;
		}
		
		public boolean getReady(){
			return ready;
		}
		
		public PlayerState getState(){
			return playerState;
		}
		
		public void setState(PlayerState stateIn){
			playerState = stateIn;
		}
		
		public boolean isAlive() {
			return isAlive;
		}
		
		public void setAlive(boolean isAlive) {
			this.isAlive = isAlive;
		}
}
