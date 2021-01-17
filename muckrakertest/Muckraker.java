package muckrakertest;

import battlecode.common.*;

public class Muckraker extends Robot {
	/**
	 * Constants
	 */
	public final int ROUND_TO_START_HARASS = 500;
	public final int EXPLORER_MUCKRAKER = 1;
	public final int HARASS_MUCKRAKER = 2;
	public final int BOUNCE_EXPLORER = 1;
	public final int EDGE_EXPLORER = 2;

	/**
	 * Muckraker's attributes
	 */
	public int muckrakerType;
	RobotInfo[] robotsInExpose;
	RobotInfo[] robotsInSense;
	// Explorer Muckraker
	public MapLocation target;
	public Direction heading;
	public int explorerType;
	public boolean edgeDetected;
	public int lastEdgeType = -1;
	// Harass Muckraker
	public MapLocation enemyEC;
	public MapLocation[] enemyECCircle;
	public int enemyECCircleIndex;

	/**
	 * Constructor Set muckraker type to a either a bounce or edge explorer
	 * muckraker if before a certain round number Set its heading and target based
	 * on whichever side of EC it is spawned; initialize any other variables
	 * 
	 * @param rc
	 * @throws GameActionException
	 */
	public Muckraker(RobotController rc) throws GameActionException {
		super(rc);
		robotsInExpose = rc.senseNearbyRobots(12, opponentTeam);
		robotsInSense = rc.senseNearbyRobots(30, opponentTeam);
		if (roundNum < ROUND_TO_START_HARASS) {
			muckrakerType = EXPLORER_MUCKRAKER;
			target = getTargetRelativeEC();
			if (coinFlip()) {
				explorerType = EDGE_EXPLORER;
			} else {
				explorerType = BOUNCE_EXPLORER;
			}
		} else {
			muckrakerType = HARASS_MUCKRAKER;
		}
	}

	/**
	 * Override Robot's take turn method for muckraker Expose any slanders if
	 * possible or move towards one to get in range; check for any enemy ECs and
	 * save location; switch statement to execute code based on muckraker type
	 * 
	 * @throws GameActionException
	 */
	public void takeTurn() throws GameActionException {
		super.takeTurn();
		robotsInExpose = rc.senseNearbyRobots(12, opponentTeam);
		robotsInSense = rc.senseNearbyRobots(30, opponentTeam);
		if (exposeOnSight()) {
			return;
		}
		if (enemyEC == null) {
			findEnemyEC();
		}
		switch (muckrakerType) {
			case EXPLORER_MUCKRAKER:
				explorerMuckraker();
				break;
			case HARASS_MUCKRAKER:
				harassMuckraker();
				break;
		}
	}

	/**
	 * Explorer Muckraker; reports any edges or corners to EC; updates target and
	 * heading if at edge; does not update target unless its a new edge
	 * 
	 * @throws GameActionException
	 */
	public void explorerMuckraker() throws GameActionException {
		// comms.reportEdge
		int[] edges = nav.lookForEdges();
		if (edges == null) {
			edgeDetected = false;
		} else if (edges[0] != lastEdgeType) {
			if (lastEdgeType % 2 == 1) {
				if (edges[0] == ((lastEdgeType + 9) % 8) || edges[0] == ((lastEdgeType + 7) % 8)) {
					lastEdgeType = edges[0];
				} else {
					edgeDetected = false;
				}
			} else {
				edgeDetected = false;
			}
		} else {
			edgeDetected = true;
		}
		if (edges != null && !edgeDetected) {
			// raise flag telling it found edge and coordinates of edge
			edgeDetected = true;
			updateTargetAtEdge(edges);
			lastEdgeType = edges[0];
		}
		System.out.println(heading);
		nav.tryMoveToTarget(target);
	}

	/**
	 * Harass Muckraker; Muckraker that sits near enemy EC killing any slanders
	 * 
	 * @throws GameActionException
	 */
	public void harassMuckraker() throws GameActionException {
		generateSitLocations();
	}

	/**
	 * Method for all muckrakers to expose any slanderers if detected or move
	 * towards slanderer if not in range
	 * 
	 * @return true if it moved towards an enemy slanderer
	 * @throws GameActionException
	 */
	public boolean exposeOnSight() throws GameActionException {
		if (robotsInExpose.length > 0) {
			MapLocation exposeLoc = findMaxConv(false);
			if (rc.canExpose(exposeLoc)) {
				rc.expose(exposeLoc);
			}
			// comms.sendMessage("exposed a slander of influence X")
			return false;
		} else if (robotsInSense.length > 0) {
			MapLocation robotLoc = findMaxConv(true);
			nav.tryMoveToTarget(robotLoc);
			return true;
		}
		return false;
	}

	/**
	 * Finds the robot with the most conviction in either sensor radius (if
	 * checkMaxRadius is true) or expose radius (if checkMaxRadius is false)
	 * 
	 * @param checkMaxRadius
	 * @return
	 */
	public MapLocation findMaxConv(boolean checkMaxRadius) {
		if (checkMaxRadius) {
			int indexOfMaxConv = 0;
			for (int i = 0; i < robotsInSense.length; i++) {
				RobotInfo ri = robotsInSense[i];
				if (ri.getType().equals(RobotType.SLANDERER)) {
					if (ri.getConviction() > robotsInSense[indexOfMaxConv].getConviction()) {
						indexOfMaxConv = i;
					}
				}
			}
			return robotsInSense[indexOfMaxConv].getLocation();
		} else {
			int indexOfMaxConv = 0;
			for (int i = 0; i < robotsInExpose.length; i++) {
				RobotInfo ri = robotsInExpose[i];
				if (ri.getType().equals(RobotType.SLANDERER)) {
					if (ri.getConviction() > robotsInExpose[indexOfMaxConv].getConviction()) {
						indexOfMaxConv = i;
					}
				}
			}
			return robotsInExpose[indexOfMaxConv].getLocation();
		}
	}

	/**
	 * checks for the enemy EC and saves it to a variable
	 * 
	 */
	public void findEnemyEC() {
		for (int i = 0; i < robotsInSense.length; i++) {
			RobotInfo ri = robotsInSense[i];
			if (ri.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
				System.out.println("Found enemy EC");
				enemyEC = ri.getLocation();
				return;
			}
		}
		// check comms for enemyEC
	}

	/**
	 * Finds a target location based on direction relative to the EC
	 * 
	 * @return MapLocation
	 */
	public MapLocation getTargetRelativeEC() {
		heading = nav.relativeLocToEC();
		return new MapLocation(currLoc.x + heading.dx * 64, currLoc.y + heading.dy * 64);
	}

	/**
	 * Figure out the new target location based on type of muckraker
	 * 
	 * @param edges
	 */
	public void updateTargetAtEdge(int[] edges) {
		int edgeType = edges[0];
		MapLocation cornerEdgeLoc = new MapLocation(edges[1], edges[2]);
		Direction directionToCornerEdge = nav.edgeTypeToDir(edgeType);
		updateHeadingAtEdge(cornerEdgeLoc, edgeType, directionToCornerEdge);
		target = new MapLocation(currLoc.x + (heading.dx * 64), currLoc.y + (heading.dy * 64));
	}

	/**
	 * Figure out the new heading based on type of muckraker; if it is a bounce
	 * type, pick one of the possible bounce directions; if it is a edge type turn
	 * 90 degrees randomly
	 * 
	 * @param cornerEdgeLoc
	 * @param edgeType
	 * @param directionToCornerEdge
	 */
	public void updateHeadingAtEdge(MapLocation cornerEdgeLoc, int edgeType, Direction directionToCornerEdge) {
		if (explorerType == BOUNCE_EXPLORER) {
			Direction iterDirection = directionToCornerEdge.rotateRight().rotateRight().rotateRight();
			Direction[] possibleDirections = new Direction[2];
			// iterate through possible directions and add them to array, ignore if it is
			// the direction it just came from
			int j = 0;
			for (int i = 0; i <= possibleDirections.length; i++) {
				System.out.println("Iter direction: " + iterDirection.toString());
				if (iterDirection.equals(directionToCornerEdge.opposite())) {
					iterDirection = iterDirection.rotateRight();
					continue;
				}
				possibleDirections[j] = iterDirection;
				iterDirection = iterDirection.rotateRight();
				j++;
			}
			// pick one of the random directions and calculate target in that direction
			heading = possibleDirections[(int) (Math.random() * possibleDirections.length)];
		} else {
			// check if edge type is an edge rotate heading randomly 90 degree clockwise or
			// counterclockwise
			if (edgeType % 2 == 0) {
				if (coinFlip()) {
					heading = directionToCornerEdge.rotateRight().rotateRight();
				} else {
					heading = directionToCornerEdge.rotateLeft().rotateLeft();
				}
				// check if heading is a cardinal and muckraker is coming from EC
			} else {
				if (heading == directionToCornerEdge.rotateLeft().rotateLeft().rotateLeft().opposite()) {
					heading = directionToCornerEdge.rotateRight().rotateRight().rotateRight();
				} else {
					heading = directionToCornerEdge.rotateLeft().rotateLeft().rotateLeft();
				}
			}
		}
	}
}