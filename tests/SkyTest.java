import org.junit.Test;
import static org.junit.Assert.*;

public class SkyTest {

    @Test
    public void testPlaceShip() {
        Sky sky = new Sky();
        Coordinate coordinate = new Coordinate(0, 0);
        Spaceship spaceship = new PlayerSpaceship(coordinate);

        sky.placeShip(spaceship, coordinate);

        assertEquals(sky.getSquares()[0][0].getShip(), spaceship);
    }

    @Test
    public void testMoveShip() {
        Sky sky = new Sky();
        Coordinate playerCoordinate = new Coordinate(0, 0);
        Coordinate enemyCoordinate = new Coordinate(1, 0);
        Spaceship playerSpaceship = new PlayerSpaceship(playerCoordinate);
        Spaceship enemySpaceship = new EnemySpaceship(enemyCoordinate);


        sky.placeShip(playerSpaceship, playerCoordinate);
        sky.placeShip(enemySpaceship, enemyCoordinate);

        // check player ship moves
        sky.moveShip("right", playerSpaceship);

        assertEquals(sky.getSquares()[0][0].getShip(), null);
        assertEquals(sky.getSquares()[0][1].getShip(), playerSpaceship);
        assertEquals(sky.getSquares()[1][0].getShip(), enemySpaceship);

        // check enemy ship moves
        sky.moveShip("right", enemySpaceship);

        assertEquals(sky.getSquares()[0][1].getShip(), playerSpaceship);
        assertEquals(sky.getSquares()[1][0].getShip(), null);
        assertEquals(sky.getSquares()[1][1].getShip(), enemySpaceship);

        // check player ship takes enemy ship
        sky.moveShip("down", playerSpaceship);

        assertEquals(sky.getSquares()[0][1].getShip(), null);
        assertEquals(sky.getSquares()[1][1].getShip(), playerSpaceship);
    }

    @Test
    public void testNumberOfSurroundingEnemies() {
        Sky sky = new Sky();
        Coordinate playerCoordinate = new Coordinate(0, 0);
        Coordinate enemyCoordinate1 = new Coordinate(0, 1);
        Coordinate enemyCoordinate2 = new Coordinate(1, 0);
        Spaceship playerSpaceship = new PlayerSpaceship(playerCoordinate);
        Spaceship enemySpaceship1 = new EnemySpaceship(enemyCoordinate1);
        Spaceship enemySpaceship2 = new EnemySpaceship(enemyCoordinate2);

        sky.placeShip(playerSpaceship, playerCoordinate);
        sky.placeShip(enemySpaceship1, enemyCoordinate1);
        sky.placeShip(enemySpaceship2, enemyCoordinate2);

        int numberOfEnemies = sky.numberOfSurroundingEnemies(playerSpaceship);

        assertEquals(numberOfEnemies, 2);
    }
}