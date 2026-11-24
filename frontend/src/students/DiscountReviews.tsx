import { useEffect, useState, type FormEvent } from "react";
import { api, formatInstant, type ApiReview } from "../api";
import { toApiError } from "../api/mappers";
import { Alert, Button, Icon, TextAreaField } from "../components";

const MAX_LENGTH = 500;

/**
 * HU 3.1.1 Visualizar reseñas y HU 3.1.2 Escribir reseña. El texto se muestra como texto plano: React lo escapa,
 * así una reseña no puede inyectar HTML ni scripts (sección 7.5, XSS).
 */
export function DiscountReviews({ discountId }: { discountId: string }) {
  const [reviews, setReviews] = useState<ApiReview[] | null>(null);
  const [loadError, setLoadError] = useState("");
  const [writing, setWriting] = useState(false);
  const [text, setText] = useState("");
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [sending, setSending] = useState(false);

  useEffect(() => {
    let cancelled = false;
    api
      .reviews(discountId)
      .then((list) => !cancelled && setReviews(list))
      .catch((e) => {
        const apiError = toApiError(e);
        if (!cancelled && apiError.status !== 401) setLoadError(apiError.message);
      });
    return () => {
      cancelled = true;
    };
  }, [discountId]);

  const alreadyReviewed = Boolean(reviews?.some((review) => review.mine));

  async function submit(event: FormEvent) {
    event.preventDefault();
    setNotice("");
    if (!text.trim()) return setError("Debes escribir el contenido de la reseña.");
    if (text.length > MAX_LENGTH) return setError(`La reseña no puede superar ${MAX_LENGTH} caracteres.`);
    setSending(true);
    try {
      const review = await api.publishReview(discountId, text.trim());
      setReviews((list) => [{ ...review, mine: true }, ...(list ?? [])]);
      setText("");
      setWriting(false);
      setNotice("Tu reseña fue publicada.");
    } catch (e) {
      const apiError = toApiError(e);
      setError(apiError.fields.text ?? apiError.message);
    } finally {
      setSending(false);
    }
  }

  return (
    <section className="reviews-section" aria-labelledby="reviews-title">
      <div className="reviews-section__heading">
        <p className="form-section__title" id="reviews-title">
          Reseñas de estudiantes {reviews && reviews.length > 0 ? `(${reviews.length})` : ""}
        </p>
        {!writing && reviews && !alreadyReviewed && (
          <button type="button" className="reviews-section__write-btn" onClick={() => setWriting(true)}>
            Escribir reseña
          </button>
        )}
      </div>

      {notice && <Alert tone="success">{notice}</Alert>}

      {writing && (
        <form className="review-form" onSubmit={submit} noValidate>
          <TextAreaField
            id="review-text"
            autoFocus
            label="Cuéntales a otros estudiantes si el beneficio realmente se aplicó"
            value={text}
            onChange={(value) => {
              setText(value);
              setError("");
            }}
            error={error}
            placeholder="Escribe tu experiencia con este descuento"
          />
          <span className="small-note">
            {text.length}/{MAX_LENGTH} caracteres
          </span>
          <div className="review-form__actions">
            <Button
              variant="text"
              onClick={() => {
                setWriting(false);
                setError("");
              }}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={sending}>
              {sending ? <Icon name="spinner" /> : null} Publicar reseña
            </Button>
          </div>
        </form>
      )}

      {loadError && <Alert tone="error">{loadError}</Alert>}
      {!reviews && !loadError && <p className="reviews-section__empty">Cargando reseñas…</p>}
      {reviews && reviews.length === 0 && (
        <p className="reviews-section__empty">Aún no hay reseñas disponibles para este descuento.</p>
      )}
      {reviews && reviews.length > 0 && (
        <div className="review-list">
          {reviews.map((review) => (
            <article className="review-item" key={review.id}>
              <div className="review-item__meta">
                <span className="review-item__author">
                  {review.author}
                  {review.mine ? " (tú)" : ""}
                </span>
                <span className="review-item__date">{formatInstant(review.publishedAt)}</span>
              </div>
              <p className="review-item__text">{review.text}</p>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
