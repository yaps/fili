// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.metadata

import com.yahoo.bard.webservice.data.config.names.TestApiDimensionName
import com.yahoo.bard.webservice.data.config.names.TestApiMetricName
import com.yahoo.bard.webservice.data.config.names.TestDruidTableName

import com.google.common.collect.Range
import com.google.common.collect.RangeSet
import com.google.common.collect.TreeRangeSet

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Interval

import io.druid.timeline.DataSegment
import io.druid.timeline.partition.ShardSpec
import spock.lang.Shared
import spock.lang.Specification

/**
 * A class of utility methods to construct Interval, DataSegment
 */
class BaseDataSourceMetadataSpec extends Specification {
    @Shared
    String tableName = TestDruidTableName.ALL_PETS.asName()

    @Shared
    RangeSet<DateTime> rangeSet12

    @Shared
    DateTimeZone currentTZ

    @Shared
    List<String> dimensions

    @Shared
    List<String> metrics

    def setupSpec() {
        currentTZ = DateTimeZone.getDefault()
        DateTimeZone.setDefault(DateTimeZone.UTC)

        dimensions = [TestApiDimensionName.BREED, TestApiDimensionName.SPECIES, TestApiDimensionName.SEX]*.asName()
        metrics = [TestApiMetricName.A_ROW_NUM, TestApiMetricName.A_LIMBS, TestApiMetricName.A_DAY_AVG_LIMBS]*.asName()
    }

    def shutdownSpec() {
        DateTimeZone.setDefault(currentTZ)
    }

    def constructDataSegment(
            String interval,
            String version,
            ShardSpec shardSpec,
            int binaryVersion,
            int size
    ) {
        return new DataSegment(
                tableName,
                Interval.parse(interval),
                version,
                null,
                metrics,
                metrics,
                shardSpec,
                binaryVersion,
                size
        )
    }
}
