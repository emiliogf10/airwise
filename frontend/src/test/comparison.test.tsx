import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ComparisonPanel } from '../components/ComparisonPanel'
import { baggageText, money, validDecimal } from '../lib/format'
import { airportFixture, comparisonFixture, offerFixture, offerId, searchFixture } from './fixtures'

function renderComparison(result = null as ReturnType<typeof comparisonFixture> | null) {
  const onCompare = vi.fn()
  const onRemove = vi.fn()
  render(<ComparisonPanel selected={[offerFixture(1), offerFixture(2)]} context={searchFixture().context} dataset={airportFixture().dataset} result={result} busy={false} onRemove={onRemove} onCompare={onCompare} onInvalidate={vi.fn()} />)
  return { onCompare, onRemove }
}

describe('comparación y estimaciones explícitas', () => {
  it('envía criterio de duración sin componentes ni estimaciones de otro criterio', async () => {
    const user = userEvent.setup()
    const { onCompare } = renderComparison()
    await user.selectOptions(screen.getByLabelText('Criterio de comparación'), 'FLIGHT_DURATION')
    await user.click(screen.getByRole('button', { name: 'Comparar selección' }))
    expect(onCompare).toHaveBeenCalledWith(expect.objectContaining({ primary: 'FLIGHT_DURATION', offerIds: [offerId(1), offerId(2)], requiredComponents: [], groundEstimates: [] }))
  })

  it('requiere escoger componentes antes de enviar subtotales', async () => {
    const user = userEvent.setup()
    const { onCompare } = renderComparison()
    await user.selectOptions(screen.getByLabelText('Criterio de comparación'), 'KNOWN_SUBTOTAL')
    await user.click(screen.getByRole('button', { name: 'Comparar selección' }))
    expect(onCompare).not.toHaveBeenCalled()
    expect(screen.getByText(/Selecciona al menos un componente terrestre/)).toBeInTheDocument()
  })

  it('conserva desconocida la estimación vacía y transmite un cero explícito como decimal', async () => {
    const user = userEvent.setup()
    const { onCompare } = renderComparison()
    await user.selectOptions(screen.getByLabelText('Criterio de comparación'), 'KNOWN_SUBTOTAL')
    await user.click(screen.getByRole('checkbox', { name: 'Traslado al aeropuerto de ida' }))
    await user.type(screen.getByLabelText('Traslado al aeropuerto de ida · 00000001'), '0.00')
    await user.click(screen.getByRole('button', { name: 'Comparar selección' }))
    expect(onCompare).toHaveBeenCalledWith(expect.objectContaining({
      requiredComponents: ['ORIGIN_GROUND_OUTBOUND'],
      groundEstimates: [{ offerId: offerId(1), component: 'ORIGIN_GROUND_OUTBOUND', money: { amount: '0.00', currency: 'EUR' }, basis: 'GROUP', travellerCount: 1, source: 'USER_ESTIMATE', assumption: 'USER_PROVIDED' }],
    }))
    expect(onCompare.mock.calls[0][0]).not.toHaveProperty('knownSubtotal')
    expect(onCompare.mock.calls[0][0].groundEstimates).toHaveLength(1)
  })

  it('no admite estimaciones de otra moneda y enfoca su error para corregirlo', async () => {
    const user = userEvent.setup()
    const { onCompare } = renderComparison()
    await user.selectOptions(screen.getByLabelText('Criterio de comparación'), 'KNOWN_SUBTOTAL')
    await user.click(screen.getByRole('checkbox', { name: 'Traslado al aeropuerto de ida' }))
    await user.type(screen.getByLabelText('Traslado al aeropuerto de ida · 00000001'), '12.30')
    await user.selectOptions(screen.getByLabelText('Moneda · Traslado al aeropuerto de ida · 00000001'), 'USD')
    await user.click(screen.getByRole('button', { name: 'Comparar selección' }))
    expect(onCompare).not.toHaveBeenCalled()
    const error = screen.getByText(/La estimación debe usar la misma moneda/)
    await waitFor(() => expect(error).toHaveFocus())
  })

  it.each(['1e2', '12,30', '-1', '12.345', '1000000.01'])('rechaza la estimación inválida %s sin redondearla', async (amount) => {
    const user = userEvent.setup()
    const { onCompare } = renderComparison()
    await user.selectOptions(screen.getByLabelText('Criterio de comparación'), 'KNOWN_SUBTOTAL')
    await user.click(screen.getByRole('checkbox', { name: 'Traslado al aeropuerto de ida' }))
    await user.type(screen.getByLabelText('Traslado al aeropuerto de ida · 00000001'), amount)
    await user.click(screen.getByRole('button', { name: 'Comparar selección' }))
    expect(onCompare).not.toHaveBeenCalled()
    expect(screen.getByText(/Cada estimación debe ser un decimal/)).toBeInTheDocument()
  })

  it('al cambiar desde subtotal no envía estimaciones retenidas en el formulario', async () => {
    const user = userEvent.setup()
    const { onCompare } = renderComparison()
    const criterion = screen.getByLabelText('Criterio de comparación')
    await user.selectOptions(criterion, 'KNOWN_SUBTOTAL')
    await user.click(screen.getByRole('checkbox', { name: 'Traslado al aeropuerto de ida' }))
    await user.type(screen.getByLabelText('Traslado al aeropuerto de ida · 00000001'), '12.30')
    await user.selectOptions(criterion, 'CONNECTIONS')
    await user.click(screen.getByRole('button', { name: 'Comparar selección' }))
    expect(onCompare).toHaveBeenCalledWith(expect.objectContaining({ primary: 'CONNECTIONS', requiredComponents: [], groundEstimates: [] }))
  })

  it('un rango nulo sigue sin clasificar aunque conozcamos el importe', () => {
    const comparison = comparisonFixture()
    comparison.groups[0].rows[0].rank = null
    comparison.groups[0].rows[0].reasons = ['EXPIRED_DATA']
    comparison.groups[0].recommendationIds = []
    comparison.groups[0].rankingComplete = false
    renderComparison(comparison)
    expect(screen.getByText('Sin clasificar')).toBeInTheDocument()
    expect(screen.getByText('Datos caducados en el escenario')).toBeInTheDocument()
    expect(screen.queryByText(/^Recomendada entre/)).not.toBeInTheDocument()
  })

  it('nunca presenta un subtotal ausente como un total del viaje', () => {
    const comparison = comparisonFixture()
    comparison.primary = 'KNOWN_SUBTOTAL'
    comparison.requiredComponents = ['ORIGIN_GROUND_OUTBOUND']
    for (const row of comparison.groups[0].rows) {
      row.rank = null
      row.primaryKnown = false
      row.missingFields = ['ORIGIN_GROUND_OUTBOUND']
      row.reasons = ['GROUND_COMPONENT_MISSING']
    }
    comparison.groups[0].recommendationIds = []
    comparison.groups[0].rankingComplete = false
    renderComparison(comparison)
    expect(screen.getAllByText('Subtotal desconocido')).toHaveLength(2)
    expect(screen.getAllByText('Subtotal de componentes seleccionados con estimaciones')).toHaveLength(2)
    expect(screen.queryByText(/^Total (del )?viaje/i)).not.toBeInTheDocument()
  })
})

describe('presentación conservadora de dinero y equipaje', () => {
  it('formatea decimales sin pérdida de precisión ni confusión entre null y cero', () => {
    expect(money({ amount: '999999999.99', currency: 'EUR' })).toBe('999.999.999,99 EUR')
    expect(money({ amount: '0.00', currency: 'USD' })).toBe('0,00 USD')
    expect(money(null)).toBe('Precio desconocido')
    expect(validDecimal('1000000.00', 2, '1000000')).toBe(true)
    expect(validDecimal('1000000.01', 2, '1000000')).toBe(false)
    expect(validDecimal('1000000000', 2, '1000000000', true)).toBe(false)
  })

  it('no inventa inclusión para un estado de equipaje inesperado', () => {
    expect(baggageText({ status: 'FUTURE_UNKNOWN_STATUS', pieces: null, weightKg: null })).toMatch(/desconocido/i)
    expect(baggageText({ status: 'NOT_INCLUDED', pieces: 0, weightKg: null })).toMatch(/No incluido.*desconocido/)
  })
})
