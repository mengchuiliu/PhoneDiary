package com.rdcx.bean;

public class WeekDate {
    private String weekFirstDay;// 一周开始日期
    private String weekEndDay;// 一周结束日期
    private int weekNumber;// 年内的周数
    private String month;
    private long startTime;
    private long endTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getWeekFirstDay() {
        return weekFirstDay;
    }

    public void setWeekFirstDay(String weekFirstDay) {
        this.weekFirstDay = weekFirstDay;
    }

    public String getWeekEndDay() {
        return weekEndDay;
    }

    public void setWeekEndDay(String weekEndDay) {
        this.weekEndDay = weekEndDay;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    @Override
    public String toString() {
        return "WeekDate [weekFirstDay=" + weekFirstDay + ", weekEndDay="
                + weekEndDay + ", weekNumber=" + weekNumber + "]";
    }
}
