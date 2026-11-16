package com.airwise.backend.airport.api;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class AirportHttpTests {
    @Autowired MockMvc mvc;

    @Test void publicCatalogIsVersionedSortedAndSessionless() throws Exception {
        var response = mvc.perform(get("/api/v1/demo/airports").header("X-Correlation-ID", "client-controlled"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.contractVersion").value("airwise-v1"))
                .andExpect(jsonPath("$.dataset.datasetVersion").value("demo-v1"))
                .andExpect(jsonPath("$.items[0].code").value("BCN"))
                .andExpect(jsonPath("$.items[3].code").value("SCQ"))
                .andExpect(jsonPath("$.pagination.totalCount").value(4))
                .andExpect(header().doesNotExist("Set-Cookie")).andReturn();
        String correlation = response.getResponse().getHeader("X-Correlation-ID");
        assertThat(UUID.fromString(correlation)).isNotNull();
        assertThat(response.getResponse().getContentAsString()).contains(correlation);
        assertThat(response.getRequest().getSession(false)).isNull();
    }

    @Test void catalogSearchNormalizesDiacriticsAndPagingIsStable() throws Exception {
        mvc.perform(get("/api/v1/demo/airports").param("q", "  bÁrcelona  "))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].code").value("BCN"));
        mvc.perform(get("/api/v1/demo/airports").param("pageSize", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pagination.hasMore").value(true));
        mvc.perform(get("/api/v1/demo/airports").param("page", "1").param("pageSize", "2").param("datasetVersion", "demo-v1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].code").value("OPO"))
                .andExpect(jsonPath("$.pagination.hasMore").value(false));
        mvc.perform(get("/api/v1/demo/airports").param("page", "100000").param("datasetVersion", "demo-v1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty());
    }

    @Test void versionCannotDriftBetweenPages() throws Exception {
        mvc.perform(get("/api/v1/demo/airports").param("page", "1"))
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.fieldErrors[0].path").value("/datasetVersion"));
        mvc.perform(get("/api/v1/demo/airports").param("datasetVersion", "retired-v0"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DATASET_VERSION_UNAVAILABLE"));
    }

    @Test void catalogRejectsUnknownRepeatedAndNonIntegerParameters() throws Exception {
        for (String parameter : new String[]{"provider", "token", "asOf"}) {
            mvc.perform(get("/api/v1/demo/airports").param(parameter, "synthetic-secret"))
                    .andExpect(status().isBadRequest()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("synthetic-secret"))));
        }
        mvc.perform(get("/api/v1/demo/airports").param("q", "a", "b")).andExpect(status().isBadRequest());
        for (String page : new String[]{"1.0", "1e0", "+1"})
            mvc.perform(get("/api/v1/demo/airports").param("page", page)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/demo/airports").param("pageSize", "51")).andExpect(status().isUnprocessableContent());
    }
}
