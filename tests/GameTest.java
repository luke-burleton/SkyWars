import static org.junit.Assert.assertEquals;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.junit.Before;
import org.junit.Test;

public class GameTest {
    
    private Game game;
    
    @Before
    public void setUp() {
        game = new Game();
    }

    @Test
    public void testStartNewGame() {
        game.startNewGame();
        assertEquals(GameState.START, game.getGameState());
    }

    @Test
    public void testResetGame() {
        game.setMoveCount(10);
        game.setScore(100);
        game.setGameState(GameState.PAUSED);
        game.resetGame(game);
        assertEquals(0, game.getMoveCount());
        assertEquals(0, game.getScore());
        assertEquals(GameState.RUNNING, game.getGameState());
    }

    @Test
    public void testSpawnEnemyShip() {
        Sky sky = new Sky();
        game.setSky(sky);
        game.setEnemySpawnPoint(new Coordinate(0, 0));
        game.spawnEnemyShip();
        assertEquals(1, sky.getEnemyShips().size());
    }

    @Test
    public void testMoveEnemyShips() {
        Sky sky = new Sky();
        game.setSky(sky);
        EnemySpaceship enemyShip = new EnemySpaceship(new Coordinate(0, 0));
        sky.placeShip(enemyShip, enemyShip.getCoordinate());
        game.moveEnemyShips();
        assertEquals(true, enemyShip.getCoordinate().getX() != 0 || enemyShip.getCoordinate().getY() != 0);
    }

    @Test
    public void testMovePlayerShip() {
        Sky sky = new Sky();
        game.setSky(sky);
        PlayerSpaceship playerShip = new PlayerSpaceship(new Coordinate(4, 4));
        sky.placeShip(playerShip, playerShip.getCoordinate());
        game.setPlayerShip(playerShip);
        game.movePlayerShip("up");
        assertEquals(3, playerShip.getCoordinate().getX());
    }

}