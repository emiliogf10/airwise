import type { FlightOffer, Journey, Price } from '../api/types'
import { baggageText, date, duration, label, localTime, money, offerTitle, shortId } from '../lib/format'

export function PriceDetails({ price }: { price: Price }) {
  return <><dl className="facts">
    <div><dt>Evidencia del precio</dt><dd>{label(price.evidenceKind)}</dd></div>
    <div><dt>Cobertura</dt><dd>{label(price.scope.coverage)} · {label(price.scope.basis)} · {price.scope.travellerCount === null ? 'Viajeros desconocidos' : `${price.scope.travellerCount} adulto`} · {label(price.scope.cabin)}</dd></div>
    <div><dt>Impuestos</dt><dd>{label(price.taxes)}</dd></div>
    <div><dt>Vigencia simulada</dt><dd>{label(price.freshness)}</dd></div>
    <div><dt>Observado</dt><dd>{price.observedAt ? `${date(price.observedAt)} · ${price.observedAt.slice(11)} UTC` : 'Desconocido'}</dd></div>
    <div><dt>Recuperado en el escenario</dt><dd>{date(price.retrievedAt)} · {price.retrievedAt.slice(11)} UTC</dd></div>
    <div><dt>Caducidad simulada</dt><dd>{price.expiresAt ? `${date(price.expiresAt)} · ${price.expiresAt.slice(11)} UTC` : 'Desconocida'}</dd></div>
  </dl>
    {price.money === null && <p className="hint">Motivo del precio desconocido: {label(price.unknownReason)}.</p>}
    {price.scope.coveredSegmentIds.length > 0 && <p className="small">Segmentos cubiertos: {price.scope.coveredSegmentIds.join(', ')}</p>}
    <h4>Costes adicionales informados</h4>{price.additionalCosts.length === 0 ? <p className="hint">Sin desglose. No significa que los servicios sean gratuitos.</p> : <ul className="plain-list">{price.additionalCosts.map(cost => <li key={cost.kind}>{label(cost.kind)}: {money(cost.money)} · {cost.includedInQuotedPrice === null ? 'inclusión desconocida' : cost.includedInQuotedPrice ? 'incluido en el importe' : 'fuera del importe'}{cost.money === null ? ` · ${label(cost.unknownReason)}` : ''}</li>)}</ul>}
  </>
}

function JourneyDetails({ journey }: { journey: Journey }) {
  return <div className="journey-detail"><h4>{journey.direction === 'OUTBOUND' ? 'Trayecto de ida' : 'Trayecto de vuelta'} · {duration(journey.durationSeconds)}</h4>
    <ol className="segment-list">{journey.segments.map((segment, index) => {
      const connection = journey.connections.find(item => item.afterSegmentIndex === index)
      return <li key={segment.segmentId}>
        <div className="segment-route"><strong>{segment.departureAirport} → {segment.arrivalAirport}</strong><span>{segment.flightNumber ?? 'Número de vuelo desconocido'}</span></div>
        <p>Salida: {localTime(segment.departureTime)}</p><p>Llegada: {localTime(segment.arrivalTime)}</p>
        <p className="small">Comercializadora: {segment.marketingAirline?.name ?? segment.marketingAirline?.code ?? 'Desconocida'} · Operadora: {segment.operatingAirline?.name ?? segment.operatingAirline?.code ?? 'Desconocida'}</p>
        <p className="small">{duration(segment.durationSeconds)} · Paradas técnicas: {segment.technicalStops === null ? 'desconocidas' : segment.technicalStops}</p>
        {connection && <div className="connection">Conexión: {duration(connection.durationSeconds)} · {connection.airportChange ? 'Cambio de aeropuerto' : 'Mismo aeropuerto'} · {connection.selfTransfer === null ? 'Traslado por cuenta propia desconocido' : connection.selfTransfer ? 'Traslado por cuenta propia' : 'Sin traslado por cuenta propia informado'}. Sin garantía de conexión.</div>}
      </li>
    })}</ol>
  </div>
}

interface Props { offer: FlightOffer; selected: boolean; selectionFull: boolean; disabled?: boolean; onSelect: () => void }
export function FlightCard({ offer, selected, selectionFull, disabled, onSelect }: Props) {
  const id = shortId(offer.offerId)
  return <article className={`flight-card${selected ? ' is-selected' : ''}`} aria-labelledby={`offer-${offer.offerId}`}>
    <div className="flight-card-top"><span className="pill pill-soft">DEMO · Datos sintéticos</span><span className="small">Alternativa {id}</span></div>
    <div className="flight-card-main"><div className="flight-itinerary"><h3 id={`offer-${offer.offerId}`}>{offerTitle(offer)}</h3>
      {offer.itinerary.journeys.map(journey => { const first = journey.segments[0]; const last = journey.segments[journey.segments.length - 1]; return <div className="journey-summary" key={journey.direction}>
        <span className="direction">{journey.direction === 'OUTBOUND' ? 'Ida' : 'Vuelta'}</span><div><strong>{first.departureAirport} <span aria-hidden="true">→</span> {last.arrivalAirport}</strong><p>{localTime(first.departureTime)}</p><p>Llegada: {localTime(last.arrivalTime)}</p></div>
        <span className="small">{duration(journey.durationSeconds)}<br />{journey.connections.length} conexión(es)</span>
      </div> })}
      <p className="small">Duración de vuelo total: {duration(offer.itinerary.durationSeconds)} · {offer.itinerary.connectionCount} conexión(es). Excluye estancia y transporte terrestre.</p>
    </div><div className="flight-price"><p className="price">{money(offer.price.money)}</p><p className="small">{label(offer.price.evidenceKind)}</p><p className={`freshness ${offer.price.freshness === 'FRESH' ? '' : 'warning-text'}`}>{label(offer.price.freshness)}</p><p className="small">{label(offer.price.scope.coverage)}</p>
      <div className="choice select-offer"><input type="checkbox" id={`select-${offer.offerId}`} checked={selected} disabled={disabled || (!selected && selectionFull)} onChange={onSelect} /><label htmlFor={`select-${offer.offerId}`}>{`Seleccionar oferta ${id}`}</label></div>
    </div></div>
    <details><summary>Ver itinerario y condiciones <span className="visually-hidden">de la oferta {id}</span></summary><div className="flight-detail-body">
      {offer.itinerary.journeys.map(journey => <JourneyDetails key={journey.direction} journey={journey} />)}
      <h4>Precio y condiciones</h4><PriceDetails price={offer.price} />
      <dl className="facts"><div><dt>Equipaje de cabina</dt><dd>{baggageText(offer.baggage.cabin)}</dd></div><div><dt>Equipaje facturado</dt><dd>{baggageText(offer.baggage.checked)}</dd></div><div><dt>Vendedor de ejemplo</dt><dd>{offer.seller?.name ?? 'Desconocido'}</dd></div><div><dt>Procedencia</dt><dd>Proveedor {offer.providerId} · {offer.dataOrigin} · Ninguna confirmación real de disponibilidad o reserva.</dd></div></dl>
      {offer.warnings.length > 0 && <ul className="issue-list">{offer.warnings.map((warning, index) => <li key={`${warning.code}-${index}`}>{label(warning.code)}</li>)}</ul>}
    </div></details>
  </article>
}
