package application;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {

	// String containing the user's query for a name substring
	private String nameQuery = "";

	// ArrayList containing the "Filter by nutrients" rules specified by user
	private List<String> nutritionRules = new ArrayList<String>();

	// Stores all food items that have been added to the "library"
	private FoodData foodData = new FoodData();
	
	// Keeps track of number of total FoodItems in master list
	private int totalItems = 0;
	
	private Label options = new Label("Food Options (" + totalItems + " total)");
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root1 = new BorderPane(); // border pane of main scene

			// add background image
			Image image = new Image("file:fruit.jpg");
			ImageView mv = new ImageView(image);
			mv.setOpacity(.2f);
			root1.getChildren().addAll(mv);

			// create the scene
			Scene scene1 = new Scene(root1, 1000, 600);

			// set title of the application
			primaryStage.setTitle("Food Query");

			List<FoodItem> food = foodData.getAllFoodItems();

			// alphabetizes list
			food.sort((food1, food2) -> food1.getName().toLowerCase().compareTo(food2.getName()
					.toLowerCase()));

			/*
			 * Adds check boxes to the food list so that the user can choose food to create
			 * a meal
			 */
			VBox foodList = new VBox();
			VBox checkBoxes = new VBox(); // contains check boxes and scroll bar
			// ArrayList of checkboxes user can click and add to the current meal
			List<CheckBox> addToMeal = new ArrayList<CheckBox>();

			foodList.getChildren().addAll(new Label("\n"), options, new Label("\n"));

			// FoodData item of all foods in list
			FoodData foodDataList = new FoodData();

			// iterates through entire food list and adds them to the options pane
			for (int i = 0; i < food.size(); i++) {
				CheckBox c = new CheckBox(food.get(i).getName());
				checkBoxes.getChildren().add(c);
				foodDataList.addFoodItem(food.get(i));
				addToMeal.add(c);
			}

			/*
			 * Creates a scroll bar for the list of food and check boxes
			 */
			ScrollPane scroll = new ScrollPane();
			totalItems = checkBoxes.getChildren().size();
			options = new Label("Food Options (" + totalItems + " total)");
			scroll.setContent(checkBoxes);
			scroll.setPrefHeight(400);
			scroll.setMinWidth(250);
			foodList.getChildren().add(scroll);

			/*
			 * Buttons for adding food to the current meal, filtering by nutrition, and
			 * searching
			 */
			Button addFood = new Button("Add to current meal");
			Button queryRules = new Button("Filter by Nutrition");
			Button nameSearch = new Button("Search");
			
			// add tooltips to make application's functions more new-user friendly
			addFood.setTooltip(new Tooltip("Add a new food to your meal"));
			queryRules.setTooltip(new Tooltip("Filter above results by nutrition"));
			nameSearch.setTooltip(new Tooltip("Search results by name"));

			foodList.getChildren().addAll(new Label("\n"), addFood, queryRules, nameSearch);
			foodList.setPrefWidth(300);

			// dialog that allows user to set and remove nutritional filters to the food
			// list
			queryRules.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					final Stage dialog = new Stage();
					dialog.setMinWidth(400);
					dialog.setMinHeight(500);
					dialog.initModality(Modality.APPLICATION_MODAL);
					dialog.initOwner(primaryStage);
					VBox dialogVbox = new VBox();

					// ArrayList storing which rules have already been applied
					List<CheckBox> checkRules = new ArrayList<CheckBox>();

					// display rules that are currently in effect
					dialogVbox.getChildren().add(new Label("Filters currently being " + 
							"applied: \n"));
					for (int i = 0; i < nutritionRules.size(); i++) {
						checkRules.add(new CheckBox(nutritionRules.get(i)));
					}

					// if there aren't any rules in place, display "none"
					if (checkRules.size() == 0)
						dialogVbox.getChildren().add(new Label("none"));

					dialogVbox.getChildren().addAll(checkRules);

					// Buttons allowing user to remove selected/all of the applied rules
					Button removeCheckedRules = new Button("Remove selected rules");
					Button removeAllRules = new Button("Remove all rules");
					removeCheckedRules.setTooltip(new Tooltip("The filters you have checked will"
							+ "no longer be applied to the food results"));
					removeAllRules.setTooltip(new Tooltip("All nutritional filters will be "
							+ "deleted"));
					
					dialogVbox.getChildren().addAll(removeCheckedRules, removeAllRules, 
							new Label("\n"), new Separator());

					// TextFields/Button allowing user to add new rules to the query
					Button addNewRule = new Button("Add rule");
					addNewRule.setTooltip(new Tooltip("Apply your nutritional filter to the "
							+ "food results"));
					HBox hNutrient = new HBox();
					TextField nutrient = new TextField();
					hNutrient.getChildren().addAll(new Label("Nutrient:  "), nutrient);
					HBox hOperator = new HBox();
					TextField operator = new TextField();
					hOperator.getChildren().addAll(new Label("Operator: "), operator);
					HBox hValue = new HBox();
					TextField value = new TextField();
					hValue.getChildren().addAll(new Label("Value:       "), value);

					/*
					 * Adds a new nutritional filter when addNewRule is clicked. Resets checkBoxes
					 * and addToMeal to account for the changes.
					 */
					addNewRule.setOnAction(e -> {
						// catch exceptions from user not inputting numeric value for "value"
						try {
							/*
							 * if/else if statements check if user input is valid. Nutrient must 
							 * be equal to calories, fat, carbohydrate, fiber, or protein. 
							 * Operator must be equal to >=, ==, or <=. Value must be a positive 
							 * number. If user inputs an invalid sequence for any of these 
							 * properties, an appropriate error message is displayed.
							 */
							if (!nutrient.getText().equals("calories") 
									&& !nutrient.getText().equals("fat")
									&& !nutrient.getText().equals("carbohydrate") 
									&& !nutrient.getText().equals("fiber")
									&& !nutrient.getText().equals("protein")) {
								Alert alert = new Alert(AlertType.INFORMATION);
								alert.setTitle(null);
								alert.setHeaderText("ERROR");
								alert.setContentText("Nutrient must be a valid type, i.e. "
										+ "calories, fat, carbohydrate, fiber, or protein");
								alert.showAndWait();
							} else if (!operator.getText().equals("==") 
									&& !operator.getText().equals(">=")
									&& !operator.getText().equals("<=")) {
								Alert alert = new Alert(AlertType.INFORMATION);
								alert.setTitle(null);
								alert.setHeaderText("ERROR");
								alert.setContentText("Operator must be valid, i.e. >=, ==, or <=");
								alert.showAndWait();
							} else if (Double.parseDouble(value.getText()) < 0) {
								Alert alert = new Alert(AlertType.INFORMATION);
								alert.setTitle(null);
								alert.setHeaderText("ERROR");
								alert.setContentText("Value must be a positive number");
								alert.showAndWait();
							} else {
								String add = nutrient.getText() + " " + operator.getText() + " " 
										+ value.getText();
								nutritionRules.add(add);

								// clear old data to be reset
								checkBoxes.getChildren().clear();
								addToMeal.clear();
								// get the FoodItems that meet the new criteria (and old, too!)
								List<FoodItem> fNuts = foodData.filterByNutrients(nutritionRules);

								if (nameQuery.length() > 0)
									fNuts = foodData.filterByName(nameQuery);

								// re-add only foods that fit nutritional and name requirements
								for (int i = 0; i < fNuts.size(); i++) {
									CheckBox c = new CheckBox(fNuts.get(i).getName());
									checkBoxes.getChildren().add(c);
									addToMeal.add(c);
								}

								// reset scroll box to the refreshed content
								scroll.setContent(checkBoxes);
								totalItems = checkBoxes.getChildren().size();
								options = new Label("Food Options (" + totalItems + " total)");
								foodList.getChildren().clear();
								foodList.getChildren().addAll(new Label("\n"), options, 
										new Label("\n"), scroll, new Label("\n"), addFood, 
										queryRules, nameSearch);
								foodList.setPrefWidth(300);
								dialog.close();
							}
						} catch (Exception ex) {
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle(null);
							alert.setHeaderText("ERROR");
							alert.setContentText("Invalid input: value must be a number");
							alert.showAndWait();
						}
					});

					/*
					 * Updates nutritionRules by removing the requirements selected by the user.
					 * Then gets the FoodItems that fit the new guidelines and resets the scroll
					 * pane displaying the food list to reflect these changes.
					 */
					removeCheckedRules.setOnAction(e -> {
						// update nutrition filter list
						for (int i = 0; i < checkRules.size(); i++) {
							if (checkRules.get(i).isSelected()) {
								String name = checkRules.get(i).getText();
								int index = nutritionRules.indexOf(name);
								nutritionRules.remove(index);
							}
						}

						// clear old data
						checkBoxes.getChildren().clear();
						addToMeal.clear();
						// get the FoodItems that meet the new criteria (and old, too!)
						List<FoodItem> fNuts = foodData.filterByNutrients(nutritionRules);

						if (nameQuery.length() > 0)
							fNuts = foodData.filterByName(nameQuery);

						// add foods that fit the new rules
						for (int i = 0; i < fNuts.size(); i++) {
							CheckBox c = new CheckBox(fNuts.get(i).getName());
							checkBoxes.getChildren().add(c);
							addToMeal.add(c);
						}

						// reset content to reflect changes
						scroll.setContent(checkBoxes);
						totalItems = checkBoxes.getChildren().size();
						options = new Label("Food Options (" + totalItems + " total)");
						foodList.getChildren().clear();
						foodList.getChildren().addAll(new Label("\n"), options, new Label("\n"), 
								scroll, new Label("\n"), addFood, queryRules, nameSearch);
						foodList.setPrefWidth(300);

						dialog.close();
					});

					// eliminate all nutritional rules & updates food list to reflect this
					removeAllRules.setOnAction(e -> {
						nutritionRules.clear();

						// since nutritionRules has been cleared, this will return all FoodItems
						List<FoodItem> fNuts = foodData.filterByNutrients(nutritionRules);

						// filter results by name, if applicable
						if (nameQuery.length() > 0)
							fNuts = foodData.filterByName(nameQuery);

						// re-add only foods that fit nutritional and name requirements
						for (int i = 0; i < fNuts.size(); i++) {
							CheckBox c = new CheckBox(fNuts.get(i).getName());
							checkBoxes.getChildren().add(c);
							addToMeal.add(c);
						}

						// reset scroll content back to all FoodItems since rules are gone
						scroll.setContent(checkBoxes);
						totalItems = checkBoxes.getChildren().size();
						options = new Label("Food Options (" + totalItems + " total)");
						foodList.getChildren().clear();
						foodList.getChildren().addAll(new Label("\n"), options, new Label("\n"), 
								scroll, new Label("\n"), addFood, queryRules, nameSearch);
						foodList.setPrefWidth(300);

						dialog.close();
					});

					// add components to allow new rules to be set
					dialogVbox.getChildren().addAll(hNutrient, hOperator, hValue, 
							new Label("\n"), addNewRule);
					Scene dialogScene = new Scene(dialogVbox, 300, 200);
					dialog.setScene(dialogScene);

					dialog.show();
				}
			});

			/*
			 * Dialog that allows users to search for a food by name. Any nutrition rules
			 * that the user has already input will be applied to this search.
			 */
			nameSearch.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					final Stage dialog = new Stage();
					dialog.initModality(Modality.APPLICATION_MODAL);
					dialog.initOwner(primaryStage);
					VBox dialogVbox = new VBox();

					// displays current name query user has in place
					String current = nameQuery;
					if (!(current.length() > 0))
						current = "none";
					Label currentQuery = new Label("Current name query in place: " + current);

					Button removeNameQuery = new Button("Remove search filter");
					removeNameQuery.setTooltip(new Tooltip("Your search query will no longer be"
							+ " applied to the food results"));

					removeNameQuery.setOnAction(e -> {
						nameQuery = "";

						// clear old data
						checkBoxes.getChildren().clear();
						addToMeal.clear();

						// apply any nutritional rules in place
						List<FoodItem> notFiltered = foodData.filterByNutrients(nutritionRules);

						// add all FoodItems fitting nutritionalRules, but eliminate name query
						for (int i = 0; i < notFiltered.size(); i++) {
							CheckBox c = new CheckBox(notFiltered.get(i).getName());
							checkBoxes.getChildren().add(c);
							addToMeal.add(c);
						}

						// reset scroll's content to reflect changes
						scroll.setContent(checkBoxes);
						totalItems = checkBoxes.getChildren().size();
						options = new Label("Food Options (" + totalItems + " total)");
						foodList.getChildren().clear();
						foodList.getChildren().addAll(new Label("\n"), options, new Label("\n"), 
								scroll, new Label("\n"), addFood, queryRules, nameSearch);
						foodList.setPrefWidth(300);

						dialog.close();
					});

					// allows user to enter a search query
					TextField searchBox = new TextField();

					// applies user's query to the food list
					Button apply = new Button("Apply search");
					apply.setTooltip(new Tooltip("Display only food results containing your "
							+ "search string"));

					// when apply is clicked, update food list to reflect user request
					apply.setOnAction(e -> {
						// clear old data
						checkBoxes.getChildren().clear();
						addToMeal.clear();

						nameQuery = searchBox.getText();

						// make sure results also fit any nutrition rules that have been applied
						foodData.filterByNutrients(nutritionRules);
						// get filtered results
						List<FoodItem> filteredNames = foodData.filterByName(nameQuery);

						// add only FoodItems that contain the specified substring
						for (int i = 0; i < filteredNames.size(); i++) {
							CheckBox c = new CheckBox(filteredNames.get(i).getName());
							checkBoxes.getChildren().add(c);
							addToMeal.add(c);
						}

						// reset scroll's content to reflect changes
						scroll.setContent(checkBoxes);
						totalItems = checkBoxes.getChildren().size();
						options = new Label("Food Options (" + totalItems + " total)");
						foodList.getChildren().clear();
						foodList.getChildren().addAll(new Label("\n"), options, new Label("\n"), 
								scroll, new Label("\n"), addFood, queryRules, nameSearch);
						foodList.setPrefWidth(300);

						dialog.close();
					});

					// add search box and apply button
					dialogVbox.getChildren().addAll(currentQuery, removeNameQuery, 
							new Label("Search: "), searchBox, apply);
					Scene dialogScene = new Scene(dialogVbox, 300, 200);
					dialog.setScene(dialogScene);

					dialog.show();
				}
			});

			// keeps track of the foods user has added to their current meal
			MealObject newMeal = new MealObject();

			Label meal = new Label("Your Meal");

			// stores items user has added to their meal
			TableView<FoodItem> mealList = new TableView<FoodItem>();
			// table contains one column: the food items user has added
			TableColumn<FoodItem, String> foodItemList = new TableColumn<FoodItem, String>
					("Food Items");
			foodItemList.setMinWidth(400);
			foodItemList.setCellValueFactory(new PropertyValueFactory<FoodItem, String>("name"));
			mealList.getColumns().add(foodItemList);
			// list that stores items in the mealList
			ObservableList<FoodItem> oList = FXCollections.observableArrayList();

			/*
			 * Adds selected food to the current meal
			 */
			addFood.setOnAction(e -> {
				// clear old data
				oList.clear();
				// add user selected items to the new meal
				for (int i = 0; i < addToMeal.size(); i++) {
					if (addToMeal.get(i).isSelected()) {
						String name = addToMeal.get(i).getText();
						int index = foodData.findIndex(name);
						if (!newMeal.getMeal().contains(food.get(index))) {
							newMeal.addFood(food.get(index));
						}
						addToMeal.get(i).setSelected(false);
					}
				}

				// don't allow duplicates to be added to the list of items in the meal
				for (int j = 0; j < newMeal.getMeal().size(); j++) {
					oList.add(newMeal.getMeal().get(j));
				}
				mealList.setItems(oList);
			});

			/*
			 * Middle section of the display that displays the food list and allows user to
			 * analyze their meal and remove items from it.
			 */
			VBox totals = new VBox();
			totals.setPrefWidth(400);

			// display nutritional content of the meal
			Button analyzeMeal = new Button();
			analyzeMeal.setTooltip(new Tooltip("View the nutritional content of your meal"));
			analyzeMeal.setText("Analyze Meal");

			/*
			 * Gets nutritional content of each food item in the user's meal and displays
			 * the calorie, fat, carbohydrate, fiber, and protein content of the meal as a
			 * whole
			 */
			analyzeMeal.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					// store totals in each category
					int totalCalories = 0;
					int totalFat = 0;
					int totalCarbs = 0;
					int totalFiber = 0;
					int totalProtein = 0;

					// add each food item's content to each category's total
					for (int i = 0; i < newMeal.getMeal().size(); i++) {
						totalCalories += newMeal.getMeal().get(i).getNutrientValue("calories");
						totalFat += newMeal.getMeal().get(i).getNutrientValue("fat");
						totalCarbs += newMeal.getMeal().get(i).getNutrientValue("carbohydrate");
						totalFiber += newMeal.getMeal().get(i).getNutrientValue("fiber");
						totalProtein += newMeal.getMeal().get(i).getNutrientValue("protein");
					}

					final Stage dialog = new Stage();
					dialog.initModality(Modality.APPLICATION_MODAL);
					dialog.initOwner(primaryStage);
					VBox dialogVbox = new VBox();
					dialogVbox.getChildren().add(new Text("Nutrition Summary:"));
					Label calories = new Label("Calories: " + totalCalories);
					Label fat = new Label("Fat: " + totalFat);
					Label carbs = new Label("Carbs: " + totalCarbs);
					Label fiber = new Label("Fiber: " + totalFiber);
					Label protein = new Label("Protein: " + totalProtein);
					Scene dialogScene = new Scene(dialogVbox, 300, 200);
					dialogVbox.getChildren().addAll(calories, fat, carbs, fiber, protein);
					dialog.setScene(dialogScene);

					dialog.show();
				}
			});

			// allow users to remove food items that they've already added to their meal
			Button removeFood = new Button("Remove items");
			removeFood.setTooltip(new Tooltip("Remove items that you've already added to your "
					+ "meal"));
			removeFood.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					final Stage dialog = new Stage();
					VBox dialogVbox = new VBox();
					VBox scrollBox = new VBox();
					scrollBox.setPrefHeight(200);
					dialog.setMinHeight(300);
					dialog.setMinWidth(400);

					/*
					 * ArrayList storing all of the items that have been added to the meal and
					 * allows users to select which one(s) to remove.
					 */
					List<CheckBox> removeFromMeal = new ArrayList<CheckBox>();
					// puts list in a scrollable pane
					ScrollPane list = new ScrollPane();
					list.setContent(scrollBox);

					// adds items in the meal to the scroll box
					for (int i = 0; i < newMeal.getMeal().size(); i++) {
						CheckBox c = new CheckBox(newMeal.getMeal().get(i).getName());
						scrollBox.getChildren().add(c);
						removeFromMeal.add(c);
					}

					// allows user to remove items they have selected
					Button remove = new Button("Remove selected items");
					remove.setTooltip(new Tooltip("Remove from your meal items you have checked"));

					remove.setOnAction(e -> {
						// clear old data
						oList.clear();

						// remove selected items from the meal
						for (int i = removeFromMeal.size() - 1; i >= 0; i--) {
							if (removeFromMeal.get(i).isSelected()) {
								String name = removeFromMeal.get(i).getText();
								newMeal.removeAtIndex(newMeal.findIndex(name));
								removeFromMeal.get(i).setSelected(false);
							}
						}

						// re-add items that the user did not select to be removed
						for (int j = 0; j < newMeal.getMeal().size(); j++) {
							if (!oList.contains(newMeal.getMeal().get(j))) {
								oList.add(newMeal.getMeal().get(j));
							}
						}

						mealList.setItems(oList);

						dialog.close();
					});

					// remove all of the items in the meal
					Button removeAll = new Button("Remove all items");
					removeAll.setTooltip(new Tooltip("Remove all added items from your meal"));

					removeAll.setOnAction(e -> {
						// clear old data
						oList.clear();

						// remove all items in the meal
						for (int i = newMeal.getMeal().size() - 1; i >= 0; i--) {
							newMeal.removeAtIndex(i);
						}

						mealList.setItems(oList);

						dialog.close();
					});

					// add components to the remove vBox
					dialogVbox.getChildren().addAll(new Text("Select items to remove"), 
							list, remove, removeAll);

					dialog.initModality(Modality.APPLICATION_MODAL);
					dialog.initOwner(primaryStage);
					Scene dialogScene = new Scene(dialogVbox, 300, 200);
					dialog.setScene(dialogScene);
					dialog.show();
				}
			});

			// add proper components to the middle section of the application
			totals.getChildren().addAll(new Label("\n"), meal, new Label("\n"), mealList,
					new Label("\n"), analyzeMeal, removeFood);

			/*
			 * Allows the user to create a new food item that contains a given number of
			 * calories, fat, carbohydrates, fiber, and protein. ID is auto-generated.
			 */
			VBox vBox2 = new VBox();
			vBox2.setPrefWidth(300);
			Label vBox2Label = new Label("Create New Food Item");
			vBox2.getChildren().addAll(new Label("\n"), vBox2Label, new Label("\n"));

			HBox hBox = new HBox();
			TextField addName = new TextField();
			hBox.getChildren().addAll(new Label("Food Name:      "), addName);

			HBox hBox2 = new HBox();
			TextField addCalories = new TextField();
			hBox2.getChildren().addAll(new Label("Calories:       "), addCalories);

			HBox hBox3 = new HBox();
			TextField addFat = new TextField();
			hBox3.getChildren().addAll(new Label("Fat:            "), addFat);

			HBox hBox4 = new HBox();
			TextField addCarbs = new TextField();
			hBox4.getChildren().addAll(new Label("Carbohydrates:  "), addCarbs);

			HBox hBox5 = new HBox();
			TextField addFiber = new TextField();
			hBox5.getChildren().addAll(new Label("Fiber:          "), addFiber);

			HBox hBox6 = new HBox();
			TextField addProtein = new TextField();
			hBox6.getChildren().addAll(new Label("Protein:        "), addProtein);

			HBox hBox7 = new HBox();
			Button newFood = new Button("Create New Food Item");
			newFood.setTooltip(new Tooltip("Create a food item to add to the master list on "
					+ "the left"));
			hBox7.getChildren().add(newFood);

			HBox hBox8 = new HBox();
			Button saveItems = new Button("Save food info to a file");
			saveItems.setTooltip(new Tooltip("Save all food items to a .txt file"));
			hBox8.getChildren().addAll(saveItems, new Label("\n"));

			HBox hBox9 = new HBox();
			Button loadItems = new Button("Load food info from a file");
			loadItems.setTooltip(new Tooltip("Populate the food list on the left with food items"
					+ "from a .txt file"));
			hBox9.getChildren().addAll(loadItems, new Label("\n"));

			vBox2.getChildren().addAll(hBox, hBox2, hBox3, hBox4, hBox5, hBox6, new Label("\n"), 
					hBox7, new Label("\n\n\n\n\n\n\n\n\n"), hBox8, hBox9);

			/*
			 * Creates a new food item based on user input and adds given nutrients
			 */
			newFood.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					FoodItem newFood = null;

					// ensure all fields passed in are non-empty and non-null
					if (addName.getText() == null || addName.getText().isEmpty() 
							|| addCalories.getText() == null
							|| addCalories.getText().isEmpty() || addFat.getText() == null 
							|| addFat.getText().isEmpty() || addCarbs.getText() == null 
							|| addCarbs.getText().isEmpty() || addFiber.getText() == null
							|| addFiber.getText().isEmpty() || addProtein.getText() == null
							|| addProtein.getText().isEmpty()) {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle(null);
						alert.setHeaderText(null);
						alert.setContentText("Nutritional information required for each field");

						alert.showAndWait();
					} else {
						/*
						 * Generate a random sequence of 24 digits to be the ID of the FoodItem.
						 * This ensures that each FoodItem is unique.
						 */
						char[] ch = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 
								'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 
								'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

						char[] randSequence = new char[24];
						Random random = new Random();
						for (int i = 0; i < 24; i++) {
							randSequence[i] = ch[random.nextInt(ch.length)];
						}

						/*
						 * Ensure that all user input for the nutrition fields is numeric
						 * and non-negative.
						 */
						try {
							// checks that all nutrient values are greater than zero
							if(Double.parseDouble(addCalories.getText()) < 0 || 
									Double.parseDouble(addFat.getText()) < 0 ||
									 Double.parseDouble(addCarbs.getText()) < 0 ||
									 Double.parseDouble(addFiber.getText()) < 0 ||
									 Double.parseDouble(addProtein.getText()) < 0) {
								throw new Exception();
							}
							
							// since all fields are valid, create a new FoodItem and add to list
							newFood = new FoodItem(String.valueOf(randSequence), addName.getText());
							food.add(newFood);
							foodDataList.addFoodItem(newFood);
							food.sort((food1, food2) -> food1.getName().toLowerCase()
									.compareTo(food2.getName().toLowerCase()));
							
							checkBoxes.getChildren().clear();
							addToMeal.clear();
							for (int i = 0; i < food.size(); i++) {
								CheckBox c = new CheckBox(food.get(i).getName());
								checkBoxes.getChildren().add(c);
								addToMeal.add(c);
							}
							
							// adds each nutrient to the new food item
							newFood.addNutrient("calories", Double.parseDouble(addCalories.getText()));
							newFood.addNutrient("fat", Double.parseDouble(addFat.getText()));
							newFood.addNutrient("carbohydrate", Double.parseDouble(addCarbs.getText()));
							newFood.addNutrient("fiber", Double.parseDouble(addFiber.getText()));
							newFood.addNutrient("protein", Double.parseDouble(addProtein.getText()));

							foodList.getChildren().clear();
							totalItems = checkBoxes.getChildren().size();
							options = new Label("Food Options (" + totalItems + " total)");
							scroll.setContent(checkBoxes);
							foodList.getChildren().addAll(new Label("\n"), options, 
									new Label("\n"), scroll, new Label("\n"), addFood, 
									queryRules, nameSearch);
							foodList.setPrefWidth(300);
						} catch (Exception m) {
							// alerts the user to enter positive numbers for nutrient values
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle(null);
							alert.setHeaderText(null);
							alert.setContentText("Nutritional values must be positive numbers");
							alert.showAndWait();
						}
					}
				}
			});

			/*
			 * Creates help button in top right corner of the screen. When button is clicked, a screen
			 * pops up to explain the application to the user.
			 */
			Button help = new Button("Help");
			HBox hbox11 = new HBox(help);
			//Display help screen to the user when button is clicked
			help.setOnAction(e -> {
				final Stage dialog = new Stage();
				dialog.initModality(Modality.APPLICATION_MODAL);
				dialog.initOwner(primaryStage);
				VBox dialogVbox = new VBox();
				Label l = new Label();
				l.setText("Welcome to Food Query!\n\n"
						+ "In Food Query, you can create your own meals and analyze "
						+ "the nutritional value of them.\nTo get started, select your "
						+ "desired foods from the 'Food Options' window. To add the food "
						+ "items to your meal, click the 'Add to current meal' button. These "
						+ "items should now show up under the 'Food Items' window.\n"
						+ "You can click the 'Filter by Nutrition' button to show a pop up that"
						+ " will allow you to filter the food list by nutritional values.\n"
						+ "You can also search for a specific food by name if you click the "
						+ "'Search' button.\nTo analyze the nutritional content of your current "
						+ "meal, hit the 'Analyze Meal' button.\nYou can also remove any/all food "
						+ "items from your meal by hitting the 'Remove items' button.\nTo create "
						+ "your own food item, type the name and nutritional information of the food"
						+ "under the 'Create New Food Item' section, and hit 'Create New Food Item' to"
						+ " add it to the food list. \nLastly, you can save and load the food list to a .txt"
						+ " file by clicking on the 'Save food info to a file' and 'Load food into a "
						+ "file' buttons followed by entering the file name.\n\nHover your mouse over "
						+ "any button for a brief description of what it does.");
				dialogVbox.getChildren().add(l);
				l.setWrapText(true);
				Scene dialogScene = new Scene(dialogVbox, 700, 400);
				
				dialog.setScene(dialogScene);

				dialog.show();
			});
			
			/*
			 * Saves current meal to the file Meals.txt
			 */
			saveItems.setOnAction(e -> {
				// creates a pop up asking for the user to input a name for their new file
				final Stage dialog = new Stage();
				dialog.initModality(Modality.APPLICATION_MODAL);
				dialog.initOwner(primaryStage);
				
				// adds text field and button to dialog box
				VBox dialogVbox = new VBox();
				TextField fileName = new TextField();
				Button submit = new Button("Submit");
				dialogVbox.getChildren().addAll(new Label("Enter file name: "), fileName, submit);

				// when user clicks the button, their input is used to create a new file
				// with the current meal
				submit.setOnAction(f -> {
					String saveFileName = fileName.getText();
					boolean loop = false;
					
					// if the user does not enter any text into the field, they are alerted
					if (fileName.getText().isEmpty() || fileName.getText() == null
							|| fileName.getText().trim().equals("")) {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle(null);
						alert.setHeaderText(null);
						alert.setContentText("No file name detected.");
						alert.showAndWait();
						loop = false;
					} else if (saveFileName.contains(".txt")) {
						// adds ".txt" to the end of the file name if the user didn't
						foodDataList.saveFoodItems(saveFileName);
						loop = true;
					} else {
						foodDataList.saveFoodItems(saveFileName + ".txt");
						loop = true;
					}
					
					// tells the user their file was saved successfully to a specific file
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle(null);
					alert.setHeaderText(null);
					if (saveFileName.contains(".txt") && loop == true) {
						alert.setContentText("Meal saved successfully to '" + saveFileName + "'.");
						alert.showAndWait();
						dialog.hide();
					} else if (!saveFileName.contains(".txt") && loop == true) {
						alert.setContentText("Meal saved successfully to '" + saveFileName + ".txt'.");
						alert.showAndWait();
						dialog.hide();
					}
				});

				// shows the dialog box on the screen
				Scene dialogScene = new Scene(dialogVbox, 300, 200);
				dialog.setScene(dialogScene);

				dialog.show();

			});

			/*
			 * Loads current meal to the file Meals.txt
			 */
			loadItems.setOnAction(e -> {
				// a pop up window asks the user to enter the name of the file they want ot load
				final Stage dialog = new Stage();
				dialog.setMinWidth(375);
				dialog.initModality(Modality.APPLICATION_MODAL);
				dialog.initOwner(primaryStage);
				
				// adds a text field and button to the dialog box
				VBox dialogVbox = new VBox();
				TextField fileName = new TextField();
				Button submit = new Button("Submit");
				dialogVbox.getChildren().addAll(new Label("Enter file name: (must be a txt. file "
						+ "in project folder"), fileName, submit);

				// when the user clicks the button, the new file will be loaded
				submit.setOnAction(f -> {
					try {
						String loadFile = fileName.getText();

						// checks if the file exist before attempting to load it
						if (!loadFile.contains(".txt"))
							loadFile += ".txt";
						File file = new File(loadFile);
						
						// if the file does not exist, an exception is thrown 
						if (!file.exists())
							throw new Exception();
						else
							foodData.loadFoodItems(loadFile);
	
						foodData.loadFoodItems(loadFile);

						// alphabetizes list
						food.sort((food1, food2) -> food1.getName().toLowerCase().compareTo(food2.getName()
								.toLowerCase()));
						
						checkBoxes.getChildren().clear();

						// adds a check box to each food item in the list
						for (int i = 0; i < food.size(); i++) {
							CheckBox c = new CheckBox(food.get(i).getName());
							checkBoxes.getChildren().add(c);
							addToMeal.add(c);
						}

						totalItems = checkBoxes.getChildren().size();
						options = new Label("Food Options (" + totalItems + " total)");
						scroll.setContent(checkBoxes);
						foodList.getChildren().clear();
						foodList.getChildren().addAll(new Label("\n"), options, 
								new Label("\n"), scroll, new Label("\n"), addFood, 
								queryRules, nameSearch);
						foodList.setPrefWidth(300);
						dialog.close();
						
						// tells the user the new meal was successfully loaded
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle(null);
						alert.setHeaderText(null);
						alert.setContentText("Meal loaded successfully from '" + loadFile + "'.");
						alert.showAndWait();
						dialog.hide();
					} catch (Exception ex) {
						// alerts the user that the specified file does not exist
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle(null);
						alert.setHeaderText(null);
						alert.setContentText("File not found.");
						alert.showAndWait();
					}
				});

				// shows the diaglog box
				Scene dialogScene = new Scene(dialogVbox, 300, 200);
				dialog.setScene(dialogScene);

				dialog.show();
			});

			/*
			 * Sets parts of the border pane so that the main screen looks nice visually
			 */
			root1.setRight(vBox2);
			root1.setLeft(foodList);
			root1.setCenter(totals);
			root1.setTop(hbox11);

			/*
			 * Sets the style of the main screen
			 */
			scene1.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene1);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}