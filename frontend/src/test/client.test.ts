import { describe, expect, it, vi } from 'vitest'
import { ApiError, compareFlights, getAirports, searchFlights } from '../api/client'
import type { ComparisonRequest, SearchRequest } from '../api/types'
import { airportFixture, comparisonFixture, correlationId, errorFixture, jsonResponse, offerId, queryFixture, searchFixture } from './fixtures'

function searchRequest(): SearchRequest {
  return {
    query: queryFixture(), datasetVersion: 'demo-v1',
    filters: { origins: [], airlineCodes: [], maxConnections: null, maxDurationMinutes: null, currency: 'EUR', maxPrice: '99.90' },
    sort: 'PRICE_ASC', page: 1, pageSize: 5,
  }
}

describe('cliente HTTP demo', () => {
  it('obtiene el catálogo por GET relativo y conserva el calendario sintético del servidor', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(airportFixture()))
    vi.stubGlobal('fetch', fetchMock)
    const result = await getAirports()
    expect(result.dataset.calendarStart).toBe('2027-01-01')
    expect(result.items.map(({ code }) => code)).toEqual(['BCN', 'MAD', 'OPO', 'SCQ'])
    const [url, options] = fetchMock.mock.calls[0]
    expect(String(url)).toMatch(/^\/api\/v1\/demo\/airports(?:\?|$)/)
    expect(options?.method ?? 'GET').toBe('GET')
  })

  it('envía filtros, orden y página al servidor sin convertir importes decimales', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(searchFixture()))
    vi.stubGlobal('fetch', fetchMock)
    const request = searchRequest()
    await searchFlights(request)
    const [url, options] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/v1/demo/flight-searches')
    expect(options.method).toBe('POST')
    expect(JSON.parse(options.body)).toEqual(request)
    expect(JSON.parse(options.body).filters.maxPrice).toBe('99.90')
    expect(new Headers(options.headers).get('Content-Type')).toContain('application/json')
  })

  it('envía comparación con IDs y estimaciones explícitas, sin precios de vuelo calculados', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(comparisonFixture()))
    vi.stubGlobal('fetch', fetchMock)
    const request: ComparisonRequest = {
      query: queryFixture(), datasetVersion: 'demo-v1', offerIds: [offerId(1), offerId(2)],
      primary: 'KNOWN_SUBTOTAL', requiredComponents: ['ORIGIN_GROUND_OUTBOUND'],
      groundEstimates: [{ offerId: offerId(1), component: 'ORIGIN_GROUND_OUTBOUND', money: { amount: '12.30', currency: 'EUR' }, basis: 'GROUP', travellerCount: 1, source: 'USER_ESTIMATE', assumption: 'USER_PROVIDED' }],
    }
    await compareFlights(request)
    const [url, options] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/v1/demo/comparisons')
    expect(options.method).toBe('POST')
    expect(JSON.parse(options.body)).toEqual(request)
    expect(options.body).not.toContain('flightPrice')
  })

  it('mantiene la versión fijada al recorrer páginas del catálogo', async () => {
    const first = airportFixture()
    first.items = first.items.slice(0, 2)
    first.pagination = { page: 0, pageSize: 2, totalCount: 4, hasMore: true }
    const second = airportFixture()
    second.items = second.items.slice(2)
    second.pagination = { page: 1, pageSize: 2, totalCount: 4, hasMore: false }
    const fetchMock = vi.fn().mockResolvedValueOnce(jsonResponse(first)).mockResolvedValueOnce(jsonResponse(second))
    vi.stubGlobal('fetch', fetchMock)
    expect((await getAirports()).items).toHaveLength(4)
    const continuation = new URL(String(fetchMock.mock.calls[1][0]), 'http://127.0.0.1')
    expect(continuation.searchParams.get('datasetVersion')).toBe('demo-v1')
    expect(continuation.searchParams.get('page')).toBe('1')
  })

  it('preserva cancelación sin transformarla en resultado vacío', async () => {
    const controller = new AbortController()
    const aborted = new DOMException('Cancelado', 'AbortError')
    const fetchMock = vi.fn().mockRejectedValue(aborted)
    vi.stubGlobal('fetch', fetchMock)
    await expect(searchFlights(searchRequest(), controller.signal)).rejects.toMatchObject({ name: 'AbortError' })
    expect(fetchMock.mock.calls[0][1].signal).toBe(controller.signal)
  })

  it('preserva una cancelación durante la lectura del cuerpo de respuesta', async () => {
    const controller = new AbortController()
    const response = jsonResponse(searchFixture())
    vi.spyOn(response, 'json').mockImplementation(async () => {
      controller.abort()
      throw new DOMException('Cancelado durante lectura', 'AbortError')
    })
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response))
    await expect(searchFlights(searchRequest(), controller.signal)).rejects.toMatchObject({ name: 'AbortError' })
  })

  it.each([403, 409, 422, 429, 500, 503])('expone un error HTTP %i sin eco de mensajes del servidor', async (status) => {
    const body = { ...errorFixture(status === 403 ? 'ACCESS_DENIED' : 'PROVIDERS_FAILED'), message: '<img src=x onerror=alert(1)>internal-secret-marker' }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(body, status)))
    const failure = await searchFlights(searchRequest()).catch((error: unknown) => error)
    expect(failure).toBeInstanceOf(ApiError)
    expect(failure).toMatchObject({ status, correlationId })
    expect((failure as Error).message).not.toMatch(/internal-secret-marker|<img|onerror/)
  })

  it('sanitiza HTML de un proxy y no lo interpreta como respuesta vacía', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('<html>internal-stack-marker</html>', { status: 502, headers: { 'Content-Type': 'text/html' } })))
    const failure = await getAirports().catch((error: unknown) => error)
    expect(failure).toBeInstanceOf(ApiError)
    expect((failure as Error).message).not.toMatch(/internal-stack-marker|<html>/)
  })

  it('rechaza una versión de contrato ajena en vez de proyectar datos incompatibles', async () => {
    const response = { ...airportFixture(), contractVersion: 'unknown-v9' }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(response)))
    await expect(getAirports()).rejects.toBeInstanceOf(ApiError)
  })

  it('rechaza un importe numérico del servidor en vez de aceptar precisión flotante', async () => {
    const body = searchFixture()
    const malformed: unknown = { ...body, offers: [{ ...body.offers[0], price: { ...body.offers[0].price, money: { amount: 0.1, currency: 'EUR' } } }] }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(malformed)))
    await expect(searchFlights(searchRequest())).rejects.toMatchObject({ code: 'INVALID_RESPONSE' })
  })

  it('rechaza un catálogo que mezcle versiones durante la paginación', async () => {
    const first = airportFixture()
    first.items = first.items.slice(0, 2)
    first.pagination.hasMore = true
    const second = airportFixture()
    second.dataset.datasetVersion = 'demo-other'
    second.items = second.items.slice(2)
    second.pagination.page = 1
    vi.stubGlobal('fetch', vi.fn().mockResolvedValueOnce(jsonResponse(first)).mockResolvedValueOnce(jsonResponse(second)))
    await expect(getAirports()).rejects.toMatchObject({ code: 'INVALID_RESPONSE' })
  })

  it('rechaza aeropuertos duplicados entre páginas en vez de ocultar una truncación', async () => {
    const body = airportFixture()
    body.items[1] = { ...body.items[0] }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(body)))
    await expect(getAirports()).rejects.toMatchObject({ code: 'INVALID_RESPONSE' })
  })

  it('no convierte un fallo de red en resultados demo ni expone su detalle operativo', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('internal-network-marker')))
    const failure = await searchFlights(searchRequest()).catch((error: unknown) => error)
    expect(failure).toBeInstanceOf(ApiError)
    expect((failure as Error).message).not.toContain('internal-network-marker')
  })
})
