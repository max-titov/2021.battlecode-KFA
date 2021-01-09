package muckrakertest;

import battlecode.common.*;

public class Muckraker extends Robot {

	public final int REGULAR_MUCKRAKER = 1;

	public int muckrakerType;
	public Direction heading;
	public Direction headingToRobot;
	public boolean robotInRange;

	public Muckraker(RobotController rc) {
		super(rc);
		muckrakerType = REGULAR_MUCKRAKER;
		heading = nav.relativeLocToEC(currLoc);
		robotInRange = false;
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		// check message to see muckraker type
		switch (muckrakerType) {
			case REGULAR_MUCKRAKER:
				regularMuckraker();
				break;
		}
	}

	public void regularMuckraker() throws GameActionException {
		RobotInfo[] robotsInExpose = rc.senseNearbyRobots(12, opponentTeam);
		RobotInfo[] robotsInSense = rc.senseNearbyRobots(30, opponentTeam);
		if (robotsInExpose.length > 0) {
			exposeInExposeRad(robotsInExpose);
			// send message to EC
		} else if (robotsInSense.length > 0) {
			MapLocation robotLoc = findMaxConv(robotsInSense);
			Direction dirToRobot = currLoc.directionTo(robotLoc);
			nav.tryMove(dirToRobot);
		} else {
			// if edge of map detected report to EC and change heading
			nav.tryMove(heading);
		}
	}

	public void exposeInExposeRad(RobotInfo[] robotsInSight) throws GameActionException {
		MapLocation exposeLoc = findMaxConv(robotsInSight);
		if (rc.canExpose(exposeLoc)) {
			rc.expose(exposeLoc);
		}
	}

	public MapLocation findMaxConv(RobotInfo[] robots) {
		int indexOfMaxConv = 0;
		for (int i = 0; i < robots.length; i++) {
			RobotInfo ri = robots[i];
			if (ri.getType().equals(RobotType.SLANDERER)) {
				if (ri.getConviction() > robots[indexOfMaxConv].getConviction()) {
					indexOfMaxConv = i;
				}
			}
		}
		return robots[indexOfMaxConv].getLocation();
	}

}