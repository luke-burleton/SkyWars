import java.io.Serializable;

public class Coordinate implements Serializable{

	// declare attributes
	private int x;
	private int y;
	
	// constructor
	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	// getters and setters
	public int getX() {
		return this.x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return this.y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	// complex methods
	public void updateCoordinate(int x, int y) {
		setX(x);
		setY(y);
	}
	
}// end class
