package muckrakertest;

import battlecode.common.*;

public class Navigation {
	/**
	 * Constants
	 */
	public final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };
	public final int directionsLen = directions.length;

	public final Direction N = Direction.NORTH;
	public final Direction NW = Direction.NORTHWEST;
	public final Direction W = Direction.WEST;
	public final Direction SW = Direction.SOUTHWEST;
	public final Direction S = Direction.SOUTH;
	public final Direction SE = Direction.SOUTHEAST;
	public final Direction E = Direction.EAST;
	public final Direction NE = Direction.NORTHEAST;

	public final int NORTH_INT = 0;
	public final int NORTHEAST_INT = 1;
	public final int EAST_INT = 2;
	public final int SOUTHEAST_INT = 3;
	public final int SOUTH_INT = 4;
	public final int SOUTHWEST_INT = 5;
	public final int WEST_INT = 6;
	public final int NORTHWEST_INT = 7;

	/**
	 * Navigation attributes
	 */
	public RobotController rc;
	public MapLocation currLoc;
	public MapLocation myECLoc;
	public int noReturnLocLen = 13;
	public MapLocation[] previousLocs = new MapLocation[noReturnLocLen];
	public boolean DEBUG = false;

	/**
	 * Constructor
	 * 
	 * @param rc
	 */
	public Navigation(RobotController rc, MapLocation currLoc, MapLocation myECLoc) {
		this.rc = rc;
		this.currLoc = currLoc;
		this.myECLoc = myECLoc;
	}

	/**
	 * Uses pathfinding to find the most efficient route to reach a target location
	 * 
	 * @param target
	 * @throws GameActionException
	 */
	public void tryMoveToTarget(MapLocation target) throws GameActionException {
		if (currLoc.equals(target)) {
			// if at target, reset previous locations array
			previousLocs = new MapLocation[noReturnLocLen];
			return;
		}
		// look at adj 8 locations and calc best squares
		double[] adjEfficiency = getAdjEfficiencyMap(target);
		Direction[] bestDirsToMove = new Direction[directionsLen];
		// sort the adj locations to move best to worst
		for (int i = 0; i < directionsLen; i++) {
			int maxIndex = 0;
			for (int j = 0; j < directionsLen; j++) {
				if (adjEfficiency[j] > adjEfficiency[maxIndex]) {
					maxIndex = j;
				}
			}
			bestDirsToMove[i] = directions[maxIndex];
			adjEfficiency[maxIndex] = -999999;
		}
		if (DEBUG) {
			// show the previous locations
			for (int i = 0; i < noReturnLocLen; i++) {
				if (previousLocs[i] != null) {
					rc.setIndicatorDot(previousLocs[i], i * 20, 255 - i * 20, 255 - i * 20);
				}
			}
		}
		// try to move to an adj location, from best to worst
		for (int i = 0; i < directionsLen; i++) {
			Direction tryMoveDirection = bestDirsToMove[i];
			if (tryMoveAvoidPrevLocs(tryMoveDirection)) {
				shiftPrevLocArray(); // shifts list of previously visited locations
				break;
			}
		}
	}

	/**
	 * THIS DOES NOT WORK RIGHT NOW Uses selection sort in pathfinding to find most
	 * efficient route to reach a target location
	 * 
	 * @param target
	 * @throws GameActionException
	 */
	public void tryMoveToTargetSelectionSort(MapLocation target) throws GameActionException {
		if (!currLoc.equals(target)) {
			double[] adjEfficiency = getAdjEfficiencyMap(target);
			Direction[] bestDirsToMove = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
					Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };
			int directionsLenMinusOne = directionsLen - 1;
			printList(adjEfficiency);
			for (int i = 0; i < directionsLenMinusOne; i++) {
				int maxIndex = i + 1;
				for (int j = i + 1; j < directionsLen; j++) {
					if (adjEfficiency[j] >= adjEfficiency[maxIndex]) {
						maxIndex = j;
					}
				}
				double swapEfficiency = adjEfficiency[maxIndex];
				Direction swapDir = bestDirsToMove[maxIndex];
				adjEfficiency[maxIndex] = adjEfficiency[i];
				bestDirsToMove[maxIndex] = bestDirsToMove[i];
				adjEfficiency[i] = swapEfficiency;
				bestDirsToMove[i] = swapDir;
			}
			printList(bestDirsToMove);
			for (int i = 0; i < directionsLen; i++) {
				Direction tryMoveDirection = bestDirsToMove[i];
				if (tryMove(tryMoveDirection)) {
					break;
				}
			}
		}
	}

	/**
	 * Prints a list
	 * 
	 * @param list
	 */
	public void printList(Object[] list) {
		for (int i = 0; i < list.length; i++) {
			System.out.print(list[i].toString() + " ");
		}
		System.out.println();
	}

	/**
	 * Prints a list
	 * 
	 * @param list
	 */
	public void printList(double[] list) {
		for (int i = 0; i < list.length; i++) {
			System.out.print(String.format("%.2g%n", list[i]) + " ");
		}
		System.out.println();
	}

	/**
	 * Returns an array of efficiencies of all adjacent tiles depending on target
	 * location
	 * 
	 * @param target
	 * @return
	 * @throws GameActionException
	 */
	public double[] getAdjEfficiencyMap(MapLocation target) throws GameActionException {
		double[] efficiencies = new double[8];
		double passabilityWeight = 1.3;
		double directionWeight = 0.75;
		double directionalBias = 1.4;
		for (int i = 0; i < 8; i++) {
			MapLocation testLoc = currLoc.add(directions[i]);
			if (!rc.onTheMap(testLoc)) {
				efficiencies[i] = -99999;
				continue;
			}
			double passability = rc.sensePassability(testLoc);
			double directionalAccuracy = Math.sqrt(currLoc.distanceSquaredTo(target))
					- Math.sqrt(testLoc.distanceSquaredTo(target));
			efficiencies[i] = passabilityWeight * passability
					+ directionWeight * (directionalAccuracy + directionalBias);
		}
		return efficiencies;
	}

	/**
	 * Gets an array of passabilities of adjacent tiles
	 * 
	 * @return
	 * @throws GameActionException
	 */
	public double[] getAdjPassabilityMap() throws GameActionException {
		double[] passabilities = new double[8];
		for (int i = 0; i < 8; i++) {
			passabilities[i] = rc.sensePassability(currLoc.add(directions[i]));
		}
		return passabilities;
	}

	/**
	 * Finds location relative to EC
	 * 
	 * @return
	 */
	public Direction relativeLocToEC() {
		return myECLoc.directionTo(currLoc);
	}

	/**
	 * Calculates the turns it would take to move on a path given the passabilities
	 * of the tiles in the path
	 * 
	 * @param pathPassabilities
	 * @return
	 */
	public double calcTurnsOfPath(double[] pathPassabilities) {
		double cooldown = rc.getCooldownTurns();
		for (int i = 0; i < pathPassabilities.length; i++) {
			cooldown += getBaseCooldown() / pathPassabilities[i];
		}
		return cooldown;
	}

	/**
	 * looks for edges
	 * 
	 * @return
	 * @throws GameActionException
	 */
	public int[] lookForEdges() throws GameActionException {
		int currX = currLoc.x;
		int currY = currLoc.y;
		int cardinalSensorRadius = getCardinalSensorRadius();

		MapLocation checkNorth = new MapLocation(currX, currY + cardinalSensorRadius);
		MapLocation checkEast = new MapLocation(currX + cardinalSensorRadius, currY);
		MapLocation checkSouth = new MapLocation(currX, currY - cardinalSensorRadius);
		MapLocation checkWest = new MapLocation(currX - cardinalSensorRadius, currY);

		boolean northNotOnMap = !rc.onTheMap(checkNorth);
		boolean eastNotOnMap = !rc.onTheMap(checkEast);
		boolean southNotOnMap = !rc.onTheMap(checkSouth);
		boolean westNotOnMap = !rc.onTheMap(checkWest);

		if (northNotOnMap) {
			MapLocation testLoc = new MapLocation(checkNorth.x, checkNorth.y - 1);
			while (!rc.onTheMap(testLoc)) {
				testLoc = new MapLocation(testLoc.x, testLoc.y - 1);
			}
			checkNorth = new MapLocation(testLoc.x, testLoc.y);
		}
		if (eastNotOnMap) {
			MapLocation testLoc = new MapLocation(checkEast.x - 1, checkEast.y);
			while (!rc.onTheMap(testLoc)) {
				testLoc = new MapLocation(testLoc.x - 1, testLoc.y);
			}
			checkEast = new MapLocation(testLoc.x, testLoc.y);
		}
		if (southNotOnMap) {
			MapLocation testLoc = new MapLocation(checkSouth.x, checkSouth.y + 1);
			while (!rc.onTheMap(testLoc)) {
				testLoc = new MapLocation(testLoc.x, testLoc.y + 1);
			}
			checkSouth = new MapLocation(testLoc.x, testLoc.y);
		}
		if (westNotOnMap) {
			MapLocation testLoc = new MapLocation(checkWest.x + 1, checkWest.y);
			while (!rc.onTheMap(testLoc)) {
				testLoc = new MapLocation(testLoc.x + 1, testLoc.y);
			}
			checkWest = new MapLocation(testLoc.x, testLoc.y);
		}

		MapLocation locationToSend = null;
		int typeOfEdge = -1;
		if (northNotOnMap) {
			if (eastNotOnMap) {
				// northeast corner
				locationToSend = new MapLocation(checkEast.x, checkNorth.y);
				typeOfEdge = NORTHEAST_INT;
			} else if (westNotOnMap) {
				// northwest corner
				locationToSend = new MapLocation(checkWest.x, checkNorth.y);
				typeOfEdge = NORTHWEST_INT;
			} else {
				// north side
				locationToSend = new MapLocation(checkNorth.x, checkNorth.y);
				typeOfEdge = NORTH_INT;
			}
		} else if (southNotOnMap) {
			if (eastNotOnMap) {
				// southeast corner
				locationToSend = new MapLocation(checkEast.x, checkSouth.y);
				typeOfEdge = SOUTHEAST_INT;
			} else if (westNotOnMap) {
				// southwest corner
				locationToSend = new MapLocation(checkWest.x, checkSouth.y);
				typeOfEdge = SOUTHWEST_INT;
			} else {
				// south side
				locationToSend = new MapLocation(checkSouth.x, checkSouth.y);
				typeOfEdge = SOUTH_INT;
			}
		} else if (eastNotOnMap) {
			// east side
			locationToSend = new MapLocation(checkEast.x, checkEast.y);
			typeOfEdge = EAST_INT;
		} else if (westNotOnMap) {
			// west side
			locationToSend = new MapLocation(checkWest.x, checkWest.y);
			typeOfEdge = WEST_INT;
		}
		if (typeOfEdge == -1) {
			return null;
		}
		int[] returnArr = new int[3];
		returnArr[0] = typeOfEdge;
		returnArr[1] = locationToSend.x;
		returnArr[2] = locationToSend.y;

		return returnArr;
	}

	public Direction edgeTypeToDir(int type) {
		switch (type) {
			case NORTH_INT:
				return Direction.NORTH;
			case NORTHEAST_INT:
				return Direction.NORTHEAST;
			case EAST_INT:
				return Direction.EAST;
			case SOUTHEAST_INT:
				return Direction.SOUTHEAST;
			case SOUTH_INT:
				return Direction.SOUTH;
			case SOUTHWEST_INT:
				return Direction.SOUTHWEST;
			case WEST_INT:
				return Direction.WEST;
			case NORTHWEST_INT:
				return Direction.NORTHWEST;
			default:
				return Direction.NORTH;
		}
	}

	public String edgeTypeToString(int type) {
		switch (type) {
			case NORTH_INT:
				return "NORTH";
			case NORTHEAST_INT:
				return "NORTH EAST";
			case EAST_INT:
				return "EAST";
			case SOUTHEAST_INT:
				return "SOUTH EAST";
			case SOUTH_INT:
				return "SOUTH";
			case SOUTHWEST_INT:
				return "SOUTHWEST";
			case WEST_INT:
				return "WEST";
			case NORTHWEST_INT:
				return "NORTH WEST";
			default:
				return "NOT AN EDGE";
		}
	}

	/**
	 * returns base cooldown based on robot type
	 * 
	 * @return
	 */
	public double getBaseCooldown() {
		switch (rc.getType()) {
			case ENLIGHTENMENT_CENTER:
				return 2.0;
			case POLITICIAN:
				return 1.0;
			case SLANDERER:
				return 2.0;
			case MUCKRAKER:
				return 1.5;
			default:
				return -1;
		}
	}

	/**
	 * returns cardinal sensor radius based on robot type
	 * 
	 * @return
	 */
	public int getCardinalSensorRadius() {
		switch (rc.getType()) {
			case ENLIGHTENMENT_CENTER:
				return 6;
			case POLITICIAN:
				return 5;
			case SLANDERER:
				return 4;
			case MUCKRAKER:
				return 5;
			default:
				return -1;
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
	 * checks if robot can move in dir and whether the location is not in the
	 * previousLoc array if so, move there and update the previousLoc array
	 * 
	 * @param dir direction to attempt to move in
	 * @return whether the robot moved in dir
	 * @throws GameActionException
	 */
	public boolean tryMoveAvoidPrevLocs(Direction dir) throws GameActionException {
		if (!rc.canMove(dir)) {
			return false;
		}
		MapLocation attemptMoveLoc = rc.getLocation().add(dir);
		boolean shouldMove = true;
		for (int i = 0; i < noReturnLocLen; i++) {
			MapLocation previousLoc = previousLocs[i];
			if (previousLoc != null && previousLoc.equals(attemptMoveLoc)) {
				shouldMove = false;
				break;
			}
		}
		if (shouldMove) {
			// update array with new location
			previousLocs[noReturnLocLen - 1] = attemptMoveLoc;
			rc.move(dir);
			return true;
		}
		return false;
	}

	/**
	 * shifts elements of previous location array by one and makes the last one null
	 * used to prevent robot from returning to past # of locations when pathfinding
	 * 
	 * @throws GameActionException
	 */
	public void shiftPrevLocArray() throws GameActionException {
		for (int i = 0; i < noReturnLocLen - 1; i++) {
			previousLocs[i] = previousLocs[i + 1];
		}
		previousLocs[noReturnLocLen - 1] = null;
	}

	public void updateCurrLoc(MapLocation currLoc) {
		this.currLoc = currLoc;
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
