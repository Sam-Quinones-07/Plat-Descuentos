import { CategoryIcon, Icon } from "../components";
import { type Discount, formatDiscountDate, getDiscountCategory } from "../types";

/** Información resumida de un descuento (HU 2.2.1 esc. 1). Toda la tarjeta abre el detalle; el marcador guarda (HU 2.2.4). */
export function StudentDiscountCard({
  discount,
  onSelect,
  onToggleSave,
}: {
  discount: Discount;
  onSelect: () => void;
  onToggleSave?: () => void;
}) {
  const category = getDiscountCategory(discount.category);
  if (!category) return null;

  return (
    <article className="student-discount-card">
      <button type="button" className="student-discount-card__hit" onClick={onSelect}>
        <span className="sr-only">Ver detalle de {discount.benefit}</span>
      </button>
      <div className="student-discount-card__top">
        <span className={`discount-category category-chip--${category.id}`}>
          <CategoryIcon name={category.icon} />
          {category.name}
        </span>
        {onToggleSave && (
          <div className="student-discount-card__controls">
            <button
              type="button"
              className={`save-button ${discount.saved ? "save-button--active" : ""}`}
              onClick={onToggleSave}
              aria-pressed={discount.saved}
              aria-label={discount.saved ? "Quitar de guardados" : "Guardar descuento"}
              title={discount.saved ? "Quitar de guardados" : "Guardar descuento"}
            >
              <Icon name="bookmark" />
            </button>
          </div>
        )}
      </div>
      <span className="student-discount-card__business">{discount.businessName}</span>
      <p className="discount-card__benefit">{discount.benefit}</p>
      <div className="student-discount-card__meta">
        <span className="meta-line">
          <Icon name="pin" />
          {discount.location}
        </span>
        <span className="meta-line">
          <Icon name="calendar" />
          Hasta el {formatDiscountDate(discount.endDate)}
        </span>
      </div>
      <span className="student-discount-card__cta" aria-hidden="true">
        Ver detalle <Icon name="arrow" />
      </span>
    </article>
  );
}
