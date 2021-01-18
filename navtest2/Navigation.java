package navtest2;

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

	public final Direction[] navCircle = { N, E, S, S, W, W, N, N, N, E, E, SE, S, S, SW, W, W, NW, N, N, N, NE, E, E,
			SE, SE, S, S, SW, SW, W, W, NW, NW, N, N, N, N, E, N, E, E, E, E, S, E, S, E, S, S, S, S, W, S, W, S, W, W,
			W, W, N, W, N, W, N, N, N, N, N, NE, NE, E, E, E, E, SE, SE, SE, S, S, S, S, SW, SW, SW, W, W, W, W, NW, NW,
			NW, N, N, N, N };
	// N,NE,NE,NE,W,W,W,W,SE,SE,SE,SE,S,S,S,S,SW,SW,SW,SW,W,W,W,W,NW,NW,NW,NW,N,N,N,N};

	public float[][] navHeatMap = new float[11][11];
	public RobotController rc;

	public Navigation(RobotController r) {
		this.rc = r;
	}

	public double approxTurnsToTarget(MapLocation loc, MapLocation target) throws GameActionException {
		int xdiff = Math.abs(loc.x - target.x);
		int ydiff = Math.abs(loc.y - target.y);
		if (xdiff > ydiff)
			return 1 / rc.sensePassability(loc) + ydiff / 0.5;
		return 1 / rc.sensePassability(loc) + xdiff / 0.5;
	}

	public void generateHeatMap() throws GameActionException {
		int navCircleLen = navCircle.length;
		MapLocation tempLoc = rc.getLocation();
		for (int i = 0; i < navCircleLen; i++) {
			tempLoc = tempLoc.add(navCircle[i]);

		}
	}

	/**
	 * returns a random direction
	 * 
	 * @return
	 */
	public Direction randomDirection() {
		return directions[(int) (Math.random() * directions.length)];
	}

	/**
	 * tries to move in a direction if it can
	 * 
	 * @param dir
	 * @return
	 * @throws GameActionException
	 */
	public boolean tryMove(Direction dir) throws GameActionException {
		if (rc.canMove(dir)) {
			rc.move(dir);
			return true;
		} else
			return false;
	}

}
