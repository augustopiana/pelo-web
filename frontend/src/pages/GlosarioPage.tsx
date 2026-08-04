import { Link } from 'react-router-dom'
import { GOLDMINE_ORDEN, goldmine } from '../lib/goldmine'

/** Glosario de la escala Goldmine (spec §8, D-1). */
export function GlosarioPage() {
  return (
    <section className="glosario">
      <Link to="/catalogo" className="upper muted">← Volver al catálogo</Link>
      <h1 style={{ marginTop: '0.75rem' }}>Escala Goldmine</h1>
      <p className="muted" style={{ maxWidth: 640 }}>
        El estado de cada disco se describe con la escala Goldmine, un estándar entre coleccionistas.
        Va de mejor a peor. Así sabés en qué condición está el vinilo antes de comprarlo.
      </p>
      <table>
        <thead>
          <tr>
            <th>Sigla</th>
            <th>Nombre</th>
            <th>Qué significa</th>
          </tr>
        </thead>
        <tbody>
          {GOLDMINE_ORDEN.map((k) => {
            const g = goldmine(k)
            return (
              <tr key={k}>
                <td className="sigla">{g.sigla}</td>
                <td><strong>{g.nombre}</strong></td>
                <td>{g.descripcion}</td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </section>
  )
}
