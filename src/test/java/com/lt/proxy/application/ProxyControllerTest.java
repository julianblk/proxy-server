package com.lt.proxy.application;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.lt.proxy.exception.ProxyControllerExceptionHandler;
import com.lt.proxy.exception.ProxyException;
import com.lt.proxy.exception.ResourceNotFoundException;
import com.lt.proxy.service.ProxyService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProxyControllerTest {

    private static final String BASE_URL = "/proxy";

    private final ProxyService proxyServiceMock = mock(ProxyService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ProxyController(proxyServiceMock))
                                               .setControllerAdvice(new ProxyControllerExceptionHandler())
                                               .build();

    @Test
    void getResponseHeaders_proxyCallExecutedSuccessfully_OkResponse() throws Exception {

        //GIVEN: a valid http-protocol url to proxy
        final String proxyUrl = "http://www.google.com";

        //AND: the proxy call is correctly executed through the service
        Map<String, String> expectedHeaders =
            Map.of(
                "some-header-name-1", "some-header-value-1",
                "some-header-name-2", "some-header-value-2"
            );
        when(
            proxyServiceMock.getResponseHeaders(proxyUrl)
        ).thenReturn(expectedHeaders);

        //WHEN: requesting the response headers from a call to the proxy url
        String url = String.format("%s?proxy-url=%s", BASE_URL, proxyUrl);
        ResultActions results = mvc.perform(get(url));

        //THEN: the headers are in the response body
        results.andExpect(status().isOk())
               .andExpect(
                   content().json(
                       "{" +
                           "'some-header-name-1': 'some-header-value-1'," +
                           "'some-header-name-2': 'some-header-value-2'" +
                       "}"
                   )
               );
    }

    @Test
    void getResponseHeaders_proxyCallThrowsProxyException_BadRequestResponse() throws Exception {

        //GIVEN: a valid http-protocol url to proxy
        final String proxyUrl = "http://www.google.com";

        //AND: the proxy call through the service fails with ProxyException
        String errorMessage = "some error message";
        Exception cause = new Exception("Some cause");
        ProxyException exception = new ProxyException(errorMessage, cause);
        when(
            proxyServiceMock.getResponseHeaders(proxyUrl)
        ).thenThrow(exception);

        //WHEN: requesting the response headers from a call to the proxy url
        String url = String.format("%s?proxy-url=%s", BASE_URL, proxyUrl);
        ResultActions results = mvc.perform(get(url));

        //THEN: the error message is in the response body
        results.andExpect(status().isBadRequest())
               .andExpect(
                   content().json(
                       "{" +
                           "'errorMessage': '" + errorMessage + "'" +
                       "}"
                   )
               );
    }

    @Test
    void getResponseHeaders_proxyCallThrowsResourceNotFoundException_NotFoundResponse() throws Exception {

        //GIVEN: a valid http-protocol url to proxy
        final String proxyUrl = "http://www.google.com";

        //AND: the proxy call through the service fails with ResourceNotFoundException
        String errorMessage = "some error message";
        Exception cause = new Exception("Some cause");
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage, cause);
        when(
            proxyServiceMock.getResponseHeaders(proxyUrl)
        ).thenThrow(exception);

        //WHEN: requesting the response headers from a call to the proxy url
        String url = String.format("%s?proxy-url=%s", BASE_URL, proxyUrl);
        ResultActions results = mvc.perform(get(url));

        //THEN: the error message is in the response body
        results.andExpect(status().isNotFound())
               .andExpect(
                   content().json(
                       "{" +
                           "'errorMessage': '" + errorMessage + "'" +
                       "}"
                   )
               );
    }
}