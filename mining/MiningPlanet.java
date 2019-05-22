package mining;

// Environment code for project jasonTeamSimLocal.mas2j

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MiningPlanet extends jason.environment.Environment {

    private Logger logger = Logger.getLogger("jasonTeamSimLocal.mas2j." + MiningPlanet.class.getName());

    WorldModel  model;
    WorldView   view;

    int     simId    = 4; // type of environment
    int     nbWorlds = 4;

    int     sleep    = 0;
    boolean running  = true;
    boolean hasGUI   = true;

    public static final int SIM_TIME = 60;  // in seconds

    Term                    up       = Literal.parseLiteral("do(up)");
    Term                    down     = Literal.parseLiteral("do(down)");
    Term                    right    = Literal.parseLiteral("do(right)");
    Term                    left     = Literal.parseLiteral("do(left)");
    Term                    skip     = Literal.parseLiteral("do(skip)");
    Term                    fight     = Literal.parseLiteral("do(fight)");
    Term                    destroy     = Literal.parseLiteral("do(destroy)");
    Term                    extinguish     = Literal.parseLiteral("do(extinguish)");

    public enum Move {
        UP, DOWN, RIGHT, LEFT
    };

    String agent1Name = "spaceship";
    String agent2Name = "stormtrooper";

    @Override
    public void init(String[] args) {
        hasGUI = args[1].equals("yes");
        sleep  = Integer.parseInt(args[0]);
        initWorld(4);
        model.nbSpaceships = Integer.parseInt(args[2]);
        model.nbStormtroopers = Integer.parseInt(args[3]);
    }

    public int getSimId() {
        return simId;
    }

    public void setSleep(int s) {
        sleep = s;
    }

    @Override
    public void stop() {
        running = false;
        super.stop();
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        boolean result = false;
        try {
            if (sleep > 0) {
                Thread.sleep(sleep);
            }

            // get the agent id based on its name
            int agId = getAgIDFromName(ag);

            if (action.equals(up)) {
                result = model.move(Move.UP, agId);
            } else if (action.equals(down)) {
                result = model.move(Move.DOWN, agId);
            } else if (action.equals(right)) {
                result = model.move(Move.RIGHT, agId);
            } else if (action.equals(left)) {
                result = model.move(Move.LEFT, agId);
            } else if (action.equals(skip)) {
                result = true;
            } else if (action.equals(fight)) {
                result = model.fight(agId);
            } else if (action.equals(destroy)) {
                result = model.destroy(agId);
                //view.udpateCollectedGolds();
            } else {
                logger.info("executing: " + action + ", but not implemented!");
            }

            
            if (result) {
                updateAgPercept(agId);
                return true;
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error executing " + action + " for " + ag, e);
        }
        return false;
    }

    public int getAgIDFromName(String agName) {
        if (agName.equals("radar")) {
            return 0;
        }
        if (agName.equals("firealarm")) {
            return 1;
        }
        if (agName.startsWith(agent1Name)) {
            if (model.nbSpaceships == 1) {
                return 2;
            }
            else {
                return (Integer.parseInt(agName.substring(agent1Name.length()))) + 1;
            }
        }
        if (agName.startsWith(agent2Name)) {
            if (model.nbStormtroopers == 1) {
                return model.nbSpaceships;
            }
            else {
                return (Integer.parseInt(agName.substring(agent2Name.length()))) + (model.nbSpaceships + 1);
            }
        }
        logger.warning("There is no ID for agent named "+agName);
        return -1;
    }

    public String getAgNameFromID(int id) {
        if (id == 0) {
            return "radar";
        }
        if (id == 1) {
            return "firealarm";
        }
        if (id < model.nbSpaceships + 2) {
            if (model.nbSpaceships == 1) {
                return agent1Name;
            }
            else {
                return agent1Name + (id-1);
            }
        }
        else {
            if (model.nbStormtroopers == 1) {
                return agent2Name;
            }
            else {
                return agent2Name + (id-(model.nbSpaceships+1));
            }
        }
    }

    public void initWorld(int w) {
        simId = 4;
        try {
            model = WorldModel.world4();
            clearPercepts();
            addPercept(Literal.parseLiteral("gsize(" + simId + "," + model.getWidth() + "," + model.getHeight() + ")"));
            //addPercept(Literal.parseLiteral("depot(" + simId + "," + model.getDepot().x + "," + model.getDepot().y + ")"));
            List<Location> base = model.getBase();
            addPercept(Literal.parseLiteral("base1(" + base.get(0).x + "," + base.get(0).y + ")"));
            addPercept(Literal.parseLiteral("base2(" + base.get(1).x + "," + base.get(1).y + ")"));
            addPercept(Literal.parseLiteral("base3(" + base.get(2).x + "," + base.get(2).y + ")"));
            addPercept(Literal.parseLiteral("base4(" + base.get(3).x + "," + base.get(3).y + ")"));
            addPercept(Literal.parseLiteral("base5(" + base.get(4).x + "," + base.get(4).y + ")"));
            addPercept(Literal.parseLiteral("base6(" + base.get(5).x + "," + base.get(5).y + ")"));

            if (hasGUI) {
                view = new WorldView(model);
                view.setEnv(this);
                //view.udpateCollectedGolds();
            }
            updateAgsPercept();
            informAgsEnvironmentChanged();
        } catch (Exception e) {
            logger.warning("Error creating world "+e);
        }
    }

    public void endSimulation() {
        addPercept(Literal.parseLiteral("end_of_simulation(" + simId + ",0)"));
        informAgsEnvironmentChanged();
        if (view != null) view.setVisible(false);
        WorldModel.destroy();
    }

    private void updateAgsPercept() {
        for (int i = 0; i < model.getNbOfAgs(); i++) {
            updateAgPercept(i);
        }
    }

    private void updateAgPercept(int ag) {
        updateAgPercept(getAgNameFromID(ag), ag);
    }

    private void updateAgPercept(String agName, int ag) {
        clearPercepts(agName);
        // its location
        Location l = model.getAgPos(ag);
        addPercept(agName, Literal.parseLiteral("pos(" + l.x + "," + l.y + ")"));

        if (model.isFigthing(ag)) {
            addPercept(agName, Literal.parseLiteral("carrying_gold"));
        }

        if (agName.equals("radar")) {
            for (int i = 0; i < model.getWidth(); i++) {
                for (int j = 0; j < model.getWidth(); j++) {
                    updateRadarPercept(agName, i, j);
                }
            }
        }
        else if (agName.equals("firealarm")) {
            for (int i = 0; i < model.getWidth(); i++) {
                for (int j = 0; j < model.getWidth(); j++) {
                    updateFirealarmPercept(agName, i, j);
                }
            }
        }
        else {
            // what's around
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    updateAgPercept(agName, l.x + i, l.y + j);
                }
            }
            /*
            updateAgPercept(agName, l.x - 1, l.y - 1);
            updateAgPercept(agName, l.x - 1, l.y);
            updateAgPercept(agName, l.x - 1, l.y + 1);
            updateAgPercept(agName, l.x, l.y - 1);
            updateAgPercept(agName, l.x, l.y);
            updateAgPercept(agName, l.x, l.y + 1);
            updateAgPercept(agName, l.x + 1, l.y - 1);
            updateAgPercept(agName, l.x + 1, l.y);
            updateAgPercept(agName, l.x + 1, l.y + 1);
            */
        }
    }


    private void updateAgPercept(String agName, int x, int y) {
        if (model == null || !model.inGrid(x,y)) return;
        if (model.hasObject(WorldModel.OBSTACLE, x, y)) {
            addPercept(agName, Literal.parseLiteral("cell(" + x + "," + y + ",obstacle)"));
        } else {
            if (model.hasObject(WorldModel.ENEMY, x, y)) {
                addPercept(agName, Literal.parseLiteral("cell(" + x + "," + y + ",enemy)"));
            }
            if (model.hasObject(WorldModel.FIRE, x, y)) {
                addPercept(agName, Literal.parseLiteral("cell(" + x + "," + y + ",fire)"));
            }
            if (model.hasObject(WorldModel.AGENT, x, y)) {
                addPercept(agName, Literal.parseLiteral("cell(" + x + "," + y + ",ally)"));
            }
        }
    }

    private void updateRadarPercept(String agName, int x, int y) {
        if (model == null || !model.inGrid(x,y)) return;
        if (model.hasObject(WorldModel.ENEMY, x, y)) {
            addPercept(agName, Literal.parseLiteral("cell(" + x + "," + y + ",enemy)"));
        }
    }

    private void updateFirealarmPercept(String agName, int x, int y) {
        if (model == null || !model.inGrid(x,y)) return;
        if (model.hasObject(WorldModel.FIRE, x, y)) {
            addPercept(agName, Literal.parseLiteral("cell(" + x + "," + y + ",fire)"));
        }
    }

}
