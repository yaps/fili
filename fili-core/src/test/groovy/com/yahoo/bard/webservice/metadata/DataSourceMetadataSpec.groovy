// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.metadata

import static com.yahoo.bard.webservice.data.Columns.DIMENSIONS
import static com.yahoo.bard.webservice.data.Columns.METRICS

import com.yahoo.bard.webservice.data.Columns
import com.yahoo.bard.webservice.util.SimplifiedIntervalList

import com.google.common.collect.RangeSet

import org.joda.time.DateTime
import org.joda.time.Interval

import io.druid.timeline.DataSegment
import io.druid.timeline.partition.ShardSpec

class DataSourceMetadataSpec extends BaseDataSourceMetadataSpec {
    List<DataSegment> segments
    Interval interval

    def setup() {
        segments = [
                constructDataSegment("2015-01-01T00:00:00.000Z/2015-01-02T00:00:00.000Z", "", Mock(ShardSpec), 0, 0),
                constructDataSegment("2015-01-02T00:00:00.000Z/2015-01-03T00:00:00.000Z",  "", Mock(ShardSpec), 0, 0)
        ]
        interval = Interval.parse("2015-01-01T00:00:00.000Z/2015-01-03T00:00:00.000Z")
    }

    def "test construct healthy DataSourceMetadata (interval version)"() {
        setup:
        DataSourceMetadata dataSourceMetadata = new DataSourceMetadata(tableName, [:], segments)
        Map<Columns, Map<String, SimplifiedIntervalList>> intervalLists = DataSourceMetadata.getIntervalLists(dataSourceMetadata)

        expect:
        dataSourceMetadata.getName() == tableName
        dataSourceMetadata.getProperties() == [:]
        dataSourceMetadata.getSegments() == segments
        intervalLists.size() == 2
        intervalLists.get(DIMENSIONS).keySet().sort() == dimensions.sort()
        intervalLists.get(DIMENSIONS).values() as List == [
                [interval] as SimplifiedIntervalList,
                [interval] as SimplifiedIntervalList,
                [interval] as SimplifiedIntervalList
        ]
        intervalLists.get(METRICS).keySet().sort() == metrics.sort()
        intervalLists.get(METRICS).values() as List == [
                [interval] as SimplifiedIntervalList,
                [interval] as SimplifiedIntervalList,
                [interval] as SimplifiedIntervalList
        ]
    }

    def "test construct healthy datasource metadata (rangeSet version)"() {
        setup:
        DataSourceMetadata metadata = new DataSourceMetadata(tableName, [:], segments)
        Map<Columns, Map<String, RangeSet<DateTime>>> rangeLists = DataSourceMetadata.getRangeLists(metadata)

        expect:
        metadata.getName() == tableName
        metadata.getProperties() == [:]
        metadata.getSegments() == segments
        rangeLists.size() == 2
        rangeLists.get(DIMENSIONS).keySet().sort() == dimensions.sort()
        rangeLists.get(DIMENSIONS).values() as List == [
                rangeSet12,
                rangeSet12,
                rangeSet12
        ]
        rangeLists.get(METRICS).keySet().sort() == metrics.sort()
        rangeLists.get(METRICS).values() as List == [
                rangeSet12,
                rangeSet12,
                rangeSet12
        ]
    }
}
