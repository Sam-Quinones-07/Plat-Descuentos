import { useState, type FormEvent } from "react";
import { api, type LoginResponse } from "../api";
import { Alert, AuthLayout, Button, Icon, TextField } from "../components";
import { toApiError } from "../api/mappers";

export function Login({
  notice = "",
  noticeTone = "warning",
  onSuccess,
  goToRegister,
  goToBusiness,
}: {
  notice?: string;
  noticeTone?: "success" | "warning";
  onSuccess: (response: LoginResponse) => void;
  goToRegister: () => void;
  goToBusiness: () => void;
}) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [message, setMessage] = useState(notice);
  const [tone, setTone] = useState<"error" | "warning" | "success">(
    notice ? noticeTone : "error",
  );
  const [loading, setLoading] = useState(false);
  const [unconfirmed, setUnconfirmed] = useState(false);

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
      onSuccess(await api.login(email.trim(), password));
    } catch (error) {
      const apiError = toApiError(error);
      setUnconfirmed(apiError.code === "CORREO_NO_CONFIRMADO");
      setTone(apiError.code === "CUENTA_BLOQUEADA" || apiError.code === "CORREO_NO_CONFIRMADO" ? "warning" : "error");
      setMessage(apiError.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout
      eyebrow="BIENVENIDO DE NUEVO"
      title="Inicia sesión"
      description="Accede con tu correo electrónico y contraseña."
      compact
    >
      <form className="form" onSubmit={submit} noValidate>
        {message && <Alert tone={tone}>{message}</Alert>}
        <TextField
          id="login-email"
          label="Correo electrónico"
          type="email"
          value={email}
          onChange={(value) => {
            setEmail(value);
            setErrors((current) => ({ ...current, email: "" }));
            setMessage("");
          }}
          error={errors.email}
          placeholder="nombre@universidad.edu"
          autoComplete="email"
        />
        <TextField
          id="login-password"
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
        <div className="login-links">
          <a href="#recuperar-contrasena">¿Olvidaste tu contraseña?</a>
          {unconfirmed && <a href="#reenviar-confirmacion">Reenviar enlace de confirmación</a>}
        </div>
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
          ¿Aún no tienes una cuenta?{" "}
          <button type="button" onClick={goToRegister}>
            Regístrate
          </button>
        </p>
        <div className="portal-switch">
          <span>¿Eres un establecimiento?</span>
          <button type="button" onClick={goToBusiness}>
            Acceso para establecimientos
          </button>
        </div>
      </form>
    </AuthLayout>
  );
}
