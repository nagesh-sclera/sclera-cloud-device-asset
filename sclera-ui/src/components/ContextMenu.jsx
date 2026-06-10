import { useEffect, useRef } from 'react'
import Icon from './Icon.jsx'

// Anchored menu. `pos` = {x, y} in viewport coords. Closes on outside click / Esc.
export default function ContextMenu({ pos, items, onClose }) {
  const ref = useRef(null)
  useEffect(() => {
    const onDoc = (e) => { if (ref.current && !ref.current.contains(e.target)) onClose() }
    const onKey = (e) => { if (e.key === 'Escape') onClose() }
    document.addEventListener('mousedown', onDoc)
    document.addEventListener('keydown', onKey)
    return () => { document.removeEventListener('mousedown', onDoc); document.removeEventListener('keydown', onKey) }
  }, [onClose])

  if (!pos) return null
  const style = { top: Math.min(pos.y, window.innerHeight - (items.length * 40 + 16)), left: Math.min(pos.x, window.innerWidth - 200) }

  return (
    <div className="context-menu" style={style} ref={ref}>
      {items.map((it, i) => (
        it.divider ? <div className="ctx-divider" key={i} /> : (
          <button key={i} className={`ctx-item ${it.danger ? 'danger' : ''}`} onClick={() => { it.onClick?.(); onClose() }}>
            {it.icon && <Icon name={it.icon} size={15} />}
            <span>{it.label}</span>
          </button>
        )
      ))}
    </div>
  )
}
