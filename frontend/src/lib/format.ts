import type { Allowance, FlightOffer, Money } from '../api/types'

export function baggageText(value: Allowance): string {
  if (value.status === 'NOT_INCLUDED') return 'No incluido; precio de compra desconocido'
  if (value.status !== 'INCLUDED') return 'Desconocido; no implica gratuidad'
  return `Incluido · ${value.pieces === null ? 'piezas desconocidas' : `${value.pieces} pieza(s)`} · ${value.weightKg === null ? 'peso desconocido' : `${value.weightKg} kg`}`
}

/** Money is formatted from its decimal string, without floating-point conversion. */
export function money(value: Money | null): string {
  if (value === null) return 'Precio desconocido'
  const [whole, fraction] = value.amount.split('.')
  return `${whole.replace(/\B(?=(\d{3})+(?!\d))/g, '.')}${fraction ? `,${fraction}` : ''} ${value.currency}`
}

export function duration(seconds: number | null): string {
  if (seconds === null) return 'Duración desconocida'
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  return `${hours ? `${hours} h ` : ''}${minutes % 60} min`
}

export function date(value: string | null): string {
  if (!value) return 'Fecha desconocida'
  const [year, month, day] = value.slice(0, 10).split('-')
  return `${day}/${month}/${year}`
}

export function localTime(value: { localDateTime: string | null; offset: string | null; zoneId: string | null }): string {
  if (!value.localDateTime) return 'Horario desconocido'
  return `${date(value.localDateTime)} · ${value.localDateTime.slice(11, 16)}${value.zoneId ? ` · ${value.zoneId}` : ''}${value.offset ? ` (UTC${value.offset === 'Z' ? '' : value.offset})` : ''}`
}

export function offerTitle(offer: FlightOffer): string {
  const journey = offer.itinerary.journeys[0]
  const first = journey.segments[0]
  const last = journey.segments[journey.segments.length - 1]
  return `${first.departureAirport} → ${last.arrivalAirport}`
}

export function shortId(offerId: string): string { return offerId.slice(-8) }

const labels: Record<string, string> = {
  OBSERVED_CACHE: 'Precio observado en caché', RECENT_SEARCH: 'Resultado reciente de búsqueda',
  SELLER_ADVERTISED: 'Precio anunciado por vendedor', BOOKING_CONFIRMED: 'Confirmación de reserva',
  ESTIMATE: 'Estimación', UNKNOWN: 'Desconocido', FRESH: 'Vigente en el escenario simulado',
  EXPIRED: 'Caducado en el escenario simulado', FULL_ITINERARY: 'Itinerario completo',
  OUTBOUND_ONLY: 'Solo ida', INBOUND_ONLY: 'Solo vuelta', SEGMENT_ONLY: 'Solo segmentos indicados',
  PER_PERSON: 'Por persona', GROUP: 'Grupo de 1 adulto', ECONOMY: 'Turista',
  INCLUDED: 'Incluido', NOT_INCLUDED: 'No incluido', PARTIAL: 'Parcial',
  TAXES: 'Impuestos', CABIN_BAGGAGE: 'Equipaje de cabina', CHECKED_BAGGAGE: 'Equipaje facturado',
  SELLER_FEE: 'Comisión del vendedor', OTHER: 'Otros costes',
  PRICE_UNKNOWN: 'Importe de vuelo desconocido', PRICE_SCOPE_UNKNOWN: 'Cobertura del precio incompleta o desconocida',
  CURRENCY_UNKNOWN: 'Moneda desconocida', TIME_UNKNOWN: 'Horario desconocido',
  TIME_AMBIGUOUS: 'Horario local ambiguo', EXPIRED_DATA: 'Datos caducados en el escenario',
  FRESHNESS_UNKNOWN: 'Vigencia desconocida', BAGGAGE_UNKNOWN: 'Información de equipaje desconocida',
  ADDITIONAL_COST_UNKNOWN: 'Coste adicional desconocido', INVALID_PROVIDER_DATA: 'El conjunto reconstruido contiene datos de fuente no utilizables',
  CONNECTION_UNVERIFIED: 'Conexión sin garantía de traslado', OBSERVATION_ONLY: 'Observación sin itinerario completo',
  EVIDENCE_UNSUPPORTED: 'Esta evidencia no permite clasificar por importe', DURATION_UNKNOWN: 'Duración desconocida',
  GROUND_COMPONENT_MISSING: 'Falta una estimación terrestre requerida', INCOMPATIBLE_CURRENCY: 'Monedas incompatibles',
  ADDITIONAL_COSTS_NOT_INCLUDED: 'Hay costes adicionales sin incluir', SINGLE_EVALUABLE_OPTION: 'Solo una alternativa evaluable; no hay recomendación',
  TIE: 'Empate', TIE_UNRESOLVED: 'Empate sin resolver por datos desconocidos',
  PRICE: 'Precio', PRICE_SCOPE: 'Cobertura del precio', DURATION: 'Duración', FRESHNESS: 'Vigencia',
  ORIGIN_GROUND_OUTBOUND: 'Traslado al aeropuerto de ida', ORIGIN_GROUND_RETURN: 'Traslado desde el aeropuerto de regreso',
  PROVIDER_TIMEOUT: 'Tiempo de respuesta agotado', PROVIDER_FAILED: 'La fuente no pudo completar la búsqueda',
  CAPABILITY_UNAVAILABLE: 'Modalidad no disponible en este escenario', UNSUPPORTED: 'No compatible',
  COMPLETE: 'Completa', EMPTY: 'Sin datos', FAILED: 'Fallida', NOT_RETURNED: 'No informado',
  SCOPE_UNKNOWN: 'Cobertura desconocida', TIME_ZONE_UNKNOWN: 'Zona horaria desconocida',
  AMBIGUOUS_LOCAL_TIME: 'Hora local ambigua', INVALID_SOURCE_VALUE: 'Dato de fuente no utilizable',
  NOT_ITEMIZED: 'Sin desglose',
}

export function label(code: string | null | undefined): string {
  return code ? labels[code] ?? 'Información no especificada' : 'Desconocido'
}

export function validDecimal(value: string, minorUnits: number, maximumWhole: string, exclusive = false): boolean {
  if (!/^(0|[1-9]\d*)(\.\d+)?$/.test(value)) return false
  const [whole, fraction = ''] = value.split('.')
  if (fraction.length > minorUnits || whole.length > maximumWhole.length) return false
  if (whole.length < maximumWhole.length) return true
  if (whole < maximumWhole) return true
  if (whole > maximumWhole || exclusive) return false
  return !/[1-9]/.test(fraction)
}
