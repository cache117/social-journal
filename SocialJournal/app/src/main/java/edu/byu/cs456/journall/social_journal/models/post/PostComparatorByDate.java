package edu.byu.cs456.journall.social_journal.models.post;

import java.util.Comparator;

/**
 * A simple {@link Comparator} for a {@link Post}. This sorts them by date in reverse order.
 */

public class PostComparatorByDate implements Comparator<Post> {
    @Override
    public int compare(Post first, Post second) {
        return -1 * first.date.compareTo(second.date);
    }
}
