import { useRef, useState } from 'react'
import type { FormEvent } from 'react'
import type { DatasetDescriptor, SearchFilters as Filters, SearchQuery, SearchSort } from '../api/types'
import { validDecimal } from '../lib/format'
import { emptyFilters } from '../lib/search'

interface Props { query: SearchQuery; dataset: DatasetDescriptor; initial: Filters; sort: SearchSort; pageSize: number; busy: boolean; onApply: (filters: Filters, sort: SearchSort, pageSize: number) => void }

export function SearchFilters({ query, dataset, initial, sort: initialSort, pageSize: initialPageSize, busy, onApply }: Props) {
  const [filters, setFilters] = useState(initial)
  const [sort, setSort] = useState(initialSort)
  const [pageSize, setPageSize] = useState(initialPageSize)
  const [maxDuration, setMaxDuration] = useState(initial.maxDurationMinutes?.toString() ?? '')
  const [error, setError] = useState('')
  const [errorField, setErrorField] = useState<string | null>(null)
  const errorRef = useRef<HTMLParagraphElement>(null)
  function submit(event: FormEvent) {
    event.preventDefault()
    const currency = dataset.currencies.find(value => value.code === filters.currency)
    const minutes = maxDuration === '' ? null : Number(maxDuration)
    const priceError = filters.maxPrice !== null && filters.maxPrice !== '' && (!currency || !validDecimal(filters.maxPrice, currency.minorUnits, '1000000000', true))
    if ((minutes !== null && (!/^\d+$/.test(maxDuration) || !Number.isInteger(minutes) || minutes < 1 || minutes > 10080)) || priceError) {
      setErrorField(priceError ? 'max-price' : 'max-duration')
      setError(priceError ? 'El precio máximo necesita una moneda y un decimal con punto, sin comas ni exponentes, menor de 1.000.000.000 y con la precisión de esa moneda.' : 'La duración máxima debe ser un número entero entre 1 y 10080 minutos.')
      requestAnimationFrame(() => errorRef.current?.focus()); return
    }
    setError(''); onApply({ ...filters, maxDurationMinutes: minutes, maxPrice: filters.maxPrice || null }, sort, pageSize)
  }
  const toggle = (key: 'origins' | 'airlineCodes', value: string, checked: boolean) => setFilters(current => ({ ...current, [key]: checked ? [...current[key], value] : current[key].filter(item => item !== value) }))
  function reset() {
    const clean = emptyFilters(); setFilters(clean); setSort('DEFAULT'); setPageSize(20); setMaxDuration(''); setError(''); onApply(clean, 'DEFAULT', 20)
  }
  return <search className="filter-panel"><details open>
    <summary>Filtrar y ordenar <span className="hint">Se aplica a todos los resultados del servidor</span></summary>
    <form onSubmit={submit} noValidate aria-label="Filtros de resultados">
      {error && <p id="filter-error" className="notice notice-error" tabIndex={-1} ref={errorRef}>{error}</p>}
      <fieldset disabled={busy}><legend className="visually-hidden">Filtros y ordenación</legend>
        <div className="filter-grid">
          <fieldset><legend>Origen de las ofertas</legend><div className="inline-choices">{query.origins.map(origin => <div className="choice" key={origin}><input id={`filter-origin-${origin}`} type="checkbox" name="filterOrigins" checked={filters.origins.includes(origin)} onChange={event => toggle('origins', origin, event.target.checked)} /><label htmlFor={`filter-origin-${origin}`}>{origin}</label></div>)}</div><p className="hint">Sin selección: todos.</p></fieldset>
          <fieldset><legend>Aerolíneas</legend><div className="inline-choices">{dataset.airlineCodes.map(airline => <div className="choice" key={airline}><input id={`filter-airline-${airline}`} type="checkbox" name="airlineCodes" checked={filters.airlineCodes.includes(airline)} onChange={event => toggle('airlineCodes', airline, event.target.checked)} /><label htmlFor={`filter-airline-${airline}`}>{airline}</label></div>)}</div><p className="hint">Comercializadora u operadora.</p></fieldset>
          <div className="field"><label htmlFor="max-connections">Conexiones máximas</label><select id="max-connections" name="maxConnections" value={filters.maxConnections ?? ''} onChange={event => setFilters(current => ({ ...current, maxConnections: event.target.value === '' ? null : Number(event.target.value) }))}><option value="">Cualquiera</option>{[0, 1, 2, 3, 4, 5, 6].map(value => <option key={value} value={value}>{value === 0 ? 'Sin conexiones' : value}</option>)}</select></div>
          <div className="field"><label htmlFor="max-duration">Duración máxima (minutos)</label><input id="max-duration" name="maxDurationMinutes" type="number" min="1" max="10080" step="1" aria-invalid={Boolean(error) && errorField === 'max-duration'} aria-describedby={error && errorField === 'max-duration' ? 'filter-error' : undefined} value={maxDuration} onChange={event => { setMaxDuration(event.target.value); setError('') }} /></div>
          <div className="field"><label htmlFor="filter-currency">Moneda del precio</label><select id="filter-currency" name="currency" aria-invalid={Boolean(error) && errorField === 'max-price'} aria-describedby={error && errorField === 'max-price' ? 'filter-error' : undefined} value={filters.currency ?? ''} onChange={event => { setFilters(current => ({ ...current, currency: event.target.value || null })); setError('') }}><option value="">Todas, sin conversión</option>{dataset.currencies.map(currency => <option key={currency.code} value={currency.code}>{currency.code}</option>)}</select></div>
          <div className="field"><label htmlFor="max-price">Precio máximo</label><input id="max-price" name="maxPrice" type="text" inputMode="decimal" spellCheck={false} aria-invalid={Boolean(error) && errorField === 'max-price'} aria-describedby={`max-price-hint${error && errorField === 'max-price' ? ' filter-error' : ''}`} value={filters.maxPrice ?? ''} onChange={event => { setFilters(current => ({ ...current, maxPrice: event.target.value || null })); setError('') }} /><p id="max-price-hint" className="hint">Opcional. Usa punto decimal y elige moneda; vacío significa sin límite.</p></div>
          <div className="field"><label htmlFor="search-sort">Ordenar por</label><select id="search-sort" name="sort" value={sort} onChange={event => setSort(event.target.value as SearchSort)}><option value="DEFAULT">Orden estable</option><option value="PRICE_ASC">Precio dentro de cada grupo compatible</option><option value="DURATION_ASC">Menor duración</option><option value="CONNECTIONS_ASC">Menos conexiones</option></select></div>
          <div className="field"><label htmlFor="page-size">Ofertas por página</label><select id="page-size" name="pageSize" value={pageSize} onChange={event => setPageSize(Number(event.target.value))}>{[2, 5, 10, 20, 50].map(value => <option key={value} value={value}>{value}</option>)}</select></div>
        </div>
        <div className="form-actions"><button className="button button-small" type="submit">Aplicar filtros</button><button className="button button-secondary button-small" type="button" onClick={reset}>Restablecer filtros</button></div>
      </fieldset>
    </form>
  </details></search>
}
