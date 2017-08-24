// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.bard.webservice.data.havinggenerators;

import com.yahoo.bard.webservice.data.metric.LogicalMetric;
import com.yahoo.bard.webservice.data.metric.MetricDictionary;
import com.yahoo.bard.webservice.web.ApiHaving;

import java.util.Map;
import java.util.Set;

/**
 * Generates having maps from having strings.
 */
public interface HavingGeneratorBuilder {

    /**
     * Generates having objects based on the having query in the api request.
     *
     * @param havingQuery  Expects a URL having query String in the format:
     * (dimension name)-(operation)[(value or comma separated values)]?
     * @param logicalMetrics  The logical metrics used in this query
     * @param metricDictionary  The metric dictionary to bind parsed metrics from the query
     *
     * @return Set of having objects.
     */
    Map<LogicalMetric, Set<ApiHaving>> generateHavings(
        String havingQuery,
        Set<LogicalMetric> logicalMetrics,
        MetricDictionary metricDictionary
    );
}
