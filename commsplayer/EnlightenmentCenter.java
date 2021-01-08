package commsplayer;
import battlecode.common.*;

public class EnlightenmentCenter extends Robot {
    int spawnedBotID = 0;

    public EnlightenmentCenter(RobotController r){
        super(r);
    }

    public void takeTurn() throws GameActionException{
        RobotType toBuild = RobotType.POLITICIAN;
        if(turnCount == 5){
            int influence = 50;
            for (Direction dir : directions) {
                if (rc.canBuildRobot(toBuild, dir, influence)) {
                    rc.buildRobot(toBuild, dir, influence);
                } else {
                    break;
                }
            }
        }
        System.out.println(rc.senseNearbyRobots(-1, rc.getTeam()));
        if (rc.senseNearbyRobots(-1, rc.getTeam()).length >= 1){
            RobotInfo spawnedBot = rc.senseNearbyRobots(-1, rc.getTeam())[0];
            this.spawnedBotID = spawnedBot.getID();
        }
        //spawnedBotID = spawnedRobot.ID;
        interpretMessage(spawnedBotID);
        turnCount += 1;
    }

}