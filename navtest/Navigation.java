package navtest;

import battlecode.common.*;

public class Navigation {

	public final Direction[] directions = Direction.allDirections();
	public final Direction N = Direction.NORTH;
	public final Direction NW = Direction.NORTHWEST;
	public final Direction W = Direction.WEST;
	public final Direction SW = Direction.SOUTHWEST;
	public final Direction S = Direction.SOUTH;
	public final Direction SE = Direction.SOUTHEAST;
	public final Direction E = Direction.EAST;
	public final Direction NE = Direction.NORTHEAST;

	public final Direction[] navCircle = new Direction[] { N, W, S, S, E, E, N, N, N, E, E, SE, S, S, SW, W, W, NW, N,
			N, N, NE, E, E, SE, SE, S, S, SW, SW, W, W, NW, NW, N, N, N, N, E, N, E, E, E, E, S, E, S, E, S, S, S, S, W,
			S, W, S, W, W, W, W, N, W, N, W, N, N, N, N, N, NW, NW, W, W, W, W, SW, SW, SW, S, S, S, S, SE, SE, SE, E,
			E, E, E, NE, NE, NE, N, N, N, N, N, NW, NW, NW, W, W, W, W, SW, SW, SW, SW, S, S, S, S, SE, SE, SE, SE, E,
			E, E, E, NE, NE, NE, NE, N, N, N, N };

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

	public double calcTurnsOfPath(double[] path) {
		double cooldown = 5.3;
		for (double passability : path) {
			cooldown += getBaseCooldown() / passability;
		}
		return (cooldown);
	}

	public double getBaseCooldown() {
		switch (rc.getType()) {
			case ENLIGHTENMENT_CENTER:
				return 2.0;
			case POLITICIAN:
				return 1.0;
			case SLANDERER:
				return 2.0;
			case MUCKRAKER:
				return 1.5;
			default:
				return -1;
		}
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
