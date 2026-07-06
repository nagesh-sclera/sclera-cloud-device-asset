// Renders a PDF's pages to PNG image Files so the QR decoder (which only takes
// images) can read QR codes out of a PDF — e.g. the backend's generated QR
// label bundles. Uses pdf.js; the worker is bundled by Vite via the ?url import.
import * as pdfjsLib from 'pdfjs-dist'
import workerUrl from 'pdfjs-dist/build/pdf.worker.min.mjs?url'

pdfjsLib.GlobalWorkerOptions.workerSrc = workerUrl

// scale 3 keeps small QR labels crisp enough to decode; maxPages caps work on
// big bundles (we stop early once a code is found, so this is just a ceiling).
export async function pdfToImageFiles(file, { scale = 3, maxPages = 15 } = {}) {
  const data = await file.arrayBuffer()
  const pdf = await pdfjsLib.getDocument({ data }).promise
  const files = []
  try {
    const count = Math.min(pdf.numPages, maxPages)
    for (let i = 1; i <= count; i++) {
      const page = await pdf.getPage(i)
      const viewport = page.getViewport({ scale })
      const canvas = document.createElement('canvas')
      canvas.width = Math.ceil(viewport.width)
      canvas.height = Math.ceil(viewport.height)
      const canvasContext = canvas.getContext('2d')
      await page.render({ canvasContext, viewport }).promise
      const blob = await new Promise((res) => canvas.toBlob(res, 'image/png'))
      if (blob) files.push(new File([blob], `qr-page-${i}.png`, { type: 'image/png' }))
    }
  } finally {
    try { await pdf.destroy() } catch { /* ignore */ }
  }
  return files
}
