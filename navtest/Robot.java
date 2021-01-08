package navtest;

import battlecode.common.*;

public class Robot {
	/**
	 * Constants
	 */
	public final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };
	static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

	/**
	 * 'Robot' Object Attributes
	 */
	public RobotController rc;
	public Navigation nav;
	public Comms comms;
	public int robotAge;

	/**
	 * Constructor
	 * 
	 * @param rc
	 */
	public Robot(RobotController rc) {
		this.rc = rc;
		this.nav = new Navigation(rc);
		this.comms = new Comms(rc);
	}

	/**
	 * 'Robot' Object Methods
	 */
	public void takeTurn() throws GameActionException {
		robotAge += 1;
	}

	public Direction randomDirection() {
		return directions[(int) (Math.random() * directions.length)];
	}

	public RobotType randomSpawnableRobotType() {
		return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
	}
}