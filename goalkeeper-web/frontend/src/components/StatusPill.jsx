const STYLES = {
  ACTIVE: 'bg-cobalt/10 text-cobalt',
  COMPLETED: 'bg-amber/20 text-navy-700',
  ARCHIVED: 'bg-danger/10 text-danger',
};

const LABELS = {
  ACTIVE: 'Active',
  COMPLETED: 'Completed',
  ARCHIVED: 'Archived',
};

export default function StatusPill({ status }) {
  return <span className={`pill ${STYLES[status] || 'bg-line text-slate'}`}>{LABELS[status] || status}</span>;
}
