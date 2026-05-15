package io.sclera.utils;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
public class WebClientRequest {

    /************************************************************ HTTP REQUEST ********************************************************************/
    // Http request
    public WebClient getScleraWebClient(Integer readTimeout, Integer connectionTimeout, Integer responseTimeout) {
        if (readTimeout == null)
            readTimeout = 5000;
        if (connectionTimeout == null)
            connectionTimeout = 5000;
        if (responseTimeout == null)
            responseTimeout = 0;

        Integer finalReadTimeout = readTimeout;
        return WebClient.builder()
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(configurer -> {
                                    configurer.defaultCodecs().maxInMemorySize(1024 * 1024 * 100);
                                })
                                .build()
                )
                .clientConnector(
                        new ReactorClientHttpConnector(HttpClient.create(ConnectionProvider.newConnection())
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                                .responseTimeout(Duration.ofMillis(responseTimeout))
                                .doOnConnected(connection -> connection
                                        .addHandlerFirst(new ReadTimeoutHandler(finalReadTimeout, TimeUnit.MILLISECONDS))
                                )
                        )).build();
    }

    private MultiValueMap<String, HttpEntity<?>> addMultipartFiles(String fileKey, List<MultipartFile> multipartFiles) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        if (multipartFiles != null && multipartFiles.size() > 0) {
            for (MultipartFile multipartFile : multipartFiles) {
                builder.part(fileKey, multipartFile.getResource());
            }
        }
        return builder.build();
    }


    // Http request
    public <T> ResponseEntity<T> httpRequest(
            HttpMethod httpMethod,
            String apiURL,
            Map<String, String> params,
            Map<String, String> headers,
            Object requestBody,
            MediaType mediaType,
            Class<T> responseClass,
            String fileKey,
            List<MultipartFile> multipartFiles,
            Integer connectionTimeout,
            Integer readTimeout,
            Integer responseTimeout
    ) {
        return this.getScleraWebClient(readTimeout, connectionTimeout, responseTimeout)
                .method(httpMethod)
                .uri(apiURL, uriBuilder -> {
                    if (params != null) {
                        for (Map.Entry<String, String> entry : params.entrySet()) {
                            uriBuilder.queryParam(entry.getKey(), entry.getValue());
                        }
                    }
                    return uriBuilder.build();
                })
                .headers(httpHeaders -> {
                    if (headers != null) {
                        for (Map.Entry<String, String> entry : headers.entrySet()) {
                            httpHeaders.set(entry.getKey(), entry.getValue());
                        }
                    }
                })
                .contentType(mediaType)
                .accept(MediaType.APPLICATION_JSON)
                .body(
                        mediaType.equals(MediaType.APPLICATION_JSON) ?
                                BodyInserters.fromValue(requestBody != null ? requestBody : "") :
                                BodyInserters.fromMultipartData(addMultipartFiles(fileKey, multipartFiles))
                )
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.empty())
                .onStatus(HttpStatus::is5xxServerError, response -> Mono.empty())
                .toEntity(responseClass)
                .block();
    }
    /************************************************************ HTTP REQUEST ********************************************************************/


    /************************************************************ HTTPS REQUEST ********************************************************************/
    // Https request
    public WebClient getScleraWebClientForHttps(Integer readTimeout, Integer connectionTimeout, Integer responseTimeout) {
        if (readTimeout == null)
            readTimeout = 5000;
        if (connectionTimeout == null)
            connectionTimeout = 5000;
        if (responseTimeout == null)
            responseTimeout = 0;

        Integer finalReadTimeout = readTimeout;

        // Create an SSLContext that trusts all certificates
        SslContext sslContext;
        try {
            sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SSL context", e);
        }
        return WebClient.builder().exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(configurer -> {
                                    configurer.defaultCodecs().maxInMemorySize(1024 * 1024 * 1000);
                                })
                                .build()
                )
                .clientConnector(
                        new ReactorClientHttpConnector(HttpClient.create(ConnectionProvider.newConnection())
                                .secure(t->t.sslContext(sslContext))
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                                .responseTimeout(Duration.ofMillis(responseTimeout))
                                .doOnConnected(connection -> connection
                                        .addHandlerFirst(new ReadTimeoutHandler(finalReadTimeout, TimeUnit.MILLISECONDS))
                                )
                        )).build();
    }

    public <T> ResponseEntity<T> httpsRequest(
            HttpMethod httpMethod,
            String apiURL,
            Map<String, String> params,
            Map<String, String> headers,
            Object requestBody,
            MediaType mediaType,
            Class<T> responseClass,
            String fileKey,
            List<MultipartFile> multipartFiles,
            Integer connectionTimeout,
            Integer readTimeout,
            Integer responseTimeout
    ) {
        try {
            if (params != null && !params.isEmpty()) {
                StringBuilder uriBuilder = new StringBuilder(apiURL);
                uriBuilder.append("?");

                int count = 0;
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                    String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);

                    uriBuilder.append(encodedKey);
                    uriBuilder.append("=");
                    uriBuilder.append(encodedValue);

                    // Check if it is not the last parameter
                    if (count < params.size() - 1) {
                        uriBuilder.append("&");
                    }
                    count++;
                }

                apiURL = uriBuilder.toString();
            }
            return this.getScleraWebClientForHttps(readTimeout, connectionTimeout, responseTimeout)
                    .method(httpMethod)
                    .uri(new URI(apiURL))
                    .headers(httpHeaders -> {
                        if (headers != null) {
                            for (Map.Entry<String, String> entry : headers.entrySet()) {
                                httpHeaders.set(entry.getKey(), entry.getValue());
                            }
                        }
                    })
                    .contentType(mediaType)
                    .accept(MediaType.APPLICATION_JSON)
                    .body((
                            mediaType.equals(MediaType.APPLICATION_JSON) || mediaType.equals(MediaType.TEXT_PLAIN)) ?
                            BodyInserters.fromValue(requestBody != null ? requestBody : "") :
                            BodyInserters.fromMultipartData(addMultipartFiles(fileKey, multipartFiles))
                    )
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, response -> Mono.empty())
                    .onStatus(HttpStatus::is5xxServerError, response -> Mono.empty())
                    .toEntity(responseClass)
                    .block();
        } catch (Exception e) {
            // Handle any exceptions that may occur during the request
            throw new RuntimeException("Error making API request: " + e.getMessage(), e);
        }
    }
    /************************************************************ HTTPS REQUEST ********************************************************************/

}