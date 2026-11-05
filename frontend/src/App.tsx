import { useEffect, useRef, useState } from "react";
import { api, SESSION_ENDED_EVENT, tokenStore, type ApiProfile, type LoginResponse } from "./api";
import { type Account, type BusinessAccount, type Discount, type SessionRole, type View } from "./types";
import { AdminDashboard, AdminRequest } from "./admin";
import { BrandMark } from "./components";
import { BusinessLogin, BusinessRegister, BusinessSubmitted, DiscountForm, DiscountsDashboard } from "./businesses";
import { Login, Register } from "./auth";
import { StudentDiscountDetail, StudentDiscounts, StudentProfile } from "./students";
import { businessFromProfile, toDiscount, toRequest } from "./api/mappers";

const DISCOUNT_HASH_PREFIX = "#descuento/";

/** Id del descuento en la URL (#descuento/{id}): el detalle sobrevive a una recarga de la página. */
function discountIdFromHash(): string {
  const hash = window.location.hash;
  return hash.startsWith(DISCOUNT_HASH_PREFIX) ? decodeURIComponent(hash.slice(DISCOUNT_HASH_PREFIX.length)) : "";
}

function viewFromHash(): View {
  const hash = window.location.hash;
  if (hash.startsWith(DISCOUNT_HASH_PREFIX)) return "student-discount-detail";
  if (hash === "#registro") return "register";
  if (hash === "#iniciar-sesion") return "login";
  if (hash === "#inicio") return "student-discounts";
  if (hash === "#registro-establecimiento") return "business-register";
  if (hash === "#solicitud-establecimiento") return "business-submitted";
  if (hash === "#iniciar-sesion-establecimiento") return "business-login";
  if (hash === "#sesion-establecimiento-expirada")
    return "business-session-expired";
  if (hash === "#mis-descuentos") return "business-discounts";
  if (hash === "#publicar-descuento") return "business-discount-new";
  if (hash === "#descuentos") return "student-discounts";
  if (hash === "#mi-perfil") return "student-profile";
  if (hash === "#administracion") return "admin";
  if (hash === "#solicitud-afiliacion") return "admin-request";
  return "login";
}

const SESSION_EXPIRED_MESSAGE =
  "Tu sesión expiró por inactividad. Inicia sesión nuevamente.";

export default function App() {
  const [view, setView] = useState<View>(viewFromHash);
  const [restoring, setRestoring] = useState(() => Boolean(tokenStore.get()));
  const [sessionRole, setSessionRole] = useState<SessionRole | null>(null);
  const [account, setAccount] = useState<Account | null>(null);
  const [businessAccount, setBusinessAccount] =
    useState<BusinessAccount | null>(null);
  const [loginNotice, setLoginNotice] = useState("");
  const [loginNoticeTone, setLoginNoticeTone] =
    useState<"success" | "warning">("warning");
  const [businessAccounts, setBusinessAccounts] = useState<BusinessAccount[]>([]);
  const [reviewingRequest, setReviewingRequest] = useState<BusinessAccount | null>(null);
  const [discounts, setDiscounts] = useState<Discount[]>([]);
  const [discountsLoading, setDiscountsLoading] = useState(false);
  const [discountId, setDiscountId] = useState(discountIdFromHash);
  const [discountNotice, setDiscountNotice] = useState("");
  const roleRef = useRef<SessionRole | null>(null);
  roleRef.current = sessionRole;

  function resetSession() {
    setSessionRole(null);
    setAccount(null);
    setBusinessAccount(null);
    setDiscounts([]);
    setBusinessAccounts([]);
    setReviewingRequest(null);
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
    if (profile.role === "business") {
      setBusinessAccount(businessFromProfile(profile));
    }
  }

  // Restaura la sesión al recargar la página (el token vive en sessionStorage).
  useEffect(() => {
    if (!tokenStore.get()) return;
    api
      .me()
      .then(applyProfile)
      .catch(() => tokenStore.clear())
      .finally(() => setRestoring(false));
  }, []);

  useEffect(() => {
    const syncView = () => {
      setView(viewFromHash());
      setDiscountId(discountIdFromHash());
    };
    window.addEventListener("hashchange", syncView);
    return () => window.removeEventListener("hashchange", syncView);
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
        setLoginNotice(
          code === "SESION_EXPIRADA" ? SESSION_EXPIRED_MESSAGE : "Debes iniciar sesión.",
        );
        navigate("login");
      }
    }
    window.addEventListener(SESSION_ENDED_EVENT, handleSessionEnded);
    return () => window.removeEventListener(SESSION_ENDED_EVENT, handleSessionEnded);
  }, []);

  useEffect(() => {
    if (view === "business-session-expired") {
      tokenStore.clear();
      resetSession();
    }
  }, [view]);

  // Carga de datos según el rol y la pantalla.
  useEffect(() => {
    if (restoring || !sessionRole) return;
    let cancelled = false;
    function load<T>(promise: Promise<T>, apply: (value: T) => void) {
      promise
        .then((value) => {
          if (!cancelled) apply(value);
        })
        .catch(() => undefined) // los 401 ya los maneja SESSION_ENDED_EVENT
        .finally(() => {
          if (!cancelled) setDiscountsLoading(false);
        });
    }

    if (sessionRole === "student" && view === "student-discounts") {
      setDiscountsLoading(true);
      load(api.listDiscounts(), (list) => setDiscounts(list.map(toDiscount)));
    }
    if (sessionRole === "business" && view === "business-discounts") {
      setDiscountsLoading(true);
      load(api.myDiscounts(), (list) => setDiscounts(list.map(toDiscount)));
    }
    if (sessionRole === "admin" && view === "admin") {
      load(api.listRequests(), (list) => setBusinessAccounts(list.map(toRequest)));
    }
    return () => {
      cancelled = true;
    };
  }, [view, sessionRole, restoring]);

  function navigate(next: View) {
    const hashes: Record<View, string> = {
      register: "#registro",
      login: "#iniciar-sesion",
      home: "#descuentos",
      "business-register": "#registro-establecimiento",
      "business-submitted": "#solicitud-establecimiento",
      "business-login": "#iniciar-sesion-establecimiento",
      "business-session-expired": "#sesion-establecimiento-expirada",
      "business-discounts": "#mis-descuentos",
      "business-discount-new": "#publicar-descuento",
      "student-discounts": "#descuentos",
      "student-discount-detail": `${DISCOUNT_HASH_PREFIX}${encodeURIComponent(discountId)}`,
      "student-profile": "#mi-perfil",
      admin: "#administracion",
      "admin-request": "#solicitud-afiliacion",
    };
    if (next !== "login") setLoginNotice("");
    window.location.hash = hashes[next];
    setView(next);
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

  /** HU 1.1.4 / 1.2.5: borra el token en el servidor y en el navegador, limpia los datos y vuelve al login. */
  async function logout(destination: "login" | "business-login") {
    await api.logout();
    resetSession();
    setLoginNoticeTone("success");
    setLoginNotice(destination === "login" ? "Cerraste sesión correctamente." : "");
    window.location.replace(
      destination === "login" ? "#iniciar-sesion" : "#iniciar-sesion-establecimiento",
    );
    setView(destination);
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

  function renderLogin(destination: View = "student-discounts") {
    return (
      <Login
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

  function openDiscount(discount: Discount) {
    setDiscountId(discount.id);
    window.location.hash = `${DISCOUNT_HASH_PREFIX}${encodeURIComponent(discount.id)}`;
    setView("student-discount-detail");
  }

  const businessApproved =
    sessionRole === "business" && businessAccount?.status === "approved";
  const studentActive = sessionRole === "student" && account?.confirmed;
  const studentShell = {
    studentName: account ? `${account.firstName} ${account.lastName}`.trim() : "",
    onLogout: () => logout("login"),
  };

  if (restoring)
    return (
      <main className="home">
        <BrandMark className="home__mark" />
      </main>
    );

  if (view === "login") return renderLogin();
  if (view === "student-discounts" || view === "home")
    return studentActive ? (
      <StudentDiscounts
        discounts={discounts}
        loading={discountsLoading}
        onSelect={openDiscount}
        shell={studentShell}
      />
    ) : (
      renderLogin("student-discounts")
    );
  if (view === "student-discount-detail") {
    if (!studentActive) return renderLogin("student-discounts");
    return (
      <StudentDiscountDetail
        discountId={discountId}
        onBack={() => navigate("student-discounts")}
        onOpenId={() => navigate("student-profile")}
        shell={studentShell}
      />
    );
  }
  if (view === "student-profile")
    return studentActive && account ? (
      <StudentProfile account={account} shell={studentShell} />
    ) : (
      renderLogin("student-profile")
    );
  if (view === "business-register")
    return (
      <BusinessRegister
        onSubmitted={() => navigate("business-submitted")}
        goToLogin={() => navigate("business-login")}
        goToStudent={() => navigate("register")}
      />
    );
  if (view === "business-submitted")
    return <BusinessSubmitted goToLogin={() => navigate("business-login")} />;
  if (view === "business-login" || view === "business-session-expired")
    return renderBusinessLogin(view === "business-session-expired");
  if (view === "business-discounts")
    return businessApproved ? (
      <DiscountsDashboard
        businessName={businessAccount?.tradeName ?? ""}
        discounts={discounts}
        notice={discountNotice}
        noticeTone="success"
        onNew={() => {
          setDiscountNotice("");
          navigate("business-discount-new");
        }}
        onLogout={() => logout("business-login")}
      />
    ) : (
      renderBusinessLogin()
    );
  if (view === "business-discount-new")
    return businessApproved ? (
      <DiscountForm
        businessName={businessAccount?.tradeName ?? ""}
        onSave={async (values) => {
          await api.publishDiscount(values);
          setDiscountNotice("El descuento fue publicado correctamente.");
          navigate("business-discounts");
        }}
        onCancel={() => navigate("business-discounts")}
        onLogout={() => logout("business-login")}
        onNew={() => navigate("business-discount-new")}
      />
    ) : (
      renderBusinessLogin()
    );
  if (view === "admin")
    return sessionRole === "admin" ? (
      <AdminDashboard
        requests={businessAccounts}
        onReview={openRequest}
        onLogout={() => logout("login")}
      />
    ) : (
      renderLogin("admin")
    );
  if (view === "admin-request")
    return sessionRole !== "admin" ? (
      renderLogin("admin")
    ) : reviewingRequest ? (
      <AdminRequest
        request={reviewingRequest}
        onBack={() => navigate("admin")}
        onDecision={async (decision) => {
          const updated = toRequest(
            await api.decideRequest(reviewingRequest.id, decision),
          );
          setReviewingRequest(updated);
          setBusinessAccounts((list) =>
            list.map((item) => (item.id === updated.id ? updated : item)),
          );
        }}
        onLogout={() => logout("login")}
      />
    ) : (
      <AdminDashboard
        requests={businessAccounts}
        onReview={openRequest}
        onLogout={() => logout("login")}
      />
    );

  return (
    <Register
      onRegistered={() => {
        setLoginNoticeTone("success");
        setLoginNotice("Registro exitoso. Ya puedes iniciar sesión.");
        navigate("login");
      }}
      goToLogin={() => navigate("login")}
      goToBusiness={() => navigate("business-register")}
    />
  );
}
