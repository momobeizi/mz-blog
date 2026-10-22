import type { ReactNode } from 'react'

interface Props {
  title: string
  description?: string
  eyebrow?: string
  extra?: ReactNode
}

export function PageTitle({ title, description, eyebrow, extra }: Props) {
  return (
    <div className="page-title-row">
      <div>
        {eyebrow && <span className="page-eyebrow">{eyebrow}</span>}
        <h1>{title}</h1>
        {description && <p>{description}</p>}
      </div>
      {extra && <div className="page-title-actions">{extra}</div>}
    </div>
  )
}
