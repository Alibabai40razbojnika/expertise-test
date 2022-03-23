package com.expertise.filip.util;

import com.google.cloud.bigquery.CsvOptions;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.ExternalTableDefinition;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Field.Mode;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueryUtils {

    private static final Logger LOGGER = Logger.getLogger(QueryUtils.class.getName());

    @Autowired
    private CustomProperties properties;

    public String createSelectQuery(String serialNumber, String date) {
        return new StringBuilder("select ")
                .append(getCommaSeparatedColumnList())
                .append(" from ")
                .append(getFullFormatedTableName())
                .append(" where ")
                .append(makeWhereCondition(serialNumber, date)).toString();
    }

    /**
     * Fixed list of table column names
     *
     * @return string containing list of table column names
     */
    private String getCommaSeparatedColumnList() {
        return "serial_number, date, total_working_hours, working_hours, min_revolutions, avg_revolutions, max_revolutions, min_engine_load, avg_engine_load, max_engine_load, min_fuel_consumption, avg_fuel_consumption, max_fuel_consumption, coordinate_list";
    }

    public String getFullFormatedTableName() {
        return "`" + properties.getProjectId() + "." + properties.getDatasetName() + "." + properties.getFormatedTableName() + "`";
    }

    /**
     * Only possible way to query the table is by two keys (composite primary
     * key)
     *
     * @param serialNumber Serial number of tractor
     * @param date date for which tractor info is requested
     * @return formated where condition
     */
    private String makeWhereCondition(String serialNumber, String date) {
        return "serial_number = " + surroundWithQuote(serialNumber) + " and date = " + surroundWithQuote(date);
    }

    private String surroundWithQuote(String value) {
        return "\"" + value + "\"";
    }

    public DatasetInfo createDataset() {
        return DatasetInfo.newBuilder(properties.getDatasetName()).setLocation(properties.getLocation()).build();
    }

    public TableInfo createFormatedTable() {
        return TableInfo.newBuilder(TableId.of(properties.getDatasetName(), properties.getFormatedTableName()), StandardTableDefinition.of(formatedTableSchema())).build();
    }

    /**
     *
     * @return Table schema of external table
     */
    public Schema mainTableSchema() {
        Schema schema
                = Schema.of(
                        Field.of("date_time", StandardSQLTypeName.STRING),
                        Field.of("serial_number", StandardSQLTypeName.STRING),
                        Field.of("gps_longitude", StandardSQLTypeName.FLOAT64),
                        Field.of("gps_latitude", StandardSQLTypeName.FLOAT64),
                        Field.of("total_working_hours_counter_h", StandardSQLTypeName.FLOAT64),
                        Field.of("engine_speed_rpm", StandardSQLTypeName.INT64),
                        Field.of("engine_load", StandardSQLTypeName.INT64),
                        Field.of("fuel_consumption_l_h", StandardSQLTypeName.FLOAT64),
                        Field.of("ground_speed_gearbox_km_h", StandardSQLTypeName.FLOAT64),
                        Field.of("ground_speed_radar_km_h", StandardSQLTypeName.FLOAT64),
                        Field.of("coolant_temperature_c", StandardSQLTypeName.INT64),
                        Field.of("speed_front_pto_rpm", StandardSQLTypeName.INT64),
                        Field.of("speed_rear_pto_rpm", StandardSQLTypeName.INT64),
                        Field.of("current_gear_shift", StandardSQLTypeName.STRING),
                        Field.of("ambient_temperature_c", StandardSQLTypeName.STRING),
                        Field.of("parking_brake_status", StandardSQLTypeName.INT64),
                        Field.of("transverse_differential_lock_status", StandardSQLTypeName.INT64),
                        Field.of("all_wheel_drive_status", StandardSQLTypeName.STRING),
                        Field.of("actual_status_of_creeper", StandardSQLTypeName.STRING));
        return schema;
    }

    /**
     *
     * @return Table schema of actual table in data set which contains data
     */
    public Schema formatedTableSchema() {
        Schema schema
                = Schema.of(
                        Field.of("serial_number", StandardSQLTypeName.STRING),
                        Field.of("date", StandardSQLTypeName.DATE),
                        Field.of("total_working_hours", StandardSQLTypeName.FLOAT64),
                        Field.of("working_hours", StandardSQLTypeName.FLOAT64),
                        Field.of("min_revolutions", StandardSQLTypeName.INT64),
                        Field.of("avg_revolutions", StandardSQLTypeName.FLOAT64),
                        Field.of("max_revolutions", StandardSQLTypeName.INT64),
                        Field.of("min_engine_load", StandardSQLTypeName.INT64),
                        Field.of("avg_engine_load", StandardSQLTypeName.FLOAT64),
                        Field.of("max_engine_load", StandardSQLTypeName.INT64),
                        Field.of("min_fuel_consumption", StandardSQLTypeName.FLOAT64),
                        Field.of("avg_fuel_consumption", StandardSQLTypeName.FLOAT64),
                        Field.of("max_fuel_consumption", StandardSQLTypeName.FLOAT64),
                        Field.newBuilder(
                                "coordinate_list",
                                StandardSQLTypeName.STRUCT,
                                Field.of("longitude", StandardSQLTypeName.FLOAT64),
                                Field.of("latitude", StandardSQLTypeName.FLOAT64))
                                .setMode(Mode.REPEATED)
                                .build()
                );

        return schema;
    }

    /**
     * Method creates configuration for external table
     *
     * @param fileName
     * @return
     */
    public QueryJobConfiguration executeMergeQuery(String fileName) {
        CsvOptions csvOptions = CsvOptions.newBuilder().setSkipLeadingRows(1).setFieldDelimiter(properties.getFieldDelimiter()).build();

        ExternalTableDefinition externalTableDefinition
                = ExternalTableDefinition
                        .newBuilder("gs://" + properties.getBucketName() + "/" + fileName, csvOptions)
                        .setSchema(mainTableSchema())
                        .build();

        QueryJobConfiguration queryConfig
                = QueryJobConfiguration.newBuilder(mergeTableQuery())
                        .addTableDefinition(properties.getMainTableName(), externalTableDefinition)
                        .build();
        return queryConfig;
    }

    /**
     * Full merge table query, filter file trough external table and merge to
     * actual table
     *
     * @return query string
     */
    public String mergeTableQuery() {
        StringBuilder mergeTableQueryBuilder = new StringBuilder();

        mergeTableQueryBuilder
                .append("merge ").append(getFullFormatedTableName()).append(" inTable ")
                .append("using ").append(externalTableQuery()).append(" exTable ")
                .append("on ")
                .append("inTable.serial_number = exTable.serialNumber and ")
                .append("inTable.date = exTable.date ")
                .append("when matched then ")
                .append("update set ")
                .append("inTable.total_working_hours = exTable.totalWorkingHours, ")
                .append("inTable.working_hours = exTable.workingHours, ")
                .append("inTable.min_revolutions = exTable.minRevolutions, ")
                .append("inTable.avg_revolutions = exTable.avgRevolutions, ")
                .append("inTable.max_revolutions = exTable.maxRevolutions, ")
                .append("inTable.min_engine_load = exTable.minEngineLoad, ")
                .append("inTable.avg_engine_load = exTable.avgEngineLoad, ")
                .append("inTable.max_engine_load = exTable.maxEngineLoad, ")
                .append("inTable.min_fuel_consumption = exTable.minFuelConsumption, ")
                .append("inTable.avg_fuel_consumption = exTable.avgFuelConsumption, ")
                .append("inTable.max_fuel_consumption = exTable.maxFuelConsumption, ")
                .append("inTable.coordinate_list = exTable.coordinateList ")
                .append("when not matched then ")
                .append("insert ")
                .append("values ")
                .append("(exTable.serialNumber, exTable.date, exTable.totalWorkingHours, exTable.workingHours, exTable.minRevolutions, exTable.avgRevolutions, exTable.maxRevolutions, exTable.minEngineLoad, exTable.avgEngineLoad, exTable.maxEngineLoad, exTable.minFuelConsumption, exTable.avgFuelConsumption, exTable.maxFuelConsumption, exTable.coordinateList)");
        return mergeTableQueryBuilder.toString();

    }

    /**
     * External query for filtering and transforming data from external source
     *
     * @return query string
     */
    private String externalTableQuery() {
        StringBuilder externalTableQueryBuilder = new StringBuilder();
        externalTableQueryBuilder.append("(select ")
                .append("serial_number as serialNumber, ")
                .append("date(parse_datetime('%b %e, %Y %l:%M:%S %p', date_time)) as date, ")
                .append("max(total_working_hours_counter_h) as totalWorkingHours, ")
                .append("max(total_working_hours_counter_h) - min(total_working_hours_counter_h) as workingHours, ")
                .append("min(engine_speed_rpm) as minRevolutions, ")
                .append("avg(engine_speed_rpm) as avgRevolutions, ")
                .append("max(engine_speed_rpm) as maxRevolutions, ")
                .append("min(engine_load) as minEngineLoad, ")
                .append("avg(engine_load) as avgEngineLoad, ")
                .append("max(engine_load) as maxEngineLoad, ")
                .append("min(fuel_consumption_l_h) as minFuelConsumption, ")
                .append("avg(fuel_consumption_l_h) as avgFuelConsumption, ")
                .append("max(fuel_consumption_l_h) as maxFuelConsumption, ")
                .append("array_agg(struct(gps_longitude, gps_latitude)) as coordinateList ")
                .append("from ").append(properties.getMainTableName()).append(" ")
                .append("group by date, serialNumber)");

        return externalTableQueryBuilder.toString();
    }
}
