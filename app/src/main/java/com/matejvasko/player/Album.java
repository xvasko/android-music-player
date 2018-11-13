package com.matejvasko.player;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class Album extends ExpandableGroup<Song> {

    public long id;
    public String title;
    public String artist;

    public List<Song> songs;

    public Album(long id, String title, List<Song> items, String artist) {
        super(title, items);
        this.id = id;
        this.title = title;
        this.songs = items;
        this.artist = artist;
    }



}
