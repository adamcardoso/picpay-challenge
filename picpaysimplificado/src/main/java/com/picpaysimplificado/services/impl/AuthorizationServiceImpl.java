package com.picpaysimplificado.services.impl;

import com.picpaysimplificado.domain.user.User;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Service
public class AuthorizationServiceImpl {


    private final RestTemplate restTemplate;

    public AuthorizationServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean authorizeTransaction(User sender, BigDecimal value) {
        ResponseEntity<Map<String, Object>> authorizationResponse = restTemplate.exchange(
                "https://run.mocky.io/v3/8fafdd68-a090-496f-8c9a-3442cf30dae6",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {

                }
        );

        if (authorizationResponse.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = authorizationResponse.getBody();
            if (Objects.nonNull(responseBody)) {
                String message = (String) responseBody.get("message");
                return "Autorizado".equalsIgnoreCase(message);
            }
        }
        return false;
    }
}
