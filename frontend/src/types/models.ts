import type { discountCategories } from "./constants";

export type View =
  | "register"
  | "login"
  | "home"
  | "business-register"
  | "business-submitted"
  | "business-login"
  | "business-session-expired"
  | "business-discounts"
  | "business-discount-new"
  | "student-discounts"
  | "student-discount-detail"
  | "student-profile"
  | "student-saved"
  | "business-discount-edit"
  | "business-validate"
  | "business-account"
  | "admin"
  | "admin-request"
  | "admin-reviews"
  | "confirm-email"
  | "resend-confirmation"
  | "forgot-password"
  | "reset-password"
  | "terms"
  | "privacy";

export type Account = {
  firstName: string;
  lastName: string;
  email: string;
  university: string;
  confirmed: boolean;
};

export type SessionRole = "student" | "business" | "admin";

export type BusinessAccount = {
  id: string;
  tradeName: string;
  legalName: string;
  businessEmail: string;
  phone: string;
  address: string;
  representativeName: string;
  document: string;
  email: string;
  nit: string;
  confirmed: boolean;
  submittedAt: string;
  status: "pending" | "approved" | "rejected";
};

export type DiscountCategory = (typeof discountCategories)[number]["id"];

export type CategoryIconName =
  | (typeof discountCategories)[number]["icon"]
  | "all";

export type Discount = {
  id: string;
  businessName: string;
  benefit: string;
  conditions: string;
  startDate: string;
  endDate: string;
  code: string;
  location: string;
  website: string;
  category: DiscountCategory;
  status: "active" | "inactive";
  available: boolean;
  saved: boolean;
};

export type IconName =
  | "arrow"
  | "calendar"
  | "check"
  | "pin"
  | "qr"
  | "tag"
  | "eye"
  | "eyeOff"
  | "lock"
  | "mail"
  | "shield"
  | "spinner"
  | "user"
  | "warning"
  | "copy"
  | "bookmark"
  | "star"
  | "key";

export type BusinessFormData = {
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
