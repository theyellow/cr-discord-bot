package crdiscordbot.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class PeriodLog {

    @SerializedName("items")
    private List<PeriodItem> items;

    @SerializedName("periodIndex")
    private Integer periodIndex;

    public List<PeriodItem> getItems() {
        return items;
    }

    public void setItems(List<PeriodItem> items) {
        this.items = items;
    }

    public Integer getPeriodIndex() {
        return periodIndex;
    }

    public void setPeriodIndex(Integer periodIndex) {
        this.periodIndex = periodIndex;
    }

    @Override
    public String toString() {
        return "PeriodLog{" +
                "items=" + items +
                ", periodIndex=" + periodIndex +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeriodLog periodLog = (PeriodLog) o;
        return Objects.equals(items, periodLog.items) && Objects.equals(periodIndex, periodLog.periodIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, periodIndex);
    }

}
