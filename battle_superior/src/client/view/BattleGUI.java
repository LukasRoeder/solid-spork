package client.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import server.TargetData;

public class BattleGUI extends JFrame  {

	private static final long serialVersionUID = 302317438177242328L;

	JPanel mainPanel = new JPanel();
	
	JPanel notifierPanel = new JPanel();
	JPanel textPanel = new JPanel();
	JPanel targetPanel = new JPanel();
	
	//things for the notifierPanel
	JLabel notifierLabel = new JLabel();
	
	//things for the textPanel
	JTextField inputField = new JTextField();
	
	public BattleGUI(ActionListener listener){
		
		super("You have been challenged to a battle!");
		
		notifierLabel.setText(" type in the letter next to your target into the Text field to attack him!");
		notifierLabel.setFont(new Font(notifierLabel.getFont().getName(), Font.PLAIN, 22));
		
		inputField.addActionListener(listener);
		inputField.setFont(new Font(inputField.getFont().getName(), Font.PLAIN, 22));
		inputField.setPreferredSize(new Dimension(300,50));

		notifierPanel.add(notifierLabel);
		
		targetPanel.setLayout(new GridLayout(0, 1));
		
		textPanel.add(inputField);
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		mainPanel.add(notifierPanel);
		mainPanel.add(targetPanel);
		mainPanel.add(textPanel);		
		
		add(mainPanel);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationByPlatform(true);
		setVisible(true);
	}
	
	public void lastWords(){
		notifierPanel.removeAll();
		notifierPanel.setBackground(Color.red);
		notifierLabel.setText("YOU DIED!");
		notifierLabel.setFont(new Font(notifierLabel.getFont().getName(), Font.PLAIN, 50));
		notifierPanel.add(notifierLabel);
		pack();
	}

	public void updateTargets(List<TargetData> tmpData) {
		targetPanel.removeAll();
		for (TargetData target : tmpData){
//			System.out.println(target.toString());
			JLabel tmpLabel = new JLabel(target.toString());
			tmpLabel.setFont(new Font(tmpLabel.getFont().getName(), Font.PLAIN, 22));
			targetPanel.add(tmpLabel);
		}
//		targetPanel.setLayout(new GridLayout(0, 1));
		mainPanel.add(targetPanel);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		pack();
	}

	public JTextField getInputField() {
		return inputField;
	}

	public void clearText() {
		inputField.setText("");		
	}
	
}
