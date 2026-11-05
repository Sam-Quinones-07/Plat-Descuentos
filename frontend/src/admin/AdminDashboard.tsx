import { useState } from "react";
import { AdminShell } from "./AdminShell";
import { type BusinessAccount } from "../types";
import { Button, Icon } from "../components";
import { StatusBadge } from "./StatusBadge";

export function AdminDashboard({
  requests,
  onReview,
  onLogout,
}: {
  requests: BusinessAccount[];
  onReview: (request: BusinessAccount) => void;
  onLogout: () => void;
}) {
  const [filter, setFilter] = useState<"pending" | "reviewed">("pending");
  const visibleRequests = requests.filter((r) =>
    filter === "pending" ? r.status === "pending" : r.status !== "pending",
  );
  const pendingCount = requests.filter((r) => r.status === "pending").length;
  const reviewedCount = requests.length - pendingCount;

  return (
    <AdminShell onLogout={onLogout}>
      <div className="admin-page-heading">
        <span className="eyebrow">REVISIÓN DE COMERCIOS</span>
        <p className="admin-page-title">Solicitudes de afiliación</p>
        <p className="page-description">
          Consulta la información registrada y decide sobre las solicitudes de
          los establecimientos.
        </p>
      </div>

      <section className="admin-section">
        <div className="request-tabs" role="tablist" aria-label="Filtrar solicitudes">
          <button
            type="button"
            className={filter === "pending" ? "request-tab request-tab--active" : "request-tab"}
            onClick={() => setFilter("pending")}
            role="tab"
            aria-selected={filter === "pending"}
          >
            Pendientes <span className="tab-count">{pendingCount}</span>
          </button>
          <button
            type="button"
            className={filter === "reviewed" ? "request-tab request-tab--active" : "request-tab"}
            onClick={() => setFilter("reviewed")}
            role="tab"
            aria-selected={filter === "reviewed"}
          >
            Revisadas <span className="tab-count">{reviewedCount}</span>
          </button>
        </div>

        {visibleRequests.length > 0 ? (
          <div className="request-table">
            <div className="request-table__header" aria-hidden="true">
              <span>Establecimiento</span>
              <span>NIT</span>
              <span>Representante</span>
              <span>Fecha de solicitud</span>
              <span>Estado</span>
              <span />
            </div>
            {visibleRequests.map((req) => (
              <div className="request-row" key={req.id}>
                <div className="request-primary" data-label="Establecimiento">
                  <span className="request-avatar">
                    {req.tradeName.slice(0, 1).toUpperCase()}
                  </span>
                  <span>{req.tradeName}</span>
                </div>
                <span data-label="NIT">{req.nit}</span>
                <span data-label="Representante">
                  {req.representativeName}
                </span>
                <span data-label="Fecha de solicitud">
                  {req.submittedAt}
                </span>
                <span data-label="Estado">
                  <StatusBadge status={req.status} />
                </span>
                <Button variant="secondary" onClick={() => onReview(req)}>
                  {req.status === "pending" ? "Revisar" : "Ver solicitud"}
                </Button>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-requests">
            <span className="empty-requests__icon">
              <Icon name="check" />
            </span>
            <p className="empty-requests__title">
              {filter === "pending"
                ? "No hay solicitudes pendientes"
                : "No hay solicitudes revisadas"}
            </p>
            <p className="empty-requests__description">
              {filter === "pending"
                ? "Las nuevas solicitudes aparecerán aquí para su revisión."
                : "Las solicitudes aprobadas o rechazadas aparecerán aquí."}
            </p>
          </div>
        )}
      </section>
    </AdminShell>
  );
}
