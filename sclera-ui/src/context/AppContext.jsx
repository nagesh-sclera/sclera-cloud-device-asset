import { createContext, useContext, useEffect, useState } from 'react'
import { DEMO } from '../config.js'

const AppCtx = createContext(null)

// Lightweight app-wide state. Persists selected nav/property to localStorage.
export function AppProvider({ children }) {
  const [nav, setNav] = useState(() => localStorage.getItem('sclera.nav') || 'properties')
  const [view, setView] = useState(() => localStorage.getItem('sclera.view') || 'properties') // properties | dashboard | assets
  const [property, setProperty] = useState(() => {
    try { return JSON.parse(localStorage.getItem('sclera.property')) } catch { return null }
  })
  const [ctx] = useState({ user: DEMO.user, vdmsId: DEMO.vdmsId, docker: DEMO.docker })

  useEffect(() => { localStorage.setItem('sclera.nav', nav) }, [nav])
  useEffect(() => { localStorage.setItem('sclera.view', view) }, [view])
  useEffect(() => {
    if (property) localStorage.setItem('sclera.property', JSON.stringify(property))
  }, [property])

  return (
    <AppCtx.Provider value={{ nav, setNav, view, setView, property, setProperty, ctx }}>
      {children}
    </AppCtx.Provider>
  )
}

export function useApp() {
  const ctx = useContext(AppCtx)
  if (!ctx) throw new Error('useApp must be used within AppProvider')
  return ctx
}
