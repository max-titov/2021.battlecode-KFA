package testerplayer;

import battlecode.common.*;

public class Robot {
	static RobotController rc;
	static Comms comms;
	static Nav nav;

	static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

	static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

	int robotAge;

	public Robot(RobotController r) {
		this.rc = r;
		this.comms = new Comms(rc);
		this.nav = new Nav(rc);
	}

	public void takeTurn() throws GameActionException {
		robotAge += 1;
	}

	Direction randomDirection() {
		return directions[(int) (Math.random() * directions.length)];
	}

	RobotType randomSpawnableRobotType() {
		return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
	}

	/**
	 * Attempts to move in a given direction.
	 *
	 * @param dir The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	boolean tryMove(Direction dir) throws GameActionException {
		if (rc.canMove(dir)) {
			rc.move(dir);
			return true;
		} else
			return false;
	}
}