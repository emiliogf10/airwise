// Synthetic fixtures follow the HTTP records in NormalizedViews and ComparisonViews.
// They describe contract scenarios, never real fares or provider responses.
import type { AirportResponse, Baggage, ComparisonResponse, FlightOffer, PriceGroup, PriceScope, SearchQuery, SearchResponse } from '../api/types'
export const correlationId = '00000000-0000-4000-8000-000000000001'
export const evaluationInstant = '2027-01-01T00:00:00Z'
export const offerId = (index: number) => `of_${index.toString(16).padStart(64, '0')}`
export const groupId = (currency = 'EUR') => `g_${(currency === 'EUR' ? 'a' : currency === 'USD' ? 'b' : 'c').repeat(64)}`

export function queryFixture(): SearchQuery {
  return {
    tripType: 'ONE_WAY', origins: ['OPO'], destination: 'MAD',
    outboundDate: '2027-02-10', returnDate: null,
    travellers: { adults: 1 }, cabin: 'ECONOMY',
  }
}

export function airportFixture(): AirportResponse {
  return {
    contractVersion: 'airwise-v1', correlationId,
    dataset: {
      datasetVersion: 'demo-v1', evaluationInstant, referenceDate: '2027-01-01',
      calendarStart: '2027-01-01', calendarEnd: '2027-12-31',
      currencies: [{ code: 'EUR', minorUnits: 2 }, { code: 'USD', minorUnits: 2 }],
      airlineCodes: ['D1', 'D2'],
      capabilities: {
        providerId: 'DEMO', itineraries: 'SUPPORTED', priceObservations: 'SUPPORTED',
        oneWay: 'SUPPORTED', roundTrip: 'SUPPORTED', sellerDetails: 'UNSUPPORTED',
        baggageDetails: 'SUPPORTED', outputKinds: ['OFFER', 'OBSERVATION'],
        maxOriginsPerCall: 1, requiresNetwork: false, requiresCredentials: false,
      },
    },
    items: [
      { code: 'BCN', name: 'Barcelona', city: 'Barcelona', countryCode: 'ES', timeZone: 'Europe/Madrid' },
      { code: 'MAD', name: 'Madrid', city: 'Madrid', countryCode: 'ES', timeZone: 'Europe/Madrid' },
      { code: 'OPO', name: 'Oporto', city: 'Oporto', countryCode: 'PT', timeZone: 'Europe/Lisbon' },
      { code: 'SCQ', name: 'Santiago de Compostela', city: 'Santiago de Compostela', countryCode: 'ES', timeZone: 'Europe/Madrid' },
    ],
    pagination: { page: 0, pageSize: 20, totalCount: 4, hasMore: false },
  }
}

function scopeFixture(): PriceScope {
  return { basis: 'GROUP', travellerCount: 1, cabin: 'ECONOMY', coverage: 'FULL_ITINERARY', coveredSegmentIds: [] }
}

function baggageFixture(): Baggage {
  return { cabin: { status: 'UNKNOWN', pieces: null, weightKg: null }, checked: { status: 'UNKNOWN', pieces: null, weightKg: null } }
}

export function offerFixture(index = 1, amount: string | null = '80.00', currency = 'EUR'): FlightOffer {
  return {
    offerId: offerId(index), providerId: 'DEMO', dataOrigin: 'DEMO',
    itinerary: {
      tripType: 'ONE_WAY', durationSeconds: 7200, connectionCount: 0,
      journeys: [{
        direction: 'OUTBOUND', durationSeconds: 7200, connections: [],
        segments: [{
          segmentId: `s${index}`, departureAirport: 'OPO', arrivalAirport: 'MAD',
          departureTime: { localDateTime: '2027-02-10T08:00:00', offset: 'Z', zoneId: 'Europe/Lisbon', instant: '2027-02-10T08:00:00Z', unknownReason: null },
          arrivalTime: { localDateTime: '2027-02-10T11:00:00', offset: '+01:00', zoneId: 'Europe/Madrid', instant: '2027-02-10T10:00:00Z', unknownReason: null },
          marketingAirline: { code: 'D1', name: 'Demo Air' }, operatingAirline: null,
          flightNumber: `D1${index}`, technicalStops: 0, durationSeconds: 7200,
        }],
      }],
    },
    price: {
      money: amount === null ? null : { amount, currency }, unknownReason: amount === null ? 'NOT_RETURNED' : null,
      evidenceKind: 'RECENT_SEARCH', scope: scopeFixture(), taxes: 'INCLUDED', additionalCosts: [],
      observedAt: null, retrievedAt: evaluationInstant, expiresAt: '2027-01-02T00:00:00Z', freshness: 'FRESH',
    },
    seller: null, baggage: baggageFixture(), comparisonGroupId: groupId(amount === null ? 'UNKNOWN' : currency), warnings: [],
  }
}

export function priceGroupFixture(currency = 'EUR'): PriceGroup {
  return {
    groupId: groupId(currency), dataOrigin: 'DEMO', evidenceKind: 'RECENT_SEARCH', currency,
    scope: scopeFixture(), taxes: 'INCLUDED', baggage: baggageFixture(), additionalCostKinds: [],
    monetarilyComparable: true, reasons: [],
  }
}

export function searchFixture(offers = [offerFixture(1), offerFixture(2, '120.00')]): SearchResponse {
  return {
    contractVersion: 'airwise-v1', correlationId,
    context: { datasetVersion: 'demo-v1', fingerprint: `q_${'a'.repeat(64)}`, query: queryFixture(), dataOrigin: 'DEMO', evaluationInstant },
    status: 'COMPLETE', offers,
    priceGroups: [...new Set(offers.map((offer) => offer.price.money?.currency ?? 'UNKNOWN'))].map((currency) => currency === 'UNKNOWN'
      ? { ...priceGroupFixture('UNKNOWN'), currency: null, monetarilyComparable: false, reasons: ['PRICE_UNKNOWN'] }
      : priceGroupFixture(currency)),
    observations: [], observationCount: 0, observationsTruncated: false,
    pagination: { page: 0, pageSize: 20, baseCount: offers.length, filteredCount: offers.length, excludedUnknownCount: 0, hasMore: false },
    providerResults: [{ providerId: 'DEMO', origin: 'OPO', status: offers.length ? 'COMPLETE' : 'EMPTY', requiredUnits: 1, completedUnits: 1, offerCount: offers.length, observationCount: 0, issues: [] }],
    warnings: [], errors: [],
  }
}

export function comparisonFixture(currencies = ['EUR', 'EUR']): ComparisonResponse {
  return {
    contractVersion: 'airwise-v1', correlationId,
    context: searchFixture().context, primary: 'FLIGHT_PRICE', requiredComponents: [],
    groups: [...new Set(currencies)].map((currency) => ({
      groupId: groupId(currency), priceGroup: priceGroupFixture(currency),
      rows: currencies.flatMap((rowCurrency, index) => rowCurrency === currency ? [{
        offerId: offerId(index + 1), rank: currencies.slice(0, index).filter((value) => value === currency).length + 1, primaryKnown: true,
        flightPrice: { amount: index === 0 ? '80.00' : '120.00', currency },
        durationSeconds: 7200, connectionCount: 0, knownSubtotal: null,
        groundEstimates: [], missingFields: [], reasons: currencies.filter((value) => value === currency).length === 1 ? ['SINGLE_EVALUABLE_OPTION'] : [],
      }] : []),
      rankingComplete: true, recommendationIds: currencies.filter((value) => value === currency).length > 1 ? [offerId(currencies.indexOf(currency) + 1)] : [], recommendationScope: 'EVALUABLE_GROUP',
    })),
    warnings: [],
  }
}

export function errorFixture(code = 'PROVIDERS_FAILED') {
  return { contractVersion: 'airwise-v1', correlationId, code, message: 'Mensaje de catálogo del servidor.', fieldErrors: [], fieldErrorsTruncated: false, providerResults: [] }
}

export function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json', 'X-Correlation-ID': correlationId } })
}
