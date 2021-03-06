package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;

import client.view.*;
import server.TargetData;
import server.Tile;

import static utils.SupUtils.*;

public class Client extends UnicastRemoteObject implements ClientInterface{

	private static final long serialVersionUID = 786409760918255712L;
	
	//attributes of the client
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
	final private Object guiLock = new Object();
	private PlayerState state = PlayerState.LOBBY;
	private MainGUI gui;
	private BattleGUI bGui;
	private Direction moveDirection = Direction.STAY;
	private SuperiorActionListener listener = new SuperiorActionListener();
	private SuperiorBattleActionListener battleListener = new SuperiorBattleActionListener();
	private boolean hasMoved = false;
	
	private class ClientThread extends Thread{
		public void run(){
			while(looping){
				inputInterpreter();
			}
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
	
	private class MoveThread extends Thread{
		
		Direction direction;
		
		MoveThread(Direction directionIn){
			direction = directionIn;
		}
		
		public void run(){
			System.out.println("You are moving " + direction.name());	
			try{
				synchronized(lock){
				serverStub.sendActions(me, direction); 
					try{
						lock.wait();
					   } catch (InterruptedException e){
						   e.printStackTrace();
					   }
				    }

				printSurroundings(serverStub.getSurroundings(me));
				hasMoved = false;
			   } catch (RemoteException e){
				   System.out.println("Disconnected from the server!");
				   e.printStackTrace();
			   }
		}
	}
	
	public Client() throws RemoteException{
		
		
		scanner = new Scanner(System.in);
		System.out.println("Please enter your name: ");	
		
		name = scanner.nextLine();
		String host = "localhost";
		
		try {			
			 
	        Registry reg = LocateRegistry.getRegistry(host);
	        serverStub = (server.ServerInterface) reg.lookup(RMI_NAME);
	        
			me = this;
	        
			//join the server
	        serverStub.join(this, name);
	        //initialize and start the client thread
			cT = new ClientThread();
			cT.start();
			//initialize and start the battlethread
			bT = new BattleThread();
			bT.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
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
	}
	
	private void battleInputInterpreter(){
		@SuppressWarnings("resource")
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
		   case "1": new MoveThread(Direction.NORTH).start(); break;
		   case "2": new MoveThread(Direction.EAST).start(); break;
		   case "3": new MoveThread(Direction.SOUTH).start(); break;
		   case "4": new MoveThread(Direction.WEST).start(); break;
		   case "0": new MoveThread(Direction.STAY).start(); break;
		   case "b": break;
		   default : System.out.println("moveinterpreter doesnt know shit\n");
		   			 moveInterpreter();
		   			 break;	//aww yeah recursion and shit
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
				gui = new MainGUI(listener);
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
		
		updateGui();
	}
	
	private void updateGui(){
		try {
			gui.updateTiles(serverStub.getSurroundings(me), moveDirection);
			gui.updateNearbyPlayers(serverStub.getNearbyPlayer(me));
			gui.repaint();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
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
			if(state != PlayerState.INBATTLE){
				bGui = new BattleGUI(battleListener);
			}
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
		bGui.updateTargets(tmpData);
	}

	@Override
	public void notifyBattleOver() throws RemoteException {
		bT.battleOver();
		System.out.println("Battle is over! Press any key to continue.");
		synchronized(lock){
			bGui.setVisible(false);
			state = PlayerState.TURNBASED;
			lock.notify();
		}
	}

	@Override
	public void notifyVictory() throws RemoteException {
		if(state == PlayerState.INBATTLE){
			bGui.setVisible(false);
		}
		state = PlayerState.GAMEOVER;
		System.out.println("You are Victorious!");
		gui.sendMessage("The Game is over. You have won! Glory to " + name + "!");
		looping = false;
		battleLoop = false;
	}

	@Override
	public void notifyDead() throws RemoteException {
		state = PlayerState.DEAD;
		System.out.println("YOU DIED!");
		bGui.lastWords();
		looping = false;
		battleLoop = false;
	}
	
	private class SuperiorActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			
			synchronized(guiLock){
				if (!hasMoved){
					
					hasMoved = true;
					
					Object source = e.getSource();
					
					boolean buttonBool = false;
					Direction tmpDirection = Direction.STAY;
					if(source == gui.getUpButton()){tmpDirection = Direction.NORTH; buttonBool=true;} 
					else if(source  == gui.getRightButton()){tmpDirection = Direction.EAST; buttonBool=true;;} 
					else if(source == gui.getDownButton()){tmpDirection = Direction.SOUTH; buttonBool=true;} 
					else if(source == gui.getLeftButton()){tmpDirection = Direction.WEST; buttonBool=true;}
					else if(source == gui.getRestButton1()||source == gui.getRestButton2()){tmpDirection = Direction.STAY; buttonBool=true;}
					if(buttonBool){
						moveDirection = tmpDirection;
						new MoveThread(moveDirection).start();
					}
				}
			}
		}
	}
	
	private class SuperiorBattleActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
		
			String input = bGui.getInputField().getText();
			if(e.getSource() == bGui.getInputField()){
				try{
					serverStub.sendAttack(me, input);
					bGui.clearText();
				} catch (RemoteException exc){
					System.out.println("Disconnected from the server!");
					exc.printStackTrace();
				}
			}
		}
	}
}
