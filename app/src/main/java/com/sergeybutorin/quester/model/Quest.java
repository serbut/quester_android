package com.sergeybutorin.quester.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.UUID;

public class Quest extends QuestBase implements Serializable {
    private int id;
    private String title = "Очень классный маршрут";
    private String description = "Это очень классный маршрут! Советую посетить всем!";
    private LinkedList<Point> points = new LinkedList<>();

    public Quest() {
    }

    public Quest(UUID uuid) {
        super(uuid, 0);
    }

    public Quest(int id,
                 UUID uuid,
                 int version,
                 String title,
                 String description) {
        super(uuid, version);
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public Quest(UUID uuid,
                 String title,
                 String description,
                 LinkedList<Point> points) {
        super(uuid);
        this.title = title;
        this.description = description;
        this.points = points;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public void setPoints(LinkedList<Point> points) {
        this.points = points;
    }

    public LinkedList<Point> getPoints() {
        return points;
    }

    public void clear() {
        points.clear();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
