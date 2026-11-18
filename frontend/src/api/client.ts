import type { AirportResponse, ComparisonRequest, ComparisonResponse, SearchRequest, SearchResponse } from './types'

const base = '/api/v1/demo'
const messages: Record<string, string> = {
  NETWORK_ERROR: 'No se puede conectar con el backend demo local. Comprueba que está iniciado.',
  INVALID_RESPONSE: 'El backend no ha devuelto una respuesta compatible. No se han sustituido los datos.',
  DATASET_UNAVAILABLE: 'La versión del escenario ya no está disponible. Recarga el catálogo y busca de nuevo.',
}
export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly correlationId: string | null
  readonly fieldErrors: { path: string; code: string }[]
  readonly retryAfter: string | null
  constructor(
    status: number,
    code: string,
    correlationId: string | null = null,
    fieldErrors: { path: string; code: string }[] = [],
    retryAfter: string | null = null,
  ) {
    super(messages[code] ?? (status === 409 ? messages.DATASET_UNAVAILABLE : status === 429
      ? 'Se ha alcanzado el límite local. Espera antes de volver a intentarlo.' : status === 503
      ? 'La fuente demo no pudo completar la operación. Puedes volver a intentarlo.' : status === 400 || status === 422
      ? 'Revisa los datos enviados. El servidor ha rechazado la solicitud.' : 'No se pudo completar la operación demo.'))
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.correlationId = correlationId
    this.fieldErrors = fieldErrors
    this.retryAfter = retryAfter
  }
}
type ObjectValue = Record<string, unknown>
const object = (value: unknown): value is ObjectValue => value !== null && typeof value === 'object' && !Array.isArray(value)
const text = (value: unknown): value is string => typeof value === 'string'
const integer = (value: unknown): boolean => Number.isSafeInteger(value) && (value as number) >= 0
const nullableText = (v: unknown): boolean => v === null || text(v)
const strings = (v: unknown): v is string[] => Array.isArray(v) && v.every(text)
const money = (v: unknown): boolean => v === null || (object(v) && text(v.amount) && /^(0|[1-9]\d*)(\.\d{1,4})?$/.test(v.amount) && text(v.currency) && /^[A-Z]{3}$/.test(v.currency))
const issues = (v: unknown): boolean => Array.isArray(v) && v.every(i => object(i) && text(i.code) && nullableText(i.path) && text(i.message))
const allowance = (v: unknown): boolean => object(v) && text(v.status) && (v.pieces === null || integer(v.pieces)) && nullableText(v.weightKg)
const baggage = (v: unknown): boolean => object(v) && allowance(v.cabin) && allowance(v.checked)
const scope = (v: unknown): boolean => object(v) && text(v.basis) && (v.travellerCount === null || integer(v.travellerCount)) && nullableText(v.cabin) && text(v.coverage) && strings(v.coveredSegmentIds)
const price = (v: unknown): boolean => object(v) && money(v.money) && nullableText(v.unknownReason) && text(v.evidenceKind) && scope(v.scope) && text(v.taxes) && text(v.freshness) && text(v.retrievedAt) && nullableText(v.observedAt) && nullableText(v.expiresAt) && Array.isArray(v.additionalCosts) && v.additionalCosts.every(c => object(c) && text(c.kind) && money(c.money) && nullableText(c.unknownReason) && (c.includedInQuotedPrice === null || typeof c.includedInQuotedPrice === 'boolean'))
const time = (v: unknown): boolean => object(v) && ['localDateTime', 'offset', 'zoneId', 'instant', 'unknownReason'].every(k => nullableText(v[k]))
const airline = (v: unknown): boolean => v === null || (object(v) && nullableText(v.code) && nullableText(v.name))
const segment = (v: unknown): boolean => object(v) && text(v.segmentId) && text(v.departureAirport) && text(v.arrivalAirport) && time(v.departureTime) && time(v.arrivalTime) && airline(v.marketingAirline) && airline(v.operatingAirline) && nullableText(v.flightNumber) && (v.technicalStops === null || integer(v.technicalStops)) && (v.durationSeconds === null || integer(v.durationSeconds))
const itinerary = (v: unknown): boolean => object(v) && ['ONE_WAY', 'ROUND_TRIP'].includes(v.tripType as string) && integer(v.connectionCount) && (v.durationSeconds === null || integer(v.durationSeconds)) && Array.isArray(v.journeys) && v.journeys.length > 0 && v.journeys.every(j => object(j) && ['OUTBOUND', 'INBOUND'].includes(j.direction as string) && Array.isArray(j.segments) && j.segments.length > 0 && j.segments.every(segment) && (j.durationSeconds === null || integer(j.durationSeconds)) && Array.isArray(j.connections) && j.connections.every(c => object(c) && integer(c.afterSegmentIndex) && (c.durationSeconds === null || integer(c.durationSeconds)) && typeof c.airportChange === 'boolean' && (c.selfTransfer === null || typeof c.selfTransfer === 'boolean')))
const group = (v: unknown): boolean => object(v) && text(v.groupId) && v.dataOrigin === 'DEMO' && text(v.evidenceKind) && nullableText(v.currency) && scope(v.scope) && text(v.taxes) && baggage(v.baggage) && typeof v.monetarilyComparable === 'boolean' && strings(v.reasons) && Array.isArray(v.additionalCostKinds) && v.additionalCostKinds.every(c => object(c) && text(c.kind) && nullableText(c.currency) && (c.includedInQuotedPrice === null || typeof c.includedInQuotedPrice === 'boolean'))
const query = (v: unknown): boolean => object(v) && ['ONE_WAY', 'ROUND_TRIP'].includes(v.tripType as string) && strings(v.origins) && text(v.destination) && text(v.outboundDate) && nullableText(v.returnDate) && object(v.travellers) && v.travellers.adults === 1 && v.cabin === 'ECONOMY'
const context = (v: unknown): boolean => object(v) && v.dataOrigin === 'DEMO' && text(v.datasetVersion) && text(v.fingerprint) && text(v.evaluationInstant) && query(v.query)
const providers = (v: unknown): boolean => Array.isArray(v) && v.every(p => object(p) && text(p.providerId) && text(p.origin) && text(p.status) && ['requiredUnits', 'completedUnits', 'offerCount', 'observationCount'].every(k => integer(p[k])) && Array.isArray(p.issues) && p.issues.every(i => object(i) && text(i.code) && text(i.message)))
const envelope = (v: unknown): v is ObjectValue => object(v) && v.contractVersion === 'airwise-v1' && text(v.correlationId) && /^[0-9a-f-]{36}$/i.test(v.correlationId)
const catalogResponse = (v: unknown): v is AirportResponse => envelope(v) && object(v.dataset) && ['datasetVersion', 'evaluationInstant', 'referenceDate', 'calendarStart', 'calendarEnd'].every(k => text((v.dataset as ObjectValue)[k])) && strings(v.dataset.airlineCodes) && Array.isArray(v.dataset.currencies) && v.dataset.currencies.every(c => object(c) && text(c.code) && integer(c.minorUnits)) && object(v.dataset.capabilities) && v.dataset.capabilities.providerId === 'DEMO' && Array.isArray(v.items) && v.items.every(a => object(a) && text(a.code) && ['name', 'city', 'countryCode', 'timeZone'].every(k => nullableText(a[k]))) && object(v.pagination) && integer(v.pagination.page) && integer(v.pagination.pageSize) && integer(v.pagination.totalCount) && typeof v.pagination.hasMore === 'boolean'
const observation = (v: unknown): boolean => object(v)
  && text(v.observationId) && text(v.providerId) && v.dataOrigin === 'DEMO'
  && text(v.origin) && text(v.destination)
  && (v.tripType === null || ['ONE_WAY', 'ROUND_TRIP'].includes(v.tripType as string))
  && nullableText(v.outboundDate) && nullableText(v.returnDate)
  && price(v.price) && issues(v.warnings)

const offer = (v: unknown): boolean => object(v)
  && text(v.offerId) && text(v.providerId) && v.dataOrigin === 'DEMO'
  && itinerary(v.itinerary) && price(v.price) && baggage(v.baggage)
  && (v.seller === null || (object(v.seller) && text(v.seller.id) && text(v.seller.name)))
  && text(v.comparisonGroupId) && issues(v.warnings)

const searchResponse = (v: unknown): v is SearchResponse => envelope(v)
  && context(v.context) && ['COMPLETE', 'PARTIAL'].includes(v.status as string)
  && Array.isArray(v.offers) && v.offers.every(offer)
  && Array.isArray(v.priceGroups) && v.priceGroups.every(group)
  && Array.isArray(v.observations) && v.observations.every(observation)
  && integer(v.observationCount) && typeof v.observationsTruncated === 'boolean'
  && object(v.pagination)
  && ['page', 'pageSize', 'baseCount', 'filteredCount', 'excludedUnknownCount'].every(k => integer((v.pagination as ObjectValue)[k]))
  && typeof v.pagination.hasMore === 'boolean'
  && providers(v.providerResults) && issues(v.warnings)
  && Array.isArray(v.errors) && v.errors.every(i => object(i) && text(i.code) && text(i.message))
const comparisonResponse = (v: unknown): v is ComparisonResponse => envelope(v) && context(v.context) && ['FLIGHT_PRICE', 'FLIGHT_DURATION', 'CONNECTIONS', 'KNOWN_SUBTOTAL'].includes(v.primary as string) && strings(v.requiredComponents) && Array.isArray(v.groups) && v.groups.every(g => object(g) && text(g.groupId) && group(g.priceGroup) && typeof g.rankingComplete === 'boolean' && strings(g.recommendationIds) && g.recommendationScope === 'EVALUABLE_GROUP' && Array.isArray(g.rows) && g.rows.every(r => object(r) && text(r.offerId) && (r.rank === null || integer(r.rank)) && typeof r.primaryKnown === 'boolean' && money(r.flightPrice) && money(r.knownSubtotal) && (r.durationSeconds === null || integer(r.durationSeconds)) && integer(r.connectionCount) && strings(r.missingFields) && strings(r.reasons) && Array.isArray(r.groundEstimates) && r.groundEstimates.every(e => object(e) && text(e.offerId) && text(e.component) && money(e.money) && e.money !== null && e.basis === 'GROUP' && e.travellerCount === 1 && e.source === 'USER_ESTIMATE' && e.assumption === 'USER_PROVIDED'))) && issues(v.warnings)

async function request<T>(path: string, validate: (v: unknown) => v is T, body?: unknown, signal?: AbortSignal): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${base}${path}`, { method: body === undefined ? 'GET' : 'POST', credentials: 'omit', cache: 'no-store', redirect: 'error', headers: { Accept: 'application/json', ...(body === undefined ? {} : { 'Content-Type': 'application/json' }) }, ...(body === undefined ? {} : { body: JSON.stringify(body) }), signal })
  } catch (error) {
    if (signal?.aborted || (object(error) && error.name === 'AbortError')) throw error
    throw new ApiError(0, 'NETWORK_ERROR')
  }
  let payload: unknown
  try { payload = response.headers.get('content-type')?.includes('application/json') ? await response.json() : null } catch (error) {
    if (signal?.aborted || (object(error) && error.name === 'AbortError')) throw error
    throw new ApiError(response.status, 'INVALID_RESPONSE')
  }
  if (!response.ok) {
    const errorBody = envelope(payload) ? payload : null
    const code = errorBody && text(errorBody.code) && /^[A-Z_]{1,64}$/.test(errorBody.code) ? errorBody.code : 'HTTP_ERROR'
    const fields = errorBody && Array.isArray(errorBody.fieldErrors) ? errorBody.fieldErrors.filter((f: unknown): f is {path:string;code:string} => object(f) && text(f.path) && text(f.code) && f.path.length <= 128 && /^[A-Z_]{1,64}$/.test(f.code)).slice(0, 32) : []
    const retry = response.headers.get('Retry-After')
    throw new ApiError(response.status, code, errorBody ? errorBody.correlationId as string : null, fields, retry && /^\d{1,6}$/.test(retry) ? retry : null)
  }
  if (!validate(payload)) throw new ApiError(response.status, 'INVALID_RESPONSE')
  return payload
}

/** Fetch every catalog page pinned to the concrete first-page dataset; never silently truncate. */
export async function getAirports(signal?: AbortSignal): Promise<AirportResponse> {
  const first = await request('/airports?page=0&pageSize=50', catalogResponse, undefined, signal)
  const { dataset } = first
  const dates = [dataset.referenceDate, dataset.calendarStart, dataset.calendarEnd]
  if (dates.some(d => !/^\d{4}-\d{2}-\d{2}$/.test(d)) || dates[0] > dates[1] || dates[1] > dates[2]
    || dataset.currencies.length === 0 || dataset.currencies.length > 16
    || dataset.currencies.some(c => !/^[A-Z]{3}$/.test(c.code) || c.minorUnits > 4)
    || new Set(dataset.currencies.map(c => c.code)).size !== dataset.currencies.length) {
    throw new ApiError(200, 'INVALID_RESPONSE')
  }
  const items = [...first.items]
  let current = first
  for (let page = 1; current.pagination.hasMore; page++) {
    if (page > 100 || current.items.length === 0) throw new ApiError(200, 'INVALID_RESPONSE')
    current = await request(`/airports?datasetVersion=${encodeURIComponent(first.dataset.datasetVersion)}&page=${page}&pageSize=50`, catalogResponse, undefined, signal)
    if (current.dataset.datasetVersion !== first.dataset.datasetVersion || current.pagination.page !== page || current.pagination.totalCount !== first.pagination.totalCount) throw new ApiError(200, 'INVALID_RESPONSE')
    items.push(...current.items)
  }
  if (items.length !== first.pagination.totalCount || new Set(items.map(a => a.code)).size !== items.length) throw new ApiError(200, 'INVALID_RESPONSE')
  return { ...first, items, pagination: { ...first.pagination, hasMore: false } }
}
export const searchFlights = (body: SearchRequest, signal?: AbortSignal): Promise<SearchResponse> => request('/flight-searches', searchResponse, body, signal)
export const compareFlights = (body: ComparisonRequest, signal?: AbortSignal): Promise<ComparisonResponse> => request('/comparisons', comparisonResponse, body, signal)
