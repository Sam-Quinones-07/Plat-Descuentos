import { AuthLayout, Button, Icon } from "../components";

export function BusinessSubmitted({ goToLogin }: { goToLogin: () => void }) {
  return (
    <AuthLayout
      eyebrow="SOLICITUD REGISTRADA"
      title="Tu solicitud está pendiente de revisión"
      description="El administrador revisará la información proporcionada, incluido el NIT, antes de aprobar o rechazar la afiliación."
      audience="business"
      compact
    >
      <div className="status-panel">
        <span className="status-panel__icon">
          <Icon name="shield" />
        </span>
        <div>
          <p className="status-panel__title">Revisión pendiente</p>
          <p className="status-panel__description">
            La solicitud de afiliación fue registrada correctamente. Podrás
            iniciar sesión cuando sea aprobada.
          </p>
        </div>
      </div>
      <div className="result-actions">
        <Button onClick={goToLogin}>
          Ir a iniciar sesión <Icon name="arrow" />
        </Button>
      </div>
    </AuthLayout>
  );
}
