package ch.unibas.dmi.dbis.cs108.game;

import java.util.*;

public class Field {
    String fieldId;
    String color;
    Set<Field> neighbors;

    public Field(String fieldId, String color) {
        this.fieldId = fieldId;
        this.color = color;
        this.neighbors = new HashSet<>();
    }

    public void addNeighbor(Field neighbor) {
        this.neighbors.add(neighbor);
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getColor() {
        return color;
    }

    public Set<Field> getNeighbors() {
        return neighbors;
    }
}
