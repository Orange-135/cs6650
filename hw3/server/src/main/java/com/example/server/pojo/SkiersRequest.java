package com.example.server.pojo;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SkiersRequest {
    @NotNull(message = "skierID is required")
    @Min(value = 1, message = "skierID must be at least 1")
    @Max(value = 100000, message = "skierID must be no more than 100000")
    private Integer skierID;

    @NotNull(message = "resortID is required")
    @Min(value = 1, message = "resortID must be at least 1")
    @Max(value = 10, message = "resortID must be no more than 10")
    private Integer resortID;

    @NotNull(message = "liftID is required")
    @Min(value = 1, message = "liftID must be at least 1")
    @Max(value = 40, message = "liftID must be no more than 40")
    private Integer liftID;

    @NotNull(message = "seasonID is required")
    @Min(value = 2024, message = "seasonID must be 2024")
    @Max(value = 2024, message = "seasonID must be 2024")
    private Integer seasonID;

    @NotNull(message = "dayID is required")
    @Min(value = 1, message = "dayID must be 1")
    @Max(value = 1, message = "dayID must be 1")
    private Integer dayID;

    @NotNull(message = "time is required")
    @Min(value = 1, message = "time must be at least 1")
    @Max(value = 360, message = "time must be no more than 360")
    private Integer time;

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
}
