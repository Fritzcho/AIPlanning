package aiplanning;
import java.awt.Point;
import java.io.File;
import java.nio.file.Paths;

import deterministicplanning.models.Plan;
import deterministicplanning.models.WorldModel;
import deterministicplanning.models.pedagogy.ListbasedNongenericWorldModel;
import deterministicplanning.solvers.Planning;
import deterministicplanning.solvers.planningoutcomes.FailedPlanningOutcome;
import deterministicplanning.solvers.planningoutcomes.PlanningOutcome;
import deterministicplanning.solvers.planningoutcomes.SuccessfulPlanningOutcome;
import finitestatemachine.Action;
import finitestatemachine.State;
import obstaclemaps.MapDisplayer;
import obstaclemaps.ObstacleMap;
import obstaclemaps.Path;

public class AssignmentGlobalStructure {
	
	public static void main(String[] args)
	{
		/**
		 * First step of the processing pipeline: sensing
		 * This step provides the decision system with the right information about the environment.
		 * In this case, this information is: where do we start, where do we end, where are the obstacles.
		 */
		File inputFile = Paths.get(args[0]).toFile();
		ObstacleMap om = generateObstacleMap(inputFile);
		Point start = getStart(inputFile);
		Point goal = getEnd(inputFile);
		
		//A bit of free visualisation, for you to better see the map!
		MapDisplayer md = MapDisplayer.newInstance(om);
		md.setVisible(true);
		
		
		State startState = toState(start);
		State goalState = toState(goal);
		
		/**
		 * Second step of the processing pipeline: deciding
		 * This step projects the pre-processed sensory input into a decision
		 * structure  
		 */
		
		WorldModel<State,Action> wm = generateWorldModel(om, goal);
		
		
		PlanningOutcome po = Planning.resolve(wm,startState, goalState, 50);
		
		/**
		 * Third step of the processing pipeline: action
		 * This step turns the outcome of the decision into a concrete action:
		 * either printing that no plan is found or which plan is found.
		 */
		if(po instanceof FailedPlanningOutcome) { System.out.println("No plan could be found."); return;}
		else {
			Plan<State, Action> plan = ((SuccessfulPlanningOutcome)po).getPlan();
			Path p = planToPath(plan);
			md.setPath(p);
			System.out.println("Path found:"+p);
		}
	}

	private static Path planToPath(Plan<State, Action> plan) {
		throw new Error("To be implemented");
	}

	private static State toState(Point start) {
		throw new Error("To be implemented");
	}

	private static ObstacleMap generateObstacleMap(File inputFile) {
		throw new Error("To be implemented");	
		}

	private static Point getEnd(File inputFile) {
		throw new Error("To be implemented");
	}

	private static Point getStart(File inputFile) {
		throw new Error("To be implemented");
	}

	private static WorldModel<State, Action> generateWorldModel(ObstacleMap om, Point goal) {
		/**
		 * This is where you describe your own word model. Checkout deterministicplanning.mains.MainForAiDeveloppers or
		 * deterministicplanning.mains.MainMinimalItKnowledge for some examples of how to implement such function.
		 * 
		 * Note:
		 * In the minimal example presented above, all states, actions, transitions, etc were "hardcoded".
		 * However, in the context of our exercise, you will programmatic constructs will become necessary, 
		 * such as "for" loops and "if then else" blocks.
		 */
		throw new Error("To be implemented");
	}

}
