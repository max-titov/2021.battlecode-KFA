package navtest;

import battlecode.common.*;

public class Navigation {

	public static final Direction[] directions = Direction.allDirections();

	private RobotController rc;

	public Navigation(RobotController rc) {
		this.rc = rc;
	}

	public boolean tryMoveToTarget(MapLocation target) {
		if (rc.canMove(randomDirection())) {
			int targetX = target.x;
			int targetY = target.y;
			return true;
		} else
			return false;
	}

	public Direction randomDirection() {
		return directions[(int) (Math.random() * directions.length)];
	}

	public boolean tryMove(Direction dir) throws GameActionException {
		System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " "
				+ rc.canMove(dir));
		if (rc.canMove(dir)) {
			rc.move(dir);
			return true;
		} else
			return false;
	}
}
