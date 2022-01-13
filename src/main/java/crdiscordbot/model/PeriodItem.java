package crdiscordbot.model;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class PeriodItem {

    @SerializedName("clan")
    private PeriodClanWithTag clan;

    @SerializedName("pointsEarned")
    private Integer pointsEarned;

    @SerializedName("progressStartOfDay")
    private Integer progressStartOfDay;

    @SerializedName("progressEndOfDay")
    private Integer progressEndOfDay;

    @SerializedName("endOfDayRank")
    private Integer endOfDayRank;

    @SerializedName("progressEarned")
    private Integer progressEarned;

    @SerializedName("numOfDefensesRemaining")
    private Integer numOfDefensesRemaining;

    @SerializedName("progressEarnedFromDefenses")
    private Integer progressEarnedFromDefenses;

    public PeriodClanWithTag getClan() {
        return clan;
    }

    public void setClan(PeriodClanWithTag clan) {
        this.clan = clan;
    }

    public Integer getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public Integer getProgressStartOfDay() {
        return progressStartOfDay;
    }

    public void setProgressStartOfDay(Integer progressStartOfDay) {
        this.progressStartOfDay = progressStartOfDay;
    }

    public Integer getProgressEndOfDay() {
        return progressEndOfDay;
    }

    public void setProgressEndOfDay(Integer progressEndOfDay) {
        this.progressEndOfDay = progressEndOfDay;
    }

    public Integer getEndOfDayRank() {
        return endOfDayRank;
    }

    public void setEndOfDayRank(Integer endOfDayRank) {
        this.endOfDayRank = endOfDayRank;
    }

    public Integer getProgressEarned() {
        return progressEarned;
    }

    public void setProgressEarned(Integer progressEarned) {
        this.progressEarned = progressEarned;
    }

    public Integer getNumOfDefensesRemaining() {
        return numOfDefensesRemaining;
    }

    public void setNumOfDefensesRemaining(Integer numOfDefensesRemaining) {
        this.numOfDefensesRemaining = numOfDefensesRemaining;
    }

    public Integer getProgressEarnedFromDefenses() {
        return progressEarnedFromDefenses;
    }

    public void setProgressEarnedFromDefenses(Integer progressEarnedFromDefenses) {
        this.progressEarnedFromDefenses = progressEarnedFromDefenses;
    }

    @Override
    public String toString() {
        return "PeriodItem{" +
                "clan='" + clan + '\'' +
                ", pointsEarned=" + pointsEarned +
                ", progressStartOfDay=" + progressStartOfDay +
                ", progressEndOfDay=" + progressEndOfDay +
                ", endOfDayRank=" + endOfDayRank +
                ", progressEarned=" + progressEarned +
                ", numOfDefensesRemaining=" + numOfDefensesRemaining +
                ", progressEarnedFromDefenses=" + progressEarnedFromDefenses +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeriodItem that = (PeriodItem) o;
        return clan.equals(that.clan) && Objects.equals(pointsEarned, that.pointsEarned) && Objects.equals(progressStartOfDay, that.progressStartOfDay) && Objects.equals(progressEndOfDay, that.progressEndOfDay) && Objects.equals(endOfDayRank, that.endOfDayRank) && Objects.equals(progressEarned, that.progressEarned) && Objects.equals(numOfDefensesRemaining, that.numOfDefensesRemaining) && Objects.equals(progressEarnedFromDefenses, that.progressEarnedFromDefenses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clan, pointsEarned, progressStartOfDay, progressEndOfDay, endOfDayRank, progressEarned, numOfDefensesRemaining, progressEarnedFromDefenses);
    }

}
