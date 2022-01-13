package crdiscordbot.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class CurrentRiverRace {

    @SerializedName("state")
    private String state;

    @SerializedName("clan")
    private RiverClan clan;

    @SerializedName("clans")
    private List<RiverClan> clans;

    @SerializedName("collectionEndTime")
    private String collectionEndTime;

    @SerializedName("warEndTime")
    private String warEndTime;

    @SerializedName("sectionIndex")
    private Integer sectionIndex;

    @SerializedName("periodIndex")
    private Integer periodIndex;

    @SerializedName("periodType")
    private String periodType;

    @SerializedName("periodLogs")
    private List<PeriodLog> periodLogs;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public RiverClan getClan() {
        return clan;
    }

    public void setClan(RiverClan clan) {
        this.clan = clan;
    }

    public List<RiverClan> getClans() {
        return clans;
    }

    public void setClans(List<RiverClan> clans) {
        this.clans = clans;
    }

    public String getCollectionEndTime() {
        return collectionEndTime;
    }

    public void setCollectionEndTime(String collectionEndTime) {
        this.collectionEndTime = collectionEndTime;
    }

    public String getWarEndTime() {
        return warEndTime;
    }

    public void setWarEndTime(String warEndTime) {
        this.warEndTime = warEndTime;
    }

    public Integer getSectionIndex() {
        return sectionIndex;
    }

    public void setSectionIndex(Integer sectionIndex) {
        this.sectionIndex = sectionIndex;
    }

    public Integer getPeriodIndex() {
        return periodIndex;
    }

    public void setPeriodIndex(Integer periodIndex) {
        this.periodIndex = periodIndex;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public List<PeriodLog> getPeriodLogs() {
        return periodLogs;
    }

    public void setPeriodLogs(List<PeriodLog> periodLogs) {
        this.periodLogs = periodLogs;
    }

    @Override
    public String toString() {
        return "CurrentRiverRace{" +
                "state='" + state + '\'' +
                ", clan=" + clan +
                ", clans=" + clans +
                ", collectionEndTime='" + collectionEndTime + '\'' +
                ", warEndTime='" + warEndTime + '\'' +
                ", sectionIndex=" + sectionIndex +
                ", periodIndex=" + periodIndex +
                ", periodType='" + periodType + '\'' +
                ", periodLogs=" + periodLogs +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrentRiverRace that = (CurrentRiverRace) o;
        return Objects.equals(state, that.state) && clan.equals(that.clan) && Objects.equals(clans, that.clans) && Objects.equals(collectionEndTime, that.collectionEndTime) && Objects.equals(warEndTime, that.warEndTime) && Objects.equals(sectionIndex, that.sectionIndex) && Objects.equals(periodIndex, that.periodIndex) && Objects.equals(periodType, that.periodType) && Objects.equals(periodLogs, that.periodLogs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, clan, clans, collectionEndTime, warEndTime, sectionIndex, periodIndex, periodType, periodLogs);
    }
}
