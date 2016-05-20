package gda.components;

import gda.Goal;
import gda.GoalType;
import gda.planner.Planner;
import gda.planner.GDABotConnection;
import java.util.ArrayList;

/**
 * This class manages goals. Note: This is a singleton class and should be
 * accessed via getInstance
 * 
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 12, 2013
 */
public class GoalManager {

	private static GoalManager instance;

	// private b/c singleton
	private GoalManager() {
		currentGoals = new ArrayList<>();
	}

	private ArrayList<Goal> currentGoals;
	// private Goal currentGoal;
	private int regionIdForGoal = -1;

	public static synchronized GoalManager getInstance() {
		if (instance == null) {
			instance = new GoalManager();
		}
		return instance;
	}

	public void setRegionIdForGoal(int r) {
		this.regionIdForGoal = r;
	}

	public void recieveGoalFromGoalFormulator(GoalType goalType) {
		Goal g = new Goal(goalType);
		this.currentGoals.add(g);
		Planner.getInstance().addPlan(g.getPlan());
	}

	public void recieveGoalFromGoalFormulator(GoalType goalType, int regionId) {
		Goal g = null;
		if (goalType == GoalType.RUSH_DEFEND) {
			g = new Goal(goalType, regionId);
			this.currentGoals.add(g);
			Planner.getInstance().addPlan(g.getPlan());
		}
	}
}
