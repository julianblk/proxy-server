package com.lt.proxy.service;

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
        String expectedResponseBody = "some response body";
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>(expectedResponseBody, expectedResponseHeaders, HttpStatus.OK);
        when(
            restTemplateMock.exchange(proxyUrl, HttpMethod.GET, null, String.class)
        ).thenReturn(responseEntity);

        //WHEN: requesting a call to the proxy url
        String responseBody = proxyService.callUrl(proxyUrl);

        //THEN: the response body from the proxy call are returned
        assertEquals(expectedResponseBody, responseBody);
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

        //WHEN: requesting a call to the proxy url
        ResourceNotFoundException exception =
            assertThrows(
                ResourceNotFoundException.class,
                () -> proxyService.callUrl(proxyUrl)
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

        //AND: the proxy call response is neither OK nor NOT_FOUND
        HttpClientErrorException internalException = new HttpClientErrorException(HttpStatus.GATEWAY_TIMEOUT);
        when(
            restTemplateMock.exchange(proxyUrl, HttpMethod.GET, null, String.class)
        ).thenThrow(internalException);

        //WHEN: requesting a call to the proxy url
        ProxyException exception =
            assertThrows(
                ProxyException.class,
                () -> proxyService.callUrl(proxyUrl)
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

        //WHEN: requesting a call to the proxy url
        ProxyException exception =
            assertThrows(
                ProxyException.class,
                () -> proxyService.callUrl(proxyUrl)
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