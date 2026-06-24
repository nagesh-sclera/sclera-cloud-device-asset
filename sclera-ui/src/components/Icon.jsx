// Minimal inline-SVG icon set (stroke = currentColor) used across the app.
const P = { fill: 'none', stroke: 'currentColor', strokeWidth: 1.8, strokeLinecap: 'round', strokeLinejoin: 'round' }

const paths = {
  grid: <><rect x="3" y="3" width="7" height="7" rx="1" {...P} /><rect x="14" y="3" width="7" height="7" rx="1" {...P} /><rect x="3" y="14" width="7" height="7" rx="1" {...P} /><rect x="14" y="14" width="7" height="7" rx="1" {...P} /></>,
  box: <><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" {...P} /><path d="M3.27 6.96 12 12l8.73-5.04M12 22V12" {...P} /></>,
  users: <><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" {...P} /><circle cx="9" cy="7" r="4" {...P} /><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" {...P} /></>,
  network: <><rect x="9" y="2" width="6" height="6" rx="1" {...P} /><rect x="2" y="16" width="6" height="6" rx="1" {...P} /><rect x="16" y="16" width="6" height="6" rx="1" {...P} /><path d="M12 8v4M12 12H5v4M12 12h7v4" {...P} /></>,
  layers: <><path d="m12 2 9 5-9 5-9-5 9-5zM3 12l9 5 9-5M3 17l9 5 9-5" {...P} /></>,
  database: <><ellipse cx="12" cy="5" rx="9" ry="3" {...P} /><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5M3 12c0 1.66 4 3 9 3s9-1.34 9-3" {...P} /></>,
  portal: <><circle cx="12" cy="12" r="10" {...P} /><path d="M2 12h20M12 2a15 15 0 0 1 0 20 15 15 0 0 1 0-20z" {...P} /></>,
  globe: <><circle cx="12" cy="12" r="10" {...P} /><path d="M2 12h20M12 2a15 15 0 0 1 0 20 15 15 0 0 1 0-20z" {...P} /></>,
  barcode: <><path d="M3 5v14M7 5v14M11 5v14M14 5v14M18 5v14M21 5v14" {...P} /></>,
  qrcode: <><rect x="3" y="3" width="7" height="7" rx="1" {...P} /><rect x="14" y="3" width="7" height="7" rx="1" {...P} /><rect x="3" y="14" width="7" height="7" rx="1" {...P} /><path d="M14 14h3v3M21 14v7M14 21h7" {...P} /></>,
  settings: <><circle cx="12" cy="12" r="3" {...P} /><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-2.82 1.17V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 8 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 3.6 14H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 8.6l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 5.6V5a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 2.82 1.17l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 20.4 9H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" {...P} /></>,
  search: <><circle cx="11" cy="11" r="7" {...P} /><path d="m21 21-4.3-4.3" {...P} /></>,
  refresh: <><path d="M23 4v6h-6M1 20v-6h6" {...P} /><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" {...P} /></>,
  plus: <><path d="M12 5v14M5 12h14" {...P} /></>,
  edit: <><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" {...P} /><path d="M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" {...P} /></>,
  trash: <><path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" {...P} /></>,
  more: <><circle cx="12" cy="5" r="1.6" fill="currentColor" stroke="none" /><circle cx="12" cy="12" r="1.6" fill="currentColor" stroke="none" /><circle cx="12" cy="19" r="1.6" fill="currentColor" stroke="none" /></>,
  device: <><rect x="2" y="4" width="20" height="14" rx="2" {...P} /><path d="M8 21h8M12 18v3" {...P} /></>,
  list: <><path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01" {...P} /></>,
  info: <><circle cx="12" cy="12" r="10" {...P} /><path d="M12 16v-4M12 8h.01" {...P} /></>,
  x: <><path d="M18 6 6 18M6 6l12 12" {...P} /></>,
  camera: <><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" {...P} /><circle cx="12" cy="13" r="4" {...P} /></>,
  upload: <><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" {...P} /></>,
  import: <><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" {...P} /></>,
  sliders: <><path d="M4 21v-7M4 10V3M12 21v-9M12 8V3M20 21v-5M20 12V3M1 14h6M9 8h6M17 16h6" {...P} /></>,
  filter: <><path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" {...P} /></>,
  'arrow-right': <><path d="M5 12h14M13 6l6 6-6 6" {...P} /></>,
  clipboard: <><path d="M9 2h6a1 1 0 0 1 1 1v1h2a2 2 0 0 1 2 2v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2V3a1 1 0 0 1 1-1z" {...P} /><path d="M9 4h6" {...P} /><path d="M8 11h8M8 15h5" {...P} /></>,
}

export default function Icon({ name, size = 18, className = '', style }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" className={className} style={style} aria-hidden>
      {paths[name] || paths.device}
    </svg>
  )
}
