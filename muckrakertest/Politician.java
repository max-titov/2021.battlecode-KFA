package muckrakertest;

import battlecode.common.*;

public class Politician extends Robot {

	public Politician(RobotController rc) throws GameActionException {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		nav.tryMove(nav.randomDirection());
	}

}