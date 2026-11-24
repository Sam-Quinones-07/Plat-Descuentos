import { useEffect, useState } from "react";
import { api, formatInstant, type ApiReviewModeration } from "../api";
import { toApiError } from "../api/mappers";
import { Alert, Button, ConfirmDialog, Icon } from "../components";
import { AdminShell } from "./AdminShell";

/** Moderación de reseñas (sección 7.5 y deuda técnica 10): el administrador retira reseñas falsas u ofensivas. */
export function AdminReviews({ onLogout }: { onLogout: () => void }) {
  const [reviews, setReviews] = useState<ApiReviewModeration[] | null>(null);
  const [error, setError] = useState("");
  const [toRemove, setToRemove] = useState<ApiReviewModeration | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let cancelled = false;
    api
      .recentReviews()
      .then((list) => !cancelled && setReviews(list))
      .catch((e) => {
        const apiError = toApiError(e);
        if (!cancelled && apiError.status !== 401) setError(apiError.message);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function remove() {
    if (!toRemove) return;
    setBusy(true);
    try {
      await api.removeReview(toRemove.id);
      setReviews((list) => (list ?? []).map((r) => (r.id === toRemove.id ? { ...r, removed: true } : r)));
    } catch (e) {
      setError(toApiError(e).message);
    } finally {
      setBusy(false);
      setToRemove(null);
    }
  }

  return (
    <AdminShell onLogout={onLogout} active="reviews">
      <div className="admin-page-heading">
        <span className="eyebrow">MODERACIÓN</span>
        <p className="admin-page-title">Reseñas recientes</p>
        <p className="page-description">Retira las reseñas falsas, ofensivas o repetidas.</p>
      </div>
      {error && <Alert tone="error">{error}</Alert>}
      <section className="admin-section">
        {!reviews && !error && <p className="reviews-section__empty">Cargando reseñas…</p>}
        {reviews && reviews.length === 0 && (
          <div className="empty-requests">
            <span className="empty-requests__icon">
              <Icon name="check" />
            </span>
            <p className="empty-requests__title">No hay reseñas publicadas</p>
          </div>
        )}
        {reviews && reviews.length > 0 && (
          <div className="review-list">
            {reviews.map((review) => (
              <article className="review-item" key={review.id}>
                <div className="review-item__meta">
                  <span className="review-item__author">{review.author}</span>
                  <span className="review-item__date">
                    {review.businessName} · {review.discountBenefit} · {formatInstant(review.publishedAt)}
                  </span>
                </div>
                <p className="review-item__text">{review.text}</p>
                <div className="review-form__actions">
                  {review.removed ? (
                    <span className="status-badge status-badge--rejected">
                      <span className="status-badge__dot" />
                      Retirada
                    </span>
                  ) : (
                    <Button variant="danger" onClick={() => setToRemove(review)}>
                      Retirar
                    </Button>
                  )}
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
      {toRemove && (
        <ConfirmDialog
          title="¿Retirar esta reseña?"
          description="Dejará de mostrarse en el detalle del descuento."
          confirmLabel="Retirar reseña"
          danger
          busy={busy}
          onConfirm={remove}
          onCancel={() => setToRemove(null)}
        />
      )}
    </AdminShell>
  );
}
