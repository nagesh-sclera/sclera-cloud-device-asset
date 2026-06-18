// A dashboard metric card: title + a row of labelled counts.
export default function MetricCard({ title, items, accent = 'blue', onClick }) {
  return (
    <div className={`card metric-card accent-${accent}`} onClick={onClick} role={onClick ? 'button' : undefined}>
      <div className="metric-head">{title}</div>
      <div className="metric-body">
        {items.map((it) => (
          <div className="metric-item" key={it.label}>
            <span className={`metric-num tone-${it.tone || 'default'}`}>{it.value ?? 0}</span>
            <span className="metric-label">{it.label}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
