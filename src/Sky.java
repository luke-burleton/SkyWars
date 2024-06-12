import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class Sky extends Observable implements Serializable{

	// attributes
	private final int SKY_WIDTH = 5;
	private final int SKY_HEIGHT = 5;
	private Square[][] squares = new Square[SKY_WIDTH][SKY_HEIGHT];
	private boolean playerTookEnemy;

	// constructor
	// populates the sky with squares, and assigns the squares relative coordinates
	public Sky() {
		for (int i = 0; i < SKY_WIDTH; i++) {
			for (int j = 0; j < SKY_HEIGHT; j++) {
				Coordinate coordinate = new Coordinate(i, j);
				squares[i][j] = new Square(coordinate);
			}
		}
	}

	// complex methods
	// places a ship within a square in the sky, updates the ship's coordinates
	public void placeShip(Spaceship ship, Coordinate coordinate) {
		this.squares[coordinate.getX()][coordinate.getY()].placeShip(ship);
		setChanged();
		notifyObservers(squares[coordinate.getX()][coordinate.getY()]);
	}
	// method to handle the logic of ship movement ship one square in a given direction (0 = left, 1 = right, 2 = up, 3 = down)
	public void moveShip(String direction, Spaceship ship) {
		// reset the took enemy ship boolean
		setPlayerTookEnemy(false);
		// fetch the ship's current coordinate
	    Coordinate currentCoordinate = ship.getCoordinate();

	    // fetch its X and Y values
	    int X = currentCoordinate.getX();
	    int Y = currentCoordinate.getY();
	    
	    // save original coordinates
	    int oldX = X;
	    int oldY = Y;

	    // depending on direction update the X/Y values
	    switch (direction) {
	        case "left": // left
	            Y--;
	            break;
	        case "right": // right
	            Y++;
	            break;
	        case "up": // up
	            X--;
	            break;
	        case "down": // down
	            X++;
	            break;
	    }

	    // check that the new coordinates are within bounds
	    if (X >= 0 && X < SKY_WIDTH && Y >= 0 && Y < SKY_HEIGHT) {
	        // Check if the new square is empty
	        if (squares[X][Y].isEmpty()) {
	            // move the ship to the new square
	            moveShipTo(ship, X, Y);
	        }
	        // if the square has an enemy ship, and the ship being moved is a player ship, replace the enemy ship with the player ship
	        if (squares[X][Y].getShip() instanceof EnemySpaceship && ship instanceof PlayerSpaceship && ((PlayerSpaceship) ship).getMode() == "normal") {
	            // check if the square is surrounded by other enemies - if so removes playership
	        	if(numberOfSurroundingEnemies(squares[X][Y].getShip()) > 0) {
	        		squares[oldX][oldY].removeShip();
	        		setChanged();
	        		notifyObservers(this.squares[oldX][oldY]);
	        	    notifyObservers(this.squares[X][Y]);
	        	}
	        	// move the ship to the new square
	        	else{
	        		moveShipTo(ship, X, Y);
	        		setPlayerTookEnemy(true);
	        	}
	        }
	        // handle when the ship is in the secret master mode - no rules for taking enemy ships
	        if (squares[X][Y].getShip() instanceof EnemySpaceship && ship instanceof PlayerSpaceship && ((PlayerSpaceship) ship).getMode() == "master") {
	            // check if the square is surrounded by other enemies - if so removes playership
	        	moveShipTo(ship, X, Y);
	        	setPlayerTookEnemy(true);
	        }
	    }
	}
	// method to avoid repetition in above moveShip method
	private void moveShipTo(Spaceship ship, int newX, int newY) {
	    // fetch the ship's current coordinate
	    Coordinate currentCoordinate = ship.getCoordinate();

	    // fetch its X and Y values
	    int oldX = currentCoordinate.getX();
	    int oldY = currentCoordinate.getY();

	    // remove the ship from the old square
	    this.squares[oldX][oldY].removeShip();

	    // create an object for the new coordinate
	    Coordinate newCoordinate = new Coordinate(newX, newY);

	    // place the ship in the new square (also updates coordinates)
	    placeShip(ship, newCoordinate);

	    // notify the observers that the squares have been updated
	    setChanged();
	    notifyObservers(this.squares[oldX][oldY]);
	    notifyObservers(this.squares[newX][newY]);
	}
	
	// method to scan around a ship and returns the number of surrounding enemies
	// only to be used after a move
	public int numberOfSurroundingEnemies(Spaceship ship) {
	    int numberOfEnemies = 0;
	    Coordinate shipCoordinate = ship.getCoordinate();

	    // iterate over the surrounding coordinates of the spaceship's current coordinate
	    for (Coordinate surroundingCoord : getSurroundingCoordinates(shipCoordinate)) {
	        int x = surroundingCoord.getX();
	        int y = surroundingCoord.getY();

	        // check if there is an enemy ship in the surrounding square
	        if (squares[x][y].getShip() instanceof EnemySpaceship) {
	            // if there is an enemy ship, increment the collision count
	        	numberOfEnemies++;
	        }
	    }

	    return numberOfEnemies;
	}
	// method which returns a list of surrounding coordinates for a given coordinate 
	public List<Coordinate> getSurroundingCoordinates(Coordinate coordinate) {
	    
		// create a list to store the coordinates
		List<Coordinate> surroundingCoordinates = new ArrayList<>();

	    // iterate over the 3x3 square centred at the given coordinate
	    for (int i = coordinate.getX() - 1; i <= coordinate.getX() + 1; i++) {
	        for (int j = coordinate.getY() - 1; j <= coordinate.getY() + 1; j++) {
	            // skip the coordinate itself
	            if (i == coordinate.getX() && j == coordinate.getY()) {
	                continue;
	            }
	            // add the surrounding coordinate if it is within bounds
	            if (i >= 0 && i < SKY_WIDTH && j >= 0 && j < SKY_HEIGHT) {
	                surroundingCoordinates.add(new Coordinate(i, j));
	            }
	        }
	    }

	    return surroundingCoordinates;
	}
	// method which returns a list of all enemy ships currently in the sky
	public List<Spaceship> getEnemyShips() {
	    List<Spaceship> enemyShips = new ArrayList<>();

	    for (int i = 0; i < SKY_WIDTH; i++) {
	        for (int j = 0; j < SKY_HEIGHT; j++) {
	            Square currentSquare = squares[i][j];
	            if (!currentSquare.isEmpty() && currentSquare.getShip().isPlayer()==false) {
	                enemyShips.add(currentSquare.getShip());
	            }
	        }
	    }

	    return enemyShips;
	}
	// method which checks if there are any PlayerShips in the sky, returns true if so and false if not
	public boolean hasPlayerShips() {
	    for (int i = 0; i < SKY_WIDTH; i++) {
	        for (int j = 0; j < SKY_HEIGHT; j++) {
	            Square square = squares[i][j];
	            if (!square.isEmpty() && square.getShip() instanceof PlayerSpaceship) {
	                return true;
	            }
	        }
	    }
	    return false;
	}
	
	// getters and setters
	public Square[][] getSquares() {
		return squares;
	}

	public void setSquares(Square[][] squares) {
		this.squares = squares;
	}

	public int getSKY_WIDTH() {
		return SKY_WIDTH;
	}

	public int getSKY_HEIGHT() {
		return SKY_HEIGHT;
	}
	public Square getSquare(int x, int y) {
		return this.squares[x][y];
	}
	public boolean isPlayerTookEnemy() {
		return playerTookEnemy;
	}

	public void setPlayerTookEnemy(boolean playerTookEnemy) {
		this.playerTookEnemy = playerTookEnemy;
	}


}