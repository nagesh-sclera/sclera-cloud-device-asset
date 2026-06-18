// Session-only store for asset images added in the UI but NOT yet persisted to the
// backend. Keyed by device id, holds a data URL. Read as a fallback after the real
// asset_image_url so an image added when creating an asset still appears on the card /
// detail panel. Lost on a full page refresh — by design, since it isn't saved server-side.
const localImages = new Map()

export const setLocalImage = (id, dataUrl) => {
  if (!id) return
  if (dataUrl) localImages.set(id, dataUrl)
  else localImages.delete(id) // empty value clears the cached image (e.g. after a delete)
}

export const getLocalImage = (id) => (id ? localImages.get(id) : undefined)
