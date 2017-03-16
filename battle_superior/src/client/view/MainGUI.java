package client.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import server.Tile;
import static utils.SupUtils.*;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;



public class MainGUI extends JFrame  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3331023724041787963L;
	//game world map 
	private ViewMap map = new ViewMap();
	//nearby player map
	private Map<String, Integer> nearbyPlayerMap = new ConcurrentHashMap<String, Integer>();
	private int unit = 80;
	private int halfMapSize = 300;
	private int playerSize = (int)(unit * 0.7);
	private int enemySize = (int)(unit * 0.5);
	private int nameOffset = (int)(unit * 0.2);
	private int playerOffset = (int)((unit - playerSize)/2);
	private int enemyOffset = (int)((unit - enemySize)/2);
	
	private JButton restButton1 = new JButton("Don't move");
	private JButton restButton2 = new JButton("Don't move");
	private JButton upButton = new JButton("Move Up");
	private JButton leftButton = new JButton("Move Left");
	private JButton downButton = new JButton("Move Down");
	private JButton rightButton = new JButton("Move Right");
	
	private JLabel messager = new JLabel("Battle Inferior!");
	
	


	public MainGUI(ActionListener listener){
		super("Battle Inferior");
		
		//Main panel. Everything is on here
		JPanel mainPanel = new JPanel();
		
		//This will display the map
		JPanel mapPanel = new GameGraphicsPanel();
		//This will contain things that can notify the user
		JPanel notifierPanel = new JPanel();
		
		JPanel controllerPanel = new JPanel();
		
		messager.setBackground(Color.red);
		
		notifierPanel.add(messager);
		
		restButton1.addActionListener(listener);
		upButton.addActionListener(listener);
		restButton2.addActionListener(listener);
		leftButton.addActionListener(listener);
		downButton.addActionListener(listener);
		rightButton.addActionListener(listener);
		
		controllerPanel.add(restButton1);
		controllerPanel.add(upButton);
		controllerPanel.add(restButton2);
		controllerPanel.add(leftButton);
		controllerPanel.add(downButton);
		controllerPanel.add(rightButton);
		
		controllerPanel.setLayout(new GridLayout(2,3));

		
		//sets the preffered size of the panel. Prevents the screen from being squished
		mapPanel.setPreferredSize(new Dimension(halfMapSize*2,halfMapSize*2));
		
		mapPanel.setVisible(true);
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		//Adds the mapPanel and the notifierPanel to the mainPanel
		mainPanel.add(mapPanel);
		mainPanel.add(notifierPanel);
		mainPanel.add(controllerPanel);
		
		//Sets the Layout of the mainPanel;		
		add(mainPanel);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationByPlatform(true);
		setVisible(true);
	}
	
	public void updateTiles(Tile[] surroundings, Direction direction) {
		map.update(surroundings, direction);
	}
	
	public Color getColor(TileType type) {
		
		switch(type){
		case EMPTY: return Color.black;
		case FIELD: return Color.yellow; 
		case FORREST: return Color.green; 
		case MOUNTAIN: return Color.getHSBColor(30, 100, 30);
		case SEA: return Color.blue; 
		case PATH: return Color.getHSBColor(40, 100, 65);
		case PETTING_ZOO: return Color.magenta;
		default: return Color.white;
		}
		
		
		
		//case 0: return "nothing";
		//case 1: return "field"; 
		//case 2: return "forest"; 
		//case 3: return "mountain";
		//case 4: return "sea"; 
		//case 5: return "path";
		//case 6: return "petting zoo";
		//}
	}
	
	public void updateNearbyPlayers(Map<String, Integer> nearby) {
		nearbyPlayerMap.clear();
		nearbyPlayerMap = nearby;
	}
	
	public JButton getRestButton1() {
		return restButton1;
	}
	public JButton getRestButton2() {
		return restButton2;
	}
	public JButton getUpButton() {
		return upButton;
	}

	public JButton getLeftButton() {
		return leftButton;
	}

	public JButton getDownButton() {
		return downButton;
	}

	public JButton getRightButton() {
		return rightButton;
	}
	
	public void sendMessage(String message){
		messager.setText(message);
	}
	
	class GameGraphicsPanel extends JPanel{
		private static final long serialVersionUID = -6945198394136770675L;

		public void paint(Graphics g){
			
			int playerX = map.getPlayerX();
			int playerY = map.getPlayerY();
			
			//Paints the tiles
			for(Entry<Integer, Map<Integer, ViewTile>> column : map.getColumns().entrySet()){
				for(Entry<Integer, ViewTile> entry : column.getValue().entrySet() ){
					g.setColor(getColor(entry.getValue().getType()));
					g.fillRect(column.getKey()*unit - playerX*unit + halfMapSize, 
							   entry.getKey()*unit - playerY*unit + halfMapSize, 
							   unit, unit);
				}
			}
			
			//paints the player
			g.setColor(Color.CYAN);
			g.fillOval(playerX + halfMapSize + playerOffset, playerY + halfMapSize + playerOffset, playerSize, playerSize);
										
			//initialized the strings for the names
			String centerPlayers = "", northPlayers = "", eastPlayers = "", southPlayers = "", westPlayers = "";
				
			//drawing the enemies and their names
			g.setColor(Color.RED);
			for(Entry<String, Integer> test : nearbyPlayerMap.entrySet()){
				String name = test.getKey();
				int direction = test.getValue();
					
				switch(direction){
				case 0: centerPlayers = centerPlayers + name + "\n";
						g.fillOval(playerX + halfMapSize+enemyOffset, playerY + halfMapSize+enemyOffset, enemySize, enemySize);
						break;
				case 1: northPlayers = northPlayers + name + "\n";
						g.fillOval(playerX + halfMapSize+enemyOffset, playerY-unit + halfMapSize+enemyOffset, enemySize, enemySize);
						break;	
				case 2: eastPlayers = eastPlayers + name + "\n";
						g.fillOval(playerX+unit + halfMapSize+enemyOffset, playerY + halfMapSize+enemyOffset, enemySize, enemySize);
						break;
				case 3: southPlayers = southPlayers + name + "\n";
						g.fillOval(playerX+halfMapSize+enemyOffset, playerY+unit + halfMapSize+enemyOffset, enemySize, enemySize);
						break;
				case 4: westPlayers = westPlayers + name + "\n";
						g.fillOval(playerX-unit+halfMapSize+enemyOffset, playerY+halfMapSize+enemyOffset, enemySize, enemySize);
						break;
				}					
			}
			
			g.setColor(Color.black);
			g.drawString(centerPlayers, playerX + nameOffset + halfMapSize, playerY + nameOffset+halfMapSize);
			g.drawString(northPlayers, playerX + nameOffset+halfMapSize, playerY - unit + nameOffset+halfMapSize);
			g.drawString(eastPlayers, playerX+ unit + nameOffset+halfMapSize, playerY + nameOffset+halfMapSize);
			g.drawString(southPlayers, playerX + nameOffset+halfMapSize, playerY + unit + nameOffset+halfMapSize);
			g.drawString(westPlayers, playerX - unit + nameOffset+halfMapSize, playerY + nameOffset+halfMapSize);

		}
	}
	
	
	
}

	
	
	

