package sprintplayer;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {
	/**
	 * Constants
	 */
	public static final int NUM_OF_UNITS_TO_TRACK = 100;
	public static final int PRIORITY_BUILD_QUEUE_SIZE = 20;

	/**
	 * Attributes
	 */
	public int[] robotIDs;
	public int numOfRobotsCreated;
	// Map
	public int northY;
	public int southY;
	public int eastX;
	public int westX;
	public int mapWidth;
	public int mapHeight;
	// EC Locations
	public MapLocation[] enemyECLocs;
	public int enemyECsIndex;
	public MapLocation[] fellowECLocs;
	public int fellowECsIndex;
	public MapLocation[] neutralECLocs;
	public int neutralECsIndex;
	// Build Queues
	public boolean initialBuildCycleDone;
	public BuildUnit[] initialBuildCycle;
	public int initialBuildCycleIndex;
	public BuildUnit[] regularBuildCycle;
	public int regularBuildCycleIndex;
	public BuildUnit[] priorityBuildQueue;
	// Directions
	public Direction[] availableDirs;
	public int muckrakerDirIndex;
	public int politicianDirIndex;
	public Direction[] slandererDirs;
	public int slandererDirIndex;
	// Common BuildUnits
	BuildUnit S130;
	BuildUnit M1;
	BuildUnit P18;
	BuildUnit S41;

	/**
	 * Constructor
	 * 
	 * @param rc
	 * @throws GameActionException
	 */
	public EnlightenmentCenter(RobotController rc) throws GameActionException {
		super(rc);
		robotIDs = new int[NUM_OF_UNITS_TO_TRACK];
		enemyECLocs = new MapLocation[12];
		fellowECLocs = new MapLocation[12];
		neutralECLocs = new MapLocation[6];
		S130 = new BuildUnit(RobotType.SLANDERER, 130);
		M1 = new BuildUnit(RobotType.MUCKRAKER, 1);
		P18 = new BuildUnit(RobotType.POLITICIAN, 18);
		S41 = new BuildUnit(RobotType.SLANDERER, 41);
		initialBuildCycle = new BuildUnit[] { S130, M1, P18, S41, S41, S41, P18, S41, S41, P18, S41, S41, P18, S41, S41,
				P18, S41, S41, P18, S41 };
		regularBuildCycle = new BuildUnit[] { P18, S41, M1, P18, S41, M1 };
		priorityBuildQueue = new BuildUnit[PRIORITY_BUILD_QUEUE_SIZE];
		availableDirs = checkAdjTiles();
		slandererDirs = getSlandererDirs();
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		checkFlags();
		// for (int i = 0; i < initialBuildCycle.length; i++) {
		// if (i == initialBuildCycleIndex) {
		// System.out.println("Initial Build Cycle: *" + initialBuildCycle[i].type +
		// "*");
		// continue;
		// }
		// System.out.println("Initial Build Cycle: " + initialBuildCycle[i].type);
		// }
		// for (int i = 0; i < regularBuildCycle.length; i++) {
		// if (i == regularBuildCycleIndex) {
		// System.out.println("Regular Build Cycle: *" + regularBuildCycle[i].type +
		// "*");
		// continue;
		// }
		// System.out.println("Regular Build Cycle: " + regularBuildCycle[i].type);
		// }
		// for (int i = 0; i < priorityBuildQueue.length; i++) {
		// if (priorityBuildQueue[i] != null) {
		// System.out.println("Priority Build Queue: " + priorityBuildQueue[i].type);
		// }
		// }
		if (initialBuildCycleIndex < initialBuildCycle.length) {
			System.out.println("\nIn Initial Build Cycle");
			for (int i = 0; i < initialBuildCycle.length; i++) {
				if (i == initialBuildCycleIndex) {
					System.out.println("*" + initialBuildCycle[i] + "*");
					continue;
				}
				System.out.println(initialBuildCycle[i]);
			}
			buildCycleUnit();
		} else if (priorityBuildQueue[0] != null) {
			System.out.println("\nIn Priority Build Queue");
			for (int i = 0; i < priorityBuildQueue.length; i++) {
				System.out.println(priorityBuildQueue[i]);
			}
			buildPriorityQueueUnit();
		} else {
			System.out.println("\nIn Regular Build Cycle");
			for (int i = 0; i < regularBuildCycle.length; i++) {
				if (i == regularBuildCycleIndex) {
					System.out.println("*" + regularBuildCycle[i] + "*");
					continue;
				}
				System.out.println(regularBuildCycle[i]);
			}
			buildCycleUnit();
		}
	}

	public Direction[] getSlandererDirs() throws GameActionException {
		int[] edges = nav.lookForEdges();
		if (edges == null) {
			Direction randDir = randomDirection(availableDirs);
			return new Direction[] { randDir.rotateLeft(), randDir, randDir.rotateRight() };
		}
		int edgeType = edges[0];
		edgeType += 8;
		int centerDir = 0;
		MapLocation edgeLoc = new MapLocation(edges[1], edges[2]);
		// Cardinal Directions
		if (edgeType % 2 == 0) {
			if (currLoc.distanceSquaredTo(edgeLoc) >= 16) {
				centerDir = edgeType;
			} else {
				centerDir = (edgeType + 2) % directions.length;
			}
		}
		// Corners
		else {
			int xEdge = edgeType + 1;
			int yEdge = edgeType - 1;
			if (Math.abs(currLoc.x - edgeLoc.x) < 4) {
				xEdge += 4;
			}
			if (Math.abs(currLoc.y - edgeLoc.y) < 4) {
				yEdge += 4;
			}
			centerDir = (xEdge + yEdge) / 2;
		}
		return new Direction[] { nav.edgeTypeToDir((centerDir - 1) % directions.length),
				nav.edgeTypeToDir(centerDir % directions.length),
				nav.edgeTypeToDir((centerDir + 1) % directions.length) };
	}

	public Direction[] checkAdjTiles() throws GameActionException {
		int existCount = 0;
		for (int i = 0; i < directions.length; i++) {
			if (rc.onTheMap(rc.adjacentLocation(directions[i])) == true) {
				existCount++;
			}
		}
		Direction[] clearDirs = new Direction[existCount];
		int tempIndex = 0;
		for (int i = 0; i < directions.length; i++) {
			if (rc.onTheMap(rc.adjacentLocation(directions[i])) == true) {
				clearDirs[tempIndex] = directions[i];
				tempIndex++;
			}
		}
		return clearDirs;
	}

	public void checkFlags() throws GameActionException {
		for (int i = 0; i < numOfRobotsCreated; i++) {
			int[] info = comms.readMessage(robotIDs[i]);
			if (info == null) {
				continue;
			}
			int messageType = info[0];
			switch (messageType) {
				case Comms.FOUND_EDGE:
					saveEdge(info);
					updateDims();
					break;
				case Comms.FOUND_EC:
					int teamInt = info[1];
					MapLocation toAdd = new MapLocation(currLoc.x + info[3], currLoc.y + info[4]);
					switch (teamInt) {
						case 0:
							if (!checkInArray(enemyECLocs, toAdd)) {
								fellowECLocs[fellowECsIndex++] = toAdd;
							}
							break;
						case 1:
							if (!checkInArray(enemyECLocs, toAdd)) {
								enemyECLocs[enemyECsIndex++] = toAdd;
							}
							break;
						case 2:
							if (!checkInArray(neutralECLocs, toAdd)) {
								neutralECLocs[neutralECsIndex++] = toAdd;
								buildQueueAdd(priorityBuildQueue, new BuildUnit(RobotType.POLITICIAN,
										comms.convIntRangeToMaxConv(info[2]) + 11, toAdd, Team.NEUTRAL));
								System.out.println("Added to Priority Build Queue");
							}
							break;
					}
					break;
			}
		}
	}

	public void saveEdge(int[] info) {
		int edgeType = info[1];
		int xOff = info[2];
		int yOff = info[3];
		switch (edgeType) {
			case Navigation.NORTH_INT:
				if (northY == 0) {
					northY = currLoc.y + yOff;
				}
				break;
			case Navigation.NORTHEAST_INT:
				if (northY == 0) {
					northY = currLoc.y + yOff;
				}
				if (eastX == 0) {
					eastX = currLoc.x + xOff;
				}
				break;
			case Navigation.EAST_INT:
				if (eastX == 0) {
					eastX = currLoc.x + xOff;
				}
				break;
			case Navigation.SOUTHEAST_INT:
				if (southY == 0) {
					southY = currLoc.y + yOff;
				}
				if (eastX == 0) {
					eastX = currLoc.x + xOff;
				}
				break;
			case Navigation.SOUTH_INT:
				if (southY == 0) {
					southY = currLoc.y + yOff;
				}
				break;
			case Navigation.SOUTHWEST_INT:
				if (southY == 0) {
					southY = currLoc.y + yOff;
				}
				if (westX == 0) {
					westX = currLoc.x + xOff;
				}
				break;
			case Navigation.WEST_INT:
				if (westX == 0) {
					westX = currLoc.x + xOff;
				}
				break;
			case Navigation.NORTHWEST_INT:
				if (northY == 0) {
					northY = currLoc.y + yOff;
				}
				if (westX == 0) {
					westX = currLoc.x + xOff;
				}
				break;
		}
	}

	public void updateDims() {
		if (westX != 0 && eastX != 0 && mapWidth == 0) {
			mapWidth = eastX - westX + 1;
		}
		if (southY != 0 && northY != 0 && mapHeight == 0) {
			mapHeight = northY - southY + 1;
		}
	}

	public void buildQueueRemove(BuildUnit[] buildQueue) {
		for (int i = 1; i < buildQueue.length; i++) {
			buildQueue[i - 1] = buildQueue[i];
		}
		buildQueue[buildQueue.length - 1] = null;
	}

	public void buildQueueAdd(BuildUnit[] buildQueue, BuildUnit bu) {
		for (int i = 0; i < buildQueue.length; i++) {
			if (buildQueue[i] == null) {
				buildQueue[i] = bu;
				return;
			}
		}
	}

	public void buildPriorityQueueUnit() throws GameActionException {
		BuildUnit bu = priorityBuildQueue[0];
		Direction dirToBuild = dirToBuild(bu);
		if (rc.canBuildRobot(bu.type, dirToBuild, bu.conviction)) {
			rc.buildRobot(bu.type, dirToBuild, bu.conviction);
			addID(dirToBuild);
			if (bu.hasTargetEC()) {
				comms.sendFoundECMessage(bu.targetECLoc.x, bu.targetECLoc.y, bu.targetECTeam, bu.conviction);
			}
			buildQueueRemove(priorityBuildQueue);
		}
	}

	public void buildCycleUnit() throws GameActionException {
		BuildUnit bu;
		if (initialBuildCycleIndex < initialBuildCycle.length) {
			bu = initialBuildCycle[initialBuildCycleIndex];
		} else {
			bu = regularBuildCycle[regularBuildCycleIndex];
		}
		Direction dirToBuild = dirToBuild(bu);
		if (rc.canBuildRobot(bu.type, dirToBuild, bu.conviction)) {
			rc.buildRobot(bu.type, dirToBuild, bu.conviction);
			addID(dirToBuild);
			if (initialBuildCycleIndex < initialBuildCycle.length) {
				initialBuildCycleIndex++;
			} else {
				regularBuildCycleIndex++;
				if (regularBuildCycleIndex == regularBuildCycle.length) {
					regularBuildCycleIndex = 0;
				}
			}
		} else if (initialBuildCycleIndex < initialBuildCycle.length) {
			dirToBuild = dirToBuild(M1);
			if (rc.canBuildRobot(RobotType.MUCKRAKER, dirToBuild, 1)) {
				rc.buildRobot(RobotType.MUCKRAKER, dirToBuild, 1);
				addID(dirToBuild);
			}
		}
	}

	public Direction dirToBuild(BuildUnit bu) throws GameActionException {
		Direction dirToBuild;
		if (bu.hasTargetEC()) {
			dirToBuild = currLoc.directionTo(bu.targetECLoc);
			while (!rc.onTheMap(currLoc.add(dirToBuild)) || rc.isLocationOccupied(currLoc.add(dirToBuild))) {
				dirToBuild = dirToBuild.rotateRight();
			}
		} else if (bu.type.equals(RobotType.SLANDERER)) {
			dirToBuild = slandererDirs[slandererDirIndex++];
			if (slandererDirIndex == slandererDirs.length) {
				slandererDirIndex = 0;
			}
			while (rc.isLocationOccupied(currLoc.add(dirToBuild))) {
				dirToBuild = slandererDirs[slandererDirIndex++];
				if (slandererDirIndex == slandererDirs.length) {
					slandererDirIndex = 0;
				}
			}
		} else if (bu.type.equals(RobotType.POLITICIAN) && bu.conviction == Politician.HERDER_POLITCIAN_INFLUENCE) {
			dirToBuild = slandererDirs[(slandererDirIndex - 1 + slandererDirs.length) % slandererDirs.length];
			while (!rc.onTheMap(currLoc.add(dirToBuild)) || rc.isLocationOccupied(currLoc.add(dirToBuild))) {
				dirToBuild = dirToBuild.rotateRight();
			}
		} else if (bu.type.equals(RobotType.POLITICIAN)) {
			dirToBuild = availableDirs[politicianDirIndex++];
			if (politicianDirIndex == availableDirs.length) {
				politicianDirIndex = 0;
			}
			while (rc.isLocationOccupied(currLoc.add(dirToBuild))) {
				dirToBuild = availableDirs[politicianDirIndex++];
				if (politicianDirIndex == availableDirs.length) {
					politicianDirIndex = 0;
				}
			}
		} else {
			dirToBuild = availableDirs[muckrakerDirIndex++];
			if (muckrakerDirIndex == availableDirs.length) {
				muckrakerDirIndex = 0;
			}
			while (rc.isLocationOccupied(currLoc.add(dirToBuild))) {
				dirToBuild = availableDirs[muckrakerDirIndex++];
				if (muckrakerDirIndex == availableDirs.length) {
					muckrakerDirIndex = 0;
				}
			}
		}
		return dirToBuild;
	}

	public void addID(Direction dirToCheck) throws GameActionException {
		if (numOfRobotsCreated == NUM_OF_UNITS_TO_TRACK) {
			return;
		}
		robotIDs[numOfRobotsCreated++] = rc.senseRobotAtLocation(currLoc.add(dirToCheck)).ID;
	}

	public boolean checkInArray(MapLocation[] arr, MapLocation toCheck) {
		int len = arr.length;
		for (int i = 0; i < len; i++) {
			if (arr[i] == null) {
				continue;
			}
			if (arr[i].equals(toCheck)) {
				return true;
			}
		}
		return false;
	}

	public int getIndexOfID(int id) {
		for (int i = 0; i < numOfRobotsCreated; i++) {
			if (robotIDs[i] == id) {
				return i;
			}
		}
		return -1;
	}
}