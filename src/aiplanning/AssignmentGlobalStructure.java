package aiplanning;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import deterministicplanning.models.FunctionBasedDeterministicWorldModel;
import deterministicplanning.models.Plan;
import deterministicplanning.models.WorldModel;
import deterministicplanning.solvers.Planning;
import deterministicplanning.solvers.planningoutcomes.FailedPlanningOutcome;
import deterministicplanning.solvers.planningoutcomes.PlanningOutcome;
import deterministicplanning.solvers.planningoutcomes.SuccessfulPlanningOutcome;
import finitestatemachine.Action;
import finitestatemachine.State;
import finitestatemachine.impl.StringStateImpl;
import obstaclemaps.MapDisplayer;
import obstaclemaps.ObstacleMap;
import obstaclemaps.Path;


public class AssignmentGlobalStructure {
	static class validState implements State {
		private final Point point;

		public validState(Point p) {
			this.point = p;
		}

		public Point getPoint(){
			return point;
		}

		public Set<Point> getAdjacent(){
			Set<Point> s = new HashSet<>();

			s.add(new Point((int)point.getX()+1, (int)point.getY()));
			s.add(new Point((int)point.getX()-1, (int)point.getY()));
			s.add(new Point((int)point.getX(), (int)point.getY()+1));
			s.add(new Point((int)point.getX(), (int)point.getY()-1));

			return s;
		}

		public String toString() {
			return "State: ("+(point.x+1)+","+(point.y+1)+")";
		}

	}

	private enum Actions implements Action {NORTH, SOUTH, EAST, WEST, STILL}
	private static final Set<validState> stateList = new HashSet<>();

	public static void main(String[] args)
	{
		/*
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


		validState startState = toState(start);
		validState goalState = toState(goal);

		/*
		 * Second step of the processing pipeline: deciding
		 * This step projects the pre-processed sensory input into a decision
		 * structure
		 */

		WorldModel<validState, Actions> wm = generateWorldModel(om, goal);


		PlanningOutcome po = Planning.resolve(wm,startState, goalState, 50);

		/*
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

	private static validState toState(Point start) {
		validState v = new validState(start);
		stateList.add(v);
		return v;
	}

	private static ObstacleMap generateObstacleMap(File inputFile) {
		HashSet<Point> set = new HashSet<>();
		int height = 0, width = 0;
		try {
			Scanner fileReader = new Scanner(inputFile);
			while (fileReader.hasNext()) {
				String mapLine = fileReader.nextLine();
				if (mapLine.length() > width) width = mapLine.length();
				for (int x = 0; x < mapLine.toCharArray().length; x++) {
					char chr = mapLine.toCharArray()[x];
					if (chr == '#') set.add(new Point(x, height));
				}
				height++;
			}
			return new ObstacleMap(width, height, set);
		} catch (FileNotFoundException e) {
			System.out.println("Map-file not found");
			return null;
		}
	}

	private static Point getEnd(File inputFile) {
		int y = 0;
		try {
			Scanner fileReader = new Scanner(inputFile);
			while (fileReader.hasNext()) {
				String mapLine = fileReader.nextLine();
				for (int x = 0; x < mapLine.toCharArray().length; x++) {
					char chr = mapLine.toCharArray()[x];
					if (chr == '.') return (new Point(x,y));
				}
				y++;
			}
		} catch (FileNotFoundException e) {
			System.out.println("Map-file not found");
		}
		throw new Error();
	}

	private static Point getStart(File inputFile) {
		int y = 0;
		try {
			Scanner fileReader = new Scanner(inputFile);
			while (fileReader.hasNext()) {
				String mapLine = fileReader.nextLine();
				for (int x = 0; x < mapLine.toCharArray().length; x++) {
					char chr = mapLine.toCharArray()[x];
					if (chr == '@') return (new Point(x,y));
				}
				y++;
			}
		} catch (FileNotFoundException e) {
			System.out.println("Map-file not found");
		}
		throw new Error();
	}

	private static WorldModel<validState, Actions> generateWorldModel(ObstacleMap om, Point goal) {
		//validState goalState = new validState(goal);

		//stateList.add(goalState);

		for (int y =0; y < om.getHeight(); y++) {
			for  (int x = 0; x< om.getWidth(); x++){
				final Point p = new Point(x,y);
				if(!om.getObstacles().contains(p) && !stateList.stream().anyMatch(n ->
						n.getPoint().equals(p))){
					stateList.add(new validState(p));
					System.out.println("Added state");
				}
			}
		}

		Function<validState, Set<Actions>> getActionsFrom = state -> {
			Set<Actions> possibleActions = new HashSet();
			if (state.point.equals(goal)) {
				possibleActions.add(Actions.STILL);
				return possibleActions;
			} else {
				for(Point p : state.getAdjacent()){
					if(!om.getObstacles().contains(p)) {
						if (p.x == state.point.x+1) {
							possibleActions.add(Actions.EAST);
						}
						if (p.x == state.point.x-1) {
							possibleActions.add(Actions.WEST);
						}
						if (p.y == state.point.y+1) {
							possibleActions.add(Actions.NORTH);
						}
						if (p.y == state.point.y-1) {
							possibleActions.add(Actions.SOUTH);
						}
					}
				}
			}
			return possibleActions;
		};

		BiFunction<validState, Actions, validState> getConsequenceOfPlaying = (state, action) -> {
			for (validState v:stateList) {
				System.out.println("Valid states:"+v.toString());
			}
			System.out.println("Current state:"+state.toString());
			switch (action) {
				case EAST:
					System.out.println("Enter East on position:"+state.toString());
					return new validState(new Point((int)state.getPoint().getX() + 1,
						(int)state.getPoint().getY()));
				case WEST:
					System.out.println("Enter West on position:"+state.toString());
					return new validState(new Point((int)state.getPoint().getX() - 1,
						(int)state.getPoint().getY()));
				case NORTH:
					System.out.println("Enter North on position:"+state.toString());
					return new validState(new Point((int)state.getPoint().getX(),
						(int)state.getPoint().getY() - 1));
				case SOUTH:
					System.out.println("Enter South on position:"+state.toString());
					return new validState(new Point((int)state.getPoint().getX(),
						(int)state.getPoint().getY() + 1));
				case STILL:
					System.out.println("Enter Still on position:"+state.toString());
					return state;
				default: throw new Error();
			}
		};

		BiFunction<validState, Actions, Double> getReward = (state, action) -> -1d;

		return FunctionBasedDeterministicWorldModel.newInstance(
				stateList,
				getConsequenceOfPlaying,
				getReward,
				getActionsFrom);
	}
}