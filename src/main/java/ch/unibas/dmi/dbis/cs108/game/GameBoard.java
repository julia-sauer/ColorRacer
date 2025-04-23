package ch.unibas.dmi.dbis.cs108.game;

import java.util.*;

import static ch.unibas.dmi.dbis.cs108.server.Server.colors;

/**
 * This class specifies the GameBoard with its fields. Every player has its own object of this
 * class.
 *
 * @author Jana
 */
public class GameBoard {

  private Map<String, Field> fields;
  private Field currentField;
  private Set<Field> selectedFields;
  private final List<Field> selectedFieldList = new ArrayList<>();

  /**
   * Constructor of the class GameBoard. Defines the Map over the fields. Defines the Set over the
   * selectedFields.
   */
  public GameBoard() {
    this.fields = new HashMap<>();
    this.selectedFields = new HashSet<>();

    initializeFields();
    initializeNeighbors();
  }

  /**
   * This method initializes every field with its fieldId. Sets the current field for the start.
   */
  private void initializeFields() {
    String[] colors = {"white", "purple", "yellow", "orange", "blue", "pink", "red"};
    int[] fieldCounts = {1, 10, 7, 10, 10, 10, 7};

    for (int i = 0; i < colors.length; i++) {
      for (int j = 1; j <= fieldCounts[i]; j++) {
        String fieldId = colors[i] + j;
        fields.put(fieldId, new Field(fieldId, colors[i]));
      }
    }
    this.currentField = fields.get("white1");
  }

  /**
   * This method initializes all the neighbors for every single field. By assigning an ArrayList
   * with every neighbor to the single fields.
   */
  private void initializeNeighbors() {
    Map<String, List<String>> neighborMap = new HashMap<>();

    neighborMap.put("white1", Arrays.asList("purple1", "yellow1", "orange1"));
    neighborMap.put("blue1", Arrays.asList("orange1", "yellow1", "purple2", "pink1"));
    neighborMap.put("blue2", Arrays.asList("orange2", "pink2"));
    neighborMap.put("blue3",
        Arrays.asList("pink1", "pink2", "orange3", "orange4", "purple3", "red2"));
    neighborMap.put("blue4",
        Arrays.asList("red2", "purple4", "orange5", "purple5", "pink3", "yellow2"));
    neighborMap.put("blue5", Arrays.asList("pink4", "yellow3", "blue6", "purple6"));
    neighborMap.put("blue6", Arrays.asList("blue5", "yellow3", "red4", "purple6", "yellow4"));
    neighborMap.put("blue7", Arrays.asList("orange7", "pink6", "red6", "orange9"));
    neighborMap.put("blue8", Arrays.asList("orange9", "pink8", "yellow5", "purple9"));
    neighborMap.put("blue9", Arrays.asList("purple9", "yellow5", "pink9", "yellow6"));
    neighborMap.put("blue10", Arrays.asList("purple10", "orange10", "pink10"));
    neighborMap.put("red1", Arrays.asList("purple1", "yellow1", "purple2", "orange2"));
    neighborMap.put("red2", Arrays.asList("orange4", "blue3", "purple3", "purple4", "blue4"));
    neighborMap.put("red3", Arrays.asList("purple5", "orange5", "pink4", "pink5", "yellow3"));
    neighborMap.put("red4", Arrays.asList("orange6", "yellow3", "blue6"));
    neighborMap.put("red5", Arrays.asList("yellow4", "orange7", "purple7"));
    neighborMap.put("red6",
        Arrays.asList("pink7", "orange8", "pink6", "blue7", "orange9", "pink8"));
    neighborMap.put("red7", Arrays.asList("purple9", "yellow7", "orange10"));
    neighborMap.put("orange1", Arrays.asList("yellow1", "blue1"));
    neighborMap.put("orange2", Arrays.asList("red1", "purple2", "blue2"));
    neighborMap.put("orange3", Arrays.asList("pink2", "blue3", "purple3"));
    neighborMap.put("orange4", Arrays.asList("pink1", "blue3", "red2"));
    neighborMap.put("orange5", Arrays.asList("blue4", "purple4", "pink4", "red3", "purple5"));
    neighborMap.put("orange6", Arrays.asList("pink5", "yellow3", "red4"));
    neighborMap.put("orange7", Arrays.asList("yellow4", "red5", "purple7", "pink6", "blue7"));
    neighborMap.put("orange8", Arrays.asList("pink7", "red6"));
    neighborMap.put("orange9", Arrays.asList("blue7", "red6", "pink8", "blue8"));
    neighborMap.put("orange10", Arrays.asList("purple10", "yellow7", "red7", "pink10", "blue10"));
    neighborMap.put("purple1", Arrays.asList("yellow1", "red1"));
    neighborMap.put("purple2", Arrays.asList("yellow1", "blue1", "pink1", "red1", "orange2"));
    neighborMap.put("purple3", Arrays.asList("orange3", "blue3", "red2"));
    neighborMap.put("purple4", Arrays.asList("red2", "blue4", "orange5", "pink4"));
    neighborMap.put("purple5", Arrays.asList("pink3", "blue4", "orange5", "red3", "pink5"));
    neighborMap.put("purple6", Arrays.asList("blue5", "blue6", "yellow4"));
    neighborMap.put("purple7", Arrays.asList("red5", "orange7", "pink6", "pink7"));
    neighborMap.put("purple8", Arrays.asList("pink8", "yellow5", "pink9"));
    neighborMap.put("purple9", Arrays.asList("blue8", "yellow5", "blue9", "red7"));
    neighborMap.put("purple10", Arrays.asList("yellow6", "orange10", "blue10"));
    neighborMap.put("yellow1", Arrays.asList("orange1", "purple1", "blue1", "red1", "purple2"));
    neighborMap.put("yellow2", Arrays.asList("blue4", "pink3"));
    neighborMap.put("yellow3",
        Arrays.asList("blue6", "red4", "blue5", "orange6", "pink4", "red3", "pink5"));
    neighborMap.put("yellow4", Arrays.asList("orange7", "red5", "purple6", "blue6"));
    neighborMap.put("yellow5",
        Arrays.asList("pink8", "purple8", "pink9", "blue9", "purple9", "blue8"));
    neighborMap.put("yellow6", Arrays.asList("pink9", "blue9", "purple10"));
    neighborMap.put("yellow7", Arrays.asList("red7", "orange10", "pink10"));
    neighborMap.put("pink1", Arrays.asList("blue1", "purple2", "blue3", "orange4"));
    neighborMap.put("pink2", Arrays.asList("blue2", "blue3", "orange3"));
    neighborMap.put("pink3", Arrays.asList("yellow2", "blue4", "purple5"));
    neighborMap.put("pink4", Arrays.asList("purple4", "orange5", "red3", "yellow3", "blue5"));
    neighborMap.put("pink5", Arrays.asList("purple5", "red3", "yellow3", "orange6"));
    neighborMap.put("pink6", Arrays.asList("purple7", "orange7", "blue7", "red6"));
    neighborMap.put("pink7", Arrays.asList("purple7", "red6", "orange8"));
    neighborMap.put("pink8", Arrays.asList("purple8", "blue8", "red6", "orange9", "yellow5"));
    neighborMap.put("pink9", Arrays.asList("purple8", "yellow5", "blue9", "yellow6"));
    neighborMap.put("pink10", Arrays.asList("yellow7", "orange10", "blue10"));

    for (Map.Entry<String, List<String>> entry : neighborMap.entrySet()) {
      Field field = fields.get(entry.getKey());
      for (String neighborId : entry.getValue()) {
        Field neighbor = fields.get(neighborId);
        if (neighbor != null) {
          field.addNeighbor(neighbor);
        }
      }
    }
  }

  /**
   * This method checks whether the field chosen by the user is valid or not. It checks if the field
   * is connected to the field the player is currently on or if it is connected to an already
   * selected field. It checks if the filed corresponds to a color that was rolled and is not
   * already used.
   *
   * @param fieldId the id of the chosen field.
   * @return true if field is valid, false if field is not valid
   */
  public boolean isValidField(String fieldId) {
    Field targetField = fields.get(fieldId);
    String fieldColor = fieldId.split("\\d")[0];

    boolean isNeighbor = currentField.getNeighbors().contains(targetField);

    boolean isConnectedNeighbor = false;
    for (Field selectedField : selectedFields) {
      if (selectedField.getNeighbors().contains(targetField)) {
        isConnectedNeighbor = true;
        break;
      }
    }
    if (isNeighbor || isConnectedNeighbor) {
      boolean colormatches = false;
      for (int i = 0; i < colors.length; i++) {
        if (fieldColor.equals(colors[i])) {
          colors[i] = null;
          colormatches = true;
          break;
        }
      }
      if (!colormatches) {
        return false;
      } else {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the correct Field using the fieldIds.
   *
   * @param fieldId fieldId
   * @return the field corresponding to the fieldId
   */
  public Field getFieldById(String fieldId) {
    return fields.get(fieldId);
  }

  /**
   * Takes a chosen field out of the Set of selectedFields.
   *
   * @param field the chosen field
   */
  public void removeSelectedField(Field field) {
    selectedFields.remove(field);
    selectedFieldList.remove(field); // for MOVE
  }

  /**
   * Adds a chosen field to the Set of selectedFields.
   *
   * @param field the chosen field
   */
  public void addSelectedField(Field field) {
    selectedFields.add(field);
    selectedFieldList.add(field); // for MOVE
  }

  /**
   * checks if given field is in the HashSet selectedFields
   *
   * @param field the field searched for
   * @return true or false (depending on whether the field is in selectedFields or not)
   */
  public boolean inSelectedField(Field field) {
    return selectedFields.contains(field);
  }

  /**
   * Returns the last field that was selected via CHOS. This is used when the player confirms their
   * move with MOVE.
   *
   * @return The last selected field in the order of selection, or null if no fields were selected.
   */
  public Field getLastSelectedField() {
    if (selectedFieldList.isEmpty()) {
      return null;
    }
    return selectedFieldList.get(selectedFieldList.size() - 1);
  }

  /**
   * Executes the player's move by setting the current field to the last selected field from CHOS.
   * Clears the selection afterward. Used when the player confirms their movement with MOVE.
   */
  public void moveToLastSelected() {
    Field last = getLastSelectedField();
    if (last != null) {
      currentField = last;
      selectedFields.clear();
      selectedFieldList.clear();
    }
  }

  /**
   * returns the current field, the player is on. Used to report the player's final position after
   * moving.
   *
   * @return the current field.
   */
  public Field getCurrentField() {
    return currentField;
  }

  /**
   * sets the current field to a specific field Used to set the currentField to white1 before
   * restarting the game
   *
   * @param field the field that should be the currentField
   * @return returns the current field
   */
  public Field setCurrentField(Field field) {
    currentField = field;
    return currentField;
  }

  /**
   * checks if the HashSet selectedFields is empty
   *
   * @return true or false
   */
  public boolean selectedFieldsEmpty() {
    if (selectedFieldList.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Goes through the hashSet selectedFields, as soon as it found the deselectedField it is removed.
   * Every field that comes after the deselectedField is removed to and the fieldcolors are put back
   * in the colors array.
   *
   * @param deselectedField the field the player clicked on
   * @return String with the "new" available colors
   */
  public String deselectFields(Field deselectedField) {
    boolean found = false;
    Iterator<Field> iterator = selectedFieldList.iterator();
    while (iterator.hasNext()) {
      Field field = iterator.next();
      if (found || field.equals(deselectedField)) {
        iterator.remove();
        selectedFields.remove(field);
        String fieldColor = field.getFieldId().split("\\d")[0];
        for (int i = 0; i < colors.length; i++) {
          if (colors[i] == null) {
            colors[i] = fieldColor;
            break;
          }
        }
        found = true;
      }
    }

    String newColors = Arrays.toString(colors);
    return newColors;
  }

}