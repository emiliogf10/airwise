import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import App from '../App'
import type { ComparisonRequest, ComparisonResponse, SearchRequest, SearchResponse } from '../api/types'
import { airportFixture, comparisonFixture, errorFixture, jsonResponse, offerFixture, offerId, searchFixture } from './fixtures'

type SearchHandler = (request: SearchRequest) => SearchResponse | Response | Promise<SearchResponse | Response>
type ComparisonHandler = (request: ComparisonRequest) => ComparisonResponse | Promise<ComparisonResponse>

function installTransport(search: SearchHandler = () => searchFixture(), comparison: ComparisonResponse | ComparisonHandler = comparisonFixture()) {
  const searches: SearchRequest[] = []
  const comparisons: ComparisonRequest[] = []
  const fetchMock = vi.fn(async (url: string | URL | Request, options?: RequestInit) => {
    const path = String(url)
    if (path.startsWith('/api/v1/demo/airports')) return jsonResponse(airportFixture())
    if (path === '/api/v1/demo/flight-searches') {
      const request = JSON.parse(String(options?.body)) as SearchRequest
      searches.push(request)
      const result = await search(request)
      if (result instanceof Response) return result
      const context = { ...result.context, query: request.query, datasetVersion: request.datasetVersion }
      return jsonResponse({ ...result, context })
    }
    if (path === '/api/v1/demo/comparisons') {
      const request = JSON.parse(String(options?.body)) as ComparisonRequest
      comparisons.push(request)
      const result = typeof comparison === 'function' ? await comparison(request) : comparison
      return jsonResponse({ ...result, context: { ...result.context, query: request.query }, primary: request.primary, requiredComponents: request.requiredComponents })
    }
    throw new Error('La prueba ha intentado una ruta no demo')
  })
  vi.stubGlobal('fetch', fetchMock)
  return { searches, comparisons, fetchMock }
}

async function fillQuery(user: ReturnType<typeof userEvent.setup>) {
  const origin = await screen.findByRole('checkbox', { name: /^OPO/ }) as HTMLInputElement
  for (const code of ['BCN', 'MAD', 'SCQ']) {
    const other = screen.getByRole('checkbox', { name: new RegExp(`^${code}`) }) as HTMLInputElement
    if (other.checked) await user.click(other)
  }
  if (!origin.checked) await user.click(origin)
  await user.selectOptions(screen.getByLabelText(/^Aeropuerto de destino/), 'MAD')
  await user.click(screen.getByRole('radio', { name: 'Solo ida' }))
  fireEvent.change(screen.getByLabelText(/^Fecha de ida/), { target: { value: '2027-02-10' } })
}

async function submitSearch(user: ReturnType<typeof userEvent.setup>) {
  await fillQuery(user)
  await user.click(screen.getByRole('button', { name: 'Buscar vuelos' }))
  await screen.findAllByRole('checkbox', { name: /^Seleccionar oferta/ })
}

describe('experiencia Airwise demo', () => {
  it('presenta landmarks, controles etiquetados y calendario obtenido del backend', async () => {
    installTransport()
    render(<App />)
    const origin = await screen.findByRole('checkbox', { name: /^OPO/ })
    expect(screen.getAllByRole('main')).toHaveLength(1)
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1)
    expect(screen.getByRole('group', { name: /^Aeropuertos de origen/ })).toContainElement(origin)
    expect(screen.getByLabelText(/^Aeropuerto de destino/)).toHaveAccessibleName()
    expect(screen.getByLabelText(/^Fecha de ida/)).toHaveAttribute('min', '2027-01-01')
    expect(screen.getByLabelText(/^Fecha de ida/)).toHaveAttribute('max', '2027-12-31')
    expect(screen.getAllByText(/demo|sintétic/i).length).toBeGreaterThan(0)
  })

  it('permite activar un origen mediante teclado nativo', async () => {
    const user = userEvent.setup()
    installTransport()
    render(<App />)
    const origin = await screen.findByRole('checkbox', { name: /^SCQ/ }) as HTMLInputElement
    const previous = origin.checked
    origin.focus()
    await user.keyboard('[Space]')
    expect(origin).toHaveFocus()
    expect(origin.checked).toBe(!previous)
  })

  it('envía una búsqueda válida exacta con un adulto y sin inventar fecha de vuelta', async () => {
    const user = userEvent.setup()
    const { searches } = installTransport()
    render(<App />)
    await submitSearch(user)
    expect(searches).toHaveLength(1)
    expect(searches[0].query).toEqual({ tripType: 'ONE_WAY', origins: ['OPO'], destination: 'MAD', outboundDate: '2027-02-10', returnDate: null, travellers: { adults: 1 }, cabin: 'ECONOMY' })
    expect(searches[0].datasetVersion).toBe('demo-v1')
    expect(searches[0].page).toBe(0)
  })

  it('no envía la consulta si origen y destino coinciden', async () => {
    const user = userEvent.setup()
    const { searches } = installTransport()
    render(<App />)
    await fillQuery(user)
    const destination = screen.getByLabelText(/^Aeropuerto de destino/)
    const duplicate = within(destination).queryByRole('option', { name: /OPO/ }) as HTMLOptionElement | null
    if (duplicate && !duplicate.disabled) {
      await user.selectOptions(destination, 'OPO')
      await user.click(screen.getByRole('button', { name: 'Buscar vuelos' }))
      expect(searches).toHaveLength(0)
      expect(screen.getByText('Revisa la búsqueda')).toBeInTheDocument()
    } else {
      expect(duplicate === null || duplicate.disabled).toBe(true)
    }
  })

  it('valida la fecha de retorno y permite corregir ida/vuelta', async () => {
    const user = userEvent.setup()
    const { searches } = installTransport()
    render(<App />)
    await fillQuery(user)
    await user.click(screen.getByRole('radio', { name: 'Ida y vuelta' }))
    const returnDate = screen.getByLabelText(/^Fecha de vuelta/)
    fireEvent.change(returnDate, { target: { value: '2027-02-09' } })
    await user.click(screen.getByRole('button', { name: 'Buscar vuelos' }))
    expect(searches).toHaveLength(0)
    fireEvent.change(returnDate, { target: { value: '2027-02-17' } })
    await user.click(screen.getByRole('button', { name: 'Buscar vuelos' }))
    await waitFor(() => expect(searches).toHaveLength(1))
    expect(searches[0].query.tripType).toBe('ROUND_TRIP')
    expect(searches[0].query.returnDate).toBe('2027-02-17')
  })

  it('rechaza fechas ajenas al dataset sin usar el reloj real como calendario', async () => {
    const user = userEvent.setup()
    const { searches } = installTransport()
    render(<App />)
    await fillQuery(user)
    fireEvent.change(screen.getByLabelText(/^Fecha de ida/), { target: { value: '2028-01-01' } })
    await user.click(screen.getByRole('button', { name: 'Buscar vuelos' }))
    expect(searches).toHaveLength(0)
  })

  it('distingue un precio conocido de cero de otro desconocido y no inventa equipaje', async () => {
    const user = userEvent.setup()
    installTransport(() => searchFixture([offerFixture(1, '0.00'), offerFixture(2, null)]))
    render(<App />)
    await submitSearch(user)
    expect(screen.getByText(/0[,.]00\s*EUR/)).toBeInTheDocument()
    expect(screen.getAllByText(/desconocido/i).length).toBeGreaterThan(0)
    expect(screen.queryByText(/equipaje gratis/i)).not.toBeInTheDocument()
  })

  it('renderiza texto de aerolínea como texto y nunca como HTML ejecutable', async () => {
    const user = userEvent.setup()
    const offer = offerFixture()
    offer.itinerary.journeys[0].segments[0].marketingAirline = { code: 'D1', name: '<img src=x onerror=alert(1)>' }
    installTransport(() => searchFixture([offer]))
    const { container } = render(<App />)
    await submitSearch(user)
    expect(screen.getAllByText(/<img src=x onerror=alert\(1\)>/).length).toBeGreaterThan(0)
    expect(container.querySelector('img[onerror]')).toBeNull()
  })

  it('no sustituye un error del servidor por vuelos ni muestra detalles internos', async () => {
    const user = userEvent.setup()
    installTransport(() => jsonResponse({ ...errorFixture(), message: 'internal-stack-marker' }, 503))
    render(<App />)
    await fillQuery(user)
    await user.click(screen.getByRole('button', { name: 'Buscar vuelos' }))
    expect(await screen.findByRole('alert')).toBeInTheDocument()
    expect(screen.queryByText(/internal-stack-marker/)).not.toBeInTheDocument()
    expect(screen.queryByRole('checkbox', { name: /^Seleccionar oferta/ })).not.toBeInTheDocument()
  })

  it('presenta el estado vacío sin fabricar ofertas', async () => {
    const user = userEvent.setup()
    installTransport(() => searchFixture([]))
    render(<App />)
    await fillQuery(user)
    await user.click(screen.getByRole('button', { name: 'Buscar vuelos' }))
    await waitFor(() => expect(screen.getByText(/sin resultados|no hay (?:ofertas|resultados|alternativas)|no se encontraron/i)).toBeInTheDocument())
    expect(screen.queryByRole('checkbox', { name: /^Seleccionar oferta/ })).not.toBeInTheDocument()
  })

  it('muestra resultado parcial y conserva los datos recibidos', async () => {
    const user = userEvent.setup()
    installTransport(() => {
      const result = searchFixture()
      result.status = 'PARTIAL'
      result.providerResults[0] = { ...result.providerResults[0], status: 'PARTIAL', requiredUnits: 2, completedUnits: 1, issues: [{ code: 'PROVIDER_TIMEOUT', message: 'Una unidad sintética no se completó.' }] }
      result.errors = [{ code: 'PROVIDER_TIMEOUT', message: 'Una unidad sintética no se completó.' }]
      return result
    })
    render(<App />)
    await submitSearch(user)
    expect(screen.getAllByText(/parcial/i).length).toBeGreaterThan(0)
    expect(screen.getAllByRole('checkbox', { name: /^Seleccionar oferta/ })).toHaveLength(2)
  })

  it('limita la selección a cuatro ofertas y exige dos para comparar', async () => {
    const user = userEvent.setup()
    installTransport(() => searchFixture(Array.from({ length: 5 }, (_, index) => offerFixture(index + 1))))
    render(<App />)
    await submitSearch(user)
    expect(screen.queryByRole('button', { name: 'Comparar selección' })).not.toBeInTheDocument()
    const choices = screen.getAllByRole('checkbox', { name: /^Seleccionar oferta/ })
    await user.click(choices[0])
    expect(screen.queryByRole('button', { name: 'Comparar selección' })).not.toBeInTheDocument()
    await user.click(choices[1])
    expect(screen.getByRole('button', { name: 'Comparar selección' })).toBeEnabled()
    await user.click(choices[2])
    await user.click(choices[3])
    if (!(choices[4] as HTMLInputElement).disabled) await user.click(choices[4])
    expect(choices.filter((choice) => (choice as HTMLInputElement).checked)).toHaveLength(4)
  })

  it('mantiene selección entre páginas y envía todos los IDs seleccionados al comparador', async () => {
    const user = userEvent.setup()
    const { searches, comparisons } = installTransport((request) => {
      const body = searchFixture(request.page === 0 ? [offerFixture(1)] : [offerFixture(2)])
      body.pagination = { page: request.page, pageSize: request.pageSize, baseCount: 2, filteredCount: 2, excludedUnknownCount: 0, hasMore: request.page === 0 }
      return body
    })
    render(<App />)
    await submitSearch(user)
    await user.click(screen.getByRole('checkbox', { name: /^Seleccionar oferta/ }))
    await user.click(screen.getByRole('button', { name: 'Siguiente' }))
    await waitFor(() => expect(searches).toHaveLength(2))
    const nextOffer = await screen.findByRole('checkbox', { name: /Seleccionar oferta 00000002/ })
    await user.click(nextOffer)
    await user.click(screen.getByRole('button', { name: 'Comparar selección' }))
    await waitFor(() => expect(comparisons).toHaveLength(1))
    expect(comparisons[0].offerIds).toEqual([offerId(1), offerId(2)])
    expect(comparisons[0].query).toEqual(searches[0].query)
    expect(searches[1].page).toBe(1)
    expect(searches[1].datasetVersion).toBe('demo-v1')
  })

  it('reinicia selección al enviar una consulta base distinta', async () => {
    const user = userEvent.setup()
    installTransport()
    render(<App />)
    await submitSearch(user)
    const choices = screen.getAllByRole('checkbox', { name: /^Seleccionar oferta/ })
    await user.click(choices[0])
    await user.click(choices[1])
    fireEvent.change(screen.getByLabelText(/^Fecha de ida/), { target: { value: '2027-02-11' } })
    await user.click(screen.getByRole('button', { name: 'Buscar vuelos' }))
    await waitFor(() => expect(screen.queryByRole('button', { name: 'Comparar selección' })).not.toBeInTheDocument())
    expect(screen.getAllByRole('checkbox', { name: /^Seleccionar oferta/ }).every((choice) => !(choice as HTMLInputElement).checked)).toBe(true)
  })

  it('envía ordenación y paginación de servidor, sin reordenar solo la página local', async () => {
    const user = userEvent.setup()
    const { searches } = installTransport()
    render(<App />)
    await submitSearch(user)
    await user.selectOptions(screen.getByLabelText('Ordenar por'), 'DURATION_ASC')
    await user.click(screen.getByRole('button', { name: 'Aplicar filtros' }))
    await waitFor(() => expect(searches).toHaveLength(2))
    expect(searches[1].sort).toBe('DURATION_ASC')
    expect(searches[1].page).toBe(0)
  })

  it('muestra monedas incompatibles en grupos separados y sin ganador global', async () => {
    const user = userEvent.setup()
    installTransport(() => searchFixture([offerFixture(1, '80.00', 'EUR'), offerFixture(2, '120.00', 'USD')]), comparisonFixture(['EUR', 'USD']))
    render(<App />)
    await submitSearch(user)
    for (const choice of screen.getAllByRole('checkbox', { name: /^Seleccionar oferta/ })) await user.click(choice)
    await user.click(screen.getByRole('button', { name: 'Comparar selección' }))
    await screen.findByRole('heading', { name: 'Grupo compatible 2' })
    expect(screen.getAllByRole('heading', { name: /^Grupo compatible/ })).toHaveLength(2)
    expect(screen.queryByText(/^ganador(?:a)? global$/i)).not.toBeInTheDocument()
    expect(screen.queryByText(/^Recomendada entre las alternativas evaluables/)).not.toBeInTheDocument()
  })

  it('mantiene el estado de carga hasta que la búsqueda responde', async () => {
    const user = userEvent.setup()
    let resolve: ((value: SearchResponse) => void) | undefined
    installTransport(() => new Promise<SearchResponse>((done) => { resolve = done }))
    render(<App />)
    await fillQuery(user)
    await user.click(screen.getByRole('button', { name: 'Buscar vuelos' }))
    expect(screen.getByRole('button', { name: /busca/i })).toBeDisabled()
    expect(screen.queryByRole('checkbox', { name: /^Seleccionar oferta/ })).not.toBeInTheDocument()
    await act(async () => { resolve?.(searchFixture()) })
    await screen.findAllByRole('checkbox', { name: /^Seleccionar oferta/ })
  })

  it('limita los orígenes a tres y permite liberar una selección', async () => {
    const user = userEvent.setup()
    const { searches } = installTransport()
    render(<App />)
    await screen.findByRole('checkbox', { name: /^OPO/ })
    for (const code of ['BCN', 'OPO', 'SCQ']) await user.click(screen.getByRole('checkbox', { name: new RegExp(`^${code}`) }))
    expect(screen.getByRole('checkbox', { name: /^MAD/ })).toBeDisabled()
    await user.click(screen.getByRole('checkbox', { name: /^SCQ/ }))
    expect(screen.getByRole('checkbox', { name: /^MAD/ })).toBeEnabled()
    expect(searches).toHaveLength(0)
  })

  it('valida precio máximo con moneda y aplica filtros al servidor sin perder la selección oculta', async () => {
    const user = userEvent.setup()
    const { searches, comparisons } = installTransport((request) => {
      const second = offerFixture(2)
      second.itinerary.journeys[0].segments[0].marketingAirline = { code: 'D2', name: 'Demo Two' }
      const response = searchFixture(request.filters.airlineCodes.includes('D2') ? [second] : [offerFixture(1), second])
      response.pagination.baseCount = 2
      return response
    })
    render(<App />)
    await submitSearch(user)
    await user.click(screen.getAllByRole('checkbox', { name: /^Seleccionar oferta/ })[0])
    await user.type(screen.getByLabelText('Precio máximo'), '99.90')
    await user.click(screen.getByRole('button', { name: 'Aplicar filtros' }))
    expect(searches).toHaveLength(1)
    expect(screen.getByText(/El precio máximo necesita una moneda/)).toBeInTheDocument()
    await user.selectOptions(screen.getByLabelText('Moneda del precio'), 'EUR')
    await user.click(screen.getByRole('checkbox', { name: 'D2' }))
    await user.click(screen.getByRole('button', { name: 'Aplicar filtros' }))
    await screen.findByText(/seleccionada\(s\) no está\(n\) visible/)
    expect(searches[1].filters).toMatchObject({ airlineCodes: ['D2'], currency: 'EUR', maxPrice: '99.90' })
    await user.click(screen.getByRole('checkbox', { name: /^Seleccionar oferta/ }))
    await user.click(screen.getByRole('button', { name: 'Comparar selección' }))
    await waitFor(() => expect(comparisons).toHaveLength(1))
    expect(comparisons[0].offerIds).toEqual([offerId(1), offerId(2)])
  })

  it('limpia selección y contexto ante versión retirada y permite reiniciar el catálogo', async () => {
    const user = userEvent.setup()
    const { searches } = installTransport((request) => request.sort === 'DURATION_ASC'
      ? jsonResponse(errorFixture('DATASET_VERSION_UNAVAILABLE'), 409) : searchFixture())
    render(<App />)
    await submitSearch(user)
    for (const choice of screen.getAllByRole('checkbox', { name: /^Seleccionar oferta/ })) await user.click(choice)
    await user.selectOptions(screen.getByLabelText('Ordenar por'), 'DURATION_ASC')
    await user.click(screen.getByRole('button', { name: 'Aplicar filtros' }))
    const restart = await screen.findByRole('button', { name: 'Reiniciar catálogo y búsqueda' })
    expect(searches).toHaveLength(2)
    expect(screen.queryByRole('button', { name: 'Comparar selección' })).not.toBeInTheDocument()
    expect(screen.queryByRole('checkbox', { name: /^Seleccionar oferta/ })).not.toBeInTheDocument()
    await user.click(restart)
    expect(await screen.findByRole('checkbox', { name: /^OPO/ })).not.toBeChecked()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('ignora una comparación tardía cancelada cuando una búsqueda nueva cambia el contexto', async () => {
    const user = userEvent.setup()
    let resolve: ((value: ComparisonResponse) => void) | undefined
    const { searches, comparisons } = installTransport(() => searchFixture(), () => new Promise<ComparisonResponse>((done) => { resolve = done }))
    render(<App />)
    await submitSearch(user)
    for (const choice of screen.getAllByRole('checkbox', { name: /^Seleccionar oferta/ })) await user.click(choice)
    await user.click(screen.getByRole('button', { name: 'Comparar selección' }))
    expect(comparisons).toHaveLength(1)
    fireEvent.change(screen.getByLabelText(/^Fecha de ida/), { target: { value: '2027-02-11' } })
    await user.click(screen.getByRole('button', { name: 'Buscar vuelos' }))
    await waitFor(() => expect(searches).toHaveLength(2))
    await screen.findAllByRole('checkbox', { name: /^Seleccionar oferta/ })
    await act(async () => { resolve?.(comparisonFixture()) })
    expect(screen.queryByRole('heading', { name: /^Grupo compatible/ })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Comparar selección' })).not.toBeInTheDocument()
    expect(screen.getAllByRole('checkbox', { name: /^Seleccionar oferta/ }).every((choice) => !(choice as HTMLInputElement).checked)).toBe(true)
  })
})
