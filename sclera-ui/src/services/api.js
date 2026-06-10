import { API_CONFIG, DEMO } from '../config.js'

const { BASE_URL, ASSET_PREFIX, VDMS_PREFIX } = API_CONFIG

function qs(params) {
  const sp = new URLSearchParams()
  Object.entries(params || {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null) sp.append(k, v)
  })
  const s = sp.toString()
  return s ? `?${s}` : ''
}

// Core fetch with timeout + one retry on network/5xx failures.
async function request(url, { method = 'GET', body, headers, retries = 1, timeout = 15000 } = {}) {
  const ctrl = new AbortController()
  const timer = setTimeout(() => ctrl.abort(), timeout)
  try {
    const res = await fetch(url, {
      method,
      mode: 'cors',
      signal: ctrl.signal,
      headers: { 'Content-Type': 'application/json', ...(headers || {}) },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
    clearTimeout(timer)
    if (!res.ok) {
      if (res.status >= 500 && retries > 0) {
        return request(url, { method, body, headers, retries: retries - 1, timeout })
      }
      const text = await res.text().catch(() => '')
      const err = new Error(`HTTP ${res.status} ${res.statusText}${text ? ` — ${text.slice(0, 200)}` : ''}`)
      err.status = res.status
      throw err
    }
    if (res.status === 204) return null
    const ct = res.headers.get('content-type') || ''
    if (ct.includes('application/json')) return res.json()
    const txt = await res.text()
    try { return JSON.parse(txt) } catch { return txt }
  } catch (e) {
    clearTimeout(timer)
    if (e.name === 'AbortError') throw new Error('Request timed out')
    if (retries > 0 && e.name === 'TypeError') {
      // network error (e.g. backend not up yet) — retry once
      return request(url, { method, body, headers, retries: retries - 1, timeout })
    }
    throw e
  }
}

const asset = (p) => `${BASE_URL}${ASSET_PREFIX}${p}`
const vdms = (p) => `${BASE_URL}${VDMS_PREFIX}${p}`
// Sensors are owned by the integrations service (gateway /integrations/** route).
const integrations = (p) => `${BASE_URL}/integrations${p}`

// Scope params present on nearly every device call.
function scope({ user = DEMO.user, vdmsId = DEMO.vdmsId } = {}) {
  return { username: user, vdmsid: vdmsId, vdms_id: vdmsId }
}

export const api = {
  // ---- Properties / VDMS ----
  getVdmsId: () => request(vdms('/id')),
  getVdmsDetails: () => request(vdms('/details')),

  // ---- Assets ----
  // Browse: the real app's primary list call (top-level / parent devices).
  listParentDevices: ({ docker = DEMO.docker, condition = 'all', pageno = 1, pagesize = 12, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/getsubsystemparentdevicesbypagination${qs({ ...scope(ctx), condition, pageno, pagesize, assignee: 'all' })}`)),

  // Filtered/search list (supports searchKey + condition).
  listDevices: ({ docker = DEMO.docker, condition = 'all', searchKey = 'null', pageno = 1, pagesize = 12, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/getfilterdevice${qs({ ...scope(ctx), condition, searchKey, pageno, pagesize })}`)),

  // Multi-keyword search/sort/filter (POST) — the dedicated filter API (img_9).
  searchSortFilter: (criteria = {}, { docker = DEMO.docker, condition = 'all', pageno = 1, pagesize = 50, onboard_status = 123, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/searchsortfilterdevices${qs({ ...scope(ctx), condition, pageno, pagesize, onboard_status })}`), {
      method: 'POST', body: criteria,
    }),

  // Count for the same filter (img_9).
  searchSortFilterCount: (criteria = {}, { docker = DEMO.docker, condition = 'all', onboard_status = 123, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/searchsortfilterdevicescount${qs({ ...scope(ctx), condition, onboard_status })}`), {
      method: 'POST', body: criteria,
    }),

  // Filter option endpoints (img_10).
  alertMessages: ({ docker = DEMO.docker, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/getalertmessages${qs(scope(ctx))}`)),
  assignedEmails: ({ network = DEMO.docker, vdmsId = DEMO.vdmsId } = {}) =>
    request(asset(`/device/network/${encodeURIComponent(network)}/getassignedemail${qs({ vdms_id: vdmsId })}`)),
  uniqueDeviceTypes: ({ vdmsId = DEMO.vdmsId, network = 'all', user = DEMO.user } = {}) =>
    request(asset(`/getuniquedevicetypes${qs({ vdms_id: vdmsId, username: user, network_name: network, floor_id: 'null' })}`)),
  uniqueAssetGroups: ({ vdmsId = DEMO.vdmsId, network = 'all', user = DEMO.user } = {}) =>
    request(asset(`/getuniqueassetgroups${qs({ vdms_id: vdmsId, username: user, network_name: network })}`)),
  uniqueCategory: ({ vdmsId = DEMO.vdmsId, network = 'all', user = DEMO.user } = {}) =>
    request(asset(`/getuniquecategory${qs({ vdms_id: vdmsId, username: user, network_name: network })}`)),

  // Filter field definitions (custom asset fields).
  assetFields: ({ vdmsId = DEMO.vdmsId, user = DEMO.user } = {}) =>
    request(asset(`/asset-fields${qs({ vdms_id: vdmsId, username: user })}`)),

  // Audit / activity log (vdms-service).
  auditLog: ({ vdmsId = DEMO.vdmsId, page = 0, size = 50 } = {}) =>
    request(`${BASE_URL}${VDMS_PREFIX}/audit-log${qs({ vdmsId, page, size })}`),

  archiveDevices: (ids, { docker = DEMO.docker, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/archivedevices${qs({ ...scope(ctx), assignee: 'all' })}`), {
      method: 'POST', body: ids,
    }),

  // ---- Sensors (owned by the integrations service; device_id relates them) ----
  listSensors: (deviceId, { vdmsId = DEMO.vdmsId, user = DEMO.user } = {}) =>
    request(integrations(`/sensors/device/${encodeURIComponent(deviceId)}${qs({ username: user, vdmsid: vdmsId })}`)),

  addSensor: (deviceId, sensor, { vdmsId = DEMO.vdmsId, user = DEMO.user } = {}) =>
    request(integrations(`/sensors/device/${encodeURIComponent(deviceId)}${qs({ username: user, vdmsid: vdmsId })}`), {
      method: 'POST', body: sensor,
    }),

  deleteSensor: (deviceId, sensorId, { vdmsId = DEMO.vdmsId, user = DEMO.user } = {}) =>
    request(integrations(`/sensors/device/${encodeURIComponent(deviceId)}/${encodeURIComponent(sensorId)}${qs({ username: user, vdmsid: vdmsId })}`), {
      method: 'DELETE',
    }),

  // ---- Notes (real, device-asset service) ----
  listNotes: (deviceId, { docker = DEMO.docker, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/device/${encodeURIComponent(deviceId)}/notes${qs(scope(ctx))}`)),
  addNote: (deviceId, note, { docker = DEMO.docker, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/device/${encodeURIComponent(deviceId)}/note${qs(scope(ctx))}`), {
      method: 'POST', body: note,
    }),
  deleteNote: (deviceId, noteId, { docker = DEMO.docker, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/device/${encodeURIComponent(deviceId)}/note/${encodeURIComponent(noteId)}${qs(scope(ctx))}`), {
      method: 'DELETE',
    }),

  deviceCount: ({ docker = DEMO.docker, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/getdevicecount${qs({ ...scope(ctx), assignee: 'all' })}`)),

  getDevice: (deviceId, { docker = DEMO.docker, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/device/${encodeURIComponent(deviceId)}/getdevice${qs(scope(ctx))}`)),

  upsertDevices: (devices, { docker = DEMO.docker, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/devicesupsert${qs({ ...scope(ctx), assignee: 'all' })}`), {
      method: 'POST', body: Array.isArray(devices) ? devices : [devices],
    }),

  editDevice: (deviceId, device, { docker = DEMO.docker, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/device/${encodeURIComponent(deviceId)}/edit${qs({ ...scope(ctx), assignee: 'all' })}`), {
      method: 'POST', body: device,
    }),

  // Onboard / un-onboard a device (status 3 = onboarded, 0 = not onboarded).
  onboard: (deviceId, { docker = DEMO.docker, status = 3, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/device/${encodeURIComponent(deviceId)}/onboard${qs({ ...scope(ctx), status })}`), {
      method: 'POST',
    }),

  deleteDevices: (ids, { docker = DEMO.docker, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/deletedevices${qs({ ...scope(ctx), assignee: 'all' })}`), {
      method: 'DELETE', body: ids,
    }),

  // ---- Networks (gateways / dockers) ----
  listNetworks: ({ vdmsId = DEMO.vdmsId } = {}) =>
    request(asset(`/networks${qs({ vdms_id: vdmsId })}`)),
  addNetwork: (net, { vdmsId = DEMO.vdmsId, user = DEMO.user } = {}) =>
    request(asset(`/network${qs({ vdms_id: vdmsId, username: user })}`), { method: 'POST', body: net }),
  moveDeviceNetwork: (deviceId, dockerName, { vdmsId = DEMO.vdmsId, user = DEMO.user } = {}) =>
    request(asset(`/device/${encodeURIComponent(deviceId)}/network${qs({ docker_name: dockerName, vdms_id: vdmsId, username: user })}`), { method: 'POST' }),
  // Stamp updated_timestamp/updated_email (editDevice doesn't touch them).
  touchDevice: (deviceId, { vdmsId = DEMO.vdmsId, user = DEMO.user } = {}) =>
    request(asset(`/device/${encodeURIComponent(deviceId)}/touch${qs({ username: user, vdmsid: vdmsId })}`), { method: 'POST' }),

  // ---- Dropdown data ----
  getBuildings: ({ vdmsId = DEMO.vdmsId } = {}) =>
    request(asset(`/getbuildingsbyvdmsid${qs({ vdms_id: vdmsId })}`)),

  getLocations: ({ ...ctx } = {}) =>
    request(asset(`/getlocations${qs(scope(ctx))}`)),

  getFloorsByBuilding: (buildingId, { vdmsId = DEMO.vdmsId, user = DEMO.user } = {}) =>
    request(asset(`/building/${encodeURIComponent(buildingId)}/getfloorsbybuildingid${qs({ username: user, vdms_id: vdmsId, building_id: buildingId })}`)),

  getLocationsByFloor: (floorId, { vdmsId = DEMO.vdmsId, user = DEMO.user } = {}) =>
    request(asset(`/floor/${encodeURIComponent(floorId)}/getlocationsbyfloorid${qs({ username: user, vdms_id: vdmsId })}`)),

  addBuilding: ({ name, code = '' }, { vdmsId = DEMO.vdmsId, user = DEMO.user } = {}) =>
    request(asset(`/upsertbuildings${qs({ username: user, vdms_id: vdmsId })}`), { method: 'POST', body: [{ name, code }] }),

  addFloor: (buildingId, name, { vdmsId = DEMO.vdmsId, user = DEMO.user } = {}) =>
    request(asset(`/building/${encodeURIComponent(buildingId)}/upsertfloors${qs({ username: user, vdms_id: vdmsId })}`), { method: 'POST', body: [{ name }] }),

  addLocation: (floorId, name, { vdmsId = DEMO.vdmsId, user = DEMO.user } = {}) =>
    request(asset(`/floor/${encodeURIComponent(floorId)}/upsertlocations${qs({ username: user, vdms_id: vdmsId })}`), { method: 'POST', body: [{ name }] }),
}

export default api
