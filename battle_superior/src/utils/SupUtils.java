package utils;

public class SupUtils {
	
	/** rmi name of the server */
	public static final String RMI_NAME = "battle_superiorServer";
	
	/** a direction you can go in */
	public enum Direction {
		STAY, NORTH, EAST, SOUTH, WEST
	}
	
	/** specifies a state of the Server and Game*/
	public enum GameState{
		LOBBY, TURNBASED, GAMEOVER
	}
	
	/** states the client can be in */
	public enum PlayerState{
		LOBBY, TURNBASED, INBATTLE, DEAD, GAMEOVER
	}
	
	/**
	 * Tile types (forest, field etc.)
	 * @author Leo
	 *
	 */
	public enum TileType{
		EMPTY, FIELD, FORREST, SEA, MOUNTAIN, PATH, PETTING_ZOO
	}
	
	public static final String moveHelp = "\nChoose one of the following actions:\n"
			+ "h  help"
	   		+ "m  move\n"
			+ "q  quit the program\n";
	   		
	public static final String lobbyHelp = "\nChoose one of the following actions:\n"
			+ "h  open the help\n"
			+ "r  signify you are ready to start the game\n"
			+ "q  quit the program\n"
			+ "sm set the world size\n";
	public static final String moveDirections = "\nWhich direction do you want to move in? Press\n"
	   		+ "1 for North,\n"
	   		+ "2 for East,\n"
	   		+ "3 for South,\n"
	   		+ "4 for West or\n"
	   		+ "0 to stay where you are.\n"
	   		+ "Or press b to go back to the menu.\n";
	public static int convertInputToTarget(String input){
		int tmp = 42;
		
		//überfettes switch
		switch(input){
		case "0" : tmp = 0; break;
		case "1" : tmp = 1; break;
		case "2" : tmp = 2; break;
		case "3" : tmp = 3; break;
		case "4" : tmp = 4; break;
		case "5" : tmp = 5; break;
		case "6" : tmp = 6; break;
		case "7" : tmp = 7; break;
		case "8" : tmp = 8; break;
		case "9" : tmp = 9; break;
		case "q" : tmp = 10; break;
		case "w" : tmp = 11; break;
		case "e" : tmp = 12; break;
		case "r" : tmp = 13; break;
		case "t" : tmp = 14; break;
		case "z" : tmp = 15; break;
		case "u" : tmp = 16; break;
		case "i" : tmp = 17; break;
		case "o" : tmp = 18; break;
		case "p" : tmp = 19; break;
		case "a" : tmp = 20; break;
		case "s" : tmp = 21; break;
		case "d" : tmp = 22; break;
		case "f" : tmp = 23; break;
		case "g" : tmp = 24; break;
		case "h" : tmp = 25; break;
		case "j" : tmp = 26; break;
		case "k" : tmp = 27; break;
		case "l" : tmp = 28; break;
		case "y" : tmp = 29; break;
		case "x" : tmp = 30; break;
		case "c" : tmp = 31; break;
		case "v" : tmp = 32; break;
		case "b" : tmp = 33; break;
		case "n" : tmp = 34; break;
		case "m" : tmp = 35; break;
		}
		return tmp;
	}
	
	//converts the target int (randomly generated) into a String
	public static String convertTargetToInput(int target){
		String tmp = "you done goofed";
		
		//noch ein fettes switch
		switch(target){
		case 0 : tmp = "0"; break; 
		case 1 : tmp = "1"; break;
		case 2 : tmp = "2"; break;
		case 3 : tmp = "3"; break;	
		case 4 : tmp = "4"; break;
		case 5 : tmp = "5"; break; 
		case 6 : tmp = "6"; break;
		case 7 : tmp = "7"; break;
		case 8 : tmp = "8"; break;	
		case 9 : tmp = "9"; break;
		case 10 : tmp = "q"; break; 
		case 11 : tmp = "w"; break;
		case 12 : tmp = "e"; break;
		case 13 : tmp = "r"; break;	
		case 14 : tmp = "t"; break;
		case 15 : tmp = "z"; break; 
		case 16 : tmp = "u"; break;
		case 17 : tmp = "i"; break;
		case 18 : tmp = "o"; break;	
		case 19 : tmp = "p"; break;
		case 20 : tmp = "a"; break; 
		case 21 : tmp = "s"; break;
		case 22 : tmp = "d"; break;
		case 23 : tmp = "f"; break;	
		case 24 : tmp = "g"; break;
		case 25 : tmp = "h"; break; 
		case 26 : tmp = "j"; break;
		case 27 : tmp = "k"; break;
		case 28 : tmp = "l"; break;	
		case 29 : tmp = "y"; break;
		case 30 : tmp = "x"; break; 
		case 31 : tmp = "c"; break;
		case 32 : tmp = "v"; break;
		case 33 : tmp = "b"; break;	
		case 34 : tmp = "n"; break;
		case 35 : tmp = "m"; break;
		}
		
		return tmp;
	}
	
	
}
