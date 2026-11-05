

export function DetailItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="detail-item">
      <span className="detail-item__label">{label}</span>
      <span className="detail-item__value">{value}</span>
    </div>
  );
}
