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
import markov.impl.PairImpl;
import obstaclemaps.MapDisplayer;
import obstaclemaps.ObstacleMap;
import obstaclemaps.Path;


public class AssignmentGlobalStructure {

	private enum Actions implements Action {NORTH, SOUTH, EAST, WEST, STILL}
	private static final Set<validState> stateList = new LinkedHashSet<>();
	private static LinkedHashSet<SokobanBox> boxes = new LinkedHashSet<>();
	private static LinkedHashSet<Point> boxEnds = new LinkedHashSet<>();
	private static validState startState;
	private static validState goalState;

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


		if(boxes.isEmpty()){
			startState = toState(start);
			goalState = toState(goal);
		}else{
			startState = toStateSokoban(start, boxes);

			LinkedHashSet<SokobanBox> b = new LinkedHashSet<>();
			for(Point p : boxEnds){
				b.add(new SokobanBox(p));
			}

			goalState = toStateSokoban(start, b);

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
			wm = generateWorldModelSokoban(om, boxEnds);
			System.out.println(wm.getStates().size());
		}

		PlanningOutcome po = Planning.resolve(wm,startState, goalState, 150);


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

	private static validState toState(Point start) {
		validState v = new validState(start);
		stateList.add(v);

		return v;
	}

	private static validState toStateSokoban(Point start, LinkedHashSet<SokobanBox> b) {
		validState v = new validState(start, b);
		stateList.add(v);
		return v;
	}

	private static Path planToPath(Plan<validState, Actions> plan) {
		List<PairImpl<validState, Actions>> pairs = plan.getStateActionPairs();
		List<Path.Direction> directions = new ArrayList<>();
		for (PairImpl<validState,Actions> p: plan.getStateActionPairs()) {
			directions.add(Path.Direction.valueOf(p.getRight().toString()));
		}
		return new Path(pairs.get(0).getLeft().getPoint(), directions);
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

	private static LinkedHashSet<SokobanBox> getBoxes(File inputFile) {
		LinkedHashSet<SokobanBox> set = new LinkedHashSet<>();
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

	private static WorldModel<validState, Actions> generateWorldModelSokoban(ObstacleMap om, Set<Point> goalPoints) {
		LinkedHashSet<validState> potentialStates = new LinkedHashSet<>();
		Set<SokobanBox> goalBoxes = new LinkedHashSet<>();
		HashMap<Integer, HashMap<Integer, validState>> statesMap = new LinkedHashMap<>();
		LinkedHashSet<validState> states = new LinkedHashSet<>(stateList);

		for (Point gp : goalPoints){ //Initialize all potential final statesMap
			goalBoxes.add(new SokobanBox(gp));
		}

		for (SokobanBox sb : goalBoxes){ //Initialize all potential final statesMap
			HashMap<Integer, Double> points = new HashMap<>();
			for(Point pAdjacent : sb.getAdjacent()){
				if(!om.getObstacles().contains(pAdjacent) && goalPoints.stream().noneMatch(p -> p.equals(pAdjacent))){
					validState fresh = new validState(pAdjacent, new HashSet<>(goalBoxes));
					int hash = goalBoxes.toString().hashCode();
					points.put(fresh.getPoint().toString().hashCode(), 1d);

					if(!statesMap.containsKey(hash)){
						statesMap.put(hash, new HashMap<Integer, validState>());
					}

					if(!statesMap.get(hash).containsKey(fresh.getPoint().toString().hashCode())){
						statesMap.get(hash).put(fresh.getPoint().toString().hashCode(), fresh);
						states.add(fresh);
						potentialStates.add(fresh);
					}
				}
			}

		}

		while (!potentialStates.isEmpty()){
			LinkedHashSet<validState> toCheck = new LinkedHashSet<>(potentialStates);
			System.out.println(states.size());
			states.addAll(potentialStates);
			potentialStates.clear();
			for(validState v:toCheck){
				for(Point p : (v).getAdjacent()){ //Search for next potential statesMap for player
					validState fresh = new validState(p, v.boxes);
					int hash = fresh.boxes.toString().hashCode();
					if(!om.getObstacles().contains(p) && fresh.boxes.stream().noneMatch(b-> b.getPoint().equals(p))
							&& !statesMap.get(hash).containsKey(fresh.getPoint().toString().hashCode())){
						statesMap.get(hash).put(fresh.getPoint().toString().hashCode(), fresh);

						potentialStates.add(fresh);
					}
				}

				for(SokobanBox box : v.getBoxes()){//Search for next potential statesMap for each box
					for(Point p : box.getAdjacent()){
						if(!om.getObstacles().contains(p) && !v.hasBox(p) && !p.equals(v.getPoint())){
							validState fresh = new validState(v.getPoint(), v.getBoxes());
							fresh.getBox(box.getPoint()).setPoint(p);
							int hash = fresh.boxes.toString().hashCode();
							if(!statesMap.containsKey(hash)){
								statesMap.put(hash, new HashMap<>());
							}

							if(!statesMap.get(hash).containsKey(fresh.getPoint().toString().hashCode())){
								statesMap.get(hash).put(fresh.getPoint().toString().hashCode(), fresh);
								potentialStates.add(fresh);
							}
						}
					}
				}
			}
		}

		for(validState v : states){
			int hash = v.getBoxes().toString().hashCode();
			statesMap.put(hash, new HashMap<>());
			statesMap.get(hash).put(v.getPoint().toString().hashCode(), v);
		}

		Function<validState, Set<Actions>> getActionsFrom = state -> {
			Set<Actions> possibleActions = new HashSet<>();
			possibleActions.add(Actions.STILL);
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
			validState v;
			switch (action) {
				case EAST -> {
					v = new validState(state.getEast(), state.getBoxes());
					if (v.hasBox(v.getPoint())) {
						v.getBox(v.getPoint()).moveEast();
					}
				}
				case WEST -> {
					v = new validState(state.getWest(), state.getBoxes());
					if (v.hasBox(v.getPoint())) {
						v.getBox(v.getPoint()).moveWest();
					}
				}
				case NORTH -> {
					v = new validState(state.getNorth(), state.getBoxes());
					if (v.hasBox(v.getPoint())) {
						v.getBox(v.getPoint()).moveNorth();
					}
				}
				case SOUTH -> {
					v = new validState(state.getSouth(), state.getBoxes());
					if (v.hasBox(v.getPoint())) {
						v.getBox(v.getPoint()).moveSouth();
					}
				}
				case STILL -> {
					return state;
				}

				default -> throw new IllegalStateException("Unexpected value: " + action);
			}

			int hash = v.getBoxes().toString().hashCode();
			return states.stream().filter(b -> b.getPoint().equals(v.getPoint()) && b.boxes.toString().equals(v.boxes.toString())).findFirst().orElseThrow();

			//System.out.println(" || [RETURNS]: " + v.getPoint() + ", " + v.getBoxes());

			/*statesMap.get(hash).put(v.getPoint().toString().hashCode(), v);
			states.add(v);
			System.out.println(v);
			return v;*/

		};

		BiFunction<validState, Actions, Double> getReward = (state, action) -> {
			//System.out.println(state.getPoint() + " | " + state.getBoxes() + " " + action);
			double score = 0d;
			for(SokobanBox b : state.boxes){
				if(goalPoints.contains(b.getPoint())){
					score++;
				}
			}
			return score;
		};

		return FunctionBasedDeterministicWorldModel.newInstance(
				states,
				getConsequenceOfPlaying,
				getReward,
				getActionsFrom);
	}

	private static WorldModel<validState, Actions> generateWorldModel(ObstacleMap om, Point goal) {
		validState goalState = new validState(goal);
		Stack<validState> potentialStates = new Stack<>();
		potentialStates.addAll(stateList);

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
			Set<Actions> possibleActions = new HashSet<>();
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

			if (possibleActions.isEmpty()) {
				possibleActions.add(Actions.STILL);
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