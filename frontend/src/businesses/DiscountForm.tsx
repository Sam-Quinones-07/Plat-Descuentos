import { useState, type FormEvent } from "react";
import { Alert, Button, CategoryIcon, Icon, TextAreaField, TextField } from "../components";
import { BusinessShell } from "./BusinessShell";
import { type Discount, type DiscountCategory, discountCategories } from "../types";
import { toApiError } from "../api/mappers";

type DiscountValues = Omit<Discount, "id" | "status" | "businessName" | "saved" | "available">;

/** HU 2.1.1 Publicar y HU 2.1.2 Editar (con initial). Cancelar no guarda nada (HU 2.1.2 esc. 4). */
export function DiscountForm({
  businessName,
  initial,
  onSave,
  onCancel,
  onLogout,
  onNew,
}: {
  businessName: string;
  initial?: DiscountValues;
  onSave: (values: DiscountValues) => Promise<void>;
  onCancel: () => void;
  onLogout: () => void;
  onNew: () => void;
}) {
  const editing = Boolean(initial);
  const [benefit, setBenefit] = useState(initial?.benefit ?? "");
  const [conditions, setConditions] = useState(initial?.conditions ?? "");
  const [startDate, setStartDate] = useState(initial?.startDate ?? "");
  const [endDate, setEndDate] = useState(initial?.endDate ?? "");
  const [code, setCode] = useState(initial?.code ?? "");
  const [location, setLocation] = useState(initial?.location ?? "");
  const [website, setWebsite] = useState(initial?.website ?? "");
  const [category, setCategory] = useState<DiscountCategory | "">(initial?.category ?? "");
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
      active={editing ? "discounts" : "new"}
      onNew={onNew}
    >
      <button type="button" className="back-button" onClick={onCancel}>
        <span aria-hidden="true">←</span> Volver a mis descuentos
      </button>
      <div className="admin-page-heading">
        <span className="eyebrow">{editing ? "EDITAR DESCUENTO" : "NUEVO DESCUENTO"}</span>
        <p className="admin-page-title">{editing ? "Edita tu descuento" : "Publica un descuento"}</p>
        <p className="page-description">
          {editing
            ? "Actualiza las condiciones o la vigencia. Los estudiantes verán los cambios al guardar."
            : "Completa la información del beneficio para publicarlo."}
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
          {editing && (
            <Button variant="secondary" onClick={onCancel}>
              Cancelar
            </Button>
          )}
          <Button type="submit" disabled={loading}>
            {loading ? (
              <>
                <Icon name="spinner" /> Guardando…
              </>
            ) : (
              editing ? "Guardar cambios" : "Publicar descuento"
            )}
          </Button>
        </div>
      </form>
    </BusinessShell>
  );
}
