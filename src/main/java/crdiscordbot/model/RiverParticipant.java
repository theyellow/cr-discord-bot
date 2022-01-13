package crdiscordbot.model;

import java.util.Objects;

public class RiverParticipant {


    private String tag;

    private String name;

    private Integer fame;

    private Integer repairPoints;

    private Integer boatAttacks;

    private Integer decksUsed;

    private Integer decksUsedToday;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getBoatAttacks() {
        return boatAttacks;
    }

    public void setBoatAttacks(Integer boatAttacks) {
        this.boatAttacks = boatAttacks;
    }

    public Integer getDecksUsed() {
        return decksUsed;
    }

    public void setDecksUsed(Integer decksUsed) {
        this.decksUsed = decksUsed;
    }

    public Integer getDecksUsedToday() {
        return decksUsedToday;
    }

    public void setDecksUsedToday(Integer decksUsedToday) {
        this.decksUsedToday = decksUsedToday;
    }

    @Override
    public String toString() {
        return "RiverParticipant{" +
                "tag='" + tag + '\'' +
                ", name='" + name + '\'' +
                ", fame=" + fame +
                ", repairPoints=" + repairPoints +
                ", boatAttacks=" + boatAttacks +
                ", decksUsed=" + decksUsed +
                ", decksUsedToday=" + decksUsedToday +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiverParticipant that = (RiverParticipant) o;
        return Objects.equals(tag, that.tag) && Objects.equals(name, that.name) && Objects.equals(fame, that.fame) && Objects.equals(repairPoints, that.repairPoints) && Objects.equals(boatAttacks, that.boatAttacks) && Objects.equals(decksUsed, that.decksUsed) && Objects.equals(decksUsedToday, that.decksUsedToday);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, name, fame, repairPoints, boatAttacks, decksUsed, decksUsedToday);
    }
}
