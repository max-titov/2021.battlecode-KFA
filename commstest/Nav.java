package commstest;

import battlecode.common.*;

public class Nav {

	static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

	static RobotController rc;

	public Nav(RobotController r) {
		this.rc = r;
	}
}
