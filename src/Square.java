import java.io.Serializable;

public class Square implements Serializable{
	
	// attributes
	private Spaceship ship;
	private boolean empty;
	private Coordinate coordinate;
	
	// constructor
	public Square(Coordinate coordinate) {
	    this.ship = null;
	    this.empty = true;
	    setCoordinate(coordinate);
	}
		
	// complex methods
	
	// method to place a spaceship within a square
	// sets the coordinate of the ship to that of the square
	public void placeShip(Spaceship ship) {
		this.ship = ship;
		this.empty = false;
		this.ship.setCoordinate(this.coordinate);
	}
	public void removeShip() {
	    this.ship = null;
	    this.empty = true;
	}
	// returns true if the ship in the square is an enemy
	public boolean hasEnemyShip() {
		
		if(this.ship.isPlayer() == false) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// getters and setters
	public Spaceship getShip() {
		return ship;
	}
	public boolean isEmpty() {
		return empty;
	}
	public void setEmpty(boolean empty) {
		this.empty = empty;
	}
	public Coordinate getCoordinate() {
		return coordinate;
	}
	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}
	// end getters and setters
	

}// end class
