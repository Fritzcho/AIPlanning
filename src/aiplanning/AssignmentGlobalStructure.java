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
	private static HashSet<SokobanBox> boxes = new LinkedHashSet<>();
	private static HashSet<Point> boxEnds = new LinkedHashSet<>();

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
		boxes = getBoxes(inputFile);
		getBoxEnds(inputFile);


		//A bit of free visualisation, for you to better see the map!
		MapDisplayer md = MapDisplayer.newInstance(om);
		md.setVisible(true);

		validState startState;
		validState goalState;

		startState = toStateSokoban(start, boxes);
		Set<SokobanBox> b = new HashSet<>();
		for(Point p : boxEnds){
			b.add(new SokobanBox(p));
		}
		goalState = new validState(b);



		/*
		 * Second step of the processing pipeline: deciding
		 * This step projects the pre-processed sensory input into a decision
		 * structure
		 */
		WorldModel<validState, Actions> wm;
		wm = generateWorldModel2(om, boxEnds);

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


	private static validState toStateSokoban(Point start, Set<SokobanBox> b) {
		validState v = new validState(start, b);
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

	private static HashSet<SokobanBox> getBoxes(File inputFile) {
		HashSet<SokobanBox> set = new HashSet<>();
		int y = 0;
		try {
			Scanner fileReader = new Scanner(inputFile);
			while (fileReader.hasNext()) {
				String mapLine = fileReader.nextLine();
				for (int x = 0; x < mapLine.toCharArray().length; x++) {
					char chr = mapLine.toCharArray()[x];
					if (chr == '$' || chr == '*') set.add(new SokobanBox(new Point(x,y)));
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
					if (chr == '.' || chr == '*'){
						boxEnds.add(new Point(x,y));
					}

				}
				y++;
			}
		} catch (FileNotFoundException e) {
			System.out.println("Map-file not found");
			throw new Error();
		}
	}

	private static WorldModel<validState, Actions> generateWorldModel2(ObstacleMap om, Set<Point> goalPoints) {

		for (int x = 0; x < om.getWidth(); x++) {
			for (int y = 0; y < om.getHeight(); y++) {
				if (!om.getObstacles().contains(new Point(x, y))) {
					stateList.add(new validState(new Point(x,y), boxes));
				}
			}
		}

		Function<validState, Set<Actions>> getActionsFrom = state -> {
			Set<Actions> possibleActions = new HashSet<>();
			for(Point p : state.getAdjacent()) {
				if(!om.getObstacles().contains(p)) {
					if (!state.hasBox(p)) {
						if (p.x != state.getPoint().x)
							possibleActions.add(p.x == state.getPoint().x + 1 ? Actions.EAST : Actions.WEST);
						if (p.y != state.getPoint().y)
							possibleActions.add(p.y == state.getPoint().y + 1 ? Actions.SOUTH : Actions.NORTH);
					} else if (state.hasBox(p)){
						if (state.getBox(p).getPoint().equals(state.getEast()) &&
								!state.hasBox(state.getBox(p).getEast()) &&
								!om.getObstacles().contains(state.getBox(p).getEast()))
							possibleActions.add(Actions.EAST);
						if (state.getBox(p).getPoint().equals(state.getWest()) &&
								!state.hasBox(state.getBox(p).getWest()) &&
								!om.getObstacles().contains(state.getBox(p).getWest()))
							possibleActions.add(Actions.WEST);
						if (state.getBox(p).getPoint().equals(state.getSouth()) &&
								!state.hasBox(state.getBox(p).getSouth()) &&
								!om.getObstacles().contains(state.getBox(p).getSouth()))
							possibleActions.add(Actions.SOUTH);
						if (state.getBox(p).getPoint().equals(state.getNorth()) &&
								!state.hasBox(state.getBox(p).getNorth()) &&
								!om.getObstacles().contains(state.getBox(p).getNorth()))
							possibleActions.add(Actions.NORTH);
					}
				}
			}
			return possibleActions;
		};


		BiFunction<validState, Actions, validState> getConsequenceOfPlaying = (state, action) -> {

			switch (action) {
				case EAST: {
					if (state.hasBox(state.getEast())) {
						state.getBox(state.getEast()).moveEast();
						for (validState v : stateList) {
							v.setBoxes(state.getBoxes());
						}
					}
					return stateList.stream().filter(n ->
							n.getPoint().equals(new Point(state.getPoint().x+1, state.getPoint().y)))
							.findFirst()
							.orElseThrow();
				}
				case WEST: {
					if (state.hasBox(state.getWest())) {
						state.getBox(state.getWest()).moveWest();
						for (validState v: stateList) {
							v.setBoxes(state.getBoxes());
						}
					}
					return stateList.stream().filter(n ->
							n.getPoint().equals(new Point(state.getPoint().x-1, state.getPoint().y)))
							.findFirst()
							.orElseThrow();
				}
				case NORTH: {
					if (state.hasBox(state.getNorth())) {
						state.getBox(state.getNorth()).moveNorth();
						for (validState v: stateList) {
							v.setBoxes(state.getBoxes());
						}
					}
					//state.getPoint().setLocation(state.getNorth());
					return stateList.stream().filter(n ->
							n.getPoint().equals(new Point(state.getPoint().x, state.getPoint().y-1)))
							.findFirst()
							.orElseThrow();
				}
				case SOUTH: {
					if (state.hasBox(state.getSouth())) {
						state.getBox(state.getSouth()).moveSouth();
						for (validState v: stateList) {
							v.setBoxes(state.getBoxes());
						}
					}
					return stateList.stream().filter(n ->
							n.getPoint().equals(new Point(state.getPoint().x, state.getPoint().y+1)))
							.findFirst()
							.orElseThrow();
				}
				default: return state;
			}
		};

		BiFunction<validState, Actions, Double> getReward = (state, action) -> {
			double score = 0;
			for(SokobanBox b : state.getBoxes()){
				if(goalPoints.contains(b.getPoint())){score++;}
			}
			return score;
		};

		return FunctionBasedDeterministicWorldModel.newInstance(
				stateList,
				getConsequenceOfPlaying,
				getReward,
				getActionsFrom);
	}
}