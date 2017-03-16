package server;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFileChooser;

import client.ClientInterface;
import utils.SupUtils.*;

public class Server extends UnicastRemoteObject implements ServerInterface{

	private static final long serialVersionUID = 8766238206613141779L;
	
	/** contains the clients and maps them to a playerId. */
	private Map<ClientInterface, Integer> clientMap;
	private Map<Integer, ClientInterface> clientMapReversed;
	
	/** the server implementation of the game. Most of the game logic happens on this object */
	private Game game = new Game();
	
	/** is true if the game is not yet over */
	private boolean looping;
	
	private ServerThread sT;
	private ServerInputThread sIT;
	
	/** The lock object on which we synchronise. */
	private Object lock = new Object();
	
	private class ServerThread extends Thread{
		public void run(){
			looping = true;
			while(looping){
				//updates stuff
				update();
				try {
					sleep(30);
				} catch (InterruptedException e){
					System.out.println("(ServerThread) I was interrupted!");
				}
			}
		}		
	}
	
	private class ServerInputThread extends Thread{
		private boolean running = true;
		public void run(){
			while(running){
				try {
					inputHandler();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void inputHandler() throws ClassNotFoundException {
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			String input = scanner.nextLine();
			
			switch(input){
			case "s" : saveWorld(); break;
			case "l" : loadSavedWorld(); break;
			case "q" : running = false;
			}
		}

		
		private void saveWorld() {
			synchronized(lock){
				if(game.getState() != GameState.LOBBY){
					int tmpWidth;
					int tmpHeight;
					Tile[][] tmpMap;
					
					tmpWidth = game.getWidth();
					tmpHeight = game.getHeight();
					tmpMap = game.getGameWorld().getMap();
					
					SavedWorld tmpWorld = new SavedWorld(tmpWidth, tmpHeight,tmpMap);
					
					JFileChooser fileChooser = new JFileChooser();
					int returnVal = fileChooser.showSaveDialog(null);
					if(returnVal == JFileChooser.APPROVE_OPTION){
						try{
							OutputStream os = new FileOutputStream(fileChooser.getSelectedFile());
							@SuppressWarnings("resource")
							ObjectOutputStream oos = new ObjectOutputStream(os);
							oos.writeObject(tmpWorld);
							System.out.println("Saving successful!");
						} catch (IOException e){
							System.out.println("File could not be saved!");
							System.out.println(e);
						}
					}
				} else{
					System.out.println("Failed to save World!");
				}
			}
		}


		private void loadSavedWorld() throws ClassNotFoundException{
			synchronized(lock){
				SavedWorld tmpWorld = null;
				JFileChooser fileChooser = new JFileChooser();
				int returnVal = fileChooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						InputStream is = new FileInputStream(fileChooser.getSelectedFile());
						ObjectInputStream ois = new ObjectInputStream(is);
						tmpWorld = (SavedWorld) ois.readObject();
						ois.close();
					} catch (IOException exc) {
						System.out.println("File could not be loaded!");
						System.out.println(exc);
					}
					game.loadSavedWorld(tmpWorld);
					System.out.println("loading successful");
				}	else{
					System.out.println("Loading cancelled.");
				}
			}
		}
	}
	
	/** Constructor */
	public Server() throws RemoteException{
		game = new Game();
		clientMap = new ConcurrentHashMap<>();
		clientMapReversed = new ConcurrentHashMap<>();
		
        System.out.println("The server is open!");
		
		sT = new ServerThread();
		sT.start();
		sIT = new ServerInputThread();
		sIT.start();
	}
	
	/** This does a server tick. */
	private void update() {
		synchronized(lock){
			//check for disconnects(kills a player if he dc'd)
			checkForDisconnects();
			game.removeDeadPlayers();
			switch (game.getState()){
			case LOBBY :
				if(allPlayersReady()){
					System.out.println("the players are ready");
					game.setState(GameState.TURNBASED);
					System.out.println("The game will start now!");
					initGame();
					game.checkForBattles();
					wakeUpClients();
				}
				break;
			case TURNBASED :
				if(game.isOver()){
					game.setState(GameState.GAMEOVER);
					looping = false;
					int winnerId = game.getWinner().getId();
					try{
						clientMapReversed.get(winnerId).notifyVictory();
					} catch (RemoteException e){
						e.printStackTrace();
					}
				} else {
					if(game.everyoneTookAction()){
						game.nextTick();
						game.checkForBattles();
						wakeUpClients();
					}
//					battleHandler();
				}
				battleHandler();
				break;
			case GAMEOVER : break;
			default: System.out.println("you done goofed"); break;
			}
			lock.notifyAll();
		}
	}
	
	/** notifies all clients */
	private void wakeUpClients() {
//		System.out.println("Notifying the clients now.");
		for(ClientInterface c : clientMap.keySet()){
			PlayerState curState = getPlayer(c).getState();
			if(curState == PlayerState.TURNBASED){
				try {
					c.wakeUpTurnBased();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else if(curState == PlayerState.INBATTLE){
				try{
					c.wakeUpBattle();
				} catch (RemoteException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Handles all necessary updated of all battles. 
	 */
	private void battleHandler(){
		//process every battle
		for (Entry<Integer, Battle> battleEntry : game.getBattleIdToBattle().entrySet()){
			Battle curBattle = battleEntry.getValue();
			boolean battleUpdatesSend = false;
			List<TargetData> tmpData = curBattle.getTargetData(); 
			Map<Integer, Player> tmpContenders = curBattle.getContenders();
			//iterate over ever player in the current battle
			for (Entry<Integer, Player> curContender : tmpContenders.entrySet()){
				Integer curContenderId = curContender.getKey();
				Player curPlayer = curContender.getValue();
				//check if the player is alive
				if(curPlayer.isAlive()){
					//check if the battle was updated
					if (curBattle.isBattleUpdated()){
						try {
							battleUpdatesSend = true;
							//Here the data gets send to the client
							clientMapReversed.get(curContenderId).sendTargetData(tmpData);
						} catch (RemoteException e) {
							disconnect(clientMapReversed.get(curContenderId));
							e.printStackTrace();
						}
					} 
				//if the player is dead
				} else {
					System.out.println("killing a player");
					game.killPlayer(curContenderId);
					try {
						clientMapReversed.get(curContenderId).notifyDead();
					} catch (Exception e) {
//						e.printStackTrace();
					}
				}
			}
			if(battleUpdatesSend){
				curBattle.setBattleUpdated(false);
			}
			
			if(curBattle.isBattleOver()){
				battleOverHandler(curBattle);
			} 
		}
	}

	/**
	 * Gets called if the battle is over. Notifies the survivor that the battle is over(wakes him up),
	 * respawns him and sets his state to turnbased. Also ends the battle
	 * @param battle
	 */
	private void battleOverHandler(Battle battle) {
		for (Entry<Integer, Player> contenderEntry : battle.getContenders().entrySet()){
			int contenderId = contenderEntry.getKey();
			Player curPlayer = contenderEntry.getValue();
			ClientInterface curStub = clientMapReversed.get(contenderId);
			
			if(curPlayer.isAlive()){
				game.getEveryPlayerMap().get(contenderId).setState(PlayerState.TURNBASED);
				game.respawn(curPlayer);
				try{
					curStub.notifyBattleOver();
				} catch(RemoteException e){
					e.printStackTrace();
				}
			}
		}
		game.endBattle(battle);
	}
		
//		for (Entry<Integer, Player> contenderEntry : battle.getContenders().entrySet()){
//			int contenderId = contenderEntry.getKey();
//			Player curPlayer = contenderEntry.getValue();
//			SuperiorInterface curStub = clientStubMap.get(contenderId);
//			
//			//respawns the surviver
//			if(curPlayer.isAlive()){
//				game.respawn(curPlayer);
//				try {
//					curStub.notifyClient("BattleOver");
//				} catch (RemoteException e) {
//					removeDisconnectedPlayer(contenderId);
//					e.printStackTrace();
//				}
//				prnt("Tha battle is over! " + curPlayer.getPlayerName() + "(" +contenderId + ") is victorious!");
//					
//				//tests if the whole game is over
//				declareWinner();
//			}
//		}
	

	/** checks for disconnects. Disconnects the clients properly if one is detected */
	private void checkForDisconnects() {
		for(ClientInterface c : clientMap.keySet()){
			try{
				c.ping();
			} catch (RemoteException e){
				System.out.println("Ping failed! for " + getPlayer(c).getPlayerName());
				disconnect(c);
			}
		}
	}
	
	/** properly disconnects the client and removes him from the game */
	private void disconnect(ClientInterface c) {
		int playerId = clientMap.get(c);
		if(!(game.getState() == GameState.LOBBY)){
			game.killPlayer(playerId);
		}
		System.out.println("Player " + clientMap.get(c) + " has disconnected!");
		clientMap.remove(c);
		clientMapReversed.remove(playerId);
	}
	
	/** initialises the game */
	private void initGame() {
		game.start();
		
		System.out.println("The game has started.");
	}
	
	/** checks if all players connected to the server are ready
	 *  @return true if all players are ready, false if not. Also returns false if less then 2 players have connected.*/
	private boolean allPlayersReady(){
		boolean tmp = true;
		for(Integer playerId : clientMap.values()){
			Player curPlayer = game.getEveryPlayerMap().get(playerId);
			if(!curPlayer.getReady()){
				tmp = false;
			} 
		}
		//check if theres more than one player connected to the server
		if(clientMap.size() < 2){
			tmp = false;
		}
		return tmp;
	}
		
	/** returns the player associeted with this clientStub */
	private Player getPlayer(ClientInterface clientIn){
		return game.getEveryPlayerMap().get(clientMap.get(clientIn));
	}
	
	public boolean join(ClientInterface c, String name) throws RemoteException {
		synchronized(lock){
			try{
				lock.wait();
			} catch (InterruptedException e){
				e.printStackTrace();
			}
			if(game.getState() == GameState.LOBBY){
				Player curPlayer = new Player(name);
				int curPlayerId = curPlayer.getId();
				clientMap.put(c, curPlayerId);
				clientMapReversed.put(curPlayerId, c);
				game.join(curPlayer);
				
				System.out.println(name + "(" + curPlayer.getId() + ") has joined the Server! ");
				return true;
			} else{
				return false;
			}
		}
	}
	
	@Override
	public boolean move(ClientInterface c, int direction) throws RemoteException {
		synchronized(lock){
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
		return false;
	}

	@Override
	public void playerReady(ClientInterface clientIn) throws RemoteException {
		synchronized(lock){
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("A Player is ready now!");
			game.getEveryPlayerMap().get(clientMap.get(clientIn)).setReady();
		}
	}
	
	@Override
	public Tile[] getSurroundings(ClientInterface clientIn) throws RemoteException {		
		Tile[] tmp = null;
		int xPos = getPlayer(clientIn).getXPos();
		int yPos = getPlayer(clientIn).getYPos();
		tmp = game.getGameWorld().getSurroundings(xPos, yPos);
		return tmp;
	}

	@Override
	public void sendActions(ClientInterface clientIn, Direction direction) throws RemoteException {
		game.addAction(clientMap.get(clientIn), direction);
	}

	@Override
	public void sendAttack(ClientInterface clientIn, String inputIn) throws RemoteException {
		game.relayAttack(clientMap.get(clientIn), inputIn);
	}

	@Override
	public Map<String, Integer> getNearbyPlayer(ClientInterface clientIn) throws RemoteException {
		
		Map<Integer, Player> tmpPlayerMap = game.getEveryPlayerMap();
		Map<String, Integer> nearbyPlayers = new HashMap<String, Integer>();
		
		int playerId = clientMap.get(clientIn);
		
		int xMe = tmpPlayerMap.get(playerId).getXPos();
		int yMe = tmpPlayerMap.get(playerId).getYPos();
		
		for(Integer key : tmpPlayerMap.keySet()){
			if (tmpPlayerMap.get(key).isAlive()){
				if (key != playerId){
					int xOther = tmpPlayerMap.get(key).getXPos();
					int yOther = tmpPlayerMap.get(key).getYPos();
					String nameOther = tmpPlayerMap.get(key).getPlayerName() + "(" + key + ")";
					
					//Other is north of us
					if(xMe == xOther && yMe == yOther + 1){
						nearbyPlayers.put(nameOther, 1);
					//Other is east of us
					} else if (xMe == xOther - 1 && yMe == yOther){
						nearbyPlayers.put(nameOther, 2);
					//Other is south of us
					} else if (xMe == xOther && yMe == yOther - 1){
						nearbyPlayers.put(nameOther, 3);
					//Other is west of us
					} else if (xMe == xOther + 1 && yMe == yOther){
						nearbyPlayers.put(nameOther, 4);
					//Other is standing on top of you (literally)
					} else if (xMe == xOther && yMe == yOther){
						nearbyPlayers.put(nameOther,0);
					}
				}
			}
		}
		return nearbyPlayers;		
	}
}
