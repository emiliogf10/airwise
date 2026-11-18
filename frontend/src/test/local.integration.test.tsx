import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { expect, it, vi } from 'vitest'
import App from '../App'

// Explicit opt-in: this observes React + real local HTTP, not a real browser.
// Normal unit-test runs never open a socket or silently substitute fixtures.
it.skipIf(import.meta.env.MODE !== 'local-integration')('integra la UI con los tres endpoints demo locales y criterios del servidor', async () => {
  const nativeFetch = globalThis.fetch.bind(globalThis)
  const origin = 'http://127.0.0.1:5173'
  const allowed = new Set(['/api/v1/demo/airports', '/api/v1/demo/flight-searches', '/api/v1/demo/comparisons'])
  const requests: string[] = []
  vi.stubGlobal('fetch', async (input: string | URL | Request, options?: RequestInit) => {
    const url = new URL(String(input), origin)
    if (url.origin !== origin || !allowed.has(url.pathname)) throw new Error('Solo se permiten los tres endpoints de la demo local')
    const headers = new Headers(options?.headers)
    headers.set('Origin', origin)
    requests.push(url.pathname)
    return nativeFetch(url.href, { ...options, headers })
  })

  const user = userEvent.setup()
  render(<App />)
  await screen.findByRole('checkbox', { name: /^OPO/ }, { timeout: 10000 })
  await user.click(screen.getByRole('button', { name: 'Cargar ejemplo demo' }))
  await user.click(screen.getByRole('button', { name: 'Buscar vuelos' }))
  const offers = await screen.findAllByRole('checkbox', { name: /^Seleccionar oferta/ }, { timeout: 10000 })
  expect(offers.length).toBeGreaterThanOrEqual(2)
  await user.click(offers[0])
  await user.click(offers[1])

  for (const primary of ['FLIGHT_PRICE', 'FLIGHT_DURATION', 'CONNECTIONS', 'KNOWN_SUBTOTAL']) {
    await user.selectOptions(screen.getByLabelText('Criterio de comparación'), primary)
    if (primary === 'KNOWN_SUBTOTAL') {
      await user.click(screen.getByRole('checkbox', { name: 'Traslado al aeropuerto de ida' }))
      const estimates = screen.getAllByRole('textbox', { name: /^Traslado al aeropuerto de ida ·/ })
      expect(estimates).toHaveLength(2)
      for (const input of estimates) await user.type(input, '20.00')
    }
    await user.click(screen.getByRole('button', { name: 'Comparar selección' }))
    await screen.findAllByRole('heading', { name: /^Grupo compatible/ }, { timeout: 10000 })
    await waitFor(() => expect(screen.queryByRole('button', { name: 'Comparando…' })).not.toBeInTheDocument())
  }

  expect(requests.filter((path) => path.endsWith('/airports'))).toHaveLength(1)
  expect(requests.filter((path) => path.endsWith('/flight-searches'))).toHaveLength(1)
  expect(requests.filter((path) => path.endsWith('/comparisons'))).toHaveLength(4)
  expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  expect(screen.getAllByText('Subtotal de componentes seleccionados con estimaciones')).toHaveLength(2)
}, 30000)
