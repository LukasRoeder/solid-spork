package server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import utils.SupUtils.GameState;

import static utils.SupUtils.*;

public class Game {
	
	
	
	private Map<Integer, Player> everyPlayerMap = new ConcurrentHashMap<Integer, Player>();
	//list of arrays, first entry is the player id, second entry is movement
	private Map<Integer, Direction> actionMap = new HashMap<Integer, Direction>();
	//map of every playerId containing a boolean if he has already moved this turn
	private Map<Integer, Boolean> hasTakenActionMap = new HashMap<Integer, Boolean>();
	//map that keeps track of who is alive
	private Map<Integer, Player> alivePlayers = new ConcurrentHashMap<Integer, Player>();
	//Map <battleId, Battle>
	//relates a battleId to an actual battle
	private Map<Integer, Battle> battleIdToBattle = new ConcurrentHashMap<Integer, Battle>();
	//Map <playerId, battleId>
	//relates a player id to a battle id
	private Map<Integer, Integer> playerToBattleId = new ConcurrentHashMap<Integer, Integer>();

	private int width = 4;
	private int height = 4;
	private World gameWorld;
	
	private GameState gameState;
	
	private boolean useLoadedWorld = false;
	
	public Game(){
		gameState = GameState.LOBBY;
	}
	
	private void initActionMap(){
		for (Integer key : everyPlayerMap.keySet()) {
			actionMap.put(key, Direction.STAY);
		}
	}

	//creates the world and spawns every connected player in random positions
	private void initWorld(){
		if (useLoadedWorld){
			//construct the loaded world
			gameWorld = new World(everyPlayerMap);
			gameWorld.spawnPlayers();
		} else{
			//construct a random new world
			gameWorld = new World(width, height, everyPlayerMap);
			gameWorld.spawnPlayers();
		}
	}

	private void movePlayers(){
		for(Entry<Integer, Direction> action : actionMap.entrySet()){
			
			int key = action.getKey();
		    Direction dir = action.getValue();
		    
		    if(everyPlayerMap.get(key).getState() == PlayerState.TURNBASED){
				switch (dir){
				case STAY  :	/* player rests */break;
				case NORTH : movePlayer(key, 0, -1); break;
				case EAST  : movePlayer(key, 1, 0); break;
				case SOUTH : movePlayer(key, 0, 1); break;
				case WEST  : movePlayer(key, -1, 0); break;
				}
		    }
		}
		//}
	}

	//actually moves a given player in a given direction
	private void movePlayer(int playerIn, int xMoveIn, int yMoveIn){
		Player player = everyPlayerMap.get(playerIn);
		int potX = player.getXPos() + xMoveIn;
		int potY = player.getYPos() + yMoveIn;
		if( potX < width && potX >= 0 && potY < height && potY >= 0){
			
			//removes the player from tile
			gameWorld.removeFromTile(player);
			//sets new position
			player.setXPos(potX);
			player.setYPos(potY);
			//puts the player into the new tile
			gameWorld.putIntoTile(player, potX, potY);
			
			}
	}
	
	private void initHasTakenActionMap(boolean bool){
		for (Integer key : everyPlayerMap.keySet()){
			hasTakenActionMap.put(key, bool);
		}
	}

	private void resetHasTakenActionMap() {
		for (Integer key : everyPlayerMap.keySet()){
			hasTakenActionMap.put(key, false);
		}
		
	}

	//initializes the AliveMap
	private void initAliveMap(){
		for(Entry<Integer, Player> entry: everyPlayerMap.entrySet()){
			alivePlayers.put(entry.getKey(), entry.getValue());
		}
	}

	//creates a new player with a given name and adds the player to everyPlayerMap.
	//returns the player id so the client knows who he is.
	public void addPlayer(Player playerIn){
		everyPlayerMap.put(playerIn.getId(), playerIn);
	}

	//makes a player join the server, asks for a name  and returns the player ID for the client
	public void join(Player playerIn){
		addPlayer(playerIn);
	}
	
	//starts the game
	public void start(){
		initActionMap();
		initAliveMap();
		initHasTakenActionMap(false);
		initWorld();
	}		
	
	//adds an action to the actionMap, returns true if every player has added an action to the actionMap for the next Tick.
	public void addAction(int idIn, Direction action){		
		if(everyPlayerMap.get(idIn).isAlive()){
			actionMap.put(idIn, action);
			//Flags the player in the hasTakenActionMap as true for having send an action
			hasTakenActionMap.put(idIn, true);
		}
	}
	
	/**
	 * Tests if everyone has taken action for this turn.
	 * @return
	 */
	public boolean everyoneTookAction(){
		boolean allPlayersHaveTakenAction = true;
		//Tests if all players have send an action. Sets tmp to false if someone didn't.
		for (Entry<Integer, Boolean> entry : hasTakenActionMap.entrySet()){
			int tmpPlayerId = entry.getKey();
			Player tmpPlayer = everyPlayerMap.get(tmpPlayerId);
									//only test players that are in the turn based game
			if (!entry.getValue() && tmpPlayer.getState() == PlayerState.TURNBASED){
				allPlayersHaveTakenAction = false;
				break;
			} 
		}
		return allPlayersHaveTakenAction;
	}
	
	//getter and setter
	public int[] getPlayerPos(int id){
		Player player = everyPlayerMap.get(id);
		int[] array = new int[2];
		array[0] = player.getXPos();
		array[1] = player.getYPos();
		return array;
	}
	
	public Map<Integer, Player> getEveryPlayerMap() {
		return everyPlayerMap;
	}

	public Map<Integer, Direction> getActionMap() {
		return actionMap;
	}

	public World getGameWorld() {
		return gameWorld;
	}
	
	//checks if all players are ready
	public boolean allPlayersReady() {
		//Initialises temp as true, if it never gets set to false it means every player is ready.
		boolean temp = true;
			//iterate over every player in the everyPlayerMap
			for (Player player : everyPlayerMap.values()){ 
				//check if player is not ready
				if (!player.getReady()){
					//if player is not ready, set temp to false and break out of the loop
					temp = player.getReady();
					break;
				}
			}
		return temp;
	}

	/** generates a challenge map of every challenge currently going on and processes them into a battle. */
	public void checkForBattles(){
		//initialise an empty challenge map
		LinkedList<Map<Integer, Player>> challenges;
		//fill the challenge map with all challenges going on atm
		challenges = checkForChallenges();
		Battle tmpBattle;
		//process new battles.
		for (Map<Integer, Player> challenge : challenges){
			tmpBattle = new Battle(challenge);
			int tmpBattleId = tmpBattle.getBattleId();
			assignPlayersToBattle(challenge, tmpBattleId);
			//the battle gets added to the battle list and the battle gets started.
			battleIdToBattle.put(tmpBattleId, tmpBattle);
		}
	}

	/**assign every player in a challenge one battle, remove them from the world and set their state to battle
	 * @param challenge 
	 * @param tmpBattleId
	 */
	private void assignPlayersToBattle(Map<Integer, Player> challenge, int tmpBattleId) {
		for (Player player : challenge.values()){
			//players in battle will fall into a wormhole and get removed from the world.
			removeFromWorld(player);
			//sets the players state to 2 (battling)
			setPlayerState(player.getId(), PlayerState.INBATTLE);
			playerToBattleId.put(player.getId(), tmpBattleId);
		}
	}
	
//			//notify the clients that the battle started
//			for(Entry<Integer, Player> battleContender : tmpBattle.getContenders().entrySet()){
//				int contenderId = battleContender.getKey();
//				SuperiorInterface curClientStub = clientStubMap.get(contenderId);
//				try {
//					curClientStub.notifyClient("battleStart");
//				} catch (RemoteException e) {
//					e.printStackTrace();
//					removeDisconnectedPlayer(contenderId);
//				}
//			}
//			battleUpdateHandler(tmpBattle);
	
//	private void nextTick (){
//		//checks for new battles.
//		LinkedList<Map<Integer, Player>> challenges = game.checkForBattles();
//		
//		Battle tmpBattle;
//
//		//process new battles.
//		for (Map<Integer, Player> challenge : challenges){		
//			tmpBattle = new Battle(challenge);
//			int tmpBattleId = tmpBattle.getBattleId();
//			for (Player player : challenge.values()){
//				//players in battle will fall into a wormhole and get removed from the world.
//				game.removeFromWorld(player);
//				//sets the players state to 2 (battling)
//				game.setPlayerState(player.getId(), 2);
//				playerToBattleId.put(player.getId(), tmpBattleId);
//			}
//			//the battle gets added to the battle list and the battle gets started.
//			battleIdToBattle.put(tmpBattleId, tmpBattle);
//			//notify the clients that the battle started
//			for(Entry<Integer, Player> battleContender : tmpBattle.getContenders().entrySet()){
//				int contenderId = battleContender.getKey();
//				SuperiorInterface curClientStub = clientStubMap.get(contenderId);
//				try {
//					curClientStub.notifyClient("battleStart");
//				} catch (RemoteException e) {
//					e.printStackTrace();
//					removeDisconnectedPlayer(contenderId);
//				}
//			}
//			battleUpdateHandler(tmpBattle);
//		}
//		notifyAllClients("newTick");
//	}
	
//	checks for any possible battles
	/** checks every tile if it has more than one player on it, if so they challenge each other to a battle.
	 * @return challengeList a list of all challenges that are issued this tick.
	 */
	public LinkedList<Map<Integer, Player>> checkForChallenges() {
		//initialise an empty list of challenges
		LinkedList<Map<Integer, Player>> challengeList = new LinkedList<Map<Integer, Player>>();
		//check for every tile if there is more than 1 player on it
		for (int x = 0; x < width; x++){
			for (int y = 0; y < height; y++){
				Map<Integer, Player> playersInTile = gameWorld.getPlayersFromTile(x, y);
				//if theres more than one player on a tile
				if (playersInTile.size() > 1){
					//add them to the challengelist
					challengeList.add(playersInTile);
				}	
			}
		}
		return challengeList;
	}

	public void removeFromWorld(Player player){
		gameWorld.removeFromTile(player);
	}

	//formally and brutally executes the player.
	public void killPlayer(int playerId) {
		everyPlayerMap.get(playerId).setAlive(false);
	}
	
	public void removeDeadPlayer(int playerId){
		removeFromWorld(everyPlayerMap.get(playerId));
		actionMap.remove(playerId);
		alivePlayers.remove(playerId);
		hasTakenActionMap.remove(playerId);
//		everyPlayerMap.remove(playerId);
	}
	
	public void removeDeadPlayers(){
		for (Entry<Integer, Player> entry : everyPlayerMap.entrySet()){
			if (!entry.getValue().isAlive()){
				removeDeadPlayer(entry.getKey());
			}
		}
	}

	public void respawn(Player player) {
		int playerId = player.getId();
		hasTakenActionMap.put(playerId, false);
		everyPlayerMap.get(playerId).setState(PlayerState.TURNBASED);
		gameWorld.putIntoTile(player, player.getXPos(), player.getYPos());
	}

	public GameState getState() {
		return gameState;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	//Flags the player with the playerId as ready
	public void setReady(int playerIdIn){
		everyPlayerMap.get(playerIdIn).setReady();
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setState(GameState stateIn) {
		gameState = stateIn;
	}

	//use this to set the state of a player, never call it directly.
	public void setPlayerState(int playerIdIn, PlayerState stateIn){
		
		everyPlayerMap.get(playerIdIn).setState(stateIn);
	}

	//tests if the game is over
	public boolean isOver() {
		if (alivePlayers.size()<=1){
			return true;
		} else{
			return false;
		}
	}

	//must only get called if the game is over
	public Player getWinner() {
		for (Player player : alivePlayers.values()){
			return player;
		}
		return null;
	}

	@SuppressWarnings("static-access")
	public String loadSavedWorld(SavedWorld tmpWorld) throws ClassNotFoundException {
		String tmp = gameWorld.loadFromFile(tmpWorld);		
		width = tmpWorld.getWidth();
		height = tmpWorld.getHeight();
		if(tmp == "SuccessfullLoad"){
			useLoadedWorld = true;
			System.out.println("(Game.loadSavedWorld)We will use the saved world.");
		}
		return tmp;
	}	
	
	public Map<Integer, Player> getAlivePlayers() {
		return alivePlayers;
	}
	
	public void removeFromTakenActionMap(int playerId){
		hasTakenActionMap.remove(playerId);
	}

	public void nextTick() {
		movePlayers();
		resetHasTakenActionMap();
	}

	public Map<Integer, Battle> getBattleIdToBattle() {
		return battleIdToBattle;
	}

	public void relayAttack(Integer playerId, String inputIn) {
		try{
			Battle curBattle = battleIdToBattle.get(playerToBattleId.get(playerId));
			curBattle.attack(inputIn);
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("Invalid attack!");
		}
	}

	public void endBattle(Battle battle) {
		for(Integer playerId : battle.getContenders().keySet()){
			playerToBattleId.remove(playerId);
		}
			
		battleIdToBattle.remove(battle.getBattleId());
	}
	
}

