import { type CategoryIconName } from "../types";

export function CategoryIcon({ name }: { name: CategoryIconName }) {
  const common = {
    width: 18,
    height: 18,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: 1.8,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
    "aria-hidden": true,
  };

  if (name === "restaurant") {
    return (
      <svg {...common}>
        <path d="M7 3v7M4.5 3v4.5A2.5 2.5 0 0 0 7 10M9.5 3v4.5A2.5 2.5 0 0 1 7 10v11M16 3v18M16 3c2.5 1.2 4 3.7 4 6.5V12h-4" />
      </svg>
    );
  }
  if (name === "entertainment") {
    return (
      <svg {...common}>
        <rect x="3" y="5" width="18" height="14" rx="2" />
        <path d="M3 9h18M7 5l2 4M13 5l2 4M17 5l2 4" />
        <path d="m10 12 4 2.5-4 2.5v-5Z" />
      </svg>
    );
  }
  if (name === "technology") {
    return (
      <svg {...common}>
        <rect x="3" y="4" width="18" height="13" rx="2" />
        <path d="M8 21h8M12 17v4" />
      </svg>
    );
  }
  if (name === "fashion") {
    return (
      <svg {...common}>
        <path d="m8 4 4 2 4-2 4 4-3 3v9H7v-9L4 8l4-4Z" />
        <path d="M9 4a3 3 0 0 0 6 0" />
      </svg>
    );
  }
  if (name === "beauty") {
    return (
      <svg {...common}>
        <path d="M9 3h6v4H9zM8 7h8l1 13H7L8 7Z" />
        <path d="M10 11h4M18.5 4.5l.5 1.2 1.2.5-1.2.5-.5 1.3-.5-1.3-1.2-.5 1.2-.5.5-1.2Z" />
      </svg>
    );
  }
  if (name === "stores") {
    return (
      <svg {...common}>
        <path d="M5 8h14l1 13H4L5 8Z" />
        <path d="M8 9V6a4 4 0 0 1 8 0v3" />
      </svg>
    );
  }
  return (
    <svg {...common}>
      <rect x="3" y="3" width="7" height="7" rx="1" />
      <rect x="14" y="3" width="7" height="7" rx="1" />
      <rect x="3" y="14" width="7" height="7" rx="1" />
      <rect x="14" y="14" width="7" height="7" rx="1" />
    </svg>
  );
}
