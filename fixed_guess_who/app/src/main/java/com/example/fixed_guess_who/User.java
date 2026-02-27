package com.example.fixed_guess_who;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    public String username;
    public String email;
    public int money;
    public int winStreak = 0;
    public List<String> categories;
    public Map<String, Integer> powerups;
    public boolean musicOn = true;
    public boolean sfxOn = true;
    public boolean isOnline = false;

    public User() {}

    public User(String username, int money, String initialCategory) {
        this.username = username;
        this.money = money;
        this.musicOn = true;
        this.sfxOn = true;
        this.winStreak = 0;
        this.isOnline = true;

        this.categories = new ArrayList<>();
        this.categories.add(initialCategory);
        
        this.powerups = new HashMap<>();
        this.powerups.put("hint", 0);
        this.powerups.put("peek", 0);
    }
}