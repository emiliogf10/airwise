package com.airwise.backend.flightprovider.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import com.airwise.backend.airport.domain.Airport;
import com.airwise.backend.flightsearch.application.*;
import com.airwise.backend.flightsearch.domain.*;
import static com.airwise.backend.flightsearch.domain.DomainChecks.*;
import static com.airwise.backend.flightsearch.domain.FlightTypes.*;

/** Loads the complete closed classpath corpus before any scenario can be served. */
final class DemoCorpus {
    static final String RESOURCE = "/demo/demo-v1.properties";
    final DatasetDescriptor dataset;
    final List<Airport> airports;
    final Map<ScenarioKey, Scenario> scenarios;
    private final Map<String, Airport> airportByCode;
    private final Properties properties;
    private final Set<String> consumed = new HashSet<>();
    private final Set<String> recordKeys = new HashSet<>();

    DemoCorpus(InputStream input) {
        present(input);
        properties = new StrictProperties();
        try (input) {
            byte[] bytes = input.readNBytes(65537);
            require(bytes.length <= 65536, "Demo corpus exceeds limit");
            var decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            properties.load(new InputStreamReader(new java.io.ByteArrayInputStream(bytes), decoder));
        } catch (IOException e) {
            throw new IllegalArgumentException("Demo corpus cannot be read", e);
        }
        require(properties.size() <= 1000, "Demo corpus exceeds field limit");
        require("DEMO".equals(get("dataOrigin")), "Corpus must identify synthetic data");
        var caps = new ProviderCapabilities(ProviderId.DEMO, CapabilityState.SUPPORTED, CapabilityState.SUPPORTED,
                CapabilityState.SUPPORTED, CapabilityState.SUPPORTED, CapabilityState.UNSUPPORTED, CapabilityState.SUPPORTED,
                List.of(OutputKind.OFFER, OutputKind.OBSERVATION), 1, false, false);
        List<CurrencyDefinition> currencies = csv(get("currencies")).stream().map(s -> {
            String[] c = fields(s, ":", 2);
            return new CurrencyDefinition(c[0], Integer.parseInt(c[1]));
        }).toList();
        dataset = new DatasetDescriptor(get("datasetVersion"), Instant.parse(get("evaluationInstant")),
                LocalDate.parse(get("referenceDate")), LocalDate.parse(get("calendarStart")), LocalDate.parse(get("calendarEnd")),
                currencies, csv(get("airlineCodes")), caps);
        require("demo-v1".equals(dataset.datasetVersion()), "Unexpected demo dataset version");
        List<String> codes = csv(get("airports"));
        require(codes.size() >= 1 && codes.size() <= 16 && codes.stream().distinct().count() == codes.size(), "Invalid airport catalogue");
        airports = codes.stream().map(code -> {
            String[] v = fields(get("airport." + code), "\\|", 4);
            Airport airport = new Airport(code, v[0], empty(v[1]), empty(v[2]), v[3]);
            require(airport.name() != null && airport.timeZone() != null, "Demo airport lacks reference metadata");
            return airport;
        }).sorted(Comparator.comparing(Airport::code)).toList();
        airportByCode = airports.stream().collect(Collectors.toUnmodifiableMap(Airport::code, a -> a));
        List<String> scenarioIds = csv(get("scenarios"));
        require(scenarioIds.size() <= 128 && scenarioIds.stream().distinct().count() == scenarioIds.size(), "Invalid scenario registry");
        Map<ScenarioKey, Scenario> loaded = new LinkedHashMap<>();
        for (String scenarioId : scenarioIds) {
            id(scenarioId);
            String[] s = fields(get("scenario." + scenarioId), "\\|", 9);
            ScenarioKey key = new ScenarioKey(TripType.valueOf(s[0]), LocalDate.parse(s[1]),
                    s[2].isEmpty() ? null : LocalDate.parse(s[2]), airport(s[3]), airport(s[4]));
            require(dataset.contains(key.outbound) && (key.inbound == null || dataset.contains(key.inbound)), "Scenario outside dataset calendar");
            require(airportByCode.containsKey(key.origin) && airportByCode.containsKey(key.destination), "Scenario outside airport catalogue");
            var query = new FlightSearchRequest(key.tripType, List.of(key.origin), key.destination, key.outbound, key.inbound,
                    new Travellers(1), Cabin.ECONOMY);
            int required = Integer.parseInt(s[5]), completed = Integer.parseInt(s[6]);
            List<ProviderIssue> issues = s[7].isEmpty() ? List.of() : List.of(ProviderIssue.of(ProviderIssueCode.valueOf(s[7])));
            List<String> records = s[8].isEmpty() ? List.of() : csv(s[8]);
            require(records.stream().distinct().count() == records.size(), "Duplicate record key");
            List<Fixture> fixtures = records.stream().map(record -> readFixture(record, query)).toList();
            long offers = fixtures.stream().filter(f -> f.kind == OutputKind.OFFER).count();
            long observations = fixtures.size() - offers;
            require(offers <= 20 && observations <= 20, "Scenario exceeds result bounds");
            ProviderStatus status = completed == required
                    ? (fixtures.isEmpty() ? ProviderStatus.EMPTY : ProviderStatus.COMPLETE)
                    : completed == 0 ? ProviderStatus.FAILED : ProviderStatus.PARTIAL;
            Scenario scenario = new Scenario(required, completed, status, List.copyOf(fixtures), issues);
            // Materialization checks every invariant before the immutable corpus is published.
            scenario.result(key.origin, StableIdentity.fingerprint(dataset.datasetVersion(), query));
            require(loaded.putIfAbsent(key, scenario) == null, "Duplicate scenario query");
        }
        require(consumed.equals(properties.stringPropertyNames()), "Unexpected demo corpus field");
        scenarios = Map.copyOf(loaded);
    }

    private Fixture readFixture(String key, FlightSearchRequest query) {
        id(key);
        require(recordKeys.add(key), "Record key reused in dataset");
        String prefix = "record." + key + ".";
        OutputKind kind = OutputKind.valueOf(get(prefix + "kind"));
        String[] p = fields(get(prefix + "price"), "\\|", 9);
        Money money = p[0].isEmpty() ? null : Money.parse(p[0], p[1]);
        require(money != null || p[1].isEmpty(), "Unknown amount must not claim Money currency");
        if (money != null) {
            require(dataset.currencies().stream().anyMatch(c -> c.code().equals(money.currency())), "Unregistered fixture currency");
        }
        PriceScope scope = new PriceScope(Basis.valueOf(p[3]), 1, Cabin.ECONOMY, Coverage.valueOf(p[4]), List.of());
        Instant observed = parseInstant(p[6]), retrieved = Instant.parse(p[7]), expires = parseInstant(p[8]);
        PriceInformation preliminary = new PriceInformation(money, money == null ? UnknownReason.NOT_RETURNED : null,
                EvidenceKind.valueOf(p[2]), scope, Taxes.valueOf(p[5]), List.of(), observed, retrieved, expires, Freshness.UNKNOWN);
        PriceInformation price = new PriceInformation(money, preliminary.unknownReason(), preliminary.evidenceKind(), scope,
                preliminary.taxes(), List.of(), observed, retrieved, expires, preliminary.freshnessAt(dataset.evaluationInstant()));
        require(price.evidenceKind() != EvidenceKind.BOOKING_CONFIRMED, "Demo cannot confirm bookings");
        List<DataIssue> warnings = new ArrayList<>();
        if (money == null) warnings.add(new DataIssue(DataIssueCode.PRICE_UNKNOWN, "/price/money", "Importe de vuelo desconocido."));
        if (scope.coverage() != Coverage.FULL_ITINERARY) warnings.add(new DataIssue(DataIssueCode.PRICE_SCOPE_UNKNOWN, "/price/scope", "El precio no cubre todo el itinerario."));
        if (price.freshness() == Freshness.EXPIRED) warnings.add(new DataIssue(DataIssueCode.EXPIRED_DATA, "/expiresAt", "Dato sintético caducado en el escenario."));
        if (price.freshness() == Freshness.UNKNOWN) warnings.add(new DataIssue(DataIssueCode.FRESHNESS_UNKNOWN, "/expiresAt", "Vigencia desconocida."));
        if (kind == OutputKind.OBSERVATION) {
            warnings.add(new DataIssue(DataIssueCode.OBSERVATION_ONLY, "/itinerary", "Observación sin itinerario completo."));
            return new Fixture(key, kind, query, null, price, null, issues(warnings));
        }
        List<FlightJourney> journeys = new ArrayList<>();
        journeys.add(readJourney(get(prefix + "outbound"), Direction.OUTBOUND));
        if (query.tripType() == TripType.ROUND_TRIP) journeys.add(readJourney(get(prefix + "inbound"), Direction.INBOUND));
        FlightItinerary itinerary = new FlightItinerary(query.tripType(), journeys,
                journeys.stream().anyMatch(j -> j.durationSeconds() == null) ? null : journeys.stream().mapToLong(FlightJourney::durationSeconds).sum(),
                journeys.stream().mapToInt(j -> j.connections().size()).sum());
        validateRoute(query, itinerary);
        String[] b = fields(get(prefix + "baggage"), "\\|", 2);
        BaggageInformation baggage = new BaggageInformation(allowance(BaggageStatus.valueOf(b[0])), allowance(BaggageStatus.valueOf(b[1])));
        if (baggage.checked().status() == BaggageStatus.UNKNOWN) {
            warnings.add(new DataIssue(DataIssueCode.BAGGAGE_UNKNOWN, "/baggage/checked", "Equipaje facturado desconocido."));
        }
        if (itinerary.connectionCount() > 0) warnings.add(new DataIssue(DataIssueCode.CONNECTION_UNVERIFIED, "/itinerary", "Conexiones sintéticas sin garantía de traslado."));
        return new Fixture(key, kind, query, itinerary, price, baggage, issues(warnings));
    }

    private FlightJourney readJourney(String source, Direction direction) {
        List<FlightSegment> segments = new ArrayList<>();
        for (String row : source.split(";", -1)) {
            String[] s = fields(row, ",", 7);
            Airport departureAirport = airportByCode.get(s[1]), arrivalAirport = airportByCode.get(s[2]);
            require(departureAirport != null && arrivalAirport != null, "Segment airport missing from catalogue");
            FlightTime departure = FlightTime.fromLocal(LocalDateTime.parse(s[3]), ZoneId.of(departureAirport.timeZone()), null);
            FlightTime arrival = FlightTime.fromLocal(LocalDateTime.parse(s[4]), ZoneId.of(arrivalAirport.timeZone()), null);
            require(dataset.airlineCodes().contains(s[5]), "Unregistered fixture airline");
            Airline airline = new Airline(s[5], "Aerolínea Demo " + s[5]);
            Long duration = departure.instant() == null || arrival.instant() == null ? null : Duration.between(departure.instant(), arrival.instant()).getSeconds();
            segments.add(new FlightSegment(s[0], s[1], s[2], departure, arrival, airline, null, s[6], 0, duration));
        }
        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < segments.size() - 1; i++) {
            FlightSegment a = segments.get(i), b = segments.get(i + 1);
            Long duration = a.arrivalTime().instant() == null || b.departureTime().instant() == null ? null
                    : Duration.between(a.arrivalTime().instant(), b.departureTime().instant()).getSeconds();
            connections.add(new Connection(i, duration, !a.arrivalAirport().equals(b.departureAirport()), null));
        }
        Long duration = segments.getFirst().departureTime().instant() == null || segments.getLast().arrivalTime().instant() == null ? null
                : Duration.between(segments.getFirst().departureTime().instant(), segments.getLast().arrivalTime().instant()).getSeconds();
        return new FlightJourney(direction, segments, connections, duration);
    }

    private static void validateRoute(FlightSearchRequest query, FlightItinerary itinerary) {
        var out = itinerary.journeys().getFirst();
        require(query.origins().contains(out.segments().getFirst().departureAirport())
                && query.destination().equals(out.segments().getLast().arrivalAirport()), "Fixture route contradicts query");
        require(query.outboundDate().equals(out.segments().getFirst().departureTime().localDateTime().toLocalDate()), "Fixture outbound date contradicts query");
        if (query.tripType() == TripType.ROUND_TRIP) {
            require(query.returnDate().equals(itinerary.journeys().getLast().segments().getFirst().departureTime().localDateTime().toLocalDate()),
                    "Fixture return date contradicts query");
        }
    }

    private String get(String key) {
        require(properties.containsKey(key), "Missing demo corpus field");
        consumed.add(key);
        return properties.getProperty(key);
    }

    private static String[] fields(String source, String separator, int size) {
        String[] values = source.split(separator, -1);
        require(values.length == size, "Malformed demo corpus row");
        return values;
    }

    private static List<String> csv(String value) {
        List<String> result = List.of(value.split(",", -1));
        require(result.stream().noneMatch(String::isBlank), "Empty demo corpus item");
        return result;
    }

    private static String empty(String value) { return value.isEmpty() ? null : value; }
    private static Instant parseInstant(String value) { return value.isEmpty() ? null : Instant.parse(value); }
    private static BaggageAllowance allowance(BaggageStatus status) {
        return new BaggageAllowance(status, status == BaggageStatus.NOT_INCLUDED ? 0 : null, null);
    }
    private static List<DataIssue> issues(List<DataIssue> issues) {
        return issues.stream().distinct().sorted(Comparator.comparing((DataIssue i) -> i.path() == null ? "" : i.path()).thenComparing(i -> i.code().name())).toList();
    }

    record ScenarioKey(TripType tripType, LocalDate outbound, LocalDate inbound, String origin, String destination) {}
    record Scenario(int required, int completed, ProviderStatus status, List<Fixture> fixtures, List<ProviderIssue> issues) {
        ProviderResult result(String origin, String fingerprint) {
            List<FlightOffer> offers = fixtures.stream().filter(f -> f.kind == OutputKind.OFFER)
                    .map(f -> f.offer(fingerprint)).sorted(Comparator.comparing(FlightOffer::offerId)).toList();
            List<PriceObservation> observations = fixtures.stream().filter(f -> f.kind == OutputKind.OBSERVATION)
                    .map(f -> f.observation(fingerprint)).sorted(Comparator.comparing(PriceObservation::observationId)).toList();
            return new ProviderResult(ProviderId.DEMO, origin, status, required, completed, offers, observations, issues);
        }
    }
    record Fixture(String key, OutputKind kind, FlightSearchRequest query, FlightItinerary itinerary,
            PriceInformation price, BaggageInformation baggage, List<DataIssue> warnings) {
        FlightOffer offer(String fingerprint) {
            return new FlightOffer(StableIdentity.offerId(fingerprint, key), ProviderId.DEMO, DataOrigin.DEMO,
                    itinerary, price, null, baggage, StableIdentity.groupId(fingerprint, DataOrigin.DEMO, price, baggage), warnings);
        }
        PriceObservation observation(String fingerprint) {
            return new PriceObservation(StableIdentity.observationId(fingerprint, key), ProviderId.DEMO, DataOrigin.DEMO,
                    query.origins().getFirst(), query.destination(), query.tripType(), query.outboundDate(), query.returnDate(), price, warnings);
        }
    }

    private static final class StrictProperties extends Properties {
        private static final long serialVersionUID = 1L;
        @Override public synchronized Object put(Object key, Object value) {
            require(!containsKey(key), "Duplicate demo corpus property");
            return super.put(key, value);
        }
    }
}
