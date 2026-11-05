import type { BusinessFormData } from "./models";

export const discountCategories = [
  { id: "restaurants", name: "Restaurantes", icon: "restaurant" },
  { id: "entertainment", name: "Entretenimiento", icon: "entertainment" },
  { id: "technology", name: "Tecnología", icon: "technology" },
  { id: "fashion", name: "Moda", icon: "fashion" },
  { id: "beauty", name: "Belleza", icon: "beauty" },
  { id: "stores", name: "Tiendas", icon: "stores" },
] as const;

export const passwordRules = [
  { label: "8 caracteres como mínimo", test: (value: string) => value.length >= 8 },
  { label: "Una letra mayúscula", test: (value: string) => /[A-Z]/.test(value) },
  { label: "Una letra minúscula", test: (value: string) => /[a-z]/.test(value) },
  { label: "Un número", test: (value: string) => /\d/.test(value) },
  {
    label: "Un carácter especial",
    test: (value: string) => /[^A-Za-z0-9]/.test(value),
  },
];

export const emptyBusinessForm: BusinessFormData = {
  tradeName: "",
  legalName: "",
  nit: "",
  businessEmail: "",
  phone: "",
  address: "",
  representativeName: "",
  document: "",
  email: "",
  password: "",
  confirmation: "",
};
