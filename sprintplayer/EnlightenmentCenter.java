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
	public int enemyECsIndexForAttacks;
	public MapLocation[] fellowECLocs;
	public int fellowECsIndex;
	public MapLocation[] neutralECLocs;
	public int neutralECsIndex;
	// Build Queues
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
	BuildUnit P35;
	BuildUnit S41;
	// Votes
	int twoRoundsAgoVoteCount = -2;
	int lastRoundVoteCount = -1;
	int currentVoteAmount = 1;
	int currentVoteAmountToIncreaseBy = 1;

	boolean raisedFlag = false;
	BuildUnit messageBU;

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
		findNearbyECs();
		S130 = new BuildUnit(RobotType.SLANDERER, 130);
		M1 = new BuildUnit(RobotType.MUCKRAKER, 1);
		P18 = new BuildUnit(RobotType.POLITICIAN, 18);
		P35 = new BuildUnit(RobotType.POLITICIAN, 35);
		S41 = new BuildUnit(RobotType.SLANDERER, 41);
		initialBuildCycle = new BuildUnit[] { S130, M1, P18, S41, S41, S41, P18, S41, S41, P18, S41, S41, P18, S41, S41,
				P18, S41, S41, P18, S41, P18 };
		regularBuildCycle = new BuildUnit[] { P35, S41, M1, P35, S41, M1, P35 };
		priorityBuildQueue = new BuildUnit[PRIORITY_BUILD_QUEUE_SIZE];
		availableDirs = checkAdjTiles();
		slandererDirs = getSlandererDirs();
		if (roundNum > 1) {
			initialBuildCycleIndex = initialBuildCycle.length;
		}
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		comms.dropFlag();
		if (raisedFlag) {
			comms.sendFoundECMessage(messageBU.targetECLoc.x, messageBU.targetECLoc.y, messageBU.targetECTeam,
					messageBU.conviction);
			raisedFlag = false;
			messageBU = null;
		}
		if (roundNum > 500) {
			vote();
		}
		checkFlags();
		if (roundNum - 50 >= 0 && roundNum % 50 == 0) {
			sendOutAttackMessage();
		} else if (getEnemyUnitsInArea()) {
			comms.sendHelpMessage();
			if (!priorityBuildQueue[0].equals(M1)) {
				buildQueueLineCut(M1);
			}
		}
		if (cooldownTurns >= 1) {
			return;
		}
		if (initialBuildCycleIndex < initialBuildCycle.length) {
			// System.out.println("\nIn Initial Build Cycle");
			// for (int i = 0; i < initialBuildCycle.length; i++) {
			// if (i == initialBuildCycleIndex) {
			// System.out.println("*" + initialBuildCycle[i] + "*");
			// continue;
			// }
			// System.out.println(initialBuildCycle[i]);
			// }
			buildCycleUnit();
		} else if (priorityBuildQueue[0] != null) {
			// System.out.println("\nIn Priority Build Queue");
			// for (int i = 0; i < priorityBuildQueue.length; i++) {
			// System.out.println(priorityBuildQueue[i]);
			// }
			buildPriorityQueueUnit();
		} else {
			// System.out.println("\nIn Regular Build Cycle");
			// for (int i = 0; i < regularBuildCycle.length; i++) {
			// if (i == regularBuildCycleIndex) {
			// System.out.println("*" + regularBuildCycle[i] + "*");
			// continue;
			// }
			// System.out.println(regularBuildCycle[i]);
			// }
			buildCycleUnit();
		}
	}

	public void vote() throws GameActionException {
		int currentVotes = rc.getTeamVotes();
		if (currentVotes == twoRoundsAgoVoteCount) {
			currentVoteAmountToIncreaseBy++;
		}
		if (currentVotes == lastRoundVoteCount) {
			currentVoteAmount += currentVoteAmountToIncreaseBy;
		}
		if (rc.canBid(currentVoteAmount)) {
			rc.bid(currentVoteAmount);
		}
		twoRoundsAgoVoteCount = lastRoundVoteCount;
		lastRoundVoteCount = currentVotes;
	}

	public void sendOutAttackMessage() throws GameActionException {
		MapLocation ECToAttack = null;
		for (int i = 0; i < enemyECLocs.length; i++) {
			ECToAttack = enemyECLocs[(enemyECsIndexForAttacks + i) % enemyECLocs.length];
			if (ECToAttack != null) {
				break;
			}
		}
		if (ECToAttack == null) {
			return;
		}
		comms.sendFoundECMessage(ECToAttack.x, ECToAttack.y, opponentTeam, 0);
		enemyECsIndexForAttacks++;
	}

	public boolean getEnemyUnitsInArea() throws GameActionException {
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(40, opponentTeam);
		int totalConv = 0;
		int len = nearbyEnemies.length;
		for (int i = 0; i < len; i++) {
			RobotInfo ri = nearbyEnemies[i];
			if (ri.type.equals(RobotType.POLITICIAN)) {
				totalConv += ri.conviction;
			}
		}
		if (influence < 150 && totalConv > influence) {
			return true;
		} else if (totalConv > 200) {
			return true;
		}
		return false;
	}

	public void findNearbyECs() {
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
		int len = nearbyRobots.length;
		for (int i = 0; i < len; i++) {
			RobotInfo ri = nearbyRobots[i];
			if (ri.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
				if (ri.team.equals(myTeam)) {
					if (!checkInArray(enemyECLocs, ri.location)) {
						fellowECLocs[fellowECsIndex++] = ri.location;
					}
				} else if (ri.team.equals(opponentTeam)) {
					if (!checkInArray(enemyECLocs, ri.location)) {
						enemyECLocs[enemyECsIndex++] = ri.location;
					}
				} else {
					if (!checkInArray(neutralECLocs, ri.location)) {
						neutralECLocs[neutralECsIndex++] = ri.location;
						buildQueueAdd(
								new BuildUnit(RobotType.POLITICIAN, ri.conviction + 11, ri.location, Team.NEUTRAL));
					}
				}
			}
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
								buildQueueAdd(new BuildUnit(RobotType.POLITICIAN,
										comms.convIntRangeToMaxConv(info[2]) + 11, toAdd, Team.NEUTRAL));
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

	public void buildQueueRemove() {
		int len = priorityBuildQueue.length;
		for (int i = 1; i < len; i++) {
			priorityBuildQueue[i - 1] = priorityBuildQueue[i];
		}
		priorityBuildQueue[priorityBuildQueue.length - 1] = null;
	}

	public void buildQueueAdd(BuildUnit bu) {
		int len = priorityBuildQueue.length;
		for (int i = 0; i < len; i++) {
			if (priorityBuildQueue[i] == null) {
				priorityBuildQueue[i] = bu;
				return;
			}
		}
	}

	public void buildQueueLineCut(BuildUnit bu) {
		int len = priorityBuildQueue.length;
		for (int i = 0; i < len - 1; i++) {
			priorityBuildQueue[i + 1] = priorityBuildQueue[i];
		}
		priorityBuildQueue[0] = null;
		buildQueueAdd(bu);
	}

	public void buildPriorityQueueUnit() throws GameActionException {
		BuildUnit bu = priorityBuildQueue[0];
		Direction dirToBuild = dirToBuild(bu);
		if (rc.canBuildRobot(bu.type, dirToBuild, bu.conviction)) {
			rc.buildRobot(bu.type, dirToBuild, bu.conviction);
			addID(dirToBuild);
			if (bu.hasTargetEC()) {
				raisedFlag = true;
				messageBU = bu;
			}
			buildQueueRemove();
		}
	}

	public void buildCycleUnit() throws GameActionException {
		BuildUnit bu;
		int multiplier = 1;
		if (initialBuildCycleIndex < initialBuildCycle.length) {
			bu = initialBuildCycle[initialBuildCycleIndex];
		} else {
			bu = regularBuildCycle[regularBuildCycleIndex];
			if (bu.type.equals(RobotType.SLANDERER)) {
				multiplier = influence / 50;
			} else if (bu.type.equals(RobotType.POLITICIAN)) {
				if (bu.type.equals(RobotType.POLITICIAN) && bu.conviction == Politician.HERDER_POLITICIAN_INFLUENCE) {
					multiplier = 1;
				} else {
					multiplier = influence / 100;
				}
			} else if (bu.type.equals(RobotType.MUCKRAKER) && coinFlip(0.2)) {
				multiplier = 100;
			}
		}
		Direction dirToBuild = dirToBuild(bu);
		if (rc.canBuildRobot(bu.type, dirToBuild, bu.conviction * multiplier)) {
			rc.buildRobot(bu.type, dirToBuild, bu.conviction * multiplier);
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
		int len = directions.length;
		int aLen = availableDirs.length;
		if (bu.hasTargetEC()) {
			dirToBuild = currLoc.directionTo(bu.targetECLoc);
			for (int i = 0; i < len; i++) {
				if (rc.onTheMap(currLoc.add(dirToBuild)) && !rc.isLocationOccupied(currLoc.add(dirToBuild))) {
					break;
				}
				dirToBuild = dirToBuild.rotateRight();
			}
		} else if (bu.type.equals(RobotType.SLANDERER)) {
			dirToBuild = slandererDirs[slandererDirIndex++];
			if (slandererDirIndex == slandererDirs.length) {
				slandererDirIndex = 0;
			}
			for (int i = 0; i < len; i++) {
				if (rc.onTheMap(currLoc.add(dirToBuild)) && !rc.isLocationOccupied(currLoc.add(dirToBuild))) {
					break;
				}
				dirToBuild = dirToBuild.rotateRight();
			}
		} else if (bu.type.equals(RobotType.POLITICIAN)
				&& (bu.conviction % Politician.HERDER_POLITICIAN_INFLUENCE) == 0) {
			dirToBuild = slandererDirs[(slandererDirIndex - 1 + slandererDirs.length) % slandererDirs.length];
			for (int i = 0; i < len; i++) {
				if (rc.onTheMap(currLoc.add(dirToBuild)) && !rc.isLocationOccupied(currLoc.add(dirToBuild))) {
					break;
				}
				dirToBuild = dirToBuild.rotateRight();
			}
		} else if (bu.type.equals(RobotType.POLITICIAN)) {
			dirToBuild = availableDirs[politicianDirIndex++];
			if (politicianDirIndex == availableDirs.length) {
				politicianDirIndex = 0;
			}
			for (int i = 0; i < aLen; i++) {
				if (!rc.isLocationOccupied(currLoc.add(dirToBuild))) {
					break;
				}
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
			for (int i = 0; i < aLen; i++) {
				if (!rc.isLocationOccupied(currLoc.add(dirToBuild))) {
					break;
				}
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