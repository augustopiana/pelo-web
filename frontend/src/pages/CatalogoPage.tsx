import { useEffect, useState } from 'react'
import { catalogApi } from '../lib/api'
import type { CatalogFiltros, Genero, PageResponse, ViniloResumen } from '../lib/types'
import { ViniloCard } from '../components/ViniloCard'
import { GOLDMINE_ORDEN, goldmine } from '../lib/goldmine'

const VACIO: CatalogFiltros = {
  q: '',
  artista: '',
  generoId: '',
  precioMin: '',
  precioMax: '',
  estadoDisco: '',
  page: 0,
  size: 12,
}

export function CatalogoPage() {
  const [generos, setGeneros] = useState<Genero[]>([])
  const [filtros, setFiltros] = useState<CatalogFiltros>(VACIO)
  const [aplicados, setAplicados] = useState<CatalogFiltros>(VACIO)
  const [data, setData] = useState<PageResponse<ViniloResumen> | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    catalogApi.generos().then(setGeneros).catch(() => {})
  }, [])

  useEffect(() => {
    setCargando(true)
    setError(null)
    catalogApi
      .list(aplicados)
      .then(setData)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Error al cargar'))
      .finally(() => setCargando(false))
  }, [aplicados])

  const set = (campo: keyof CatalogFiltros, valor: string) =>
    setFiltros((f) => ({ ...f, [campo]: valor }))

  const buscar = (e: React.FormEvent) => {
    e.preventDefault()
    setAplicados({ ...filtros, page: 0 })
  }

  const limpiar = () => {
    setFiltros(VACIO)
    setAplicados(VACIO)
  }

  const irAPagina = (page: number) => setAplicados((a) => ({ ...a, page }))

  return (
    <section>
      <div className="page-title">
        <h1>Catálogo</h1>
        {data && <span className="result-count">{data.totalElements} vinilos</span>}
      </div>

      <form className="filters" onSubmit={buscar}>
        <div className="field">
          <label>Buscar</label>
          <input
            className="input"
            placeholder="Título o artista"
            value={filtros.q}
            onChange={(e) => set('q', e.target.value)}
          />
        </div>
        <div className="field">
          <label>Artista</label>
          <input className="input" value={filtros.artista} onChange={(e) => set('artista', e.target.value)} />
        </div>
        <div className="field">
          <label>Género</label>
          <select className="input" value={filtros.generoId} onChange={(e) => set('generoId', e.target.value)}>
            <option value="">Todos</option>
            {generos.map((g) => (
              <option key={g.id} value={g.id}>{g.nombre}</option>
            ))}
          </select>
        </div>
        <div className="field">
          <label>Estado</label>
          <select className="input" value={filtros.estadoDisco} onChange={(e) => set('estadoDisco', e.target.value)}>
            <option value="">Cualquiera</option>
            {GOLDMINE_ORDEN.map((k) => (
              <option key={k} value={k}>{goldmine(k).nombre} ({goldmine(k).sigla})</option>
            ))}
          </select>
        </div>
        <div className="field">
          <label>Precio mín.</label>
          <input className="input" type="number" min="0" value={filtros.precioMin} onChange={(e) => set('precioMin', e.target.value)} />
        </div>
        <div className="field">
          <label>Precio máx.</label>
          <input className="input" type="number" min="0" value={filtros.precioMax} onChange={(e) => set('precioMax', e.target.value)} />
        </div>
        <div className="filters__actions">
          <button type="submit" className="btn btn--neon">Filtrar</button>
          <button type="button" className="btn" onClick={limpiar}>Limpiar</button>
        </div>
      </form>

      {cargando && <div className="spinner">Cargando catálogo…</div>}
      {error && <div className="alert alert--error">{error}</div>}

      {!cargando && !error && data && (
        <>
          {data.content.length === 0 ? (
            <div className="center-note">No hay vinilos que coincidan con tu búsqueda.</div>
          ) : (
            <div className="grid">
              {data.content.map((v) => (
                <ViniloCard key={v.id} v={v} />
              ))}
            </div>
          )}

          {data.totalPages > 1 && (
            <div className="pager">
              <button className="btn" disabled={data.page === 0} onClick={() => irAPagina(data.page - 1)}>
                ← Anterior
              </button>
              <span className="muted">
                Página {data.page + 1} de {data.totalPages}
              </span>
              <button
                className="btn"
                disabled={data.page >= data.totalPages - 1}
                onClick={() => irAPagina(data.page + 1)}
              >
                Siguiente →
              </button>
            </div>
          )}
        </>
      )}
    </section>
  )
}
