package navtest;

import battlecode.common.*;

public class Muckraker extends Robot {

	public Muckraker(RobotController rc) {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		int[] edges = nav.lookForEdges();
		String print = "no edges found";
		if (edges!= null){
			print = "Type: " + edges[0]+" ("+edges[1]+","+edges[2]+")";
		}
		System.out.println(print);
		nav.tryMoveToTarget(new MapLocation(10027, 23949));
	}
}