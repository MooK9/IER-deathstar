package deathstar;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import deathstar.DeathStar.Move;

public class WorldModel extends GridWorldModel {

    public static final int   DAMAGE_TO_ENEMY  = 16;
    public static final int   ENEMY  = 240;
    public static final int   DAMAGE_TO_FIRE = 256;
    public static final int   FIRE = 3840;
    public static final int   SIREN_RED = 4096;
    public static final int   SIREN_BLUE = 8192;
    

    Set<Integer>              agFighting;  // which agent is fighting
    List<Location>            base;
    Set<Location>             inside;
    Set<Location>             outside;

    private Logger            logger   = Logger.getLogger("jasonTeamSimLocal.mas2j." + WorldModel.class.getName());

    private String            id = "WorldModel";
    
    int nbSpaceships = 4;
    int nbStormtroopers = 1;

    // singleton pattern
    protected static WorldModel model = null;

    synchronized public static WorldModel create(int w, int h, int nbAgs) {
        if (model == null) {
            model = new WorldModel(w, h, nbAgs);
        }
        return model;
    }

    public static WorldModel get() {
        return model;
    }

    public static void destroy() {
        model = null;
    }

    private WorldModel(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
        agFighting = new HashSet<Integer>();
        base = new ArrayList<Location>();
        inside = new HashSet<Location>();
        outside = new HashSet<Location>();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String toString() {
        return id;
    }

    public List<Location> getBase() {
        return base;
    }

    public Location getBase(int ag) {
        return base.get(ag);
    }
    
    public boolean isFigthing(int ag) {
        return agFighting.contains(ag);
    }
    
    public void addBase(int x, int y) {
        base.add(new Location(x, y));
    }
    
    public void setAgFighting(int ag) {
        agFighting.add(ag);
    }
    public void setAgNotFighting(int ag) {
        agFighting.remove(ag);
    }
    
    public void subtract(int value, Location l) {
        subtract(value, l.x, l.y);
    }

    public void subtract(int value, int x, int y) {
        data[x][y] -= value;
        if (view != null) view.update(x,y);
    }

    public void addInside(int x, int y) {
        inside.add(new Location(x, y));
    }

    public void addOutside(int x, int y) {
        outside.add(new Location(x, y));
    }

    public boolean isInside(int x, int y) {
        return inside.contains(new Location(x, y));
    }

    public boolean isOutside(int x, int y) {
        return outside.contains(new Location(x, y));
    }

    /** Actions **/

    boolean move(Move dir, int ag) throws Exception {
        Location l = getAgPos(ag);
        switch (dir) {
        case UP:
            if (isFree(l.x, l.y - 1)) {
                setAgPos(ag, l.x, l.y - 1);
            }
            break;
        case DOWN:
            if (isFree(l.x, l.y + 1)) {
                setAgPos(ag, l.x, l.y + 1);
            }
            break;
        case RIGHT:
            if (isFree(l.x + 1, l.y)) {
                setAgPos(ag, l.x + 1, l.y);
            }
            break;
        case LEFT:
            if (isFree(l.x - 1, l.y)) {
                setAgPos(ag, l.x - 1, l.y);
            }
            break;
        }
        return true;
    }

    boolean fight(int ag) {
        Location l = getAgPos(ag);
        if (hasObject(ENEMY, l.x, l.y)) {
            subtract(DAMAGE_TO_ENEMY, l.x, l.y);
            setAgFighting(ag);
            return true;
        }
        else {
            setAgNotFighting(ag);
            logger.warning("Agent " + (ag + 1) + " is trying to fight, but there is no enemy at " + l.x + "x" + l.y + "!");
        }
        return false;
    }

    boolean destroy(int ag) {
        Location l = getAgPos(ag);
        if (isFigthing(ag)) {
            logger.info("Agent " + (ag + 1) + " destroyed an enemy!");
            //add(WorldModel.RESOURCE, l.x, l.y);
            setAgNotFighting(ag);
            return true;
        }
        return false;
    }

    boolean extinguish(int ag) {
        Location l = getAgPos(ag);
        remove(FIRE, l.x, l.y);
        return true;
    }

    boolean siren_on(int ag) {
        Location l1 = new Location(24, 26);
        Location l2 = new Location(25, 26);
        remove(SIREN_RED, l1.x, l1.y);
        add(SIREN_BLUE, l1.x, l1.y);
        remove(SIREN_BLUE, l2.x, l2.y);
        add(SIREN_RED, l2.x, l2.y);
        return true;
    }

    boolean siren_off(int ag) {
        Location l1 = new Location(24, 26);
        Location l2 = new Location(25, 26);
        remove(SIREN_RED, l1.x, l1.y);
        remove(SIREN_BLUE, l1.x, l1.y);
        remove(SIREN_RED, l2.x, l2.y);
        remove(SIREN_BLUE, l2.x, l2.y);
        return true;
    }

    boolean change_color(int ag) {
        Location l1 = new Location(24, 26);
        Location l2 = new Location(25, 26);
        if (hasObject(SIREN_RED, l1.x, l1.y)) {
            remove(SIREN_RED, l1.x, l1.y);
            add(SIREN_BLUE, l1.x, l1.y);
            remove(SIREN_BLUE, l2.x, l2.y);
            add(SIREN_RED, l2.x, l2.y);
            return true;
        }
        else {
            remove(SIREN_BLUE, l1.x, l1.y);
            add(SIREN_RED, l1.x, l1.y);
            remove(SIREN_RED, l2.x, l2.y);
            add(SIREN_BLUE, l2.x, l2.y);
            return true;
        }
    }

    /** no enemy/no fire/no obstacle world */
    static WorldModel world1() throws Exception {
        WorldModel model = WorldModel.create(21, 21, 4);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 3, 20);
        model.setAgPos(3, 20, 20);
        return model;
    }
	
 /** world with enemies, fires and obstacles */
    static WorldModel world4() throws Exception {
        WorldModel model = WorldModel.create(35, 35, 7);
        model.setId("Scenario 6");
        model.addBase(3, 29);
        model.addBase(31, 29);
        model.addBase(30, 4);
        model.addBase(4, 4);
        model.addBase(24, 27);
        model.addBase(32, 33);

        //radar
        model.setAgPos(0, 17, 24);

        //firealarm
        model.setAgPos(1, 24, 31);

        //spaceships
        model.setAgPos(2, 1, 0);
        model.setAgPos(3, 20, 0);
        model.setAgPos(4, 6, 26);
        model.setAgPos(5, 20, 20);

		//stormtroopers
        model.setAgPos(6, 22, 28);

        model.add(WorldModel.ENEMY, 20, 13);
        model.add(WorldModel.ENEMY, 15, 20);
        model.add(WorldModel.ENEMY, 1, 1);
        model.add(WorldModel.ENEMY, 3, 5);
        
        model.add(WorldModel.FIRE, 3, 34);
        model.add(WorldModel.FIRE, 23, 26);
        model.add(WorldModel.FIRE, 31, 32);
        

        model.add(WorldModel.OBSTACLE, 1, 34);
		model.add(WorldModel.OBSTACLE, 1, 33);
		model.add(WorldModel.OBSTACLE, 2, 32);
		model.add(WorldModel.OBSTACLE, 3, 31);
        model.add(WorldModel.OBSTACLE, 3, 30);
		model.add(WorldModel.OBSTACLE, 4, 29);
		model.add(WorldModel.OBSTACLE, 5, 28);
		model.add(WorldModel.OBSTACLE, 6, 27);
		model.add(WorldModel.OBSTACLE, 7, 26);
		model.add(WorldModel.OBSTACLE, 8, 26);
		
		model.add(WorldModel.OBSTACLE, 9, 25);
		model.add(WorldModel.OBSTACLE, 10, 24);
		model.add(WorldModel.OBSTACLE, 11, 24);
		model.add(WorldModel.OBSTACLE, 12, 24);
		
		//top
		for(int i=13;i<22;i++)
			model.add(WorldModel.OBSTACLE, i, 24);
		//right
		model.add(WorldModel.OBSTACLE, 25, 25);
		model.add(WorldModel.OBSTACLE, 22, 24);
		model.add(WorldModel.OBSTACLE, 23, 24);
		model.add(WorldModel.OBSTACLE, 24, 24);
		
		model.add(WorldModel.OBSTACLE, 25, 25);
		model.add(WorldModel.OBSTACLE, 26, 26);
		model.add(WorldModel.OBSTACLE, 27, 26);
		model.add(WorldModel.OBSTACLE, 28, 27);
		model.add(WorldModel.OBSTACLE, 29, 28);
		model.add(WorldModel.OBSTACLE, 30, 29);
		model.add(WorldModel.OBSTACLE, 31, 30);
		model.add(WorldModel.OBSTACLE, 31, 31);
		model.add(WorldModel.OBSTACLE, 31, 30);
		model.add(WorldModel.OBSTACLE, 32, 32);
		model.add(WorldModel.OBSTACLE, 33, 33);
		model.add(WorldModel.OBSTACLE, 33, 34);
		
		//inner circle
		model.add(WorldModel.OBSTACLE, 26, 27);
		model.add(WorldModel.OBSTACLE, 31, 30);
		model.add(WorldModel.OBSTACLE, 27, 27);
		model.add(WorldModel.OBSTACLE, 25, 28);
		
		model.add(WorldModel.OBSTACLE, 24, 29);
		model.add(WorldModel.OBSTACLE, 24, 30);
		model.add(WorldModel.OBSTACLE, 24, 31);
		
		model.add(WorldModel.OBSTACLE, 25, 32);
		model.add(WorldModel.OBSTACLE, 26, 33);
		model.add(WorldModel.OBSTACLE, 27, 33);
		model.add(WorldModel.OBSTACLE, 28, 33);
		model.add(WorldModel.OBSTACLE, 29, 32);
		model.add(WorldModel.OBSTACLE, 30, 31);
		model.add(WorldModel.OBSTACLE, 30, 30);
		
		//dot
        model.add(WorldModel.OBSTACLE, 27, 30);
        
        //outside
        for (int i = 0; i < 35; i++) {
            for (int j = 0; j < 24; j++) {
                model.addOutside(i, j);
            }
        }
        for (int i = 0; i < 10; i++) {
            model.addOutside(i, 24);
            model.addOutside(34 - i, 24);
        }
        for (int i = 0; i < 9; i++) {
            model.addOutside(i, 25);
            model.addOutside(34 - i, 25);
        }
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 4; j++) {
                model.addOutside(i - j, 26 + j);
                model.addOutside(34 - i + j, 26 + j);
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                model.addOutside(i, 30 + j);
                model.addOutside(34 - i, 30 + j);
            }
        }
        for (int i = 0; i < 2; i++) {
            model.addOutside(i, 32);
            model.addOutside(34 - i, 32);
        }
        for (int i = 0; i < 2; i++) {
            model.addOutside(0, 33 + i);
            model.addOutside(34, 33 + i);
        }

        //inside
        for (int i = 10; i <= 24; i++) {
            model.addInside(i, 25);
        }
        for (int i = 9; i <= 25; i++) {
            model.addInside(i, 26);
        }
        for (int i = 7; i <= 25; i++) {
            model.addInside(i, 27);
        }
        for (int i = 6; i <= 24; i++) {
            model.addInside(i, 28);
        }
        for (int i = 5; i <= 23; i++) {
            model.addInside(i, 29);
        }
        for (int i = 4; i <= 23; i++) {
            model.addInside(i, 30);
        }
        for (int i = 4; i <= 23; i++) {
            model.addInside(i, 31);
        }
        for (int i = 3; i <= 24; i++) {
            model.addInside(i, 32);
        }
        for (int i = 30; i <= 31; i++) {
            model.addInside(i, 32);
        }
        for (int i = 2; i <= 25; i++) {
            model.addInside(i, 33);
        }
        for (int i = 29; i <= 32; i++) {
            model.addInside(i, 33);
        }
        for (int i = 2; i <= 32; i++) {
            model.addInside(i, 34);
        }

		
        return model;
    }	
}
