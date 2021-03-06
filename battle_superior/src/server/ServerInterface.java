package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import client.ClientInterface;
import utils.SupUtils.Direction;

public interface ServerInterface extends Remote{

	/**
	 * join the server via this method.
	 * @param c the Client to register
	 * @param name the name of the player. Names that already exist are allowed,
	 * since players can be identified by a unique ID in the player object.
	 * @throws RemoteException
	 */
	public boolean join(ClientInterface c, String name) throws RemoteException;
	
	/** Use this to send your movement */
	public boolean move(ClientInterface c, int direction) throws RemoteException;

	/** Signify that you are ready */
	public void playerReady(ClientInterface clientIn) throws RemoteException;

	/** returns your surrounding Tiles
	 * @param clientIn 
	 * @return
	 * @throws RemoteException
	 */
	public Tile[] getSurroundings(ClientInterface clientIn) throws RemoteException;

	/**
	 * send your movement direction to the Server
	 * @param clientIn you
	 * @param direction the direction you want to move to
	 * @throws RemoteException
	 */
	public void sendActions(ClientInterface clientIn, Direction direction) throws RemoteException;

	/**
	 * relays the attack of the player to the game.
	 * @param clientIn the attacking player
	 * @param inputIn the attack of the player
	 * @throws RemoteException
	 */
	public void sendAttack(ClientInterface clientIn, String inputIn) throws RemoteException;
	
	/**
	 * Returns the players on all adjacent tiles of the client calling this method.
	 * @param playerId
	 * @param nearbyPlayers
	 * @return a Hashmap of playerNames and their IDs
	 * @throws RemoteException
	 */
	public Map<String, Integer> getNearbyPlayer(ClientInterface clientIn) throws RemoteException;
}
