package com.zackrauen.hangman.gui;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import javafx.scene.media.AudioClip;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.zackrauen.hangman.backend.*;
import com.zackrauen.hangman.backend.events.*;
import com.zackrauen.hangman.backend.listeners.*;

public class HangmanGui extends JFrame implements HangmanListener, ActionListener, WindowListener, SerialPortEventListener,ThreadListener {


	private static final long serialVersionUID = 1L;

	private static HangmanGui mainframe = new HangmanGui("Hangman!");
		public Dimension initialDimension = new Dimension(800,400);
		public Dimension secondaryDimension = new Dimension(600,400);
		public static Dimension setupDimension = new Dimension(300,200);

	private Hangman game = new Hangman();

    private JSplitPane mainView = new JSplitPane();
    private Integer iconSize = 24;

	private JScrollPane gameScroll = new JScrollPane();
	private JLabel infoTitle = new JLabel("Hangman!",SwingConstants.CENTER);
	private JLabel alertMessage = new JLabel(" ",SwingConstants.CENTER);
	private JLabel winsMessage = new JLabel("Wins: ");
	private JLabel lossesMessage = new JLabel("Losses: ");

	private JMenuBar mainMenu = new JMenuBar();
	private JMenu gameMenu = new JMenu("Game");
	private JMenu helpMenu = new JMenu("Help");
	private JMenu setupMenu = new JMenu("Setup");

	private JMenu difficultyMenu = new JMenu("Difficulty");
	//ImageIcon foo = new ImageIcon();
	private URL easyDebug = this.getClass().getResource("resources/difficulties/easy.png");
	private Icon diffEasyImage = new ImageIcon(new ImageIcon(easyDebug).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH));
	private JMenuItem diffEasy = new JMenuItem("Easy",diffEasyImage);
	private Icon diffMedImage = new ImageIcon(new ImageIcon(this.getClass().getResource("resources/difficulties/med.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH));
	private JMenuItem diffMed = new JMenuItem("Medium",diffMedImage);
	private Icon diffHardImage = new ImageIcon(new ImageIcon(this.getClass().getResource("resources/difficulties/hard.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH));
	private JMenuItem diffHard = new JMenuItem("Hard",diffHardImage);
	private Icon diffInsaneImage = new ImageIcon(new ImageIcon(this.getClass().getResource("resources/difficulties/impossible.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH));
	private JMenuItem diffInsane = new JMenuItem("Insane",diffInsaneImage);

	private Icon diffUnknownImage = new ImageIcon(new ImageIcon(this.getClass().getResource("resources/difficulties/unknown.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH));


	private JMenuItem standaloneOption = new JMenuItem("Toggle Standalone");
	private JMenuItem debugSend = new JMenuItem("Force Clear LCD",new ImageIcon(new ImageIcon(this.getClass().getResource("resources/menuIcons/forceData.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH)));
	private String defaultConnect = "Connect a COMM...";
	private JMenuItem connectACOMM = new JMenuItem(defaultConnect,new ImageIcon(new ImageIcon(this.getClass().getResource("resources/menuIcons/connectCOM.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH)));
	private String defaultDisconnect = "Disconnect a COMM";
	private JMenuItem disconnectCOMM = new JMenuItem(defaultDisconnect,new ImageIcon(new ImageIcon(this.getClass().getResource("resources/menuIcons/disconnectCOM.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH)));
	private JMenuItem aboutProgram = new JMenuItem("About",new ImageIcon(new ImageIcon(this.getClass().getResource("resources/menuIcons/about.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH)));
	private JMenuItem howToUse = new JMenuItem("How To Use",new ImageIcon(new ImageIcon(this.getClass().getResource("resources/menuIcons/howToUse.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH)));
	private JMenuItem closeProgram = new JMenuItem("Exit Game",new ImageIcon(new ImageIcon(this.getClass().getResource("resources/menuIcons/exitProgram.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH)));
    private JMenuItem newWord = new JMenuItem("Change Word",new ImageIcon(new ImageIcon(this.getClass().getResource("resources/menuIcons/changeWord.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH)));
    private JMenuItem newFile = new JMenuItem("Open File...",new ImageIcon(new ImageIcon(this.getClass().getResource("resources/menuIcons/open.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH)));

    private Boolean standaloneGame = true;
    private JButton newGameButton = new JButton("New Game?");

    private HangmanPicture drawPanel = new HangmanPicture();
    private Integer numInRows = 7;
    private Integer standalonePictureOffsetx = 30;

    private SerialPort connectionPort = null;
    private PeripheralControl external;
    private Boolean threadInProgress = false;
    private String portName = null;
    private Boolean portConnected = false;
    private String easyGameTxt = "easyGame.txt";
    private String medGameTxt = "medGame.txt";
    private String hardGameTxt = "hardGame.txt";
    private String insaneGameTxt = "insaneGame.txt";

    private URL easyGameFile;
    private URL medGameFile;
    private URL hardGameFile;
    private URL insaneGameFile;

    private String winSound = "win.mp3";
    private AudioClip winMP3;
    private String lossSound = "loss.mp3";
    private AudioClip lossMP3;

    final JFileChooser fc = new JFileChooser();

	public HangmanGui(String title) {
		super(title);
		this.addWindowListener(this);
		this.setIconImage(new ImageIcon(this.getClass().getResource("resources/mainicon.png")).getImage());
        newWord.addActionListener(this);
        closeProgram.addActionListener(this);
        aboutProgram.addActionListener(this);
        howToUse.addActionListener(this);
        standaloneOption.addActionListener(this);
        connectACOMM.addActionListener(this);
        disconnectCOMM.addActionListener(this);
        newFile.addActionListener(this);
        debugSend.addActionListener(this);

        aboutProgram.setMnemonic(KeyEvent.VK_A);
        aboutProgram.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        helpMenu.add(aboutProgram);
        howToUse.setMnemonic(KeyEvent.VK_H);
        howToUse.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        helpMenu.add(howToUse);
        helpMenu.addSeparator();
        debugSend.setMnemonic(KeyEvent.VK_F);
        debugSend.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        helpMenu.add(debugSend);
        standaloneOption.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        standaloneOption.setMnemonic(KeyEvent.VK_T);
        setupMenu.add(standaloneOption);
        setupMenu.addSeparator();
        connectACOMM.setMnemonic(KeyEvent.VK_C);
        connectACOMM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        setupMenu.add(connectACOMM);
        disconnectCOMM.setMnemonic(KeyEvent.VK_D);
        disconnectCOMM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        setupMenu.add(disconnectCOMM);
        disconnectCOMM.setEnabled(false);
        newFile.setMnemonic(KeyEvent.VK_O);
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        gameMenu.add(newFile);
        newWord.setMnemonic(KeyEvent.VK_W);
        newWord.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        gameMenu.add(newWord);
        gameMenu.addSeparator();
        closeProgram.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_END,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()+InputEvent.ALT_DOWN_MASK, false));
        closeProgram.setMnemonic(KeyEvent.VK_X);



        diffEasy.setMnemonic(KeyEvent.VK_E);
        difficultyMenu.add(diffEasy);
        diffEasy.addActionListener(this);
        diffMed.setMnemonic(KeyEvent.VK_M);
        difficultyMenu.add(diffMed);
        diffMed.addActionListener(this);
        diffHard.setMnemonic(KeyEvent.VK_H);
        diffHard.addActionListener(this);
        difficultyMenu.add(diffHard);
        diffInsane.setMnemonic(KeyEvent.VK_F);
        diffInsane.addActionListener(this);
        difficultyMenu.add(diffInsane);
        difficultyMenu.setMnemonic(KeyEvent.VK_D);
        gameMenu.add(difficultyMenu);
        difficultyMenu.setIcon(new ImageIcon(new ImageIcon(this.getClass().getResource("resources/difficulties/easy.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH)));
        gameMenu.addSeparator();
        gameMenu.add(closeProgram);

		gameMenu.setMnemonic(KeyEvent.VK_G);
		mainMenu.add(gameMenu);
		setupMenu.setMnemonic(KeyEvent.VK_S);
		mainMenu.add(setupMenu);
		helpMenu.setMnemonic(KeyEvent.VK_H);
		mainMenu.add(helpMenu);
		this.setJMenuBar(mainMenu);

		fc.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));

			newGameButton.addActionListener(this);
		    URL temp = this.getClass().getResource("resources/"+easyGameTxt);
			easyGameFile = temp;
			medGameFile = this.getClass().getResource("resources/"+medGameTxt);
			hardGameFile = this.getClass().getResource("resources/"+hardGameTxt);
			insaneGameFile = this.getClass().getResource("resources/"+insaneGameTxt);

			winMP3 = new AudioClip(this.getClass().getResource("resources/"+winSound).toExternalForm());
			lossMP3 = new AudioClip(this.getClass().getResource("resources/"+lossSound).toExternalForm());
			game.addGameListener(this);
		    try {
				game.setupNewMatch(temp);
				game.setDefaultGame(temp);
			} catch (IOException e) {
				alertMessage.setForeground(Color.RED);
				alertMessage.setText("Sorry, somehow the game died...");
			}
	}

	private JPanel getLettersPanel(Boolean guessed) {
		JPanel alphaPanel = new JPanel();
        alphaPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		alphaPanel.setLayout(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		for (int i=0;i<game.getGameChars().size();i++) {
			if (game.getGameChars().get(i).isGuessable()) {
				if (i<13) {
					c2.gridy=0;
					c2.gridx=i;
				}
				else {
					c2.gridy=1;
					c2.gridx=i-13;
				}
				JLabel sensorLabel;
			if (game.getGameChars().get(i).hasBeenGuessed()==guessed)
				sensorLabel = new JLabel(game.getGameChars().get(i).getValue() + "  ");
			else
				sensorLabel = new JLabel("   ");
	    	sensorLabel.setFont(new Font(sensorLabel.getFont().getFontName(),Font.BOLD, sensorLabel.getFont().getSize()));
			alphaPanel.add(sensorLabel,c2);
			}
		}
		return alphaPanel;
	}

	private void getGamePanel() {
		JPanel gamePanel = new JPanel();
        gamePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		gamePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill=GridBagConstraints.HORIZONTAL;
		c.anchor=GridBagConstraints.PAGE_START;

		infoTitle.setForeground(Color.BLACK);
		infoTitle.setFont(new Font("Arial", Font.BOLD, 16));
		c.gridx=0;
		c.gridy=0;
		c.insets=new Insets(0,0,10,0);
		gamePanel.add(infoTitle,c);

		alertMessage.setFont(new Font("Arial", Font.ITALIC, 12));
		c.gridx=0;
		c.gridy++;
		c.insets=new Insets(0,0,10,0);
		gamePanel.add(alertMessage,c);

		winsMessage.setFont(new Font("Arial", Font.BOLD, 12));
		float[] hsbvals = new float[3];
		Color.RGBtoHSB(46, 110, 0, hsbvals);
		winsMessage.setForeground(Color.getHSBColor(hsbvals[0],hsbvals[1],hsbvals[2]));

		lossesMessage.setFont(new Font("Arial", Font.BOLD, 12));
		lossesMessage.setForeground(Color.RED);

		JPanel recordPanel = new JPanel();
        recordPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
		recordPanel.setLayout(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();

		winsMessage.setText("Wins: " + game.getNumOfWins());
		lossesMessage.setText("Losses: " + game.getNumOfLosses());

			c2.gridx=0;
			c2.gridy=0;
			c2.insets=new Insets(0,0,0,20);
			recordPanel.add(winsMessage,c2);
			c2.insets=new Insets(0,0,0,0);
			c2.gridx=1;
			recordPanel.add(lossesMessage,c2);

		c.insets=new Insets(0,0,0,0);
		c.gridx=0;
		c.gridy++;
		gamePanel.add(recordPanel,c);

		c.insets=new Insets(0,0,0,0);
		c.gridx=0;
		c.gridy++;
		gamePanel.add(new JLabel("Word:"),c);

		JPanel wordPanel = new JPanel();
        wordPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		wordPanel.setLayout(new GridBagLayout());
		GridBagConstraints ca = new GridBagConstraints();
		for (int i=0;i<game.getCurrentWord().size();i++) {
			ca.gridy=0;
			ca.gridx=i;
			JLabel sensorLabel;
			sensorLabel = new JLabel(game.getCurrentWord().get(i).displayValue(game.isGameOver()) + "  ");
	    	sensorLabel.setFont(new Font(sensorLabel.getFont().getFontName(),Font.BOLD, sensorLabel.getFont().getSize()));
			wordPanel.add(sensorLabel,ca);
		}

		c.gridx=0;
		c.gridy++;
		gamePanel.add(wordPanel,c);

		c.gridx=0;
		c.gridy++;
		gamePanel.add(new JLabel("Letters Available:"),c);

		if (!standaloneGame) {

			c.gridx=0;
			c.gridy++;
			gamePanel.add(getLettersPanel(false),c);

			c.gridx=0;
			c.gridy++;
			gamePanel.add(new JLabel("Letters Guessed:"),c);


			c.gridx=0;
			c.gridy++;
			gamePanel.add(getLettersPanel(true),c);
		}
		else {
			JPanel alphaPanel = new JPanel();
	        alphaPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
			alphaPanel.setLayout(new GridBagLayout());
			c2 = new GridBagConstraints();

			c2.gridx=0;
			c2.gridy=0;
			for (int i=0;i<game.getGameChars().size();i++) {
				if (game.getGameChars().get(i).isGuessable()) {

					JButton sensorLabel = new JButton();
					sensorLabel.setText(game.getGameChars().get(i).getValue() + "");
					sensorLabel.setPreferredSize(new Dimension(45,25));

					if (game.getGameChars().get(i).hasBeenGuessed())
						sensorLabel.setEnabled(false);

					sensorLabel.addActionListener(this);
					alphaPanel.add(sensorLabel,c2);
					if (c2.gridx<numInRows-1) {
						c2.gridx++;
					}
					else {
						c2.gridy++;
						if (c2.gridy<3)
							c2.gridx=0;
						else
							c2.gridx=1;
					}
				}
			}
			c.gridx=0;
			c.gridy++;
			gamePanel.add(alphaPanel,c);
		}

		if (game.isGameOver()) {
			c.gridx=0;
			c.gridy++;
			gamePanel.add(newGameButton,c);
		}


		gameScroll.setViewportView(gamePanel);
		gameScroll.setPreferredSize(new Dimension(235,215));
		gameScroll.setMinimumSize(new Dimension(235,150));
		gameScroll.getVerticalScrollBar().setUnitIncrement(25);
	}

	@Override
	public void gameEvent(HangmanEvent e) {

		float[] hsbvals = new float[3];
		Color.RGBtoHSB(46, 110, 0, hsbvals);

		if (e.getSource().equals(game)) {

		if (e.getEvent().equals(HangmanEvent.Event.CorrectGuess) || e.getEvent().equals(HangmanEvent.Event.WrongGuess)) {
			updateScreen();
			alertMessage.setText(" ");
		}

		if (e.getEvent().equals(HangmanEvent.Event.GameLost)) {
			lossMP3.play();
			updateScreen();
			alertMessage.setForeground(Color.RED);
			alertMessage.setText("Bring Add/Drop Form!");
		}

		if (e.getEvent().equals(HangmanEvent.Event.NewMatch) || e.getEvent().equals(HangmanEvent.Event.NewGame)) {
			updateScreen();

			if (!portConnected && e.getEvent().equals(HangmanEvent.Event.NewMatch)) {
				game.newGame();
			}
			alertMessage.setText(" ");
			updateScreen();
		}

		if (e.getEvent().equals(HangmanEvent.Event.GivenUp)) {
			updateScreen();
    		alertMessage.setForeground(Color.RED);
			alertMessage.setText("Giving up counts as a loss in my book.");
		}

		if (e.getEvent().equals(HangmanEvent.Event.GameWon)) {
			winMP3.play();
			updateScreen();
			alertMessage.setForeground(Color.getHSBColor(hsbvals[0],hsbvals[1],hsbvals[2]));
			alertMessage.setText("You WIN!");
		}

		if (e.getEvent().equals(HangmanEvent.Event.MatchWin)) {
			String congratsText = "Good job, you got " + game.getNumOfWins() + " out of " + (game.getNumOfWins()+game.getNumOfLosses());
			updateScreen();
			alertMessage.setForeground(Color.getHSBColor(hsbvals[0],hsbvals[1],hsbvals[2]));
			alertMessage.setText(congratsText);
		}
		else if (e.getEvent().equals(HangmanEvent.Event.MatchLoss)) {
			String lossMessage = "Nice try, you only got " + game.getNumOfWins() + " out of " + (game.getNumOfWins()+game.getNumOfLosses());
			updateScreen();
    		alertMessage.setForeground(Color.RED);
			alertMessage.setText(lossMessage);
		}
		else if (e.getEvent().equals(HangmanEvent.Event.MatchTie)) {
			String tieMessage = "A tie, not bad, you got " + game.getNumOfWins() + " out of " + (game.getNumOfWins()+game.getNumOfLosses());
			updateScreen();
			alertMessage.setForeground(Color.getHSBColor(hsbvals[0],hsbvals[1],hsbvals[2]));
			alertMessage.setText(tieMessage);
		}
		}
	}

	private void updateScreen() {

		if (standaloneGame) {
			if (!this.getSize().equals(initialDimension)) {
				this.setSize(initialDimension);
				this.setLocationRelativeTo(null);
			}
		}
		else {
			if (!this.getSize().equals(secondaryDimension)) {
				this.setSize(secondaryDimension);
				this.setLocationRelativeTo(null);
			}
		}
	       getGamePanel();
	       gameScroll.revalidate();
	       gameScroll.repaint();
	       updateHangman();

//	    try {
//			externalThread.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	private void updateHangman() {
    	drawPanel.step=game.getNumOfWrongGuesses();
    	if (standaloneGame)
    		drawPanel.offsetx=standalonePictureOffsetx;
    	else
    		drawPanel.offsetx=0;
    	drawPanel.repaint();
	}

	private void createMainScreen(Container pane) {

        drawPanel.setPreferredSize(new Dimension(300,400));

        gameScroll.setMinimumSize(new Dimension(275,400));

        mainView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gameScroll, drawPanel);

        mainView.setResizeWeight(0.7);
        mainView.setEnabled(false);
        pane.add(mainView);
	}

	public void threadAction(ThreadEvent t) {
		if (t.getSource().equals(external)) {
			if (t.getEvent()==ThreadEvent.Event.start)
				threadInProgress=true;
			else if (t.getEvent()==ThreadEvent.Event.stop)
				threadInProgress=false;
		}
	}


	@Override
	public void serialEvent(SerialPortEvent s) {
			if (s.getEventType() == SerialPortEvent.DATA_AVAILABLE&& !threadInProgress) {
				try {
					char input =(char) connectionPort.getInputStream().read();
					System.out.println(input + " " + (new Character(input).hashCode()));

						if (game.isGameOver()==false && input >=65 && input <=90) {
							try {
								game.guessLetter(input);
							} catch (Exception e) {
								alertMessage.setForeground(Color.RED);
								alertMessage.setText("You managed to guess an invalid character, congrats.");
							}
						}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					connectionPort.getInputStream().skip(connectionPort.getInputStream().available());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
	}

	private void connectPort(String portToUse) {
		try {
			connectionPort.close();
			connectionPort.removeEventListener();
			}
			catch (NullPointerException tu) {

			}
//	   if (!PortFunctions.isPortInUse(portToUse)) {
		  try {
			connectionPort = (SerialPort) CommPortIdentifier.getPortIdentifier(portToUse).open(portToUse,2000);
			connectionPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			portConnected=true;
			connectionPort.addEventListener(this);
			connectionPort.notifyOnDataAvailable(true);
			this.threadInProgress=true;
			external = new PeripheralControl(connectionPort, game);
			difficultyMenu.setIcon(diffEasyImage);
			external.addThreadListener(this);
			portName = portToUse;
			connectACOMM.setEnabled(false);
			connectACOMM.setText("Connected to " + portName);
			disconnectCOMM.setEnabled(true);
			disconnectCOMM.setText("Disconnect from " + portName);
		} catch (TooManyListenersException | PortInUseException | NoSuchPortException | UnsupportedCommOperationException e1) {
			System.out.println("no connect");
		}
		  standaloneGame=false;
		JOptionPane.showMessageDialog(this,"Successfully connected to " + portName,"Connected!",JOptionPane.QUESTION_MESSAGE);
//	   }
	   updateScreen();
	}

	private List<CommPortIdentifier> listSerialPorts() {
		List<CommPortIdentifier> serialList = new ArrayList<CommPortIdentifier>();
		 Enumeration<?> pList = CommPortIdentifier.getPortIdentifiers();
		    // Process the list.
		    while (pList.hasMoreElements()) {
		      CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
		      if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
		    	serialList.add(cpi);
		      }
		    }
		    return serialList;
	}

	public void showSetupPanel() {
		List<CommPortIdentifier> commList = listSerialPorts();
		String[] commArray = new String[commList.size()+1];

		for (int i=0;i<commList.size();i++) {
			commArray[i]=commList.get(i).getName();
		}
		commArray[commList.size()] = "None";
		String port = (String)JOptionPane.showInputDialog(this,"Choose a port to connect to:\n","Connect a COMM...",
				JOptionPane.PLAIN_MESSAGE,
				new ImageIcon(new ImageIcon(this.getClass().getResource("resources/menuIcons/connectCOM.png")).getImage().getScaledInstance(iconSize, iconSize,java.awt.Image.SCALE_SMOOTH)),
				commArray,commArray[0]);

		if ((port != null) && (port.length() > 0)) {
			if (!port.equals("None")) {
				System.out.println("to connect");
				connectPort(port);
			}
		}
		}

	@SuppressWarnings({ "deprecation" })
	public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(disconnectCOMM)) {
    		try {
    				external.interrupt();
	    			connectionPort.close();
	    			connectionPort.removeEventListener();
	    			JOptionPane.showMessageDialog(this,"Successfully disconnected from " + portName,"Disconnected!",JOptionPane.QUESTION_MESSAGE);
	    			disconnectCOMM.setEnabled(false);
	    			disconnectCOMM.setText(defaultDisconnect);
					connectACOMM.setEnabled(true);
					connectACOMM.setText(defaultConnect);
	    			portName = null;
	    			connectionPort = null;
	    			portConnected=false;
	    			standaloneGame=true;
	    			updateScreen();
    			}
    			catch (NullPointerException z) {
    				JOptionPane.showMessageDialog(this,"No port is currently connected!","No Port",JOptionPane.ERROR_MESSAGE);
    			}
        }
        if (e.getSource().equals(howToUse)) {
        	JOptionPane.showMessageDialog(this, "In order to use the software, you must decide if you want to use it as a standalone, or by \nusing an external PS/2 keyboard. "
        			+ "If you prefer the former, then the system is already set \nup and should be fine.\n\n"
        			+ "However, for the latter, connect your keyboard for the RS232 setup.\n"
        			+ "Next, choose \"Setup>Connect a COMM...\" Your keyboard should show up there.\n"
        			+ "Select the corresponding button and you should be informed when the system connects.\n"
        			+ "After that, you should be able to simply press your guess on the keyboard.","How To Use",JOptionPane.PLAIN_MESSAGE);
        }
        if (e.getSource().equals(aboutProgram)) {
        	JOptionPane.showMessageDialog(this, "This hangman game has the option of playing "
        			+ "as a standalone or connecting to an \nexternal PS/2 port via the RS232 standard. \n\n"
        			+ "This software was designed and developed by Zack Rauen "
        			+ "of www.ZackRauen.com","About The Software",JOptionPane.PLAIN_MESSAGE);
        }
        if (e.getSource().equals(standaloneOption)) {
        	standaloneGame=!standaloneGame;
        	updateScreen();
        }
        if (e.getSource().equals(closeProgram))
        	this.processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));

		if (!threadInProgress) {
			if (e.getActionCommand().length()==1) {
				try {
					game.guessLetter(e.getActionCommand().charAt(0));
				} catch (Exception e1) {
					alertMessage.setForeground(Color.RED);
					alertMessage.setText("You managed to guess an invalid character, congrats.");
				}
			}
	        if (e.getSource().equals(newWord) || e.getSource().equals(newGameButton)) {
	        	if (e.getSource().equals(newWord) && !game.isGameOver()) {
	        		System.out.println("Good");
	        		game.giveUp();
	        	}
	        	else {
	        		System.out.println("Good");
	        		game.newGame();
	        	}
	        }
	        if (e.getSource().equals(connectACOMM)) {
	        	showSetupPanel();
	        }
	        if (e.getSource().equals(newFile)) {
	        	SwingUtilities.updateComponentTreeUI(fc);
	            int returnVal = fc.showOpenDialog(this);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	               // File file = fc.getSelectedFile();
	                try {
						game.setupNewMatch(fc.getSelectedFile().toURL());
						difficultyMenu.setIcon(diffUnknownImage);
					} catch (IOException e1) {
						alertMessage.setForeground(Color.RED);
						alertMessage.setText("Sorry, we couldnt read in your file.");
					}
	            }
	        }
	        if (e.getSource().equals(debugSend)) {
	        	try {
					external.initializeLCD();
	        		//external.clearDisplay();
					//external.longMessageTest();
	        		//external.displayWord(game.getWordInPlay(), false);
	        		//external.sendLetterToSerialComm('S');
					JOptionPane.showMessageDialog(this,"Successfully cleared the LCD on " + portName,"Sent!",JOptionPane.QUESTION_MESSAGE);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(this,"No port is currently connected!","No Port",JOptionPane.ERROR_MESSAGE);
				}
	        }
	        if (e.getSource().equals(diffEasy)) {
	        	try {
					game.setupNewMatch(easyGameFile);
				} catch (IOException e1) {
					alertMessage.setForeground(Color.RED);
					alertMessage.setText("Sorry, somehow this mode broke...");
				}
	        	difficultyMenu.setIcon(diffEasyImage);
	        }
	        if (e.getSource().equals(diffMed)) {
	        	try {
					game.setupNewMatch(medGameFile);
				} catch (IOException e1) {
					alertMessage.setForeground(Color.RED);
					alertMessage.setText("Sorry, somehow this mode broke...");
				}
	        	difficultyMenu.setIcon(diffMedImage);
	        }
	        if (e.getSource().equals(diffHard)) {
	        	try {
					game.setupNewMatch(hardGameFile);
				} catch (IOException e1) {
					alertMessage.setForeground(Color.RED);
					alertMessage.setText("Sorry, somehow this mode broke...");
				}
	        	difficultyMenu.setIcon(diffHardImage);
	        }
	        if (e.getSource().equals(diffInsane)) {
	        	try {
					game.setupNewMatch(insaneGameFile);
				} catch (IOException e1) {
					alertMessage.setForeground(Color.RED);
					alertMessage.setText("Sorry, somehow this mode broke...");
				}
	        	difficultyMenu.setIcon(diffInsaneImage);
	        }
		}
		else {
			if (e.getActionCommand().length()==1 || e.getSource().equals(newWord) || e.getSource().equals(newGameButton)
					|| e.getSource().equals(newFile) || e.getSource().equals(debugSend) || e.getSource().equals(diffEasy)
					|| e.getSource().equals(diffMed) || e.getSource().equals(diffHard) || e.getSource().equals(diffInsane)) {
				JOptionPane.showMessageDialog(this,"Please check the LCD and respond to any prompts.","LCD Running",JOptionPane.ERROR_MESSAGE);
//				int n = JOptionPane.showConfirmDialog(this,"Would you like to interrupt the LCD and do this?","An Inane Question",JOptionPane.YES_NO_OPTION);
//				if (n==JOptionPane.YES_OPTION) {
//					external.interrupt();
//					external.resetInterrupt();
//					this.actionPerformed(new ActionEvent(e.getSource(),e.getID(),e.getActionCommand()));
//
//				}
	        }
		}
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		try {
			external.interrupt();
			connectionPort.close();
			connectionPort.removeEventListener();
		}
		catch (NullPointerException e) {

		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 */

	private static void createAndShowGUI() {
		mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainframe.setSize(mainframe.initialDimension);
		mainframe.setResizable(false);
		mainframe.createMainScreen(mainframe.getContentPane());
		SwingUtilities.updateComponentTreeUI(mainframe);
		mainframe.setLocationRelativeTo(null);
		mainframe.setVisible(true);
	}

	public static void main(String[] args) {
		/* Use an appropriate Look and Feel */
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                        //UIManager.setLookAndFeel("javax.swing.plaf.metal");
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}


	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}
	@Override
	public void windowOpened(WindowEvent arg0) {}
	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}

}