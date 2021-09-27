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
import markov.impl.PairImpl;
import obstaclemaps.MapDisplayer;
import obstaclemaps.ObstacleMap;
import obstaclemaps.Path;


public class AssignmentGlobalStructure {

	private enum Actions implements Action {NORTH, SOUTH, EAST, WEST, STILL}
	private static final Set<validState> stateList = new LinkedHashSet<>();
	private static Set<SokobanBox> boxes = new HashSet<>();
	private static Set<Point> boxEnds = new HashSet<>();

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
		boxes = getBoxes(inputFile);


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
		WorldModel<validState, Actions> wm;
		if (boxes.isEmpty()) {
			wm = generateWorldModel(om, goal);
		} else {
			wm = generateWorldModel2(om, boxEnds);
		}

		PlanningOutcome po = Planning.resolve(wm,startState, goalState, 50);

		/*
		 * Third step of the processing pipeline: action
		 * This step turns the outcome of the decision into a concrete action:
		 * either printing that no plan is found or which plan is found.
		 */
		if(po instanceof FailedPlanningOutcome) { System.out.println("No plan could be found."); return;}
		else {
			Plan<validState, Actions> plan = ((SuccessfulPlanningOutcome)po).getPlan();
			Path p = planToPath(plan);
			md.setPath(p);
			System.out.println("Path found:"+p);
		}
	}

	private static Path planToPath(Plan<validState, Actions> plan) {
		List<PairImpl<validState, Actions>> pairs = plan.getStateActionPairs();
		List<Path.Direction> directions = new ArrayList<>();
		for (PairImpl<validState,Actions> p: plan.getStateActionPairs()) {
			directions.add(Path.Direction.valueOf(p.getRight().toString()));
		}
		return new Path(pairs.get(0).getLeft().getPoint(), directions);
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

	private static Set<SokobanBox> getBoxes(File inputFile) {
		HashSet<SokobanBox> set = new HashSet<>();
		int y = 0;
		try {
			Scanner fileReader = new Scanner(inputFile);
			while (fileReader.hasNext()) {
				String mapLine = fileReader.nextLine();
				for (int x = 0; x < mapLine.toCharArray().length; x++) {
					char chr = mapLine.toCharArray()[x];
					if (chr == '$') set.add(new SokobanBox(new Point(x,y)));
				}
				y++;
			}
			return set;
		} catch (FileNotFoundException e) {
			System.out.println("Map-file not found");
			return null;
		}
	}

	private static void getBoxEnds(File inputFile) {
		int y = 0;
		try {
			Scanner fileReader = new Scanner(inputFile);
			while (fileReader.hasNext()) {
				String mapLine = fileReader.nextLine();
				for (int x = 0; x < mapLine.toCharArray().length; x++) {
					char chr = mapLine.toCharArray()[x];
					if (chr == '.' || chr == '*') boxEnds.add(new Point(x,y));
				}
				y++;
			}
		} catch (FileNotFoundException e) {
			System.out.println("Map-file not found");
			throw new Error();
		}
	}

	private static WorldModel<validState, Actions> generateWorldModel(ObstacleMap om, Point goal) {
		validState goalState = new validState(goal);
		Stack<validState> potentialStates = new Stack<>();

		potentialStates.add(goalState);

		for(validState v;!potentialStates.isEmpty();){
			v = potentialStates.pop();
			stateList.add(v);
			for(Point p : (v).getAdjacent()){
				validState fresh = new validState(p);
				if(!om.getObstacles().contains(p) && stateList.stream().noneMatch(n-> n.getPoint().equals(p))){
					potentialStates.push(fresh);
				}
			}
		}

		Function<validState, Set<Actions>> getActionsFrom = state -> {
			Set<Actions> possibleActions = new HashSet();
			if (state.getPoint().equals(goal)) {
				possibleActions.add(Actions.STILL);
				return possibleActions;
			} else {
				for(Point p : state.getAdjacent()){
					if(!om.getObstacles().contains(p)) {
						if (p.x != state.getPoint().x)
							possibleActions.add(p.x == state.getPoint().x+1? Actions.EAST: Actions.WEST);
						if (p.y != state.getPoint().y)
							possibleActions.add(p.y == state.getPoint().y+1? Actions.SOUTH: Actions.NORTH);
					}
				}
			}
			return possibleActions;
		};


		BiFunction<validState, Actions, validState> getConsequenceOfPlaying = (state, action) -> switch (action) {
			case EAST -> stateList.stream().filter(n ->
							n.getPoint().equals(new Point(state.getPoint().x+1, state.getPoint().y)))
					.findFirst()
					.orElseThrow();
			case WEST -> stateList.stream().filter(n ->
							n.getPoint().equals(new Point(state.getPoint().x-1, state.getPoint().y)))
					.findFirst()
					.orElseThrow();
			case NORTH -> stateList.stream().filter(n ->
							n.getPoint().equals(new Point(state.getPoint().x, state.getPoint().y-1)))
					.findFirst()
					.orElseThrow();
			case SOUTH -> stateList.stream().filter(n ->
							n.getPoint().equals(new Point(state.getPoint().x, state.getPoint().y+1)))
					.findFirst()
					.orElseThrow();
			case STILL -> state;
		};

		BiFunction<validState, Actions, Double> getReward = (state, action) -> (state.getPoint().equals(goal) ? 0 : -1d);

		return FunctionBasedDeterministicWorldModel.newInstance(
				stateList,
				getConsequenceOfPlaying,
				getReward,
				getActionsFrom);
	}



	private static WorldModel<validState, Actions> generateWorldModel2(ObstacleMap om, Set<Point> goalPoints) {
		Stack<validState> potentialStates = new Stack<>();
		for (Point gp : goalPoints){
			for(Point pAdjacent : new SokobanBox(gp).getAdjacent()){
				if(!om.getObstacles().contains(pAdjacent) && goalPoints.stream().noneMatch(p -> p.equals(pAdjacent))){
					validState newValidState = new validState(pAdjacent);
					for(Point gp2: goalPoints){
						newValidState.addBox(new SokobanBox(gp2));
					}
					potentialStates.add(newValidState);
				}
			}
		}

			for(validState v;!potentialStates.isEmpty();){
				v = potentialStates.pop();
				stateList.add(v);
				for(SokobanBox box : v.getBoxes()){
					for(Point p : box.getAdjacent()){
						if(!om.getObstacles().contains(p) && v.getBoxes().stream().noneMatch(b -> b.getPoint().equals(p))
						&& p != v.getPoint()){
							validState fresh = v;
							fresh.getBoxes().stream().filter(n -> n.getPoint().equals(box.getPoint()))
									.findFirst()
									.orElseThrow()
									.setPoint(p);
							potentialStates.push(fresh);
						}
					}
				}

				for(Point p : (v).getAdjacent()){
					validState fresh = v;
					if(!om.getObstacles().contains(p) && fresh.getBoxes().stream().noneMatch(b-> b.getPoint().equals(p))){
						potentialStates.push(fresh);
					}
				}
			}

		Function<validState, Set<Actions>> getActionsFrom = state -> {
			Set<Actions> possibleActions = new HashSet();
			if (state.getPoint().equals(goal)) {
				possibleActions.add(Actions.STILL);
				return possibleActions;
			} else {
				for(Point p : state.getAdjacent()){
					if(!om.getObstacles().contains(p)) {
						if (p.x != state.getPoint().x)
							possibleActions.add(p.x == state.getPoint().x+1? Actions.EAST: Actions.WEST);
						if (p.y != state.getPoint().y)
							possibleActions.add(p.y == state.getPoint().y+1? Actions.SOUTH: Actions.NORTH);
					}
				}
			}
			return possibleActions;
		};


		BiFunction<validState, Actions, validState> getConsequenceOfPlaying = (state, action) -> switch (action) {
			case EAST -> stateList.stream().filter(n ->
							n.getPoint().equals(new Point(state.getPoint().x+1, state.getPoint().y)))
					.findFirst()
					.orElseThrow();
			case WEST -> stateList.stream().filter(n ->
							n.getPoint().equals(new Point(state.getPoint().x-1, state.getPoint().y)))
					.findFirst()
					.orElseThrow();
			case NORTH -> stateList.stream().filter(n ->
							n.getPoint().equals(new Point(state.getPoint().x, state.getPoint().y-1)))
					.findFirst()
					.orElseThrow();
			case SOUTH -> stateList.stream().filter(n ->
							n.getPoint().equals(new Point(state.getPoint().x, state.getPoint().y+1)))
					.findFirst()
					.orElseThrow();
			case STILL -> state;
		};

		BiFunction<validState, Actions, Double> getReward = (state, action) -> (state.getPoint().equals(goal) ? 0 : -1d);

		return FunctionBasedDeterministicWorldModel.newInstance(
				stateList,
				getConsequenceOfPlaying,
				getReward,
				getActionsFrom);
	}
}