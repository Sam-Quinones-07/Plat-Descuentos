

export function BrandMark({ className }: { className: string }) {
  return (
    <div className={className} aria-hidden="true">
      <svg width="44" height="36" viewBox="0 0 44 36" fill="none">
        <path
          d="M5 2H29.5L42 18L29.5 34H5C3.34 34 2 32.66 2 31V5C2 3.34 3.34 2 5 2Z"
          fill="#8f8cff"
        />
        <circle cx="32" cy="18" r="2.25" fill="#14213d" />
        <text
          x="17"
          y="23.5"
          fill="#ffffff"
          fontSize="16"
          fontWeight="800"
          textAnchor="middle"
        >
          U
        </text>
      </svg>
    </div>
  );
}
