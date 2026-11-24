import { BrandMark, LegalFooter } from "../components";

/** Sección 11.1 del documento de diseño: Términos de Servicio y Política de Tratamiento de Datos. */
export function LegalPage({ kind, onBack }: { kind: "terms" | "privacy"; onBack: () => void }) {
  return (
    <div className="legal-page">
      <header className="legal-page__header">
        <BrandMark className="home__mark" />
        <button type="button" className="back-button" onClick={onBack}>
          <span aria-hidden="true">←</span> Volver
        </button>
      </header>
      <article className="legal-page__body">
        {kind === "terms" ? <Terms /> : <Privacy />}
        <p className="legal-page__version">Versión 2026-11 · Vigente desde noviembre de 2026.</p>
      </article>
      <LegalFooter />
    </div>
  );
}

function Terms() {
  return (
    <>
      <span className="eyebrow">PROMOU</span>
      <h1>Términos de Servicio</h1>
      <p>
        Al registrarte en PromoU aceptas estos términos. La aceptación mediante la casilla del formulario de registro
        tiene validez como mensaje de datos según la Ley 527 de 1999.
      </p>
      <h2>1. Responsabilidad sobre las ofertas</h2>
      <p>
        Cada establecimiento es responsable de la veracidad, las condiciones y la vigencia de los descuentos que publica,
        en los términos del Estatuto del Consumidor (Ley 1480 de 2011). PromoU actúa como canal de difusión y no es parte
        de la transacción entre el estudiante y el comercio.
      </p>
      <h2>2. Uso de la identificación digital</h2>
      <p>
        La identificación digital es personal e intransferible. Prestarla o alterarla causa la suspensión de la cuenta.
      </p>
      <h2>3. Reseñas</h2>
      <p>
        Las reseñas deben describir experiencias reales con el beneficio. El administrador puede retirar reseñas falsas,
        ofensivas o masivas.
      </p>
      <h2>4. Reserva de derechos</h2>
      <p>
        El nombre PromoU, el diseño de la interfaz y la documentación son obra del equipo de trabajo; los logotipos y
        nombres de los establecimientos pertenecen a sus titulares. El código fuente se distribuye bajo la licencia MIT.
      </p>
    </>
  );
}

function Privacy() {
  return (
    <>
      <span className="eyebrow">PROMOU</span>
      <h1>Política de Tratamiento de Datos</h1>
      <p>
        El tratamiento de los datos personales en PromoU se rige por la Ley 1581 de 2012 y el Decreto 1074 de 2015
        (que compila el Decreto 1377 de 2013). Solo recolectamos los datos necesarios y los usamos para las finalidades
        declaradas.
      </p>
      <h2>Datos y finalidades</h2>
      <ul>
        <li>
          <strong>Estudiantes:</strong> nombre, apellido, universidad, correo institucional y contraseña (solo su hash),
          para verificar la condición de estudiante, emitir la identificación digital y dar acceso a los descuentos.
        </li>
        <li>
          <strong>Representantes de establecimientos:</strong> nombre, documento y correo del representante; NIT, razón
          social, nombre comercial, teléfono y dirección del establecimiento, para revisar la afiliación, comunicar su
          resultado y publicar los descuentos.
        </li>
        <li>
          <strong>Todos los usuarios:</strong> registros técnicos de sesión e intentos de inicio de sesión (guardados
          como hash), para la seguridad de las cuentas y la prevención de fraude.
        </li>
      </ul>
      <h2>Derechos del titular</h2>
      <p>
        Puedes conocer, actualizar, rectificar y suprimir tus datos, revocar la autorización y presentar quejas ante la
        Superintendencia de Industria y Comercio. Las consultas se responden en máximo 10 días hábiles y los reclamos en
        máximo 15 días hábiles (artículos 14 y 15 de la Ley 1581).
      </p>
      <h2>No cesión de datos</h2>
      <p>
        Los datos no se venden ni se comparten con terceros. A un establecimiento solo se le muestra lo indispensable para
        validar un beneficio: nombre del estudiante, universidad y vigencia de su identificación.
      </p>
      <h2>Prueba de la autorización</h2>
      <p>Guardamos la fecha, la hora y la versión de los documentos que aceptaste al registrarte.</p>
    </>
  );
}
