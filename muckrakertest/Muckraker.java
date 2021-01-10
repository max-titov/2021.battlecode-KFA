package muckrakertest;

import battlecode.common.*;

public class Muckraker extends Robot {
	/**
	 * Constants
	 */
	public final int REGULAR_MUCKRAKER = 1;

	/**
	 * Muckraker's attributes
	 */
	public int muckrakerType;
	public Direction heading;
	public Direction headingToRobot;
	public boolean robotInRange;

	/**
	 * Constructor Set to a Regular Muckraker at creation Set its heading based on
	 * whichever side of EC it is spawned Initialize any other variables
	 * 
	 * @param rc
	 */
	public Muckraker(RobotController rc) {
		super(rc);
		muckrakerType = REGULAR_MUCKRAKER;
		heading = nav.relativeLocToEC(currLoc);
		robotInRange = false;
	}

	/**
	 * Muckraker's take turn method Switch statement to execute different actions
	 * for different types of muckrakers
	 */
	public void takeTurn() throws GameActionException {
		super.takeTurn();
		// check message to see muckraker type
		switch (muckrakerType) {
			case REGULAR_MUCKRAKER:
				regularMuckraker();
				break;
		}
	}

	/**
	 * Regular Muckraker Detects a robot in its expose radius, if detected, kill the
	 * one with the highest conviction If none is detected, check for any robots in
	 * sense radius, if found move towards it Otherwise keep exploring map by moving
	 * towards heading
	 * 
	 * @throws GameActionException
	 */
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

	/**
	 * Finds the robot with the max conviction given an array of robots and exposes
	 * it if it can
	 * 
	 * @param robotsInSight
	 * @throws GameActionException
	 */
	public void exposeInExposeRad(RobotInfo[] robotsInSight) throws GameActionException {
		MapLocation exposeLoc = findMaxConv(robotsInSight);
		if (rc.canExpose(exposeLoc)) {
			rc.expose(exposeLoc);
		}
	}

	/**
	 * Finds the robot with the most conviction
	 * 
	 * @param robots
	 * @return
	 */
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