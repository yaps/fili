// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.web;

import com.yahoo.bard.webservice.data.dimension.Dimension;
import com.yahoo.bard.webservice.data.dimension.DimensionDictionary;
import com.yahoo.bard.webservice.data.dimension.DimensionField;
import com.yahoo.bard.webservice.data.filterbuilders.DruidFilterBuilder;
import com.yahoo.bard.webservice.data.metric.LogicalMetric;
import com.yahoo.bard.webservice.data.metric.MetricDictionary;
import com.yahoo.bard.webservice.data.time.TimeGrain;
import com.yahoo.bard.webservice.druid.model.filter.Filter;
import com.yahoo.bard.webservice.druid.model.having.Having;
import com.yahoo.bard.webservice.druid.model.orderby.OrderByColumn;
import com.yahoo.bard.webservice.druid.model.query.Granularity;
import com.yahoo.bard.webservice.table.LogicalTable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Data API Request. Such an API Request binds, validates, and models the parts of a request to the data endpoint.
 */
 public interface DataApiRequest extends ApiRequest {
    String REQUEST_MAPPER_NAMESPACE = "dataApiRequestMapper";
    Logger LOG = LoggerFactory.getLogger(DataApiRequest.class);
    String COMMA_AFTER_BRACKET_PATTERN = "(?<=]),";

    /**
     * Validity rules for non-aggregatable dimensions that are only referenced in filters.
     * A query that references a non-aggregatable dimension in a filter without grouping by this dimension, is valid
     * only if the requested dimension field is a key for this dimension and only a single value is requested
     * with an inclusive operator ('in' or 'eq').
     *
     * @return A predicate that determines a given dimension is non aggregatable and also not constrained to one row
     * per result
     */
     default Predicate<ApiFilter> isNonAggregatableInFilter() {
        return apiFilter ->
                !apiFilter.getDimensionField().equals(apiFilter.getDimension().getKey()) ||
                        apiFilter.getValues().size() != 1 ||
                        !(
                                apiFilter.getOperation().equals(FilterOperation.in) ||
                                        apiFilter.getOperation().equals(FilterOperation.eq)
                        );
    }

    /**
     * Builds and returns the Druid filters from this request's {@link ApiFilter}s.
     * <p>
     * The Druid filters are built (an expensive operation) every time this method is called. Use it judiciously.
     *
     * @return the Druid filter
     */
     Filter getFilter();

    /**
     * The having constraints for this request, grouped by logical metrics.
     *
     * @return a map of havings by metrics.
     */
     Map<LogicalMetric, Set<ApiHaving>> getHavings();

    /**
     *  The fact model having (should probably remove this).
     *
     *  @return A fact model having
     */
     Having getHaving();

    /**
     * A prioritized list of sort columns.
     *
     * @return sort columns.
     */
     LinkedHashSet<OrderByColumn> getSorts();

    /**
     * An optional limit of records returned.
     *
     * @return An optional integer.
     */
     OptionalInt getCount();

    /**
     * The limit per time bucket for a top n query.
     *
     * @return The number of values per bucket.
     */
     OptionalInt getTopN();

    /**
     * The date time zone to apply to the dateTime parameter and to express the response and granularity in.
     *
     * @return A time zone
     */
     DateTimeZone getTimeZone();

    /**
     * A filter builder (remove).
     *
     * @return a filter builder.
     */
     DruidFilterBuilder getFilterBuilder();

    /**
     * An optional sorting predicate for the time column.
     *
     * @return The sort direction
     */
     Optional<OrderByColumn> getDateTimeSort();

    /**
     * Get the dimensions used in filters on this request.
     *
     * @return A set of dimensions
     */
     Set<Dimension> getFilterDimensions();

    /**
     * The logical table for this request.
     *
     * @return A logical table
     */
     LogicalTable getTable();

    /**
     * The grain to group the results of this request.
     *
     * @return a granularity
     */
     Granularity getGranularity();

    /**
     * The set of grouping dimensions on this ApiRequest.
     *
     * @return a set of dimensions
     */
     Set<Dimension> getDimensions();

    /**
     * A map of dimension fields specified for the output schema.
     *
     * @return  The dimension fields for output grouped by their dimension
     */
     LinkedHashMap<Dimension, LinkedHashSet<DimensionField>> getDimensionFields();

    /**
     * The logical metrics requested in this query.
     *
     * @return A set of logical metrics
     */
     Set<LogicalMetric> getLogicalMetrics();

    /**
     * The intervals for this query.
     *
     * @return A set of intervals
     */
     Set<Interval> getIntervals();

    /**
     * The filters for this ApiRequest, grouped by dimensions.
     *
     * @return a map of filters by dimension
     */
     Map<Dimension, Set<ApiFilter>> getFilters();

    /**
     * Generates filter objects on the based on the filter query in the api request.
     *
     * @param filterQuery  Expects a URL filter query String in the format:
     * (dimension name).(fieldname)-(operation):[?(value or comma separated values)]?
     * @param table  The logical table for the data request
     * @param dimensionDictionary  DimensionDictionary
     *
     * @return Set of filter objects.
     */
     Map<Dimension, Set<ApiFilter>> generateFilters(
            String filterQuery,
            LogicalTable table,
            DimensionDictionary dimensionDictionary
    );

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

    /**
     * Generate current date based on granularity.
     *
     * @param dateTime  The current moment as a DateTime
     * @param timeGrain  The time grain used to round the date time
     *
     * @return truncated current date based on granularity
     */
     default DateTime getCurrentDate(DateTime dateTime, TimeGrain timeGrain) {
        return timeGrain.roundFloor(dateTime);
    }
}
