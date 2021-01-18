package sprintplayer;

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
	public RobotInfo[] robotsInExpose;
	public RobotInfo[] robotsInSense;
	// Explorer Muckraker
	public MapLocation target;
	public Direction heading;
	public int explorerType;
	public boolean edgeDetected;
	public int lastEdgeType = -1;
	// Harass Muckraker
	public MapLocation enemyEC;
	public MapLocation[] sitLocs;
	public MapLocation sitLoc;
	public int sitIndex;
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
			explorerType = BOUNCE_EXPLORER;
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
		switch (muckrakerType) {
			case EXPLORER_MUCKRAKER:
				break;
			case HARASS_MUCKRAKER:
				break;
		}
		robotsInExpose = rc.senseNearbyRobots(12, opponentTeam);
		robotsInSense = rc.senseNearbyRobots(30, opponentTeam);
		if (exposeOnSight()) {
			return;
		}
		if (roundNum >= ROUND_TO_START_HARASS && enemyEC != null) {
			muckrakerType = HARASS_MUCKRAKER;
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
		findEnemyEC();
		int[] edges = nav.lookForEdges();
		// comms.reportEdge
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
		System.out.println("Heading: " + heading + " Target: " + target);
		if (edges != null && !edgeDetected) {
			// raise flag telling it found edge and coordinates of edge
			edgeDetected = true;
			updateTargetAtEdge(edges);
			lastEdgeType = edges[0];
		}
		nav.tryMoveToTarget(target);
	}

	/**
	 * Harass Muckraker; Muckraker that sits near enemy EC killing any slanders
	 * 
	 * @throws GameActionException
	 */
	public void harassMuckraker() throws GameActionException {
		if (sitLocs == null) {
			generateSitLocations();
		}
		if (sitLoc == null) {
			sitLoc = sitLocs[sitIndex];
		}
		if (currLoc.equals(sitLoc)) {
			return;
		}
		if (!rc.canSenseLocation(enemyEC)) {
			nav.tryMoveToTarget(enemyEC);
			return;
		}
		while (!rc.canSenseLocation(sitLoc) || rc.isLocationOccupied(sitLoc)) {
			sitIndex++;
			if (sitIndex == sitLocs.length) {
				sitIndex = 0;
				break;
			}
			sitLoc = sitLocs[sitIndex];
		}
		nav.tryMoveToTarget(sitLoc);
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
			if (exposeLoc == null) {
				return false;
			}
			if (rc.canExpose(exposeLoc)) {
				rc.expose(exposeLoc);
			}
			// comms.sendMessage("exposed a slander of influence X")
			return false;
		} else if (robotsInSense.length > 0) {
			MapLocation robotLoc = findMaxConv(true);
			if (robotLoc == null) {
				return false;
			}
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
			boolean foundSlanderer = false;
			for (int i = 0; i < robotsInSense.length; i++) {
				RobotInfo ri = robotsInSense[i];
				if (ri.getType().equals(RobotType.SLANDERER)) {
					foundSlanderer = true;
					if (ri.getConviction() > robotsInSense[indexOfMaxConv].getConviction()) {
						indexOfMaxConv = i;
					}
				}
			}
			return foundSlanderer ? robotsInSense[indexOfMaxConv].getLocation() : null;
		} else {
			int indexOfMaxConv = 0;
			boolean foundSlanderer = false;
			for (int i = 0; i < robotsInExpose.length; i++) {
				RobotInfo ri = robotsInExpose[i];
				if (ri.getType().equals(RobotType.SLANDERER)) {
					foundSlanderer = true;
					if (ri.getConviction() > robotsInExpose[indexOfMaxConv].getConviction()) {
						indexOfMaxConv = i;
					}
				}
			}
			return foundSlanderer ? robotsInSense[indexOfMaxConv].getLocation() : null;
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
				// System.out.println("Found enemy EC");
				enemyEC = ri.getLocation();
				return;
			}
		}
		if (muckrakerType == HARASS_MUCKRAKER) {
			// check comms for enemyEC
		}
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
				if (!iterDirection.equals(heading.opposite())) {
					if (j == 2) {
						break;
					}
					possibleDirections[j] = iterDirection;
					j++;
				}
				iterDirection = iterDirection.rotateRight();
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

	/**
	 * creates an array of map locations around the enemy EC
	 */
	public void generateSitLocations() {
		sitLocs = new MapLocation[8];
		int x = enemyEC.x;
		int y = enemyEC.y;
		sitLocs[0] = new MapLocation(x, y + 1);
		sitLocs[1] = new MapLocation(x + 1, y + 1);
		sitLocs[2] = new MapLocation(x + 1, y);
		sitLocs[3] = new MapLocation(x + 1, y - 1);
		sitLocs[4] = new MapLocation(x, y - 1);
		sitLocs[5] = new MapLocation(x - 1, y - 1);
		sitLocs[6] = new MapLocation(x - 1, y);
		sitLocs[7] = new MapLocation(x - 1, y + 1);
	}
}