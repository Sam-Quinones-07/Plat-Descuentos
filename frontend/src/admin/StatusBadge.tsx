import { type BusinessAccount } from "../types";

export function StatusBadge({
  status,
}: {
  status: BusinessAccount["status"];
}) {
  const labels = {
    pending: "Pendiente",
    approved: "Aprobada",
    rejected: "Rechazada",
  };
  return (
    <span className={`status-badge status-badge--${status}`}>
      <span className="status-badge__dot" />
      {labels[status]}
    </span>
  );
}
