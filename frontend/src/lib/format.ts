// Formateo de precios en pesos argentinos.
const fmt = new Intl.NumberFormat('es-AR', {
  style: 'currency',
  currency: 'ARS',
  maximumFractionDigits: 0,
})

export function precio(valor: number): string {
  return fmt.format(valor)
}
