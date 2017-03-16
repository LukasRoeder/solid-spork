package server;

import java.io.Serializable;

public class TargetData implements Serializable{
	
	/** generated this cuz it was fun */
	private static final long serialVersionUID = -3391229442727774563L;
	private String target;
	private int hp;
	private String name;
	
	public TargetData(String targetIn, int hpIn, String nameIn){
		target = targetIn;
		hp = hpIn;
		name = nameIn;
	}

	public String getTarget() {
		return target;
	}

	public int getHp() {
		return hp;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + " HP: " + hp + "  " + target;
	}	
}
