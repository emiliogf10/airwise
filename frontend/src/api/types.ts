/** HTTP projections of airwise-v1. Monetary decimals stay strings throughout the UI. */
export type TripType = 'ONE_WAY' | 'ROUND_TRIP'
export type PrimaryCriterion = 'FLIGHT_PRICE' | 'FLIGHT_DURATION' | 'CONNECTIONS' | 'KNOWN_SUBTOTAL'
export type GroundComponent = 'ORIGIN_GROUND_OUTBOUND' | 'ORIGIN_GROUND_RETURN'
export type SearchSort = 'DEFAULT' | 'PRICE_ASC' | 'DURATION_ASC' | 'CONNECTIONS_ASC'
export interface Airport { code: string; name: string | null; city: string | null; countryCode: string | null; timeZone: string | null }
export interface DatasetDescriptor {
  datasetVersion: string; evaluationInstant: string; referenceDate: string; calendarStart: string; calendarEnd: string
  currencies: { code: string; minorUnits: number }[]; airlineCodes: string[]
  capabilities: { providerId: string; itineraries: string; priceObservations: string; oneWay: string; roundTrip: string; sellerDetails: string; baggageDetails: string; outputKinds: string[]; maxOriginsPerCall: number; requiresNetwork: boolean; requiresCredentials: boolean }
}
export interface AirportResponse {
  contractVersion: 'airwise-v1'; correlationId: string; dataset: DatasetDescriptor; items: Airport[]
  pagination: { page: number; pageSize: number; totalCount: number; hasMore: boolean }
}
export interface SearchQuery { tripType: TripType; origins: string[]; destination: string; outboundDate: string; returnDate: string | null; travellers: { adults: 1 }; cabin: 'ECONOMY' }
export interface SearchFilters { origins: string[]; airlineCodes: string[]; maxConnections: number | null; maxDurationMinutes: number | null; currency: string | null; maxPrice: string | null }
export interface SearchRequest { query: SearchQuery; datasetVersion: string; filters: SearchFilters; sort: SearchSort; page: number; pageSize: number }
export interface Money { amount: string; currency: string }
export interface PriceScope { basis: string; travellerCount: number | null; cabin: string | null; coverage: string; coveredSegmentIds: string[] }
export interface DataIssue { code: string; path: string | null; message: string }
export interface Allowance { status: string; pieces: number | null; weightKg: string | null }
export interface Baggage { cabin: Allowance; checked: Allowance }
export interface Price {
  money: Money | null; unknownReason: string | null; evidenceKind: string; scope: PriceScope; taxes: string
  additionalCosts: { kind: string; money: Money | null; unknownReason: string | null; includedInQuotedPrice: boolean | null }[]
  observedAt: string | null; retrievedAt: string; expiresAt: string | null; freshness: string
}
export interface FlightTime { localDateTime: string | null; offset: string | null; zoneId: string | null; instant: string | null; unknownReason: string | null }
export interface Segment {
  segmentId: string; departureAirport: string; arrivalAirport: string; departureTime: FlightTime; arrivalTime: FlightTime
  marketingAirline: { code: string | null; name: string | null } | null; operatingAirline: { code: string | null; name: string | null } | null
  flightNumber: string | null; technicalStops: number | null; durationSeconds: number | null
}
export interface Journey { direction: 'OUTBOUND' | 'INBOUND'; segments: Segment[]; connections: { afterSegmentIndex: number; durationSeconds: number | null; airportChange: boolean; selfTransfer: boolean | null }[]; durationSeconds: number | null }
export interface FlightOffer {
  offerId: string; providerId: string; dataOrigin: string
  itinerary: { tripType: TripType; journeys: Journey[]; durationSeconds: number | null; connectionCount: number }
  price: Price; seller: { id: string; name: string } | null; baggage: Baggage; comparisonGroupId: string; warnings: DataIssue[]
}
export interface PriceGroup { groupId: string; dataOrigin: string; evidenceKind: string; currency: string | null; scope: PriceScope; taxes: string; baggage: Baggage; additionalCostKinds: { kind: string; includedInQuotedPrice: boolean | null; currency: string | null }[]; monetarilyComparable: boolean; reasons: string[] }
export interface SearchContext { datasetVersion: string; fingerprint: string; query: SearchQuery; dataOrigin: string; evaluationInstant: string }
export interface ProviderIssue { code: string; message: string }
export interface ProviderResult { providerId: string; origin: string; status: string; requiredUnits: number; completedUnits: number; offerCount: number; observationCount: number; issues: ProviderIssue[] }
export interface PriceObservation { observationId: string; providerId: string; dataOrigin: string; origin: string; destination: string; tripType: TripType | null; outboundDate: string | null; returnDate: string | null; price: Price; warnings: DataIssue[] }
export interface SearchResponse {
  contractVersion: 'airwise-v1'; correlationId: string; context: SearchContext; status: 'COMPLETE' | 'PARTIAL'; offers: FlightOffer[]; priceGroups: PriceGroup[]
  observations: PriceObservation[]; observationCount: number; observationsTruncated: boolean
  pagination: { page: number; pageSize: number; baseCount: number; filteredCount: number; excludedUnknownCount: number; hasMore: boolean }
  providerResults: ProviderResult[]; warnings: DataIssue[]; errors: ProviderIssue[]
}
export interface GroundEstimate { offerId: string; component: GroundComponent; money: Money; basis: 'GROUP'; travellerCount: 1; source: 'USER_ESTIMATE'; assumption: 'USER_PROVIDED' }
export interface ComparisonRequest { query: SearchQuery; datasetVersion: string; offerIds: string[]; primary: PrimaryCriterion; requiredComponents: GroundComponent[]; groundEstimates: GroundEstimate[] }
export interface ComparisonRow { offerId: string; rank: number | null; primaryKnown: boolean; flightPrice: Money | null; durationSeconds: number | null; connectionCount: number; knownSubtotal: Money | null; groundEstimates: GroundEstimate[]; missingFields: string[]; reasons: string[] }
export interface ComparisonResponse {
  contractVersion: 'airwise-v1'; correlationId: string; context: SearchContext; primary: PrimaryCriterion; requiredComponents: GroundComponent[]
  groups: { groupId: string; priceGroup: PriceGroup; rows: ComparisonRow[]; rankingComplete: boolean; recommendationIds: string[]; recommendationScope: 'EVALUABLE_GROUP' }[]; warnings: DataIssue[]
}
