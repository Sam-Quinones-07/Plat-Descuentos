

export function TextAreaField({
  id,
  label,
  value,
  onChange,
  error,
  placeholder,
  autoFocus,
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  placeholder: string;
  autoFocus?: boolean;
}) {
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      <div className={`input-shell textarea-shell ${error ? "input-shell--error" : ""}`}>
        <textarea
          id={id}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder={placeholder}
          autoFocus={autoFocus}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${id}-error` : undefined}
        />
      </div>
      {error && (
        <span className="field-error" id={`${id}-error`}>
          {error}
        </span>
      )}
    </div>
  );
}
