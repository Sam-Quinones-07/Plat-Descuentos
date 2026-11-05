import { ApiError, formatInstant, type ApiDiscount, type ApiProfile, type ApiRequestDetail, type ApiRequestSummary } from "./index";
import { type BusinessAccount, type Discount, type DiscountCategory } from "../types";

export function toApiError(error: unknown): ApiError {
  return error instanceof ApiError
    ? error
    : new ApiError(0, "ERROR", "Ocurrió un error inesperado. Inténtalo de nuevo.");
}

export function toDiscount(discount: ApiDiscount): Discount {
  return {
    id: discount.id,
    businessName: discount.businessName,
    benefit: discount.benefit,
    conditions: discount.conditions,
    startDate: discount.startDate,
    endDate: discount.endDate,
    code: discount.code ?? "",
    location: discount.location,
    website: discount.website ?? "",
    category: discount.category as DiscountCategory,
    status: discount.status,
  };
}

export function toRequest(
  request: ApiRequestSummary | ApiRequestDetail,
): BusinessAccount {
  const detail = request as Partial<ApiRequestDetail>;
  return {
    id: request.id,
    tradeName: request.tradeName,
    legalName: detail.legalName ?? "",
    businessEmail: detail.businessEmail ?? "",
    phone: detail.phone ?? "",
    address: detail.address ?? "",
    representativeName: request.representativeName ?? detail.representativeName ?? "",
    document: detail.document ?? "",
    email: detail.email ?? "",
    nit: request.nit,
    confirmed: detail.emailConfirmed ?? false,
    submittedAt: formatInstant(request.submittedAt),
    status: request.status,
  };
}

export function businessFromProfile(profile: ApiProfile): BusinessAccount {
  return {
    id: "",
    tradeName: profile.tradeName ?? "",
    legalName: "",
    businessEmail: profile.email,
    phone: "",
    address: "",
    representativeName: "",
    document: "",
    email: "",
    nit: "",
    confirmed: true,
    submittedAt: "",
    status: profile.status ?? "approved",
  };
}
