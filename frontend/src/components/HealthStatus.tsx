import { useEffect, useState } from 'react'
import { getHealth, type HealthResponse } from '../lib/api'

type State =
  | { kind: 'loading' }
  | { kind: 'ok'; data: HealthResponse }
  | { kind: 'error'; message: string }

/** Health-check del circuito front -> back -> BD (pie de página). */
export function HealthStatus() {
  const [state, setState] = useState<State>({ kind: 'loading' })

  useEffect(() => {
    let activo = true
    getHealth()
      .then((data) => activo && setState({ kind: 'ok', data }))
      .catch((err: unknown) => {
        if (!activo) return
        setState({ kind: 'error', message: err instanceof Error ? err.message : 'error' })
      })
    return () => {
      activo = false
    }
  }, [])

  if (state.kind === 'loading') return <span className="health health--loading">conectando…</span>
  if (state.kind === 'error') return <span className="health health--error">backend offline</span>

  const dbOk = state.data.database === 'UP'
  return (
    <span className={`health ${dbOk ? 'health--ok' : 'health--warn'}`}>
      ● backend {state.data.status.toLowerCase()} · bd {state.data.database.toLowerCase()}
    </span>
  )
}
