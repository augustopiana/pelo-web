import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { adminApi, ApiError, catalogApi } from '../../lib/api'
import type { Foto, Genero, ViniloFormData } from '../../lib/types'
import { GOLDMINE_ORDEN, goldmine } from '../../lib/goldmine'

const VACIO = {
  titulo: '',
  artista: '',
  generoId: '',
  anio: '',
  sello: '',
  edicionPais: '',
  estadoDisco: 'NEAR_MINT',
  descripcion: '',
  precio: '',
  descuentoCortePct: '0',
}

export function PanelViniloFormPage() {
  const { id } = useParams<{ id: string }>()
  const editando = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState({ ...VACIO })
  const [generos, setGeneros] = useState<Genero[]>([])
  const [fotos, setFotos] = useState<Foto[]>([])
  const [error, setError] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)
  const [subiendo, setSubiendo] = useState(false)
  const fileInput = useRef<HTMLInputElement>(null)

  const set = (campo: keyof typeof form, valor: string) => setForm((f) => ({ ...f, [campo]: valor }))

  useEffect(() => {
    catalogApi.generos().then(setGeneros).catch(() => {})
  }, [])

  useEffect(() => {
    if (!id) return
    adminApi
      .getVinilo(id)
      .then((v) => {
        setForm({
          titulo: v.titulo,
          artista: v.artista,
          generoId: v.genero?.id ?? '',
          anio: v.anio?.toString() ?? '',
          sello: v.sello ?? '',
          edicionPais: v.edicionPais ?? '',
          estadoDisco: v.estadoDisco,
          descripcion: v.descripcion ?? '',
          precio: v.precio.toString(),
          descuentoCortePct: v.descuentoCortePct.toString(),
        })
        setFotos(v.fotos)
      })
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Error al cargar'))
  }, [id])

  const armarPayload = (): ViniloFormData => ({
    titulo: form.titulo.trim(),
    artista: form.artista.trim(),
    generoId: form.generoId || null,
    anio: form.anio ? Number(form.anio) : null,
    sello: form.sello || null,
    edicionPais: form.edicionPais || null,
    estadoDisco: form.estadoDisco,
    descripcion: form.descripcion || null,
    precio: Number(form.precio),
    descuentoCortePct: Number(form.descuentoCortePct),
  })

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setGuardando(true)
    try {
      if (editando && id) {
        await adminApi.actualizarVinilo(id, armarPayload())
      } else {
        const creado = await adminApi.crearVinilo(armarPayload())
        // Redirige a edición para poder cargar las fotos.
        navigate(`/panel/vinilos/${creado.id}`)
        return
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo guardar')
    } finally {
      setGuardando(false)
    }
  }

  const subirFotos = async (files: FileList | null) => {
    if (!files || files.length === 0 || !id) return
    setSubiendo(true)
    setError(null)
    try {
      const actualizadas = await adminApi.subirFotos(id, Array.from(files))
      setFotos(actualizadas)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudieron subir las fotos')
    } finally {
      setSubiendo(false)
      if (fileInput.current) fileInput.current.value = ''
    }
  }

  const borrarFoto = async (fotoId: string) => {
    if (!id) return
    try {
      await adminApi.borrarFoto(id, fotoId)
      setFotos((prev) => prev.filter((f) => f.id !== fotoId))
    } catch (err) {
      alert(err instanceof ApiError ? err.message : 'No se pudo borrar')
    }
  }

  return (
    <section>
      <Link to="/panel/vinilos" className="upper muted">← Volver a vinilos</Link>
      <h1 style={{ marginTop: '0.75rem' }}>{editando ? 'Editar vinilo' : 'Nuevo vinilo'}</h1>
      {error && <div className="alert alert--error">{error}</div>}

      <form onSubmit={submit} className="panel-form">
        <div className="grid-2">
          <div className="field">
            <label>Título *</label>
            <input className="input" required value={form.titulo} onChange={(e) => set('titulo', e.target.value)} />
          </div>
          <div className="field">
            <label>Artista *</label>
            <input className="input" required value={form.artista} onChange={(e) => set('artista', e.target.value)} />
          </div>
          <div className="field">
            <label>Género</label>
            <select className="input" value={form.generoId} onChange={(e) => set('generoId', e.target.value)}>
              <option value="">Sin género</option>
              {generos.map((g) => (
                <option key={g.id} value={g.id}>{g.nombre}</option>
              ))}
            </select>
          </div>
          <div className="field">
            <label>Año</label>
            <input className="input" type="number" value={form.anio} onChange={(e) => set('anio', e.target.value)} />
          </div>
          <div className="field">
            <label>Sello</label>
            <input className="input" value={form.sello} onChange={(e) => set('sello', e.target.value)} />
          </div>
          <div className="field">
            <label>Edición / país</label>
            <input className="input" value={form.edicionPais} onChange={(e) => set('edicionPais', e.target.value)} />
          </div>
          <div className="field">
            <label>Estado del disco (Goldmine) *</label>
            <select className="input" value={form.estadoDisco} onChange={(e) => set('estadoDisco', e.target.value)}>
              {GOLDMINE_ORDEN.map((k) => (
                <option key={k} value={k}>{goldmine(k).nombre} ({goldmine(k).sigla})</option>
              ))}
            </select>
          </div>
          <div className="field">
            <label>Precio (ARS) *</label>
            <input className="input" type="number" min="0" required value={form.precio} onChange={(e) => set('precio', e.target.value)} />
          </div>
          <div className="field">
            <label>Descuento en corte (%)</label>
            <input className="input" type="number" min="0" max="100" value={form.descuentoCortePct} onChange={(e) => set('descuentoCortePct', e.target.value)} />
          </div>
        </div>
        <div className="field">
          <label>Descripción</label>
          <textarea className="input" rows={3} value={form.descripcion} onChange={(e) => set('descripcion', e.target.value)} />
        </div>
        <button className="btn btn--neon" disabled={guardando}>
          {guardando ? 'Guardando…' : editando ? 'Guardar cambios' : 'Crear y cargar fotos'}
        </button>
      </form>

      {editando && (
        <div className="panel-fotos">
          <h2 className="upper">Fotos <span className="muted">(la primera es la portada)</span></h2>
          <div className="fotos-grid">
            {fotos.map((f) => (
              <div className="foto-item" key={f.id}>
                <img src={f.url} alt="" />
                {f.esPortada && <span className="foto-portada">portada</span>}
                <button type="button" className="foto-borrar" onClick={() => borrarFoto(f.id)} aria-label="Borrar foto">✕</button>
              </div>
            ))}
          </div>
          <label className="btn" style={{ marginTop: '0.8rem', cursor: 'pointer' }}>
            {subiendo ? 'Subiendo…' : '+ Subir fotos'}
            <input
              ref={fileInput}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              multiple
              hidden
              onChange={(e) => subirFotos(e.target.files)}
            />
          </label>
        </div>
      )}
    </section>
  )
}
