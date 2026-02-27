package com.example.fixed_guess_who;

import java.util.HashMap;


public class GameCharacter {

    public String name;
    public int imageResId;
    public HashMap<String, Boolean> attributes = new HashMap<>();
    public GameCharacter(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }
    public void addAttr(String key, boolean value) {
        attributes.put(key, value);
    }
    public boolean hasAttribute(String key) {
        return attributes.getOrDefault(key, false);
    }

}
