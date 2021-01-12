package navtest2;

import battlecode.common.*;

public class Politician extends Robot {

	public Politician(RobotController r) {
		super(r);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		Team enemy = rc.getTeam().opponent();
		int actionRadius = rc.getType().actionRadiusSquared;
		RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
		if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
			rc.empower(actionRadius);
			return;
		}
		nav.tryMove(nav.randomDirection());
	}

}