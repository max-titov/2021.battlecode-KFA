package sprintplayer;

import battlecode.common.*;

public class BuildUnit {
	/**
	 * Attributes
	 */
	public RobotType type;
	public int conviction;
	// Only for capture politicians and attack politicians
	public MapLocation targetECLoc;
	public Team targetECTeam;

	public BuildUnit(RobotType type, int conviction, MapLocation targetECLoc, Team targetECTeam) {
		this.type = type;
		this.conviction = conviction;
		this.targetECLoc = targetECLoc;
	}

	public BuildUnit(RobotType type, int conviction) {
		this.type = type;
		this.conviction = conviction;
	}

	public boolean hasTargetEC() {
		return targetECLoc != null;
	}

	public String toString() {
		return type.toString() + " " + conviction + " has target EC: " + hasTargetEC();
	}

}
