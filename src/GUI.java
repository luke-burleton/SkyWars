import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.io.InputStream;

public class GUI extends JFrame implements Observer {
	private JFrame frame;
	// 2D array of panels to represent the sky
	private JPanel[][] panels;
	private JPanel topBar;
	private JLabel scoreLabel;
	// define image paths
	private String playerShipImgPath = "images\\Ship_1.png";
	private String enemyShipImgPath = "images\\Ship_2.png";
	private String pauseMenuImgPath = "images\\space.jpg";
	// define title/menu font path
	private String titleFontPath = "fonts\\upheavtt.TTF";
	private String menuFontPath = "fonts\\upheavtt.TTF";
	// define title/menu font size (must be a float)
	private float titleFontSize = 72;
	private float menuFontSize = 24;
	// initialise the title/menu font
	private Font titleFont = null;
	private Font menuFont = null;

	// constructor
	public GUI(Game game) {
		// call the initialiseGUI method
		initialiseGUI(game);
	}

	// method to initialise the GUI
	public void initialiseGUI(Game game) {
	    // checks if the game is already running, if it is close any previous frames
	    if(this.frame != null) {
	        frame.dispose();
	    }
	    // set the fonts
	    this.titleFont = loadFont(titleFontPath);
	    this.menuFont = loadFont(menuFontPath);
	    
	    // get the sky from the game object
	    Sky sky = game.getSky();

	    // make the SkyGUI observe the sky and game object
	    sky.addObserver(this);
	    game.addObserver(this);

	    this.frame = new JFrame("Sky Wars");
	    
	    // create a top bar to display the score etc.
	    this.topBar = new JPanel();
	    this.topBar.setBackground(Color.WHITE);
	    this.topBar.setBorder(null);
	    this.frame.add(this.topBar, BorderLayout.NORTH);
	    
	    // create a score label to display the current score
	    scoreLabel = new JLabel("Score: " + game.getScore());
	    scoreLabel.setForeground(Color.BLACK); // set the text color to white
	    scoreLabel.setFont(menuFont.deriveFont(Font.BOLD, menuFontSize));
	    this.topBar.add(scoreLabel, BorderLayout.EAST); // add the label to the left side of the panel

	    this.panels = new JPanel[sky.getSKY_WIDTH()][sky.getSKY_HEIGHT()];
	    
	    // grid layout to organise the panels
	    JPanel gridLayout = new JPanel(new GridLayout(sky.getSKY_WIDTH(), sky.getSKY_HEIGHT()));
	    gridLayout.setBackground(Color.BLACK);
	    
	    // scan the whole sky and place ship icons where there are ships
	    for (int i = 0; i < sky.getSKY_WIDTH(); i++) {
	        for (int j = 0; j < sky.getSKY_HEIGHT(); j++) {
	            Square square = sky.getSquares()[i][j];
	            JPanel p = new JPanel(new BorderLayout());
	            p.setOpaque(false);
	            
	            // if the square has a ship (not empty)
	            if (square.isEmpty() == false) {
	            	// if the square has a player ship, place the player ship image
	                if (square.getShip().isPlayer() == true) {
	                    ImageIcon icon = new ImageIcon(playerShipImgPath);
	                    JLabel label = new JLabel(icon);
	                    p.add(label);
	                }
	                // if the square has an enemy ship, place the enemy ship image 
	                else {
	                    ImageIcon icon = new ImageIcon(enemyShipImgPath);
	                    JLabel label = new JLabel(icon);
	                    p.add(label);
	                }
	            }
	            
	            panels[i][j] = p;
	            gridLayout.add(p);
	        }
	    }
	    frame.add(gridLayout);
	    frame.addKeyListener(game);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setResizable(false);
	    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	    frame.setVisible(true);
	}


	// method to update the appearance of the gui depending on the status of the sky
	public void update(Observable o, Object arg) {
		// if a square is recieved, assumes a ship has moved
		if (arg instanceof Square) {
			Square square = (Square) arg;
			int x = square.getCoordinate().getX();
			int y = square.getCoordinate().getY();
			JPanel squarePanel = panels[x][y];
			squarePanel.removeAll(); // remove any previous components
			if (!square.isEmpty()) {
				if (square.getShip().isPlayer() == true) {
					ImageIcon icon = new ImageIcon(playerShipImgPath);
					JLabel label = new JLabel(icon);
					squarePanel.add(label);
				} else {
					ImageIcon icon = new ImageIcon(enemyShipImgPath);
					JLabel label = new JLabel(icon);
					squarePanel.add(label);
				}
			}
			squarePanel.revalidate(); // revalidate the panel to update the display
			squarePanel.repaint(); // repaint the panel to ensure that the changes are displayed immediately
		}
		// if a GameState enum is recieved, handle the UI gamestate
		if(arg instanceof GameState) {
			// if the GameState is of type PAUSED or RUNNING - toggle the pause menu
			if(arg == GameState.PAUSED || arg == GameState.RUNNING) {
			togglePauseMenu((GameState) arg);
			}
			// if the GameState is of type GAME_OVER, show game over screen
			if(arg == GameState.GAME_OVER) {
				displayGameOver();
				}
			// if the GameState is of type START, show the start menu
			if(arg == GameState.START) {
				toggleStartMenu((GameState)arg);
			}
		}
		// if a game object is recieved assumes it is loading the game from a save
		if(arg instanceof Game) {
			initialiseGUI((Game)arg);
		}
		// if an integer is recieved, assume it is updating the score
		if(arg instanceof Integer) {
			int score = (Integer) arg;
		    scoreLabel.setText("Score: " + score);
		}
	}

	// method that when called, displays an image over the paused game to show the pause menu
	public void togglePauseMenu(GameState gameState) {
	    JLayeredPane layeredPane = frame.getLayeredPane();
	    // if start is true, display the pause menu image in a JLayeredPane
	    if (gameState == GameState.PAUSED) {
	        // paused panel
	        JPanel pausePanel = new JPanel();
	        pausePanel.setBackground(Color.BLACK);
	        pausePanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
	        pausePanel.setBounds(frame.getWidth() / 4, frame.getHeight() / 4, frame.getWidth() / 2, frame.getHeight() / 2);
	        pausePanel.setLayout(new BoxLayout(pausePanel, BoxLayout.Y_AXIS));
	        pausePanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

	        // paused label
	        JLabel pauseLabel = new JLabel("PAUSED");
	        pauseLabel.setForeground(Color.WHITE);
	        pauseLabel.setFont(titleFont.deriveFont(Font.BOLD, titleFontSize));
	        pauseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	        pausePanel.add(pauseLabel);

	        // options label
	        JLabel optionsLabel = new JLabel("P = pause/resume | S = save | L = load | Q = quit");
	        optionsLabel.setForeground(Color.WHITE);
	        optionsLabel.setFont(menuFont.deriveFont(Font.BOLD, menuFontSize));
	        optionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	        pausePanel.add(optionsLabel);

	        // add the panel to the layered pane
	        layeredPane.add(pausePanel, JLayeredPane.PALETTE_LAYER);
	    } 
	    // if paused is false, remove the pause menu
	    else {
	        Component[] components = layeredPane.getComponentsInLayer(JLayeredPane.PALETTE_LAYER);
	        for (Component component : components) {
	            layeredPane.remove(component);
	        }
	        // restore the GUI
	        frame.revalidate();
	        frame.repaint();
	    }
	}
	// method to toggle the display of the start menu
	public void toggleStartMenu(GameState gameState) {
	    JLayeredPane layeredPane = frame.getLayeredPane();
	    // if start is true, display the pause menu image in a JLayeredPane
	    if (gameState == GameState.START) {
	        // set the background image of the start menu
	    	ImageIcon startImage = new ImageIcon(pauseMenuImgPath);
	        JLabel startLabel = new JLabel(startImage);
	        startLabel.setBounds(0, 0, frame.getWidth(), frame.getHeight());

	        // add labels with menu options below the 'Sky Wars' title
	        //title
	        JLabel titleLabel = new JLabel("Sky Wars");
	        titleLabel.setForeground(Color.WHITE);
	        titleLabel.setFont(titleFont.deriveFont(Font.BOLD, titleFontSize));
	        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
	        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
	        titleLabel.setBounds(0, 50, frame.getWidth(), 50);
	        startLabel.add(titleLabel);
	        
	        // menu
	        JLabel menulabel = new JLabel("N = new game | L = load | Q = quit");
	        menulabel.setForeground(Color.WHITE);
	        menulabel.setFont(menuFont.deriveFont(Font.BOLD, menuFontSize));
	        menulabel.setHorizontalAlignment(SwingConstants.CENTER);
	        menulabel.setVerticalAlignment(SwingConstants.CENTER);
	        menulabel.setBounds(0, 120, frame.getWidth(), 150);
	        startLabel.add(menulabel);
	        
	        // game info 
	        JLabel infolabel = new JLabel("Welcome to Sky Wars. Use the arrow keys to move and p to pause.");
	        infolabel.setForeground(Color.WHITE);
	        infolabel.setFont(menuFont.deriveFont(Font.BOLD, menuFontSize));
	        infolabel.setHorizontalAlignment(SwingConstants.CENTER);
	        infolabel.setVerticalAlignment(SwingConstants.CENTER);
	        infolabel.setBounds(0, 190, frame.getWidth(), 250);
	        startLabel.add(infolabel);
	        
	        // rules
	        JLabel ruleslabel = new JLabel("If an enemy is protected by another it is game over. Enemy ships spawn in the top right, take care near their warp.");
	        ruleslabel.setForeground(Color.WHITE);
	        ruleslabel.setFont(menuFont.deriveFont(Font.BOLD, menuFontSize));
	        ruleslabel.setHorizontalAlignment(SwingConstants.CENTER);
	        ruleslabel.setVerticalAlignment(SwingConstants.CENTER);
	        ruleslabel.setBounds(0, 260, frame.getWidth(), 200);
	        startLabel.add(ruleslabel);

	        layeredPane.add(startLabel, JLayeredPane.PALETTE_LAYER);
	    } 
	    // if if start is false, remove the start menu
	    else {
	        Component[] components = layeredPane.getComponentsInLayer(JLayeredPane.PALETTE_LAYER);
	        for (Component component : components) {
	            if (component instanceof JLabel) {
	                layeredPane.remove(component);
	            }
	        }
	        // restore the GUI
	        frame.revalidate();
	        frame.repaint();
	    }
	}
	// method to display game over message
	public void displayGameOver() {
	    // game over panel
	    JPanel gameOverPanel = new JPanel();
	    gameOverPanel.setBackground(Color.BLACK);
	    gameOverPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
	    gameOverPanel.setBounds(frame.getWidth() / 4, frame.getHeight() / 4, frame.getWidth() / 2, frame.getHeight() / 2);
	    gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.Y_AXIS));
	    gameOverPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

	    // game over label
	    JLabel gameOverLabel = new JLabel("Game Over!");
	    gameOverLabel.setForeground(Color.WHITE);
	    gameOverLabel.setFont(titleFont.deriveFont(Font.BOLD, titleFontSize));
	    gameOverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	    gameOverPanel.add(gameOverLabel);

	    // options label
	    JLabel optionsLabel = new JLabel("N = new game | L = load | Q = quit");
	    optionsLabel.setForeground(Color.WHITE);
	    optionsLabel.setFont(menuFont.deriveFont(Font.BOLD, menuFontSize));
	    optionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	    gameOverPanel.add(optionsLabel);

	    // add the panel to the layered pane
	    JLayeredPane layeredPane = frame.getLayeredPane();
	    layeredPane.add(gameOverPanel, JLayeredPane.PALETTE_LAYER);
	}
	// method to load in a custom font
	public Font loadFont(String fontPath) {
	    Font font = null;
	    try {
	        font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath));
	    } catch (IOException e) {
	        System.err.println("Error loading font file: " + e.getMessage());
	    } catch (FontFormatException e) {
	        System.err.println("Font format is not supported: " + e.getMessage());
	    }
	    return font;
	}
	
	// getters and setters
	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public JPanel[][] getPanels() {
		return panels;
	}

	public void setPanels(JPanel[][] panels) {
		this.panels = panels;
	}

	public JPanel getTopBar() {
		return topBar;
	}

	public void setTopBar(JPanel topBar) {
		this.topBar = topBar;
	}

	public JLabel getScoreLabel() {
		return scoreLabel;
	}

	public void setScoreLabel(JLabel scoreLabel) {
		this.scoreLabel = scoreLabel;
	}

	public String getPlayerShipImgPath() {
		return playerShipImgPath;
	}

	public void setPlayerShipImgPath(String playerShipImgPath) {
		this.playerShipImgPath = playerShipImgPath;
	}

	public String getEnemyShipImgPath() {
		return enemyShipImgPath;
	}

	public void setEnemyShipImgPath(String enemyShipImgPath) {
		this.enemyShipImgPath = enemyShipImgPath;
	}

	public String getPauseMenuImgPath() {
		return pauseMenuImgPath;
	}

	public void setPauseMenuImgPath(String pauseMenuImgPath) {
		this.pauseMenuImgPath = pauseMenuImgPath;
	}

	public String getTitleFontPath() {
		return titleFontPath;
	}

	public void setTitleFontPath(String titleFontPath) {
		this.titleFontPath = titleFontPath;
	}

	public String getMenuFontPath() {
		return menuFontPath;
	}

	public void setMenuFontPath(String menuFontPath) {
		this.menuFontPath = menuFontPath;
	}

	public float getTitleFontSize() {
		return titleFontSize;
	}

	public void setTitleFontSize(float titleFontSize) {
		this.titleFontSize = titleFontSize;
	}

	public float getMenuFontSize() {
		return menuFontSize;
	}

	public void setMenuFontSize(float menuFontSize) {
		this.menuFontSize = menuFontSize;
	}

	public Font getTitleFont() {
		return titleFont;
	}

	public void setTitleFont(Font titleFont) {
		this.titleFont = titleFont;
	}

	public Font getMenuFont() {
		return menuFont;
	}

	public void setMenuFont(Font menuFont) {
		this.menuFont = menuFont;
	}
}
