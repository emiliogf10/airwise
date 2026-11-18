// Explicit local integration check. No external URLs, credentials or mocked responses.
import assert from 'node:assert/strict'
import { fileURLToPath } from 'node:url'
import { createServer } from 'vite'

const nativeFetch = globalThis.fetch
const origin = 'http://127.0.0.1:5173'
const allowed = new Set(['/api/v1/demo/airports', '/api/v1/demo/flight-searches', '/api/v1/demo/comparisons'])
const server = await createServer({ root: fileURLToPath(new URL('../', import.meta.url)), configFile: false, server: { middlewareMode: true } })
let requests = 0
globalThis.fetch = (path, options) => {
  const url = new URL(path, origin)
  assert.equal(url.origin, origin)
  assert(allowed.has(url.pathname))
  requests++
  return nativeFetch(url, { ...options, headers: { ...options.headers, Origin: origin } })
}
try {
  const { getAirports, searchFlights, compareFlights } = await server.ssrLoadModule('/src/api/client.ts')
  const catalog = await getAirports()
  assert.equal(catalog.dataset.datasetVersion, 'demo-v1')
  assert.equal(catalog.items.length, 4)
  const query = { tripType: 'ROUND_TRIP', origins: ['OPO', 'SCQ'], destination: 'MAD', outboundDate: '2027-02-10', returnDate: '2027-02-17', travellers: { adults: 1 }, cabin: 'ECONOMY' }
  const view = { query, datasetVersion: catalog.dataset.datasetVersion, filters: { origins: [], airlineCodes: [], maxConnections: null, maxDurationMinutes: null, currency: null, maxPrice: null }, sort: 'DEFAULT', page: 0, pageSize: 2 }
  const search = await searchFlights(view)
  assert.equal(search.offers.length, 2)
  assert(search.offers.every(o => o.itinerary.journeys.length === 2))
  assert.equal(search.observationCount, 1)
  const selected = search.offers.map(o => o.offerId)
  for (const primary of ['FLIGHT_PRICE', 'FLIGHT_DURATION', 'CONNECTIONS', 'KNOWN_SUBTOTAL']) {
    const estimate = primary === 'KNOWN_SUBTOTAL'
    const result = await compareFlights({ query, datasetVersion: view.datasetVersion, offerIds: selected, primary, requiredComponents: estimate ? ['ORIGIN_GROUND_OUTBOUND'] : [], groundEstimates: estimate ? selected.map(offerId => ({ offerId, component: 'ORIGIN_GROUND_OUTBOUND', money: { amount: '20.00', currency: 'EUR' }, basis: 'GROUP', travellerCount: 1, source: 'USER_ESTIMATE', assumption: 'USER_PROVIDED' })) : [] })
    assert.equal(result.primary, primary)
    assert.equal(result.context.fingerprint, search.context.fingerprint)
    assert.equal(result.groups.flatMap(g => g.rows).length, 2)
    assert(result.groups.every(g => g.recommendationScope === 'EVALUABLE_GROUP'))
    if (estimate) assert.deepEqual(result.groups[0].rows.map(r => r.knownSubtotal.amount).sort(), ['120.00', '130.00'])
  }
  const oneWay = { ...query, tripType: 'ONE_WAY', origins: ['OPO'], outboundDate: '2027-02-12', returnDate: null }
  const first = await searchFlights({ ...view, query: oneWay, pageSize: 5 })
  const second = await searchFlights({ ...view, query: oneWay, page: 1, pageSize: 5 })
  assert.equal(first.pagination.baseCount, 8)
  assert.equal(first.context.fingerprint, second.context.fingerprint)
  const amounts = [...first.offers, ...second.offers].map(o => o.price.money?.amount ?? null)
  assert(amounts.includes('0.00') && amounts.includes(null))
  const empty = await searchFlights({ ...view, query: { ...oneWay, outboundDate: '2027-02-14' } })
  assert.equal(empty.pagination.baseCount, 0)
  assert.equal(empty.status, 'COMPLETE')
  const partial = await searchFlights({ ...view, query: { ...query, outboundDate: '2027-02-11', returnDate: '2027-02-18' } })
  assert.equal(partial.status, 'PARTIAL')
  await assert.rejects(searchFlights({ ...view, query: { ...oneWay, outboundDate: '2027-02-13' } }), error => error.status === 503 && error.code === 'PROVIDERS_FAILED')
  await assert.rejects(searchFlights({ ...view, datasetVersion: 'demo-retired' }), error => error.status === 409)
  console.log(`Local integration OK: ${requests} real HTTP requests through Vite, 3 endpoints, 4 criteria, estimates, paging, unknown/zero, partial/empty and 409/503. Not a browser E2E test.`)
} finally {
  globalThis.fetch = nativeFetch
  await server.close()
}
