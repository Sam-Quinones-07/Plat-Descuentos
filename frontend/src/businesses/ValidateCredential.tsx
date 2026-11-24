import { useState, type FormEvent } from "react";
import { api, formatInstant, type ApiQrValidation } from "../api";
import { toApiError } from "../api/mappers";
import { Alert, Button, ChangePasswordCard, Icon, TextField } from "../components";
import { BusinessShell } from "./BusinessShell";

type ShellProps = { businessName: string; onLogout: () => void; onNew: () => void };

/**
 * HU 1.4.2 Validar identificación digital. El comercio escanea el QR con la cámara del celular (el QR contiene el
 * identificador, p. ej. U-AB12CD34EF) o lo digita. El servidor responde lo mínimo: nombre corto, universidad y
 * vigencia (Ley 1581).
 */
export function ValidateCredential({ businessName, onLogout, onNew }: ShellProps) {
  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [result, setResult] = useState<ApiQrValidation | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setResult(null);
    if (!code.trim()) return setError("Ingresa o escanea el código.");
    setLoading(true);
    try {
      setResult(await api.validateCredential(code.trim()));
    } catch (e) {
      const apiError = toApiError(e);
      if (apiError.status !== 401) setError(apiError.fields.code ?? apiError.message);
    } finally {
      setLoading(false);
    }
  }

  const valid = result?.result === "VALIDA";
  return (
    <BusinessShell businessName={businessName} onLogout={onLogout} onNew={onNew} active="validate">
      <div className="admin-page-heading">
        <span className="eyebrow">VALIDACIÓN PRESENCIAL</span>
        <p className="admin-page-title">Validar identificación digital</p>
        <p className="page-description">
          Escanea el código QR del estudiante con la cámara de tu celular o escribe el identificador que aparece debajo
          del código.
        </p>
      </div>
      <form className="discount-form detail-card" onSubmit={submit} noValidate>
        <TextField
          id="credential-code"
          label="Identificador de la credencial"
          value={code}
          onChange={(value) => {
            setCode(value.toUpperCase());
            setError("");
            setResult(null);
          }}
          error={error}
          placeholder="U-XXXXXXXXXX"
          autoComplete="off"
        />
        <div className="discount-form__actions">
          <Button type="submit" disabled={loading}>
            {loading ? (
              <>
                <Icon name="spinner" /> Validando…
              </>
            ) : (
              <>
                <Icon name="shield" /> Validar
              </>
            )}
          </Button>
        </div>
        {result && (
          <div className="validation-result" aria-live="polite">
            <Alert tone={valid ? "success" : "warning"}>{result.message}</Alert>
            {valid && (
              <div className="detail-grid">
                <div className="detail-item">
                  <span className="detail-item__label">Estudiante</span>
                  <span className="detail-item__value">{result.studentName}</span>
                </div>
                <div className="detail-item">
                  <span className="detail-item__label">Universidad</span>
                  <span className="detail-item__value">{result.university}</span>
                </div>
                {result.expiresAt && (
                  <div className="detail-item">
                    <span className="detail-item__label">Vigente hasta</span>
                    <span className="detail-item__value">{formatInstant(result.expiresAt)}</span>
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </form>
    </BusinessShell>
  );
}

/** HU 1.3.1 también para el representante del establecimiento. */
export function BusinessSecurity({ businessName, onLogout, onNew }: ShellProps) {
  return (
    <BusinessShell businessName={businessName} onLogout={onLogout} onNew={onNew} active="account">
      <div className="admin-page-heading">
        <span className="eyebrow">MI CUENTA</span>
        <p className="admin-page-title">Seguridad</p>
        <p className="page-description">Administra la contraseña de la cuenta del establecimiento.</p>
      </div>
      <div className="profile-layout">
        <ChangePasswordCard />
      </div>
    </BusinessShell>
  );
}
