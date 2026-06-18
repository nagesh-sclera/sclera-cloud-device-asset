export function Skeleton({ w = '100%', h = 14, r = 6, style }) {
  return <span className="skeleton" style={{ width: w, height: h, borderRadius: r, ...style }} />
}

export function CardSkeleton({ count = 4 }) {
  return (
    <div className="metric-grid">
      {Array.from({ length: count }).map((_, i) => (
        <div className="card metric-card" key={i}>
          <Skeleton w="50%" h={16} />
          <div style={{ height: 12 }} />
          <Skeleton w="80%" h={40} />
        </div>
      ))}
    </div>
  )
}

export function RowSkeleton({ count = 6 }) {
  return (
    <div className="asset-list">
      {Array.from({ length: count }).map((_, i) => (
        <div className="asset-row" key={i}>
          <Skeleton w={40} h={40} r={8} />
          <div style={{ flex: 1 }}>
            <Skeleton w="40%" h={14} />
            <div style={{ height: 8 }} />
            <Skeleton w="25%" h={10} />
          </div>
        </div>
      ))}
    </div>
  )
}

export function Spinner({ size = 18 }) {
  return <span className="spinner" style={{ width: size, height: size }} />
}
