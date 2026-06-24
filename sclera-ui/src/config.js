// Central API configuration. BASE_URL is configurable via the VITE_API_BASE env
// var so the same build can point at local docker-compose, QA, or any gateway.
const env = import.meta.env || {}

export const API_CONFIG = {
  BASE_URL: env.VITE_API_BASE || 'http://localhost:8080',
  // The /asset route is StripPrefix=1 at the gateway, so after stripping "/asset"
  // the downstream controller base mapping is reached.
  ASSET_PREFIX: '/asset/api/v1/sclera-cloud-device-asset-service',
  VDMS_PREFIX: '/vdms',
}

// Demo context used to scope calls. Matches the live backend data.
// NOTE: docker name must not be a SQL reserved word ("default" crashes the
// native device queries) — the live gateway uses "right_wing".
export const DEMO = {
  user: env.VITE_DEMO_USER || 'admin',
  displayName: env.VITE_DEMO_DISPLAY || 'Master User',
  vdmsId: env.VITE_DEMO_VDMS || 'VDMS760',
  docker: env.VITE_DEMO_DOCKER || 'right_wing',
}

export const NAV_ITEMS = [
  { key: 'properties', label: 'Property List', icon: 'grid' },
  { key: 'products', label: 'My Products', icon: 'box' },
  { key: 'users', label: 'User List', icon: 'users' },
  { key: 'proxy', label: 'Proxy Networks', icon: 'network' },
  { key: 'inventory', label: 'Inventory', icon: 'layers' },
  { key: 'workorders', label: 'Work Orders', icon: 'clipboard' },
  { key: 'qrcodes', label: 'QR Codes', icon: 'qrcode' },
  { key: 'adc', label: 'Asset Data Collection', icon: 'database' },
  { key: 'portal', label: 'Client Portal', icon: 'portal' },
  { key: 'config', label: 'Global Configuration', icon: 'globe' },
]

export const NAV_FOOTER = [
  { key: 'barcode', label: 'Scan Bar Code', icon: 'barcode' },
  { key: 'qrcode', label: 'Scan QR Code', icon: 'qrcode' },
]

// Ticket status vocabulary (matches the workorders service count-map keys).
export const TICKET_STATUSES = ['new', 'open', 'on_hold', 'closed']
export const statusLabel = (s) => ({ new: 'New', open: 'Open', on_hold: 'On Hold', closed: 'Closed' }[s] || s || '—')
export const statusTone = (s) => ({ new: 'default', open: 'online', on_hold: 'default', closed: 'muted' }[s] || 'default')

// Ticket category IS the type discriminator the workorders service requires: it drives the
// ticket-number prefix (incident_request -> INC-, service_request -> SRN-) and the derived
// name. A missing/unknown category makes upsertticket 500, so the UI must send one of these.
export const TICKET_CATEGORIES = ['incident_request', 'service_request']
export const categoryLabel = (c) => ({ incident_request: 'Incident Request', service_request: 'Service Request' }[c] || c || '—')

// status int -> {label, color-token}
export function statusInfo(device) {
  const monitored = device?.monitor === 1
  if (!monitored) return { label: 'Unmonitored', tone: 'muted' }
  if (device?.status === 1) return { label: 'Online', tone: 'online' }
  if (device?.status === 0) return { label: 'Offline', tone: 'offline' }
  return { label: 'Monitored', tone: 'default' }
}
