package com.airwise.backend.flightcomparison.api;

import java.util.*;
import org.junit.jupiter.api.Test;
import com.airwise.backend.flightcomparison.application.ComparisonEngine;
import com.airwise.backend.flightsearch.domain.*;
import static com.airwise.backend.flightcomparison.application.ComparisonTestData.*;
import static com.airwise.backend.flightcomparison.domain.ComparisonTypes.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;
import static org.assertj.core.api.Assertions.*;

class ComparisonProjectionTests {
    @Test void partialWarningOverridesCollisionAfterCollectingAllCausesWithoutRemoteText() {
        var warnings=ComparisonController.warnings(List.of(new DataIssue(DataIssueCode.INVALID_PROVIDER_DATA,null,"synthetic-secret"),
                new DataIssue(DataIssueCode.PRICE_UNKNOWN,"/price/money","synthetic-secret")),List.of(ProviderIssueCode.values()));
        assertThat(warnings).hasSize(2);
        var global=warnings.getFirst();
        assertThat(global.code()).isEqualTo("INVALID_PROVIDER_DATA");assertThat(global.path()).isNull();
        assertThat(global.message()).startsWith("Conjunto demo incompleto;").doesNotContain("synthetic-secret").hasSizeLessThanOrEqualTo(240);
        for(var code:ProviderIssueCode.values())assertThat(global.message()).contains(code.name());
        String expected=String.join(", ",Arrays.stream(ProviderIssueCode.values()).map(Enum::name).sorted().toList());
        assertThat(global.message()).endsWith(expected+".");
        assertThat(warnings.getLast().message()).doesNotContain("synthetic-secret");
    }

    @Test void projectionPreservesExactMoneyAndNullWithoutAddingGlobalWinnerFields() {
        var a=offer("a","0.00",3600L,0);var b=offer("b","100.00",3600L,0);var selected=List.of(a,b);
        var group=new ComparisonEngine().compare(request(Primary.FLIGHT_PRICE,selected),selected,EVALUATION).groups().getFirst();
        var view=ComparisonViews.group(group);
        assertThat(view.recommendationScope()).isEqualTo("EVALUABLE_GROUP");
        assertThat(view.rows().getFirst().flightPrice().amount()).isEqualTo("0.00");
        assertThat(view.rows().getFirst().knownSubtotal()).isNull();
        assertThat(view.priceGroup().monetarilyComparable()).isTrue();
        assertThat(view.priceGroup().groupId()).isEqualTo(view.groupId());
    }
}
