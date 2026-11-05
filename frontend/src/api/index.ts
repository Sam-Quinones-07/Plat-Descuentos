// Cliente de la API de PromoU (Spring Boot). Contrato: ver README del backend.
// VITE_API_URL vacío = mismo origen (usa el proxy de Vite en desarrollo, ver vite.config.ts).

const BASE_URL: string = (import.meta.env.VITE_API_URL as string | undefined) ?? "";
const TOKEN_KEY = "promou-token";

export const SESSION_ENDED_EVENT = "promou:session-ended";

export const tokenStore = {
  get: () => window.sessionStorage.getItem(TOKEN_KEY),
  set: (token: string) => window.sessionStorage.setItem(TOKEN_KEY, token),
  clear: () => window.sessionStorage.removeItem(TOKEN_KEY),
};

/** Error de la API: `campos` se mapea directo a los errores por campo de los formularios. */
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

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const token = tokenStore.get();
  const headers: Record<string, string> = {};
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (token) headers.Authorization = `Bearer ${token}`;

  let response: Response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, "SIN_CONEXION", "No fue posible conectar con el servidor. Inténtalo de nuevo.");
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
    // Con token enviado, un 401 significa sesión vencida o revocada (no es un login fallido).
    if (token && response.status === 401) {
      tokenStore.clear();
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

export type LoginResponse = { token: string; inactivityTimeoutSeconds: number; user: ApiProfile };

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
};

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
};

/** Un login descarta cualquier token viejo (si no, un 401 de credenciales se confundiría con sesión vencida). */
async function authenticate(path: string, email: string, password: string): Promise<LoginResponse> {
  tokenStore.clear();
  const response = await request<LoginResponse>("POST", path, { email, password });
  tokenStore.set(response.token);
  return response;
}

/* ---------- Endpoints ---------- */

export const api = {
  // Registro (HU 1.1.1, 1.2.1)
  registerStudent: (data: StudentRegistration) =>
    request<{ message: string }>("POST", "/api/auth/estudiantes/registro", data),
  registerBusiness: (data: BusinessRegistration) =>
    request<{ message: string }>("POST", "/api/auth/establecimientos/registro", data),

  // Sesión (HU 1.1.3, 1.1.4, 1.2.4, 1.2.5)
  login: (email: string, password: string) => authenticate("/api/auth/login", email, password),
  businessLogin: (email: string, password: string) =>
    authenticate("/api/auth/establecimientos/login", email, password),
  me: () => request<ApiProfile>("GET", "/api/auth/me"),
  async logout() {
    try {
      await request<void>("POST", "/api/auth/logout");
    } catch {
      // aunque el servidor no responda, el token local se descarta
    } finally {
      tokenStore.clear();
    }
  },

  // Estudiante (HU 1.4.1, 2.2.1)
  credential: () => request<ApiCredential>("GET", "/api/estudiantes/me/credencial"),
  listDiscounts: () => request<ApiDiscount[]>("GET", "/api/descuentos"),
  // HU 2.2.2: 410 si el descuento ya no está disponible, 404 si no existe
  discountDetail: (id: string) => request<ApiDiscount>("GET", `/api/descuentos/${encodeURIComponent(id)}`),

  // Establecimiento (HU 2.1.1)
  myDiscounts: () => request<ApiDiscount[]>("GET", "/api/establecimientos/me/descuentos"),
  publishDiscount: (data: DiscountInput) =>
    request<ApiDiscount>("POST", "/api/establecimientos/me/descuentos", data),

  // Administrador (HU 1.2.3)
  listRequests: () => request<ApiRequestSummary[]>("GET", "/api/admin/afiliaciones"),
  requestDetail: (id: string) => request<ApiRequestDetail>("GET", `/api/admin/afiliaciones/${id}`),
  decideRequest: (id: string, decision: "approved" | "rejected") =>
    request<ApiRequestDetail>(
      "POST",
      `/api/admin/afiliaciones/${id}/${decision === "approved" ? "aprobar" : "rechazar"}`,
      {},
    ),
};

/** Fecha corta en español, como la mostraba el front ("01 oct 2026"). */
export function formatInstant(value: string): string {
  return new Intl.DateTimeFormat("es", { day: "2-digit", month: "short", year: "numeric" }).format(new Date(value));
}
