package client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;

import server.TargetData;
import server.Tile;

import static utils.SupUtils.*;

public class Client extends UnicastRemoteObject implements ClientInterface{

	private static final long serialVersionUID = 786409760918255712L;
	
	
	private String name;
	/** the Server the client connects to */
	private server.ServerInterface serverStub;
	private static Scanner scanner;
	boolean looping = true;
	boolean battleLoop = true;
	private ClientThread cT;
	private BattleThread bT;
	private Client me;
	final private Object lock = new Object();
	final private Object battleLock = new Object();
	private PlayerState state = PlayerState.LOBBY;
	
	private class ClientThread extends Thread{
		public void run(){
			while(looping){
//				inputInterpreter(scanner);
				inputInterpreter();
			}
//		        System.out.println(WhoisRIR.valueOf("arin".toUpperCase()));
			scanner.close();
			System.exit(0);
		}
	}
	
	private class BattleThread extends Thread{
		Scanner battleScanner = new Scanner(System.in);
		boolean battleIsRunning;
		public void run(){
			while(battleLoop){
				synchronized (battleLock){
					try{
						battleLock.wait();
					} catch (InterruptedException e){
						e.printStackTrace();
					}
				}
				System.out.println("its time for a duel");
//				System.out.println(state.name());
				
				battleIsRunning = true;
				while(battleIsRunning){
					battleInputInterpreter();
				}
			}
			battleScanner.close();
		}
		
		public void battleOver(){
			battleIsRunning = false;
		}
	}
	
	public Client() throws RemoteException{
		
		
		scanner = new Scanner(System.in);
		System.out.println("Please enter your name: ");	
		
		String name = scanner.nextLine();
		String host = "localhost";
		
		try {			
			 
	        Registry reg = LocateRegistry.getRegistry(host);
	        serverStub = (server.ServerInterface) reg.lookup(RMI_NAME);
	        
			me = this;
	        
//	        if(!serverStub.join(this, name)){
//	        	System.out.println("Failed to join the server!");
//	        } else{
//				cT = new ClientThread();
//				cT.start();
//	        }
	        
			//join the server
	        serverStub.join(this, name);
	        //initialize and start the client thread
			cT = new ClientThread();
			cT.start();
			//initialize and start the battlethread
			bT = new BattleThread();
			bT.start();
	        
//			scanner.close();
//			System.exit(0);
	            
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
//	public synchronized void run() throws InterruptedException {
//		try {
//			serverStub.join(me, name);
//			while(looping){
//				String input = scanner.nextLine();
//				inputInterpreter(input);
//				try{
//					wait();
//				} catch (InterruptedException e){
//					System.out.println("I was woken up!");
//				}
//				System.out.println("After the catch");
//			}
////	        System.out.println(WhoisRIR.valueOf("arin".toUpperCase()));
//		} catch (RemoteException e) {
//			System.out.println("Failed to join the server!");
//			e.printStackTrace();
//		}
//	}
	
	private synchronized void inputInterpreter(){
		String input = "";
		input = scanner.nextLine();
		
		if(state == PlayerState.LOBBY){
			switch(input){
			case "r" : try {
					serverStub.playerReady(me);
					System.out.println("I am now ready! The Game will start when all players are ready");
					waitForGameStart();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					   break;
			case "q" : looping = false;
					   System.out.println("Quitting the Programm."); break;
			default : System.out.println("sry i dont know that command");
			}
		} else if(state == PlayerState.TURNBASED){
			switch (input){
			case "move": 
			case "m" : moveInterpreter(); break;
			case "help":
			case "h" : System.out.println(moveHelp); break;
			case "quit":
			case "q" : System.out.println("havent implemented this yet, just close the window"); break;
			}
		} else if(state == PlayerState.DEAD){
			System.out.println("YOU DIED!");
			looping = false;
			battleLoop = false;
		} else if(state == PlayerState.GAMEOVER){
			System.out.println("You already won, go home.");
			looping = false;
			battleLoop = false;
		}
//		else if(state == PlayerState.INBATTLE){
//			try {
//				serverStub.sendAttack(me, input);
//			} catch (RemoteException e) {
//				System.out.println("Disconnected from the server!");
//				e.printStackTrace();
//			}
//		}
	}
	
	private void battleInputInterpreter(){
		Scanner battleScanner = new Scanner(System.in);
		String input = battleScanner.nextLine();
		if(state == PlayerState.INBATTLE){
			try {
				serverStub.sendAttack(me, input);
			} catch (RemoteException e) {
				System.out.println("Disconnected from the server!");
				e.printStackTrace();
			}
		} else if(state == PlayerState.DEAD){
			
		} else {
			try {
				printSurroundings(serverStub.getSurroundings(me));
				
			} catch (RemoteException e) {
				System.out.println("Disconnected from the server!");
				e.printStackTrace();
			}
		}
		
	}
	
	private void moveInterpreter(){
		   System.out.println(moveDirections);
		   
		   //we make a new scanner here because using the old one is causing problems with going back. i dont even know
		   @SuppressWarnings("resource")
		   Scanner moveScanner = new Scanner(System.in);
		   
		   String input = moveScanner.nextLine();
		   switch (input){
		   case "1": move(Direction.NORTH); break;
		   case "2": move(Direction.EAST); break;
		   case "3": move(Direction.SOUTH); break;
		   case "4": move(Direction.WEST); break;
		   case "0": move(Direction.STAY); break;
		   case "b": break;
		   default : System.out.println("moveinterpreter doesnt know shit\n");
		   			 moveInterpreter();
		   			 break;	//aww yeah recursion and shit
		   }	  
	}

	/**sends the players move to the server, waits until the tick starts and then asks the server for his new surroundings.
	 * @param direction the direction the player wants to move in */
	private void move(Direction direction) {
		System.out.println("You are moving " + direction.name());	
		try{
			serverStub.sendActions(me, direction); 
			synchronized(lock){
				try{
//					System.out.println("waiting now");
					lock.wait();
//					System.out.println("done waiting");
				   } catch (InterruptedException e){
					   System.out.println("Next round should be now.");
				   }
			   }
//			System.out.println("after synchronized, getting surroundings");
			printSurroundings(serverStub.getSurroundings(me));
//			System.out.println("gotem");
		   } catch (RemoteException e){
			   System.out.println("Disconnected from the server!");
			   e.printStackTrace();
		   }
	}

	/** waiting for the game to start */
	private void waitForGameStart() {
		synchronized(lock){
			System.out.println("Waiting for the game to start");
			try{
				lock.wait();
			} catch (InterruptedException e){
				e.printStackTrace();
			}
			System.out.println("The Game has started!");
						
			try {
				state = PlayerState.TURNBASED;
				printSurroundings(serverStub.getSurroundings(me));
				
				
			} catch (RemoteException e) {
				System.out.println("Disconnected from the server!");
				e.printStackTrace();
			}
		}
	}
	
	private void printSurroundings(Tile[] tiles){
		System.out.println("You are standing on a " + tiles[0].getType().name());
		System.out.println("North of you is a " + tiles[1].getType().name());
		System.out.println("East of you is a " + tiles[2].getType().name());
		System.out.println("South of you is a " + tiles[3].getType().name());
		System.out.println("West of you is a " + tiles[4].getType().name());
		
		//TODO: uncomment this when we implement 
//		gui.updateTiles(surroundings, direction);
//		printNearbyPlayers();
//		gui.repaint();
	}


	public boolean ping() throws RemoteException {
		return true;
	}

	public void wakeUpTurnBased() throws RemoteException {
		synchronized (lock){
			lock.notify();
		}
	}

	@Override
	public void wakeUpBattle() throws RemoteException {
		synchronized (battleLock){
			state = PlayerState.INBATTLE;
			battleLock.notify();
		}
	}

	@Override
	public void sendTargetData(List<TargetData> tmpData) throws RemoteException {
		for(TargetData curData : tmpData){
			System.out.println(curData.toString());
		}
		System.out.println("");
	}

	@Override
	public void notifyBattleOver() throws RemoteException {
		bT.battleOver();
					//TODO ?
		System.out.println("Battle is over! Press any key to continue.");
//		printSurroundings(serverStub.getSurroundings(me));
		synchronized(lock){
			state = PlayerState.TURNBASED;
			lock.notify();
		}
	}

	@Override
	public void notifyVictory() throws RemoteException {
		state = PlayerState.GAMEOVER;
		System.out.println("You are Victorious!");
		looping = false;
		battleLoop = false;
	}

	@Override
	public void notifyDead() throws RemoteException {
		state = PlayerState.DEAD;
		System.out.println("YOU DIED!");
		looping = false;
		battleLoop = false;
	}
}
