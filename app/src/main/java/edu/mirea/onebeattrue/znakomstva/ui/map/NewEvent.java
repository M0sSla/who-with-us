package edu.mirea.onebeattrue.znakomstva.ui.map;

import java.util.ArrayList;

public class NewEvent {
    private String eventId;
    private String eventUser;
    private String eventName;
    private String eventDescription;
    private String eventTime;
    private String eventDate;
    private String eventPlace;
    private String eventCategory;
    private boolean editMode;
    private int eventNumberOfVisitors;
    private ArrayList<String> eventVisitors;

    NewEvent() {}

    public NewEvent(String eventName, String eventDescription, String eventTime, String eventDate, String eventPlace) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventTime = eventTime;
        this.eventDate = eventDate;
        this.eventPlace = eventPlace;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventUser() {
        return eventUser;
    }

    public void setEventUser(String eventUser) {
        this.eventUser = eventUser;
    }

    public String getEventPlace() {
        return eventPlace;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public void setEventPlace(String eventPlace) {
        this.eventPlace = eventPlace;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public int getEventNumberOfVisitors() {
        return eventNumberOfVisitors;
    }

    public void setEventNumberOfVisitors(int eventNumberOfVisitors) {
        this.eventNumberOfVisitors = eventNumberOfVisitors;
    }

    public void addVisitor(String visitor) {
        if (!this.eventVisitors.contains(visitor)) {
            this.eventNumberOfVisitors++;
            this.eventVisitors.add(visitor);
        }
    }

    public void removeVisitor(String visitor) {
        if (this.eventVisitors.contains(visitor)) {
            this.eventNumberOfVisitors--;
            this.eventVisitors.remove(visitor);
        }
    }

    public ArrayList<String> getEventVisitors() {
        return eventVisitors;
    }

    public void setEventVisitors(ArrayList<String> eventVisitors) {
        this.eventVisitors = eventVisitors;
    }
}
