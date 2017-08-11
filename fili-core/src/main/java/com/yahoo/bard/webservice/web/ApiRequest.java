// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.web;

import com.yahoo.bard.webservice.config.BardFeatureFlag;
import com.yahoo.bard.webservice.data.dimension.Dimension;
import com.yahoo.bard.webservice.data.dimension.DimensionDictionary;
import com.yahoo.bard.webservice.logging.RequestLog;
import com.yahoo.bard.webservice.logging.TimedPhase;
import com.yahoo.bard.webservice.table.LogicalTable;
import com.yahoo.bard.webservice.util.Pagination;
import com.yahoo.bard.webservice.web.util.PaginationParameters;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;
import java.util.stream.Stream;

import static com.yahoo.bard.webservice.util.DateTimeFormatterFactory.FULLY_OPTIONAL_DATETIME_FORMATTER;
import static com.yahoo.bard.webservice.web.ErrorMessageFormat.FILTER_DIMENSION_NOT_IN_TABLE;


/**
 * API Request. Interface offering default implementations for the common components of API request objects.
 */
public interface ApiRequest {
    long SYNCHRONOUS_ASYNC_AFTER_VALUE = Long.MAX_VALUE;
    long ASYNCHRONOUS_ASYNC_AFTER_VALUE = -1;
    Logger LOG = LoggerFactory.getLogger(ApiRequest.class);
    String COMMA_AFTER_BRACKET_PATTERN = "(?<=]),";

    /**
     * Get the DateTimeFormatter shifted to the given time zone.
     *
     * @param timeZone  TimeZone to shift the default formatter to
     *
     * @return the timezone-shifted formatter
     */
    default DateTimeFormatter generateDateTimeFormatter(DateTimeZone timeZone) {
        return FULLY_OPTIONAL_DATETIME_FORMATTER.withZone(timeZone);
    }

    /**
     * Get the type of the requested response format.
     *
     * @return The format of the response for this API request.
     */
     ResponseFormatType getFormat();

    /**
     * Get the requested pagination parameters.
     *
     * @return The pagination parameters for this API request
     */
     Optional<PaginationParameters> getPaginationParameters();

    /**
     * Get the uri info.
     *
     * @return The uri info of this API request
     */
     UriInfo getUriInfo();

    /**
     * Get the pagination object associated with this request.
     * This object has valid contents after a call to {@link #getPage}
     *
     * @return The pagination object.
     */
     Pagination<?> getPagination();

    /**
     * Returns how long the user is willing to wait before a request should go asynchronous.
     *
     * @return The maximum number of milliseconds the request is allowed to take before going from synchronous to
     * asynchronous
     */
     long getAsyncAfter();


    /**
     * Get the response builder associated with this request.
     *
     * @return The response builder.
     */
     Response.ResponseBuilder getBuilder();

    /**
     * Get the default pagination parameters for this type of API request.
     *
     * @return The uri info of this type of API request
     */
     PaginationParameters getDefaultPagination();


    /**
     * Add links to the response builder and return a stream with the requested page from the raw data.
     *
     * @param <T>  The type of the collection elements
     * @param data  The data to be paginated.
     *
     * @return A stream corresponding to the requested page.
     *
     * @deprecated Pagination is moving to a Stream and pushing creation of the page to a more general
     * method ({@link #getPage(Pagination)}) to allow for more flexibility
     * in how pagination is done.
     */
    @Deprecated
     <T> Stream<T> getPage(Collection<T> data);

    /**
     * Add links to the response builder and return a stream with the requested page from the raw data.
     *
     * @param <T>  The type of the collection elements
     * @param pagination  The pagination object
     *
     * @return A stream corresponding to the requested page.
     */
     <T> Stream<T> getPage(Pagination<T> pagination);

    /**
     * Generates filter objects on the based on the filter query in the api request.
     *
     * @param filterQuery  Expects a URL filter query String in the format:
     * (dimension name).(fieldname)-(operation):[?(value or comma separated values)]?
     * @param table  The logical table for the data request
     * @param dimensionDictionary  DimensionDictionary
     *
     * @return Set of filter objects.
     * @throws BadApiRequestException if the filter query string does not match required syntax, or the filter
     * contains a 'startsWith' or 'contains' operation while the BardFeatureFlag.DATA_STARTS_WITH_CONTAINS_ENABLED is
     * off.
     */
    default Map<Dimension, Set<ApiFilter>> generateFilters(
            String filterQuery,
            LogicalTable table,
            DimensionDictionary dimensionDictionary
    ) throws BadApiRequestException {
        try (TimedPhase timer = RequestLog.startTiming("GeneratingFilters")) {
            LOG.trace("Dimension Dictionary: {}", dimensionDictionary);
            // Set of filter objects
            Map<Dimension, Set<ApiFilter>> generated = new LinkedHashMap<>();

            // Filters are optional hence check if filters are requested.
            if (filterQuery == null || "".equals(filterQuery)) {
                return generated;
            }

            // split on '],' to get list of filters
            List<String> apiFilters = Arrays.asList(filterQuery.split(COMMA_AFTER_BRACKET_PATTERN));
            for (String apiFilter : apiFilters) {
                ApiFilter newFilter;
                try {
                    newFilter = new ApiFilter(apiFilter, dimensionDictionary);

                    // If there is a logical table and the filter is not part of it, throw exception.
                    if (! table.getDimensions().contains(newFilter.getDimension())) {
                        String filterDimensionName = newFilter.getDimension().getApiName();
                        LOG.debug(FILTER_DIMENSION_NOT_IN_TABLE.logFormat(filterDimensionName, table));
                        throw new BadFilterException(
                                FILTER_DIMENSION_NOT_IN_TABLE.format(filterDimensionName, table.getName())
                        );
                    }

                } catch (BadFilterException filterException) {
                    throw new BadApiRequestException(filterException.getMessage(), filterException);
                }

                if (!BardFeatureFlag.DATA_FILTER_SUBSTRING_OPERATIONS.isOn()) {
                    FilterOperation filterOperation = newFilter.getOperation();
                    if (filterOperation.equals(FilterOperation.startswith)
                            || filterOperation.equals(FilterOperation.contains)
                            ) {
                        throw new BadApiRequestException(
                                ErrorMessageFormat.FILTER_SUBSTRING_OPERATIONS_DISABLED.format()
                        );

                    }
                }
                Dimension dim = newFilter.getDimension();
                if (!generated.containsKey(dim)) {
                    generated.put(dim, new LinkedHashSet<>());
                }
                Set<ApiFilter> filterSet = generated.get(dim);
                filterSet.add(newFilter);
            }
            LOG.trace("Generated map of filters: {}", generated);

            return generated;
        }
    }
}
