package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import server.TargetData;

public interface ClientInterface extends Remote{


	/**
	 * used to check if the player is still connected to the server
	 * @return
	 * @throws RemoteException
	 */
	public boolean ping() throws RemoteException;
	
	/**
	 * wakes up a player in the turn based state when a new turn begins.
	 * @throws RemoteException
	 */
	public void wakeUpTurnBased() throws RemoteException;

	/**
	 * wakes up the battle thread of a player when he enters battle with another player
	 * @throws RemoteException
	 */
	public void wakeUpBattle() throws RemoteException;

	/**
	 * sends the vital information about all current battle contenders to a client
	 * @param tmpData list of targetData(Name, Hp and Target of a player)
	 * @throws RemoteException
	 */
	public void sendTargetData(List<TargetData> tmpData) throws RemoteException;

	/**
	 * notifies a client that his battle is over and sets his state back to Turnbased,
	 * should only be called for the player winning the battle
	 * @throws RemoteException
	 */
	public void notifyBattleOver() throws RemoteException;
	
	/**
	 * notifies the player that he has won the game, sets his state to gameover.
	 * @throws RemoteException
	 */
	public void notifyVictory() throws RemoteException;
	
	/**
	 * notifies the player that he died and sets his state to dead.
	 * @throws RemoteException
	 */
	public void notifyDead() throws RemoteException;
}
