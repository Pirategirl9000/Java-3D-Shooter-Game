package Java3DShooter.Player;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * 
 * Represents a single bullet; contains methods for handling the bullet
 */
public class BasicBullet extends Box {
    /**
     * Width of the bullet
     */
    private static final double WIDTH = 3;

    /**
     * Height of the bullet
     */
    private static final double HEIGHT = 3;

    /**
     * Depth (z length) of the bullet
     */
    private static final double DEPTH = 3;

    /**
     * Speed of the bullet, serves as a magnitude for the motion vector
     */
    private static final double BULLETSPEED = 10;

    /**
     * Number of move frames before the bullet dies
     */
    private int timeToLive = 300;

    /**
     * Color of the bullets
     */
    private static final Color color = Color.BLACK;

    /**
     * X, Y, Z velocity of the bullet - Does not decrease as it travels
     */
    private final double[] velocity;

    /**
     * Creates a new bullet at the set position
     */
    protected BasicBullet(double x, double y, double z, double xVel, double yVel, double zVel) {
        // Hey that's cool, you can't reference these fields(WIDTH, HEIGHT, DEPTH) unless they are static because super precedes the object's creation
        // I guess that's why super() has to be the first call in the constructor since the parent class has to be initialized first before the child
        // I did not know that :)
        super(WIDTH, HEIGHT, DEPTH);

        super.setTranslateX(x);
        super.setTranslateY(y);
        super.setTranslateZ(z);

        super.setMaterial(new PhongMaterial(color));

        velocity = new double[] {xVel * BULLETSPEED, yVel * BULLETSPEED, zVel * BULLETSPEED};
    }

    /**
     * Returns the bullet's remaining timeToLive
     * @return Frames left before death
     */
    protected int getTimeToLive() { return timeToLive; }

    /**
     * Moves the bullet forward by one frame
     */
    protected void move() {
        this.setTranslateX(this.getTranslateX() + velocity[0]);
        this.setTranslateY(this.getTranslateY() + velocity[1]);
        this.setTranslateZ(this.getTranslateZ() + velocity[2]);

        timeToLive--;
    }

    /**
     * Kills the bullet early, used when it collides with an enemy
     */
    public void killBullet() { this.timeToLive = 0; }
}
