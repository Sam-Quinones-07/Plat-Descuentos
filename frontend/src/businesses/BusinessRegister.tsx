import { useMemo, useState, type FormEvent } from "react";
import { api } from "../api";
import { Alert, AuthLayout, Button, Icon, TextField } from "../components";
import { type BusinessFormData, emptyBusinessForm, passwordRules } from "../types";
import { toApiError } from "../api/mappers";

export function BusinessRegister({
  onSubmitted,
  goToLogin,
  goToStudent,
}: {
  onSubmitted: (values: BusinessFormData) => void;
  goToLogin: () => void;
  goToStudent: () => void;
}) {
  const [values, setValues] = useState(emptyBusinessForm);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState("");
  const [loading, setLoading] = useState(false);
  const ruleResults = useMemo(
    () =>
      passwordRules.map((rule) => ({
        ...rule,
        passed: rule.test(values.password),
      })),
    [values.password],
  );

  function update(field: keyof BusinessFormData, value: string) {
    setValues((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: "" }));
    setFormError("");
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    const nextErrors: Record<string, string> = {};
    const required: Array<keyof BusinessFormData> = [
      "tradeName",
      "legalName",
      "nit",
      "businessEmail",
      "phone",
      "address",
      "representativeName",
      "document",
      "email",
      "password",
      "confirmation",
    ];
    required.forEach((field) => {
      if (!values[field].trim()) nextErrors[field] = "Este campo es obligatorio.";
    });
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (values.businessEmail && !emailPattern.test(values.businessEmail))
      nextErrors.businessEmail = "Ingresa un correo electrónico válido.";
    if (values.email && !emailPattern.test(values.email))
      nextErrors.email = "Ingresa un correo electrónico válido.";
    if (values.nit && !/^[0-9-]{6,20}$/.test(values.nit))
      nextErrors.nit = "Ingresa un NIT válido.";
    if (values.phone && !/^\+?[0-9 ()-]{7,20}$/.test(values.phone))
      nextErrors.phone = "Ingresa un número de teléfono válido.";
    if (values.document && !/^[A-Za-z0-9-]{5,24}$/.test(values.document))
      nextErrors.document = "Ingresa un documento de identidad válido.";
    if (values.password && !ruleResults.every((rule) => rule.passed))
      nextErrors.password = "La contraseña aún no cumple todos los requisitos.";
    if (values.confirmation && values.password !== values.confirmation)
      nextErrors.confirmation = "Las contraseñas no coinciden.";

    setErrors(nextErrors);
    setFormError("");
    if (Object.keys(nextErrors).length) return;
    setLoading(true);
    try {
      const clean = {
        ...values,
        nit: values.nit.trim(),
        businessEmail: values.businessEmail.trim(),
        email: values.email.trim(),
      };
      await api.registerBusiness(clean);
      onSubmitted(clean);
    } catch (error) {
      // Los conflictos (NIT/correo ya registrados) llegan con mensaje genérico por diseño (HU 1.2.1 esc. 3 y 5).
      const apiError = toApiError(error);
      if (Object.keys(apiError.fields).length) setErrors(apiError.fields);
      else setFormError(apiError.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout
      eyebrow="COMERCIOS ALIADOS"
      title="Solicita la afiliación de tu establecimiento"
      description="Completa la información para que el administrador pueda revisar tu solicitud."
      audience="business"
      wide
    >
      <form className="form business-form" onSubmit={submit} noValidate>
        {formError && <Alert tone="error">{formError}</Alert>}
        <div className="form-section">
          <div className="form-section__heading">
            <span className="form-section__number">01</span>
            <div>
              <p className="form-section__title">Datos del establecimiento</p>
              <p className="form-section__description">
                Información legal y de contacto del comercio.
              </p>
            </div>
          </div>
          <div className="form-grid">
            <TextField
              id="business-trade-name"
              label="Nombre comercial"
              value={values.tradeName}
              onChange={(value) => update("tradeName", value)}
              error={errors.tradeName}
              placeholder="Nombre del establecimiento"
              autoComplete="organization"
            />
            <TextField
              id="business-legal-name"
              label="Razón social"
              value={values.legalName}
              onChange={(value) => update("legalName", value)}
              error={errors.legalName}
              placeholder="Razón social registrada"
              autoComplete="organization"
            />
            <TextField
              id="business-nit"
              label="NIT"
              value={values.nit}
              onChange={(value) => update("nit", value)}
              error={errors.nit}
              placeholder="Número de identificación tributaria"
              autoComplete="off"
            />
            <TextField
              id="business-email"
              label="Correo electrónico del establecimiento"
              type="email"
              value={values.businessEmail}
              onChange={(value) => update("businessEmail", value)}
              error={errors.businessEmail}
              placeholder="contacto@establecimiento.com"
              autoComplete="email"
            />
            <TextField
              id="business-phone"
              label="Número de teléfono"
              type="tel"
              value={values.phone}
              onChange={(value) => update("phone", value)}
              error={errors.phone}
              placeholder="Número de contacto"
              autoComplete="tel"
            />
            <TextField
              id="business-address"
              label="Dirección"
              value={values.address}
              onChange={(value) => update("address", value)}
              error={errors.address}
              placeholder="Dirección del establecimiento"
              autoComplete="street-address"
            />
          </div>
        </div>

        <div className="form-section">
          <div className="form-section__heading">
            <span className="form-section__number">02</span>
            <div>
              <p className="form-section__title">Datos del representante</p>
              <p className="form-section__description">
                Información de la persona responsable de la solicitud.
              </p>
            </div>
          </div>
          <div className="form-grid">
            <TextField
              id="representative-name"
              label="Nombre completo"
              value={values.representativeName}
              onChange={(value) => update("representativeName", value)}
              error={errors.representativeName}
              placeholder="Nombre y apellidos"
              autoComplete="name"
            />
            <TextField
              id="representative-document"
              label="Documento de identidad"
              value={values.document}
              onChange={(value) => update("document", value)}
              error={errors.document}
              placeholder="Número de documento"
              autoComplete="off"
            />
            <TextField
              id="representative-email"
              label="Correo electrónico"
              type="email"
              value={values.email}
              onChange={(value) => update("email", value)}
              error={errors.email}
              placeholder="correo@ejemplo.com"
              autoComplete="email"
            />
            <span className="form-grid__spacer" aria-hidden="true" />
            <TextField
              id="representative-password"
              label="Contraseña"
              type="password"
              value={values.password}
              onChange={(value) => update("password", value)}
              error={errors.password}
              placeholder="Crea una contraseña segura"
              autoComplete="new-password"
            />
            <TextField
              id="representative-confirmation"
              label="Confirmar contraseña"
              type="password"
              value={values.confirmation}
              onChange={(value) => update("confirmation", value)}
              error={errors.confirmation}
              placeholder="Repite tu contraseña"
              autoComplete="new-password"
            />
            <div className="requirements form-grid__full" aria-live="polite">
              <span className="requirements__title">
                Tu contraseña debe incluir:
              </span>
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
          </div>
        </div>

        <div className="business-form__footer">
          <Button type="submit" disabled={loading}>
            {loading ? (
              <>
                <Icon name="spinner" /> Registrando solicitud…
              </>
            ) : (
              <>
                Enviar solicitud <Icon name="arrow" />
              </>
            )}
          </Button>
          <p className="form-switch">
            ¿Ya registraste una solicitud?{" "}
            <button type="button" onClick={goToLogin}>
              Inicia sesión
            </button>
          </p>
          <Button variant="text" onClick={goToStudent}>
            Volver al acceso para estudiantes
          </Button>
        </div>
      </form>
    </AuthLayout>
  );
}
