package com.airwise.backend.flightsearch.application;

import static com.airwise.backend.flightsearch.domain.FlightTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.airwise.backend.flightprovider.demo.DemoFlightProvider;
import com.airwise.backend.flightsearch.domain.FlightSearchRequest;
import com.airwise.backend.flightsearch.domain.Travellers;

class ProviderContractTests {

    private static final Instant NOW = Instant.parse("2030-01-01T00:00:00Z");
    private final DemoFlightProvider provider = new DemoFlightProvider(Clock.fixed(NOW, ZoneOffset.UTC));

    private FlightSearchRequest query(String outbound) {
        return new FlightSearchRequest(TripType.ONE_WAY, List.of("OPO"), "MAD", LocalDate.parse(outbound),
                null, new Travellers(1), Cabin.ECONOMY);
    }

    private ProviderSearchRequest request(DatasetDescriptor descriptor) {
        return new ProviderSearchRequest(query("2027-02-10"), "OPO", descriptor,
                new ProviderExecutionContext(UUID.fromString("00000000-0000-4000-8000-000000000001"), NOW.plusSeconds(60)));
    }

    @Test
    void fingerprintAndRecordIdentityMatchRatifiedCanonicalBytes() {
        var q = query("2027-02-10");
        var fingerprint = StableIdentity.fingerprint("demo-v1", q);
        assertEquals("q_f4cb1de712e0a33f1260c695fb0ccdf53d6f4da2ec05fa534625969aac8dd930", fingerprint);
        assertEquals("of_f04264b1cb41c28788bb5f40d3c18f96f88966050d68ffc6f3f3cc286afc3c83",
                StableIdentity.offerId(fingerprint, "opo-mad-oneway"));
        assertNotEquals(fingerprint, StableIdentity.fingerprint("demo-v2", q));
        assertNotEquals(fingerprint, StableIdentity.fingerprint("demo-v1", query("2027-02-11")));
        assertNotEquals(StableIdentity.offerId(fingerprint, "a"), StableIdentity.offerId(fingerprint, "b"));
        assertNotEquals(StableIdentity.offerId(fingerprint, "a"), StableIdentity.observationId(fingerprint, "a"));
    }

    @Test
    void serverDescriptorCannotBeReplacedUsingSameVersionWithDifferentClock() {
        var d = provider.dataset();
        var forged = new DatasetDescriptor(d.datasetVersion(), d.evaluationInstant().plusSeconds(1),
                d.referenceDate(), d.calendarStart(), d.calendarEnd(), d.currencies(), d.airlineCodes(), d.capabilities());
        assertFalse(provider.supports(request(forged)));
        assertThrows(IllegalArgumentException.class, () -> provider.search(request(forged)));
        assertEquals(d, provider.dataset());
        assertFalse(provider.supports(request(null)));
    }

    @Test
    void serverDescriptorRejectsChangedCalendarCapabilitiesAndVersion() {
        var d = provider.dataset();
        var changedCalendar = new DatasetDescriptor(d.datasetVersion(), d.evaluationInstant(), d.referenceDate(),
                d.calendarStart().plusDays(1), d.calendarEnd(), d.currencies(), d.airlineCodes(), d.capabilities());
        assertFalse(provider.supports(request(changedCalendar)));
        var c = d.capabilities();
        var changedCapability = new ProviderCapabilities(c.providerId(), CapabilityState.UNSUPPORTED, c.priceObservations(),
                c.oneWay(), c.roundTrip(), c.sellerDetails(), c.baggageDetails(), c.outputKinds(),
                c.maxOriginsPerCall(), c.requiresNetwork(), c.requiresCredentials());
        var changedCaps = new DatasetDescriptor(d.datasetVersion(), d.evaluationInstant(), d.referenceDate(),
                d.calendarStart(), d.calendarEnd(), d.currencies(), d.airlineCodes(), changedCapability);
        assertFalse(provider.supports(request(changedCaps)));
        var wrongVersion = new DatasetDescriptor("demo-retired", d.evaluationInstant(), d.referenceDate(),
                d.calendarStart(), d.calendarEnd(), d.currencies(), d.airlineCodes(), d.capabilities());
        assertFalse(provider.supports(request(wrongVersion)));
    }

    @Test
    void currencyDefinitionsAndDatasetCollectionsAreValidated() {
        assertThrows(IllegalArgumentException.class, () -> new CurrencyDefinition("EUR", 3));
        assertThrows(IllegalArgumentException.class, () -> new CurrencyDefinition("XXX", 0));
        assertThrows(UnsupportedOperationException.class, () -> provider.dataset().currencies().clear());
        assertThrows(UnsupportedOperationException.class, () -> provider.capabilities().outputKinds().clear());
        var d = provider.dataset();
        assertThrows(IllegalArgumentException.class, () -> new DatasetDescriptor("demo-v1", d.evaluationInstant(),
                d.calendarStart().plusDays(1), d.calendarStart(), d.calendarEnd(), d.currencies(), d.airlineCodes(), d.capabilities()));
    }

    @Test
    void providerStatusInvariantsDoNotTurnFailuresIntoEmptySuccess() {
        var issue = List.of(ProviderIssue.of(ProviderIssueCode.PROVIDER_OFFLINE));
        assertDoesNotThrow(() -> new ProviderResult(ProviderId.DEMO, "OPO", ProviderStatus.EMPTY, 1, 1,
                List.of(), List.of(), List.of()));
        assertDoesNotThrow(() -> new ProviderResult(ProviderId.DEMO, "OPO", ProviderStatus.FAILED, 1, 0,
                List.of(), List.of(), issue));
        assertDoesNotThrow(() -> new ProviderResult(ProviderId.DEMO, "OPO", ProviderStatus.PARTIAL, 2, 1,
                List.of(), List.of(), issue));
        assertThrows(IllegalArgumentException.class, () -> new ProviderResult(ProviderId.DEMO, "OPO", ProviderStatus.EMPTY, 1, 0,
                List.of(), List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ProviderResult(ProviderId.DEMO, "OPO", ProviderStatus.COMPLETE, 1, 1,
                List.of(), List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ProviderResult(ProviderId.DEMO, "OPO", ProviderStatus.FAILED, 1, 0,
                List.of(), List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ProviderResult(ProviderId.DEMO, "OPO", ProviderStatus.PARTIAL, 1, 1,
                List.of(), List.of(), issue));
        assertThrows(IllegalArgumentException.class, () -> new ProviderResult(ProviderId.DEMO, "OPO", ProviderStatus.UNSUPPORTED, 1, 0,
                List.of(), List.of(), issue));
    }

    @Test
    void capabilityIssueIsExplicitAndCannotBeOperationalFailure() {
        var capability = List.of(ProviderIssue.of(ProviderIssueCode.CAPABILITY_UNVERIFIED));
        assertDoesNotThrow(() -> new ProviderResult(ProviderId.DEMO, "OPO", ProviderStatus.UNSUPPORTED, 1, 0,
                List.of(), List.of(), capability));
        assertThrows(IllegalArgumentException.class, () -> new ProviderResult(ProviderId.DEMO, "OPO", ProviderStatus.FAILED, 1, 0,
                List.of(), List.of(), capability));
        assertThrows(IllegalArgumentException.class, () -> new ProviderIssue(ProviderIssueCode.PROVIDER_FAILURE, "unsafe\nmessage"));
        for (var code : ProviderIssueCode.values()) {
            var issue = ProviderIssue.of(code);
            assertTrue(issue.message().length() <= 240);
            assertFalse(issue.message().contains("http"));
            assertFalse(issue.message().contains("token"));
        }
    }
}
