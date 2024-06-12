
public class EnemySpaceship extends Spaceship{
	
	// give enemy ships a type variable so there is the option to add more in the future
	private String type;

	public EnemySpaceship(Coordinate coordinate) {
		super(coordinate, false);
		this.type = "Basic";
	}
}
