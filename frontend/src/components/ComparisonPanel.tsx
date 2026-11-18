import { useRef, useState } from 'react'
import type { FormEvent } from 'react'
import type { ComparisonRequest, ComparisonResponse, DatasetDescriptor, FlightOffer, GroundComponent, GroundEstimate, PrimaryCriterion, SearchContext } from '../api/types'
import { baggageText, duration, label, money, offerTitle, shortId, validDecimal } from '../lib/format'

interface Props { selected: FlightOffer[]; context: SearchContext; dataset: DatasetDescriptor; result: ComparisonResponse | null; busy: boolean; onRemove: (id: string) => void; onCompare: (request: ComparisonRequest) => void; onInvalidate: () => void }
interface EstimateDraft { amount: string; currency: string }

export function ComparisonPanel({ selected, context, dataset, result, busy, onRemove, onCompare, onInvalidate }: Props) {
  const [primary, setPrimary] = useState<PrimaryCriterion>('FLIGHT_PRICE')
  const [components, setComponents] = useState<GroundComponent[]>([])
  const [estimates, setEstimates] = useState<Record<string, EstimateDraft>>({})
  const [error, setError] = useState('')
  const errorRef = useRef<HTMLParagraphElement>(null)
  const key = (offerId: string, component: GroundComponent) => `${offerId}:${component}`
  const draft = (offer: FlightOffer, component: GroundComponent): EstimateDraft => estimates[key(offer.offerId, component)] ?? { amount: '', currency: offer.price.money?.currency ?? dataset.currencies[0].code }
  function changeEstimate(offer: FlightOffer, component: GroundComponent, update: Partial<EstimateDraft>) { onInvalidate(); setEstimates(current => ({ ...current, [key(offer.offerId, component)]: { ...draft(offer, component), ...update } })); setError('') }
  function submit(event: FormEvent) {
    event.preventDefault()
    let message = selected.length < 2 || selected.length > 4 ? 'Selecciona entre dos y cuatro ofertas de la misma búsqueda.' : ''
    const groundEstimates: GroundEstimate[] = []
    if (primary === 'KNOWN_SUBTOTAL') {
      if (components.length === 0) message = 'Selecciona al menos un componente terrestre para comparar subtotales.'
      for (const offer of selected) for (const component of components) {
        const value = draft(offer, component)
        if (value.amount === '') continue // Missing stays missing; it is never converted into a zero estimate.
        const currency = dataset.currencies.find(item => item.code === value.currency)
        if (!currency || !validDecimal(value.amount, currency.minorUnits, '1000000')) message = 'Cada estimación debe ser un decimal con punto, sin comas ni exponentes, de 0 a 1.000.000 y con la precisión de la moneda.'
        else if (offer.price.money && offer.price.money.currency !== value.currency) message = 'La estimación debe usar la misma moneda que el importe de vuelo. No se convierten monedas.'
        else groundEstimates.push({ offerId: offer.offerId, component, money: { amount: value.amount, currency: value.currency }, basis: 'GROUP', travellerCount: 1, source: 'USER_ESTIMATE', assumption: 'USER_PROVIDED' })
      }
    }
    setError(message)
    if (message) { requestAnimationFrame(() => errorRef.current?.focus()); return }
    onCompare({ query: context.query, datasetVersion: context.datasetVersion, offerIds: selected.map(offer => offer.offerId), primary, requiredComponents: primary === 'KNOWN_SUBTOTAL' ? [...components].sort() : [], groundEstimates })
  }
  return <>
    <div className="selection-box"><p><strong>{selected.length} de 4 alternativas seleccionadas</strong></p><p className="hint">Se conservan al filtrar y cambiar de página. Cambiar la búsqueda o la versión inicia una selección nueva.</p>
      {selected.length > 0 && <ul className="selection-list">{selected.map(offer => <li key={offer.offerId}><div><strong>{offerTitle(offer)}</strong><span className="small"> · {shortId(offer.offerId)} · {money(offer.price.money)}</span></div><button type="button" className="text-button" disabled={busy} aria-label={`Quitar oferta ${shortId(offer.offerId)}`} onClick={() => onRemove(offer.offerId)}>Quitar</button></li>)}</ul>}
    </div>
    {selected.length < 2 ? <p className="notice">Selecciona al menos dos ofertas de los resultados para abrir una comparación. Las observaciones sin itinerario no son seleccionables.</p> : <form onSubmit={submit} noValidate aria-label="Comparador de alternativas">
      {error && <p id="comparison-validation" tabIndex={-1} ref={errorRef} className="notice notice-error">{error}</p>}
      <fieldset disabled={busy}><legend className="visually-hidden">Preferencias de comparación</legend><div className="field criterion-field"><label htmlFor="primary-criterion">Criterio de comparación</label><select id="primary-criterion" name="primary" value={primary} onChange={event => { onInvalidate(); setPrimary(event.target.value as PrimaryCriterion); setError('') }}><option value="FLIGHT_PRICE">Precio del vuelo</option><option value="FLIGHT_DURATION">Duración de vuelo</option><option value="CONNECTIONS">Número de conexiones</option><option value="KNOWN_SUBTOTAL">Subtotal conocido con estimaciones</option></select></div>
        {primary === 'KNOWN_SUBTOTAL' && <div className="ground-panel"><fieldset aria-invalid={Boolean(error) && components.length === 0} aria-describedby={error && components.length === 0 ? 'comparison-validation' : undefined}><legend>Componentes terrestres requeridos</legend><p className="hint">Importes del grupo de 1 adulto aportados por ti. Un campo vacío significa desconocido. No son tarifas investigadas y no se guarda información personal.</p><div className="inline-choices">{(['ORIGIN_GROUND_OUTBOUND', ...(context.query.tripType === 'ROUND_TRIP' ? ['ORIGIN_GROUND_RETURN'] : [])] as GroundComponent[]).map(component => <div className="choice" key={component}><input id={`component-${component}`} name="requiredComponents" type="checkbox" checked={components.includes(component)} onChange={event => { onInvalidate(); setComponents(current => event.target.checked ? [...current, component] : current.filter(value => value !== component)); setError('') }} /><label htmlFor={`component-${component}`}>{label(component)}</label></div>)}</div></fieldset>
          {selected.map(offer => <fieldset className="estimate-offer" key={offer.offerId}><legend>{offerTitle(offer)} · {shortId(offer.offerId)}</legend>{components.map(component => { const value = draft(offer, component); const id = `estimate-${offer.offerId}-${component}`; return <div className="estimate-row" key={component}><div className="field"><label htmlFor={id}>{label(component)} · {shortId(offer.offerId)}</label><input id={id} name={id} type="text" inputMode="decimal" spellCheck={false} value={value.amount} aria-invalid={Boolean(error) && value.amount !== ''} aria-describedby={`estimate-hint${error ? ' comparison-validation' : ''}`} onChange={event => changeEstimate(offer, component, { amount: event.target.value })} /></div><div className="field"><label htmlFor={`${id}-currency`}>Moneda · {label(component)} · {shortId(offer.offerId)}</label><select id={`${id}-currency`} name={`${id}-currency`} aria-invalid={Boolean(error) && value.amount !== ''} aria-describedby={error ? 'comparison-validation' : undefined} value={value.currency} onChange={event => changeEstimate(offer, component, { currency: event.target.value })}>{dataset.currencies.map(currency => <option key={currency.code} value={currency.code}>{currency.code}</option>)}</select></div></div> })}</fieldset>)}
          <p className="hint" id="estimate-hint">Decimal con punto, máximo 1.000.000 por componente. El servidor calcula únicamente el subtotal de componentes seleccionados con estimaciones; equipaje y otros costes desconocidos siguen desconocidos.</p>
        </div>}
        <button type="submit" className="button">{busy ? 'Comparando…' : 'Comparar selección'} <span aria-hidden="true">↗</span></button>
      </fieldset>
    </form>}
    {result && <ComparisonResults result={result} selected={selected} />}
  </>
}

function ComparisonResults({ result, selected }: { result: ComparisonResponse; selected: FlightOffer[] }) {
  return <div className="comparison-results"><p className="notice">Clasificación del servidor por grupos compatibles. No existe un ganador global entre monedas, evidencias o condiciones diferentes. Una recomendación solo se refiere a las alternativas evaluables del grupo.</p>
    {result.warnings.length > 0 && <ul className="issue-list">{result.warnings.map((issue, index) => <li key={index}>{label(issue.code)}</li>)}</ul>}
    {result.groups.map((group, index) => <section className="comparison-group" key={group.groupId}><div className="section-heading"><h3>Grupo compatible {index + 1}</h3><span className="pill pill-soft">{group.priceGroup.currency ?? 'Moneda desconocida'} · DEMO</span></div>
      <p className="small">{label(group.priceGroup.evidenceKind)} · {label(group.priceGroup.scope.coverage)} · {label(group.priceGroup.scope.basis)} · Impuestos: {label(group.priceGroup.taxes)}</p>
      <p className="small">{group.rankingComplete ? 'Clasificación completa dentro del grupo.' : 'Clasificación parcial: revisa los motivos y valores desconocidos.'}{!group.priceGroup.monetarilyComparable && ' Este grupo no es comparable monetariamente.'}</p>
      {group.priceGroup.reasons.length > 0 && <p className="small">{group.priceGroup.reasons.map(label).join(' · ')}</p>}
      <details><summary>Condiciones que definen este grupo</summary><dl className="facts"><div><dt>Equipaje de cabina</dt><dd>{baggageText(group.priceGroup.baggage.cabin)}</dd></div><div><dt>Equipaje facturado</dt><dd>{baggageText(group.priceGroup.baggage.checked)}</dd></div><div><dt>Viajeros y cabina</dt><dd>{group.priceGroup.scope.travellerCount ?? 'Desconocido'} · {label(group.priceGroup.scope.cabin)}</dd></div></dl>{group.priceGroup.additionalCostKinds.length ? <ul>{group.priceGroup.additionalCostKinds.map(cost => <li key={cost.kind}>{label(cost.kind)} · {cost.includedInQuotedPrice === null ? 'inclusión desconocida' : cost.includedInQuotedPrice ? 'incluido en el precio' : 'no incluido'} · {cost.currency ?? 'moneda desconocida'}</li>)}</ul> : <p className="hint">Sin desglose de costes adicionales; no acredita gratuidad.</p>}</details>
      <div className="comparison-rows">{group.rows.map(row => { const offer = selected.find(value => value.offerId === row.offerId); const recommended = group.recommendationIds.includes(row.offerId); return <article className="comparison-row" key={row.offerId}>
        <div className="rank">{row.rank === null ? '—' : `#${row.rank}`}<span>{row.rank === null ? 'Sin clasificar' : 'Rango en el grupo'}</span></div><div><h4>{offer ? offerTitle(offer) : `Alternativa ${shortId(row.offerId)}`} <span className="small">{shortId(row.offerId)}</span></h4>{recommended && <p className="recommendation">Recomendada entre las alternativas evaluables de este grupo</p>}
          <dl className="comparison-metrics"><div><dt>Precio de vuelo</dt><dd>{money(row.flightPrice)}</dd></div><div><dt>Duración de vuelo</dt><dd>{duration(row.durationSeconds)}</dd></div><div><dt>Conexiones</dt><dd>{row.connectionCount}</dd></div>{result.primary === 'KNOWN_SUBTOTAL' && <div><dt>Subtotal de componentes seleccionados con estimaciones</dt><dd>{row.knownSubtotal === null ? 'Subtotal desconocido' : money(row.knownSubtotal)}</dd></div>}</dl>
          {row.groundEstimates.length > 0 && <ul className="small">{row.groundEstimates.map(estimate => <li key={estimate.component}>{label(estimate.component)}: {money(estimate.money)} · Estimación manual de usuario para el grupo de 1 adulto.</li>)}</ul>}
          {row.missingFields.length > 0 && <p className="warning-text">Datos ausentes: {row.missingFields.map(label).join(' · ')}</p>}
          {row.reasons.length > 0 && <ul className="issue-list">{row.reasons.map(reason => <li key={reason}>{label(reason)}</li>)}</ul>}
          {!row.primaryKnown && <p className="small">El valor del criterio principal es desconocido.</p>}
        </div></article> })}</div>
    </section>)}
  </div>
}
