package ch.unibas.dmi.dbis.cs108.game;

import java.util.HashSet;
import java.util.Set;

/**
 * This class specifies the fields of our game with the fieldId the color (-> part of the fieldId)
 * and the neighbors of each field.
 *
 * @author Jana
 */
public class Field {

    /**
     * The ID of the field as a string.
     */
    String fieldId;

    /**
     * The color of the field.
     */
    String color;

    /**
     * The neighbouring fields of the field.
     */
    Set<Field> neighbors;

    /**
     * Constructor of the class Field
     *
     * @param fieldId the id of the field
     * @param color   the color of the field
     */
    public Field(String fieldId, String color) {
        this.fieldId = fieldId;
        this.color = color;
        this.neighbors = new HashSet<>();
    }

    /**
     * Method to add a neighbor to the HashSet of neighbors of field.
     *
     * @param neighbor neighbor of the field
     */
    public void addNeighbor(Field neighbor) {
        if (neighbor != null) {
            this.neighbors.add(neighbor);
        }
    }

    /**
     * Method to get the identification of a field
     *
     * @return the fieldId
     */
    public String getFieldId() {
        return fieldId;
    }

    /**
     * Method to get color of a field
     *
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * Method to get the neighbors of a field
     *
     * @return the Set with all neighbors of a field
     */
    public Set<Field> getNeighbors() {
        return neighbors;
    }
}
