import { useEffect, useState } from "react";
import QRCode from "qrcode";

/**
 * HU 1.4.1: código QR real (escaneable) con el identificador único de la credencial.
 * El SVG lo genera la librería qrcode a partir de un valor propio del sistema (no de entrada del usuario).
 */
export function DigitalQr({ value }: { value: string }) {
  const [svg, setSvg] = useState("");

  useEffect(() => {
    let cancelled = false;
    QRCode.toString(value, {
      type: "svg",
      errorCorrectionLevel: "M",
      margin: 0,
      color: { dark: "#14213d", light: "#ffffff" },
    })
      .then((markup) => {
        if (!cancelled) setSvg(markup);
      })
      .catch(() => {
        if (!cancelled) setSvg("");
      });
    return () => {
      cancelled = true;
    };
  }, [value]);

  return (
    <div
      className="digital-qr"
      role="img"
      aria-label={`Código QR de la identificación ${value}`}
      dangerouslySetInnerHTML={{ __html: svg }}
    />
  );
}
