import { useState } from "react";
import { AdminShell } from "./AdminShell";
import { Alert, Button, DetailItem, Icon } from "../components";
import { type BusinessAccount } from "../types";
import { StatusBadge } from "./StatusBadge";
import { toApiError } from "../api/mappers";

export function AdminRequest({
  request,
  onBack,
  onDecision,
  onLogout,
}: {
  request: BusinessAccount;
  onBack: () => void;
  onDecision: (decision: "approved" | "rejected") => Promise<void>;
  onLogout: () => void;
}) {
  const [confirmation, setConfirmation] = useState<
    "approved" | "rejected" | null
  >(null);
  const [notification, setNotification] = useState<
    "approved" | "rejected" | null
  >(null);

  const [decisionError, setDecisionError] = useState("");

  async function confirmDecision() {
    if (!confirmation) return;
    const decision = confirmation;
    setDecisionError("");
    try {
      await onDecision(decision);
      setNotification(decision);
    } catch (error) {
      // p. ej. otra persona ya revisó la solicitud (409): se informa su estado actual
      setDecisionError(toApiError(error).message);
    } finally {
      setConfirmation(null);
    }
  }

  return (
    <AdminShell onLogout={onLogout}>
      <button type="button" className="back-button" onClick={onBack}>
        <span aria-hidden="true">←</span> Volver a solicitudes
      </button>
      <div className="admin-detail-heading">
        <div>
          <span className="eyebrow">SOLICITUD DE AFILIACIÓN</span>
          <p className="admin-page-title">{request.tradeName}</p>
          <p className="page-description">
            Solicitud registrada el {request.submittedAt}
          </p>
        </div>
        <div className="detail-status">
          <span className="detail-item__label">ESTADO ACTUAL</span>
          <StatusBadge status={request.status} />
        </div>
      </div>

      {decisionError && <Alert tone="error">{decisionError}</Alert>}
      {notification && (
        <Alert tone={notification === "approved" ? "success" : "warning"}>
          <span>
            <strong>
              {notification === "approved"
                ? "Solicitud aprobada."
                : "Solicitud rechazada."}
            </strong>{" "}
            Se notificó el resultado al representante ({request.email}).
          </span>
        </Alert>
      )}

      <div className="detail-layout">
        <div className="detail-content">
          <section className="detail-card">
            <div className="detail-card__heading">
              <span className="form-section__number">01</span>
              <div>
                <p className="form-section__title">Datos del establecimiento</p>
                <p className="form-section__description">
                  Información comercial y legal registrada.
                </p>
              </div>
            </div>
            <div className="detail-grid">
              <DetailItem label="Nombre comercial" value={request.tradeName} />
              <DetailItem label="Razón social" value={request.legalName} />
              <DetailItem label="NIT" value={request.nit} />
              <DetailItem
                label="Correo electrónico"
                value={request.businessEmail}
              />
              <DetailItem label="Número de teléfono" value={request.phone} />
              <DetailItem label="Dirección" value={request.address} />
            </div>
          </section>

          <section className="detail-card">
            <div className="detail-card__heading">
              <span className="form-section__number">02</span>
              <div>
                <p className="form-section__title">Datos del representante</p>
                <p className="form-section__description">
                  Persona responsable de la solicitud.
                </p>
              </div>
            </div>
            <div className="detail-grid">
              <DetailItem
                label="Nombre completo"
                value={request.representativeName}
              />
              <DetailItem
                label="Documento de identidad"
                value={request.document}
              />
              <DetailItem label="Correo electrónico" value={request.email} />
            </div>
          </section>
        </div>

        <aside className="review-card">
          <span className="review-card__icon">
            <Icon name="shield" />
          </span>
          <p className="review-card__title">
            {request.status === "pending"
              ? "Pendiente de revisión"
              : request.status === "approved"
                ? "Solicitud aprobada"
                : "Solicitud rechazada"}
          </p>
          <p className="review-card__description">
            {request.status === "pending"
              ? "Revisa la información proporcionada antes de tomar una decisión."
              : "Esta solicitud ya fue revisada y no admite una nueva decisión."}
          </p>
          {request.status === "pending" && (
            <div className="review-card__actions">
              <Button onClick={() => setConfirmation("approved")}>
                <Icon name="check" /> Aprobar solicitud
              </Button>
              <Button
                variant="danger"
                onClick={() => setConfirmation("rejected")}
              >
                Rechazar solicitud
              </Button>
            </div>
          )}
        </aside>
      </div>

      {confirmation && (
        <div className="modal-backdrop" role="presentation">
          <div
            className="decision-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="decision-title"
          >
            <span
              className={`decision-modal__icon ${
                confirmation === "rejected" ? "decision-modal__icon--danger" : ""
              }`}
            >
              <Icon name={confirmation === "approved" ? "check" : "warning"} />
            </span>
            <p className="decision-modal__title" id="decision-title">
              {confirmation === "approved"
                ? "¿Aprobar esta solicitud?"
                : "¿Rechazar esta solicitud?"}
            </p>
            <p className="decision-modal__description">
              Esta decisión actualizará el estado de la solicitud y se
              notificará al representante. No se podrá revisar de nuevo.
            </p>
            <div className="decision-modal__actions">
              <Button variant="secondary" onClick={() => setConfirmation(null)}>
                Cancelar
              </Button>
              <Button
                variant={confirmation === "approved" ? "primary" : "danger"}
                onClick={confirmDecision}
              >
                {confirmation === "approved"
                  ? "Sí, aprobar solicitud"
                  : "Sí, rechazar solicitud"}
              </Button>
            </div>
          </div>
        </div>
      )}
    </AdminShell>
  );
}
