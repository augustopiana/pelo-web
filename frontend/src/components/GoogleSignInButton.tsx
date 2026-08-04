import { useEffect, useRef } from 'react'

const CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID

/**
 * Botón "Sign in with Google" (Google Identity Services).
 * Solo se muestra si VITE_GOOGLE_CLIENT_ID está configurado. Al obtener el
 * credential (ID token), lo pasa a onCredential para enviarlo al backend.
 */
export function GoogleSignInButton({ onCredential }: { onCredential: (idToken: string) => void }) {
  const ref = useRef<HTMLDivElement>(null)
  const cb = useRef(onCredential)
  cb.current = onCredential

  useEffect(() => {
    if (!CLIENT_ID) return
    let intervalo: number | undefined

    const init = () => {
      if (!window.google || !ref.current) return false
      window.google.accounts.id.initialize({
        client_id: CLIENT_ID,
        callback: (resp) => cb.current(resp.credential),
      })
      window.google.accounts.id.renderButton(ref.current, {
        theme: 'filled_black',
        size: 'large',
        text: 'signin_with',
        shape: 'rectangular',
        width: 320,
      })
      return true
    }

    if (!init()) {
      // El script GIS puede no haber cargado aún: reintentar.
      intervalo = window.setInterval(() => {
        if (init() && intervalo) window.clearInterval(intervalo)
      }, 200)
    }
    return () => {
      if (intervalo) window.clearInterval(intervalo)
    }
  }, [])

  if (!CLIENT_ID) return null
  return <div ref={ref} style={{ marginTop: '0.6rem', display: 'flex', justifyContent: 'center' }} />
}
