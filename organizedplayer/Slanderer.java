package organizedplayer;
import battlecode.common.*;

public class Slanderer extends Robot {

    public Slanderer(RobotController r){
        super(r);
    }

    public void takeTurn() throws GameActionException{
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

}