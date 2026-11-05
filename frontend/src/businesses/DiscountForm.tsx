import { useState, type FormEvent } from "react";
import { Alert, Button, CategoryIcon, Icon, TextAreaField, TextField } from "../components";
import { BusinessShell } from "./BusinessShell";
import { type Discount, type DiscountCategory, discountCategories } from "../types";
import { toApiError } from "../api/mappers";

export function DiscountForm({
  businessName,
  onSave,
  onCancel,
  onLogout,
  onNew,
}: {
  businessName: string;
  onSave: (
    values: Omit<Discount, "id" | "status" | "businessName" | "saved">,
  ) => Promise<void>;
  onCancel: () => void;
  onLogout: () => void;
  onNew: () => void;
}) {
  const [benefit, setBenefit] = useState("");
  const [conditions, setConditions] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [code, setCode] = useState("");
  const [location, setLocation] = useState("");
  const [website, setWebsite] = useState("");
  const [category, setCategory] = useState<DiscountCategory | "">("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const nextErrors: Record<string, string> = {};
    if (!benefit.trim()) nextErrors.benefit = "El beneficio es obligatorio.";
    if (!conditions.trim())
      nextErrors.conditions = "Las condiciones son obligatorias.";
    if (!startDate) nextErrors.startDate = "La fecha de inicio es obligatoria.";
    if (!endDate)
      nextErrors.endDate = "La fecha de finalización es obligatoria.";
    if (!location.trim())
      nextErrors.location = "La ubicación es obligatoria.";
    if (!category) nextErrors.category = "La categoría es obligatoria.";
    setErrors(nextErrors);
    setFormError("");
    if (Object.keys(nextErrors).length) return;
    if (endDate < startDate) {
      setFormError(
        "La vigencia no es válida. La fecha de finalización debe ser posterior a la fecha de inicio.",
      );
      return;
    }

    setLoading(true);
    try {
      await onSave({
        benefit: benefit.trim(),
        conditions: conditions.trim(),
        startDate,
        endDate,
        code: code.trim(),
        location: location.trim(),
        website: website.trim(),
        category: category as DiscountCategory,
      });
    } catch (error) {
      // Los errores por campo del servidor usan los mismos nombres que este formulario.
      const apiError = toApiError(error);
      if (Object.keys(apiError.fields).length) setErrors(apiError.fields);
      else setFormError(apiError.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <BusinessShell
      businessName={businessName}
      onLogout={onLogout}
      active="new"
      onNew={onNew}
    >
      <button type="button" className="back-button" onClick={onCancel}>
        <span aria-hidden="true">←</span> Volver a mis descuentos
      </button>
      <div className="admin-page-heading">
        <span className="eyebrow">NUEVO DESCUENTO</span>
        <p className="admin-page-title">Publica un descuento</p>
        <p className="page-description">
          Completa la información del beneficio para publicarlo.
        </p>
      </div>
      <form className="discount-form detail-card" onSubmit={submit} noValidate>
        {formError && <Alert tone="error">{formError}</Alert>}
        <TextField
          id="discount-benefit"
          label="Beneficio"
          value={benefit}
          onChange={(value) => {
            setBenefit(value);
            setErrors((current) => ({ ...current, benefit: "" }));
          }}
          error={errors.benefit}
          placeholder="Indica el beneficio ofrecido"
          autoComplete="off"
        />
        <TextAreaField
          id="discount-conditions"
          label="Condiciones"
          value={conditions}
          onChange={(value) => {
            setConditions(value);
            setErrors((current) => ({ ...current, conditions: "" }));
          }}
          error={errors.conditions}
          placeholder="Indica las condiciones de uso"
        />
        <div className="field">
          <label>Categoría</label>
          <div className="category-picker" role="group" aria-label="Categoría">
            {discountCategories.map((item) => (
              <button
                type="button"
                className={`category-chip category-chip--${item.id} ${
                  category === item.id ? "category-chip--selected" : ""
                }`}
                onClick={() => {
                  setCategory(item.id);
                  setErrors((current) => ({ ...current, category: "" }));
                }}
                aria-pressed={category === item.id}
                key={item.id}
              >
                <CategoryIcon name={item.icon} />
                {item.name}
              </button>
            ))}
          </div>
          {errors.category && (
            <span className="field-error">{errors.category}</span>
          )}
        </div>
        <div className="form-grid">
          <TextField
            id="discount-start"
            label="Fecha de inicio"
            type="date"
            value={startDate}
            onChange={(value) => {
              setStartDate(value);
              setErrors((current) => ({ ...current, startDate: "" }));
              setFormError("");
            }}
            error={errors.startDate}
            placeholder=""
            autoComplete="off"
          />
          <TextField
            id="discount-end"
            label="Fecha de finalización"
            type="date"
            value={endDate}
            onChange={(value) => {
              setEndDate(value);
              setErrors((current) => ({ ...current, endDate: "" }));
              setFormError("");
            }}
            error={errors.endDate}
            placeholder=""
            autoComplete="off"
          />
        </div>
        <TextField
          id="discount-code"
          label="Código de descuento (opcional)"
          value={code}
          onChange={setCode}
          placeholder="Ingresa un código si aplica"
          autoComplete="off"
        />
        <TextField
          id="discount-location"
          label="Ubicación donde aplica"
          value={location}
          onChange={(value) => {
            setLocation(value);
            setErrors((current) => ({ ...current, location: "" }));
          }}
          error={errors.location}
          placeholder="Indica dónde está disponible"
          autoComplete="street-address"
        />
        <TextField
          id="discount-website"
          label="Sitio web (opcional)"
          type="url"
          value={website}
          onChange={setWebsite}
          placeholder="https://"
          autoComplete="url"
        />
        <div className="discount-form__actions">
          <Button type="submit" disabled={loading}>
            {loading ? (
              <>
                <Icon name="spinner" /> Guardando…
              </>
            ) : (
              "Publicar descuento"
            )}
          </Button>
        </div>
      </form>
    </BusinessShell>
  );
}
