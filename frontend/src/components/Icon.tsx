import { type IconName } from "../types";

export function Icon({ name }: { name: IconName }) {
  const common = {
    width: 20,
    height: 20,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: 1.8,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
    "aria-hidden": true,
  };

  if (name === "copy") {
    return (
      <svg {...common}>
        <rect x="9" y="9" width="11" height="11" rx="2" />
        <path d="M5 15V6a2 2 0 0 1 2-2h9" />
      </svg>
    );
  }
  if (name === "bookmark") {
    return (
      <svg {...common}>
        <path d="M6 4h12v16l-6-4-6 4V4Z" />
      </svg>
    );
  }
  if (name === "star") {
    return (
      <svg {...common}>
        <path d="m12 3.5 2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9L12 3.5Z" />
      </svg>
    );
  }
  if (name === "key") {
    return (
      <svg {...common}>
        <circle cx="8" cy="15" r="4" />
        <path d="m10.8 12.2 8.7-8.7M16 7l2.5 2.5M14 9l2 2" />
      </svg>
    );
  }
  if (name === "check") {
    return (
      <svg {...common}>
        <path d="m5 12 4.2 4.2L19 6.5" />
      </svg>
    );
  }
  if (name === "eye") {
    return (
      <svg {...common}>
        <path d="M2.5 12s3.4-6 9.5-6 9.5 6 9.5 6-3.4 6-9.5 6-9.5-6-9.5-6Z" />
        <circle cx="12" cy="12" r="2.5" />
      </svg>
    );
  }
  if (name === "eyeOff") {
    return (
      <svg {...common}>
        <path d="m3 3 18 18M10.6 6.2A9.8 9.8 0 0 1 12 6c6.1 0 9.5 6 9.5 6a15 15 0 0 1-2.1 2.8M6.1 6.1C3.8 7.8 2.5 12 2.5 12s3.4 6 9.5 6a9 9 0 0 0 3.1-.5M9.9 9.9a3 3 0 0 0 4.2 4.2" />
      </svg>
    );
  }
  if (name === "mail") {
    return (
      <svg {...common}>
        <rect x="3" y="5" width="18" height="14" rx="2" />
        <path d="m4 7 8 6 8-6" />
      </svg>
    );
  }
  if (name === "lock") {
    return (
      <svg {...common}>
        <rect x="4" y="10" width="16" height="11" rx="2" />
        <path d="M8 10V7a4 4 0 0 1 8 0v3" />
      </svg>
    );
  }
  if (name === "shield") {
    return (
      <svg {...common}>
        <path d="M12 3 4.5 6v5.2c0 4.5 3.1 8.5 7.5 9.8 4.4-1.3 7.5-5.3 7.5-9.8V6L12 3Z" />
        <path d="m9 12 2 2 4-4" />
      </svg>
    );
  }
  if (name === "user") {
    return (
      <svg {...common}>
        <circle cx="12" cy="8" r="3.5" />
        <path d="M5.5 20a6.5 6.5 0 0 1 13 0" />
      </svg>
    );
  }
  if (name === "warning") {
    return (
      <svg {...common}>
        <path d="M10.3 4.2 2.7 17.3A1.8 1.8 0 0 0 4.3 20h15.4a1.8 1.8 0 0 0 1.6-2.7L13.7 4.2a2 2 0 0 0-3.4 0Z" />
        <path d="M12 9v4M12 16.5h.01" />
      </svg>
    );
  }
  if (name === "tag") {
    return (
      <svg {...common}>
        <path d="M3 12V4.5A1.5 1.5 0 0 1 4.5 3H12l9 9-9 9-9-9Z" />
        <circle cx="7.5" cy="7.5" r="1.5" />
      </svg>
    );
  }
  if (name === "qr") {
    return (
      <svg {...common}>
        <rect x="3" y="3" width="7" height="7" rx="1" />
        <rect x="14" y="3" width="7" height="7" rx="1" />
        <rect x="3" y="14" width="7" height="7" rx="1" />
        <path d="M14 14h3v3h-3zM20 14v.01M14 20h.01M17 20h4v-3" />
      </svg>
    );
  }
  if (name === "pin") {
    return (
      <svg {...common}>
        <path d="M12 21s-7-6.1-7-11.5a7 7 0 0 1 14 0C19 14.9 12 21 12 21Z" />
        <circle cx="12" cy="9.5" r="2.5" />
      </svg>
    );
  }
  if (name === "calendar") {
    return (
      <svg {...common}>
        <rect x="3" y="5" width="18" height="16" rx="2" />
        <path d="M3 10h18M8 3v4M16 3v4" />
      </svg>
    );
  }
  if (name === "spinner") {
    return (
      <svg {...common} className="spinner">
        <path d="M20 12a8 8 0 1 1-5.3-7.5" />
      </svg>
    );
  }
  return (
    <svg {...common}>
      <path d="M5 12h14M14 7l5 5-5 5" />
    </svg>
  );
}
