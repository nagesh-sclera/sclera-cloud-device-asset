package io.sclera.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.sclera.exception.ServerException;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.SslProvider;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ScleraWebClient {


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
                                .codecs(configure -> {
                                    configure.defaultCodecs().maxInMemorySize(1024 * 1024 * 100);
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
                                .followRedirect(true)
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
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.empty())
                .toEntity(responseClass)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).jitter(0.75)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            throw new ServerException("External Service failed to process after max retries", 900, apiURL);
                        }))
                .block();
    }


    public <T> ResponseEntity<T> httpRequest2(
            HttpMethod httpMethod,
            String apiURL,
            Map<String, String> params,
            Map<String, String> headers,
            Object requestBody,
            MediaType mediaType,
            Class<T> responseClass,
            MultiValueMap<String, Object> formData,
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
//                        mediaType.equals(MediaType.APPLICATION_JSON) ?
//                                BodyInserters.fromValue(requestBody) :
//                                BodyInserters.fromMultipartData(formData)
                        mediaType.equals(MediaType.APPLICATION_JSON) ?
                                BodyInserters.fromValue(requestBody != null ? requestBody : "") :
                                (mediaType.equals(MediaType.APPLICATION_FORM_URLENCODED) ?
                                        BodyInserters.fromFormData(addFormDataParameters(requestBody)) :
                                        BodyInserters.fromMultipartData(formData))

                )
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.empty())
                .toEntity(responseClass)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).jitter(0.75)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            throw new ServerException("External Service failed to process after max retries", 900, apiURL);
                        }))
                .block();
    }

    public MultiValueMap<String, String> addFormDataParameters(Object object) {
        ObjectMapper mapObject = new ObjectMapper();

        Map<String, String> formDataMap = mapObject.convertValue(object, HashMap.class);
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        for (Map.Entry<String, String> entry : formDataMap.entrySet()) {
            formData.add(entry.getKey(), entry.getValue());
        }

        return formData;
    }

}