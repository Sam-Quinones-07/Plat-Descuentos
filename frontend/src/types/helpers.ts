import { discountCategories } from "./constants";
import type { DiscountCategory } from "./models";

export function getDiscountCategory(category: DiscountCategory) {
  return discountCategories.find((item) => item.id === category);
}

export function formatDiscountDate(value: string) {
  return new Intl.DateTimeFormat("es", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(new Date(`${value}T00:00:00`));
}

export function normalizeWebsite(website: string) {
  return /^https?:\/\//i.test(website) ? website : `https://${website}`;
}
