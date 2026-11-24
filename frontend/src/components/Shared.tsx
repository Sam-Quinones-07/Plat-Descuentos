import { useEffect, useMemo, useState, type FormEvent, type ReactNode } from "react";
import { api, SERVER_WAKING_EVENT } from "../api";
import { toApiError } from "../api/mappers";
import { passwordRules } from "../types";
import { Alert } from "./Alert";
import { Button } from "./Button";
import { Icon } from "./Icon";
import { TextField } from "./TextField";

/** Sección 11.1: casilla obligatoria, sin marcar por defecto, con enlaces a los documentos. */
export function TermsCheckbox({
  checked,
  onChange,
  error,
}: {
  checked: boolean;
  onChange: (value: boolean) => void;
  error?: string;
}) {
  return (
    <div className="field">
      <label className="terms-check">
        <input
          type="checkbox"
          checked={checked}
          onChange={(event) => onChange(event.target.checked)}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? "accept-terms-error" : undefined}
        />
        <span>
          Autorizo el tratamiento de mis datos personales según la{" "}
          <a href="#privacidad" target="_blank" rel="noreferrer">
            Política de Tratamiento de Datos
          </a>{" "}
          y acepto los{" "}
          <a href="#terminos" target="_blank" rel="noreferrer">
            Términos de Servicio
          </a>
          .
        </span>
      </label>
      {error && (
        <span className="field-error" id="accept-terms-error">
          {error}
        </span>
      )}
    </div>
  );
}

export function PasswordRequirements({ value }: { value: string }) {
  return (
    <div className="requirements" aria-live="polite">
      <span className="requirements__title">Tu contraseña debe incluir:</span>
      <div className="requirements__grid">
        {passwordRules.map((rule) => {
          const passed = rule.test(value);
          return (
            <span className={`requirement ${passed ? "requirement--passed" : ""}`} key={rule.label}>
              <span className="requirement__check">{passed && <Icon name="check" />}</span>
              {rule.label}
            </span>
          );
        })}
      </div>
    </div>
  );
}

/** HU 1.3.1 Cambiar contraseña: exige la actual y valida la nueva con las mismas reglas del registro. */
export function ChangePasswordCard() {
  const [open, setOpen] = useState(false);
  const [current, setCurrent] = useState("");
  const [next, setNext] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [message, setMessage] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [loading, setLoading] = useState(false);
  const rulesOk = useMemo(() => passwordRules.every((rule) => rule.test(next)), [next]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const nextErrors: Record<string, string> = {};
    if (!current) nextErrors.currentPassword = "Ingresa tu contraseña actual.";
    if (!next) nextErrors.newPassword = "Ingresa la nueva contraseña.";
    else if (!rulesOk) nextErrors.newPassword = "La contraseña aún no cumple todos los requisitos.";
    if (next && confirmation !== next) nextErrors.confirmation = "Las contraseñas no coinciden.";
    setErrors(nextErrors);
    setMessage(null);
    if (Object.keys(nextErrors).length) return;
    setLoading(true);
    try {
      const response = await api.changePassword(current, next, confirmation);
      setMessage({ tone: "success", text: response.message });
      setCurrent("");
      setNext("");
      setConfirmation("");
      setOpen(false);
    } catch (error) {
      const apiError = toApiError(error);
      if (apiError.code === "CONTRASENA_ACTUAL_INCORRECTA") {
        setErrors({ currentPassword: apiError.message });
      } else if (Object.keys(apiError.fields).length) {
        setErrors(apiError.fields);
      } else {
        setMessage({ tone: "error", text: apiError.message });
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="security-card">
      <div className="security-card__heading">
        <div>
          <p className="form-section__title">Contraseña</p>
          <p className="form-section__description">
            Cámbiala periódicamente. Al cambiarla se cierran tus otras sesiones abiertas.
          </p>
        </div>
        {!open && (
          <Button variant="secondary" onClick={() => setOpen(true)}>
            <Icon name="key" /> Cambiar contraseña
          </Button>
        )}
      </div>
      {message && <Alert tone={message.tone}>{message.text}</Alert>}
      {open && (
        <form className="password-change-form" onSubmit={submit} noValidate>
          <TextField
            id="current-password"
            label="Contraseña actual"
            type="password"
            value={current}
            onChange={(value) => {
              setCurrent(value);
              setErrors((e) => ({ ...e, currentPassword: "" }));
            }}
            error={errors.currentPassword}
            placeholder="Tu contraseña actual"
            autoComplete="current-password"
          />
          <TextField
            id="new-password"
            label="Nueva contraseña"
            type="password"
            value={next}
            onChange={(value) => {
              setNext(value);
              setErrors((e) => ({ ...e, newPassword: "" }));
            }}
            error={errors.newPassword}
            placeholder="Crea una contraseña segura"
            autoComplete="new-password"
          />
          <PasswordRequirements value={next} />
          <TextField
            id="new-password-confirmation"
            label="Confirmar nueva contraseña"
            type="password"
            value={confirmation}
            onChange={(value) => {
              setConfirmation(value);
              setErrors((e) => ({ ...e, confirmation: "" }));
            }}
            error={errors.confirmation}
            placeholder="Repite la nueva contraseña"
            autoComplete="new-password"
          />
          <div className="review-form__actions">
            <Button variant="text" onClick={() => setOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? (
                <>
                  <Icon name="spinner" /> Guardando…
                </>
              ) : (
                "Guardar contraseña"
              )}
            </Button>
          </div>
        </form>
      )}
    </section>
  );
}

/** Ventana de confirmación reutilizable (HU 2.1.3 esc. 2 y 3). */
export function ConfirmDialog({
  title,
  description,
  confirmLabel,
  danger = false,
  busy = false,
  onConfirm,
  onCancel,
}: {
  title: string;
  description: ReactNode;
  confirmLabel: string;
  danger?: boolean;
  busy?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  return (
    <div className="modal-backdrop" role="presentation" onClick={onCancel}>
      <div
        className="decision-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirm-dialog-title"
        onClick={(event) => event.stopPropagation()}
      >
        <span className={`decision-modal__icon ${danger ? "decision-modal__icon--danger" : ""}`}>
          <Icon name="warning" />
        </span>
        <p className="decision-modal__title" id="confirm-dialog-title">
          {title}
        </p>
        <p className="decision-modal__description">{description}</p>
        <div className="decision-modal__actions">
          <Button variant="secondary" onClick={onCancel}>
            Cancelar
          </Button>
          <Button variant={danger ? "danger" : "primary"} onClick={onConfirm} disabled={busy}>
            {busy ? <Icon name="spinner" /> : null} {confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  );
}

/** Aviso mientras el backend gratuito de Render despierta (riesgo "arranque lento", sección 7.5). */
export function ServerWakingBanner() {
  const [waking, setWaking] = useState(false);
  useEffect(() => {
    const handler = (event: Event) => setWaking(Boolean((event as CustomEvent<boolean>).detail));
    window.addEventListener(SERVER_WAKING_EVENT, handler);
    return () => window.removeEventListener(SERVER_WAKING_EVENT, handler);
  }, []);
  if (!waking) return null;
  return (
    <div className="waking-banner" role="status">
      <Icon name="spinner" />
      <span>
        Estamos activando el servidor. La primera carga después de un rato sin uso puede tardar hasta un minuto.
      </span>
    </div>
  );
}

export function LegalFooter() {
  return (
    <footer className="legal-footer">
      <span>© 2026 Equipo PromoU · Universidad de Antioquia</span>
      <a href="#terminos">Términos de Servicio</a>
      <a href="#privacidad">Política de Tratamiento de Datos</a>
    </footer>
  );
}
