import type { ReactNode } from 'react'

export function TableToolbar({ children, extra }: { children: ReactNode; extra?: ReactNode }) {
  return <div className="table-toolbar"><div className="table-toolbar-filters">{children}</div>{extra && <div>{extra}</div>}</div>
}
