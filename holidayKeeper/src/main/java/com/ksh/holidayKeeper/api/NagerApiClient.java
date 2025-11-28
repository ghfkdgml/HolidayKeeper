package com.ksh.holidayKeeper.api;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NagerApiClient {

    private final RestClient restClient;

    public List<Map<String, Object>> fetchCountries() {
        return restClient.get()
                .uri("https://date.nager.at/api/v3/AvailableCountries")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    public List<Map<String, Object>> fetchHolidays(int year, String countryCode) {
        String url = "https://date.nager.at/api/v3/PublicHolidays/%d/%s".formatted(year, countryCode);

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }
}
