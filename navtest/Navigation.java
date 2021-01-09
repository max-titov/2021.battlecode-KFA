package navtest;

import java.util.Map;

import battlecode.common.*;

public class Navigation {

	static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };
	public final Direction N = Direction.NORTH;
	public final Direction NW = Direction.NORTHWEST;
	public final Direction W = Direction.WEST;
	public final Direction SW = Direction.SOUTHWEST;
	public final Direction S = Direction.SOUTH;
	public final Direction SE = Direction.SOUTHEAST;
	public final Direction E = Direction.EAST;
	public final Direction NE = Direction.NORTHEAST;

	private RobotController rc;

	public Navigation(RobotController rc) {
		this.rc = rc;
	}

	public void tryMoveToTarget(MapLocation currLoc, MapLocation target) throws GameActionException {
		MapLocation prevLoc = currLoc;
		if (!currLoc.equals(target)) {
			double[] adjEfficiency = getAdjEfficiencyMap(currLoc, target);
			int maxIndex = 0;
			int secondMaxIndex = 0;
			for (int i = 0; i < 8; i++) {
				if (adjEfficiency[i] > adjEfficiency[maxIndex]) {
					maxIndex = i;
				}
			}
			for (int i = 0; i < 8; i++) {
				if (i != maxIndex && adjEfficiency[i] > adjEfficiency[secondMaxIndex]) {
					secondMaxIndex = i;
				}
			}
			Direction mostEfficientDirection = directions[maxIndex];
			if (currLoc.add(mostEfficientDirection).equals(prevLoc)) {
				mostEfficientDirection = directions[secondMaxIndex];
			}
			if (tryMove(mostEfficientDirection)) {
				prevLoc = currLoc;
			}
			currLoc = rc.getLocation();
		}
	}

	public double[] getAdjEfficiencyMap(MapLocation currLoc, MapLocation target) throws GameActionException {
		double[] efficiencies = new double[8];
		double passabilityWeight = 1;
		double directionWeight = 0.65;
		double directionalBias = 1.4;
		for (int i = 0; i < 8; i++) {
			MapLocation testLoc = currLoc.add(directions[i]);
			double passability = rc.sensePassability(testLoc);
			double directionalAccuracy = Math.sqrt(currLoc.distanceSquaredTo(target))
					- Math.sqrt(testLoc.distanceSquaredTo(target));
			efficiencies[i] = passabilityWeight * passability
					+ directionWeight * (directionalAccuracy + directionalBias);
		}
		return efficiencies;
	}

	public double[] getAdjPassabilityMap(MapLocation currLoc) throws GameActionException {
		double[] passabilities = new double[8];
		for (int i = 0; i < 8; i++) {
			passabilities[i] = rc.sensePassability(currLoc.add(directions[i]));
		}
		return passabilities;
	}

	public Direction relativeLocToEC(MapLocation currLoc) {
		RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
		for (int i = 0; i < robots.length; i++) {
			RobotInfo ri = robots[i];
			if (ri.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
				return ri.getLocation().directionTo(currLoc);
			}
		}
		return Direction.NORTH;
	}

	public double calcTurnsOfPath(double[] path) {
		double cooldown = 5.3;
		for (int i = 0; i < path.length; i++) {
			cooldown += getBaseCooldown() / path[i];
		}
		return cooldown;
	}

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

	public Direction randomDirection() {
		return directions[(int) (Math.random() * directions.length)];
	}

	public boolean tryMove(Direction dir) throws GameActionException {
		if (rc.canMove(dir)) {
			rc.move(dir);
			return true;
		} else
			return false;
	}

}
