// Resolves a device's `asset_image_url` to a single src usable in an <img> tag.
//
// The backend stores it the same way the root project does: a JSON array of hosted URLs, e.g.
//   ["http://localhost:8085/images/assets/29399_1909140104380190.png"]
// Older/edge values may be a bare URL or a legacy base64 data URL, so handle all three.
export function firstAssetImage(raw) {
  if (!raw || typeof raw !== 'string') return ''
  const s = raw.trim()
  if (!s) return ''
  if (s.startsWith('[')) {
    try {
      const arr = JSON.parse(s)
      if (Array.isArray(arr) && arr.length) return String(arr[0])
    } catch { /* not valid JSON — fall through */ }
    return ''
  }
  // bare hosted URL ("http://…") or legacy data URL ("data:image/…")
  return s
}
