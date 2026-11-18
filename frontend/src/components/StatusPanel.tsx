import { ApiError } from '../api/client'

const errorMessages: Record<string, string> = {
  NETWORK_ERROR: 'No se pudo conectar con la API demo local. Comprueba que el backend esté iniciado.',
  INVALID_RESPONSE: 'La API ha devuelto una respuesta incompatible. No se han sustituido los datos por resultados inventados.',
  DATASET_VERSION_UNAVAILABLE: 'La versión del escenario ya no está disponible. Reinicia el catálogo y crea una búsqueda nueva.',
  VALIDATION_FAILED: 'Revisa los criterios: la combinación no es válida para este escenario.',
  INVALID_REQUEST: 'La solicitud no pudo validarse. Revisa los campos e inténtalo de nuevo.',
  PROVIDERS_FAILED: 'La búsqueda ha fallado. No se han sustituido los errores por resultados vacíos.',
  CAPABILITY_UNAVAILABLE: 'La modalidad no está disponible para los orígenes de este escenario.',
  RATE_LIMITED: 'Se ha alcanzado el límite de solicitudes. Espera antes de volver a intentarlo.',
  ACCESS_DENIED: 'La API no ha permitido esta solicitud. Comprueba la conexión local y el origen autorizado.',
}

export function ErrorPanel({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  const known = error instanceof ApiError
  return <div className="notice notice-error" role="alert">
    <strong>No se pudo completar la operación</strong>
    <p>{known ? errorMessages[error.code] ?? 'No se pudo procesar la respuesta de la API demo.' : 'No se pudo conectar con la API demo local. Comprueba que el backend esté iniciado.'}</p>
    {known && error.correlationId && <p className="small">Referencia: <code>{error.correlationId}</code></p>}
    {onRetry && <button type="button" className="button button-secondary" onClick={onRetry}>Reintentar</button>}
  </div>
}

export function LoadingPanel({ children }: { children: string }) {
  return <div className="notice loading"><progress aria-label={children} /><p>{children}</p></div>
}
