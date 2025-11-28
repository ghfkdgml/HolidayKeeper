package com.ksh.holidayKeeper.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class NagerApiClientUnitTest {

    private NagerApiClient apiClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // RestTemplate 기반으로 RestClient 생성
        RestTemplate restTemplate = new RestTemplate(); 

        // Mock 서버 연결
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        RestClient restClient = RestClient.create(restTemplate);

        apiClient = new NagerApiClient(restClient);
    }

    @Test
    void testFetchHolidays() {
        // given
        String mockJson = """
                [
                  {
                    "date": "2025-01-01",
                    "localName": "새해",
                    "name": "New Year's Day",
                    "type": "Public"
                  }
                ]
                """;

        mockServer.expect(
                        MockRestRequestMatchers.requestTo(
                                "https://date.nager.at/api/v3/PublicHolidays/2025/KR"
                        )
                )
                .andRespond(
                        MockRestResponseCreators.withSuccess(mockJson, MediaType.APPLICATION_JSON)
                );

        // when
        List<Map<String, Object>> result = apiClient.fetchHolidays(2025, "KR");

        // then
        assertEquals(1, result.size());
        assertEquals("새해", result.get(0).get("localName"));
        assertEquals("Public", result.get(0).get("type"));

        mockServer.verify();
    }
}
