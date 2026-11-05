import { useEffect, useState } from "react";
import { api, formatInstant, type ApiCredential } from "../api";
import { type Account } from "../types";
import { Alert, Button, Icon } from "../components";
import { DigitalQr } from "./DigitalQr";
import { StudentShell, type StudentShellProps } from "./StudentShell";
import { toApiError } from "../api/mappers";

/** HU 1.4.1 Generar identificación digital y HU 1.1.4 Cerrar sesión. (Cambiar contraseña es de Release 3.) */
export function StudentProfile({
  account,
  shell,
}: {
  account: Account;
  shell: StudentShellProps;
}) {
  const [credential, setCredential] = useState<ApiCredential | null>(null);
  const [credentialError, setCredentialError] = useState("");

  useEffect(() => {
    let cancelled = false;
    api
      .credential()
      .then((value) => {
        if (!cancelled) setCredential(value);
      })
      .catch((error) => {
        // Esc. 3: un estudiante no verificado recibe el mensaje del servidor (403)
        if (!cancelled) setCredentialError(toApiError(error).message);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const digitalId = credential?.digitalId ?? "";
  const valid = credential?.valid ?? false;

  return (
    <StudentShell {...shell} active="profile">
      <div className="admin-page-heading">
        <span className="eyebrow">IDENTIFICACIÓN ESTUDIANTIL</span>
        <p className="admin-page-title">Mi identificación digital</p>
        <p className="page-description">
          Muestra este código QR en los comercios aliados para demostrar tu condición de estudiante.
        </p>
      </div>

      <div className="profile-layout">
        {credentialError && <Alert tone="warning">{credentialError}</Alert>}
        {!credentialError && (
          <section className="digital-id-card" aria-busy={!credential}>
            <div className="digital-id-card__header">
              <div>
                <span className="digital-id-card__kicker">IDENTIFICACIÓN DIGITAL</span>
                <span className="digital-id-card__institution">ESTUDIANTE UNIVERSITARIO</span>
              </div>
              {credential && (
                <span className={`verified-badge ${valid ? "" : "verified-badge--expired"}`}>
                  <Icon name={valid ? "check" : "warning"} /> {valid ? "Verificado" : "No vigente"}
                </span>
              )}
            </div>
            <div className="digital-id-card__body">
              <div className="digital-id-card__qr">
                {digitalId ? <DigitalQr value={digitalId} /> : <span className="qr-placeholder" />}
              </div>
              <div className="digital-id-card__identity">
                <span className="digital-id-card__label">ESTUDIANTE</span>
                <p className="digital-id-card__name">
                  {account.firstName} {account.lastName}
                </p>
                <p className="digital-id-card__university">{account.university}</p>
                <div className="digital-id-card__facts">
                  <div className="digital-id-card__number">
                    <span>IDENTIFICADOR ÚNICO</span>
                    <strong>{digitalId || "…"}</strong>
                  </div>
                  {credential && (
                    <div className="digital-id-card__number">
                      <span>VÁLIDA HASTA</span>
                      <strong>{formatInstant(credential.expiresAt)}</strong>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </section>
        )}

        <section className="security-card">
          <div className="security-card__heading">
            <div>
              <p className="form-section__title">Sesión</p>
              <p className="form-section__description">
                Conectado como {account.email}. Cierra tu sesión cuando termines, sobre todo en equipos compartidos.
              </p>
            </div>
          </div>
          <div className="profile-logout-row">
            <Button variant="secondary" onClick={shell.onLogout}>
              Cerrar sesión
            </Button>
          </div>
        </section>
      </div>
    </StudentShell>
  );
}
