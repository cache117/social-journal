package edu.byu.cs456.journall.social_journal.post;

import java.util.Comparator;

/**
 * Created by cstaheli on 4/15/2017.
 */

public class PostComparatorByDate implements Comparator<Post> {
    @Override
    public int compare(Post first, Post second) {
        return -1 * first.date.compareTo(second.date);
    }
}
