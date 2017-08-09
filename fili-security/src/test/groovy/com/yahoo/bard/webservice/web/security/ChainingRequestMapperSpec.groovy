// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.web.security

import com.yahoo.bard.webservice.data.config.ResourceDictionaries
import com.yahoo.bard.webservice.web.ApiRequestImpl
import com.yahoo.bard.webservice.web.RequestMapper
import com.yahoo.bard.webservice.web.RequestValidationException

import spock.lang.Specification

import javax.ws.rs.container.ContainerRequestContext

class ChainingRequestMapperSpec extends Specification {

    RequestMapper nextMapper = Mock(RequestMapper)
    ApiRequestImpl mockRequest = Mock(ApiRequestImpl)
    ResourceDictionaries resourceDictionaries = new ResourceDictionaries()
    ContainerRequestContext containerRequestContext = Mock(ContainerRequestContext)

    def "Test that the chaining request processor delegates to the next mapper"() {
        setup:
        1 * mockRequest.clone() >> mockRequest
        1 * nextMapper.apply(mockRequest, containerRequestContext) >> mockRequest

        ChainingRequestMapper instance = new ChainingRequestMapper(resourceDictionaries, nextMapper) {
            ApiRequestImpl internalApply(final ApiRequestImpl request, final ContainerRequestContext context)
                    throws RequestValidationException {
                return (ApiRequestImpl) mockRequest.clone()
            }
        }

        expect:
        mockRequest == instance.apply(mockRequest, containerRequestContext)
    }
}
