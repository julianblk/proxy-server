package com.lt.proxy.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.lt.proxy.exception.ProxyException;
import com.lt.proxy.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProxyServiceTest {

    @Mock private RestTemplate restTemplateMock;

    @InjectMocks
    private ProxyService proxyService;

    @Test
    void getResponseHeaders_validProxyUrl_success() {

        //GIVEN: a valid http-protocol url to proxy
        final String proxyUrl = "http://www.google.com";

        //AND: the proxy call is successful
        HttpHeaders expectedResponseHeaders = new HttpHeaders();
        expectedResponseHeaders.setContentType(MediaType.TEXT_HTML);
        expectedResponseHeaders.set("some-header-name", "some-header-value");
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("some ignored body", expectedResponseHeaders, HttpStatus.OK);
        when(
            restTemplateMock.exchange(proxyUrl, HttpMethod.GET, null, String.class)
        ).thenReturn(responseEntity);


        //WHEN: requesting the response headers for a proxy url call
        Map<String, String> responseHeaders = proxyService.getResponseHeaders(proxyUrl);

        //THEN: the expected headers from the proxy call are returned
        assertEquals(
            List.of(
                "Content-Type :: text/html",
                "some-header-name :: some-header-value"
            ),
            responseHeaders.entrySet()
                           .stream()
                           .map(e -> e.getKey() + " :: " + e.getValue())
                           .sorted()
                           .collect(Collectors.toList())
        );
    }

    @Test
    void getResponseHeaders_validProxyUrl_notFoundResponse_exceptionThrown() {

        //GIVEN: a valid http-protocol url to proxy
        final String proxyUrl = "http://www.google.com/some-path";

        //AND: the proxy call results in NOT_FOUND
        HttpClientErrorException internalException = new HttpClientErrorException(HttpStatus.NOT_FOUND);
        when(
            restTemplateMock.exchange(proxyUrl, HttpMethod.GET, null, String.class)
        ).thenThrow(internalException);

        //WHEN: requesting the response headers for a proxy url call
        ResourceNotFoundException exception =
            assertThrows(
                ResourceNotFoundException.class,
                () -> proxyService.getResponseHeaders(proxyUrl)
            );

        //THEN: the expected exception is thrown with details about the error
        assertEquals(
            String.format(
                "Could not found: %s",
                proxyUrl
            ),
            exception.getMessage()
        );
        assertEquals(
            internalException,
            exception.getCause()
        );
    }

    @Test
    void getResponseHeaders_validProxyUrl_responseDifferentToNotFound_exceptionThrown() {

        //GIVEN: a valid http-protocol url to proxy
        final String proxyUrl = "http://www.google.com/some-path";

        //AND: the proxy call results is neither OK nor NOT_FOUND
        HttpClientErrorException internalException = new HttpClientErrorException(HttpStatus.GATEWAY_TIMEOUT);
        when(
            restTemplateMock.exchange(proxyUrl, HttpMethod.GET, null, String.class)
        ).thenThrow(internalException);

        //WHEN: requesting the response headers for a proxy url call
        ProxyException exception =
            assertThrows(
                ProxyException.class,
                () -> proxyService.getResponseHeaders(proxyUrl)
            );

        //THEN: the expected exception is thrown with details about the error
        assertEquals(
            String.format(
                "Unexpected error when Executing GET call to: %s",
                proxyUrl
            ),
            exception.getMessage()
        );
        assertEquals(
            internalException,
            exception.getCause()
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "https://some-test-url.com",
            "s3://bucket/key.extension",
            "ftp://some-folder/some-file.ext"
        }
    )
    void getResponseHeaders_nonHttpProtocol_exceptionThrown(String proxyUrl) {

        //GIVEN: a non http-protocol url to proxy

        //WHEN: requesting the response headers for calling the proxy url
        ProxyException exception =
            assertThrows(
                ProxyException.class,
                () -> proxyService.getResponseHeaders(proxyUrl)
            );

        //THEN: the expected exception is thrown with details about the error
        assertEquals(
            String.format(
                "Cannot proxy to %s. Only HTTP protocol URI are accepted. Proxy URL must start with: http://",
                proxyUrl
            ),
            exception.getMessage()
        );
    }
}