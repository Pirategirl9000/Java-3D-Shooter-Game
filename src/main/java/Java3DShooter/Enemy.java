package Java3DShooter;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import java.util.Random;

/**
 * The Enemy class represents a single enemy in the game. It contains methods relating to its movement, taking damage, spawn location, etc.
 * <p></p>
 * Before any Enemy's can be created you have to initialize the groundPlaneBoundingBox which is the groundPlane you wish to spawn the enemy on
 * After initializing it you can create enemies without issue
 */
public class Enemy extends Box {

    /**
     * The bounding box of the ground plane as defined by {{xMin, xMax}, {yMin, yMax}, {zMin, zMax}}
     */
    private static double[][] groundPlaneBoundingBox;

    /**
     * Width of the enemy's sprite
     */
    private static final double WIDTH = 10;

    /**
     * Height of the enemy's sprite
     */
    private static final double HEIGHT = 20;

    /**
     * Depth of the enemy's sprite (length of z-axis)
     */
    private static final double DEPTH = 10;

    /**
     * Serves as the random number generator for the programmer, used to determine spawn locations. Seed is set upon class definition
     */
    private static final Random rand = new Random(System.currentTimeMillis());

    /**
     * Color of the enemy's sprite
     */
    private static final Color COLOR = Color.RED;

    /**
     * Speed of the enemy
     */
    private static final double SPEED = 1;

    /**
     * The damage any new instances of Enemy will do
     */
    private static final double MAXDAMAGE = 1.0;

    /**
     * The damage this instance of Enemy does
     */
    private final double DAMAGE = MAXDAMAGE;

    /**
     * The closest an enemy can spawn to the player
     */
    private static final int minDistanceFromPlayerOnSpawn = 500;

    /**
     * Max hitpoints for an enemy. This is their starting hp
     */
    private static double MaxHP = 1;

    /**
     * Current HP of the enemy
     */
    private double HP = MaxHP;

    /**
     * initializes a new Enemy
     * <p>
     * Ensure that you call {@link #setGroundPlaneBoundingBox(double[][])} beforehand so the enemy's spawn coordinates can be determined
     * @param player Box of the player
     * @throws IllegalArgumentException if the {@link #setGroundPlaneBoundingBox(double[][])} was not called beforehand
     */
    public Enemy(Box player) throws IllegalArgumentException {
        super(WIDTH, HEIGHT, DEPTH);
        super.setTranslateY(groundPlaneBoundingBox[1][0] - HEIGHT/2);  // Y position doesn't change for enemies so we assign it here and never change it

        // Not the best practice way to do things, but it ensures we have fast creation of enemies
        // Better than passing the groundPlane every time we create an enemy
        if (groundPlaneBoundingBox == null) {
            throw new IllegalArgumentException("No groundPlaneBoundingBox set, call Enemy.setGroundPlaneBoundingBox(double[][]) before instantiating any Enemies");
        }

        PhongMaterial material = new PhongMaterial(COLOR);
        super.setMaterial(material);

        this.setSpawn(player);
    }

    /**
     * Sets the spawn location for the enemy based
     */
    private void setSpawn(Box player) {
        // Credit where it's due, this was not my original idea. I got this from AI and then prompted it several times till I (somewhat) understood the math behind it.
        // I believe AI should be used as a learning tool and not to do everything for me
        // Hopefully my comments do a good job at showing my understanding of the topic rather than blind trust in a random equation an artificial intellegence suggested
        // That being said this is quite ingenius, and I'm upset I didn't think of something like this myself, but I know myself well enough to know I couldn't do something like this without seeing it first

        // Get the player's x and z coords
        double px = player.getTranslateX();
        double pz = player.getTranslateZ();

        // Strap in gamers, this code gets funky
        // I'm talking Austin Powers funky
        // My explanation is pretty bad here, but it uses a uniform distribution which will allow it to have equal chance of spawning anywhere outside the no-spawn ring around the player
        // It's a very complex formula but hopefully my explanation does a decent job at explaning this.


        // Get a random angle to spawn the enemy relative to
        double angle = rand.nextDouble() * 2 * Math.PI;

        // The minimum distance for a radius^2 ring of our spawn circle
        // We use r^2 since the area of a circle grows with it's radius^2
        // If we didn't do this it wouldn't be uniform and the spawn ring would be more likely to be close to the minDistanceFromPlayerOnSpawn
        double minR2 = Math.pow(minDistanceFromPlayerOnSpawn, 2);

        // The maximum distance for a radius^2 ring of our spawn circle
        // We use the length of the groundPlaneBoundingBox divided by two which gives us a circle that is inside of this plane
        // It doesn't reach the farthest corners, but it is contained entirelly within the ground plane
        // This all assumes the x and z coordinates of the ground plane form a square otherwise this will not work
        double maxR2 = Math.pow((groundPlaneBoundingBox[0][1] - groundPlaneBoundingBox[0][0]) / 2, 2);

        // Gets a random radius^2 within our possible radii for the spawn ring then sqrt's it so we can convert it back to Cartesian coordinates and then assign that position to the enemy
        double radius = Math.sqrt(minR2 + rand.nextDouble() * (maxR2 - minR2));

        // Convert from Polar coordinates to Cartesian for the enemy to spawn at
        // Polar to Cartesian form = (h + r * cos(angle), k + r * sin(angle))
        // Where (h, k) is the center of the circle (in our case the player's location is (h,k)
        double x = px + radius * Math.cos(angle);
        double z = pz + radius * Math.sin(angle);

        // Then we set these new coordinates
        this.setTranslateX(x);
        this.setTranslateZ(z);
    }

    /**
     * Sets the Ground plane's bounding box which is used to determine where an enemy can spawn
     * </p>
     * Must be called before instantiation of an Enemy
     * @param boundingBox the bounding box as defined by {{xMin, xMax}, {yMin, yMax}, {zMin, zMax}}
     */
    public static void setGroundPlaneBoundingBox(double[][] boundingBox) { groundPlaneBoundingBox = boundingBox; }

    /**
     * Returns the damage the enemy deals
     * @return the damage dealt as a double
     */
    public double getDamage() {return DAMAGE;}

    /**
     * Returns whether this enemy's health is less than or equal to 0
     * @return whether the enemy is considered dead
     */
    public boolean isDead() { return this.HP <= 0;}

    /**
     * Reduces the enemy's health by the damage taken unless the enemy is already dead
     * @param damage damage taken
     */
    public void takeDamage(int damage) {
        if (isDead()) {return;}  // Enemy is already dead
        this.HP -= damage;
    }

    /**
     * Moves the enemy one frame in the directon of the player
     * @param playerX Player's x position
     * @param playerZ Player's z position
     */
    public void move(double playerX, double playerZ) {
        double dx = this.getTranslateX() - playerX;
        double dz = this.getTranslateZ() - playerZ;

        // Note that in this case: cos(a) = z; sin(a) = x
        // Thus in a tan() x->y and z->x
        double angle = Math.atan2(dx, dz);  // In radians so we don't need to convert it later

        this.setTranslateX(this.getTranslateX() + (-SPEED * Math.sin(angle)));
        this.setTranslateZ(this.getTranslateZ() + (-SPEED * Math.cos(angle)));
    }
}
