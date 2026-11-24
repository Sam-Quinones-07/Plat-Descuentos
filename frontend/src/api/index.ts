// Cliente de la API de PromoU (Spring Boot).
// La API siempre se llama por el mismo dominio del front: en Vercel con el rewrite de vercel.json, en desarrollo con
// el proxy de Vite y en Docker con nginx. Así la cookie de sesión (HttpOnly, SameSite=Strict) es de primera parte.

/** Cabecera obligatoria en peticiones que cambian datos (protección CSRF del backend). */
const CSRF_HEADER = "X-PromoU";

export const SESSION_ENDED_EVENT = "promou:session-ended";
/** El backend gratuito de Render tarda cerca de un minuto en despertar: el front avisa mientras tanto. */
export const SERVER_WAKING_EVENT = "promou:server-waking";

const SLOW_REQUEST_MS = 6000;
const RETRY_DELAY_MS = 5000;
const MAX_RETRIES = 18; // ~90 s: cubre el arranque en frío del plan gratuito

/** Hay una sesión abierta en este navegador (la cookie no se puede leer desde JavaScript). */
let sessionOpen = false;
export const session = {
  open: () => {
    sessionOpen = true;
  },
  close: () => {
    sessionOpen = false;
  },
};

/** Error de la API: `fields` se mapea directo a los errores por campo de los formularios. */
export class ApiError extends Error {
  status: number;
  code: string;
  fields: Record<string, string>;

  constructor(status: number, code: string, message: string, fields: Record<string, string> = {}) {
    super(message);
    this.status = status;
    this.code = code;
    this.fields = fields;
  }
}

let pendingSlow = 0;
function setWaking(delta: number) {
  pendingSlow = Math.max(0, pendingSlow + delta);
  window.dispatchEvent(new CustomEvent(SERVER_WAKING_EVENT, { detail: pendingSlow > 0 }));
}

const wait = (ms: number) => new Promise((resolve) => window.setTimeout(resolve, ms));

/** 502/503/504 o sin conexión mientras Render despierta el servicio. */
const isWakingStatus = (status: number) => status === 502 || status === 503 || status === 504;

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (method !== "GET") headers[CSRF_HEADER] = "1";

  // Solo las consultas (GET) se reintentan: repetir un POST podría duplicar una acción.
  const retries = method === "GET" ? MAX_RETRIES : 0;
  let slow = false;
  const slowTimer = window.setTimeout(() => {
    slow = true;
    setWaking(1);
  }, SLOW_REQUEST_MS);

  let response: Response | null = null;
  try {
    for (let attempt = 0; ; attempt++) {
      try {
        response = await fetch(path, {
          method,
          headers,
          credentials: "same-origin",
          body: body === undefined ? undefined : JSON.stringify(body),
        });
        if (!isWakingStatus(response.status) || attempt >= retries) break;
      } catch {
        if (attempt >= retries) {
          throw new ApiError(0, "SIN_CONEXION", "No fue posible conectar con el servidor. Inténtalo de nuevo en un momento.");
        }
      }
      if (!slow) {
        slow = true;
        setWaking(1);
      }
      await wait(RETRY_DELAY_MS);
    }
  } finally {
    window.clearTimeout(slowTimer);
    if (slow) setWaking(-1);
  }

  if (isWakingStatus(response.status)) {
    throw new ApiError(
      response.status,
      "SERVIDOR_INICIANDO",
      "El servidor se está iniciando. Espera unos segundos e inténtalo de nuevo.",
    );
  }
  if (response.status === 204) return undefined as T;

  let data: unknown = null;
  try {
    data = await response.json();
  } catch {
    data = null;
  }

  if (!response.ok) {
    const error = (data ?? {}) as { codigo?: string; mensaje?: string; campos?: Record<string, string> };
    const apiError = new ApiError(
      response.status,
      error.codigo ?? "ERROR",
      error.mensaje ?? "Ocurrió un error inesperado. Inténtalo más tarde.",
      error.campos ?? {},
    );
    // Con una sesión abierta, un 401 significa sesión vencida o revocada (no es un login fallido).
    if (sessionOpen && response.status === 401) {
      sessionOpen = false;
      window.dispatchEvent(new CustomEvent(SESSION_ENDED_EVENT, { detail: apiError.code }));
    }
    throw apiError;
  }
  return data as T;
}

/* ---------- Tipos del contrato ---------- */

export type ApiRole = "student" | "business" | "admin";

export type ApiProfile = {
  role: ApiRole;
  email: string;
  firstName?: string;
  lastName?: string;
  university?: string;
  tradeName?: string;
  status?: "pending" | "approved" | "rejected";
};

export type LoginResponse = { inactivityTimeoutSeconds: number; user: ApiProfile };

export type ApiDiscount = {
  id: string;
  businessName: string;
  benefit: string;
  conditions: string;
  startDate: string;
  endDate: string;
  code?: string | null;
  location: string;
  website?: string | null;
  category: string;
  status: "active" | "inactive";
  available: boolean;
  saved?: boolean | null;
};

export type ApiPage<T> = { items: T[]; page: number; size: number; totalItems: number; totalPages: number };

export type DiscountInput = {
  benefit: string;
  conditions: string;
  startDate: string;
  endDate: string;
  code: string;
  location: string;
  website: string;
  category: string;
};

export type ApiCredential = {
  digitalId: string;
  firstName: string;
  lastName: string;
  university: string;
  verified: boolean;
  valid: boolean;
  issuedAt: string;
  expiresAt: string;
};

export type ApiQrValidation = {
  result: "VALIDA" | "NO_VIGENTE" | "NO_RECONOCIDA";
  message: string;
  studentName?: string;
  university?: string;
  expiresAt?: string;
};

export type ApiReview = { id: string; author: string; text: string; publishedAt: string; mine?: boolean };

export type ApiReviewModeration = {
  id: string;
  author: string;
  businessName: string;
  discountBenefit: string;
  text: string;
  publishedAt: string;
  removed: boolean;
};

export type ApiRequestSummary = {
  id: string;
  tradeName: string;
  nit: string;
  representativeName: string;
  submittedAt: string;
  status: "pending" | "approved" | "rejected";
};

export type ApiRequestDetail = {
  id: string;
  tradeName: string;
  legalName: string;
  nit: string;
  businessEmail: string;
  phone: string;
  address: string;
  representativeName: string;
  document: string;
  email: string;
  emailConfirmed: boolean;
  status: "pending" | "approved" | "rejected";
  submittedAt: string;
  reviewedAt?: string | null;
  observations?: string | null;
};

export type StudentRegistration = {
  firstName: string;
  lastName: string;
  email: string;
  university: string;
  password: string;
  confirmation: string;
  acceptTerms: boolean;
};

export type BusinessRegistration = {
  tradeName: string;
  legalName: string;
  nit: string;
  businessEmail: string;
  phone: string;
  address: string;
  representativeName: string;
  document: string;
  email: string;
  password: string;
  confirmation: string;
  acceptTerms: boolean;
};

type Message = { message: string };

async function authenticate(path: string, email: string, password: string): Promise<LoginResponse> {
  session.close();
  const response = await request<LoginResponse>("POST", path, { email, password });
  session.open();
  return response;
}

const id = (value: string) => encodeURIComponent(value);

/* ---------- Endpoints ---------- */

export const api = {
  // Registro y confirmación por correo (HU 1.1.1, 1.1.2, 1.2.1, 1.2.2)
  registerStudent: (data: StudentRegistration) => request<Message>("POST", "/api/auth/estudiantes/registro", data),
  registerBusiness: (data: BusinessRegistration) =>
    request<Message>("POST", "/api/auth/establecimientos/registro", data),
  confirmEmail: (token: string) =>
    request<{ message: string; role: ApiRole }>("POST", "/api/auth/confirmar", { token }),
  resendConfirmation: (email: string) => request<Message>("POST", "/api/auth/confirmacion/reenviar", { email }),

  // Contraseñas (HU 1.3.1, 1.3.2)
  requestPasswordReset: (email: string) => request<Message>("POST", "/api/auth/contrasena/recuperar", { email }),
  checkResetLink: (token: string) => request<void>("GET", `/api/auth/contrasena/restablecer/${id(token)}`),
  resetPassword: (token: string, password: string, confirmation: string) =>
    request<Message>("POST", "/api/auth/contrasena/restablecer", { token, password, confirmation }),
  changePassword: (currentPassword: string, newPassword: string, confirmation: string) =>
    request<Message>("POST", "/api/auth/contrasena", { currentPassword, newPassword, confirmation }),

  // Sesión (HU 1.1.3, 1.1.4, 1.2.4, 1.2.5)
  login: (email: string, password: string) => authenticate("/api/auth/login", email, password),
  businessLogin: (email: string, password: string) =>
    authenticate("/api/auth/establecimientos/login", email, password),
  async me() {
    const profile = await request<ApiProfile>("GET", "/api/auth/me");
    session.open();
    return profile;
  },
  async logout() {
    try {
      await request<void>("POST", "/api/auth/logout");
    } catch {
      // aunque el servidor no responda, el front descarta la sesión
    } finally {
      session.close();
    }
  },

  // Estudiante (HU 1.4.1, 2.2.1 a 2.2.5, 3.1.1, 3.1.2)
  credential: () => request<ApiCredential>("GET", "/api/estudiantes/me/credencial"),
  listDiscounts: (category: string | null, page = 0, size = 12) => {
    const params = new URLSearchParams({ pagina: String(page), tamano: String(size) });
    if (category) params.set("categoria", category);
    return request<ApiPage<ApiDiscount>>("GET", `/api/descuentos?${params.toString()}`);
  },
  // HU 2.2.2: 410 si el descuento ya no está disponible, 404 si no existe
  discountDetail: (discountId: string) => request<ApiDiscount>("GET", `/api/descuentos/${id(discountId)}`),
  savedDiscounts: () => request<ApiDiscount[]>("GET", "/api/estudiantes/me/guardados"),
  saveDiscount: (discountId: string) => request<Message>("POST", `/api/estudiantes/me/guardados/${id(discountId)}`),
  unsaveDiscount: (discountId: string) => request<void>("DELETE", `/api/estudiantes/me/guardados/${id(discountId)}`),
  reviews: (discountId: string) => request<ApiReview[]>("GET", `/api/descuentos/${id(discountId)}/resenas`),
  publishReview: (discountId: string, text: string) =>
    request<ApiReview>("POST", `/api/descuentos/${id(discountId)}/resenas`, { text }),

  // Establecimiento (HU 1.4.2, 2.1.1 a 2.1.3)
  myDiscounts: () => request<ApiDiscount[]>("GET", "/api/establecimientos/me/descuentos"),
  myDiscount: (discountId: string) => request<ApiDiscount>("GET", `/api/establecimientos/me/descuentos/${id(discountId)}`),
  publishDiscount: (data: DiscountInput) => request<ApiDiscount>("POST", "/api/establecimientos/me/descuentos", data),
  editDiscount: (discountId: string, data: DiscountInput) =>
    request<ApiDiscount>("PUT", `/api/establecimientos/me/descuentos/${id(discountId)}`, data),
  deactivateDiscount: (discountId: string) =>
    request<ApiDiscount>("POST", `/api/establecimientos/me/descuentos/${id(discountId)}/desactivar`, {}),
  validateCredential: (code: string) =>
    request<ApiQrValidation>("POST", "/api/establecimientos/me/validar-credencial", { code }),

  // Administrador (HU 1.2.3 y moderación de reseñas)
  listRequests: () => request<ApiRequestSummary[]>("GET", "/api/admin/afiliaciones"),
  requestDetail: (requestId: string) => request<ApiRequestDetail>("GET", `/api/admin/afiliaciones/${id(requestId)}`),
  decideRequest: (requestId: string, decision: "approved" | "rejected", observations = "") =>
    request<ApiRequestDetail>(
      "POST",
      `/api/admin/afiliaciones/${id(requestId)}/${decision === "approved" ? "aprobar" : "rechazar"}`,
      { observations: observations.trim() || null },
    ),
  recentReviews: () => request<ApiReviewModeration[]>("GET", "/api/admin/resenas"),
  removeReview: (reviewId: string) => request<void>("POST", `/api/admin/resenas/${id(reviewId)}/retirar`, {}),
};

/** Fecha corta en español ("01 oct 2026"). */
export function formatInstant(value: string): string {
  return new Intl.DateTimeFormat("es", { day: "2-digit", month: "short", year: "numeric" }).format(new Date(value));
}
