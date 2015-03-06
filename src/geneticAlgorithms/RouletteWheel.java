package geneticAlgorithms;

import java.util.Arrays;
import java.util.Random;

public class RouletteWheel {
	final int size;
	Object[] items;
	Float[] weights;
	Float weightSize = null;
	
	private Random rnd = new Random();
	
	public RouletteWheel(int size_) {
		size = size_;
		items = new Object[size];
		weights = new Float[size];
	}
	
	public RouletteWheel(Object[] items_) {
		size = items_.length;
		items = items_;
		weights = new Float[size];
	}
	
	public RouletteWheel(Object[] items_, Float[] weights_) {
		size = items_.length;
		items = items_;
		weights = weights_;
		try {
			sumWeights();
		} catch (InvalidWeights e) {
			System.out.println("You messed up.");
		}
	}
	
	public void setWeight(Float weight, int i) {
		//This function sets the weight at i to weight.
		weights[i] = weight;
	}
	
	public void sumWeights() throws InvalidWeights {
		//This function finds the sum of wheel weights.
		Float sum = 0.0f;
		for (int i = 0; i < size; i++) {
			if (weights[i].equals(null)) {
				throw new InvalidWeights();
			} else {
				sum += weights[i];
			}
		}
		weightSize = sum;
	}
	
	public void normalizeWeights(Float target) throws InvalidWeights {
		//This function normalizes weights so that their total is the same as specified in the function.
		sumWeights();
		Float factor = target/weightSize;
		for (int i = 0; i < size; i++) {
			weights[i] *= factor;
		}
		weightSize = target;
	}
	
	public void forcePositiveWeights() throws InvalidWeights {
		//This method is good for making all weights positive, if not done already.
		for (int i = 0; i < size; i++) {
			if (weights[i].equals(null)) {
				throw new InvalidWeights();
			} else {
				weights[i] = Math.abs(weights[i]);
			}
		}
	}
	
	public Object pickRandomItem() {
		//This function picks a random item, based randomly on an item's weight in relation to other items' weight.
		Float i = rnd.nextFloat()*weightSize;
		Float j = 0.0f;
		int k;
		for (k = 0; j < i; k++) {
			try {
				j += weights[k];
			} catch (ArrayIndexOutOfBoundsException e) {
				//Normally an error shouldn't be thrown, but occassionally it does happen...
				return items[items.length-1];
			}
		}
		try {
			return items[k-1];
		} catch (ArrayIndexOutOfBoundsException e) {
			//Extremely rare error. Only occurs once every 400,000 search depths with N being 20.
			return items[0];
		}
	}
	
	public void removeLowest(int n) {
		if (n > size-1 || n == 0) {
			return;
		}
		
		//We hunt for the lowest values.
		Float[] lowest = new Float[n];
		Arrays.fill(lowest, Float.MAX_VALUE);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < n; j++) {
				if (weights[i] < lowest[j]) {
					lowest[j] = weights[i];
					j = n;
				}
			}
		}
		
		//One the lowest values are calculated, we act!
		Float newWeight;
		for (int i = 0; i < size; i++) {
			newWeight = weights[i] - lowest[n-1];
			if (newWeight>0) {
				weights[i] = newWeight;
			} else {
				weights[i] = 0f;
			}
		}
	}
	
	@Override
	public String toString() {
		if (weightSize == null) {
			return "No weightsize detected.";
		}
		String foo = "Rhoulette Wheel of size " + size + ":\n";
		for (int i = 0; i < size; i++) {
			foo += "Item:" + items[i] + " (Weight " + weights[i] + ")\n";
		}
		return foo;
	}
}
