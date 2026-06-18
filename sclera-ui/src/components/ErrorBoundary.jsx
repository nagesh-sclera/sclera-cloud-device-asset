import { Component } from 'react'

export default class ErrorBoundary extends Component {
  constructor(props) { super(props); this.state = { error: null } }
  static getDerivedStateFromError(error) { return { error } }
  componentDidCatch(error, info) { console.error('UI crash:', error, info) }
  render() {
    if (this.state.error) {
      return (
        <div className="crash">
          <h2>Something went wrong</h2>
          <pre>{String(this.state.error?.message || this.state.error)}</pre>
          <button className="btn btn-primary" onClick={() => this.setState({ error: null })}>Try again</button>
        </div>
      )
    }
    return this.props.children
  }
}
