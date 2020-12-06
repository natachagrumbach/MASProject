package covidSimulation;

/**
 * Abstract class that represents all infected agents.
 * @author Natacha
 *
 */
public abstract class InfectedAgent extends Agent {

	/**
	 * Count the number of agents that this agent has infected. Used to compute R0.
	 */
	private int numberOfPeopleContaminated;
	
	/**
	 * Constructor. The number of contaminated persons is initialized to 0.
	 * @param goal the goal of the agent
	 * @param age the age of the agent
	 * @param atRisk a boolean equal to true if this agent has an increased risk due to medical conditions
	 * @param hasMask a boolean equal to true if the agent wears a mask
	 */
	public InfectedAgent(int goal, int age, boolean atRisk, boolean hasMask) {
		super(goal, age, atRisk, hasMask);
		
		numberOfPeopleContaminated = 0;
	}
	
	/**
	 * Increments the number of people contaminated.
	 */
	public void incrementNumberOfPeopleContaminated() {
		this.numberOfPeopleContaminated++;
	}
	
	/**
	 * Returns the number of people contaminated by this agent.
	 * @return the number of people contaminated by this agent
	 */
	public int getNumberOfPeopleContaminated() {
		return this.numberOfPeopleContaminated;
	}
	
	/**
	 * Abstract method that will be defined in children classes.
	 * Returns the contamination probability adjusted with the agent parameters.
	 * @return the contamination probability
	 */
	public abstract double computeContaminationProb();
	
	
}
