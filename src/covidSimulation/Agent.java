package covidSimulation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

/**
 * Super-class of all agents.
 * Each agent is defined by his current status, his goal (school, shopping, random...), his next status,
 * his next position, his age, if he has increased risk for medical conditions, if he wears a mask.
 * Some static variables are also defined :
 * - the probability of being infected when an infected agent is in the neighborhood
 * - the probability of recovering when infected
 * - the strategies to apply for this simulation
 * - the total number of deceased agents
 * @author Natacha
 *
 */
public abstract class Agent {
	
	/**
	 * The current status of this agent : susceptible, infected, infected without symptoms, recovered or deceased.
	 */
	protected int status;
	
	/**
	 * The goal of this agent (school, hospital, shopping, no goal).
	 */
	int goal;
	
	/**
	 * The next status given the neighborhood and the status of the agent.
	 */
	protected int nextStatus;
	
	/**
	 * The next position depending on the goal.
	 */
	protected GridPoint nextPosition;
	
	/**
	 * Age of the agent.
	 */
	int age;
	
	/**
	 * Boolean indicating if the person is at-risk, because of certain medical conditions.
	 */
	boolean atIncreasedRisk;
	
	/**
	 * Boolean indicating if the agent wears a mask.
	 */
	boolean hasMask;
	
	/**
	 * Probability of being infected when an infected agent is in the neighborhood.
	 */
	protected static float probInf;

	/**
	 * Probability of recovering when infected. The probability of decease equals 1-probRec.
	 */
	protected static float probRec;
	
	/**
	 * The constants representing the status.
	 */
	public static final int SUSCEPTIBLE_STATUS = 0;
	public static final int INFECTED_WITH_SYMPTOMS_STATUS = 1;
	public static final int INFECTED_WITHOUT_SYMPTOMS_STATUS = 2;
	public static final int RECOVERED_STATUS = 3;
	public static final int DECEASED_STATUS = 4;
	
	/**
	 * The constants representing the goals.
	 */
	public static final int RANDOM_GOAL = 0;
	public static final int SCHOOL_GOAL = 1;
	public static final int SHOPPING_GOAL = 2;
	public static final int HOSPITAL_GOAL = 3;
	
	/**
	 * The total number of deceased agents.
	 */
	private static int countTotalDeaths = 0;
	
	/**
	 * The different strategies of limitation.
	 */
	private static boolean distancing = false;
	private static boolean infectedIsolation = false;
	private static boolean lockdown = false;
	private static boolean curfew = false;
	private static boolean mask = false;

	/**
	 * Factor to decrease the risk of being infected when wearing a mask.
	 */
	static double FACTOR_WITH_MASK = 0.3;

	/**
	 * Constructor
	 * @param goal the goal of the agent
	 * @param age the age of the agent
	 * @param atRisk a boolean equal to true if this agent has an increased risk due to medical conditions
	 * @param hasMask a boolean equal to true if the agent wears a mask
	 */
	public Agent(int goal, int age, boolean atRisk, boolean hasMask) {
		this.goal = goal;
		this.age = age;
		this.atIncreasedRisk = atRisk;
		this.hasMask = hasMask;
	}
	
	/**
	 * Sets the average probability of contamination.
	 * @param probInf the probability of contamination
	 */
	public static void setProbInf(float probInf) {
		Agent.probInf = probInf;
	}

	/**
	 * Sets the average probability of recovering.
	 * @param probRec the probability of recovering
	 */
	public static void setProbRec(float probRec) {
		Agent.probRec = probRec;
	}

	/**
	 * Computes the next status according to the neighborhood and the current status of the agent.
	 * This method must be called for each agent before modifying really the status and the position.
	 * The priority is thus higher than computeNextPositionAndApply.
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = 2)
	public abstract void computeNextStatus();

	/**
	 * Computes the next position depending on the goal. If the goal is random,
	 * then choose a free position in the neighborhood randomly.
	 * Otherwise, choose a free position to move towards the goal.
	 * The next status must have been previously computed.
	 * The priority is thus lower than computeNextStatus.
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = 1)
	public abstract void computeNextPositionAndApply();
	
	/**
	 * Returns the display color of the agent
	 * @return the color to use to display this agent
	 */
	public abstract Color getColor();
	
	/**
	 * Computes next position (distance of moving is 1 and only on a free cell).
	 * Stores it it nextPosition.
	 */
	void computeNextPosition() {
		
		// If there is a lockdown or if there is a curfew and the current time is in the curfew, the agent does not move.
		// Otherwise, compute the next position according to his goal.
		if (! Agent.isLockdown()) {
			
			if (! Agent.isCurfew() || ! mustNotMove()) {
		
				ArrayList<GridPoint> possiblePointsList;
				
				switch (goal) {
		        	case RANDOM_GOAL:  
		        		possiblePointsList = moveRandomly();
		        		break;
		        	case SCHOOL_GOAL:  
		        		possiblePointsList = moveTowardsSchool();
		        		break;
		        	case HOSPITAL_GOAL:  
		        		possiblePointsList = moveTowardsHospital();
		        		break;
		        	case SHOPPING_GOAL:  
		        		possiblePointsList = moveTowardsShoppingCenter();
		        		break;
		        	default:
		        		possiblePointsList = moveRandomly();
		        		break;
				}
				
				if (possiblePointsList.size() > 0) {
					if (! Agent.isDistancing()) {
						// Shuffle the points to avoid taking always the same direction...
						Collections.shuffle(possiblePointsList);
						
						// If the list of possible points is empty, then we don't update the next position. The agent will not move.
						if (possiblePointsList.size() > 0)
							nextPosition = possiblePointsList.get(0); 
					}
					else {
						// If distancing is enabled, choose the point with the lower number of direct neighbors
						int minNeighbours = 10;
						List<GridPoint> list = new ArrayList<GridPoint>();
						for (int i = 0; i < possiblePointsList.size(); i++) {
							GridPoint p = possiblePointsList.get(i);
							int nbNeighbours = getNbNeighbours(p);
							if (nbNeighbours < minNeighbours) {
								minNeighbours = nbNeighbours;
								list = new ArrayList<GridPoint>();
								list.add(p);
							}
							else if (nbNeighbours == minNeighbours) {
								list.add(p);
							}
						}
						
						Collections.shuffle(list);
						
						//if (list.size() > 0)
						nextPosition = list.get(0); 
						
					}
				}
				else { // No possible point to move, the agent stays at the same position.
					Context context = ContextUtils.getContext(this);
					Grid<Agent> grid = (Grid<Agent>) context.getProjection("grid");
					GridPoint gpt = grid.getLocation(this);
					nextPosition = gpt;
				}
			}
			// There is a curfew and the current time is in the curfew, the agent don't move.
			else {
				Context context = ContextUtils.getContext(this);
				Grid<Agent> grid = (Grid<Agent>) context.getProjection("grid");
				GridPoint gpt = grid.getLocation(this);
				nextPosition = gpt;
			}
		}
		else { // Don't move because of lockdown
			Context context = ContextUtils.getContext(this);
			Grid<Agent> grid = (Grid<Agent>) context.getProjection("grid");
			GridPoint gpt = grid.getLocation(this);
			nextPosition = gpt;
		}
	}
	
	int getNbNeighbours(GridPoint p) {
		int nbNeighbours = 0;
		
		Context context = ContextUtils.getContext(this);
		Grid<Agent> grid = (Grid<Agent>) context.getProjection("grid");
		
		int x = p.getX();
		int y = p.getY();
		
		if (x+1 < grid.getDimensions().getWidth()) { 
			if  (grid.getObjectAt(x+1, y) != null)
				nbNeighbours++;
			if (y+1 < grid.getDimensions().getHeight())
				if  (grid.getObjectAt(x+1, y+1) != null) // Checks that cell is free
					nbNeighbours++;
			if (y-1 >= 0)
				if  (grid.getObjectAt(x+1, y-1) != null) // Checks that cell is free
					nbNeighbours++;
		}
		if (x-1 >= 0) {
			if  (grid.getObjectAt(x-1, y) != null) // Checks that cell is free
				nbNeighbours++;
			if (y+1 < grid.getDimensions().getHeight())
				if  (grid.getObjectAt(x-1, y+1) != null) // Checks that cell is free
					nbNeighbours++;
			if (y-1 >= 0)
				if  (grid.getObjectAt(x-1, y-1) != null) // Checks that cell is free
					nbNeighbours++;
		}
	
		if (y+1 < grid.getDimensions().getHeight())
			if  (grid.getObjectAt(x, y+1) != null) // Checks that cell is free
				nbNeighbours++;
		
		if (y-1 >= 0)
			if  (grid.getObjectAt(x, y-1) != null) // Checks that cell is free
				nbNeighbours++;
		
		return nbNeighbours;
		
	}
	
	/**
	 * When there is a curfew, agents must not move between 10PM and 8AM.
	 * @return true if the time tick corresponds to the period of curfew
	 */
	boolean mustNotMove() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double currentTime= schedule.getTickCount();

		return ((currentTime % 24 > 22) || (currentTime % 24 < 8));
	}
	
	/**
	 * Returns the possible point to move on (max 8) by respecting the grid dimensions and the cell must be free.
	 * @return an array of GridPoint objects corresponding to the possible points to move on.
	 */
	ArrayList<GridPoint> moveRandomly() {
		Context context = ContextUtils.getContext(this);
		Grid<Agent> grid = (Grid<Agent>) context.getProjection("grid");
		GridPoint gpt = grid.getLocation(this);
		int x = gpt.getX();
		int y = gpt.getY();
		
		ArrayList<GridPoint> possiblePointsList = new ArrayList<GridPoint>();
		
		if (x+1 < grid.getDimensions().getWidth()) { 
				if  (grid.getObjectAt(x+1, y) == null) // Checks that cell is free
					possiblePointsList.add(new GridPoint(x+1, y));
				if (y+1 < grid.getDimensions().getHeight())
					if  (grid.getObjectAt(x+1, y+1) == null) // Checks that cell is free
						possiblePointsList.add(new GridPoint(x+1, y+1));
				if (y-1 >= 0)
					if  (grid.getObjectAt(x+1, y-1) == null) // Checks that cell is free
						possiblePointsList.add(new GridPoint(x+1, y-1));
		}
		
		if (x-1 >= 0) {
			if  (grid.getObjectAt(x-1, y) == null) // Checks that cell is free
				possiblePointsList.add(new GridPoint(x-1, y));
			if (y+1 < grid.getDimensions().getHeight())
				if  (grid.getObjectAt(x-1, y+1) == null) // Checks that cell is free
					possiblePointsList.add(new GridPoint(x-1, y+1));
			if (y-1 >= 0)
				if  (grid.getObjectAt(x-1, y-1) == null) // Checks that cell is free
					possiblePointsList.add(new GridPoint(x-1, y-1));
		}
		
		if (y+1 < grid.getDimensions().getHeight())
			if  (grid.getObjectAt(x, y+1) == null) // Checks that cell is free
				possiblePointsList.add(new GridPoint(x, y+1));
		
		if (y-1 >= 0)
			if  (grid.getObjectAt(x, y-1) == null) // Checks that cell is free
				possiblePointsList.add(new GridPoint(x, y-1));

		return possiblePointsList;
	}
	
	/**
	 * Computes the possible points to move of a distance of 1 towards the specified point.
	 * @param pt the destination point
	 * @return the list of possible points to move
	 */
	public ArrayList<GridPoint>  moveTowards (GridPoint pt) {
		Context context = ContextUtils.getContext(this);
		Grid<Agent> grid = (Grid<Agent>) context.getProjection("grid");
		// only move if we are not already in this grid location
		GridPoint currentPos = grid.getLocation(this);
		ArrayList<GridPoint> possiblePointsList = new ArrayList<GridPoint>();
		int x = currentPos.getX();
		int y = currentPos.getY();
		
		
		if (!pt.equals (currentPos)) {
			if (pt.getX() < x) {
				if  (grid.getObjectAt(x-1,y) == null) // Checks that cell is free
					possiblePointsList.add(new GridPoint(x-1,y));
				
				if (pt.getY() > y) {
					if  (grid.getObjectAt(x-1,y+1) == null) // Checks that cell is free
						possiblePointsList.add(new GridPoint(x-1,y+1));
				}
				else if (pt.getY() < y) {
					if  (grid.getObjectAt(x-1,y-1) == null) // Checks that cell is free
						possiblePointsList.add(new GridPoint(x-1,y-1));
				}
			}
			
			if (pt.getX() > x) {
				if  (grid.getObjectAt(x+1,y) == null) // Checks that cell is free
					possiblePointsList.add(new GridPoint(x+1,y));
				
				if (pt.getY() > y) {
					if  (grid.getObjectAt(x+1,y+1) == null) // Checks that cell is free
						possiblePointsList.add(new GridPoint(x+1,y+1));
				}
				else if (pt.getY() < y) {
					if  (grid.getObjectAt(x+1,y-1) == null) // Checks that cell is free
						possiblePointsList.add(new GridPoint(x+1,y-1));
				}
			}
			
			
			if (pt.getY() < y) {
				if  (grid.getObjectAt(x,y-1) == null) // Checks that cell is free
					possiblePointsList.add(new GridPoint(x,y-1));
			}
			if (pt.getY() > y) {
				if  (grid.getObjectAt(x,y+1) == null) // Checks that cell is free
					possiblePointsList.add(new GridPoint(x,y+1));
			}
			if (pt.getX() == x) {
				if (pt.getY() > y) {
					if  (grid.getObjectAt(x,y+1) == null) // Checks that cell is free
						possiblePointsList.add(new GridPoint(x,y+1));
				}
				else { // y > pt.getY() because !=
					if  (grid.getObjectAt(x,y-1) == null) // Checks that cell is free
						possiblePointsList.add(new GridPoint(x,y-1));
				}
			}
			if (pt.getY() == y) {
				if (pt.getX() > x) {
					if  (grid.getObjectAt(x+1,y) == null) // Checks that cell is free
						possiblePointsList.add(new GridPoint(x+1,y));
				}
				else { // x > pt.getx() because !=
					if  (grid.getObjectAt(x-1,y) == null) // Checks that cell is free
						possiblePointsList.add(new GridPoint(x-1,y));
				}
			} 

		}
		
		return possiblePointsList;
		
	}
	
	/**
	 * We assume that school is located in position (0,0) (top left corner).
	 * @return the list of possible points to move towards school
	 */
	public ArrayList<GridPoint> moveTowardsSchool () {
		GridPoint schoolPos = new GridPoint(0,0);
		return moveTowards(schoolPos);
	}
	
	/**
	 * We assume that shopping center is located in position (0,height of grid - 1) (top right corner)
	 * @return the list of possible points to move towards shopping center
	 */
	public ArrayList<GridPoint> moveTowardsShoppingCenter () {
		Context context = ContextUtils.getContext(this);
		Grid<Agent> grid = (Grid<Agent>) context.getProjection("grid");
		GridPoint shoppingPos = new GridPoint(grid.getDimensions().getWidth()-1,grid.getDimensions().getHeight()-1);
		return moveTowards(shoppingPos);
	}
	
	
	/**
	 * We assume that school is located in position (width - 1,0) (bottom left corner).
	 * @return the list of possible points to move towards hospital
	 */
	public ArrayList<GridPoint> moveTowardsHospital () {
		Context context = ContextUtils.getContext(this);
		Grid<Agent> grid = (Grid<Agent>) context.getProjection("grid");
		GridPoint hospitalPos = new GridPoint(grid.getDimensions().getWidth()/2,grid.getDimensions().getHeight()/2);
		return moveTowards(hospitalPos);
	}	
	
	/**
	 * Returns a boolean stating if the agent wears a mask.
	 * @return true if the agent wears a mask
	 */
	public boolean wearMask( ) {
		return this.hasMask;
	}

	/**
	 * Initializes the deaths counter to 0. Called at each initialization of the simulation.
	 */
	public void initGlobalCounters() {
		countTotalDeaths = 0;
	}
	
	/**
	 * Returns the total number of deaths for the current simulation.
	 * @return the total number of deaths
	 */
	public int getCountTotalDeaths() {
		return countTotalDeaths;
	}
	
	/**
	 * Increments the total number of deaths.
	 */
	public void incrementCountTotalDeaths() {
		countTotalDeaths++;
	}
	
	/**
	 * Returns true if the distancing strategy is activated.
	 * @return true if the distancing strategy is activated.
	 */
	public static boolean isDistancing() {
		return distancing;
	}

	/**
	 * Sets the distancing strategy.
	 * @param distancing a boolean stating if the distancing strategy is activated.
	 */
	public static void setDistancing(boolean distancing) {
		Agent.distancing = distancing;
	}

	/**
	 * Returns true if the isolation of infected agents strategy is activated.
	 * @return true if the isolation of infected agents strategy is activated.
	 */
	public static boolean isInfectedIsolation() {
		return infectedIsolation;
	}

	/**
	 * Sets the strategy of isolation of infected agents.
	 * @param infectedIsolation a boolean stating if the strategy of isolation of infected agents is activated.
	 */
	public static void setInfectedIsolation(boolean infectedIsolation) {
		Agent.infectedIsolation = infectedIsolation;
	}

	/**
	 * Returns true if the lockdown strategy is activated.
	 * @return true if the lockdown strategy is activated.
	 */
	public static boolean isLockdown() {
		return lockdown;
	}

	/**
	 * Sets the lockdown strategy attribute.
	 * @param lockdown true if the lockdown strategy is activated.
	 */
	public static void setLockdown(boolean lockdown) {
		Agent.lockdown = lockdown;
	}

	/**
	 * Returns true if the curfew strategy is activated.
	 * @return true if the curfew strategy is activated.
	 */
	public static boolean isCurfew() {
		return curfew;
	}

	/**
	 * Sets the curfew strategy attribute.
	 * @param curfew true if the curfew strategy is activated.
	 */
	public static void setCurfew(boolean curfew) {
		Agent.curfew = curfew;
	}

	/**
	 * Returns true if the mask strategy is activated.
	 * @return true if the mask strategy is activated.
	 */
	public static boolean isMask() {
		return mask;
	}

	/**
	 * Sets the mask strategy attribute.
	 * @param mask true if the mask strategy is activated.
	 */
	public static void setMask(boolean mask) {
		Agent.mask = mask;
	}

}
