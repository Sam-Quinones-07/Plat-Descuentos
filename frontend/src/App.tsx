import { useEffect, useRef, useState } from "react";
import { api, SESSION_ENDED_EVENT, type ApiProfile, type ApiRole, type LoginResponse } from "./api";
import { type Account, type BusinessAccount, type Discount, type SessionRole, type View } from "./types";
import { AdminDashboard, AdminRequest, AdminReviews } from "./admin";
import { BrandMark, ServerWakingBanner } from "./components";
import {
  BusinessLogin,
  BusinessRegister,
  BusinessSecurity,
  BusinessSubmitted,
  DiscountForm,
  DiscountsDashboard,
  ValidateCredential,
} from "./businesses";
import { ConfirmEmail, ForgotPassword, LegalPage, Login, Register, ResendConfirmation, ResetPassword } from "./auth";
import { StudentDiscountDetail, StudentDiscounts, StudentProfile, StudentSaved } from "./students";
import { businessFromProfile, toApiError, toDiscount, toRequest } from "./api/mappers";

/** Rutas con parámetro: #descuento/{id}, #editar-descuento/{id}, #confirmar/{token}, #restablecer/{token}. */
const PARAM_ROUTES: Array<[string, View]> = [
  ["#descuento/", "student-discount-detail"],
  ["#editar-descuento/", "business-discount-edit"],
  ["#confirmar/", "confirm-email"],
  ["#restablecer/", "reset-password"],
];

const SIMPLE_ROUTES: Record<string, View> = {
  "#registro": "register",
  "#iniciar-sesion": "login",
  "#inicio": "student-discounts",
  "#descuentos": "student-discounts",
  "#guardados": "student-saved",
  "#mi-perfil": "student-profile",
  "#registro-establecimiento": "business-register",
  "#solicitud-establecimiento": "business-submitted",
  "#iniciar-sesion-establecimiento": "business-login",
  "#sesion-establecimiento-expirada": "business-session-expired",
  "#mis-descuentos": "business-discounts",
  "#publicar-descuento": "business-discount-new",
  "#validar-identificacion": "business-validate",
  "#mi-cuenta-establecimiento": "business-account",
  "#administracion": "admin",
  "#solicitud-afiliacion": "admin-request",
  "#moderar-resenas": "admin-reviews",
  "#reenviar-confirmacion": "resend-confirmation",
  "#recuperar-contrasena": "forgot-password",
  "#terminos": "terms",
  "#privacidad": "privacy",
};

const HASH_BY_VIEW: Partial<Record<View, string>> = Object.fromEntries(
  Object.entries(SIMPLE_ROUTES).map(([hash, view]) => [view, hash]),
);
HASH_BY_VIEW["student-discounts"] = "#descuentos";

function parseHash(): { view: View; param: string } {
  const hash = window.location.hash;
  for (const [prefix, view] of PARAM_ROUTES) {
    if (hash.startsWith(prefix)) return { view, param: decodeURIComponent(hash.slice(prefix.length)) };
  }
  return { view: SIMPLE_ROUTES[hash] ?? "login", param: "" };
}

const SESSION_EXPIRED_MESSAGE = "Tu sesión expiró por inactividad. Inicia sesión nuevamente.";

export default function App() {
  const [route, setRoute] = useState(parseHash);
  const view = route.view;
  const [restoring, setRestoring] = useState(true);
  const [sessionRole, setSessionRole] = useState<SessionRole | null>(null);
  const [account, setAccount] = useState<Account | null>(null);
  const [businessAccount, setBusinessAccount] = useState<BusinessAccount | null>(null);
  const [loginNotice, setLoginNotice] = useState("");
  const [loginNoticeTone, setLoginNoticeTone] = useState<"success" | "warning">("warning");
  const [businessAccounts, setBusinessAccounts] = useState<BusinessAccount[]>([]);
  const [reviewingRequest, setReviewingRequest] = useState<BusinessAccount | null>(null);
  const [discounts, setDiscounts] = useState<Discount[]>([]);
  const [discountsLoading, setDiscountsLoading] = useState(false);
  const [editingDiscount, setEditingDiscount] = useState<Discount | null>(null);
  const [discountNotice, setDiscountNotice] = useState("");
  const [discountNoticeTone, setDiscountNoticeTone] = useState<"success" | "error">("success");
  const roleRef = useRef<SessionRole | null>(null);
  roleRef.current = sessionRole;

  function resetSession() {
    setSessionRole(null);
    setAccount(null);
    setBusinessAccount(null);
    setDiscounts([]);
    setBusinessAccounts([]);
    setReviewingRequest(null);
    setEditingDiscount(null);
    setDiscountNotice("");
  }

  function applyProfile(profile: ApiProfile) {
    setSessionRole(profile.role);
    if (profile.role === "student") {
      setAccount({
        firstName: profile.firstName ?? "",
        lastName: profile.lastName ?? "",
        email: profile.email,
        university: profile.university ?? "",
        confirmed: true,
      });
    }
    if (profile.role === "business") setBusinessAccount(businessFromProfile(profile));
  }

  // Restaura la sesión al recargar: la cookie HttpOnly no se puede leer, así que se pregunta al servidor.
  useEffect(() => {
    api
      .me()
      .then(applyProfile)
      .catch(() => undefined)
      .finally(() => setRestoring(false));
  }, []);

  useEffect(() => {
    const sync = () => setRoute(parseHash());
    window.addEventListener("hashchange", sync);
    return () => window.removeEventListener("hashchange", sync);
  }, []);

  // Sesión vencida o revocada en el servidor (30 min de inactividad, HU 1.1.3 esc. 7 / 1.2.4 esc. 6)
  useEffect(() => {
    function handleSessionEnded(event: Event) {
      const code = (event as CustomEvent<string>).detail;
      const wasBusiness = roleRef.current === "business";
      resetSession();
      if (wasBusiness) {
        navigate("business-session-expired");
      } else {
        setLoginNoticeTone("warning");
        setLoginNotice(code === "SESION_EXPIRADA" ? SESSION_EXPIRED_MESSAGE : "Debes iniciar sesión.");
        navigate("login");
      }
    }
    window.addEventListener(SESSION_ENDED_EVENT, handleSessionEnded);
    return () => window.removeEventListener(SESSION_ENDED_EVENT, handleSessionEnded);
  }, []);

  // Datos del establecimiento y del administrador según la pantalla.
  useEffect(() => {
    if (restoring || !sessionRole) return;
    let cancelled = false;
    if (sessionRole === "business" && view === "business-discounts") {
      setDiscountsLoading(true);
      api
        .myDiscounts()
        .then((list) => !cancelled && setDiscounts(list.map(toDiscount)))
        .catch(() => undefined)
        .finally(() => !cancelled && setDiscountsLoading(false));
    }
    if (sessionRole === "business" && view === "business-discount-edit" && route.param) {
      if (editingDiscount?.id !== route.param) {
        api
          .myDiscount(route.param)
          .then((d) => !cancelled && setEditingDiscount(toDiscount(d)))
          .catch(() => !cancelled && navigate("business-discounts"));
      }
    }
    if (sessionRole === "admin" && view === "admin") {
      api
        .listRequests()
        .then((list) => !cancelled && setBusinessAccounts(list.map(toRequest)))
        .catch(() => undefined);
    }
    return () => {
      cancelled = true;
    };
  }, [view, route.param, sessionRole, restoring]);

  function navigate(next: View, param = "") {
    if (next !== "login") setLoginNotice("");
    const prefix = PARAM_ROUTES.find(([, v]) => v === next)?.[0];
    const hash = prefix ? `${prefix}${encodeURIComponent(param)}` : (HASH_BY_VIEW[next] ?? "#iniciar-sesion");
    if (window.location.hash !== hash) window.location.hash = hash;
    setRoute({ view: next, param });
  }

  function completeLogin(response: LoginResponse, destination: View) {
    setLoginNotice("");
    applyProfile(response.user);
    navigate(response.user.role === "admin" ? "admin" : destination);
  }

  function completeBusinessLogin(response: LoginResponse) {
    applyProfile(response.user);
    navigate("business-discounts");
  }

  /** HU 1.1.4 / 1.2.5: borra la sesión en el servidor y la cookie, limpia los datos y vuelve al login. */
  async function logout(destination: "login" | "business-login") {
    await api.logout();
    resetSession();
    setLoginNoticeTone("success");
    setLoginNotice(destination === "login" ? "Cerraste sesión correctamente." : "");
    window.location.replace(destination === "login" ? "#iniciar-sesion" : "#iniciar-sesion-establecimiento");
    setRoute({ view: destination, param: "" });
  }

  async function openRequest(request: BusinessAccount) {
    try {
      const detail = await api.requestDetail(request.id);
      setReviewingRequest(toRequest(detail));
      navigate("admin-request");
    } catch {
      // los 401 los maneja SESSION_ENDED_EVENT
    }
  }

  function goToLoginFor(role?: ApiRole) {
    navigate(role === "business" ? "business-login" : "login");
  }

  function renderLogin(destination: View = "student-discounts") {
    return (
      <Login
        key={loginNotice}
        notice={loginNotice}
        noticeTone={loginNoticeTone}
        onSuccess={(response) => completeLogin(response, destination)}
        goToRegister={() => navigate("register")}
        goToBusiness={() => navigate("business-login")}
      />
    );
  }

  function renderBusinessLogin(sessionExpired = false) {
    return (
      <BusinessLogin
        sessionExpired={sessionExpired}
        goToRegister={() => navigate("business-register")}
        goToStudent={() => navigate("login")}
        onApproved={completeBusinessLogin}
      />
    );
  }

  const openDiscount = (discount: Discount) => navigate("student-discount-detail", discount.id);

  const businessApproved = sessionRole === "business" && businessAccount?.status === "approved";
  const studentActive = sessionRole === "student" && account?.confirmed;
  const studentShell = {
    studentName: account ? `${account.firstName} ${account.lastName}`.trim() : "",
    onLogout: () => logout("login"),
  };
  const businessShell = {
    businessName: businessAccount?.tradeName ?? "",
    onLogout: () => logout("business-login"),
    onNew: () => {
      setDiscountNotice("");
      navigate("business-discount-new");
    },
  };

  function screen() {
    // Pantallas públicas (enlaces del correo y documentos legales)
    if (view === "confirm-email")
      return (
        <ConfirmEmail
          token={route.param}
          goToLogin={goToLoginFor}
          goToResend={() => navigate("resend-confirmation")}
        />
      );
    if (view === "resend-confirmation") return <ResendConfirmation goToLogin={goToLoginFor} />;
    if (view === "forgot-password") return <ForgotPassword goToLogin={goToLoginFor} />;
    if (view === "reset-password")
      return (
        <ResetPassword token={route.param} goToLogin={goToLoginFor} goToForgot={() => navigate("forgot-password")} />
      );
    if (view === "terms" || view === "privacy")
      return <LegalPage kind={view} onBack={() => (window.history.length > 1 ? window.history.back() : navigate("login"))} />;

    if (restoring)
      return (
        <main className="home">
          <BrandMark className="home__mark" />
        </main>
      );

    // Estudiante
    if (view === "login") return studentActive ? renderStudentHome() : renderLogin();
    if (view === "student-discounts" || view === "home")
      return studentActive ? renderStudentHome() : renderLogin("student-discounts");
    if (view === "student-saved")
      return studentActive ? (
        <StudentSaved onSelect={openDiscount} goToDirectory={() => navigate("student-discounts")} shell={studentShell} />
      ) : (
        renderLogin("student-saved")
      );
    if (view === "student-discount-detail")
      return studentActive ? (
        <StudentDiscountDetail
          discountId={route.param}
          onBack={() => (window.history.length > 1 ? window.history.back() : navigate("student-discounts"))}
          onOpenId={() => navigate("student-profile")}
          shell={studentShell}
        />
      ) : (
        renderLogin("student-discounts")
      );
    if (view === "student-profile")
      return studentActive && account ? (
        <StudentProfile account={account} shell={studentShell} />
      ) : (
        renderLogin("student-profile")
      );

    // Establecimiento
    if (view === "business-register")
      return (
        <BusinessRegister
          onSubmitted={() => navigate("business-submitted")}
          goToLogin={() => navigate("business-login")}
          goToStudent={() => navigate("register")}
        />
      );
    if (view === "business-submitted") return <BusinessSubmitted goToLogin={() => navigate("business-login")} />;
    if (view === "business-login" || view === "business-session-expired")
      return businessApproved && view === "business-login" ? (
        renderBusinessHome()
      ) : (
        renderBusinessLogin(view === "business-session-expired")
      );
    if (!businessApproved && view.startsWith("business-")) return renderBusinessLogin();
    if (view === "business-discounts") return renderBusinessHome();
    if (view === "business-discount-new")
      return (
        <DiscountForm
          businessName={businessShell.businessName}
          onSave={async (values) => {
            await api.publishDiscount(values);
            setDiscountNoticeTone("success");
            setDiscountNotice("El descuento fue publicado correctamente.");
            navigate("business-discounts");
          }}
          onCancel={() => navigate("business-discounts")}
          onLogout={businessShell.onLogout}
          onNew={businessShell.onNew}
        />
      );
    if (view === "business-discount-edit")
      return editingDiscount && editingDiscount.id === route.param ? (
        <DiscountForm
          key={editingDiscount.id}
          businessName={businessShell.businessName}
          initial={editingDiscount}
          onSave={async (values) => {
            await api.editDiscount(editingDiscount.id, values);
            setEditingDiscount(null);
            setDiscountNoticeTone("success");
            setDiscountNotice("Los cambios del descuento se guardaron correctamente.");
            navigate("business-discounts");
          }}
          onCancel={() => {
            setEditingDiscount(null);
            navigate("business-discounts");
          }}
          onLogout={businessShell.onLogout}
          onNew={businessShell.onNew}
        />
      ) : (
        <main className="home">
          <BrandMark className="home__mark" />
        </main>
      );
    if (view === "business-validate") return <ValidateCredential {...businessShell} />;
    if (view === "business-account") return <BusinessSecurity {...businessShell} />;

    // Administrador
    if (view === "admin" || view === "admin-request" || view === "admin-reviews") {
      if (sessionRole !== "admin") return renderLogin("admin");
      if (view === "admin-reviews") return <AdminReviews onLogout={() => logout("login")} />;
      if (view === "admin-request" && reviewingRequest)
        return (
          <AdminRequest
            request={reviewingRequest}
            onBack={() => navigate("admin")}
            onDecision={async (decision) => {
              const updated = toRequest(await api.decideRequest(reviewingRequest.id, decision));
              setReviewingRequest(updated);
              setBusinessAccounts((list) => list.map((item) => (item.id === updated.id ? updated : item)));
            }}
            onLogout={() => logout("login")}
          />
        );
      return <AdminDashboard requests={businessAccounts} onReview={openRequest} onLogout={() => logout("login")} />;
    }

    return (
      <Register
        onRegistered={({ message }) => {
          setLoginNoticeTone("success");
          setLoginNotice(message);
          navigate("login");
        }}
        goToLogin={() => navigate("login")}
        goToBusiness={() => navigate("business-register")}
      />
    );
  }

  function renderStudentHome() {
    return <StudentDiscounts onSelect={openDiscount} shell={studentShell} />;
  }

  function renderBusinessHome() {
    return (
      <DiscountsDashboard
        businessName={businessShell.businessName}
        discounts={discounts}
        loading={discountsLoading}
        notice={discountNotice}
        noticeTone={discountNoticeTone}
        onNew={businessShell.onNew}
        onEdit={(discount) => {
          setDiscountNotice("");
          setEditingDiscount(discount);
          navigate("business-discount-edit", discount.id);
        }}
        onDeactivate={async (discount) => {
          try {
            const updated = toDiscount(await api.deactivateDiscount(discount.id));
            setDiscounts((list) => list.map((d) => (d.id === updated.id ? updated : d)));
            setDiscountNoticeTone("success");
            setDiscountNotice("El descuento fue desactivado y ya no aparece en el directorio.");
          } catch (error) {
            const apiError = toApiError(error);
            if (apiError.status === 401) return;
            setDiscountNoticeTone("error");
            setDiscountNotice(apiError.message);
            if (apiError.code === "DESCUENTO_YA_DESACTIVADO")
              setDiscounts((list) => list.map((d) => (d.id === discount.id ? { ...d, status: "inactive" } : d)));
          }
        }}
        onLogout={businessShell.onLogout}
      />
    );
  }

  return (
    <>
      <ServerWakingBanner />
      {screen()}
    </>
  );
}
