// Deterministic dummy data per device, representing what the sibling skeleton
// microservices (sensors/edge, inspection, workorders, inventory, alerts) would
// contribute to an asset's detail view. Stable for a given device id so the demo
// looks consistent across reloads. Swap any generator for a real gateway call
// (/inspection, /workorders, /inventory, /alerts) when those services are live.

function hash(str) {
  let h = 2166136261
  for (let i = 0; i < (str || '').length; i++) { h ^= str.charCodeAt(i); h = Math.imul(h, 16777619) }
  return Math.abs(h)
}
// seeded picker
function rng(seed) {
  let s = seed % 2147483647
  if (s <= 0) s += 2147483646
  return () => (s = (s * 16807) % 2147483647) / 2147483647
}
const pick = (r, arr) => arr[Math.floor(r() * arr.length)]
const ago = (mins) => {
  const d = new Date(Date.now() - mins * 60000)
  return d.toLocaleString()
}

export function sensorsFor(device) {
  const r = rng(hash(device?.id) + 11)
  const defs = [
    { name: 'Ambient Temperature', unit: '°C', lo: 18, hi: 27 },
    { name: 'Relative Humidity', unit: '%', lo: 35, hi: 65 },
    { name: 'Power Draw', unit: 'W', lo: 40, hi: 240 },
    { name: 'CPU Load', unit: '%', lo: 5, hi: 80 },
    { name: 'Signal Strength', unit: 'dBm', lo: -90, hi: -45 },
    { name: 'Door State', unit: '', lo: 0, hi: 1 },
  ]
  const n = 3 + Math.floor(r() * 3)
  return defs.slice(0, n).map((d, i) => {
    const val = d.unit === '' ? (r() > 0.5 ? 'Closed' : 'Open') : (d.lo + r() * (d.hi - d.lo)).toFixed(d.unit === 'dBm' ? 0 : 1)
    const status = pick(r, ['ok', 'ok', 'ok', 'warn', 'alert'])
    return { id: `${device.id}-s${i}`, name: d.name, value: val, unit: d.unit, status, protocol: pick(r, ['SNMP', 'BACnet', 'LoRaWAN', 'Modbus']), lastSeen: ago(Math.floor(r() * 30) + 1) }
  })
}

export function inspectionsFor(device) {
  const r = rng(hash(device?.id) + 23)
  const titles = ['Quarterly Safety Check', 'Preventive Maintenance', 'Compliance Audit', 'Thermal Imaging', 'Firmware Review']
  const inspectors = ['A. Mehta', 'J. Rivera', 'S. Okafor', 'L. Chen']
  const n = 2 + Math.floor(r() * 3)
  return Array.from({ length: n }).map((_, i) => ({
    id: `${device.id}-insp${i}`,
    title: pick(r, titles),
    status: pick(r, ['Passed', 'Passed', 'Pending', 'Failed']),
    score: 60 + Math.floor(r() * 40),
    inspector: pick(r, inspectors),
    date: new Date(Date.now() - Math.floor(r() * 60) * 86400000).toLocaleDateString(),
  }))
}

export function workOrdersFor(device) {
  const r = rng(hash(device?.id) + 31)
  const titles = ['Replace cooling fan', 'Recalibrate sensor', 'Firmware upgrade', 'Inspect wiring', 'Filter replacement']
  const n = 1 + Math.floor(r() * 3)
  return Array.from({ length: n }).map((_, i) => ({
    id: `WO-${1000 + (hash(device.id) % 9000) + i}`,
    title: pick(r, titles),
    priority: pick(r, ['Low', 'Medium', 'High', 'Critical']),
    status: pick(r, ['New', 'Open', 'On Hold', 'Flagged']),
    assignee: pick(r, ['tech.team', 'a.mehta', 'j.rivera']),
    due: new Date(Date.now() + Math.floor(r() * 14) * 86400000).toLocaleDateString(),
  }))
}

export function inventoryFor(device) {
  const r = rng(hash(device?.id) + 43)
  const parts = ['Air filter (HEPA)', 'Replacement battery', 'Network cable Cat6', 'Mounting bracket', 'Thermal paste']
  const n = 2 + Math.floor(r() * 2)
  return {
    trackingId: device?.inventory_tracking_id || `INV-${100000 + (hash(device.id) % 900000)}`,
    location: pick(r, ['Store Room A', 'Rack 12', 'Warehouse North', 'Spare Cabinet']),
    parts: Array.from({ length: n }).map((_, i) => ({
      id: `${device.id}-p${i}`, name: pick(r, parts), qty: 1 + Math.floor(r() * 12), status: pick(r, ['In Stock', 'In Stock', 'Low', 'Reorder']),
    })),
  }
}

export function alertsFor(device) {
  const r = rng(hash(device?.id) + 53)
  if (r() < 0.25) return []
  const msgs = ['Temperature above threshold', 'Device unreachable', 'Power fluctuation detected', 'Firmware out of date', 'Unauthorized access attempt']
  const n = 1 + Math.floor(r() * 2)
  return Array.from({ length: n }).map((_, i) => ({
    id: `${device.id}-al${i}`, severity: pick(r, ['info', 'warning', 'critical']), message: pick(r, msgs), time: ago(Math.floor(r() * 180) + 2),
  }))
}

export function documentsFor(device) {
  const r = rng(hash(device?.id) + 67)
  const docs = ['Datasheet.pdf', 'Warranty.pdf', 'Install-Guide.pdf', 'Compliance-Cert.pdf', 'Photo-front.jpg']
  const n = 2 + Math.floor(r() * 2)
  return Array.from({ length: n }).map((_, i) => ({
    id: `${device.id}-doc${i}`, name: pick(r, docs), kind: pick(r, ['PDF', 'PDF', 'Image']), size: `${(r() * 4 + 0.2).toFixed(1)} MB`,
  }))
}
