// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.web;

import static com.yahoo.bard.webservice.util.DateTimeFormatterFactory.FULLY_OPTIONAL_DATETIME_FORMATTER;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;


/**
 * API Request. Interface offering default implementations for the common components of API request objects.
 */
public interface ApiRequest {
    long SYNCHRONOUS_ASYNC_AFTER_VALUE = Long.MAX_VALUE;
    long ASYNCHRONOUS_ASYNC_AFTER_VALUE = -1;

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
}
