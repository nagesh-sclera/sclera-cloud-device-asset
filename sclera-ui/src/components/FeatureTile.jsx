import Icon from './Icon.jsx'

export default function FeatureTile({ label, icon = 'grid', onClick }) {
  return (
    <button className="feature-tile" onClick={onClick}>
      <Icon name={icon} size={26} />
      <span>{label}</span>
    </button>
  )
}
