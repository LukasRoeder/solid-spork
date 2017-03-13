package server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import utils.SupUtils;

import static utils.SupUtils.*;

public class Battle {
	
	private static int BATTLE_SRC = 0;
	private int battleId;
	private Random random = new Random();
	//map<playerId, player>
	//map of all players in the battle
	private Map<Integer, Player> contenders = new HashMap<Integer, Player>();
	//map<playerId, target>
	//map that gives every playerId in the battle a random number to attack him with
	Map<Integer, Integer> targetNumbers = new ConcurrentHashMap<Integer, Integer>();
	
	private boolean battleUpdated = true;
	


	//constructor
	public Battle(Map<Integer, Player> challenge) {
		battleId = BATTLE_SRC++;
		//apparently we have to do this because line above is too easy
		for(Entry<Integer, Player> entry : challenge.entrySet()){
			int tmpKey = entry.getKey();
			Player tmpPlayer = entry.getValue();
			contenders.put(tmpKey, tmpPlayer);
		}
		//initialise the target map
		initTargets();
		System.out.println("this is the battle in constructor: " + this);
	}
	
	private void initTargets(){
		for(int playerId : contenders.keySet()){
			targetNumbers.put(playerId, random.nextInt(35));
		}
	}
	
	private void rerollTargets(){
		Map<Integer, Integer> tmpTargetNumbers = targetNumbers;
		for (int playerId : targetNumbers.keySet()){
			if(contenders.get(playerId).isAlive()){
				tmpTargetNumbers.put(playerId, random.nextInt(35));	
			} else {
				tmpTargetNumbers.remove(playerId);
			}
		}
		targetNumbers = tmpTargetNumbers;
	}
	
	/**
	 * Handles the attack.
	 * @param input The String input the client scanner send
	 */
	
	public void attack(String input){
		int target = SupUtils.convertInputToTarget(input);
		for(Entry<Integer, Integer> targetEntry : targetNumbers.entrySet()){
			if (targetEntry.getValue() == target){
				System.out.println("SOMEONE HIT A GUY!!");
				Player attackedPlayer = contenders.get(targetEntry.getKey());
				attackedPlayer.setHp(attackedPlayer.getHp()-10);
				if(attackedPlayer.getHp() <= 0){
					attackedPlayer.setAlive(false);
				}
				System.out.println("He now has " + contenders.get(targetEntry.getKey()).getHp() + "HP! HOW CRUEL!");
				
				rerollTargets();
				battleUpdated = true;
			}
		}
	}
	

	public List<TargetData> getTargetData(){
		List<TargetData> dataList = new LinkedList<>();
		for(Entry<Integer, Player> entry : contenders.entrySet()){
			int playerId = entry.getKey();
			if (contenders.get(playerId).isAlive()){
				String target = SupUtils.convertTargetToInput(targetNumbers.get(playerId));
				int targetHp = contenders.get(playerId).getHp();
				String targetName = contenders.get(playerId).getPlayerName();
				dataList.add(new TargetData(target, targetHp, targetName));
			}
		}
		return dataList;
	}
	
	public boolean isBattleOver(){
		int AlivePlayerCount = 0;
		for (Entry <Integer, Player> curContender : contenders.entrySet()){
			if (curContender.getValue().isAlive()){
				AlivePlayerCount++;
			}
		}
		if (AlivePlayerCount <= 1){
			return true;
		} else {
			return false;
		}
		
	}
	
	public Map<Integer, Integer> getTargetNumbers() {
		return targetNumbers;
	}
	
	public int getBattleId() {
		return battleId;
	}
	
	public Map<Integer, Player> getContenders(){
		return contenders;
	}
	
	public boolean isBattleUpdated() {
		return battleUpdated;
	}

	public void setBattleUpdated(boolean battleUpdated) {
		this.battleUpdated = battleUpdated;
	}
	
}
