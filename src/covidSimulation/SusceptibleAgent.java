package covidSimulation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.query.space.grid.MooreQuery;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

/**
 * This type of agent has never been infected by the Covid.
 * @author Natacha
 */
public class SusceptibleAgent extends Agent {
	
	/**
	 * The probability to be infected and has symptoms.
	 */
	double PROBA_INFECTED_WITH_SYMPTOMS = 0.5;
	
	/**
	 * The status of this agent is "SUSCEPTIBLE".
	 * @param goal the goal of the agent
	 * @param age the age of the agent
	 * @param atRisk a boolean equal to true if this agent has an increased risk due to medical conditions
	 * @param hasMask a boolean equal to true if the agent wears a mask
	 * @param initGlobalCounters a boolean that states that global counters must be initialized (used for simulation initialization)
	 */
	public SusceptibleAgent(int goal, int age, boolean atRisk, boolean hasMask, boolean initGlobalCounters) {
		super(goal, age, atRisk, hasMask);
		status = Agent.SUSCEPTIBLE_STATUS;
		this.nextStatus = status;
		
		// At first creation, global counters are reinitialized.
		if (initGlobalCounters)
			initGlobalCounters();
	}
	
	/**
	 * Returns the color used to display this agent.
	 */
	@Override
	public Color getColor() {
		return Color.CYAN;
	}
	
	/**
	 * A suceptible agent can be infected if one of his neighbor is infected, according to a certain probability based on probInf
	 * and adjusted according to the neighbourhood.
	 * Then, a susceptible agent can "become" an infected agent either with symptoms or without symptoms, according
	 * to a certain probability.
	 */
	@Override
	public void computeNextStatus() {
		
		int infectedNeighbours = 0;
		List<InfectedAgent> listInfected = new ArrayList<InfectedAgent>();
			
		List<InfectedAgent> listInfectedWithSymptomsWithMask = new ArrayList<InfectedAgent>();
		List<InfectedAgent> listInfectedWithSymptomsWithoutMask = new ArrayList<InfectedAgent>();
		List<InfectedAgent> listInfectedWithoutSymptomsWithMask = new ArrayList<InfectedAgent>();
		List<InfectedAgent> listInfectedWithoutSymptomsWithoutMask = new ArrayList<InfectedAgent>();
				
		listInfected = getListInfectedNeighbours();
		infectedNeighbours = listInfected.size();
		
		for (int i = 0; i < infectedNeighbours; i++) {
			InfectedAgent ia = listInfected.get(i);
			
			// Keep a trace of type of infected agents to compute R0 and know which agent has infected the susceptible agent.
			if (ia instanceof InfectedWithSymptomsAgent) {
				// If the infected agent wears a mask, then we assume he is not any more contagious...
				if ( ia.wearMask() )
					listInfectedWithSymptomsWithMask.add(ia);
				else listInfectedWithSymptomsWithoutMask.add(ia);
			}
			if (ia instanceof InfectedWithoutSymptomsAgent) {
				// If the infected agent wears a mask, then we assume he is not any more contagious...
				if ( ia.wearMask() )
					listInfectedWithoutSymptomsWithMask.add(ia);
				else listInfectedWithoutSymptomsWithoutMask.add(ia);
			}
		}
		
		// If there are infected neighbours
		if (infectedNeighbours > 0) {
			// Add all neighbours probabilities
			double probaNeighbourhood = 0;
			for (int i = 0; i < infectedNeighbours; i++) {
				probaNeighbourhood += listInfected.get(i).computeContaminationProb();
			}
			
			// Average of all neighbours probabilities
			probaNeighbourhood = probaNeighbourhood / infectedNeighbours;
			
			// Multiply by a factor depending on the number of neighbours
			probaNeighbourhood = probaNeighbourhood * (1 + 0.05*(infectedNeighbours - 1));
			
			// If this agent wears a mask, decrease the probability
			if (wearMask())
				probaNeighbourhood = probaNeighbourhood * FACTOR_WITH_MASK;
			
			// Range [0;1]
			if (probaNeighbourhood < 0)
				probaNeighbourhood = 0;
			else if (probaNeighbourhood > 1)
				probaNeighbourhood = 1;
		
			// Infect the susceptible agent based on probaNeighbourhood probability
			double rand = Math.random();
			if (rand < probaNeighbourhood) {
				
				// The next status of the agent is infected with or without symptoms (by default, proba = 0.5)
				double probaWithSymptoms = PROBA_INFECTED_WITH_SYMPTOMS;
				
				// the elderly and the people with specific medical conditions are more susceptible
				// to be infected with symptoms.
				if (this.age > 65) {
					if (this.age < 75)
						probaWithSymptoms = probaWithSymptoms * 1.2;
					else probaWithSymptoms = probaWithSymptoms * 1.4;
				}
				if (this.atIncreasedRisk) {
					probaWithSymptoms = probaWithSymptoms * 1.2;
				}
				
				// Range [0;1]
				if (probaWithSymptoms > 1)
					probaWithSymptoms = 1;
				
				rand = Math.random();
				if (rand < probaWithSymptoms) {
					// Without symptoms
					this.nextStatus = Agent.INFECTED_WITH_SYMPTOMS_STATUS;
				}
				else {
					// With symptoms
					this.nextStatus = Agent.INFECTED_WITHOUT_SYMPTOMS_STATUS;
				}
				
				// One of the infected neighbor is responsible for the infection of the agent.
				// By default, takes the first element of the neighbor list of infected persons with symptoms.
				// If empty, takes the first element of the neighbor list of infected persons without symptoms.
				if (listInfectedWithSymptomsWithoutMask.size() > 0)
					listInfectedWithSymptomsWithoutMask.get(0).incrementNumberOfPeopleContaminated();
				else if (listInfectedWithoutSymptomsWithoutMask.size() > 0)
					listInfectedWithoutSymptomsWithoutMask.get(0).incrementNumberOfPeopleContaminated();
				else if (listInfectedWithSymptomsWithMask.size() > 0)
					listInfectedWithSymptomsWithMask.get(0).incrementNumberOfPeopleContaminated();
				else listInfectedWithoutSymptomsWithMask.get(0).incrementNumberOfPeopleContaminated();			
			}
		}
	}
	
	/**
	 * Returns the list of infected neighbours (the grid is "static", we don't count the neighbours "outside" the grid).
	 * @return the list of infected neighbours of this agent.
	 */
	List<InfectedAgent> getListInfectedNeighbours() {
		List<InfectedAgent> listInfected = new ArrayList<InfectedAgent>();
		
		Context context = ContextUtils.getContext(this);
		Grid<Agent> grid = (Grid<Agent>) context.getProjection("grid");
		GridPoint p = grid.getLocation(this);
		
		int x = p.getX();
		int y = p.getY();
		
		if (x+1 < grid.getDimensions().getWidth()) { 
			if  (grid.getObjectAt(x+1, y) != null && grid.getObjectAt(x+1, y) instanceof InfectedAgent)
				listInfected.add((InfectedAgent) (grid.getObjectAt(x+1, y)));
			if (y+1 < grid.getDimensions().getHeight())
				if  (grid.getObjectAt(x+1, y+1) != null && grid.getObjectAt(x+1, y+1) instanceof InfectedAgent)
					listInfected.add((InfectedAgent) (grid.getObjectAt(x+1, y+1)));
			if (y-1 >= 0)
				if  (grid.getObjectAt(x+1, y-1) != null && grid.getObjectAt(x+1, y-1) instanceof InfectedAgent)
					listInfected.add((InfectedAgent) (grid.getObjectAt(x+1, y-1)));
		}
		if (x-1 >= 0) {
			if  (grid.getObjectAt(x-1, y) != null && grid.getObjectAt(x-1, y) instanceof InfectedAgent)
				listInfected.add((InfectedAgent) (grid.getObjectAt(x-1, y)));
			if (y+1 < grid.getDimensions().getHeight())
				if  (grid.getObjectAt(x-1, y+1) != null && grid.getObjectAt(x-1, y+1) instanceof InfectedAgent)
					listInfected.add((InfectedAgent) (grid.getObjectAt(x-1, y+1)));
			if (y-1 >= 0)
				if  (grid.getObjectAt(x-1, y-1) != null && grid.getObjectAt(x-1, y-1) instanceof InfectedAgent)
					listInfected.add((InfectedAgent) (grid.getObjectAt(x-1, y-1)));
		}
	
		if (y+1 < grid.getDimensions().getHeight())
			if  (grid.getObjectAt(x, y+1) != null && grid.getObjectAt(x, y+1) instanceof InfectedAgent)
				listInfected.add((InfectedAgent) (grid.getObjectAt(x, y+1)));
		
		if (y-1 >= 0)
			if  (grid.getObjectAt(x, y-1) != null && grid.getObjectAt(x, y-1) instanceof InfectedAgent)
				listInfected.add((InfectedAgent) (grid.getObjectAt(x, y-1)));
		
		return listInfected;
	}
	
	/**
	 * Computes the next position and apply the new status and the new position.
	 * The new position is computed according to current grid. This avoid several agents being on same cell.
	 */
	@Override	
	public void computeNextPositionAndApply() {

		Context context = ContextUtils.getContext(this);
		Grid<Agent> grid = (Grid<Agent>) context.getProjection("grid");
		
		// Computes next position randomly and stores it in newPosition.
		computeNextPosition();
		
		// Apply the new status if not susceptible
		if (this.nextStatus != Agent.SUSCEPTIBLE_STATUS) {
			context.remove(this);
			InfectedAgent a = null;
			
			if (Agent.isInfectedIsolation())
				goal = Agent.HOSPITAL_GOAL;
			
			if (this.nextStatus == Agent.INFECTED_WITH_SYMPTOMS_STATUS) {
				a = new InfectedWithSymptomsAgent(goal, age, atIncreasedRisk, hasMask);
				context.add(a);
			}
			else if (this.nextStatus == Agent.INFECTED_WITHOUT_SYMPTOMS_STATUS) {
				a = new InfectedWithoutSymptomsAgent(goal, age, atIncreasedRisk, hasMask);
				context.add(a);
			}
			// Move the agent to its new position
			if (this.nextPosition != null)
				grid.moveTo(a, this.nextPosition.getX(), this.nextPosition.getY());
		}
		else { 
			// The status has not changed
			// Move the agent to its new position
			if (this.nextPosition != null)
				grid.moveTo(this, this.nextPosition.getX(), this.nextPosition.getY());
		}

	}

}
