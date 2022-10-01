package com.white.black.nonogram;

import android.graphics.Point;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gal on 31/07/2017.
 */

public enum TouchMonitor {

    INSTANCE;

    private final Map<Integer, TouchTracker> touchTrackerMap = new ConcurrentHashMap<>();

    private TouchTracker safeTouchTrackerMapGetter(int id) {
        TouchTracker touchTracker = touchTrackerMap.get(id);

        if (touchTracker == null) {
            touchTracker = new TouchTracker();
            touchTrackerMap.put(id, touchTracker);
        }

        return touchTracker;
    }

    public void removeTouchTrackerById(int touchEventId) {
        touchTrackerMap.remove(touchEventId);
    }

    private class TouchTracker {
        private Point downCoordinates = new Point(0, 0);
        private Point upCoordinates = new Point(0,0);
        private Point moveCoordinates = new Point(0,0);
        private Point coordinatesGap = new Point(0,0);

        private boolean down = false;
        private boolean up = false;

        private long downTime;
        private long upTime;
        private double distanceBetweenDownAndUp = 0;
    }

    public boolean touchDown() {
        return touchDown(0);
    }

    public boolean touchDown(int id) {
        return safeTouchTrackerMapGetter(id).down;
    }

    public double getDistanceBetweenDownAndUp() {
        return getDistanceBetweenDownAndUp(0);
    }

    private double getDistanceBetweenDownAndUp(int id) {
        return safeTouchTrackerMapGetter(id).distanceBetweenDownAndUp;
    }

    public boolean touchUp() {
        return touchUp(0);
    }

    public boolean touchUp(int id) {
        return safeTouchTrackerMapGetter(id).up;
    }

    public void setTouchDown(boolean b) {
        setTouchDown(0, b);
    }

    public void setTouchDown(int id, boolean b) {
        safeTouchTrackerMapGetter(id).down = b;
    }

    public void setTouchDown(boolean b, Point coordinates) {
        setTouchDown(0, b, coordinates);
    }

    public void setTouchDown(int id, boolean b, Point coordinates) {
        setTouchDown(id, b);
        safeTouchTrackerMapGetter(id).downTime = System.currentTimeMillis();
        safeTouchTrackerMapGetter(id).downCoordinates = coordinates;
    }

    public void setTouchUp(boolean b) {
        setTouchUp(0, b);
    }

    public void setTouchUp(int id, boolean b) {
        safeTouchTrackerMapGetter(id).up = b;

        if (getUpCoordinates(id) != null && getDownCoordinates(id) != null) {
            int xd = getUpCoordinates(id).x - getDownCoordinates(id).x;
            int yd = getUpCoordinates(id).y - getDownCoordinates(id).y;
            safeTouchTrackerMapGetter(id).distanceBetweenDownAndUp = Math.sqrt(xd * xd + yd * yd);
        }
    }

    public void setTouchUp(Point coordinates) {
        setTouchUp(0, coordinates);
    }

    public void setTouchUp(int id, Point coordinates) {
        safeTouchTrackerMapGetter(id).upTime = System.currentTimeMillis();
        safeTouchTrackerMapGetter(id).upCoordinates = coordinates;
        setTouchUp(id, true);
    }

    public void setCoordinatesGap() {
        setCoordinatesGap(0);
    }

    public void setCoordinatesGap(int id) {
        if (safeTouchTrackerMapGetter(id).upCoordinates != null && safeTouchTrackerMapGetter(id).downCoordinates != null) {
            safeTouchTrackerMapGetter(id).coordinatesGap = new Point(
                    safeTouchTrackerMapGetter(id).upCoordinates.x - safeTouchTrackerMapGetter(id).downCoordinates.x,
                    safeTouchTrackerMapGetter(id).upCoordinates.y - safeTouchTrackerMapGetter(id).downCoordinates.y
            );
        }
    }

    public long getDownTime() { return getDownTime(0); }

    private long getDownTime(int id) { return safeTouchTrackerMapGetter(id).downTime; }

    public long getUpTime() { return getUpTime(0); }

    private long getUpTime(int id) { return safeTouchTrackerMapGetter(id).upTime; }

    public Point getCoordinatesGap() {
        return getCoordinatesGap(0);
    }

    private Point getCoordinatesGap(int id) {
        return safeTouchTrackerMapGetter(id).coordinatesGap;
    }

    public void initializeCoordinatesGap() {
        initializeCoordinatesGap(0);
    }

    private void initializeCoordinatesGap(int id) {
        safeTouchTrackerMapGetter(id).coordinatesGap = new Point(0,0);
    }

    public Point getDownCoordinates() {
        return getDownCoordinates(0);
    }

    public Point getDownCoordinates(int id) {
        return safeTouchTrackerMapGetter(id).downCoordinates;
    }

    public Point getUpCoordinates() {
        return getUpCoordinates(0);
    }

    public Point getUpCoordinates(int id) {
        return safeTouchTrackerMapGetter(id).upCoordinates;
    }

    public Point getMove() {
        return getMove(0);
    }

    public Point getMove(int id) {
        return safeTouchTrackerMapGetter(id).moveCoordinates;
    }

    public void setMove(Point coordinates) {
        setMove(0, coordinates);
    }

    public void setMove(int id, Point coordinates) {
        safeTouchTrackerMapGetter(id).moveCoordinates = coordinates;
    }
}
