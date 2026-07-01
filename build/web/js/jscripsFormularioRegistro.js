/* ================================================================
   jscripsFormularioRegistro.js
   POST /api/usuarios/registrar   → registrar nuevo cliente
   POST /api/usuarios/check       → verificar correo existente
   ================================================================ */

const BASE = "/Holding_Web";

document.addEventListener("DOMContentLoaded", () => {

  /* ── Toggle contraseña ──────────────────────────────────────── */
  const togglePass = document.getElementById("togglePass");
  const passInput  = document.getElementById("password");

  if (togglePass && passInput) {
    togglePass.addEventListener("click", () => {
      const visible = passInput.type === "text";
      passInput.type = visible ? "password" : "text";
      togglePass.querySelector("i").className =
        "bi bi-eye" + (visible ? "" : "-slash");
    });
  }

  /* ── Verificar correo en blur ───────────────────────────────── */
  const correoInput = document.getElementById("correo");
  if (correoInput) {
    correoInput.addEventListener("blur", async function () {
      const email = this.value.trim().toLowerCase();
      if (!email || !/^\S+@\S+\.\S+$/.test(email)) return;

      try {
        const res  = await fetch(BASE + "/api/usuarios/check", {
          method:  "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body:    new URLSearchParams({ correo: email })
        });
        const data = await res.json();

        if (data.existe) {
          this.classList.add("is-invalid");
          this.classList.remove("is-valid");
          Swal.fire({
            icon: "warning",
            title: "⚠️ Correo ya registrado",
            text:  "¿Olvidaste tu contraseña?",
            confirmButtonColor: "#e67e22",
            showCancelButton:   true,
            cancelButtonText:   "Continuar",
            confirmButtonText:  "Ir al login"
          }).then(r => {
            if (r.isConfirmed)
              window.location.href = BASE + "/pages/index.html";
          });
        } else {
          this.classList.remove("is-invalid");
          this.classList.add("is-valid");
        }
      } catch (err) {
        console.warn("No se pudo verificar correo:", err);
      }
    });
  }

  /* ── Formulario de Registro ─────────────────────────────────── */
  const form   = document.getElementById("registerForm");
  const btnReg = document.getElementById("btnRegister");
  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const nombre    = document.getElementById("nombre").value.trim();
    const correo    = correoInput?.value.trim().toLowerCase() || "";
    const password  = passInput?.value || "";
    const direccion = document.getElementById("direccion").value.trim();

    /* ── Validaciones ─────────────────────────────────────────── */
    if (nombre.length < 3)
      return Swal.fire({ icon: "warning", title: "Nombre inválido",
        text: "Mínimo 3 caracteres", confirmButtonColor: "#e67e22" });

    if (!/^\S+@\S+\.\S+$/.test(correo))
      return Swal.fire({ icon: "warning", title: "Correo inválido",
        confirmButtonColor: "#e67e22" });

    if (password.length < 6)
      return Swal.fire({ icon: "warning", title: "Contraseña débil",
        text: "Mínimo 6 caracteres", confirmButtonColor: "#e67e22" });

    if (direccion.length < 10)
      return Swal.fire({ icon: "warning", title: "Dirección muy corta",
        text: "Mínimo 10 caracteres", confirmButtonColor: "#e67e22" });

    /* ── Loader ───────────────────────────────────────────────── */
    if (btnReg) {
      btnReg.disabled = true;
      btnReg.querySelector(".btn-text")?.classList.add("d-none");
      btnReg.querySelector(".btn-loader")?.classList.remove("d-none");
    }

    try {
      const res = await fetch(BASE + "/api/usuarios/registrar", {
        method:  "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body:    new URLSearchParams({ nombre, correo, password, direccion }).toString()
      });
      const data = await res.json();

      if (res.ok && data.ok) {
        await Swal.fire({
          icon:             "success",
          title:            "¡Registro exitoso! 🎉",
          text:             `Bienvenido ${nombre.split(" ")[0]}, ya puedes iniciar sesión.`,
          confirmButtonColor: "#e67e22",
          timer:            2500,
          timerProgressBar: true
        });
        window.location.href = BASE + "/pages/index.html";
      } else {
        Swal.fire({ icon: "error", title: "Error",
          text: data.mensaje || "Ocurrió un problema, intenta de nuevo.",
          confirmButtonColor: "#e67e22" });
      }

    } catch (err) {
      console.error("Error de red:", err);
      Swal.fire({ icon: "error", title: "Sin conexión",
        text: "No se pudo conectar al servidor.", confirmButtonColor: "#e67e22" });
    } finally {
      if (btnReg) {
        btnReg.disabled = false;
        btnReg.querySelector(".btn-text")?.classList.remove("d-none");
        btnReg.querySelector(".btn-loader")?.classList.add("d-none");
      }
    }
  });
});