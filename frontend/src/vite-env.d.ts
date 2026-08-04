/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string
  readonly VITE_GOOGLE_CLIENT_ID?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// Google Identity Services (cargado por script en index.html).
interface GoogleIdConfig {
  client_id: string
  callback: (resp: { credential: string }) => void
}
interface GoogleIdButtonOptions {
  theme?: string
  size?: string
  text?: string
  width?: number
  shape?: string
}
interface Window {
  google?: {
    accounts: {
      id: {
        initialize: (config: GoogleIdConfig) => void
        renderButton: (parent: HTMLElement, options: GoogleIdButtonOptions) => void
        prompt: () => void
      }
    }
  }
}
