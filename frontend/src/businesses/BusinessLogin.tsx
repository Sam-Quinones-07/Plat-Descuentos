import { useEffect, useState, type FormEvent } from "react";
import { api, type LoginResponse } from "../api";
import { Alert, AuthLayout, Button, Icon, TextField } from "../components";
import { toApiError } from "../api/mappers";

export function BusinessLogin({
  sessionExpired,
  goToRegister,
  goToStudent,
  onApproved,
}: {
  sessionExpired: boolean;
  goToRegister: () => void;
  goToStudent: () => void;
  onApproved: (response: LoginResponse) => void;
}) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [message, setMessage] = useState(
    sessionExpired
      ? "Tu sesión expiró por inactividad. Inicia sesión nuevamente."
      : "",
  );
  const [tone, setTone] = useState<"error" | "warning" | "success">(
    sessionExpired ? "warning" : "error",
  );
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (sessionExpired) {
      setTone("warning");
      setMessage("Tu sesión expiró por inactividad. Inicia sesión nuevamente.");
    }
  }, [sessionExpired]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const nextErrors: Record<string, string> = {};
    if (!email.trim()) nextErrors.email = "El correo electrónico es obligatorio.";
    if (!password) nextErrors.password = "La contraseña es obligatoria.";
    setErrors(nextErrors);
    setMessage("");
    if (Object.keys(nextErrors).length) return;

    setLoading(true);
    try {
      onApproved(await api.businessLogin(email.trim(), password));
    } catch (error) {
      // Credenciales incorrectas -> error; correo sin confirmar, afiliación pendiente/rechazada o bloqueo -> aviso.
      const apiError = toApiError(error);
      setTone(apiError.status === 403 || apiError.status === 423 ? "warning" : "error");
      setMessage(apiError.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout
      eyebrow="ACCESO PARA ESTABLECIMIENTOS"
      title="Inicia sesión"
      description="Accede con el correo electrónico del establecimiento y la contraseña registrados en tu solicitud."
      audience="business"
      compact
    >
      <form className="form" onSubmit={submit} noValidate>
        {message && <Alert tone={tone}>{message}</Alert>}
        <TextField
          id="business-login-email"
          label="Correo electrónico del establecimiento"
          type="email"
          value={email}
          onChange={(value) => {
            setEmail(value);
            setErrors((current) => ({ ...current, email: "" }));
            setMessage("");
          }}
          error={errors.email}
          placeholder="correo@ejemplo.com"
          autoComplete="email"
        />
        <TextField
          id="business-login-password"
          label="Contraseña"
          type={showPassword ? "text" : "password"}
          value={password}
          onChange={(value) => {
            setPassword(value);
            setErrors((current) => ({ ...current, password: "" }));
            setMessage("");
          }}
          error={errors.password}
          placeholder="Ingresa tu contraseña"
          autoComplete="current-password"
          action={
            <button
              type="button"
              className="input-action"
              onClick={() => setShowPassword((current) => !current)}
              aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
            >
              <Icon name={showPassword ? "eyeOff" : "eye"} />
            </button>
          }
        />
        <Button type="submit" disabled={loading}>
          {loading ? (
            <>
              <Icon name="spinner" /> Verificando…
            </>
          ) : (
            <>
              Iniciar sesión <Icon name="arrow" />
            </>
          )}
        </Button>
        <p className="form-switch">
          ¿Aún no registraste tu establecimiento?{" "}
          <button type="button" onClick={goToRegister}>
            Solicita la afiliación
          </button>
        </p>
        <div className="portal-switch">
          <span>¿Eres estudiante?</span>
          <button type="button" onClick={goToStudent}>
            Volver al acceso para estudiantes
          </button>
        </div>
      </form>
    </AuthLayout>
  );
}
