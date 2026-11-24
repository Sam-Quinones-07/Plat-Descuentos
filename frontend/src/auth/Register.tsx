import { useMemo, useState, type FormEvent } from "react";
import { api } from "../api";
import { Alert, AuthLayout, Button, Icon, TermsCheckbox, TextField } from "../components";
import { passwordRules } from "../types";
import { toApiError } from "../api/mappers";

export function Register({
  onRegistered,
  goToLogin,
  goToBusiness,
}: {
  onRegistered: (data: { email: string; message: string }) => void;
  goToLogin: () => void;
  goToBusiness: () => void;
}) {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [university, setUniversity] = useState("");
  const [password, setPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState("");
  const [loading, setLoading] = useState(false);
  const [acceptTerms, setAcceptTerms] = useState(false);

  const ruleResults = useMemo(
    () => passwordRules.map((rule) => ({ ...rule, passed: rule.test(password) })),
    [password],
  );

  async function submit(event: FormEvent) {
    event.preventDefault();
    const nextErrors: Record<string, string> = {};
    if (!firstName.trim()) nextErrors.firstName = "El nombre es obligatorio.";
    if (!lastName.trim()) nextErrors.lastName = "El apellido es obligatorio.";
    if (!email.trim()) nextErrors.email = "El correo electrónico es obligatorio.";
    if (
      email.trim() &&
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())
    )
      nextErrors.email = "Ingresa un correo institucional válido.";
    if (!university.trim())
      nextErrors.university = "La universidad es obligatoria.";
    if (!password) nextErrors.password = "La contraseña es obligatoria.";
    if (!confirmation)
      nextErrors.confirmation = "Confirma tu contraseña para continuar.";
    if (password && !ruleResults.every((rule) => rule.passed))
      nextErrors.password = "La contraseña aún no cumple todos los requisitos.";
    if (confirmation && password !== confirmation)
      nextErrors.confirmation = "Las contraseñas no coinciden.";
    if (!acceptTerms)
      nextErrors.acceptTerms = "Debes aceptar los términos y la política de tratamiento de datos.";

    setErrors(nextErrors);
    setFormError("");
    if (Object.keys(nextErrors).length) return;
    setLoading(true);
    try {
      const response = await api.registerStudent({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim(),
        university: university.trim(),
        password,
        confirmation,
        acceptTerms,
      });
      onRegistered({ email: email.trim(), message: response.message });
    } catch (error) {
      const apiError = toApiError(error);
      if (Object.keys(apiError.fields).length) setErrors(apiError.fields);
      else setFormError(apiError.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout
      eyebrow="CREA TU CUENTA"
      title="Regístrate como estudiante"
      description="Usa el correo electrónico proporcionado por tu universidad."
      wide
    >
      <form className="form" onSubmit={submit} noValidate>
        {formError && <Alert tone="error">{formError}</Alert>}
        <div className="form-section__heading">
          <span className="form-section__number">01</span>
          <div>
            <p className="form-section__title">Información personal</p>
            <p className="form-section__description">
              Datos asociados a tu identificación estudiantil.
            </p>
          </div>
        </div>
        <div className="form-grid">
          <TextField
            id="register-first-name"
            label="Nombre"
            value={firstName}
            onChange={(value) => {
              setFirstName(value);
              setErrors((current) => ({ ...current, firstName: "" }));
            }}
            error={errors.firstName}
            placeholder="Tu nombre"
            autoComplete="given-name"
          />
          <TextField
            id="register-last-name"
            label="Apellido"
            value={lastName}
            onChange={(value) => {
              setLastName(value);
              setErrors((current) => ({ ...current, lastName: "" }));
            }}
            error={errors.lastName}
            placeholder="Tu apellido"
            autoComplete="family-name"
          />
          <TextField
            id="register-email"
            label="Correo institucional"
            type="email"
            value={email}
            onChange={(value) => {
              setEmail(value);
              setErrors((current) => ({ ...current, email: "" }));
              setFormError("");
            }}
            error={errors.email}
            placeholder="nombre@universidad.edu"
            autoComplete="email"
          />
          <TextField
            id="register-university"
            label="Universidad"
            value={university}
            onChange={(value) => {
              setUniversity(value);
              setErrors((current) => ({ ...current, university: "" }));
            }}
            error={errors.university}
            placeholder="Nombre de tu universidad"
            autoComplete="organization"
          />
        </div>
        <div className="form-section__heading student-security-heading">
          <span className="form-section__number">02</span>
          <div>
            <p className="form-section__title">Seguridad</p>
            <p className="form-section__description">
              Crea una contraseña segura para tu cuenta.
            </p>
          </div>
        </div>
        <TextField
          id="register-password"
          label="Contraseña"
          type="password"
          value={password}
          onChange={(value) => {
            setPassword(value);
            setErrors((current) => ({ ...current, password: "" }));
          }}
          error={errors.password}
          placeholder="Crea una contraseña segura"
          autoComplete="new-password"
        />
        <div className="requirements" aria-live="polite">
          <span className="requirements__title">Tu contraseña debe incluir:</span>
          <div className="requirements__grid">
            {ruleResults.map((rule) => (
              <span
                className={`requirement ${rule.passed ? "requirement--passed" : ""}`}
                key={rule.label}
              >
                <span className="requirement__check">
                  {rule.passed && <Icon name="check" />}
                </span>
                {rule.label}
              </span>
            ))}
          </div>
        </div>
        <TextField
          id="register-confirmation"
          label="Confirmación de contraseña"
          type="password"
          value={confirmation}
          onChange={(value) => {
            setConfirmation(value);
            setErrors((current) => ({ ...current, confirmation: "" }));
          }}
          error={errors.confirmation}
          placeholder="Repite tu contraseña"
          autoComplete="new-password"
        />
        <TermsCheckbox
          checked={acceptTerms}
          onChange={(value) => {
            setAcceptTerms(value);
            setErrors((current) => ({ ...current, acceptTerms: "" }));
          }}
          error={errors.acceptTerms}
        />
        <Button type="submit" disabled={loading}>
          {loading ? (
            <>
              <Icon name="spinner" /> Creando cuenta…
            </>
          ) : (
            <>
              Registrarse <Icon name="arrow" />
            </>
          )}
        </Button>
        <p className="form-switch">
          ¿Ya tienes una cuenta?{" "}
          <button type="button" onClick={goToLogin}>
            Inicia sesión
          </button>
        </p>
        <div className="portal-switch">
          <span>¿Representas a un establecimiento?</span>
          <button type="button" onClick={goToBusiness}>
            Solicita la afiliación
          </button>
        </div>
      </form>
    </AuthLayout>
  );
}
