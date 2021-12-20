package com.lt.proxy.service;

import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.lt.proxy.exception.ProxyException;
import com.lt.proxy.exception.ResourceNotFoundException;

@Service
public class ProxyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyService.class);
    private static final String HTTP_PROTOCOL_PREFIX = "http://";

    private final RestTemplate restTemplate;

    public ProxyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, String> getResponseHeaders(String proxyUrl) {

        if (!proxyUrl.startsWith(HTTP_PROTOCOL_PREFIX)) {
            throw new ProxyException(
                String.format(
                    "Cannot proxy to %s. Only HTTP protocol URI are accepted. Proxy URL must start with: %s",
                    proxyUrl,
                    HTTP_PROTOCOL_PREFIX
                )
            );
        }

        HttpEntity<String> response;
        LOGGER.info("Executing GET call to {} ....", proxyUrl);
        try {
            response = restTemplate.exchange(proxyUrl, HttpMethod.GET, null, String.class);
        } catch (Exception e) {
            if (e instanceof HttpClientErrorException) {
                if (((HttpClientErrorException) e).getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    throw new ResourceNotFoundException("Could not found: " + proxyUrl, e);
                }
            }
            throw new ProxyException("Unexpected error when Executing GET call to: " + proxyUrl, e);
        }
        HttpHeaders headers = response.getHeaders();
        LOGGER.info(
            "\n" +
            "==================================\n" +
            "Response Headers (proxy url: {})\n" +
            "==================================\n" +
            headers.entrySet()
                   .stream()
                   .map(e -> e.getKey() + " :: " + e.getValue())
                   .sorted()
                   .collect(Collectors.joining("\n")),
            proxyUrl
        );

        return headers.toSingleValueMap();
    }
}
