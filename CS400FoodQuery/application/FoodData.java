package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents the backend for managing all the operations associated
 * with FoodItems
 * 
 * @author sapan (sapan@cs.wisc.edu)
 */
public class FoodData implements FoodDataADT<FoodItem> {

	// List of all the food items.
	private List<FoodItem> foodItemList;

	// List of all the food items filtered by name.
	private List<FoodItem> filteredListByName;

	// List of all the food items filtered by nutrients.
	private List<FoodItem> filteredListByNutrients;
	
	// Map of nutrients and value.
	private HashMap<String, Double> nutrients;

	/**
	 * Public constructor
	 */
	public FoodData() {
		foodItemList = new ArrayList<FoodItem>();
		filteredListByName = new ArrayList<FoodItem>();
		filteredListByNutrients = new ArrayList<FoodItem>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skeleton.FoodDataADT#loadFoodItems(java.lang.String)
	 */
	@Override
	public void loadFoodItems(String filePath) {
		foodItemList.clear();
		Path pathToFile = Paths.get(filePath);

		// create an instance of BufferedReader
		try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) {

			// read the first line from the text file
			String line = br.readLine();

			// loop until all lines are read
			while (line != null && line.charAt(0) != ',') {

				// use string.split to load a string array with the values from file
				String[] food = line.split(",");

				// make new FoodItem with id and name from file
				FoodItem newItem = new FoodItem(food[0], food[1]);

				// add nutrients given by the rest of the line
				for (int i = 2; i < food.length; i += 2) {
					newItem.addNutrient(food[i], Double.valueOf(food[i + 1]));
					newItem.addNutrient(food[i], Double.valueOf(food[i + 1]));
				}

				// add FoodItem to the list
				foodItemList.add(newItem);

				// read next line before looping, null if end reached
				line = br.readLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skeleton.FoodDataADT#filterByName(java.lang.String)
	 */
	@Override
	public List<FoodItem> filterByName(String substring) {
		// clear the filtered list and re-filter using the new substring
		filteredListByName.clear();
		for (int i = 0; i < filteredListByNutrients.size(); i++) {
			/*
			 * Only return results that are also consistent with the nutrient specifications.
			 * This method must ALWAYS be called after filterByNutrients so that the list is
			 * not null.
			 */
			if (filteredListByNutrients.get(i).getName().toLowerCase()
					.contains(substring.toLowerCase())) {
				filteredListByName.add(filteredListByNutrients.get(i));
			}
		}
		return filteredListByName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skeleton.FoodDataADT#filterByNutrients(java.util.List)
	 */
	@Override
	public List<FoodItem> filterByNutrients(List<String> rules) {
		// clear filteredByNutrients list so it can be overridden
		filteredListByNutrients.clear();
		
		// initially add all items in the foodItemList to filteredByNutrients
		for(int i = 0; i < foodItemList.size(); i++) {
			filteredListByNutrients.add(foodItemList.get(i));
		}
		
		// iterate through all of the rules in the rules list
		for(int f = 0; f < rules.size(); f++) {
			// get the current rule's parameters
			String[] thisRule = rules.get(f).split(" ");
			
			/*
			 * Iterate through each item in the food list and check if any of the remaining items
			 * break the current rule. If they do, find their location in the filtered list and
			 * remove them.
			 */
			for (int i = 0; i < foodItemList.size(); i++) {
				nutrients = foodItemList.get(i).getNutrients();
				
				if(thisRule[1].equals(">=") && filteredListByNutrients
						.contains(foodItemList.get(i))) {
					if(!nutrients.containsKey(thisRule[0]) 
							|| nutrients.get(thisRule[0]) < Double.valueOf(thisRule[2])) {
						int index = filteredListByNutrients.indexOf(foodItemList.get(i));
						filteredListByNutrients.remove(index);
					}
				}
				
				else if(thisRule[1].equals("==") && filteredListByNutrients
						.contains(foodItemList.get(i))) {
					if(!nutrients.containsKey(thisRule[0])
							|| !nutrients.get(thisRule[0]).equals(Double.valueOf(thisRule[2]))) {
						int index = filteredListByNutrients.indexOf(foodItemList.get(i));
						filteredListByNutrients.remove(index);
					}
				}
				
				else if(thisRule[1].equals("<=") && filteredListByNutrients
						.contains(foodItemList.get(i))) {
					if(!nutrients.containsKey(thisRule[0]) 
							|| nutrients.get(thisRule[0]) > Double.valueOf(thisRule[2])) {
						int index = filteredListByNutrients.indexOf(foodItemList.get(i));
						filteredListByNutrients.remove(index);
					}
				}
			}
		}
		return filteredListByNutrients;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skeleton.FoodDataADT#addFoodItem(skeleton.FoodItem)
	 */
	@Override
	public void addFoodItem(FoodItem foodItem) {
		foodItemList.add(foodItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skeleton.FoodDataADT#getAllFoodItems()
	 */
	@Override
	public List<FoodItem> getAllFoodItems() {
		return foodItemList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skeleton.FoodDataADT#saveFoodItems()
	 */
	@Override
	public void saveFoodItems(String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			// alphabetizes list
			foodItemList.sort((food1, food2) -> food1.getName().toLowerCase().compareTo(food2.getName().toLowerCase()));
			for (int i = 0; i < foodItemList.size(); i++) {
				writer.write(foodItemList.get(i).getID() + ",");
				writer.write(foodItemList.get(i).getName() + ",");
				writer.write("calories," + foodItemList.get(i).getNutrientValue("calories") + ",");
				writer.write("fat," + foodItemList.get(i).getNutrientValue("fat") + ",");
				writer.write("carbohydrate," + foodItemList.get(i).getNutrientValue("carbohydrate") + ",");
				writer.write("fiber," + foodItemList.get(i).getNutrientValue("fiber") + ",");
				writer.write("protein," + foodItemList.get(i).getNutrientValue("protein"));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Finds the index of a specific FoodItem in the list. If the food list does not contain
	 * the item passed in, return -1.
	 * 
	 * @param foodItem foodItem to find the index of
	 * @return index of the FoodItem passed in
	 */
	public int findIndex(String foodItem) {
		for (int i = 0; i < foodItemList.size(); i++) {
			if (foodItemList.get(i).getName().equals(foodItem)) {
				return i;
			}
		}
		return -1;
	}	
}