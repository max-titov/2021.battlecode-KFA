package navtest;

import java.util.Map;

import battlecode.common.*;

public class Robot {
	/**
	 * Constants
	 */
	public static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER,
			RobotType.MUCKRAKER, };
	public static final Direction[] directions = Direction.allDirections();

	/**
	 * 'Robot' Object Attributes
	 */
	private RobotController rc;
	private Comms comms;
	private int robotAge;

	/**
	 * Constructor
	 * 
	 * @param rc
	 */
	public Robot(RobotController rc) {
		this.rc = rc;
		this.comms = new Comms(rc);
	}

	/**
	 * 'Robot' Object Methods
	 */
	public void takeTurn() throws GameActionException {
		robotAge += 1;
	}

	public RobotType randomSpawnableRobotType() {
		return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
	}
}