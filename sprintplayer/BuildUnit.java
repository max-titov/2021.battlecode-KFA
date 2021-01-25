package sprintplayer;

import battlecode.common.*;

public class BuildUnit {
	/**
	 * Attributes
	 */
	public RobotType type;
	public int influence;
	// Only for capture politicians and attack politicians
	public MapLocation targetECLoc;

	public BuildUnit(RobotType type, int influence, MapLocation targetECLoc) {
		this.type = type;
		this.influence = influence;
		this.targetECLoc = targetECLoc;
	}

	public BuildUnit(RobotType type, int influence) {
		this.type = type;
		this.influence = influence;
	}

	public boolean hasTargetEC() {
		return targetECLoc != null;
	}

}
