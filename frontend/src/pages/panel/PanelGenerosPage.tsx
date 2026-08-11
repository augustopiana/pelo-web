import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { adminApi, ApiError, catalogApi } from '../../lib/api'
import type { Genero } from '../../lib/types'

export function PanelGenerosPage() {
  const [generos, setGeneros] = useState<Genero[]>([])
  const [nombre, setNombre] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    catalogApi.generos().then(setGeneros).catch(() => {})
  }, [])

  const agregar = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setEnviando(true)
    try {
      const g = await adminApi.crearGenero(nombre.trim())
      setGeneros((prev) => [...prev, g].sort((a, b) => a.nombre.localeCompare(b.nombre)))
      setNombre('')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo crear')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section>
      <Link to="/panel/vinilos" className="upper muted">← Volver a vinilos</Link>
      <h1 style={{ marginTop: '0.75rem' }}>Géneros</h1>
      {error && <div className="alert alert--error">{error}</div>}

      <form onSubmit={agregar} className="genero-form">
        <input className="input" placeholder="Nuevo género" value={nombre} onChange={(e) => setNombre(e.target.value)} required />
        <button className="btn btn--neon" disabled={enviando}>{enviando ? '…' : 'Agregar'}</button>
      </form>

      <ul className="genero-list">
        {generos.map((g) => (
          <li key={g.id}>{g.nombre}</li>
        ))}
      </ul>
    </section>
  )
}
