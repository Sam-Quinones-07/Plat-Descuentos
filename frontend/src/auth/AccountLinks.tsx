import { useEffect, useMemo, useState, type FormEvent } from "react";
import { api, type ApiRole } from "../api";
import { toApiError } from "../api/mappers";
import { Alert, AuthLayout, Button, Icon, PasswordRequirements, TextField } from "../components";
import { passwordRules } from "../types";

type GoTo = { goToLogin: (role?: ApiRole) => void };

/** HU 1.1.2 / 1.2.2: el enlace del correo abre #confirmar/{token} y aquí se envía al servidor. */
export function ConfirmEmail({ token, goToLogin, goToResend }: GoTo & { token: string; goToResend: () => void }) {
  const [state, setState] = useState<
    { kind: "loading" } | { kind: "ok"; message: string; role: ApiRole } | { kind: "error"; message: string; code: string }
  >({ kind: "loading" });

  useEffect(() => {
    let cancelled = false;
    api
      .confirmEmail(token)
      .then((r) => !cancelled && setState({ kind: "ok", message: r.message, role: r.role }))
      .catch((e) => {
        if (cancelled) return;
        const err = toApiError(e);
        setState({ kind: "error", message: err.message, code: err.code });
      });
    return () => {
      cancelled = true;
    };
  }, [token]);

  const already = state.kind === "error" && state.code === "CUENTA_YA_CONFIRMADA";
  return (
    <AuthLayout eyebrow="CONFIRMACIÓN DE CORREO" title="Confirmación de registro" description="" compact>
      {state.kind === "loading" && (
        <div className="confirmation-visual">
          <span className="confirmation-visual__ring">
            <Icon name="spinner" />
          </span>
        </div>
      )}
      {state.kind !== "loading" && (
        <span className={`result-icon ${state.kind === "ok" || already ? "result-icon--success" : "result-icon--error"}`}>
          <Icon name={state.kind === "ok" || already ? "check" : "warning"} />
        </span>
      )}
      {state.kind === "ok" && <Alert tone="success">{state.message}</Alert>}
      {state.kind === "error" && <Alert tone={already ? "success" : "warning"}>{state.message}</Alert>}
      {state.kind !== "loading" && (
        <div className="result-actions">
          <Button onClick={() => goToLogin(state.kind === "ok" ? state.role : undefined)}>
            Ir a iniciar sesión <Icon name="arrow" />
          </Button>
          {state.kind === "error" && !already && (
            <Button variant="secondary" onClick={goToResend}>
              Solicitar un nuevo enlace
            </Button>
          )}
        </div>
      )}
    </AuthLayout>
  );
}

/** Formulario de un solo correo, usado para reenviar la confirmación (HU 1.2.2 esc. 5) y para HU 1.3.2 esc. 1. */
function EmailRequest({
  eyebrow,
  title,
  description,
  submitLabel,
  send,
  goToLogin,
}: GoTo & {
  eyebrow: string;
  title: string;
  description: string;
  submitLabel: string;
  send: (email: string) => Promise<{ message: string }>;
}) {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [result, setResult] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setResult(null);
    if (!email.trim()) return setError("El correo electrónico es obligatorio.");
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) return setError("Ingresa un correo electrónico válido.");
    setLoading(true);
    try {
      setResult({ tone: "success", text: (await send(email.trim())).message });
    } catch (e) {
      const err = toApiError(e);
      if (err.fields.email) setError(err.fields.email);
      else setResult({ tone: "error", text: err.message });
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout eyebrow={eyebrow} title={title} description={description} compact>
      <form className="form" onSubmit={submit} noValidate>
        {result && <Alert tone={result.tone}>{result.text}</Alert>}
        <TextField
          id="request-email"
          label="Correo electrónico"
          type="email"
          value={email}
          onChange={(value) => {
            setEmail(value);
            setError("");
          }}
          error={error}
          placeholder="nombre@correo.com"
          autoComplete="email"
        />
        <Button type="submit" disabled={loading}>
          {loading ? (
            <>
              <Icon name="spinner" /> Enviando…
            </>
          ) : (
            submitLabel
          )}
        </Button>
        <p className="form-switch">
          <button type="button" onClick={() => goToLogin()}>
            Volver a iniciar sesión
          </button>
        </p>
      </form>
    </AuthLayout>
  );
}

export function ResendConfirmation({ goToLogin }: GoTo) {
  return (
    <EmailRequest
      eyebrow="CONFIRMACIÓN DE CORREO"
      title="Reenviar enlace de confirmación"
      description="Te enviaremos un nuevo enlace. El anterior dejará de funcionar."
      submitLabel="Enviar nuevo enlace"
      send={api.resendConfirmation}
      goToLogin={goToLogin}
    />
  );
}

export function ForgotPassword({ goToLogin }: GoTo) {
  return (
    <EmailRequest
      eyebrow="RECUPERAR ACCESO"
      title="¿Olvidaste tu contraseña?"
      description="Ingresa el correo de tu cuenta y te enviaremos un enlace para restablecerla."
      submitLabel="Enviar enlace"
      send={api.requestPasswordReset}
      goToLogin={goToLogin}
    />
  );
}

/** HU 1.3.2 esc. 2 a 7: verifica el enlace, pide la nueva contraseña y la guarda. */
export function ResetPassword({ token, goToLogin, goToForgot }: GoTo & { token: string; goToForgot: () => void }) {
  const [linkState, setLinkState] = useState<"checking" | "valid" | "invalid">("checking");
  const [linkMessage, setLinkMessage] = useState("");
  const [password, setPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [done, setDone] = useState("");
  const [formError, setFormError] = useState("");
  const [loading, setLoading] = useState(false);
  const rulesOk = useMemo(() => passwordRules.every((rule) => rule.test(password)), [password]);

  useEffect(() => {
    let cancelled = false;
    api
      .checkResetLink(token)
      .then(() => !cancelled && setLinkState("valid"))
      .catch((e) => {
        if (cancelled) return;
        setLinkMessage(toApiError(e).message);
        setLinkState("invalid");
      });
    return () => {
      cancelled = true;
    };
  }, [token]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const next: Record<string, string> = {};
    if (!password) next.password = "Ingresa la nueva contraseña.";
    else if (!rulesOk) next.password = "La contraseña aún no cumple todos los requisitos.";
    if (password && confirmation !== password) next.confirmation = "Las contraseñas no coinciden.";
    setErrors(next);
    setFormError("");
    if (Object.keys(next).length) return;
    setLoading(true);
    try {
      setDone((await api.resetPassword(token, password, confirmation)).message);
    } catch (e) {
      const err = toApiError(e);
      if (err.code === "ENLACE_INVALIDO") {
        setLinkMessage(err.message);
        setLinkState("invalid");
      } else if (Object.keys(err.fields).length) setErrors(err.fields);
      else setFormError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout
      eyebrow="RECUPERAR ACCESO"
      title="Restablece tu contraseña"
      description="Crea una nueva contraseña para tu cuenta."
      compact
    >
      {linkState === "checking" && (
        <div className="confirmation-visual">
          <span className="confirmation-visual__ring">
            <Icon name="spinner" />
          </span>
        </div>
      )}
      {linkState === "invalid" && (
        <>
          <Alert tone="warning">{linkMessage}</Alert>
          <div className="result-actions">
            <Button onClick={goToForgot}>Solicitar un nuevo enlace</Button>
            <Button variant="secondary" onClick={() => goToLogin()}>
              Ir a iniciar sesión
            </Button>
          </div>
        </>
      )}
      {linkState === "valid" && done && (
        <>
          <Alert tone="success">{done}</Alert>
          <div className="result-actions">
            <Button onClick={() => goToLogin()}>
              Iniciar sesión <Icon name="arrow" />
            </Button>
          </div>
        </>
      )}
      {linkState === "valid" && !done && (
        <form className="form" onSubmit={submit} noValidate>
          {formError && <Alert tone="error">{formError}</Alert>}
          <TextField
            id="reset-password"
            label="Nueva contraseña"
            type="password"
            value={password}
            onChange={(value) => {
              setPassword(value);
              setErrors((c) => ({ ...c, password: "" }));
            }}
            error={errors.password}
            placeholder="Crea una contraseña segura"
            autoComplete="new-password"
          />
          <PasswordRequirements value={password} />
          <TextField
            id="reset-confirmation"
            label="Confirmar contraseña"
            type="password"
            value={confirmation}
            onChange={(value) => {
              setConfirmation(value);
              setErrors((c) => ({ ...c, confirmation: "" }));
            }}
            error={errors.confirmation}
            placeholder="Repite la contraseña"
            autoComplete="new-password"
          />
          <Button type="submit" disabled={loading}>
            {loading ? (
              <>
                <Icon name="spinner" /> Guardando…
              </>
            ) : (
              "Restablecer contraseña"
            )}
          </Button>
        </form>
      )}
    </AuthLayout>
  );
}
