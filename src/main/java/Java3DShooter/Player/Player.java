package Java3DShooter.Player;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

import java.util.ArrayList;
import java.util.Map;

/**
 * The Player class contains information regarding the player. The player is a type of Group containing the player's hitbox. The player also has a PerspectiveCamera attached
 * to it allowing for first person movement. The player's hitbox and camera are both have position's tied to the move function which will move both Node's in accordance with the defined
 * movement controls when called
 */
public class Player extends Group {
    /**
     * Height of the player's hitbox in pixels
     */
    private static final int PLAYERHEIGHT = 20;

    /**
     * The amount of frames that must pass before the player takes damage again
     */
    private static final int invulnerabilityFramesTotal = 60 * 1;

    /**
     * The amount of frames remaining until the player can take damage again
     */
    private static int invulnerabilityFrames = 0;

    /**
     * Width of the player's hitbox in pixels
     */
    private static final int PLAYERWIDTH = 10;

    /**
     * Depth of the player's hitbox in pixels
     */
    private static final int PLAYERDEPTH = 10;

    /**
     * The maximum amount of hitpoints for the player
     */
    private static final double MAXHP = 5;

    /**
     * Current hitpoints of the player
     */
    private double HP = MAXHP;

    /**
     * The groundPlaneBoundingBox for the player. This will constrict the player's movement to it and allow the player to not fall through the ground
     */
    private final double[][] groundPlaneBoundingBox;

    /**
     * Number of frames between shots. Note that AnimationTimer pulses at 60Hz so we need to multiply the cooldown by 60
     */
    private static final int SHOTCOOLDOWN = 60 * 1;

    /**
     * Frames left before the player can shoot again
     */
    private int nextShot = 0;

    /**
     * All the current projectiles fired by the player as a list of Bullet's
     */
    private final ArrayList<Bullet> projectiles = new ArrayList<>();

    /**
     * All the projectiles as a group
     */
    private final Group projectilesGroup = new Group();

    /**
     * The speed at which the camera can move. Serves as a magnitude for our motion vectors
     */
    private static final double SPEED = 1.5;

    /**
     * How much faster the player moves when running (holding shift)
     */
    private static final double RUNSPEEDMULT = 1.2;

    /**
     * How high the player can jump
     */
    private static final double JUMPHEIGHT = 3;

    /**
     * Whether the player can currently jump
     */
    private boolean canJump = true;

    /**
     * Look speed for the camera, impacts how fast the camera will tilt
     */
    private static final double LOOKSPEED = 0.2;

    /**
     * Field of view of the camera
     */
    private static final double FOV = 40F;

    /**
     * The camera for the 3D environment, initialized through the initializeCamera(args) function
     */
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    /**
     * Hitbox for the player
     */
    private Box hitbox = new Box(PLAYERWIDTH, PLAYERHEIGHT, PLAYERDEPTH);

    /**
     * Current speed at which our camera is tilting, this value is used when updating the angle of the camera (x, y, z)
     */
    private double[] turnVelocity = {0, 0, 0};

    /**
     * Current speed at which our camera is moving, this value is determined by the lateral angle of the camera (xTilt) and is multiplied by the SPEED
     */
    private double[] velocity = {0, 0, 0};  // x, y, z move velocity

    /**
     * The force of gravity upon the player. Serves as an acceleration
     */
    private final double GRAVITY = 0.1;

    /**
     * The yaw(rotation around the y-axis) transformer of the camera
     * <p></p>
     * Used for left and right looking
     */
    private final Rotate yaw = new Rotate(0, Rotate.Y_AXIS);

    /**
     * The pitch(rotation around the x-axis) transformer of the camera
     * <p></p>
     * Used for up and down looking
     */
    private final Rotate pitch = new Rotate(0, Rotate.X_AXIS);

    /**
     * Initializes a player with x, y, and z position as well as a set farClip and nearClip for the camera
     * @param x x position
     * @param y y position
     * @param z z position
     * @param farClip farClip of the camera (render distance)
     * @param nearClip nearClip of the camera (near-render distance)
     */
    public Player(int x, int y , int z, int farClip, int nearClip, double[][] groundPlaneBoundingBox) {
        initializeCamera(x, y, z, farClip, nearClip, new Transform[] {yaw, pitch});
        initializeHitbox(x, y, z);
        this.groundPlaneBoundingBox = groundPlaneBoundingBox;
        this.getChildren().add(projectilesGroup); // Stores and updates the currently living bullets
    }

    /**
     * Initializes a player with a set x, y, and z position
     * @param x x position
     * @param y y position
     * @param z z postition
     */
    public Player(int x, int y, int z, double[][] groundPlaneBoundingBox) {
        this(x, y, z, 1000, 10, groundPlaneBoundingBox);
    }

    /**
     * Initializes a player with default parameters
     */
    public Player(double[][] groundPlaneBoundingBox) {
        this(0, -10, -200, 5000, 10, groundPlaneBoundingBox);
    }

    /**
     * Gets the x position of the player (hitbox and camera)
     * @return x position
     */
    public double getX() { return hitbox.getTranslateX(); }

    /**
     * Gets the y position of the player (hitbox and camera)
     * @return y position
     */
    public double getY() { return hitbox.getTranslateY(); }

    /**
     * Gets the z position of the player (hitbox and camera)
     * @return z position
     */
    public double getZ() { return hitbox.getTranslateZ(); }

    /**
     * Gets the perspective camera of the player
     * @return PerspectiveCamera
     */
    public PerspectiveCamera getCamera() { return this.camera; }

    /**
     * Gets the hitbox of the player
     * @return Box hitbox
     */
    public Box getHitbox() { return this.hitbox; }

    /**
     * Gets all the bullets as an array of Bullets
     * @return Bullet[] bullets
     */
    public Bullet[] getBullets() {
        return projectiles.toArray(new Bullet[0]);
    }

    /**
     * Sets the translation properties of a node
     * @param n node to be translated
     * @param x node's new x position
     * @param y node's new y position
     * @param z node's new z position
     */
    private static void setTranslate(Node n, double x, double y, double z) {
        n.setTranslateX(x);
        n.setTranslateY(y);
        n.setTranslateZ(z);
    }

    /**
     * Returns a vector of motion < cos(angle), sin(angle) >
     * <p>
     * This is used to determine the movement on two separate axis based on the angle of the camera allowing forward movement to take you in the direction of view
     * <p>
     * For x and z motion use the camera's xTilt and for z-movement use the return[0] and return[1] for x-movement
     * <p>
     * For x and y motion use the camera's yTilt and then use return[0] for x and return[1] for y
     * @param angle the angle of tilt/inclination
     * @return vector of motion for magnitude = 1
     */
    private double[] calculateMotionVector(double angle) {
        angle = Math.toRadians(angle);
        return new double[] {Math.cos(angle), Math.sin(angle)};
    }

    /**
     * Sets up the camera for the scene
     * @param x initial x position of the camera
     * @param y initial y position of the camera
     * @param z initial z position of the camera
     * @param farClip the far render distance for the camera
     * @param nearClip the near render distance for the camera
     * @param transforms the transforms or rotations for the camera
     */
    private void initializeCamera(int x, int y, int z, int farClip, int nearClip, Transform[] transforms) {
        setTranslate(camera, x, y, z);
        camera.setNearClip(nearClip);
        camera.setFarClip(farClip);
        camera.getTransforms().addAll(transforms);
        camera.setFieldOfView(FOV);
    }

    /**
     * Initializes the player's hitbox
     * @param x x position of the hitbox
     * @param y y position of the hitbox
     * @param z z position of the hitbox
     */
    private void initializeHitbox(int x, int y, int z) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.RED);
        hitbox.setMaterial(material);
        setTranslate(hitbox, x, y, z);
        hitbox.setVisible(false);
        this.getChildren().add(hitbox);
    }

    /**
     * Creates a new bullet at the player's position
     */
    private void shoot() {
        if (nextShot > 0) {return;}  // They are still on cooldown

        double yawRad = Math.toRadians(yaw.getAngle());
        double pitchRad = Math.toRadians(pitch.getAngle());

        // This bit of calculation uses a concept known as 3D kinematics
        // Because we have a 3D environment our motion vector changes a little bit
        // If we don't account for the y-angle in our x and z velocities the bullet will not go straight up or down when it's shot in those directions
        // The trigonometric representation of a 3D vector is defined below where: r = magnitude, pitch = vertical angle around x-axis, yaw = horizontal angle around y-axis
        // <r * sin(yaw) * cos(pitch), r * sin(pitch), r * cos(yaw) * cos(pitch)>
        // For more information and to see the original formulas view https://math.libretexts.org/Bookshelves/Calculus/Calculus_%28OpenStax%29/12%3A_Vectors_in_Space/12.07%3A_Cylindrical_and_Spherical_Coordinates
        // My basic understanding of it is that these are the formulas for converting spherical coordinates (like Polar but in the 3rd dimension) back to rectangular(Cartesian) coordinates
        // We worked with Polar to rectangular(Cartesian) back in the enemy spawn mechanics for reference
        double xRelVel = Math.sin(yawRad) * Math.cos(pitchRad);
        double yRelVel = -Math.sin(pitchRad); // Negative because upward tilt is negative angle
        double zRelVel = Math.cos(yawRad) * Math.cos(pitchRad);

        // We don't apply a magnitude here since that is for the bullet class to calculate thus why these are called Relative velocities
        // The bullets handle their own speed, the player handles calculation of the angle since it has the camera element

        // Add a new bullet to the projectiles list with the camera's coordinates then the velocity of the x, y, and z axis
        // Y-axis is negative here because of how the y-axis is reversed in the world of programming
        projectiles.add(new Bullet(
                camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ(),
                xRelVel, yRelVel, zRelVel
        ));

        // Set the cooldown before their next shot
        nextShot = SHOTCOOLDOWN;

    }

    /**
     * Updates the positions of every bullet and kills any bullets whose timeToLive is expired
     */
    private void moveBullets() {
        ArrayList<Bullet> deadProjectiles = new ArrayList<>();

        for (Bullet bullet : projectiles) {
            // If the bullet's TTL is expired we add it to the deadProjectiles list to remove later
            if (bullet.getTimeToLive() <= 0) {
                deadProjectiles.add(bullet);
                continue;
            }

            bullet.move();
        }

        projectiles.removeAll(deadProjectiles);

        // Reset the projectilesGroup so that it only has the currently living projectiles
        projectilesGroup.getChildren().setAll(projectiles);
    }

    /**
     * Reduces the player's health by the damage taken unless the player is already dead or have invulnerability frames remaining
     * @param damage damage taken
     */
    public void takeDamage(double damage) {
        if (isDead()) {return;}  // Player is already dead
        if (invulnerabilityFrames > 0) {return;}
        invulnerabilityFrames = invulnerabilityFramesTotal;
        this.HP -= damage;
    }

    /**
     * Returns whether the player is dead
     * @return isDead?
     */
    public boolean isDead() { return this.HP <= 0; }

    public void look(double deltaX, double deltaY) {
        pitch.setAngle(pitch.getAngle() - (deltaY * LOOKSPEED));
        yaw.setAngle(yaw.getAngle() + (deltaX * LOOKSPEED));
    }

    /**
     * Moves the player forward a frame
     * @param keysHeld the keys currently being help
     * @throws IndexOutOfBoundsException if the groundPlaneBoundingBox was improperly configured
     */
    public void move(Map<String, Boolean> keysHeld) throws IndexOutOfBoundsException {
        // Update the bullet's positions
        moveBullets();

        // Reduce the cooldown before the next shot
        // We do this through a conditional to prevent a negative overflow if they don't shoot for too long
        if (nextShot > 0) {nextShot--;}

        // Reduce the remaining iFrames
        if (invulnerabilityFrames > 0) {invulnerabilityFrames--;}

        // Clear our old velocities except for y velocity so we can reset them using the key holds
        velocity = new double[] {0, velocity[1], 0};
        turnVelocity = new double[] {0, 0, 0};


        // Calculate our motionVectors for x and z axial movement
        double[] zMotionVector = calculateMotionVector(yaw.getAngle());
        double[] xMotionVector = calculateMotionVector(yaw.getAngle() + 90);

        boolean isRunning = false;

        // Stores our total velocity vectors after considering all held keys
        // This allows movement via two keys at once e.g. 'a' & 'w'
        double vx = 0;
        double vz = 0;

        // Handle the different key presses here
        for (String key : keysHeld.keySet()) {
            switch (key) {
                // Camera Controls
                case "Up":
                    turnVelocity[1] = LOOKSPEED;
                    break;
                case "Down":
                    turnVelocity[1] = -LOOKSPEED;
                    break;
                case "Left":
                    turnVelocity[0] = -LOOKSPEED;
                    break;
                case "Right":
                    turnVelocity[0] = LOOKSPEED;
                    break;


                // Movement Controls
                case "W":
                    vx += zMotionVector[1];
                    vz += zMotionVector[0];
                    break;
                case "S":
                    vx -= zMotionVector[1];
                    vz -= zMotionVector[0];
                    break;
                case "A":
                    vx -= xMotionVector[1];
                    vz -= xMotionVector[0];
                    break;
                case "D":
                    vx += xMotionVector[1];
                    vz += xMotionVector[0];
                    break;
                case "Space":
                    if (canJump) {
                        velocity[1] = -JUMPHEIGHT;
                        canJump = false;
                    }
                    break;
                case "Shift":
                    isRunning = true;
                    break;
                case "Y":
                case "PRIMARY":  // Left click
                    shoot();
                    break;
            }
        }

        // Magnitude allows for normalization of the velocity vectors
        // Normalization means that if they hold two different movement keys at once they won't go any faster than if they were to hold just one
        double magnitude = Math.sqrt(Math.pow(vx, 2) + Math.pow(vz, 2));

        if (magnitude != 0) {
            velocity[0] = (vx / magnitude) * SPEED;
            velocity[2] = (vz / magnitude) * SPEED;
        }

        // Gravity is acceleration so we add gravity every frame to our velocity
        velocity[1] += GRAVITY;

        // Multiply by the runspeed mult if they are running
        if (isRunning) {
            velocity[0] *= RUNSPEEDMULT;
            velocity[2] *= RUNSPEEDMULT;
        }

        // Get their next position
        double newXPosition = camera.getTranslateX() + velocity[0];
        double newZPosition = camera.getTranslateZ() + velocity[2];

        // Make sure their y is above the floor and doesn't phase through it
        double newYPosition = Math.min(camera.getTranslateY() + velocity[1], groundPlaneBoundingBox[1][0] - PLAYERHEIGHT);


        // Ensure they are within the x and z bounds
        if (newXPosition > groundPlaneBoundingBox[0][1]) {
            newXPosition = groundPlaneBoundingBox[0][1];
        } else if (newXPosition < groundPlaneBoundingBox[0][0]) {
            newXPosition = groundPlaneBoundingBox[0][0];
        }

        if (newZPosition > groundPlaneBoundingBox[2][1]) {
            newZPosition = groundPlaneBoundingBox[2][1];
        } else if (newZPosition < groundPlaneBoundingBox[2][0]) {
            newZPosition = groundPlaneBoundingBox[2][0];
        }


        // If they got their position reset we also reset their gravity and jump flag so they can jump next frame
        if (newYPosition == groundPlaneBoundingBox[1][0] - PLAYERHEIGHT) {
            velocity[1] = 0;  // reset the velocity to prevent an overflow
            canJump = true;
        }

        // Set the new position
        setTranslate(camera, newXPosition, newYPosition, newZPosition);
        setTranslate(hitbox, newXPosition, newYPosition, newZPosition);

        // Camera Movement
        double newXTilt = yaw.getAngle() + turnVelocity[0];
        double newYTilt = pitch.getAngle() + turnVelocity[1];

        // Constrain how far they can look up or down to a 180deg range
        if (newYTilt > 90) {
            newYTilt = 90;
        } else if (newYTilt < -90) {
            newYTilt = -90;
        }

        yaw.setAngle(newXTilt);
        pitch.setAngle(newYTilt);
    }
}
