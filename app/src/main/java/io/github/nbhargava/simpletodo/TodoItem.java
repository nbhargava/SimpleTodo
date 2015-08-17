package io.github.nbhargava.simpletodo;

/**
 * Created by nikhil on 8/16/15.
 */
public class TodoItem {
    private long id;
    private String item;

    public TodoItem(long id, String item) {
        this.id = id;
        this.item = item;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String newItem) {
        item = newItem;
    }

    public long getId() {
        return id;
    }

    public String toString() {
        return item;
    }
}
