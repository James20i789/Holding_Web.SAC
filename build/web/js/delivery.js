/* ================================================================
   delivery.js  –  Panel Delivery
   GET /api/ventas?estado=nuevo
   PUT /api/ventas?id=&estado=
   GET /api/sesion
   ================================================================ */

const BASE = "/Holding_Web";
let intervaloPolling = null;

/* ── INIT ─────────────────────────────────────────────────────── */
document.addEventListener("DOMContentLoaded", async () => {
  await verificarSesionDelivery();
  await mostrarPedidos();
  intervaloPolling = setInterval(mostrarPedidos, 5000);
});

/* ── SESIÓN ───────────────────────────────────────────────────── */
async function verificarSesionDelivery() {
  try {
    const res  = await fetch(BASE + "/api/sesion");
    const data = await res.json();
    if (!res.ok || !data.ok) { redirigir(); return; }
    const permitidos = ["admin", "delivery"];
    if (!permitidos.includes((data.rol || "").toLowerCase())) redirigir();
  } catch { redirigir(); }
}

/* ── CARGAR PEDIDOS NUEVOS ────────────────────────────────────── */
async function mostrarPedidos() {
  const cont  = document.getElementById("listaPedidos");
  const vacio = document.getElementById("vacio");
  const counter = document.getElementById("contadorPedidos");
  if (!cont) return;

  try {
    const res    = await fetch(BASE + "/api/ventas?estado=nuevo");
    const data   = await res.json();
    const pedidos = Array.isArray(data) ? data : [];

    if (counter) counter.textContent = pedidos.length + " pedido" + (pedidos.length !== 1 ? "s" : "");

    cont.innerHTML = "";

    if (pedidos.length === 0) {
      vacio?.classList.remove("d-none");
      return;
    }
    vacio?.classList.add("d-none");

    pedidos.forEach(p => {
      const hora = p.fechaVenta
        ? new Date(p.fechaVenta).toLocaleString("es-PE")
        : "—";

      cont.innerHTML += `
        <div class="col-md-4">
          <div class="card card-pedido estado-nuevo shadow-sm p-3">
            <h5 class="fw-bold">${p.nombreCliente || "Cliente sin nombre"}</h5>
            <small class="text-muted d-block mb-1">📅 ${hora}</small>
            <small class="text-muted d-block mb-2">
              📦 ${p.tipoServicio || "—"} &nbsp;|&nbsp;
              💳 ${p.metodoPago   || "—"}
            </small>
            <hr class="my-2">
            <p class="mb-1"><b>Pedido:</b> ${p.detalleTexto || "—"}</p>
            ${p.notas ? `<p class="mb-1 text-muted"><small>📝 ${p.notas}</small></p>` : ""}
            <h6 class="text-primary fw-bold mb-3">S/ ${Number(p.total).toFixed(2)}</h6>
            <div class="d-flex justify-content-between">
              <button class="btn btn-success btn-accion"
                      onclick="aceptar(${p.idVenta})">
                ✅ Aceptar
              </button>
              <button class="btn btn-danger btn-accion"
                      onclick="rechazar(${p.idVenta})">
                ❌ Rechazar
              </button>
            </div>
          </div>
        </div>`;
    });

  } catch (e) {
    console.error("Error cargando pedidos delivery:", e);
    vacio?.classList.remove("d-none");
  }
}

/* ── ACEPTAR → estado "pendiente" (pasa a cocina) ────────────── */
async function aceptar(idVenta) {
  try {
    const res  = await fetch(
      `${BASE}/api/ventas?id=${idVenta}&estado=pendiente`,
      { method: "PUT" }
    );
    const data = await res.json();

    if (res.ok && data.ok) {
      Swal.fire({
        icon: "success", title: "✅ Enviado a cocina",
        timer: 1500, showConfirmButton: false
      });
      await mostrarPedidos();
    } else {
      Swal.fire("Error", data.mensaje || "No se pudo aceptar", "error");
    }
  } catch (e) {
    Swal.fire("Error de red", e.message, "error");
  }
}

/* ── RECHAZAR → estado "rechazado" ───────────────────────────── */
async function rechazar(idVenta) {
  const r = await Swal.fire({
    title: "¿Rechazar pedido?", icon: "warning",
    showCancelButton:  true,
    confirmButtonText: "Sí, rechazar",
    cancelButtonText:  "Cancelar",
    confirmButtonColor: "#dc3545"
  });
  if (!r.isConfirmed) return;

  try {
    const res  = await fetch(
      `${BASE}/api/ventas?id=${idVenta}&estado=rechazado`,
      { method: "PUT" }
    );
    const data = await res.json();

    if (res.ok && data.ok) {
      Swal.fire({ icon: "info", title: "Pedido rechazado", timer: 1500, showConfirmButton: false });
      await mostrarPedidos();
    } else {
      Swal.fire("Error", data.mensaje || "No se pudo rechazar", "error");
    }
  } catch (e) {
    Swal.fire("Error de red", e.message, "error");
  }
}

/* ── ACTUALIZAR MANUAL ────────────────────────────────────────── */
function actualizarManual() {
  clearInterval(intervaloPolling);
  mostrarPedidos();
  intervaloPolling = setInterval(mostrarPedidos, 5000);
}

/* ── CERRAR SESIÓN ────────────────────────────────────────────── */
function cerrarSesion() {
  clearInterval(intervaloPolling);
  fetch(BASE + "/api/logout").finally(() => {
    window.location.href = BASE + "/pages/index.html";
  });
}

function redirigir() {
  window.location.href = BASE + "/pages/index.html";
}