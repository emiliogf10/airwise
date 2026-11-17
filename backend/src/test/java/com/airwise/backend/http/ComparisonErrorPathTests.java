package com.airwise.backend.http;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;

class ComparisonErrorPathTests {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test void comparisonPointersAreBoundedAndNeverReflectUnknownFields() throws Exception {
        var fields = List.of(new FieldError("/offerIds/3", "UNKNOWN_OFFER_ID"),
                new FieldError("/requiredComponents/1", "DUPLICATE"),
                new FieldError("/groundEstimates/7/money/amount", "INVALID_AMOUNT"),
                new FieldError("/groundEstimates/7/source", "UNSUPPORTED_VALUE"),
                new FieldError("/groundEstimates/8/money/amount", "INVALID_AMOUNT"),
                new FieldError("/offerIds/4", "UNKNOWN_OFFER_ID"),
                new FieldError("/groundEstimates/0/synthetic-secret", "INVALID_FORMAT"));
        var response = render(fields);
        var json = mapper.readTree(response.getContentAsByteArray());
        var paths = new ArrayList<String>();
        json.get("fieldErrors").forEach(field -> paths.add(field.get("path").asString()));
        assertThat(paths).contains("/offerIds/3", "/requiredComponents/1", "/groundEstimates/7/money/amount", "/groundEstimates/7/source", "/");
        assertThat(response.getContentAsString()).doesNotContain("synthetic-secret", "/offerIds/4", "/groundEstimates/8");
    }

    @Test void allComparisonFieldErrorsAreSortedAndTruncationIsExplicit() throws Exception {
        var fields = new ArrayList<FieldError>();
        for (int i = 7; i >= 0; i--) {
            fields.add(new FieldError("/groundEstimates/" + i + "/money/amount", "INVALID_AMOUNT"));
            fields.add(new FieldError("/groundEstimates/" + i + "/money/currency", "INVALID_CURRENCY"));
            fields.add(new FieldError("/groundEstimates/" + i + "/component", "INVALID_COMBINATION"));
        }
        var response = render(fields);
        var json = mapper.readTree(response.getContentAsByteArray());
        assertThat(json.get("fieldErrors").size()).isEqualTo(20);
        assertThat(json.get("fieldErrorsTruncated").asBoolean()).isTrue();
        var paths = new ArrayList<String>();
        json.get("fieldErrors").forEach(field -> paths.add(field.get("path").asString()));
        assertThat(paths).isSorted();
    }

    private MockHttpServletResponse render(List<FieldError> fields) throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/v1/demo/comparisons");
        request.setAttribute(RequestContext.ATTRIBUTE, new RequestContext(UUID.randomUUID(), Instant.parse("2030-01-01T00:00:00Z")));
        var response = new MockHttpServletResponse();
        new ErrorWriter(mapper).write(request, response, ApiException.validation(fields));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getHeader("X-Correlation-ID")).isNotBlank();
        return response;
    }
}
