import { createContext, useCallback, useContext, useState } from 'react'

const ToastCtx = createContext(null)
let nextId = 1

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const remove = useCallback((id) => setToasts((t) => t.filter((x) => x.id !== id)), [])

  const push = useCallback((message, type = 'info', ttl = 4000) => {
    const id = nextId++
    setToasts((t) => [...t, { id, message, type }])
    if (ttl) setTimeout(() => remove(id), ttl)
    return id
  }, [remove])

  const toast = {
    success: (m) => push(m, 'success'),
    error: (m) => push(m, 'error', 6000),
    info: (m) => push(m, 'info'),
  }

  return (
    <ToastCtx.Provider value={toast}>
      {children}
      <div className="toast-wrap">
        {toasts.map((t) => (
          <div key={t.id} className={`toast toast-${t.type}`} onClick={() => remove(t.id)}>
            <span className="toast-dot" />
            <span className="toast-msg">{t.message}</span>
            <span className="toast-x">×</span>
          </div>
        ))}
      </div>
    </ToastCtx.Provider>
  )
}

export function useToast() {
  const ctx = useContext(ToastCtx)
  if (!ctx) throw new Error('useToast must be used within ToastProvider')
  return ctx
}
