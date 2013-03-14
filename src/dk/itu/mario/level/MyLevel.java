package dk.itu.mario.level;

import java.util.Random;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.engine.sprites.Enemy;
import dk.itu.mario.level.LevelNode.Type;

public class MyLevel extends Level {
	public static final int CHUNK_SIZE = 10;
	// Store information about the level
	public int ENEMIES = 0; // the number of enemies the level contains
	public int BLOCKS_EMPTY = 0; // the number of empty blocks
	public int BLOCKS_COINS = 0; // the number of coin blocks
	public int BLOCKS_POWER = 0; // the number of power blocks
	public int COINS = 0; // These are the coins in boxes that Mario collect

	// Record these enemies
	public static int bullets_fired = 0;
	public static int red_turtles = 0;
	public static int green_turtles = 0;
	public static int plants = 0;
	public static int goombas = 0;
	public static int spikyThing = 0;
	public static int numEnemies = 0;

	public static boolean resetRecorder = true;

	private static Random levelSeedRandom = new Random();
	public static long lastSeed;

	Random random;

	private Level testLevel;

	private int difficulty;
	private int type;
	private int gaps;
	private long seed;
	
	GamePlay playerMets;
	
	/**
	if(i==1){
				toReturn = Type.HILL;
			}
			else if(i==2){
				toReturn = Type.JUMP;
			}
			else if(i==3){
				toReturn = Type.TUBES;
			}
			else if(i==4){
				toReturn = Type.CANNONS;
			}
			else if(i==5){
				toReturn = Type.ENEMIES;*/
	
	//The different values used to randomly chose a type (less than the value means we'll be using that)
	private float hillProbability = 0.2f;
	private float jumpProbability = 0.4f;
	private float tubesProbability = 0.6f;
	private float cannonsProbability = 0.8f;
	private float enemiesProbability = 1.0f;
	
	//Booleans for whether we prefer to increase a given type (or not)
	private boolean hillIncrease;
	private boolean jumpIncrease;
	private boolean tubesIncrease;
	private boolean cannonsIncrease;
	private boolean enemiesIncrease;
	
	

	public MyLevel(int width, int height) {
		super(width, height);
	}

	public MyLevel(int width, int height, long seed, int difficulty, int type,
			GamePlay playerMetrics, boolean resetRecorder) {
		
		
		this(width, height);
		this.seed = seed;
		playerMets = playerMetrics;
		System.out.println("Number of games: "+playerMets.numRecorded);
		System.out.println("Aimless Jumps: "+playerMets.aimlessJumps);
		System.out.println("Enemies percentage killed : "+playerMets.totalPercentEnemiesKilled);
		System.out.println("Cannons percentage killed : "+playerMets.percentCannonBallsKilled);
		System.out.println("Cannons killed me : "+playerMets.timesOfDeathByCannonBall);
		System.out.println("Tubes percentage killed : "+playerMets.percentFlowersKilled);
		System.out.println("Tubes killed me : "+playerMets.timesOfDeathByJumpFlower);
		System.out.println("Death falls: "+playerMets.timesOfDeathByFallingIntoGap);
		
		
		//percentCannonBallsKilled
		
		MyLevel.resetRecorder = resetRecorder;
		
		if(resetRecorder){
			hillProbability = 0.2f;
			jumpProbability = 0.4f;
			tubesProbability = 0.6f;
			cannonsProbability = 0.8f;
			enemiesProbability = 1.0f;
			
			
		}
			
			
		
		else{
			//Calculate values
			double avgJumps = playerMets.aimlessJumps/playerMets.numRecorded;
			
			//Hill/Jump probability setting
			if(avgJumps>30){
				hillProbability+=(avgJumps-28)*0.01f;
				jumpProbability+=(avgJumps-28)*0.01f;
				
				hillIncrease=true;
				jumpIncrease=true;
				
			}
			else{
				hillProbability-=(30-avgJumps)*0.01f;
				jumpProbability+=(avgJumps-28)*0.01f;
				
				hillIncrease=false;
				jumpIncrease=false;
				
				
				if(hillProbability<0){
					hillProbability = 0;
				}
			}
			
			//int totalEnemiesKilled = playerMets.RedTurtlesKilled + playerMets.GoombasKilled + playerMets.ArmoredTurtlesKilled + playerMets.GreenTurtlesKilled;
			//double avgKilling = totalEnemiesKilled/playerMets.numRecorded;
			
			//Enemy killer probability setting
			if(playerMets.totalPercentEnemiesKilled>0.25){
				//You like to kill things, don't you? Decrease probability of all others
				cannonsProbability-= (playerMets.totalPercentEnemiesKilled-0.25)/5.0f;
				hillProbability-= (playerMets.totalPercentEnemiesKilled-0.25)/5.0f;
				tubesProbability-= (playerMets.totalPercentEnemiesKilled-0.25)/5.0f;
				jumpProbability-= (playerMets.totalPercentEnemiesKilled-0.25)/5.0f;
				
				enemiesIncrease=true;
				
			}
			else{
				//You really don't like to kill things? Make everything else more likely
				cannonsProbability-= (playerMets.totalPercentEnemiesKilled-0.25)/5.0f;
				hillProbability-= (playerMets.totalPercentEnemiesKilled-0.25)/5.0f;
				tubesProbability-= (playerMets.totalPercentEnemiesKilled-0.25)/5.0f;
				jumpProbability-= (playerMets.totalPercentEnemiesKilled-0.25)/5.0f;
				
				enemiesIncrease=false;
			}
			
			//Cannon determination
			if(playerMets.percentCannonBallsKilled>0.1){
				//Chances are, you like killing things, so we'll make everythng but enemies less likely
				hillProbability-= (playerMets.percentCannonBallsKilled-0.1)/5.0f;
				tubesProbability-= (playerMets.percentCannonBallsKilled-0.1)/5.0f;
				jumpProbability-= (playerMets.percentCannonBallsKilled-0.1)/5.0f;
				
				cannonsIncrease=true;
			}
			else{
				//Chances are, you're terrified of bullet death, make everything more likely
				hillProbability-= (playerMets.percentCannonBallsKilled-0.1)/5.0f;
				tubesProbability-= (playerMets.percentCannonBallsKilled-0.1)/5.0f;
				jumpProbability-= (playerMets.percentCannonBallsKilled-0.1)/5.0f;
				
				cannonsIncrease=false;
			}
			
			
			//Tubes determination
			if(playerMets.percentFlowersKilled>0.0){
				//Chances are, you like killing things, so we'll just make hills and jumps less likely
				hillProbability-= (playerMets.percentFlowersKilled-0.0)/5.0f;
				jumpProbability-= (playerMets.percentFlowersKilled-0.0)/5.0f;
				
				tubesIncrease=true;
			}
			else{
				//Chances are, you don't like 
				hillProbability-= (playerMets.percentFlowersKilled-0.0)/5.0f;
				jumpProbability-= (playerMets.percentFlowersKilled-0.0)/5.0f;
				
				tubesIncrease=false;
			}
			
			
			
			
		}
		
		random = new Random(seed);
		LevelResult result = simulatedAnnealing(1000, 400, 500);
		getEnemyCount(result.getSprites());
		this.setMap(result.getMap()); // We might need to modify how we do this?
		this.setSpriteMap(result.getSprites());
		// System.out.println(compareLevels(result.getMap(), testLevel.getMap(),
		// result.getSprites(), testLevel.getSpriteTemplates()));
		// creat(seed, difficulty, type);
	}

	/**
	 * Get the stats for the enemys present in our generated level
	 * 
	 * @param spriteTemplate
	 */
	public void getEnemyCount(SpriteTemplate[][] spriteTemplate) {
		for (int i = 0; i < spriteTemplate.length; i++) {
			for (int j = 0; j < spriteTemplate[i].length; j++) {
				if (spriteTemplate[i][j] != null) {
					switch (spriteTemplate[i][j].type) {
					case SpriteTemplate.RED_TURTLE:
						MyLevel.red_turtles++;
						numEnemies++;
						break;
					case SpriteTemplate.GREEN_TURTLE:
						MyLevel.green_turtles++;
						numEnemies++;
						break;
					case SpriteTemplate.GOOMPA:
						numEnemies++;
						MyLevel.goombas++;
						break;
					case SpriteTemplate.ARMORED_TURTLE:
						numEnemies++;
						spikyThing++;
						break;
					case SpriteTemplate.JUMP_FLOWER:
						plants++;
						numEnemies++;
						break;
					case SpriteTemplate.CHOMP_FLOWER:
						plants++;
						numEnemies++;
						break;
					default:
						break;

					}
				}
			}
		}
	}

	/**
	 * Testing that the maps and sprites were copying correctly.
	 * 
	 * @param map1
	 * @param map2
	 * @param sprite1
	 * @param sprite2
	 * @return
	 */
	public boolean compareLevels(byte[][] map1, byte[][] map2,
			SpriteTemplate[][] sprite1, SpriteTemplate[][] sprite2) {
		for (int i = 0; i < map1.length; i++) {
			for (int j = 0; j < map1[0].length; j++) {
				if (map1[i][j] != map2[i][j] || sprite1[i][j] != sprite2[i][j])
					return false;
			}
		}
		return true;
	}

	/**
	 * Perform simulated annealing
	 * 
	 * @param maxIterations
	 *            The max number of iterations for simulated annealing to
	 *            perform
	 * @param acceptingScore
	 *            A score that we are willing to accept
	 * @param startTemp
	 *            The temperature to start at
	 * @return the best map and sprite template that has been found
	 */
	public LevelResult simulatedAnnealing(int maxIterations,
			double acceptingScore, double startTemp) {
		double temp = startTemp;
		Level currentLevel = new RandomLevel(width, height, seed, 1, type); // Start
																			// state
																			// is
																			// random
																			// level.
																			// We
																			// will
																			// probably
																			// want
																			// to
																			// modify
																			// the
																			// difficulty.
																			// Cannons
																			// are
																			// only
																			// present
																			// in
																			// the
																			// RandomLevel
																			// when
																			// difficulty
																			// is
																			// >=
																			// 3
		xExit = currentLevel.xExit; // The exit for our level is what it was for
									// the randomly generated level
		yExit = currentLevel.yExit;
		testLevel = currentLevel;
		LevelChunker levelChunker = new LevelChunker(currentLevel, CHUNK_SIZE);
		LevelNode levelHead = levelChunker.splitLevel(); // Split the levels map
															// into chunks
		int curLevelScore = evalLevel(levelHead);
		LevelResult bestLevel = combineLevelNodes(levelHead); // Keep track of
																// the best map
																// we have found
		int bestScore = curLevelScore;
		int k = 0;
		while (k < maxIterations && curLevelScore < acceptingScore) {
			temp = getTemperature(startTemp, ((double) k / maxIterations));
			Level newLevel = new Level(width, height);
			LevelResult res = combineLevelNodes(levelHead);
			copyMapAndSprite(newLevel, res.getMap(), res.getSprites());
			LevelChunker chunker = new LevelChunker(newLevel, CHUNK_SIZE);
			LevelNode headCopy = chunker.splitLevel();
			LevelNode neighborState = getNeighbor(headCopy); // Get the head
																// node of the
																// neighbor
			if (neighborState != null) {
				int neighborScore = evalLevel(neighborState);
				if (shouldAccept(curLevelScore, neighborScore, temp)) { // Determine
																		// if we
																		// accept
																		// the
																		// neighbor
																		// state
					levelHead = neighborState;
					curLevelScore = neighborScore;
				}
				if (neighborScore > bestScore) { // Keep track of the best level
													// we have found
					bestLevel = combineLevelNodes(neighborState);
					bestScore = neighborScore;
				}
			}
			k++;
		}

		return bestLevel;
	}

	public void copyMapAndSprite(Level level, byte[][] map,
			SpriteTemplate[][] sprites) {
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				level.getMap()[i][j] = map[i][j];
				level.getSpriteTemplates()[i][j] = sprites[i][j];
			}
		}
	}

	public void printNodes(LevelNode one, LevelNode two) {
		one.printChunkMap();
		System.out.println();
		two.printChunkMap();
	}

	/**
	 * Determines if we should move to the neighborState
	 * 
	 * @param curLevelScore
	 * @param neighborScore
	 * @param temperature
	 * @return
	 */
	public boolean shouldAccept(double curLevelScore, double neighborScore,
			double temperature) {
		if (curLevelScore < neighborScore) // Always accept if better
			return true;
		else if (curLevelScore == neighborScore)
			return false;
		else { // Accept based on a probability
			return Math.exp(-Math.abs(neighborScore - curLevelScore)
					/ temperature) > Math.random();
		}
	}

	/**
	 * TODO Get a neighboring level of the given level. Should be able to just
	 * modify the passed in list and return the given head;
	 * 
	 * @param level
	 *            The head node of the level node list
	 * @return The head node of the level node list
	 */
	public LevelNode getNeighbor(LevelNode levelHead) {
		LevelNode curNode = levelHead;

		// The stored value of the minimum difficulties
		int minDualDifficulty = 100;
		// Pretend both are different
		int dualTypeDifferences = 2;

		LevelNode minNode = levelHead;

		// System.out.println("Level head");
		// levelHead.printStats();

		// Let's go ahead and set the curNode as next to head. We really
		// shouldn't mess with the first 10 squares
		curNode = curNode.getNextNode();


		// Let's get the specific node we'll be improving, semi randomly
		while (curNode.getNextNode().getNextNode().getNextNode().getNextNode()
				.getNextNode() != null) {

			LevelNode prevNode = curNode.getPrevNode();
			LevelNode nextNode = curNode.getNextNode();

			int curDualDifficulty = 0;
			int curTypeDifferences = 0;

			if (prevNode != null) {
				curDualDifficulty += Math.abs(curNode.getDifficulty()
						- prevNode.getDifficulty());

				if (curNode.getType() != prevNode.getType()) {
					curTypeDifferences++;
				}
			}

			if (nextNode != null) {
				curDualDifficulty += Math.abs(curNode.getDifficulty()
						- nextNode.getDifficulty());
				if (curNode.getType() != nextNode.getType()) {
					curTypeDifferences++;
				}
			}

			// System.out.println("Min difficulty: "+minDualDifficulty);

			// WARNING: MIGHT NOT WANT TO GRAB THIS ONE
			int randomChance = random.nextInt(4);
		
			// If we're less than the minimum, just set all the relevant values
			if (curDualDifficulty < minDualDifficulty) {

				// System.out.println("Less than: "+x+", with Difficulty: "+curDualDifficulty);
				minDualDifficulty = curDualDifficulty;
				dualTypeDifferences = curTypeDifferences;
				minNode = curNode;
			} else if (curDualDifficulty == minDualDifficulty
					|| randomChance == 0) {// If we're the same, then use the
											// difference of types on either
											// side as a tie breaker

				// System.out.println("Equal to: "+x+", with Difficulty: "+curDualDifficulty);
				// Since we're looking for the worst, if the types aren't the
				// same, that's worse
				if (curTypeDifferences < dualTypeDifferences) {
					minDualDifficulty = curDualDifficulty;
					dualTypeDifferences = curTypeDifferences;
					minNode = curNode;
				}

				// Otherwise, just keep things how they are
			}

			curNode = curNode.getNextNode();

		}

		// System.out.println("X: "+x);

		// Okay, so now we've got a new node, what's it look like?
		// System.out.println("Min Node:");
		// minNode.printStats();

		// First, let's concentrate on type, and making sure that's different
		if (dualTypeDifferences == 0) {// If neither are different, we need to
										// make this middle one more interesting

			if (minNode.getDifficulty() == 0) {// If difficulty is 0 for this
												// one, it's all flat along
												// here, let's make it randomly
												// something else

				
				
				LevelNode.Type newType = generateTypeByProbabilities();
				
				
				

				switch (newType) {
				case CANNONS:
					alterCannons(minNode, true);
					break;
				case HILL:
					alterHills(minNode, true);
					break;
				case JUMP:
					alterJumps(minNode, true);
					break;
				case TUBES:
					alterTubes(minNode, true);
					break;
				case ENEMIES:
					alterEnemies(minNode, true);
					break;
				}

			} else {
				LevelNode.Type curType = minNode.getType();

				while (curType == minNode.getType()) {
					curType = generateTypeByProbabilities();

				}// We've got a new type now!

				int nextDifficulty = 0;

				if (minNode.getNextNode() != null) {
					nextDifficulty = minNode.getNextNode().getDifficulty();
				}
				int prevDifficulty = minNode.getPrevNode().getDifficulty();
				int curDifficulty = minNode.getDifficulty();

				int differenceNext = nextDifficulty - curDifficulty;
				int differencePrev = prevDifficulty - curDifficulty;

				if (differenceNext > 0 && differencePrev > 0) {

					switch (minNode.getType()) {
					case CANNONS:
						alterCannons(minNode, false);
						break;
					case HILL:
						alterHills(minNode, false);
						break;
					case JUMP:
						alterJumps(minNode, false);
						break;
					case TUBES:
						alterTubes(minNode, false);
						break;
					case ENEMIES:
						alterEnemies(minNode, false);
						break;
					}
				} else if (differenceNext < 0 && differencePrev < 0) {
					// make us more difficult

					switch (minNode.getType()) {
					case CANNONS:
						alterCannons(minNode, true);
						break;
					case HILL:
						alterHills(minNode, true);
						break;
					case JUMP:
						alterJumps(minNode, true);
						break;
					case TUBES:
						alterTubes(minNode, true);
						break;
					case ENEMIES:
						alterEnemies(minNode, true);
						break;
					}
				} else {
					//If we've reset, have it be random
					if(resetRecorder){
						hillIncrease = random.nextBoolean();
						jumpIncrease = random.nextBoolean();
						tubesIncrease = random.nextBoolean();
						cannonsIncrease = random.nextBoolean();
						enemiesIncrease = random.nextBoolean();
					}
					
					
					
					
					switch (curType) {
					case CANNONS:
						alterCannons(minNode, cannonsIncrease);
						break;
					case HILL:
						alterHills(minNode, hillIncrease);
						break;
					case JUMP:
						alterJumps(minNode, jumpIncrease);
						break;
					case TUBES:
						alterTubes(minNode, tubesIncrease);
						break;
					case ENEMIES:
						alterEnemies(minNode, enemiesIncrease);
						break;
					}

				}

			}

		} else {// Else, we're gonna focus on altering the difficulty

			LevelNode.Type curType = minNode.getType();

			while (curType == minNode.getType()) {

				curType = generateTypeByProbabilities();
			}// We've got a new type now!

			int nextDifficulty = minNode.getDifficulty();

			if (minNode.getNextNode() != null) {
				nextDifficulty = minNode.getNextNode().getDifficulty();
			}
			int prevDifficulty = minNode.getPrevNode().getDifficulty();
			int curDifficulty = minNode.getDifficulty();

			int differenceNext = nextDifficulty - curDifficulty;
			int differencePrev = prevDifficulty - curDifficulty;

			if (differenceNext > 0 && differencePrev > 0) {
				// make us easier

				switch (minNode.getType()) {
				case CANNONS:
					alterCannons(minNode, false);
					break;
				case HILL:
					alterHills(minNode, false);
					break;
				case JUMP:
					alterJumps(minNode, false);
					break;
				case TUBES:
					alterTubes(minNode, false);
					break;
				case ENEMIES:
					alterEnemies(minNode, false);
					break;
				}
			} else if (differenceNext < 0 && differencePrev < 0) {
				// make us more difficult

				switch (minNode.getType()) {
				case CANNONS:
					alterCannons(minNode, true);
					break;
				case HILL:
					alterHills(minNode, true);
					break;
				case JUMP:
					alterJumps(minNode, true);
					break;
				case TUBES:
					alterTubes(minNode, true);
					break;
				case ENEMIES:
					alterEnemies(minNode, true);
					break;
				}
			} else {
				// If we're in an in-between state, change our type and
				// difficulty randomly
				
				//If we've reset, have it be random
				if(resetRecorder){
					hillIncrease = random.nextBoolean();
					jumpIncrease = random.nextBoolean();
					tubesIncrease = random.nextBoolean();
					cannonsIncrease = random.nextBoolean();
					enemiesIncrease = random.nextBoolean();
				}
				
				
				
				
				switch (curType) {
				case CANNONS:
					alterCannons(minNode, cannonsIncrease);
					break;
				case HILL:
					alterHills(minNode, hillIncrease);
					break;
				case JUMP:
					alterJumps(minNode, jumpIncrease);
					break;
				case TUBES:
					alterTubes(minNode, tubesIncrease);
					break;
				case ENEMIES:
					alterEnemies(minNode, enemiesIncrease);
					break;
				}

			}
		}

		System.out.println("-------------------");
		return levelHead;
	}
	
	
	public LevelNode.Type generateTypeByProbabilities(){
		float foo = random.nextFloat();
		
		if(foo<=hillProbability){
			return LevelNode.Type.HILL;
		}
		else if(foo>hillProbability && foo<=jumpProbability){
			return LevelNode.Type.JUMP;
		}
		else if(foo>jumpProbability && foo<=tubesProbability){
			return LevelNode.Type.JUMP;
		}
		else if(foo>tubesProbability && foo<=cannonsProbability){
			return LevelNode.Type.CANNONS;
		}
		else{
			return LevelNode.Type.ENEMIES;
		}
	}

	// All these alter the passed in levelNode based on the passed in boolean
	public void alterHills(LevelNode levelNode, boolean increase) {
		if (levelNode != null) {
			if (increase) {
				// System.out.println("Building hill");
			//	levelNode.setBlock(1, 0, COIN);
				buildHillSansEnemies(levelNode, random.nextInt(5), 5);
			} else {
				//levelNode.setBlock(1, 1, COIN);
				flattenLevelNode(levelNode, random.nextInt(5), 5);
			}
		}
	}
	
	
	

	// Makes a hill
	private void buildHillSansEnemies(LevelNode node, int xo, int maxLength) {
		int length = random.nextInt(5) + 2;
		if (length > maxLength)
			length = maxLength;

		int floor = 0;
		boolean foundGround = false;
		boolean doEet = true;
		for (int i = xo; i < xo + length; i++) {
			for (int j = 0; j < height; j++) {
				if (!foundGround
						&& (node.getBlock(i, j) == -127 || node.getBlock(i, j) == HILL_TOP)
						&& i < 9) {
					foundGround = true;
					floor = j;
				}
				if (node.getBlock(i, j) == BLOCK_EMPTY
						|| node.getBlock(i, j) == BLOCK_COIN
						|| node.getBlock(i, j) == BLOCK_POWERUP
						|| node.getBlock(i, j) == TUBE_TOP_LEFT
						|| node.getBlock(i, j) == TUBE_TOP_RIGHT
						|| node.getBlock(i, j) == TUBE_SIDE_LEFT
						|| node.getBlock(i, j) == TUBE_TOP_RIGHT) {
					doEet = false;
				}

			}
		}

		if (floor != 0 & doEet) {
			floor -= (2 + random.nextInt(3));

			int setCoins = random.nextInt(2);
			if (node.getBlock(xo - 1, floor) == 0
					&& node.getBlock(xo + length, floor) == 0) {
				// clear stuff out
				for (int x = xo; x < xo + length; x++) {
					for (int y = 0; y < height; y++) {
						if (y > 2 && node.getBlock(x, y) == COIN) {
							node.setBlock(x, y, (byte) 0);
						}
					}

				}
				for (int x = xo; x < xo + length; x++) {
					for (int y = 0; y < height; y++) {

						/**
						 * if(y==floor && node.getBlock(x, y)!=-127 &&
						 * node.getBlock(x, y)!=GROUND && node.getBlock(x,
						 * y)!=HILL_FILL && node.getBlock(x, y)!=HILL_TOP &&
						 * node.getBlock(x, y)!=HILL_TOP_LEFT &&
						 * node.getBlock(x, y)!=HILL_TOP_RIGHT){
						 */

						if (y == floor - 1 && setCoins == 1
								&& (node.getBlock(x, y) == 0)) {
							node.setBlock(x, y, COIN);
						} else if (y == floor
								&& (node.getBlock(x, y) == 0 || node.getBlock(
										x, y) == HILL_FILL)){
								//|| node.getBlock(x, y) == HILL_LEFT
								//|| node.getBlock(x, y) == HILL_RIGHT) {
							if (x == xo) {
								node.setBlock(x, y, HILL_TOP_LEFT);
							} else if (x == (xo + length - 1)) {// WARNING
																// SHOULD BE -1
																// changing to
																// -2
								node.setBlock(x, y, HILL_TOP_RIGHT);
							} else {
								// node.setBlock(x, y, HILL_TOP_RIGHT);
								node.setBlock(x, y, HILL_TOP);
							}
						} else if (y > floor
								&& node.getBlock(x, y) != HILL_TOP_LEFT
								&& node.getBlock(x, y) != HILL_TOP_RIGHT
								&& node.getBlock(x, y) != HILL_TOP) {
							if (node.getBlock(x, y) == 0
									|| node.getBlock(x, y) == HILL_FILL) {
								// if(node.getBlock(x, y)!=-127 &&
								// node.getBlock(x, y)!=GROUND &&
								// node.getBlock(x, y)!=HILL_FILL){

								// Need Hill Fill or it's crazy time
								if (x == xo && node.getBlock(x, y) != HILL_FILL) {
									node.setBlock(x, y, HILL_LEFT);
								} else if (x == (xo + length - 1)
										&& node.getBlock(x, y) != HILL_FILL) {
									node.setBlock(x, y, HILL_RIGHT);
								} else {
									node.setBlock(x, y, HILL_FILL);
								}
							}
						}

					}
				}
			}
		}
	}

	// Basically sets the node to FLAT type
	public void flattenLevelNode(LevelNode node, int xo, int maxLength) {

		// Only do it when you have hills totally in this area
		boolean doIt = false;

		int[] hillCount = new int[height];

		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				if (node.getBlock(x, y) == HILL_TOP_LEFT
						|| node.getBlock(x, y) == HILL_TOP_RIGHT) {
					hillCount[y] += 2;
				}
			}
		}

		for (int i = 0; i < hillCount.length; i++) {
			if (hillCount[i] % 2 != 0) {
				doIt = false;
			}
		}

		if (doIt) {
			for (int x = 0; x < 10; x++) {

				int floor = 0;
				for (int yf = 0; yf < height; yf++) {
					if (node.getBlock(x, yf) == -127
							|| node.getBlock(x, yf) == RIGHT_GRASS_EDGE
							|| node.getBlock(x, yf) == LEFT_GRASS_EDGE
							|| node.getBlock(x, yf) == RIGHT_UP_GRASS_EDGE
							|| node.getBlock(x, yf) == LEFT_UP_GRASS_EDGE
							|| node.getBlock(x, yf) == RIGHT_POCKET_GRASS
							|| node.getBlock(x, yf) == LEFT_POCKET_GRASS
							|| node.getBlock(x, yf) == GROUND
							) {
						if (floor == 0) {
							floor = yf;
						}
					}
				}

				if (floor != 0) {
					for (int y = floor - 1; y > 2; y--) {

						node.setBlock(x, y, (byte) 0);
					}
				}
			}
		}
	}

	public void alterJumps(LevelNode levelNode, boolean increase) {
		if (increase) {
			// System.out.println("Increasing jumps");
			//levelNode.setBlock(0, 0, BLOCK_EMPTY);

			int randomStart = random.nextInt(7) + 2;

			makeJump(levelNode, randomStart, 5);
		} else {
			// System.out.println("Decreasing jumps");
			//levelNode.setBlock(0, 1, BLOCK_EMPTY);
			fillInJumps(levelNode, 0, 10);
		}
	}

	// Helper methods
	public void fillInJumps(LevelNode node, int xo, int maxLength) {

		int i = xo;
		int j = 0;
		int floor = 0;
		boolean foundGround = false;
		while (i < xo + maxLength && !foundGround) {
			while (j < height && !foundGround) {
				if (node.getBlock(i, j) == -127) {
					foundGround = true;
					floor = j;
				}

				j++;
			}
			i++;
		}

		if (foundGround) {
			for (int x = xo; x < xo + maxLength; x++) {
				for (int y = 0; y < height; y++) {
					if (y == floor) {
						if (node.getBlock(x, y) == 0
								|| node.getBlock(x, y) == RIGHT_UP_GRASS_EDGE
								|| node.getBlock(x, y) == LEFT_UP_GRASS_EDGE
								) {
							node.setBlock(x, y, (byte) -127);
						}
					} else if (y > floor) {
						if (node.getBlock(x, y) == 0 || node.getBlock(x, y) == -127
								|| node.getBlock(x, y) == RIGHT_GRASS_EDGE
								|| node.getBlock(x, y) == RIGHT_UP_GRASS_EDGE
								|| node.getBlock(x, y) == LEFT_GRASS_EDGE
								|| node.getBlock(x, y) == LEFT_UP_GRASS_EDGE
								|| node.getBlock(x, y)==HILL_LEFT
								|| node.getBlock(x, y)==HILL_RIGHT) {
							node.setBlock(x, y, GROUND);
						}
					}
				}
			}
		}
	}

	// Helper methods
	public void makeJump(LevelNode node, int xo, int maxLength) {
		if (maxLength < 2 || xo < 1) {
			return;
		}

		int length = random.nextInt(10 - xo);

		if (length > maxLength) {
			length = maxLength;
		}

		int floor = height - 1;

		boolean doIt = true;
		// System.out.println("I GOT HERE!");
		for (int x = xo; x < xo + length; x++) {
			boolean foundGround = false;
			int y = 2;
			while (y < height && !foundGround) {
				if (!foundGround && node.getBlock(x, y) != 0) {
					if (node.getBlock(x, y) == -127) {
						foundGround = true;
						// Added this
						floor = y;
					} else {
						doIt = false;
					}
				}

				y++;
			}

		}

		if (node.getBlock(xo, floor) == 0
				|| node.getBlock(xo + length, floor) == 0
				|| node.getBlock(xo - 1, floor) == 0
				|| node.getBlock(xo + length + 1, floor) == 0) {
			// Don't do it! There's already a jump there
		} else if (doIt) {

			for (int x = xo - 1; x < xo + length; x++) {
				boolean foundGround = false;
				for (int y = 0; y < height; y++) {
					if (x != xo - 1 && x != xo + length - 1) {
						if (y <= floor) {
							if (!foundGround) {
								if (node.getBlock(x, y) == -127) {
									foundGround = true;
									node.setBlock(x, y, (byte) 0);
								}
							} else {
								node.setBlock(x, y, (byte) 0);
							}
							// node.setBlock(x, y,(byte)0);
						}
					} else if (x == xo - 1) {
						if (!foundGround) {
							// TODO Figure out what to di if it's a
							// right_up_grass_edge
							if (node.getBlock(x, y) == -127) {
								foundGround = true;
								node.setBlock(x, y, RIGHT_UP_GRASS_EDGE);
							} else if (node.getBlock(x, y) == RIGHT_UP_GRASS_EDGE) {
								foundGround = true;

								if (xo != 1) {
									if (node.getBlock(xo - 2, y) != -127) {
										node.setBlock(x, y, (byte) 0);
									}
								}
							} else if (node.getBlock(x, y) == LEFT_UP_GRASS_EDGE) {
								foundGround = true;
								node.setBlock(x, y, (byte) 0);
							}
						} else {
							if (node.getBlock(x, y) != -127) {
								node.setBlock(x, y, RIGHT_GRASS_EDGE);
							} else if (node.getBlock(x, y) == -127) {
								node.setBlock(x, y, RIGHT_UP_GRASS_EDGE);
							}
						}
					} else if (x == xo + length - 1) {
						if (!foundGround) {
							// TODO Figure out what to di if it's a
							// left_up_grass_edge
							if (node.getBlock(x, y) == -127) {
								foundGround = true;
								node.setBlock(x, y, LEFT_UP_GRASS_EDGE);
							} else if (node.getBlock(x, y) == LEFT_UP_GRASS_EDGE) {
								foundGround = true;
							} else if (node.getBlock(x, y) == RIGHT_UP_GRASS_EDGE) {
								foundGround = true;
								node.setBlock(x, y, (byte) 0);
							}

						}

						else {
							node.setBlock(x, y, LEFT_GRASS_EDGE);
						}
					}
				}
			}
		}
	}

	public void alterTubes(LevelNode levelNode, boolean increase) {
		if (levelNode != null) {
			if (increase) {
				// System.out.println("Building Tube");
				//levelNode.setBlock(0, 0, TUBE_TOP_LEFT);
				buildTubesNode(levelNode, random.nextInt(7), 2);
			} else {
				//levelNode.setBlock(0, 0, TUBE_TOP_RIGHT);
				// System.out.println("Squishing Tube");
				squishDownTubes(levelNode, 0, 10);
			}
		}
	}

	// Makes tubes
	private int buildTubesNode(LevelNode node, int xo, int maxLength) {
		int length = random.nextInt(5) + 5;
		if (length > maxLength)
			length = maxLength;

		int xTube = xo;

		// Fine the floor
		int floor = 0;
		boolean foundGround = false;
		boolean doEet = true;
		for (int i = xTube; i < xTube + 2; i++) {
			for (int j = 0; j < height; j++) {
				if (!foundGround && (node.getBlock(i, j) == -127)) {
					foundGround = true;
					floor = j;
				}
				if (node.getBlock(i, j) == BLOCK_EMPTY
						|| node.getBlock(i, j) == BLOCK_COIN
						|| node.getBlock(i, j) == BLOCK_POWERUP
						|| node.getBlock(i, j) == TUBE_TOP_LEFT
						|| node.getBlock(i, j) == TUBE_TOP_RIGHT
						|| node.getBlock(i, j) == TUBE_SIDE_LEFT
						|| node.getBlock(i, j) == TUBE_TOP_RIGHT
						|| node.getBlock(i, j) == (byte) (14 + 0 * 16)
						|| node.getBlock(i, j) == (byte) (14 + 1 * 16)
						|| node.getBlock(i, j) == (byte) (14 + 2 * 16)
						|| node.getBlock(i, j) == HILL_FILL
						|| node.getBlock(i, j) == HILL_TOP
						|| node.getBlock(i, j) == HILL_RIGHT
						|| node.getBlock(i, j) == HILL_LEFT
						|| node.getBlock(i, j) == HILL_TOP_RIGHT
						|| node.getBlock(i, j) == HILL_TOP_LEFT) {
					doEet = false;
				}

			}
		}

		if (floor != 0 && doEet) {

			int tubeHeight = floor - random.nextInt(2) - 2;

			boolean canBuild = true;
			for (int a = xTube; a < xTube + 2; a++) {
				for (int b = 0; b < floor; b++) {
					if (node.getBlock(a, b) != 0) {
						canBuild = false;
					}
				}
			}

			if (canBuild) {

				for (int x = xo; x < 10; x++) {

					if (x == xTube && random.nextInt(2) == 1) {
						node.setSpriteTemplate(x, tubeHeight,
								new SpriteTemplate(Enemy.ENEMY_FLOWER, false));
						ENEMIES++;
					}

					for (int y = 0; y < height; y++) {
						if (y >= floor && (x == xTube || x == xTube + 1)) {
							// Had this in here at first as I copied it from the
							// original, turns out we don't need it
							if (y == floor) {
								// node.setBlock(x, y, (byte)-127);
							} else {
								// node.setBlock(x, y, GROUND);
							}

						} else {
							if ((x == xTube || x == xTube + 1)
									&& y >= tubeHeight) {
								int xPic = 10 + x - xTube;

								if (y == tubeHeight) {
									// tube top
									if (x == xTube) {
										node.setBlock(x, y, TUBE_TOP_LEFT);
									} else if (x == xTube + 1) {
										node.setBlock(x, y, TUBE_TOP_RIGHT);
									}
								} else {
									// tube side
									node.setBlock(x, y, (byte) (xPic + 1 * 16));
								}
							}
						}
					}
				}
			}
		}
		return length;
	}

	// Squishes the first tube it sees
	private void squishDownTubes(LevelNode node, int xo, int maxLength) {
		int pipeX = 0;
		boolean foundTube = false;

		for (int x = xo; x < xo + maxLength; x++) {
			for (int y = 0; y < height; y++) {
				if (!foundTube) {
					if (node.getBlock(x, y) == Level.TUBE_TOP_LEFT
							&& x + 1 < 10) {
						foundTube = true;
						pipeX = x;
						node.setBlock(x, y, (byte) 0);
						node.setSpriteTemplate(x, y, null);
					}

				} else {
					if (x == pipeX || x == pipeX + 1) {
						if (node.getBlock(x, y) == TUBE_TOP_RIGHT
								|| node.getBlock(x, y) == TUBE_SIDE_LEFT
								|| node.getBlock(x, y) == TUBE_SIDE_RIGHT) {
							node.setBlock(x, y, (byte) 0);

						}
						node.setSpriteTemplate(x, y, null);
					}
				}
			}
		}

	}

	public void alterCannons(LevelNode levelNode, boolean increase) {
		if (levelNode != null) {
			if (increase) {
				//levelNode.setBlock(0, 0, (byte) (14 + 1 * 16));
				// System.out.println("building cannons");
				buildCannonsNode(levelNode, random.nextInt(5),
						random.nextInt(5));
			} else {
				//levelNode.setBlock(0, 1, (byte) (14 + 1 * 16));
				// System.out.println("squishing cannons");
				squishDownCannons(levelNode, 0, 10);
			}
		}
	}

	// Builds Cannons
	private int buildCannonsNode(LevelNode node, int xo, int maxLength) {
		int length = random.nextInt(10) + 2;
		if (length > maxLength)
			length = maxLength;
		// System.out.println("I WANT TO BUILD A CANNON");
		// Find the ground, use that to determine where the cannon should be

		int floor = 0;

		boolean foundGround = false;
		boolean doEet = true;

		for (int j = 0; j < height; j++) {
			if (!foundGround && (node.getBlock(xo, j) == -127)) {
				foundGround = true;
				floor = j;
			}
			if (node.getBlock(xo, j) == BLOCK_EMPTY
					|| node.getBlock(xo, j) == BLOCK_COIN
					|| node.getBlock(xo, j) == BLOCK_POWERUP
					|| node.getBlock(xo, j) == TUBE_TOP_LEFT
					|| node.getBlock(xo, j) == TUBE_TOP_RIGHT
					|| node.getBlock(xo, j) == TUBE_SIDE_LEFT
					|| node.getBlock(xo, j) == TUBE_TOP_RIGHT
					|| node.getBlock(xo, j) == (byte) (14 + 0 * 16)
					|| node.getBlock(xo, j) == (byte) (14 + 1 * 16)
					|| node.getBlock(xo, j) == (byte) (14 + 2 * 16)
					|| node.getBlock(xo, j) == HILL_FILL
					|| node.getBlock(xo, j) == HILL_TOP
					|| node.getBlock(xo, j) == HILL_RIGHT
					|| node.getBlock(xo, j) == HILL_LEFT
					|| node.getBlock(xo, j) == HILL_TOP_RIGHT
					|| node.getBlock(xo, j) == HILL_TOP_LEFT) {
				doEet = false;
			}

		}
		int cannonHeight = floor - (random.nextInt(4) + 1);

		for (int i = xo - 1; i < xo + 2; i++) {
			if (node.getBlock(i, cannonHeight) == BLOCK_EMPTY
					|| node.getBlock(i, cannonHeight) == BLOCK_COIN
					|| node.getBlock(i, cannonHeight) == BLOCK_POWERUP
					|| node.getBlock(i, cannonHeight) == TUBE_TOP_LEFT
					|| node.getBlock(i, cannonHeight) == TUBE_TOP_RIGHT
					|| node.getBlock(i, cannonHeight) == TUBE_SIDE_LEFT
					|| node.getBlock(i, cannonHeight) == TUBE_TOP_RIGHT) {
				doEet = false;
			}
		}

		if (doEet && foundGround) {
			// The x position of the cannon
			System.out.println("I MADE A CANNON");
			int xCannon = xo;
			for (int x = xo; x < xo + length; x++) {

				for (int y = 0; y < height; y++) {
					if (y >= floor) {
						// This is the ground, don't put blocks there
					} else {
						if (x == xCannon && y >= cannonHeight) {
							if (y == cannonHeight) {
								node.setBlock(x, y, (byte) (14 + 0 * 16));
							} else if (y == cannonHeight + 1) {
								node.setBlock(x, y, (byte) (14 + 1 * 16));
							} else {
								node.setBlock(x, y, (byte) (14 + 2 * 16));
							}

							node.setSpriteTemplate(x, y, null);
						}
					}
				}
			}
		}
		return length;
	}

	private void squishDownCannons(LevelNode node, int xo, int maxLength) {
		int pipeX = 0;
		boolean foundCannon = false;

		for (int x = xo; x < xo + maxLength; x++) {
			for (int y = 0; y < height; y++) {
				if (!foundCannon) {
					if (node.getBlock(x, y) == (byte) (14 + 0 * 16)
							|| node.getBlock(x, y) == (byte) (14 + 1 * 16)
							|| node.getBlock(x, y) == (byte) (14 + 2 * 16)) {
						foundCannon = true;
						pipeX = x;
						node.setBlock(x, y, (byte) 0);
					}

				} else {
					if (x == pipeX) {
						if (node.getBlock(x, y) == (byte) (14 + 0 * 16)
								|| node.getBlock(x, y) == (byte) (14 + 1 * 16)
								|| node.getBlock(x, y) == (byte) (14 + 2 * 16)) {
							node.setBlock(x, y, (byte) 0);
						}

					}
				}
			}
		}

	}

	public void alterEnemies(LevelNode levelNode, boolean increase) {
		if (levelNode != null) {
			if (increase) {
				// System.out.println("I WANT to add an enemy");
				boolean iSetAnEnemy = false;
				int x = 0;
				while (!iSetAnEnemy && x < 10) {
					int y = 15 - 4 - random.nextInt(3);
					while (!iSetAnEnemy && y < height) {
						if (levelNode.getBlock(x, y) == 0
								&& levelNode.getSprite(x, y) == null) {// levelNode.getBlock(x,
																		// y)==BLOCK_EMPTY
																		// &&
																		// levelNode.getBlock(x,
																		// y+1)!=BLOCK_EMPTY){
							// Add a single enemy
							addEnemyLineNode(levelNode, x, 1, y);
							iSetAnEnemy = true;
						}
						y++;
					}
					x++;
				}
			} else {
				// System.out.println("Removing enemy");
				removeFirstEnemyISee(levelNode, 0, 10);
			}
		}
	}

	// The name is a horribly red herring, it just makes a single enemy
	private void addEnemyLineNode(LevelNode node, int x0, int x1, int y) {
		for (int x = x0; x < x1; x++) {
			// What type of enemy it'll be
			int type = random.nextInt(4);
			if (node.getBlock(x, y) == 0) {
				// Make that enemy!
				node.setSpriteTemplate(x, y,
						new SpriteTemplate(type,
								random.nextInt(35) < difficulty));
				ENEMIES++;
			}

		}
	}

	public void removeFirstEnemyISee(LevelNode node, int xo, int maxLength) {
		boolean destroyedAnEnemy = false;
		int x = xo;
		while (x < xo + maxLength && !destroyedAnEnemy) {
			int y = 0;
			while (y < height && !destroyedAnEnemy) {
				if (node.getSprite(x, y) != null) {
					node.setSpriteTemplate(x, y, null);
					destroyedAnEnemy = true;
				}
				y++;
			}
			x++;
		}
	}

	/**
	 * Decrease the temperature based on the the fraction of the number of
	 * iterations / max iterations
	 * 
	 * @param temp
	 * @param progress
	 * @return
	 */
	public double getTemperature(double temp, double progress) {
		return temp * (1 - progress);
	}

	/**
	 * A higher score represents a better level. Currently a sum of variance and
	 * difficulty. May want to change this?
	 * 
	 * @param LevelNode
	 *            the head of the LevelNode list
	 * @return A integer representation of how good this level is.
	 */
	public int evalLevel(LevelNode head) {
		int variance = 0;
		int totalDifficulty = 0;
		LevelNode curNode = head;
		while (curNode != null) {
			curNode.determineChunkStats();
			int curDifficulty = curNode.getDifficulty();
			totalDifficulty += curDifficulty;
			LevelNode prevNode = curNode.getPrevNode();
			if (prevNode != null)
				variance += Math.abs(curDifficulty - prevNode.getDifficulty());
			curNode = curNode.getNextNode();
		}
		return variance + totalDifficulty;
	}

	/**
	 * Take the head of the level node list and concatenate all the maps
	 * 
	 * @param head
	 *            The head node of the level node list
	 * @return Concatenated maps
	 */
	public LevelResult combineLevelNodes(LevelNode head) {
		byte[][] resultMap = new byte[width][height];
		SpriteTemplate[][] resultSprites = new SpriteTemplate[width][height];
		int elementCount = 0;
		LevelNode curNode = head;
		while (curNode != null) {
			byte[][] curNodeMap = curNode.getMap();
			SpriteTemplate[][] curNodeSprites = curNode.getSprites();
			System.arraycopy(curNodeMap, 0, resultMap, elementCount,
					curNodeMap.length);
			System.arraycopy(curNodeSprites, 0, resultSprites, elementCount,
					curNodeSprites.length);
			elementCount += curNodeMap.length;
			curNode = curNode.getNextNode();
		}
		return new LevelResult(resultMap, resultSprites);
	}

	/**
	 * We will probably need to keep track of the map and the sprites, so using
	 * this class to keep track of results.
	 * 
	 */
	private class LevelResult {
		/**
		 * The map
		 */
		private byte[][] map;

		/**
		 * The sprites
		 */
		private SpriteTemplate[][] sprites;

		public LevelResult(byte[][] map, SpriteTemplate[][] sprites) {
			this.map = map;
			this.sprites = sprites;
		}

		public void setMap(byte[][] map) {
			this.map = map;
		}

		public void setSprites(SpriteTemplate[][] sprites) {
			this.sprites = sprites;
		}

		public byte[][] getMap() {
			return this.map;
		}

		public SpriteTemplate[][] getSprites() {
			return this.sprites;
		}
	}

	public void creat(long seed, int difficulty, int type) {
		this.type = type;
		this.difficulty = difficulty;

		lastSeed = seed;
		random = new Random(seed);

		// create the start location
		int length = 0;
		length += buildStraight(0, width, true);

		// create all of the medium sections
		while (length < width - 64) {
			// length += buildZone(length, width - length);
			length += buildStraight(length, width - length, false);
			// length += buildStraight(length, width-length, false);
			// length += buildHillStraight(length, width-length);
			// length += buildJump(length, width-length);
			// length += buildTubes(length, width-length);
			// length += buildCannons(length, width - length);
		}

		// set the end piece
		int floor = height - 1 - random.nextInt(4);

		xExit = length + 8;
		yExit = floor;

		// fills the end piece
		for (int x = length; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);
				}
			}
		}

		if (type == LevelInterface.TYPE_CASTLE
				|| type == LevelInterface.TYPE_UNDERGROUND) {
			int ceiling = 0;
			int run = 0;
			for (int x = 0; x < width; x++) {
				if (run-- <= 0 && x > 4) {
					ceiling = random.nextInt(4);
					run = random.nextInt(4) + 4;
				}
				for (int y = 0; y < height; y++) {
					if ((x > 4 && y <= ceiling) || x < 1) {
						setBlock(x, y, GROUND);
					}
				}
			}
		}

		fixWalls();

	}

	private int buildJump(int xo, int maxLength) {
		gaps++;
		// jl: jump length
		// js: the number of blocks that are available at either side for free
		int js = random.nextInt(4) + 2;
		int jl = random.nextInt(2) + 2;
		int length = js * 2 + jl;

		boolean hasStairs = random.nextInt(3) == 0;

		int floor = height - 1 - random.nextInt(4);
		// run from the start x position, for the whole length
		for (int x = xo; x < xo + length; x++) {
			if (x < xo + js || x > xo + length - js - 1) {
				// run for all y's since we need to paint blocks upward
				for (int y = 0; y < height; y++) { // paint ground up until the
													// floor
					if (y >= floor) {
						setBlock(x, y, GROUND);
					}
					// if it is above ground, start making stairs of rocks
					else if (hasStairs) { // LEFT SIDE
						if (x < xo + js) { // we need to max it out and level
											// because it wont
											// paint ground correctly unless two
											// bricks are side by side
							if (y >= floor - (x - xo) + 1) {
								setBlock(x, y, ROCK);
							}
						} else { // RIGHT SIDE
							if (y >= floor - ((xo + length) - x) + 2) {
								setBlock(x, y, ROCK);
							}
						}
					}
				}
			}
		}

		return length;
	}

	private int buildCannons(int xo, int maxLength) {
		int length = random.nextInt(10) + 2;
		if (length > maxLength)
			length = maxLength;

		int floor = height - 1 - random.nextInt(4);
		int xCannon = xo + 1 + random.nextInt(4);
		for (int x = xo; x < xo + length; x++) {
			if (x > xCannon) {
				xCannon += 2 + random.nextInt(4);
			}
			if (xCannon == xo + length - 1)
				xCannon += 10;
			int cannonHeight = floor - random.nextInt(4) - 1;

			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);
				} else {
					if (x == xCannon && y >= cannonHeight) {
						if (y == cannonHeight) {
							setBlock(x, y, (byte) (14 + 0 * 16));
						} else if (y == cannonHeight + 1) {
							setBlock(x, y, (byte) (14 + 1 * 16));
						} else {
							setBlock(x, y, (byte) (14 + 2 * 16));
						}
					}
				}
			}
		}

		return length;
	}

	private int buildHillStraight(int xo, int maxLength) {
		int length = random.nextInt(10) + 10;
		if (length > maxLength)
			length = maxLength;

		int floor = height - 1 - random.nextInt(4);
		for (int x = xo; x < xo + length; x++) {
			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);
				}
			}
		}

		addEnemyLine(xo + 1, xo + length - 1, floor - 1);

		int h = floor;

		boolean keepGoing = true;

		boolean[] occupied = new boolean[length];
		while (keepGoing) {
			h = h - 2 - random.nextInt(3);

			if (h <= 0) {
				keepGoing = false;
			} else {
				int l = random.nextInt(5) + 3;
				int xxo = random.nextInt(length - l - 2) + xo + 1;

				if (occupied[xxo - xo] || occupied[xxo - xo + l]
						|| occupied[xxo - xo - 1] || occupied[xxo - xo + l + 1]) {
					keepGoing = false;
				} else {
					occupied[xxo - xo] = true;
					occupied[xxo - xo + l] = true;
					addEnemyLine(xxo, xxo + l, h - 1);
					if (random.nextInt(4) == 0) {
						decorate(xxo - 1, xxo + l + 1, h);
						keepGoing = false;
					}
					for (int x = xxo; x < xxo + l; x++) {
						for (int y = h; y < floor; y++) {
							int xx = 5;
							if (x == xxo)
								xx = 4;
							if (x == xxo + l - 1)
								xx = 6;
							int yy = 9;
							if (y == h)
								yy = 8;

							if (getBlock(x, y) == 0) {
								setBlock(x, y, (byte) (xx + yy * 16));
							} else {
								if (getBlock(x, y) == HILL_TOP_LEFT)
									setBlock(x, y, HILL_TOP_LEFT_IN);
								if (getBlock(x, y) == HILL_TOP_RIGHT)
									setBlock(x, y, HILL_TOP_RIGHT_IN);
							}
						}
					}
				}
			}
		}

		return length;
	}

	private void addEnemyLine(int x0, int x1, int y) {
		for (int x = x0; x < x1; x++) {
			if (random.nextInt(35) < difficulty + 1) {
				int type = random.nextInt(4);

				if (difficulty < 1) {
					type = Enemy.ENEMY_GOOMBA;
				} else if (difficulty < 3) {
					type = random.nextInt(3);
				}

				setSpriteTemplate(x, y,
						new SpriteTemplate(type,
								random.nextInt(35) < difficulty));
				ENEMIES++;
			}
		}
	}

	private int buildTubes(int xo, int maxLength) {
		int length = random.nextInt(10) + 5;
		if (length > maxLength)
			length = maxLength;

		int floor = height - 1 - random.nextInt(4);
		int xTube = xo + 1 + random.nextInt(4);
		int tubeHeight = floor - random.nextInt(2) - 2;
		for (int x = xo; x < xo + length; x++) {
			if (x > xTube + 1) {
				xTube += 3 + random.nextInt(4);
				tubeHeight = floor - random.nextInt(2) - 2;
			}
			if (xTube >= xo + length - 2)
				xTube += 10;

			if (x == xTube && random.nextInt(11) < difficulty + 1) {
				setSpriteTemplate(x, tubeHeight, new SpriteTemplate(
						Enemy.ENEMY_FLOWER, false));
				ENEMIES++;
			}

			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);

				} else {
					if ((x == xTube || x == xTube + 1) && y >= tubeHeight) {
						int xPic = 10 + x - xTube;

						if (y == tubeHeight) {
							// tube top
							setBlock(x, y, (byte) (xPic + 0 * 16));
						} else {
							// tube side
							setBlock(x, y, (byte) (xPic + 1 * 16));
						}
					}
				}
			}
		}

		return length;
	}

	private int buildStraight(int xo, int maxLength, boolean safe) {
		int length = random.nextInt(10) + 2;

		if (safe)
			length = 10 + random.nextInt(5);

		if (length > maxLength)
			length = maxLength;

		int floor = height - 1 - random.nextInt(4);

		// runs from the specified x position to the length of the segment
		for (int x = xo; x < xo + length; x++) {
			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);
				}
			}
		}

		if (!safe) {
			if (length > 5) {
				decorate(xo, xo + length, floor);
			}
		}

		return length;
	}

	private void decorate(int xStart, int xLength, int floor) {
		// if its at the very top, just return
		if (floor < 1)
			return;

		// boolean coins = random.nextInt(3) == 0;
		boolean rocks = true;

		// add an enemy line above the box
		addEnemyLine(xStart + 1, xLength - 1, floor - 1);

		int s = random.nextInt(4);
		int e = random.nextInt(4);

		if (floor - 2 > 0) {
			if ((xLength - 1 - e) - (xStart + 1 + s) > 1) {
				for (int x = xStart + 1 + s; x < xLength - 1 - e; x++) {
					setBlock(x, floor - 2, COIN);
					COINS++;
				}
			}
		}

		s = random.nextInt(4);
		e = random.nextInt(4);

		// this fills the set of blocks and the hidden objects inside them
		if (floor - 4 > 0) {
			if ((xLength - 1 - e) - (xStart + 1 + s) > 2) {
				for (int x = xStart + 1 + s; x < xLength - 1 - e; x++) {
					if (rocks) {
						if (x != xStart + 1 && x != xLength - 2
								&& random.nextInt(3) == 0) {
							if (random.nextInt(4) == 0) {
								setBlock(x, floor - 4, BLOCK_POWERUP);
								BLOCKS_POWER++;
							} else { // the fills a block with a hidden coin
								setBlock(x, floor - 4, BLOCK_COIN);
								BLOCKS_COINS++;
							}
						} else if (random.nextInt(4) == 0) {
							if (random.nextInt(4) == 0) {
								setBlock(x, floor - 4, (byte) (2 + 1 * 16));
							} else {
								setBlock(x, floor - 4, (byte) (1 + 1 * 16));
							}
						} else {
							setBlock(x, floor - 4, BLOCK_EMPTY);
							BLOCKS_EMPTY++;
						}
					}
				}
			}
		}
	}

	private void fixWalls() {
		boolean[][] blockMap = new boolean[width + 1][height + 1];

		for (int x = 0; x < width + 1; x++) {
			for (int y = 0; y < height + 1; y++) {
				int blocks = 0;
				for (int xx = x - 1; xx < x + 1; xx++) {
					for (int yy = y - 1; yy < y + 1; yy++) {
						if (getBlockCapped(xx, yy) == GROUND) {
							blocks++;
						}
					}
				}
				blockMap[x][y] = blocks == 4;
			}
		}
		blockify(this, blockMap, width + 1, height + 1);
	}

	private void blockify(Level level, boolean[][] blocks, int width, int height) {
		int to = 0;
		if (type == LevelInterface.TYPE_CASTLE) {
			to = 4 * 2;
		} else if (type == LevelInterface.TYPE_UNDERGROUND) {
			to = 4 * 3;
		}

		boolean[][] b = new boolean[2][2];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int xx = x; xx <= x + 1; xx++) {
					for (int yy = y; yy <= y + 1; yy++) {
						int _xx = xx;
						int _yy = yy;
						if (_xx < 0)
							_xx = 0;
						if (_yy < 0)
							_yy = 0;
						if (_xx > width - 1)
							_xx = width - 1;
						if (_yy > height - 1)
							_yy = height - 1;
						b[xx - x][yy - y] = blocks[_xx][_yy];
					}
				}

				if (b[0][0] == b[1][0] && b[0][1] == b[1][1]) {
					if (b[0][0] == b[0][1]) {
						if (b[0][0]) {
							level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
						} else {
							// KEEP OLD BLOCK!
						}
					} else {
						if (b[0][0]) {
							// down grass top?
							level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
						} else {
							// up grass top
							level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
						}
					}
				} else if (b[0][0] == b[0][1] && b[1][0] == b[1][1]) {
					if (b[0][0]) {
						// right grass top
						level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
					} else {
						// left grass top
						level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
					}
				} else if (b[0][0] == b[1][1] && b[0][1] == b[1][0]) {
					level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
				} else if (b[0][0] == b[1][0]) {
					if (b[0][0]) {
						if (b[0][1]) {
							level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
						} else {
							level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
						}
					} else {
						if (b[0][1]) {
							// right up grass top
							level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
						} else {
							// left up grass top
							level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
						}
					}
				} else if (b[0][1] == b[1][1]) {
					if (b[0][1]) {
						if (b[0][0]) {
							// left pocket grass
							level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
						} else {
							// right pocket grass
							level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
						}
					} else {
						if (b[0][0]) {
							level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
						} else {
							level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
						}
					}
				} else {
					level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
				}
			}
		}
	}

	public RandomLevel clone() throws CloneNotSupportedException {

		RandomLevel clone = new RandomLevel(width, height);

		clone.xExit = xExit;
		clone.yExit = yExit;
		byte[][] map = getMap();
		SpriteTemplate[][] st = getSpriteTemplate();

		for (int i = 0; i < map.length; i++)
			for (int j = 0; j < map[i].length; j++) {
				clone.setBlock(i, j, map[i][j]);
				clone.setSpriteTemplate(i, j, st[i][j]);
			}
		clone.BLOCKS_COINS = BLOCKS_COINS;
		clone.BLOCKS_EMPTY = BLOCKS_EMPTY;
		clone.BLOCKS_POWER = BLOCKS_POWER;
		clone.ENEMIES = ENEMIES;
		clone.COINS = COINS;

		return clone;

	}

}
