import type { SearchFilters } from '../api/types'

export function emptyFilters(): SearchFilters {
  return { origins: [], airlineCodes: [], maxConnections: null, maxDurationMinutes: null, currency: null, maxPrice: null }
}
