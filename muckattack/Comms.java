package muckattack;

import battlecode.common.*;

public class Comms {
	public RobotController rc;
	public Team myTeam;
	public Team opponentTeam;
	public MapLocation currLoc;
	public MapLocation myECLoc;
	public final int edgeMessage = 0;
	public final int foundECMessage = 1;

	public Comms(RobotController rc, Team myTeam, Team opponentTeam, MapLocation currLoc, MapLocation myECLoc) {
		this.rc = rc;
		this.myTeam = myTeam;
		this.opponentTeam = opponentTeam;
		this.currLoc = currLoc;
		this.myECLoc = myECLoc;
	}

	public void sendMessage(int packedMessage) throws GameActionException {
		if (rc.canSetFlag(packedMessage)) {
			rc.setFlag(packedMessage);
		} else {
			System.out.println("Flag failed to send :(");
		}
	}

	// Edge type, X and Y offsets of the edge
	public void createEdgeMessage(int edgeType, int xOff, int yOff) throws GameActionException {
		int packedMessage = 0;
		// edgeType = 3 bits
		packedMessage = (packedMessage << 3) + edgeType;
		// xOff, yOff = 7 each
		packedMessage = (packedMessage << 7) + xOff;
		packedMessage = (packedMessage << 7) + yOff;
		// messageType = 4
		packedMessage = (packedMessage << 4) + edgeMessage;
		sendMessage(packedMessage);
	}

	// Reads edgeMessages: Edge type, x, y
	public int[] readEdgeMessage(int packedMessage) throws GameActionException {
		// [edgeType, xOff, yOff]
		int[] retList = new int[3];
		retList[2] = packedMessage & 0b1111111;
		retList[1] = (packedMessage >> 7) & 0b1111111;
		retList[0] = (packedMessage >> 7) & 0b111;
		return retList;
	}

	// Found EC Message: Ec allegiance, EC Conviction, x and y offsets of the EC
	public void createFoundECMessage(Team team, int targetConviction, int xOff, int yOff) throws GameActionException {
		int intTeam = 0;
		// Set value for the EC's team
		if (team.equals(myTeam)) {
			intTeam = 1;
		} else {
			intTeam = 2;
		}

		int convictionRange = 0;
		if (targetConviction <= 30) {
			convictionRange = 0;
		} else if (targetConviction <= 70) {
			convictionRange = 1;
		} else if (targetConviction <= 99) {
			convictionRange = 2;
		} else {
			convictionRange = 3;
		}

		int packedMessage = 0;
		packedMessage = (packedMessage << 2) + intTeam;
		packedMessage = (packedMessage << 2) + convictionRange;
		packedMessage = (packedMessage << 7) + xOff;
		packedMessage = (packedMessage << 7) + yOff;
		packedMessage = (packedMessage << 4) + foundECMessage;
		sendMessage(packedMessage);

	}

	/**
	 * Updates the current location for nav
	 * 
	 * @param currLoc
	 */
	public void updateCurrLoc(MapLocation currLoc) {
		this.currLoc = currLoc;
	}
}