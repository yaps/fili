// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.fili.webservice.table;

import com.yahoo.fili.webservice.data.config.names.DataSourceName;
import com.yahoo.fili.webservice.data.config.names.TableName;
import com.yahoo.fili.webservice.data.time.ZonedTimeGrain;
import com.yahoo.fili.webservice.metadata.DataSourceMetadataService;
import com.yahoo.fili.webservice.table.availability.StrictAvailability;

import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

/**
 * An implementation of Physical table that is backed by a single fact table and has intersect availability.
 */
public class StrictPhysicalTable extends SingleDataSourcePhysicalTable {

    /**
     * Create a strict physical table.
     *
     * @param name  Name of the physical table as TableName, also used as data source name
     * @param timeGrain  time grain of the table
     * @param columns  The columns for this table
     * @param logicalToPhysicalColumnNames  Mappings from logical to physical names
     * @param metadataService  Datasource metadata service containing availability data for the table
     */
    public StrictPhysicalTable(
            @NotNull TableName name,
            @NotNull ZonedTimeGrain timeGrain,
            @NotNull Set<Column> columns,
            @NotNull Map<String, String> logicalToPhysicalColumnNames,
            @NotNull DataSourceMetadataService metadataService
    ) {
        this(
                name,
                timeGrain,
                columns,
                logicalToPhysicalColumnNames,
                new StrictAvailability(DataSourceName.of(name.asName()), metadataService)
        );
    }

    /**
     * Create a strict physical table with the availability on this table built externally.
     *
     * @param name  Name of the physical table as TableName, also used as fact table name
     * @param timeGrain  time grain of the table
     * @param columns  The columns for this table
     * @param logicalToPhysicalColumnNames  Mappings from logical to physical names
     * @param availability  Availability that serves interval availability for columns
     */
    public StrictPhysicalTable(
            @NotNull TableName name,
            @NotNull ZonedTimeGrain timeGrain,
            @NotNull Set<Column> columns,
            @NotNull Map<String, String> logicalToPhysicalColumnNames,
            @NotNull StrictAvailability availability
    ) {
        super(
                name,
                timeGrain,
                columns,
                logicalToPhysicalColumnNames,
                availability
        );
    }

    /**
     * Create a strict physical table.
     * The fact table name will be defaulted to the name and the availability initialized to empty intervals.
     *
     * @param name  Name of the physical table as String, also used as fact table name
     * @param timeGrain  time grain of the table
     * @param columns The columns for this table
     * @param logicalToPhysicalColumnNames  Mappings from logical to physical names
     * @param metadataService  Datasource metadata service containing availability data for the table
     *
     * @deprecated Should use constructor with TableName instead of String as table name
     */
    @Deprecated
    private StrictPhysicalTable(
            @NotNull String name,
            @NotNull ZonedTimeGrain timeGrain,
            @NotNull Set<Column> columns,
            @NotNull Map<String, String> logicalToPhysicalColumnNames,
            @NotNull DataSourceMetadataService metadataService
    ) {
        this(TableName.of(name), timeGrain, columns, logicalToPhysicalColumnNames, metadataService);
    }

    /**
     * Get the name of the fact table.
     *
     * @return the name of the fact table.
     *
     * @deprecated  Use getDataSourceName instead.
     */
    @Deprecated
    public String getFactTableName() {
        return getDataSourceName().asName();
    }

    @Override
    public String toString() {
        return super.toString() + " datasourceName: " + getDataSourceName();
    }
}