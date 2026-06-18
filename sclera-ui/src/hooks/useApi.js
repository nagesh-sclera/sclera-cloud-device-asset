import { useCallback, useEffect, useRef, useState } from 'react'

// Generic async data hook with loading/error/refetch.
export function useApi(fn, deps = [], { immediate = true } = {}) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(immediate)
  const [error, setError] = useState(null)
  const mounted = useRef(true)
  const fnRef = useRef(fn)
  fnRef.current = fn

  useEffect(() => { mounted.current = true; return () => { mounted.current = false } }, [])

  const run = useCallback(async (...args) => {
    setLoading(true); setError(null)
    try {
      const res = await fnRef.current(...args)
      if (mounted.current) setData(res)
      return res
    } catch (e) {
      if (mounted.current) setError(e)
      throw e
    } finally {
      if (mounted.current) setLoading(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (immediate) run().catch(() => {})
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  return { data, loading, error, refetch: run, setData }
}

// Debounced value hook for search inputs.
export function useDebounce(value, delay = 350) {
  const [v, setV] = useState(value)
  useEffect(() => {
    const t = setTimeout(() => setV(value), delay)
    return () => clearTimeout(t)
  }, [value, delay])
  return v
}
