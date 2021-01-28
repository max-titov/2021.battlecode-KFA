package sprintplayer;

import battlecode.common.*;

public class Muckraker extends Robot {
	/**
	 * Constants
	 */
	public final int EXPLORER_MUCKRAKER = 1;
	public final int HARASS_MUCKRAKER = 2;
	public final int DEFENSE_MUCKRAKER = 3;
	public final int BOUNCE_EXPLORER = 1;
	public final int EDGE_EXPLORER = 2;

	/**
	 * Muckraker's attributes
	 */
	public int muckrakerType;
	public RobotInfo[] robotsInExpose;
	public RobotInfo[] robotsInSense;
	public RobotInfo[] enemyBots;
	public RobotInfo[] alliedBots;
	public boolean beginAttack;
	// Explorer Muckraker
	public MapLocation target;
	public Direction heading;
	public int explorerType;
	public boolean edgeDetected;
	public int lastEdgeType = -1;
	public boolean moveToCorner;
	// Harass Muckraker
	public MapLocation enemyEC;
	public MapLocation[] sitLocs;
	public MapLocation sitLoc;
	public int sitIndex;
	public int enemyECCircleIndex;
	public boolean clockwise;

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
		muckrakerType = EXPLORER_MUCKRAKER;
		target = getTargetRelativeEC();
		if (coinFlip()) {
			clockwise = true;
		}
		if (roundNum < EnlightenmentCenter.ROUND_TO_START_ATTACK) {
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
		comms.dropFlag();
		int[] message = comms.readMessage(myECid);
		// if (message != null && message[0] == Comms.HELP) {
		// muckrakerType = DEFENSE_MUCKRAKER
		// }
		if (message != null && message[0] == Comms.FOUND_EC && message[1] == 1 && coinFlip(.5)) {
			beginAttack = true;
			if (enemyEC == null) {
				int targetXOffset = message[3];
				int targetYOffset = message[4];
				int targetX = myECLoc.x + targetXOffset;
				int targetY = myECLoc.y + targetYOffset;
				enemyEC = new MapLocation(targetX, targetY);
			}
		}
		alliedBots = rc.senseNearbyRobots(sensorRadSq, myTeam);
		enemyBots = rc.senseNearbyRobots(sensorRadSq, opponentTeam);
		robotsInExpose = rc.senseNearbyRobots(12, opponentTeam);
		robotsInSense = rc.senseNearbyRobots(30, opponentTeam);
		// check if the enemy EC is no longer an enemy EC
		if (enemyEC != null && rc.canSenseLocation(enemyEC)
				&& !rc.senseRobotAtLocation(enemyEC).team.equals(opponentTeam)) {
			enemyEC = null;
		}
		if (beginAttack && enemyEC != null) {
			muckrakerType = HARASS_MUCKRAKER;
		}
		if (cooldownTurns >= 1) {
			return;
		}
		if (exposeOnSight()) {
			return;
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
			if (!moveToCorner) {
				int edgeType = edges[0];
				MapLocation cornerEdgeLoc = new MapLocation(edges[1], edges[2]);
				Direction directionToCornerEdge = nav.edgeTypeToDir(edgeType);
				if (!rc.canSenseLocation(cornerEdgeLoc)) {
					System.out.println("Moving to corner " + nav.tryMove(directionToCornerEdge));
					moveToCorner = true;
					return;
				}
			}
			comms.sendFoundEdgeMessage(edges[0], edges[1], edges[2]);
			edgeDetected = true;
			updateTargetAtEdge(edges);
			lastEdgeType = edges[0];
		}
		System.out.println("Target: " + target + " Heading: " + heading);
		nav.tryMoveToTarget(target);
		findEC();
	}

	/**
	 * Harass Muckraker; Muckraker that sits near enemy EC killing any slanders
	 * 
	 * @throws GameActionException
	 */
	public void harassMuckraker() throws GameActionException {
		if (enemyEC == null) {
			muckrakerType = EXPLORER_MUCKRAKER;
			explorerMuckraker();
		}
		// rc.setIndicatorDot(enemyEC, 0, 255, 255);
		// System.out.println("target EC at "+enemyEC);

		int alliedBotsLen = alliedBots.length;
		int nearestPoliticianDist = 1000;
		RobotInfo nearestPolitician = null;
		for (int i = 0; i < alliedBotsLen; i++) {
			RobotInfo ri = alliedBots[i];
			if (ri.type.equals(RobotType.POLITICIAN)
					&& currLoc.distanceSquaredTo(ri.location) < nearestPoliticianDist) {
				nearestPolitician = ri;
				nearestPoliticianDist = currLoc.distanceSquaredTo(ri.location);
			}
		}
		if (nearestPolitician != null && currLoc.isWithinDistanceSquared(enemyEC, 40)) {
			nav.tryMoveToTarget(nearestPolitician.location.directionTo(currLoc));
			// rc.setIndicatorLine(currLoc,
			// currLoc.add(nearestPolitician.location.directionTo(currLoc)), 255, 0, 255);
			// rc.setIndicatorDot(nearestPolitician.location, 255, 0, 255);
			return;
		}

		int sqDistToEC = currLoc.distanceSquaredTo(enemyEC);
		Direction dirToEC = currLoc.directionTo(enemyEC);

		Direction moveDir = dirToEC;
		if (sqDistToEC < 11) {
			moveDir = rotate(rotate(rotate(dirToEC)));
		} else if (sqDistToEC > 30) {
			moveDir = dirToEC;
		} else if (sqDistToEC > 20) {
			moveDir = (rotate(dirToEC));
		} else {
			moveDir = rotate(rotate(dirToEC));
		}

		if (!rc.onTheMap(currLoc.add(moveDir).add(moveDir))) {
			clockwise = !clockwise;
			return;
		}
		rc.setIndicatorLine(currLoc, currLoc.add(moveDir), 0, 255, 255);
		nav.bugNav(moveDir);
	}

	/**
	 * used when circling the enemy ec
	 * 
	 * @param dir
	 * @return
	 * @throws GameActionException
	 */
	public Direction rotate(Direction dir) throws GameActionException {
		if (clockwise) {
			return dir.rotateRight();
		}
		return dir.rotateLeft();
	}

	/**
	 * Method for all muckrakers to expose any slanderers if detected or move
	 * towards slanderer if not in range
	 * 
	 * @return true if it moved towards an enemy slanderer
	 * @throws GameActionException
	 */
	public boolean exposeOnSight() throws GameActionException {
		int indexOfMaxConv = -1;
		boolean foundSlanderer = false;
		for (int i = 0; i < robotsInExpose.length; i++) {
			RobotInfo ri = robotsInExpose[i];
			if (ri.type.equals(RobotType.SLANDERER)) {
				foundSlanderer = true;
				if (indexOfMaxConv == -1) {
					indexOfMaxConv = i;
				} else if (ri.conviction > robotsInExpose[indexOfMaxConv].conviction) {
					indexOfMaxConv = i;
				}
			}
		}
		if (foundSlanderer) {
			if (rc.canExpose(robotsInExpose[indexOfMaxConv].location)) {
				rc.expose(robotsInExpose[indexOfMaxConv].location);
				return true;
			}
		}
		for (int i = 0; i < robotsInSense.length; i++) {
			RobotInfo ri = robotsInSense[i];
			if (ri.type.equals(RobotType.SLANDERER)) {
				foundSlanderer = true;
				if (indexOfMaxConv == -1) {
					indexOfMaxConv = i;
				} else if (ri.conviction > robotsInSense[indexOfMaxConv].conviction) {
					indexOfMaxConv = i;
				}
			}
		}
		if (foundSlanderer) {
			nav.tryMoveToTarget(robotsInSense[indexOfMaxConv].location);
			return true;
		}
		return false;

	}

	/**
	 * checks for the enemy EC and saves it to a variable
	 * 
	 * @throws GameActionException
	 * 
	 */
	public void findEC() throws GameActionException {
		for (int i = 0; i < robotsInSense.length; i++) {
			RobotInfo ri = robotsInSense[i];
			if (ri.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
				comms.sendFoundECMessage(ri);
				enemyEC = ri.location;
				break;
			}
		}
		RobotInfo[] neutralRobots = rc.senseNearbyRobots(30, Team.NEUTRAL);
		for (int i = 0; i < neutralRobots.length; i++) {
			RobotInfo ri = neutralRobots[i];
			if (ri.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
				comms.sendFoundECMessage(ri);
				break;
			}
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
	 * @throws GameActionException
	 */
	public void updateTargetAtEdge(int[] edges) throws GameActionException {
		int edgeType = edges[0];
		MapLocation cornerEdgeLoc = new MapLocation(edges[1], edges[2]);
		Direction directionToCornerEdge = nav.edgeTypeToDir(edgeType);
		updateHeadingAtEdge(cornerEdgeLoc, edgeType, directionToCornerEdge);
		target = new MapLocation(currLoc.x + (heading.dx * 64), currLoc.y + (heading.dy * 64));
		moveToCorner = false;
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