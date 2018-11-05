package com.matejvasko.player;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class Album extends ExpandableGroup<Song> {

    public int id;
    public String title;

    public Album(int id, String title, List<Song> items) {
        super(title, items);
        this.id = id;
        this.title = title;
    }

}
