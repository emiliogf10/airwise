import { useEffect, useRef, useState } from 'react'
import type { AirportResponse, ComparisonRequest, ComparisonResponse, FlightOffer, SearchContext, SearchQuery, SearchRequest, SearchResponse } from './api/types'
import { ApiError, compareFlights, getAirports, searchFlights } from './api/client'
import { ComparisonPanel } from './components/ComparisonPanel'
import { SearchFilters } from './components/SearchFilters'
import { emptyFilters } from './lib/search'
import { SearchForm } from './components/SearchForm'
import { SearchResults } from './components/SearchResults'
import { ErrorPanel, LoadingPanel } from './components/StatusPanel'

function baseKey(request: SearchRequest) { return JSON.stringify({ datasetVersion: request.datasetVersion, query: { ...request.query, origins: [...request.query.origins].sort() } }) }
function isAborted(error: unknown) { return typeof error === 'object' && error !== null && 'name' in error && error.name === 'AbortError' }

export default function App() {
  const [catalog, setCatalog] = useState<AirportResponse | null>(null)
  const [catalogAttempt, setCatalogAttempt] = useState(0)
  const [catalogLoading, setCatalogLoading] = useState(true)
  const [catalogError, setCatalogError] = useState<unknown>(null)
  const [request, setRequest] = useState<SearchRequest | null>(null)
  const [result, setResult] = useState<SearchResponse | null>(null)
  const [context, setContext] = useState<SearchContext | null>(null)
  const [searchLoading, setSearchLoading] = useState(false)
  const [searchError, setSearchError] = useState<unknown>(null)
  const [selected, setSelected] = useState<Map<string, FlightOffer>>(new Map())
  const [selectionNotice, setSelectionNotice] = useState('')
  const [comparison, setComparison] = useState<ComparisonResponse | null>(null)
  const [comparisonLoading, setComparisonLoading] = useState(false)
  const [comparisonError, setComparisonError] = useState<unknown>(null)
  const [announcement, setAnnouncement] = useState('')
  const [versionUnavailable, setVersionUnavailable] = useState(false)
  const searchAbort = useRef<AbortController | null>(null)
  const comparisonAbort = useRef<AbortController | null>(null)
  const contextRef = useRef<SearchContext | null>(null)
  const resultsHeading = useRef<HTMLHeadingElement>(null)
  const compareHeading = useRef<HTMLHeadingElement>(null)
  const errorFocus = useRef<HTMLDivElement>(null)
  const compareErrorFocus = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const controller = new AbortController()
    getAirports(controller.signal).then(response => {
      if (controller.signal.aborted) return
      setCatalog(response); setCatalogLoading(false); setCatalogError(null)
    }).catch((error: unknown) => {
      if (controller.signal.aborted || isAborted(error)) return
      setCatalogLoading(false); setCatalogError(error)
    })
    return () => controller.abort()
  }, [catalogAttempt])
  useEffect(() => () => { searchAbort.current?.abort(); comparisonAbort.current?.abort() }, [])

  function invalidateComparison() {
    comparisonAbort.current?.abort(); setComparison(null); setComparisonError(null); setComparisonLoading(false)
  }

  function clearSelection(message: string) {
    invalidateComparison(); setSelected(new Map()); setSelectionNotice(message)
  }

  function resetCatalog() {
    searchAbort.current?.abort(); clearSelection('Se ha reiniciado el contexto. Crea una nueva búsqueda para seleccionar alternativas.')
    contextRef.current = null; setContext(null); setResult(null); setRequest(null); setSearchLoading(false); setSearchError(null)
    setCatalog(null); setCatalogLoading(true); setCatalogError(null); setVersionUnavailable(false); setCatalogAttempt(value => value + 1)
    setAnnouncement('Contexto reiniciado. Cargando el catálogo demo.')
  }

  async function runSearch(next: SearchRequest) {
    searchAbort.current?.abort()
    const controller = new AbortController(); searchAbort.current = controller
    if (!request || baseKey(request) !== baseKey(next)) {
      clearSelection(selected.size ? 'La búsqueda ha cambiado. Se ha borrado la selección anterior.' : '')
      contextRef.current = null; setContext(null)
    }
    setRequest(next); setResult(null); setSearchError(null); setSearchLoading(true); setVersionUnavailable(false)
    try {
      const response = await searchFlights(next, controller.signal)
      if (controller.signal.aborted) return
      const previous = contextRef.current
      if (previous && (previous.datasetVersion !== response.context.datasetVersion || previous.fingerprint !== response.context.fingerprint)) clearSelection('La versión o la consulta canónica ha cambiado. Selecciona de nuevo las alternativas.')
      contextRef.current = response.context; setContext(response.context); setResult(response)
      setRequest({ ...next, query: response.context.query, datasetVersion: response.context.datasetVersion })
      setSearchLoading(false); setAnnouncement(`${response.pagination.filteredCount} alternativas. ${response.status === 'PARTIAL' ? 'Resultado parcial.' : 'Búsqueda completada.'}`)
      requestAnimationFrame(() => resultsHeading.current?.focus())
    } catch (error) {
      if (controller.signal.aborted || isAborted(error)) return
      setSearchLoading(false); setSearchError(error); setAnnouncement('La búsqueda no se ha podido completar.')
      if (error instanceof ApiError && error.code === 'DATASET_VERSION_UNAVAILABLE') { setVersionUnavailable(true); clearSelection('Versión retirada. La selección se ha eliminado.'); contextRef.current = null; setContext(null) }
      requestAnimationFrame(() => errorFocus.current?.focus())
    }
  }

  function search(query: SearchQuery) {
    if (!catalog) return
    void runSearch({ query, datasetVersion: catalog.dataset.datasetVersion, filters: emptyFilters(), sort: 'DEFAULT', page: 0, pageSize: 20 })
  }

  function toggleSelection(offer: FlightOffer) {
    if (searchLoading || comparisonLoading || versionUnavailable) return
    invalidateComparison(); setSelectionNotice('')
    const next = new Map(selected)
    if (next.has(offer.offerId)) next.delete(offer.offerId)
    else if (next.size < 4) next.set(offer.offerId, offer)
    setSelected(next); setAnnouncement(`${next.size} de 4 alternativas seleccionadas.`)
  }

  function removeSelection(id: string) {
    invalidateComparison(); setSelected(current => { const next = new Map(current); next.delete(id); return next })
    setAnnouncement('Alternativa eliminada de la selección.')
  }

  async function runComparison(next: ComparisonRequest) {
    comparisonAbort.current?.abort()
    const controller = new AbortController(); comparisonAbort.current = controller
    setComparison(null); setComparisonError(null); setComparisonLoading(true)
    try {
      const response = await compareFlights(next, controller.signal)
      if (controller.signal.aborted) return
      const active = contextRef.current
      if (!active || response.context.datasetVersion !== active.datasetVersion || response.context.fingerprint !== active.fingerprint) {
        clearSelection('El contexto de comparación ha cambiado. Repite la búsqueda y selecciona de nuevo.'); return
      }
      setComparison(response); setComparisonLoading(false); setAnnouncement(`Comparación completada en ${response.groups.length} grupo(s) compatible(s).`)
      requestAnimationFrame(() => compareHeading.current?.focus())
    } catch (error) {
      if (controller.signal.aborted || isAborted(error)) return
      setComparisonLoading(false); setComparisonError(error)
      if (error instanceof ApiError && error.code === 'DATASET_VERSION_UNAVAILABLE') { setVersionUnavailable(true); clearSelection('Versión retirada. La selección se ha eliminado.'); contextRef.current = null; setContext(null) }
      requestAnimationFrame(() => compareErrorFocus.current?.focus())
    }
  }

  const hiddenSelection = result ? [...selected.keys()].filter(id => !result.offers.some(offer => offer.offerId === id)).length : 0
  return <>
    <a className="skip-link" href="#contenido">Saltar al contenido</a>
    <header className="site-header"><a className="brand" href="#contenido" aria-label="Airwise, inicio"><span className="brand-symbol" aria-hidden="true">↗</span>airwise<span className="brand-dot" aria-hidden="true">.</span></a><nav aria-label="Principal"><a href="#buscar">Buscar</a><a href="#resultados">Resultados</a><a href="#comparar">Comparar</a></nav><span className="demo-indicator"><span aria-hidden="true">●</span> Modo demo</span></header>
    <main>
      <section className="hero"><div><p className="eyebrow">Vuela con criterio</p><h1 id="contenido" tabIndex={-1}>Más alternativas.<br /><span>Mejores decisiones.</span></h1><p className="hero-description">Compara aeropuertos, entiende cada trayecto y descubre lo que hay detrás de un precio.</p></div><div className="hero-note"><span className="pill">DEMO · Entorno de exploración</span><p>Transparencia desde el primer vuelo.</p><span>Datos sintéticos, condiciones visibles y comparaciones explicables. Sin reservas ni disponibilidad real.</span></div></section>
      <div className="workspace">
        <div className="demo-banner"><strong>Estás explorando un escenario simulado.</strong><span>Los vuelos, precios y tiempos son sintéticos; su vigencia se evalúa con el reloj fijo del dataset, no con la fecha actual.</span></div>
        {catalogLoading && <LoadingPanel>Cargando aeropuertos del escenario demo…</LoadingPanel>}
        {catalogError !== null && <ErrorPanel error={catalogError} onRetry={() => { setCatalogLoading(true); setCatalogError(null); setCatalogAttempt(value => value + 1) }} />}
        {catalog && <SearchForm key={catalog.dataset.datasetVersion} airports={catalog.items} dataset={catalog.dataset} busy={searchLoading} onSearch={search} />}
        <section id="resultados" className="results-section" aria-busy={searchLoading}>
          <div className="section-heading"><div><p className="eyebrow">02 / Entiende cada opción</p><h2 tabIndex={-1} ref={resultsHeading}>Resultados de tu búsqueda</h2></div>{result && <span className="pill pill-soft">DEMO · {result.status === 'PARTIAL' ? 'Parcial' : 'Completa'}</span>}</div>
          {!request && !searchLoading && <div className="initial-state"><span className="initial-icon" aria-hidden="true">↗</span><h3>El viaje empieza con una buena comparación.</h3><p>Elige tus aeropuertos y fechas o carga el ejemplo demo. Aquí encontrarás itinerarios y condiciones para decidir con contexto.</p></div>}
          {request && catalog && <SearchFilters key={baseKey(request)} query={request.query} dataset={catalog.dataset} initial={request.filters} sort={request.sort} pageSize={request.pageSize} busy={searchLoading || versionUnavailable} onApply={(filters, sort, pageSize) => void runSearch({ ...request, filters, sort, pageSize, page: 0 })} />}
          {searchLoading && <LoadingPanel>Buscando alternativas en el escenario demo…</LoadingPanel>}
          {searchError !== null && <div tabIndex={-1} ref={errorFocus}><ErrorPanel error={searchError} onRetry={request && !versionUnavailable ? () => void runSearch(request) : undefined} /></div>}
          {versionUnavailable && <button type="button" className="button" onClick={resetCatalog}>Reiniciar catálogo y búsqueda</button>}
          {result && <SearchResults result={result} selected={selected} busy={searchLoading || comparisonLoading} onSelect={toggleSelection} onPage={page => { if (request) void runSearch({ ...request, page }) }} />}
        </section>
        <section id="comparar" className="compare-section" aria-busy={comparisonLoading}>
          <div className="section-heading"><div><p className="eyebrow">03 / Decide con contexto</p><h2 tabIndex={-1} ref={compareHeading}>Compara tus alternativas</h2></div><span className="pill">De 2 a 4 ofertas</span></div>
          {selectionNotice && <p className="notice">{selectionNotice}</p>}
          {hiddenSelection > 0 && <p className="notice">{hiddenSelection} alternativa(s) seleccionada(s) no está(n) visible(s) en esta página o con estos filtros. Se conservan para comparar.</p>}
          {context && catalog ? <ComparisonPanel key={`${context.fingerprint}:${[...selected.keys()].join(',')}`} selected={[...selected.values()]} context={context} dataset={catalog.dataset} result={comparison} busy={comparisonLoading || searchLoading} onRemove={removeSelection} onCompare={next => void runComparison(next)} onInvalidate={invalidateComparison} /> : <p className="hint">Busca vuelos y selecciona al menos dos itinerarios para comparar precios, duración, conexiones o componentes conocidos con estimaciones.</p>}
          {comparisonLoading && <LoadingPanel>Comparando alternativas en el servidor demo…</LoadingPanel>}
          {comparisonError !== null && <div tabIndex={-1} ref={compareErrorFocus}><ErrorPanel error={comparisonError} /></div>}
        </section>
      </div>
    </main>
    <footer className="site-footer"><div className="brand">airwise<span className="brand-dot">.</span></div><p>Comparar mejor empieza por saber qué conocemos.</p><span>Demo de portfolio · Sin compra, reserva ni datos en tiempo real.</span></footer>
    <p className="visually-hidden" role="status" aria-atomic="true">{announcement}</p>
  </>
}
