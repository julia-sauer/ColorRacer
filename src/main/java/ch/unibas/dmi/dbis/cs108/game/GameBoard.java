package ch.unibas.dmi.dbis.cs108.game;

import java.util.*;

public class GameBoard {
    private Map<String, Field> fields;
    private Field currentField;
    private Set<Field> selectedField;

    public GameBoard() {
        this.fields = new HashMap<>();
        this.selectedField = new HashSet<>();

        initializeFields();
        initializeNeighbors();
    }

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

    private void initializeNeighbors() {
        //TODO
    }
}
