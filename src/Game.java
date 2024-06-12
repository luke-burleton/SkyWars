import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Observable;

import javax.swing.Timer;

public class Game extends Observable implements ActionListener, KeyListener, Serializable {

	private Sky sky;
	// GUI connected to logic by one association
	private GUI gui;
	private PlayerSpaceship playerShip;
	private int moveCount;
	private int score;
	private final int ENEMY_MOVE_TIME = 1000;
	private final int ENEMY_SPAWN_TIME = 1000;
	private final int ENEMY_SPAWN_X = 0;
	private final int ENEMY_SPAWN_Y = 0;
	private final int PLAYER_SPAWN_X = 4;
	private final int PLAYER_SPAWN_Y = 4;
	// enum to store the game state
	private GameState gameState;
	private Coordinate enemySpawnPoint;
	private Timer enemySpawnTimer;
	private Timer enemyMoveTimer;
	// make the sound transient to avoid serialisation errors
	private transient Sound soundtrack;
	private final String soundtrackFilePath = "sounds\\soundtrack.wav";
	private final String scoreUpSoundFilePath = "sounds\\ding.wav";
	private final String gameOverSoundFilePath = "sounds\\gameover.wav";

	// empty constructor
	public Game() {
	}
	
	// method which starts a new game - uses the resetGame method which resets the game to its default attributes
	// if the game was already running (e.g. new game from game over screen) - skips showing the start menu
	public void startNewGame() {
	
		// reset game 
		resetGame(this);
		
		// create a sky object
		Sky sky = new Sky();
		
		// play the soundtrack
		playSoundtrack();

		// create a starting coordinate for the player ship
		Coordinate startingCoordinate = new Coordinate(PLAYER_SPAWN_X, PLAYER_SPAWN_Y);

		// create the player ship and place it at the starting coordinate in the sky
		PlayerSpaceship playerShip = new PlayerSpaceship(startingCoordinate);
		sky.placeShip(playerShip, startingCoordinate);

		// make the sky object accessible to methods
		setSky(sky);

		// make the playerShip object accessible to methods
		setPlayerShip(playerShip);

		// if the game was already running create a GUI object and pass the Game object to its constructor
		if (gui == null) {
		gui = new GUI(this);
		}
		// otherwise restart the GUI
		else {
			gui.initialiseGUI(this);
		}
		// notify GUI whether to display start menu OR if the game was already running, just starts a new game
		setChanged();
		notifyObservers(getGameState());
		
		// create an enemy ship and place it in the top left corner
		spawnEnemyShip();
	}

	public void resetGame(Game game) {
	    setMoveCount(0);
	    setScore(0);
	    setEnemySpawnPoint(new Coordinate(ENEMY_SPAWN_X, ENEMY_SPAWN_Y));
	    setEnemySpawnTimer(new Timer(ENEMY_SPAWN_TIME, this)); // spawn enemy every 1 seconds
	    setEnemyMoveTimer(new Timer(ENEMY_MOVE_TIME, this)); // move enemy ships every 1 second
	    getEnemySpawnTimer().start();
	    getEnemyMoveTimer().start();

	    // if the game is being opened for the first time, will display the start menu
	    if (getGameState() == null) {
	        setGameState(GameState.START);
	    } else {
	        setGameState(GameState.RUNNING);
	    }
	}

	// spawn a new enemy ship
	public void spawnEnemyShip() {
		// create an enemy ship and place it in the top left corner
		EnemySpaceship enemyShip = new EnemySpaceship(getEnemySpawnPoint());
		sky.placeShip(enemyShip, getEnemySpawnPoint());
	}

	// move all enemy ships randomly
	public void moveEnemyShips() {
		// get each enemy ship in the sky and move each a random direction
		for (Spaceship spaceship : this.sky.getEnemyShips()) {
			EnemySpaceship enemyShip = (EnemySpaceship) spaceship;

			// generate a random direction for the enemy ship to move
			int randomDirection = (int) (Math.random() * 4); // generates a random number from 0 to 3

			// move the enemy ship in the random direction
			switch (randomDirection) {
			case 0: // move up
				sky.moveShip("up", enemyShip);
				break;
			case 1: // move down
				sky.moveShip("down", enemyShip);
				break;
			case 2: // move left
				sky.moveShip("left", enemyShip);
				break;
			case 3: // move right
				sky.moveShip("right", enemyShip);
				break;
			default:
				break;
			}
		}
	}
	// move the player ship, and check if it is destroyed, if so set the game state to GAME_OVER
	public void movePlayerShip(String direction) {
	    sky.moveShip(direction, playerShip);
	    // check if that move just took an enemy ship, if so increase the score by 1
	    if(sky.isPlayerTookEnemy() == true) {
	    	increaseScore();
	    	playScoreUpSound();
	    }
	    checkGameOver();
	}
	private void increaseScore() {
		this.score++;
		setChanged();
		notifyObservers(getScore());
	}
	// handle game overs
	public void checkGameOver() {
	    if(sky.hasPlayerShips()==false) {
	        this.gameState = GameState.GAME_OVER;
	        stopSoundtrack();
	        playGameOverSound();
	        setChanged();
	        notifyObservers(getGameState());
	    }
	}

	// handle timer events
	@Override
	public void actionPerformed(ActionEvent e) {
		// only spawn new enemy ships or move existing enemy ships if the game is running
		if (gameState == GameState.RUNNING) {
			// every time an interval is sent from the enemy spawn timer, spawn an enemy ship
			if (e.getSource() == enemySpawnTimer) {
				spawnEnemyShip();
			}
			// every time an interval is sent from the enemy move timer, move an enemy ship
			if (e.getSource() == enemyMoveTimer) {
				moveEnemyShips();
			}
			checkGameOver();
		}
	}

	// handle all key events - includes ship movement when unpaused pause menu buttons when paused, and game over menu buttons when game is over
	// key pressed
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		// if the game state is START, allow start menu buttons
		if(gameState == GameState.START) {
			switch( keyCode ) { 
			// start new game with n
	        case KeyEvent.VK_N:
	            resume();
	            return;
	        // load a game with l
	        case KeyEvent.VK_L:
				stopSoundtrack();
				System.out.print("Game Loaded!\n");
				loadGame("savedata.sav");
				// resume the game 
				resume();
				setChanged();
				notifyObservers(this);
				return;
			// quit the game with q
	        case KeyEvent.VK_Q:
				System.exit(0);
				return;
	        }
		}
		// if the game is running, allow ship movement
		if(gameState == GameState.RUNNING) {
	        switch( keyCode ) {
	        // move player ship depending on arrow key pressed
	        case KeyEvent.VK_UP:
	            movePlayerShip("up");
	            return;
	        case KeyEvent.VK_DOWN:
	            movePlayerShip("down");
	            return;
	        case KeyEvent.VK_LEFT:
	            movePlayerShip("left");
	            return;
	        case KeyEvent.VK_RIGHT :
	            movePlayerShip("right");
	            return;
	        // pause the game if p is pressed
	        case KeyEvent.VK_P:
	            pause(); 
	            return;
	        // toggle between normal and master mode with z
	        case KeyEvent.VK_Z:
	        	toggleMasterMode();
	            return;
	        }
	    }
		// if paused only allow pause toggle and pause menu buttons
		if(gameState == GameState.PAUSED) {
			switch( keyCode ) { 
			case KeyEvent.VK_P: 
				resume();
				break;
			// save the game with s
			case KeyEvent.VK_S:
				System.out.print("Game Saved!\n");
				saveGame("savedata.sav");
				break;
			// load the game with l	
			case KeyEvent.VK_L:
				stopSoundtrack();
				System.out.print("Game Loaded!\n");
				loadGame("savedata.sav");
				// resume the game 
				resume();
				setChanged();
				notifyObservers(this);
				return;
			// close the game with q
			case KeyEvent.VK_Q:
				System.exit(0);
				return;
			}
		}
		// if gameover, only allow game over menu buttons (new game/load game)
		if(gameState == GameState.GAME_OVER) {
			switch( keyCode ) { 
			// start a new game with n
			case KeyEvent.VK_N: 
				// newgame method
				startNewGame();
				break;
			// load a game with l
			case KeyEvent.VK_L:
				System.out.print("Game Loaded!\n");
				loadGame("savedata.sav");
				// resume the game 
				resume();
				setChanged();
				notifyObservers(this);
				return;
			// close the game with q
			case KeyEvent.VK_Q:
				System.exit(0);
				return;
			}
		}
	}
	// key typed - not used but required
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
	// key released - not used but required
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}
	
	// toggle the player ship between master mode and normal mode (secret cheat)
	public void toggleMasterMode() {
		if (playerShip.getMode() == "normal") {
			playerShip.setMode("master");
		}
		else {
			playerShip.setMode("normal");
		}
	}

	// toggle between paused and unpaused
	public void pause() {
		this.gameState = GameState.PAUSED;
		setChanged();
		notifyObservers(getGameState());
	}
	public void resume() {
		this.gameState = GameState.RUNNING;
		setChanged();
		notifyObservers(getGameState());
	}
	// save the game state
	public void saveGame(String fileName) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
			out.writeObject(this);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// load the game state
	public void loadGame(String filename) {
	    try {
	        FileInputStream fileIn = new FileInputStream(filename);
	        ObjectInputStream in = new ObjectInputStream(fileIn);
	        Game savedGame = (Game) in.readObject();
	        in.close();
	        fileIn.close();
	        // update this game object with the saved game data
	        setSky(savedGame.getSky());
	        setPlayerShip(savedGame.getPlayerShip());
	        setScore(savedGame.getScore());
	        setGameState(savedGame.getGameState());
	        setMoveCount(savedGame.getMoveCount());
	        // set the mode to normal
	        playerShip.setMode("normal");
	        // restart the soundtrack
	        playSoundtrack();
	    } catch (IOException i) {
	        i.printStackTrace();
	    } catch (ClassNotFoundException c) {
	        c.printStackTrace();
	    }
	}

	// method to play the soundtrack
	public void playSoundtrack() {
		// create a new Sound object for the soundtrack
		Sound soundtrack = new Sound(soundtrackFilePath);
		setSoundtrack(soundtrack);
				
		// play and loop the soundtrack
		this.soundtrack.playSound();
		this.soundtrack.loopSound();
	}
	// method to stop the soundtrack
	public void stopSoundtrack() {
		this.soundtrack.stopSound();
	}
	// method to play the score up soundeffect
	public void playScoreUpSound() {
		// create new sound object
		Sound scoreUpSound = new Sound(scoreUpSoundFilePath);
		scoreUpSound.playSound();
		scoreUpSound.playOnce();
	}
	// method to play the game over soundeffect
	public void playGameOverSound() {
		// create new sound object
		Sound gameOverSound = new Sound(gameOverSoundFilePath);
		gameOverSound.playSound();
		gameOverSound.playOnce();
	}

	// getters and setters
	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public Sky getSky() {
		return sky;
	}

	public void setSky(Sky sky) {
		this.sky = sky;
	}

	public PlayerSpaceship getPlayerShip() {
		return playerShip;
	}

	public void setPlayerShip(PlayerSpaceship playerShip) {
		this.playerShip = playerShip;
	}

	public int getMoveCount() {
		return moveCount;
	}

	public void setMoveCount(int moveCount) {
		this.moveCount = moveCount;
	}

	public Coordinate getEnemySpawnPoint() {
		return enemySpawnPoint;
	}

	public void setEnemySpawnPoint(Coordinate enemySpawnPoint) {
		this.enemySpawnPoint = enemySpawnPoint;
	}

	public Timer getEnemySpawnTimer() {
		return enemySpawnTimer;
	}

	public void setEnemySpawnTimer(Timer enemySpawnTimer) {
		this.enemySpawnTimer = enemySpawnTimer;
	}

	public Timer getEnemyMoveTimer() {
		return enemyMoveTimer;
	}

	public void setEnemyMoveTimer(Timer enemyMoveTimer) {
		this.enemyMoveTimer = enemyMoveTimer;
	}
	public void setSoundtrack(Sound soundtrack) {
		this.soundtrack = soundtrack;
	}
	public GameState getGameState() {
		return gameState;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	public Sound getSoundtrack() {
		return soundtrack;
	}
	public GUI getGui() {
		return gui;
	}

	public void setGui(GUI gui) {
		this.gui = gui;
	}

	public int getENEMY_MOVE_TIME() {
		return ENEMY_MOVE_TIME;
	}

	public int getENEMY_SPAWN_TIME() {
		return ENEMY_SPAWN_TIME;
	}

	public int getENEMY_SPAWN_X() {
		return ENEMY_SPAWN_X;
	}

	public int getENEMY_SPAWN_Y() {
		return ENEMY_SPAWN_Y;
	}

	public int getPLAYER_SPAWN_X() {
		return PLAYER_SPAWN_X;
	}

	public int getPLAYER_SPAWN_Y() {
		return PLAYER_SPAWN_Y;
	}

	public String getSoundtrackFilePath() {
		return soundtrackFilePath;
	}

	public String getScoreUpSoundFilePath() {
		return scoreUpSoundFilePath;
	}

	public String getGameOverSoundFilePath() {
		return gameOverSoundFilePath;
	}


}