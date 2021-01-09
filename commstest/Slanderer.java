package commstest;

import battlecode.common.*;

public class Slanderer extends Robot {

	public Slanderer(RobotController r) {
		super(r);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		tryMove(randomDirection());
	}

}