package server;


import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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
	
	/** The lock object on which we synchronise. */
	private Object lock = new Object();
	
	private class ServerThread extends Thread{
		public void run(){
			System.out.println("running");
			looping = true;
			while(looping){
				//updates stuff
				update();
				try {
					sleep(300);
				} catch (InterruptedException e){
					System.out.println("(ServerThread) I was interrupted!");
				}
				
				
//				System.out.println("iWasNotified");
//				System.out.println("clientsMap: "+ clientsMap);
//				if(allPlayersReady()){
//					System.out.println("the players are ready");
//					looping = false;
//				}
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
	}
	
	/** This does a server tick. */
	private void update() {
//		System.out.println("game everPlayerMap: " + game.getEveryPlayerMap());
		synchronized(lock){
			checkForDisconnects();
			switch (game.getState()){
			case LOBBY :
				if(allPlayersReady()){
					System.out.println("the players are ready");
					game.setState(GameState.TURNBASED);
					System.out.println("The game will start now!");
					initGame();
					game.checkForBattles();
				}
				break;
			case TURNBASED :
				if(game.isOver()){
					game.setState(GameState.GAMEOVER);
					int winnerId = game.getWinner().getId();
					try{
						clientMapReversed.get(winnerId).notifyVictory();
					} catch (RemoteException e){
						e.printStackTrace();
					}
				} else {
//					System.out.println(game.everyoneTookAction());
					if(game.everyoneTookAction()){
						game.nextTick();
						game.checkForBattles();
						wakeUpClients();
					}
					battleHandler();
				}
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
		for (Entry<Integer, Battle> battleEntry : game.getBattleIdToBattle().entrySet()){
			Battle curBattle = battleEntry.getValue();
			boolean battleUpdatesSend = false;
			List<TargetData> tmpData = curBattle.getTargetData(); 
			Map<Integer, Player> tmpContenders = curBattle.getContenders();
			for (Entry<Integer, Player> curContender : tmpContenders.entrySet()){
				Integer curContenderId = curContender.getKey();
				Player curPlayer = curContender.getValue();
				if(curPlayer.isAlive()){
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
				} else {
					System.out.println("killing a player");
					game.killPlayer(curContenderId);
					try {
						clientMapReversed.get(curContenderId).notifyDead();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
//					if (curBattle.isBattleUpdated()){
//						try {
//							battleUpdatesSend = true;
//							//Here the data gets send to the client
//							clientMapReversed.get(curContenderId).sendTargetData(tmpData);
//						} catch (RemoteException e) {
//							disconnect(clientMapReversed.get(curContenderId));
//							e.printStackTrace();
//						}
//					}
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
		//TODO: This is not finished yet. Players need to be removed from clientMap and clientMapReversed
		int playerId = clientMap.get(c);
		if(!(game.getState() == GameState.LOBBY)){
			game.killPlayer(playerId);
		}
		System.out.println("Player " + clientMap.get(c) + " has disconnected!");
		clientMap.remove(c);
		clientMapReversed.remove(playerId);
		game.getEveryPlayerMap().remove(playerId);
	}
	
	/** initialises the game */
	private void initGame() {
		game.start();
		
		System.out.println("The game has started.");
		wakeUpClients();
	}
	
//	public void init(){
//		System.out.println("Initializing the server...");
//		try {
////	        LocateRegistry.createRegistry(Registry.REGISTRY_PORT).rebind(RMI_NAME, new Server());
////	        System.out.println("The server is open!");
//	        
//	        sT = new ServerThread();
//	        sT.start();
//	        
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        System.exit(1);
//	    }
//	}

//	private synchronized void run() throws InterruptedException{
//		System.out.println("running");
//		boolean looping = true;
//		while(looping){
//			try {
//				
//			} catch (InterruptedException e){
//				System.out.println("Too impatient to wait");
//			}
//			System.out.println("iWasNotified");
//			System.out.println("clientsMap: "+ clientsMap);
//			if(allPlayersReady()){
//				System.out.println("the players are ready");
//				looping = false;
//			}
//		}
//	}
	
	/** checks if all players connected to the server are ready
	 *  @return true if all players are ready, false if not. Also returns false if less then 2 players have connected.*/
	private boolean allPlayersReady(){
//		System.out.println("checking if all players Rdy");
//		System.out.println("allplayersRdy clientsmap : " + clientMap);
		boolean tmp = true;
		for(Integer playerId : clientMap.values()){
			Player curPlayer = game.getEveryPlayerMap().get(playerId);
			if(!curPlayer.getReady()){
//				System.out.println("someone is not ready");
				tmp = false;
			} 
		}
		//check if theres more than one player connected to the server
		if(clientMap.size() < 2){
			tmp = false;
//			System.out.println("Theres not enough players connected to the Server to play the game. You need alteast 2 players.");
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
			System.out.println("Player am ready now! ");
			game.getEveryPlayerMap().get(clientMap.get(clientIn)).setReady();
		}
	}
	
	@Override
	public Tile[] getSurroundings(ClientInterface clientIn) throws RemoteException {		
		Tile[] tmp = null;
		int xPos = getPlayer(clientIn).getXPos();
		int yPos = getPlayer(clientIn).getYPos();
		tmp = game.getGameWorld().getSurroundings(xPos, yPos);
		System.out.println("Surroundings: \nTile[]: " + tmp + "\nxPos: " + xPos + "\nyPos: " + yPos);
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
}
