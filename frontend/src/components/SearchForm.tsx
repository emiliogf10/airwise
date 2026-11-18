import { useRef, useState } from 'react'
import type { FormEvent } from 'react'
import type { Airport, DatasetDescriptor, SearchQuery, TripType } from '../api/types'
import { date } from '../lib/format'

interface Props { airports: Airport[]; dataset: DatasetDescriptor; busy: boolean; onSearch: (query: SearchQuery) => void }
type Errors = Partial<Record<'origins' | 'destination' | 'outboundDate' | 'returnDate', string>>

export function SearchForm({ airports, dataset, busy, onSearch }: Props) {
  const [origins, setOrigins] = useState<string[]>([])
  const [destination, setDestination] = useState('')
  const [tripType, setTripType] = useState<TripType>('ONE_WAY')
  const [outboundDate, setOutboundDate] = useState('')
  const [returnDate, setReturnDate] = useState('')
  const [errors, setErrors] = useState<Errors>({})
  const errorRef = useRef<HTMLDivElement>(null)
  const clear = (field: keyof Errors) => setErrors(current => ({ ...current, [field]: undefined }))
  const inCalendar = (value: string) => /^\d{4}-\d{2}-\d{2}$/.test(value) && value >= dataset.calendarStart && value >= dataset.referenceDate && value <= dataset.calendarEnd

  function submit(event: FormEvent) {
    event.preventDefault()
    const next: Errors = {}
    if (origins.length < 1 || origins.length > 3) next.origins = 'Selecciona entre uno y tres aeropuertos de origen.'
    if (!airports.some(airport => airport.code === destination)) next.destination = 'Selecciona un aeropuerto de destino.'
    else if (origins.includes(destination)) next.destination = 'El destino debe ser diferente de todos los orígenes.'
    if (!inCalendar(outboundDate)) next.outboundDate = 'Elige una fecha de ida dentro del calendario del escenario.'
    if (tripType === 'ROUND_TRIP' && (!inCalendar(returnDate) || returnDate < outboundDate)) next.returnDate = 'Elige una vuelta dentro del calendario, igual o posterior a la ida.'
    setErrors(next)
    if (Object.keys(next).length) { requestAnimationFrame(() => errorRef.current?.focus()); return }
    onSearch({ tripType, origins: [...origins].sort(), destination, outboundDate, returnDate: tripType === 'ROUND_TRIP' ? returnDate : null, travellers: { adults: 1 }, cabin: 'ECONOMY' })
  }

  function example() {
    setOrigins(['OPO', 'SCQ']); setDestination('MAD'); setTripType('ROUND_TRIP')
    setOutboundDate('2027-02-10'); setReturnDate('2027-02-17'); setErrors({})
  }

  const hasExample = dataset.datasetVersion === 'demo-v1' && ['OPO', 'SCQ', 'MAD'].every(code => airports.some(airport => airport.code === code))
  return <search id="buscar" className="search-panel">
    <div className="section-heading"><div><p className="eyebrow">01 / Explora alternativas</p><h2 id="search-title">Tu viaje, con más perspectiva.</h2></div><span className="pill">1 adulto · Turista</span></div>
    <p id="calendar-hint" className="hint">Calendario simulado: {date(dataset.calendarStart)} — {date(dataset.calendarEnd)}. Solo los escenarios con datos devolverán alternativas.</p>
    <form onSubmit={submit} noValidate aria-labelledby="search-title">
      {Object.values(errors).some(Boolean) && <div className="notice notice-error" tabIndex={-1} ref={errorRef}>
        <strong>Revisa la búsqueda</strong><ul>{Object.entries(errors).filter(([, value]) => value).map(([key, value]) => <li key={key}><a href={`#${key === 'origins' ? 'origins-group' : key}`}>{value}</a></li>)}</ul>
      </div>}
      <fieldset disabled={busy} className="form-body">
        <legend className="visually-hidden">Criterios de búsqueda</legend>
        <fieldset id="origins-group" aria-describedby={`origins-hint${errors.origins ? ' origins-error' : ''}`} aria-invalid={Boolean(errors.origins)}>
          <legend>Aeropuertos de origen <span className="required">(obligatorio)</span></legend>
          <p className="hint" id="origins-hint">Compara entre 1 y 3 aeropuertos concretos. No se amplían ciudades automáticamente.</p>
          <div className="airport-options">{airports.map(airport => <div className="choice airport-choice" key={airport.code}>
            <input id={`origin-${airport.code}`} name="origins" type="checkbox" value={airport.code} checked={origins.includes(airport.code)} disabled={!origins.includes(airport.code) && origins.length >= 3} aria-invalid={Boolean(errors.origins)} aria-describedby={errors.origins ? 'origins-error' : 'origins-hint'} onChange={event => { setOrigins(current => event.target.checked ? [...current, airport.code] : current.filter(code => code !== airport.code)); clear('origins'); clear('destination') }} />
            <label htmlFor={`origin-${airport.code}`}><strong>{airport.code}</strong><span>{airport.city ?? airport.name ?? 'Aeropuerto'}</span></label>
          </div>)}</div>
          {errors.origins && <p id="origins-error" className="field-error">{errors.origins}</p>}
        </fieldset>
        <div className="form-grid">
          <div className="field"><label htmlFor="destination">Aeropuerto de destino <span className="required">(obligatorio)</span></label>
            <select id="destination" name="destination" required value={destination} aria-invalid={Boolean(errors.destination)} aria-describedby={errors.destination ? 'destination-error' : undefined} onChange={event => { setDestination(event.target.value); clear('destination') }}>
              <option value="">Elige un destino</option>{airports.map(airport => <option key={airport.code} value={airport.code}>{airport.code} · {airport.city ?? airport.name}</option>)}
            </select>{errors.destination && <p id="destination-error" className="field-error">{errors.destination}</p>}
          </div>
          <fieldset className="trip-options"><legend>Modalidad</legend><div className="inline-choices">
            <div className="choice"><input id="one-way" type="radio" name="tripType" value="ONE_WAY" checked={tripType === 'ONE_WAY'} onChange={() => { setTripType('ONE_WAY'); clear('returnDate') }} /><label htmlFor="one-way">Solo ida</label></div>
            <div className="choice"><input id="round-trip" type="radio" name="tripType" value="ROUND_TRIP" checked={tripType === 'ROUND_TRIP'} onChange={() => setTripType('ROUND_TRIP')} /><label htmlFor="round-trip">Ida y vuelta</label></div>
          </div></fieldset>
          <div className="field"><label htmlFor="outboundDate">Fecha de ida <span className="required">(obligatorio)</span></label>
            <input id="outboundDate" name="outboundDate" type="date" required min={dataset.calendarStart > dataset.referenceDate ? dataset.calendarStart : dataset.referenceDate} max={dataset.calendarEnd} value={outboundDate} aria-invalid={Boolean(errors.outboundDate)} aria-describedby={`calendar-hint${errors.outboundDate ? ' outbound-error' : ''}`} onChange={event => { setOutboundDate(event.target.value); clear('outboundDate'); clear('returnDate') }} />
            {errors.outboundDate && <p id="outbound-error" className="field-error">{errors.outboundDate}</p>}
          </div>
          {tripType === 'ROUND_TRIP' && <div className="field"><label htmlFor="returnDate">Fecha de vuelta <span className="required">(obligatorio)</span></label>
            <input id="returnDate" name="returnDate" type="date" required min={outboundDate || dataset.calendarStart} max={dataset.calendarEnd} value={returnDate} aria-invalid={Boolean(errors.returnDate)} aria-describedby={`calendar-hint${errors.returnDate ? ' return-error' : ''}`} onChange={event => { setReturnDate(event.target.value); clear('returnDate') }} />
            {errors.returnDate && <p id="return-error" className="field-error">{errors.returnDate}</p>}
          </div>}
        </div>
        <div className="form-actions"><button type="submit" className="button">{busy ? 'Buscando…' : 'Buscar vuelos'} <span aria-hidden="true">↗</span></button>{hasExample && <button type="button" className="button button-secondary" onClick={example}>Cargar ejemplo demo</button>}<span className="hint">Sin reservas. Sin disponibilidad real.</span></div>
      </fieldset>
    </form>
  </search>
}
