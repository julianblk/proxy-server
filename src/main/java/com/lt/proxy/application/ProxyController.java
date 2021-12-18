package com.lt.proxy.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/proxy")
public class ProxyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyController.class);
    private final RestTemplate restTemplate;

    public ProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<?,?>> getResponseHeaders(@RequestParam("proxy-url") String proxyUrl) {

        HttpEntity<String> response = restTemplate.exchange("http://www.google.com", HttpMethod.GET, null, String.class);

        HttpHeaders headers = response.getHeaders();

//        System.out.println(
//            "Response Headers:\n" +
//            headers.entrySet()
//                   .stream()
//                   .map(e -> e.getKey() + " :: " + e.getValue())
//                   .sorted()
//                   .collect(Collectors.joining("\n"))
//        );

        LOGGER.info(
            "\n" +
            "================\n" +
            "Response Headers\n" +
            "================\n" +
            headers.entrySet()
                   .stream()
                   .map(e -> e.getKey() + " :: " + e.getValue())
                   .sorted()
                   .collect(Collectors.joining("\n"))
        );

        return ResponseEntity.ok(headers);

//        return ResponseEntity.ok(
//            headers.entrySet()
//                   .stream()
//                   .map(e -> e.getKey() + " :: " + e.getValue())
//                   .sorted()
//                   .collect(Collectors.toList())
//        );
    }
}
