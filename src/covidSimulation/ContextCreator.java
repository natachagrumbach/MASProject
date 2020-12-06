package covidSimulation;

import java.util.ArrayList;

import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

/**
 * Implementation of the ContextBuidler interface : builds a Context by adding projections, agents and so forth.
 * @author Natacha
 *
 */
public class ContextCreator implements ContextBuilder<Agent> {
	
	// Parameters of the simulation
	
	/**
	 * The grid width.
	 */
	int gridWidth;
	
	/**
	 * The grid height.
	 */
	int gridHeight;
	
	/**
	 * The number of susceptible agents at the beginning of the simulation.
	 */
	int numberOfSusceptibleAgents;
	
	/**
	 * The number of infected agents at the beginning of the simulation.
	 */
	int numberOfInfectedAgents;
	
	/**
	 * The mean probability of being infected by one infected agent.
	 */
	float probInf;
	
	/**
	 * The probability of recovering when infected.
	 */
	float probRec;
	
	/**
	 * The probability of being healthy (used at agents creation).
	 */
	float probHealthy;
	
	/**
	 * The probability of being elderly (used at agents creation).
	 */
	float probElderly;
	
	/**
	 *  The probability of being docile (apply government rules - used at agents creation).
	 */
	float probDocility;
	
	/**
	 * 1 if random movement, 2 if attractive places.
	 */
	int movementScenario;
	
	/**
	 * List of strategies of limitation.
	 */
	ArrayList<Integer> limitationStrategies;
	
	/**
	 * Possible values for movement scenario.
	 */
	static int RANDOM_MOVEMENT = 1;
	static String RANDOM_MVT_STR = "Random_scenario";
	static int ATTRACTIVE_PLACES = 2;
	static String ATTRACTIVE_PLACES_STR = "Attractive_places";
	
	/**
	 * Possible values for limitation strategies.
	 */
	static int STRATEGY_NONE = 0;
	static String NONE_STR = "None";
	static int STRATEGY_MASK = 1;
	static String MASK_STR = "Face_mask";
	static int STRATEGY_DISTANCING = 2;
	static String DISTANCING_STR = "Distanciation";
	static int STRATEGY_CURFEW = 3;
	static String CURFEW_STR = "Curfew";
	static int STRATEGY_LOCKDOWN = 4;
	static String LOCKDOWN_STR = "Lockdown";
	static int STRATEGY_ISOLATE_INFECTED = 5;
	static String ISOLATE_STR = "Isolation_of_infected_people";
	
	/**
	 * Maximum age.
	 */
	static int MAX_AGE = 120;
	

	
	/**
	 * Initialize the simulation.
	 */
	@Override
	public Context<Agent> build(Context<Agent> context) {
		context.setId("CovidSimulation");
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		gridWidth = params.getInteger("gridWidth");
		gridHeight = params.getInteger("gridHeight");
		numberOfSusceptibleAgents = params.getInteger("numberOfSusceptibleAgents");
		numberOfInfectedAgents = params.getInteger("numberOfInfectedAgents");
		probInf = params.getFloat("probInf");
		probRec = params.getFloat("probRec");
		probHealthy = params.getFloat("probHealthy");
		probElderly = params.getFloat("probElderly");
		probDocility = params.getFloat("probDocility");
		
		Agent.setProbInf(probInf);
		Agent.setProbRec(probRec);
		
		String movementScenarioStr = RunEnvironment.getInstance().getParameters().getString("scenarioMvt");
		if (movementScenarioStr.equals(RANDOM_MVT_STR))
			movementScenario = 1;
		else movementScenario = 2;
	
		String strategy1 = RunEnvironment.getInstance().getParameters().getString("limitationStrategy1");
		String strategy2 = RunEnvironment.getInstance().getParameters().getString("limitationStrategy2");
		String strategy3 = RunEnvironment.getInstance().getParameters().getString("limitationStrategy3");

		if (strategy1.equals(MASK_STR) || strategy2.equals(MASK_STR) || strategy3.equals(MASK_STR))
			Agent.setMask(true);
		else Agent.setMask(false);

		if (strategy1.equals(DISTANCING_STR) || strategy2.equals(DISTANCING_STR) || strategy3.equals(DISTANCING_STR))
			Agent.setDistancing(true);
		else Agent.setDistancing(false);
		
		if (strategy1.equals(CURFEW_STR) || strategy2.equals(CURFEW_STR) || strategy3.equals(CURFEW_STR))
			Agent.setCurfew(true);
		else Agent.setCurfew(false);
		
		if (strategy1.equals(LOCKDOWN_STR) || strategy2.equals(LOCKDOWN_STR) || strategy3.equals(LOCKDOWN_STR))
			Agent.setLockdown(true);
		else Agent.setLockdown(false);
		
		if (strategy1.equals(ISOLATE_STR) || strategy2.equals(ISOLATE_STR) || strategy3.equals(ISOLATE_STR))
			Agent.setInfectedIsolation(true);
		else Agent.setInfectedIsolation(false);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Agent> grid = gridFactory.createGrid("grid", context,  // "grid" is the name used in the xml file
				new GridBuilderParameters<Agent>(new WrapAroundBorders(),  // Manage limits of the grid
						new RandomGridAdder<Agent>(), false, gridWidth, gridHeight));
		
		int age;
		boolean atRisk;
				
		int goal = Agent.RANDOM_GOAL;
		
		for (int nb = 0; nb < numberOfSusceptibleAgents; nb++) {
			try {
							
				double ageRand = Math.random();
				
				if (ageRand < probElderly) {
					// Generate age between 65 and MAX_AGE
					age = (int) (Math.random() * (MAX_AGE - 65) + 65);
				}
				else {
					// Generate age between 0 and 64
					age = (int) (Math.random() * 64);
					
				}
				
				double atRiskRand = Math.random();
				
				atRisk = (atRiskRand > probHealthy);
				
				goal = Agent.RANDOM_GOAL;
				
				if (this.movementScenario == ATTRACTIVE_PLACES) {
					double rand = Math.random();
					
					if (rand < 0.5)
						goal = Agent.SCHOOL_GOAL;
					else goal = Agent.SHOPPING_GOAL;
				}
				
				//Wear the mask if mandatory and if docile
				boolean wearMask = false;
				if (Agent.isMask()) {
					double rand = Math.random();
					
					if (rand < probDocility)
						wearMask = true;
				}
				
					
				SusceptibleAgent sa = new SusceptibleAgent(goal, age, atRisk, wearMask, (nb == 0));
				context.add(sa);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (int nb = 0; nb < numberOfInfectedAgents; nb++) {
			try {
			
				double ageRand = Math.random();
				
				if (ageRand < probElderly) {
					// Generate age between 65 and MAX_AGE
					age = (int) (Math.random() * (MAX_AGE - 65) + 65);
				}
				else {
					// Generate age between 0 and 64
					age = (int) (Math.random() * 64);
					
				}
				
				atRisk = Math.random() > probHealthy;

				if (Agent.isInfectedIsolation())
					goal = Agent.HOSPITAL_GOAL;
				else goal = Agent.RANDOM_GOAL;
				
				//Wear the mask if mandatory and if docile
				boolean wearMask = false;
				if (Agent.isMask()) {
					double rand = Math.random();
					
					if (rand < probDocility)
						wearMask = true;
				}
				
				InfectedWithSymptomsAgent ia = new InfectedWithSymptomsAgent(goal, age, atRisk, wearMask);
				context.add(ia);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
		System.out.println("********************* END CONTEXT INITIALIZATION ***********************");

		return context;
	}
	
	/**
	 * Returns the movement scenario (1 if random, 2 if attractive places).
	 * @return the movement scenario
	 */
	public int getMovementScenario() {
		return this.movementScenario;
	}
}