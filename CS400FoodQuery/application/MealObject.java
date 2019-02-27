package application;

import java.util.ArrayList;
import java.util.List;

public class MealObject {

	// list of FoodItems in the meal
	private List<FoodItem> foodItems;
	
	/**
	 * Default constructor initializes foodItems.
	 */
	public MealObject() {
		foodItems = new ArrayList<FoodItem>();
	}
	
	/**
	 * Adds a FoodItem to the meal.
	 * @param food the FoodItem to be added to the list
	 */
	public void addFood(FoodItem food) {
		foodItems.add(food);
	}
	
	/**
	 * Gets all of the FoodItem's in the meal.
	 * @return a list of FoodItems containing all of the items in the meal.
	 */
	public List<FoodItem> getMeal() {
		return foodItems;
	}
	
	/**
	 * Finds the index of a FoodItem in the meal with a name matching that of a String passed in.
	 * @param name of the FoodItem to search for the index of.
	 * @return index of the name passed in. If not found, return -1.
	 */
	public int findIndex(String name) {
		for(int i = 0; i < foodItems.size(); i++) {
			if(foodItems.get(i).getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Removes a FoodItem from foodItems in the meal at a specified index.
	 * @param index in foodItems of the FoodItem to be removed.
	 */
	public void removeAtIndex(int index) {
		foodItems.remove(index);
	}
}