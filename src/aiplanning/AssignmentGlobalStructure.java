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
		Point goal = getEnd(inputFile);
		boxes = getBoxes(inputFile);
		getBoxEnds(inputFile);


		//A bit of free visualisation, for you to better see the map!
		MapDisplayer md = MapDisplayer.newInstance(om);
		md.setVisible(true);

		validState startState;
		validState goalState;


		if(boxes.isEmpty()){
			startState = toState(start);
			goalState = toState(goal);
		}else{
			startState = toStateSokoban(start, boxes);

			Set<SokobanBox> b = new HashSet<>();
			for(Point p : boxEnds){
				b.add(new SokobanBox(p));
			}
			goalState = toStateSokoban(goal, b);
		}


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
		HashMap<String, HashMap<String,Double>> states = new HashMap<>(); //hashmap som lagrar hashmaps, de e galet
		Stack<validState> potentialStates = new Stack<>();
		Set<SokobanBox> goalBoxes = new HashSet<>();

		for (Point gp : goalPoints){ //Initialize all potential final states
			goalBoxes.add(new SokobanBox(gp));
		}

		for (SokobanBox sb : goalBoxes){ //Initialize all potential final states
			HashMap<String, Double> points = new HashMap();
			for(Point pAdjacent : sb.getAdjacent()){
				if(!om.getObstacles().contains(pAdjacent) && goalPoints.stream().noneMatch(p -> p.equals(pAdjacent))){
					validState fresh = new validState(pAdjacent, goalBoxes);
					points.put(fresh.getPoint().toString(), 1d);
					potentialStates.push(fresh);
				}
			}
			states.put(goalBoxes.toString(), points);
		}

		int x = 0;
		for(validState v;!potentialStates.isEmpty();){
			v = potentialStates.pop();
			stateList.add(v);

			x++;
			System.out.println(x);

			if(x > 500000){ //debug för att printa antalet states skit bah att ignorera
				break;
			}

			for(Point p : (v).getAdjacent()){ //Search for next potential states for player
				validState fresh = new validState(p, v.boxes);
				if(!om.getObstacles().contains(p) && fresh.boxes.stream().noneMatch(b-> b.getPoint().equals(p))
						&& !states.get(fresh.boxes.toString()).containsKey(fresh.getPoint().toString())){
					states.get(fresh.boxes.toString()).put(fresh.getPoint().toString(), 1d);
					potentialStates.push(fresh);
				}
			}

			for(SokobanBox box : v.getBoxes()){//Search for next potential states for each box
				for(Point p : box.getAdjacent()){
					if(!om.getObstacles().contains(p) && !v.hasBox(p) && !p.equals(v.getPoint())){
						validState fresh = new validState(v.getPoint(), new HashSet<>(v.getBoxes()));
						fresh.getBox(box.getPoint()).setPoint(p);
						if(!states.containsKey(fresh.boxes.toString())){
							states.put(fresh.boxes.toString(), new HashMap<>());
						}

						if(!states.get(fresh.boxes.toString()).containsKey(fresh.getPoint().toString())){
							states.get(fresh.boxes.toString()).put(fresh.getPoint().toString(), 1d);
							potentialStates.push(fresh);
						}
					}
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
					} else {
						if (p.equals(state.getEast()) &&
								!state.hasBox(state.getBox(p).getEast()) &&
								!om.getObstacles().contains(state.getBox(p).getEast()))
							possibleActions.add(Actions.EAST);
						if (p.equals(state.getWest()) &&
								!state.hasBox(state.getBox(p).getWest()) &&
								!om.getObstacles().contains(state.getBox(p).getWest()))
							possibleActions.add(Actions.WEST);
						if (p.equals(state.getSouth()) &&
								!state.hasBox(state.getBox(p).getSouth()) &&
								!om.getObstacles().contains(state.getBox(p).getSouth()))
							possibleActions.add(Actions.SOUTH);
						if (p.equals(state.getNorth()) &&
								!state.hasBox(state.getBox(p).getNorth()) &&
								!om.getObstacles().contains(state.getBox(p).getNorth()))
							possibleActions.add(Actions.NORTH);
					}
				}
			}
			return possibleActions;
		};


		BiFunction<validState, Actions, validState> getConsequenceOfPlaying = (state, action) -> {
			validState v = new validState(state.getPoint(), state.getBoxes());

			switch (action) {
				case EAST: {
					v.getPoint().setLocation(v.getEast());
					if (v.hasBox(v.getEast())) {
						v.getBox(v.getEast()).moveEast();
					}
				}
				case WEST: {
					v.getPoint().setLocation(v.getWest());
					if (v.hasBox(v.getWest())) {
						v.getBox(v.getWest()).moveWest();
					}
				}
				case NORTH: {
					v.getPoint().setLocation(v.getNorth());
					if (v.hasBox(v.getNorth())) {
						v.getBox(v.getNorth()).moveNorth();
					}
				}
				case SOUTH: {
					v.getPoint().setLocation(v.getSouth());
					if (v.hasBox(v.getSouth())) {
						v.getBox(v.getSouth()).moveSouth();
					}
				}
			}

			return stateList.stream().filter(n -> n.equals(v)).findFirst().orElseThrow();
		};

		BiFunction<validState, Actions, Double> getReward = (state, action) -> {
			double score = 0d;
			for(SokobanBox b : state.boxes){
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
}