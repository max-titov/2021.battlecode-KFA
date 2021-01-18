package commsplayer;

import battlecode.common.*;

public class Slanderer extends Robot {

    public Slanderer(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void takeTurn() throws GameActionException {
        if (nav.tryMove(randomDirection()))
            System.out.println("I moved!");
    }

}