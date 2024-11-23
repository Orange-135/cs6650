package com.example.consumer.pojo;

public class SkiersRequest {
    private Integer skierID;
    private Integer resortID;
    private Integer liftID;
    private Integer seasonID;
    private Integer dayID;
    private Integer time;

    // Default constructor
    public SkiersRequest() {}

    // Parameterized constructor
    public SkiersRequest(Integer skierID, Integer resortID, Integer liftID, Integer seasonID, Integer dayID, Integer time) {
        this.skierID = skierID;
        this.resortID = resortID;
        this.liftID = liftID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.time = time;
    }

    // Getters and Setters
    public Integer getSkierID() {
        return skierID;
    }

    public void setSkierID(Integer skierID) {
        this.skierID = skierID;
    }

    public Integer getResortID() {
        return resortID;
    }

    public void setResortID(Integer resortID) {
        this.resortID = resortID;
    }

    public Integer getLiftID() {
        return liftID;
    }

    public void setLiftID(Integer liftID) {
        this.liftID = liftID;
    }

    public Integer getSeasonID() {
        return seasonID;
    }

    public void setSeasonID(Integer seasonID) {
        this.seasonID = seasonID;
    }

    public Integer getDayID() {
        return dayID;
    }

    public void setDayID(Integer dayID) {
        this.dayID = dayID;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "SkiersRequest{" +
                "skierID=" + skierID +
                ", resortID=" + resortID +
                ", liftID=" + liftID +
                ", seasonID=" + seasonID +
                ", dayID=" + dayID +
                ", time=" + time +
                '}';
    }
}
