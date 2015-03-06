package geneticAlgorithms;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Random;
import java.util.BitSet;

public class Num23 {
	
	final static int N = 30; //How many members are there per generation?
	final static int charNum = 9; //How many characters per binary?
	final static int charSize = 4; //How big is each character?
	final static int binarySize = charNum * charSize; //How big is each binary?
	final static int goal = -234; //The goal.
	final static float crossoverRate = 0.7f; //How often two randomly selected binaries will splice with each other.
	final static float mutationRate = 0.04f; //How often a bit in a binary will mutate. Higher rates require higher N.
	final static int hugeNumber = 99999; //Used in a few not very important places.
	final static int searchDepth = 10000; //How many times do you want to breed a new population of binaries?
	final static boolean writerOn = false; //This variable controls if the genetic algorithm is being recorded or not. Can generate large files for huge search depths and huge N!
	final static boolean bias = false; //Add an extra bias to possibly enhance convergence. Bias is added for binaries who's fourth operation is + or -.
	final static float biasWeight = 0.002f; //The extra bias weight.
	final static int fitnessMethod = 1; //How do you generate fitness scores? Takes 0 or 1.
	final static int suppressLowest = 12; //Automatically removes the n weakest from the current generation. 0 to turn off. If set too high, can cause Nan fitnessScores.
	
	static Float MaximumFitDist = (float) hugeNumber;
	static BitSet maxFitBit = null;
	static Float localFitDist = (float) hugeNumber;
	static BitSet localFitBit = null;
	
	private static Random rnd = new Random();
	
    static Hashtable<Integer, String> decode = new Hashtable<Integer, String>();
    
	public static void init() {
		decode.put(0, "0");
		decode.put(1, "1");
		decode.put(2, "2");
		decode.put(3, "3");
		decode.put(4, "4");
		decode.put(5, "5");
		decode.put(6, "6");
		decode.put(7, "7");
		decode.put(8, "8");
		decode.put(9, "9");
		decode.put(10, "+");
		decode.put(11, "-");
		decode.put(12, "*");
		decode.put(13, "/");
		decode.put(14, "Err");
		decode.put(15, "Err");
	}

	public static void main(String[] args) {
		init();
		BitSet[] equations = new BitSet[N];
		Float[] fitnessScores = new Float[N];
		RouletteWheel rw;
		
		BitSet[] newEquations = new BitSet[N];
		BitSet[] incoming;
		
		BitSet tempBit1;
		BitSet tempBit2;
		BitSet temp;
		
		PrintWriter writer = null;
		if (writerOn) {
			try {
				writer = new PrintWriter("test.txt", "UTF-8");
			} catch (FileNotFoundException e) {
				System.out.println("Err");
			} catch (UnsupportedEncodingException e) {
				System.out.println("Err");				
			}
			writer.println("The goal is: " + goal);
		}
			
		//We generate random bitsets.
		for (int i = 0; i < N; i++) {
			equations[i] = randomBitSet(binarySize);
		}
			
		temp = new BitSet(36);
		temp.set(0,36, false);
		for (int i = 5; i < 36; i += 8) {
			temp.set(i, true);
			temp.set(i+2, true);
		}
		equations[0] = temp;
		
		for (int gen = 0; gen < searchDepth; gen++) {
			localFitDist = (float) hugeNumber;
			//We test each bitset's fitness.
			for (int i = 0; i < N; i++) {
				fitnessScores[i] = fitnessTest(equations[i]);
			}
			
			//We record the current generation of binaries.
			if (writerOn) {
				writer.println("Generation: " + gen);
				for (int i = 0; i < N; i++) {
					writer.println(toMathEquation(equations[i]));
				}
			}
			
			rw = new RouletteWheel(equations, fitnessScores);
			
			if (suppressLowest > 0) {
				rw.removeLowest(suppressLowest);
			}
			
			try {
				rw.normalizeWeights(10f);
			} catch (InvalidWeights e) {
				System.out.println("You messed up.");
			}
			
			if (writerOn) {
				writer.println("Rw: " + rw);
				writer.println("Local maximum value achieved: " + goal + " +- "+ localFitDist + "\n");
				writer.println("It's equation was: " + toMathEquation(localFitBit) + "\n");
			}
			
			for (int i = 0; i < N; i += 2) {
				tempBit1 = mutate(clone((BitSet)rw.pickRandomItem()));
				tempBit2 = mutate(clone((BitSet)rw.pickRandomItem()));
				incoming = crossOver(tempBit1, tempBit2);
				newEquations[i] = incoming[0];
				newEquations[i+1] = incoming[1];
			}
			
			equations = newEquations;
		}
		
		if (writerOn) {
			writer.println("Generation: " + (searchDepth+1));
			for (int i = 0; i < N; i++) {
				writer.println(toMathEquation(equations[i]));
			}
			
			writer.println("Maximum value achieved: " + goal + " +- "+ MaximumFitDist);
			writer.println("It's equation was: " + toMathEquation(maxFitBit));
			writer.close();
		}
		
		System.out.println("Maximum value achieved: " + goal + " +- "+ MaximumFitDist);
		System.out.println("It's equation was: " + toMathEquation(maxFitBit));
	}

	public static BitSet randomBitSet(int n) {
		//Generates a random bitset.
		BitSet temp = new BitSet(n);
		for (int i = 0; i < n; i++) {
			temp.set(i, randomBool());
		}
		return temp;
	}
	
	public static boolean randomBool() {
		//Used in generating a random bitset.
		return rnd.nextBoolean();
	}
	
	public static int toInt(BitSet bits) {
		//We convert a bitset to an int.
		int num = 0;
		for (int i = 0; i < binarySize; i++) {
			num += (bits.get(i)? Math.pow(2, i) : 0);
		}
		return num;
	}
	
	@SuppressWarnings("all")
	public static Float fitnessTest(BitSet bits) {
		//We generate a fitness score.
		Float score = 0f;
		int interpretedScore = 0;
		Float distance = 0f;
		String oneChar;
		try {
			interpretedScore = interpretBitSet(bits);
			if (interpretedScore == hugeNumber) {
				//Bad binary!
				score = 0f;
				distance = (float) hugeNumber;
			} else {
				distance = (float) Math.abs(goal-interpretedScore);
				if (fitnessMethod == 0) {
					score = 1f/distance;
				} else if (fitnessMethod == 1) {
					score = 1f/((distance)*(distance));				
				}
			}
		} catch (ArithmeticException e) {
			System.out.println("Not sure what just happened." + toBinaryString(bits));
		}
		
		if (bias && interpretedScore != hugeNumber) {
			oneChar = decode.get(toInt(bits.get(28, 32)));
			if (oneChar.equalsIgnoreCase("+") || oneChar.equalsIgnoreCase("-")) {
				score += biasWeight;
			}
		}
		
		//We Calculate the max fitness for record keeping purposes.
		if (distance < MaximumFitDist) {
			MaximumFitDist = distance;
			maxFitBit = bits;
		}
		if (distance < localFitDist) {
			localFitDist = distance;
			localFitBit = bits;
		}
		
		//Done!
		return score;
	}
	
	public static int interpretBitSet(BitSet bits) {
		int total;
		int num;
		int operator;
		BitSet[] chars = new BitSet[charNum];
		
		//We split the binary number into individual characters, aka chars.
		chars = breakIntoChunks(bits);
		
		//We read the first character in.
		num = toInt(chars[0]);
		
		//We check that we have a valid first character.
		if (num > 9) {
			//If not 0-9, we return a large number, forcing the fitness score as close to 0 as possible.
			return hugeNumber;
		} else {
			total = num;
		}
		
		//We iterate through the rest of the bitset to compute the total.
		int i = 1;
		do {
			//We read in the next two characters.
			operator = toInt(chars[i]);
			num = toInt(chars[i+1]);
			
			//We check the characters validity.
			if (operator < 10 || operator > 13) {
				return hugeNumber;		
			}
			if (num > 9) {
				return hugeNumber;
			}
			
			//We now the characters are valid, and process them.
			if (operator == 10) {
				total += num;
			} else if (operator == 11) {
				total -= num;
			} else if (operator == 12) {
				total *= num;
			} else {
				try {
					total /= num;
				} catch (ArithmeticException e) {
					return hugeNumber;
				}
			}
			i += 2;
		} while (i < charNum);
		
		return total;
	}
	
	public static String toBinaryString(BitSet bits) {
		//This method is for convenience/testing purposes only.
		//This method turns a bitset into a string binary representation.
		String output = "";
		for (int i = 0; i < binarySize; i++) {
			output += (bits.get(i)? "1" : "0");
		}
		
		return output;
	}
	
	public static String toMathEquation(BitSet bits) {
		//This method is for convenience/testing purposes only.
		//This method turns a bitset into a mathematical equation.
		BitSet[] chars = breakIntoChunks(bits);
		
		String output = "";
		
		for (int i = 0; i < charNum; i++) {
			output += decode.get(toInt(chars[i]));
		}
		return output;
	}
	
	public static BitSet mutate(BitSet bits) {
		//Mutation occurs here.
		for (int i = 0; i < binarySize; i++) {
			if (rnd.nextFloat() < mutationRate) {
				bits.flip(i);
			}
		}
		
		return bits;
	}
	
	public static BitSet[] breakIntoChunks(BitSet bits) {
		BitSet[] chars = new BitSet[charNum];
		for (int i = 0; i < charNum; i++) {
			chars[i] = bits.get(i*charSize, i*charSize+charSize);
		}
		return chars;
	}
	
	public static BitSet[] crossOver(BitSet bits1, BitSet bits2) {
		//This method crossovers two bitsets.
		//Basically, it splices
		if (rnd.nextFloat() < crossoverRate) {
			BitSet temp1 = new BitSet();
			BitSet temp2 = new BitSet();
			
			int splice = rnd.nextInt(binarySize);
			
			int i = 0;
			do {
				temp1.set(i, bits1.get(i));
				i++;
			} while (i < splice);
			do {
				temp1.set(i, bits2.get(i));
				i++;
			} while (i < binarySize);
			
			i = 0;
			do {
				temp2.set(i, bits2.get(i));
				i++;
			} while (i < splice);
			do {
				temp2.set(i, bits1.get(i));
				i++;
			} while (i < binarySize);
			return new BitSet[]{temp1, temp2};
		}
		
		return new BitSet[]{bits1, bits2};
	}
	
	static public BitSet clone(BitSet bits) {
		BitSet newSet = new BitSet(binarySize);
		for (int i = 0; i < binarySize; i++) {
			newSet.set(i, bits.get(i));
		}
		return newSet;
	}
}
