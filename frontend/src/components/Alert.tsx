import { type ReactNode } from "react";
import { Icon } from "./Icon";

export function Alert({
  tone,
  children,
}: {
  tone: "error" | "success" | "warning";
  children: ReactNode;
}) {
  return (
    <div className={`alert alert--${tone}`} role="status">
      <span className="alert__icon">
        <Icon name={tone === "success" ? "check" : "warning"} />
      </span>
      <span>{children}</span>
    </div>
  );
}
