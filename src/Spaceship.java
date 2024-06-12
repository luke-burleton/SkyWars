import java.io.Serializable;

public abstract class Spaceship implements Serializable{
	
	// attributes
	// coordinate will match that of the square the ship is currently in
	private Coordinate coordinate;
	// each ship can have an alliance, if it belongs to player or enemy:
	// useful if the game were to expand and the player can unlock multiple ships
	private boolean player;
	
	// constructor
	public Spaceship(Coordinate coordinate, boolean player) {
		setCoordinate(coordinate);
		setPlayer(player);
	}
	
	// getters and setters
	public Coordinate getCoordinate() {
		return coordinate;
	}
	public int getX() {
		return this.coordinate.getX();
	}
	public int getY() {
		return this.coordinate.getY();
	}
	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}
	public boolean isPlayer() {
		return player;
	}
	public void setPlayer(boolean player) {
		this.player = player;
	}
	
}
