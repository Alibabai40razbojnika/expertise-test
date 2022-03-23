package com.expertise.filip.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;
import com.google.gson.Gson;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TractorFormatedDto implements Serializable {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-mm-dd");

    private String serialNumber;
    private Date date;

    private double totalWorkingHours;
    private double workingHours;

    private long minRevolutions;
    private double avgRevolutions;
    private long maxRevolutions;

    private long minEngineLoad;
    private double avgEngineLoad;
    private long maxEngineLoad;

    private double minFuelConsumption;
    private double avgFuelConsumption;
    private double maxFuelConsumption;

    private List<CoordinateDto> coordinateList = new ArrayList<>();

    @JsonIgnore
    public String getCoordinateListJson() {
        return new Gson().toJson(coordinateList);
    }

    public TractorFormatedDto(FieldValueList fieldValueList) {

        this.setSerialNumber(fieldValueList.get(0).getStringValue());
        try {
            setDate(SIMPLE_DATE_FORMAT.parse(fieldValueList.get(1).getStringValue()));
        } catch (ParseException ex) {
            Logger.getLogger(TractorFormatedDto.class.getName()).log(Level.WARNING, "Data format is incorrect!\n" + fieldValueList.get(1).getStringValue(), ex);
        }

        setTotalWorkingHours(fieldValueList.get(2).getDoubleValue());
        setWorkingHours(fieldValueList.get(3).getDoubleValue());

        setMinRevolutions(fieldValueList.get(4).getLongValue());
        setAvgRevolutions(fieldValueList.get(5).getDoubleValue());
        setMaxRevolutions(fieldValueList.get(6).getLongValue());

        setMinEngineLoad(fieldValueList.get(7).getLongValue());
        setAvgEngineLoad(fieldValueList.get(8).getDoubleValue());
        setMaxEngineLoad(fieldValueList.get(9).getLongValue());

        setMinFuelConsumption(fieldValueList.get(10).getDoubleValue());
        setAvgFuelConsumption(fieldValueList.get(11).getDoubleValue());
        setMaxFuelConsumption(fieldValueList.get(12).getDoubleValue());

        for (FieldValue record : fieldValueList.get(13).getRecordValue()) {
            coordinateList.add(
                    new CoordinateDto(
                            record.getRepeatedValue().get(0).getDoubleValue(),
                            record.getRepeatedValue().get(1).getDoubleValue()
                    )
            );
        }

    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getTotalWorkingHours() {
        return totalWorkingHours;
    }

    public void setTotalWorkingHours(double totalWorkingHours) {
        this.totalWorkingHours = totalWorkingHours;
    }

    public double getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(double workingHours) {
        this.workingHours = workingHours;
    }

    public long getMinRevolutions() {
        return minRevolutions;
    }

    public void setMinRevolutions(long minRevolutions) {
        this.minRevolutions = minRevolutions;
    }

    public double getAvgRevolutions() {
        return avgRevolutions;
    }

    public void setAvgRevolutions(double avgRevolutions) {
        this.avgRevolutions = avgRevolutions;
    }

    public long getMaxRevolutions() {
        return maxRevolutions;
    }

    public void setMaxRevolutions(long maxRevolutions) {
        this.maxRevolutions = maxRevolutions;
    }

    public long getMinEngineLoad() {
        return minEngineLoad;
    }

    public void setMinEngineLoad(long minEngineLoad) {
        this.minEngineLoad = minEngineLoad;
    }

    public double getAvgEngineLoad() {
        return avgEngineLoad;
    }

    public void setAvgEngineLoad(double avgEngineLoad) {
        this.avgEngineLoad = avgEngineLoad;
    }

    public long getMaxEngineLoad() {
        return maxEngineLoad;
    }

    public void setMaxEngineLoad(long maxEngineLoad) {
        this.maxEngineLoad = maxEngineLoad;
    }

    public double getMinFuelConsumption() {
        return minFuelConsumption;
    }

    public void setMinFuelConsumption(double minFuelConsumption) {
        this.minFuelConsumption = minFuelConsumption;
    }

    public double getAvgFuelConsumption() {
        return avgFuelConsumption;
    }

    public void setAvgFuelConsumption(double avgFuelConsumption) {
        this.avgFuelConsumption = avgFuelConsumption;
    }

    public double getMaxFuelConsumption() {
        return maxFuelConsumption;
    }

    public void setMaxFuelConsumption(double maxFuelConsumption) {
        this.maxFuelConsumption = maxFuelConsumption;
    }

    public List<CoordinateDto> getCoordinateList() {
        return coordinateList;
    }

    public void setCoordinateList(List<CoordinateDto> coordinateList) {
        this.coordinateList = coordinateList;
    }

    @Override
    public String toString() {
        return "TractorFormatedDto{" + "serialNumber=" + serialNumber + ", date=" + date + ", totalWorkingHours=" + totalWorkingHours + ", workingHours=" + workingHours + ", minRevolutions=" + minRevolutions + ", avgRevolutions=" + avgRevolutions + ", maxRevolutions=" + maxRevolutions + ", minEngineLoad=" + minEngineLoad + ", avgEngineLoad=" + avgEngineLoad + ", maxEngineLoad=" + maxEngineLoad + ", minFuelConsumption=" + minFuelConsumption + ", avgFuelConsumption=" + avgFuelConsumption + ", maxFuelConsumption=" + maxFuelConsumption + ", coordinateList=" + coordinateList + '}';
    }

}
