/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */
const BASE = "/Holding_Web";

/* ── Mapa de redirección por rol ── */
const RUTAS_ROL = {
    admin: BASE + "/pages/VistaAgregarProducto.html",
    mozo: BASE + "/pages/VistaMozo.html",
    cocina: BASE + "/pages/VistaCocina.html",
    delivery: BASE + "/pages/VistaDelivery.html",
    cliente: BASE + "/pages/VistaCliente.html",
    cajero: BASE + "/pages/VistaCliente.html"
};

document.addEventListener("DOMContentLoaded", () => {

    /* ── Toggle contraseña ── */
    const toggleBtn = document.getElementById("togglePassword");
    const passInput = document.getElementById("password");

    if (toggleBtn && passInput) {
        toggleBtn.addEventListener("click", () => {
            const visible = passInput.type === "text";
            passInput.type = visible ? "password" : "text";
            toggleBtn.querySelector("i").className = "bi bi-eye" + (visible ? "" : "-slash");
        });
    }

    /* ── Formulario de Login ── */
    const loginForm = document.getElementById("loginForm");
    const loginBtn = document.getElementById("loginBtn");

    if (loginForm) {
        loginForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            clearStatus();

            const identificador = document.getElementById("username").value.trim();
            const password = passInput.value.trim();

            /* Validaciones básicas client-side */
            if (!identificador) {
                return showStatus("⚠️ Ingresa tu usuario o correo", "warning");
            }
            if (!password || password.length < 4) {
                return showStatus("⚠️ Contraseña mínimo 4 caracteres", "warning");
            }

            setLoading(true);

            try {
                const params = new URLSearchParams({identificador, password});
                const res = await fetch(BASE + "/api/login", {
                    method: "POST",
                    headers: {"Content-Type": "application/x-www-form-urlencoded"},
                    body: params.toString()
                });

                const data = await res.json();

                if (res.ok && data.ok) {
                    showStatus(`¡Bienvenido, ${data.nombre}! 👋`, "success");
                    setTimeout(() => {
                        const destino = RUTAS_ROL[data.rol?.toLowerCase()] || RUTAS_ROL.cliente;
                        window.location.href = destino;
                    }, 900);
                } else {
                    showStatus(data.mensaje || "❌ Credenciales incorrectas", "error");
                    setLoading(false);
                }

            } catch (err) {
                console.error("Error de red:", err);
                showStatus("❌ Error de conexión con el servidor", "error");
                setLoading(false);
            }
        });
    }

    /* ── Modal recuperar contraseña ── */
    const recoverForm = document.getElementById("recoverForm");
    if (recoverForm) {
        recoverForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const correo = document.getElementById("recoverEmail").value.trim();

            if (!correo || !correo.includes("@")) {
                return Swal.fire({icon: "error", title: "Correo inválido",
                    confirmButtonColor: "#e67e22"});
            }

            /* Verificar si existe en el backend */
            try {
                const res = await fetch(BASE + "/api/usuarios/check", {
                    method: "POST",
                    headers: {"Content-Type": "application/x-www-form-urlencoded"},
                    body: new URLSearchParams({correo})
                });
                const data = await res.json();

                if (data.existe) {
                    Swal.fire({icon: "success", title: "✅ Instrucciones enviadas",
                        text: "Revisa tu bandeja (modo demo)", confirmButtonColor: "#e67e22"});
                } else {
                    Swal.fire({icon: "warning", title: "⚠️ Correo no encontrado",
                        text: "Verifica o contacta al administrador", confirmButtonColor: "#e67e22"});
                }

                /* Cerrar modal */
                bootstrap.Modal.getInstance(
                        document.getElementById("recoverModal"))?.hide();
                recoverForm.reset();

            } catch {
                Swal.fire({icon: "error", title: "Error de conexión",
                    confirmButtonColor: "#e67e22"});
            }
        });
    }
});

/* ── Helpers UI ── */
function setLoading(state) {
    const btn = document.getElementById("loginBtn");
    const text = btn?.querySelector(".btn-text");
    const loader = btn?.querySelector(".btn-loader");
    if (!btn)
        return;
    btn.disabled = state;
    btn.classList.toggle("loading", state);
    text?.classList.toggle("d-none", state);
    loader?.classList.toggle("d-none", !state);
}

function showStatus(msg, tipo) {
    const el = document.getElementById("statusMessage");
    if (!el)
        return;
    const colores = {success: "success", error: "danger", warning: "warning"};
    const iconos = {success: "check-circle", error: "x-circle", warning: "exclamation-triangle"};
    el.innerHTML = `
    <div class="alert alert-${colores[tipo] || "info"} alert-dismissible fade show" role="alert">
      <i class="bi bi-${iconos[tipo] || "info-circle"} me-2"></i>${msg}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>`;
}

function clearStatus() {
    const el = document.getElementById("statusMessage");
    if (el)
        el.innerHTML = "";
}