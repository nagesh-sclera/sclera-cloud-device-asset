import { useEffect } from 'react'
import Icon from './Icon.jsx'
import { Spinner } from './Skeleton.jsx'

// Lightbox preview for a single asset image. Click the backdrop or the × to close.
// When an `onDelete` handler is supplied, a "Delete image" button is shown — used to
// remove the image from disk AND clear asset_image_url via the backend.
export default function ImagePreview({ src, alt = 'Asset image', open, onClose, onDelete, deleting = false }) {
  // Close on Escape for keyboard users.
  useEffect(() => {
    if (!open) return
    const onKey = (e) => { if (e.key === 'Escape') onClose?.() }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [open, onClose])

  if (!open || !src) return null

  return (
    <div className="img-preview-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose?.() }}>
      <div className="img-preview" role="dialog" aria-modal="true" aria-label="Image preview">
        <div className="img-preview-bar">
          {onDelete ? (
            <button className="btn btn-danger sm" disabled={deleting} onClick={onDelete}>
              {deleting ? <Spinner size={14} /> : <Icon name="trash" size={14} />} Delete image
            </button>
          ) : <span />}
          <button className="icon-btn" onClick={onClose} aria-label="Close preview"><Icon name="x" size={18} /></button>
        </div>
        <div className="img-preview-stage"><img src={src} alt={alt} /></div>
      </div>
    </div>
  )
}
