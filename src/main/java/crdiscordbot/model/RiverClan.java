package crdiscordbot.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class RiverClan {

    @SerializedName("name")
    private String name;

    @SerializedName("tag")
    private String tag;

    @SerializedName("finishTime")
    private String finishTime;

    @SerializedName("clanScore")
    private Integer clanScore;

    @SerializedName("badgeId")
    private Integer badgeId;

    @SerializedName("fame")
    private Integer fame;

    @SerializedName("repairPoints")
    private Integer repairPoints;

    @SerializedName("periodPoints")
    private Integer periodPoints;

    @SerializedName("participants")
    private List<RiverParticipant> participants;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public Integer getClanScore() {
        return clanScore;
    }

    public void setClanScore(Integer clanScore) {
        this.clanScore = clanScore;
    }

    public Integer getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Integer badgeId) {
        this.badgeId = badgeId;
    }

    public Integer getFame() {
        return fame;
    }

    public void setFame(Integer fame) {
        this.fame = fame;
    }

    public Integer getRepairPoints() {
        return repairPoints;
    }

    public void setRepairPoints(Integer repairPoints) {
        this.repairPoints = repairPoints;
    }

    public Integer getPeriodPoints() {
        return periodPoints;
    }

    public void setPeriodPoints(Integer periodPoints) {
        this.periodPoints = periodPoints;
    }

    public List<RiverParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<RiverParticipant> participants) {
        this.participants = participants;
    }

    @Override
    public String toString() {
        return "RiverClan{" +
                "name='" + name + '\'' +
                ", tag='" + tag + '\'' +
                ", finishTime='" + finishTime + '\'' +
                ", clanScore=" + clanScore +
                ", badgeId=" + badgeId +
                ", fame=" + fame +
                ", repairPoints=" + repairPoints +
                ", periodPoints=" + periodPoints +
                ", participants=" + participants +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiverClan riverClan = (RiverClan) o;
        return Objects.equals(name, riverClan.name) && Objects.equals(tag, riverClan.tag) && Objects.equals(finishTime, riverClan.finishTime) && Objects.equals(clanScore, riverClan.clanScore) && Objects.equals(badgeId, riverClan.badgeId) && Objects.equals(fame, riverClan.fame) && Objects.equals(repairPoints, riverClan.repairPoints) && Objects.equals(periodPoints, riverClan.periodPoints) && Objects.equals(participants, riverClan.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tag, finishTime, clanScore, badgeId, fame, repairPoints, periodPoints, participants);
    }
}
