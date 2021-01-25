package sprintplayer;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {
	/**
	 * Constants
	 */
	public static final int NUM_OF_UNITS_TO_TRACK = 100;
	public static final int BUILD_QUEUE_SIZE = 12;
	// messages
	// public final int EDGE_MESSAGE = ;

	/**
	 * Attributes
	 */
	public int muckrakerDirIndex;
	public boolean firstSlandererCreated;
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
	public boolean initialBuildQueueDone;
	public BuildUnit[] initialBuildCycle;
	public BuildUnit[] regularBuildCycle;
	public BuildUnit[] priorityBuildQueue;

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
		initialBuildCycle = new BuildUnit[BUILD_QUEUE_SIZE];
		regularBuildCycle = new BuildUnit[BUILD_QUEUE_SIZE];
		priorityBuildQueue = new BuildUnit[BUILD_QUEUE_SIZE];
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		checkFlags();
		if (!buildUnit(priorityBuildQueue)) {
			if (initialBuildQueueDone) {
				buildUnit(regularBuildQueue);
			} else {
				if (!buildUnit(initialBuildQueue)) {
					buildMuckraker();
				}
				if (initialBuildQueue[0] == null) {
					initialBuildQueueDone = true;
				}
			}
		}
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
										comms.convIntRangeToMaxConv(info[2]), currLoc.directionTo(toAdd), toAdd));
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

	public boolean buildUnit(BuildUnit[] buildQueue) throws GameActionException {
		if (buildQueue[0] == null) {
			return false;
		}
		if (rc.canBuildRobot(buildQueue[0].type, buildQueue[0].dirToBuild, buildQueue[0].influence)) {
			rc.buildRobot(buildQueue[0].type, buildQueue[0].dirToBuild, buildQueue[0].influence);
			buildQueueRemove(buildQueue);
			return true;
		}
		return false;
	}

	public void buildMuckraker() throws GameActionException {
		if (rc.canBuildRobot(RobotType.MUCKRAKER, directions[muckrakerDirIndex], 1)) {
			rc.buildRobot(RobotType.MUCKRAKER, directions[muckrakerDirIndex++], 1);
		}
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