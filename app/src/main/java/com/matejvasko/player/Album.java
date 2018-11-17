package com.matejvasko.player;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class Album extends ExpandableGroup<Song> {

    public long id;
    public String title;
    public String artist;

    public Album(long id, String title, List<Song> songs, String artist) {
        super(title, songs);
        this.id = id;
        this.title = title;
        this.artist = artist;
    }

}
