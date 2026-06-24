import { createContext, useContext, useEffect, useState } from 'react'
import { DEMO } from '../config.js'

const AppCtx = createContext(null)

// Lightweight app-wide state. Persists selected nav/property to localStorage.
// Views the app can render. A persisted view outside this set (e.g. the removed
// 'onboarding') falls back to 'properties' so the content area never renders blank.
const KNOWN_VIEWS = ['properties', 'dashboard', 'assets', 'location', 'activity', 'networks', 'workorders', 'qrcodes']

export function AppProvider({ children }) {
  const [nav, setNav] = useState(() => localStorage.getItem('sclera.nav') || 'properties')
  const [view, setView] = useState(() => {
    const saved = localStorage.getItem('sclera.view')
    return KNOWN_VIEWS.includes(saved) ? saved : 'properties'
  })
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
