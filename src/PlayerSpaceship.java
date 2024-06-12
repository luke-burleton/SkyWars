
public class PlayerSpaceship extends Spaceship{

	private String mode;

	public PlayerSpaceship(Coordinate coordinate) {
		super(coordinate, true);
		setMode("normal");
	}
	
	// getters and setters
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
