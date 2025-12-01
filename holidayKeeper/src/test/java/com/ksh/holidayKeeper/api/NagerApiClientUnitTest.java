package com.ksh.holidayKeeper.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RestClientTest(NagerApiClient.class)
public class NagerApiClientUnitTest {

    @Autowired
    private NagerApiClient apiClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    @DisplayName("특정 연도와 국가 코드로 공휴일 정보를 성공적으로 가져온다")
    void testFetchHolidays() {
        // given
        int year = 2025;
        String countryCode = "KR";
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
                                "https://date.nager.at/api/v3/PublicHolidays/" + year + "/" + countryCode
                        )
                )
                .andRespond(
                        MockRestResponseCreators.withSuccess(mockJson, MediaType.APPLICATION_JSON)
                );

        // when
        List<Map<String, Object>> result = apiClient.fetchHolidays(year, countryCode);

        // then
        assertEquals(1, result.size());
        assertEquals("새해", result.get(0).get("localName"));
        assertEquals("Public", result.get(0).get("type"));

        mockServer.verify();
    }
}
