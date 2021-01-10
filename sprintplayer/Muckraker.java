package sprintplayer;

import battlecode.common.*;

public class Muckraker extends Robot {
	/**
	 * Constants
	 */
	public final int EXPLORER_MUCKRAKER = 1;
	public final int HARASS_MUCKRAKER = 1;

	/**
	 * Muckraker's attributes
	 */
	public int muckrakerType;
	public MapLocation target;
	public Direction headingToRobot;

	/**
	 * Constructor Set to a Regular Muckraker at creation Set its heading based on
	 * whichever side of EC it is spawned Initialize any other variables
	 * 
	 * @param rc
	 */
	public Muckraker(RobotController rc) {
		super(rc);
		if (roundNum < ROUND_TO_START_DEFENSE) {
			muckrakerType = EXPLORER_MUCKRAKER;
		} else {
			muckrakerType = EXPLORER_MUCKRAKER;
		}
		// check EC to see if heading should change
		if (muckrakerType == EXPLORER_MUCKRAKER) {
			target = getTargetRelativeEC();
		}
	}

	/**
	 * Muckraker's take turn method Switch statement to execute different actions
	 * for different types of muckrakers
	 */
	public void takeTurn() throws GameActionException {
		super.takeTurn();
		// check message to see muckraker type
		RobotInfo[] robotsInExpose = rc.senseNearbyRobots(12, opponentTeam);
		RobotInfo[] robotsInSense = rc.senseNearbyRobots(30, opponentTeam);
		exposeOnSight(robotsInExpose, robotsInSense);
		MapLocation enemyEC = findEnemyEC(robotsInSense);
		if (enemyEC != null && muckrakerType == EXPLORER_MUCKRAKER) {
			// send enemy EC coordinates
			muckrakerType = HARASS_MUCKRAKER;
		}
		switch (muckrakerType) {
			case EXPLORER_MUCKRAKER:
				explorerMuckraker(robotsInSense);
				break;
		}
	}

	/**
	 * Muckraker that looks for map edges and neutral ECs
	 * 
	 * @throws GameActionException
	 */
	public void explorerMuckraker(RobotInfo[] robotsInSight) throws GameActionException {
		// if edge is detected report location to EC if EC does not know map corners
		// yet, and change target
		nav.tryMoveToTarget(target);
	}

	/**
	 * Muckraker that circles enemy EC killing any slanders
	 * 
	 * @throws GameActionException
	 */
	public void harassMuckraker(MapLocation enemyEC) throws GameActionException {
		// circle enemy EC
	}

	/**
	 * Muckraker that fills a lattice defense formation
	 * 
	 * @throws GameActionException
	 */
	public void defenseMuckraker(RobotInfo[] robotsInSight) throws GameActionException {
		// calculate lattice and find open slots
		// if all slots are filled, change to explore muckraker
	}

	/**
	 * Method for all muckrakers to kill any slanders if detected
	 * 
	 * @throws GameActionException
	 */
	public void exposeOnSight(RobotInfo[] robotsInExpose, RobotInfo[] robotsInSense) throws GameActionException {
		if (robotsInExpose.length > 0) {
			exposeMaxConv(robotsInExpose);
			// send message to EC
		} else if (robotsInSense.length > 0) {
			MapLocation robotLoc = findMaxConv(robotsInSense);
			nav.tryMoveToTarget(robotLoc);
		}
	}

	/**
	 * Finds the robot with the max conviction given an array of robots and exposes
	 * it if it can
	 * 
	 * @param robotsInSight
	 * @throws GameActionException
	 */
	public void exposeMaxConv(RobotInfo[] robotsInSight) throws GameActionException {
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

	/**
	 * checks for the enemy EC
	 * 
	 * @param robots
	 * @return
	 */
	public MapLocation findEnemyEC(RobotInfo[] robots) {
		for (int i = 0; i < robots.length; i++) {
			RobotInfo ri = robots[i];
			if (ri.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
				return ri.getLocation();
			}
		}
		return null;
	}

	/**
	 * Finds a target location based on direction relative to the EC
	 * 
	 * @return
	 */
	public MapLocation getTargetRelativeEC() {
		int dx = 0;
		int dy = 0;
		RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
		for (int i = 0; i < robots.length; i++) {
			RobotInfo ri = robots[i];
			if (ri.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
				dx = currLoc.x - ri.getLocation().x;
				dy = currLoc.x - ri.getLocation().y;
			}
		}
		return new MapLocation(dx * 64, dy * 64);
	}

}