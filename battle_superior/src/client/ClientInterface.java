package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import server.TargetData;

public interface ClientInterface extends Remote{
	
	/**
	 * debug method to determine if the rmi structure works
	 * @param s
	 * @throws RemoteException
	 */
	public void saySomething(String s) throws RemoteException;

	public boolean ping() throws RemoteException;
	
	public void wakeUpTurnBased() throws RemoteException;

	public void wakeUpBattle() throws RemoteException;

	public void sendTargetData(List<TargetData> tmpData) throws RemoteException;

	void notifyBattleOver() throws RemoteException;
}
