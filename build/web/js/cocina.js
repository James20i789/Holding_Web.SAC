/* ================================================================
   cocina.js  –  Panel de Cocina
   GET /api/ventas?estado=pendiente|preparando|listo
   PUT /api/ventas?id=&estado=
   GET /api/sesion
   ================================================================ */

const BASE = "/Holding_Web";
let intervaloPolling = null;

/* ── INIT ─────────────────────────────────────────────────────── */
document.addEventListener("DOMContentLoaded", async () => {
  await verificarSesionCocina();
  await renderCocina();
  intervaloPolling = setInterval(renderCocina, 3000);
});

/* ── SESIÓN ───────────────────────────────────────────────────── */
async function verificarSesionCocina() {
  try {
    const res  = await fetch(BASE + "/api/sesion");
    const data = await res.json();
    if (!res.ok || !data.ok) { redirigir(); return; }
    const permitidos = ["admin", "cocina"];
    if (!permitidos.includes((data.rol || "").toLowerCase())) redirigir();
  } catch { redirigir(); }
}

/* ── CARGAR PEDIDOS ───────────────────────────────────────────── */
async function renderCocina() {
  try {
    const [rPend, rPrep, rListo] = await Promise.all([
      fetch(BASE + "/api/ventas?estado=pendiente"),
      fetch(BASE + "/api/ventas?estado=preparando"),
      fetch(BASE + "/api/ventas?estado=listo")
    ]);

    const pendientes = parsearRespuesta(await rPend.json());
    const preparando = parsearRespuesta(await rPrep.json());
    const listos     = parsearRespuesta(await rListo.json());

    pintar("colPendientes",  pendientes);
    pintar("colPreparacion", preparando);
    pintar("colListos",      listos);
  } catch (e) {
    console.error("Error cargando cocina:", e);
  }
}

function parsearRespuesta(data) {
  return Array.isArray(data) ? data : [];
}

/* ── PINTAR COLUMNA ───────────────────────────────────────────── */
function pintar(id, lista) {
  const cont = document.getElementById(id);
  if (!cont) return;
  cont.innerHTML = "";

  if (lista.length === 0) {
    cont.innerHTML = `<p style="text-align:center;color:#9ca3af;margin-top:1rem">Sin pedidos</p>`;
    return;
  }

  lista.forEach(p => {
    const hora = p.fechaVenta
      ? new Date(p.fechaVenta).toLocaleTimeString("es-PE",
          { hour: "2-digit", minute: "2-digit" })
      : "—";

    cont.innerHTML += `
      <div class="card big-card">
        <div class="top">
          <strong>🪑 ${p.mesa || "Sin mesa"}</strong>
          <span>⏰ ${hora}</span>
        </div>
        <div class="body">
          🍽️ <b>Pedido:</b> ${p.detalleTexto || "—"}<br>
          📦 <b>Tipo:</b>   ${p.tipoServicio  || "—"}<br>
          💳 <b>Método:</b> ${p.metodoPago    || "—"}<br>
          💰 <b>Total:</b>  S/ ${Number(p.total).toFixed(2)}
          ${p.notas ? `<br>📝 <b>Notas:</b> ${p.notas}` : ""}
        </div>
        <div class="bottom">
          <div>${botonesAccion(p)}</div>
          <button onclick="imprimirPedido(${p.idVenta})" class="btn-print">
            🧾 Imprimir
          </button>
        </div>
      </div>`;
  });
}

/* ── BOTONES POR ESTADO ───────────────────────────────────────── */
function botonesAccion(p) {
  if (p.estado === "pendiente") {
    return `<button class="btn-prep"
              onclick="cambiarEstado(${p.idVenta}, 'preparando')">
              🔵 Preparación
            </button>`;
  }
  if (p.estado === "preparando") {
    return `
      <button class="btn-ok"
              onclick="cambiarEstado(${p.idVenta}, 'listo')">✅ Listo</button>
      <button class="btn-cancel"
              onclick="cambiarEstado(${p.idVenta}, 'pendiente')">↩ Regresar</button>`;
  }
  if (p.estado === "listo") {
    return `<button class="btn-cancel"
              onclick="cambiarEstado(${p.idVenta}, 'preparando')">↩ Volver</button>`;
  }
  return "";
}

/* ── CAMBIAR ESTADO ───────────────────────────────────────────── */
async function cambiarEstado(idVenta, nuevoEstado) {
  try {
    const res  = await fetch(
      `${BASE}/api/ventas?id=${idVenta}&estado=${nuevoEstado}`,
      { method: "PUT" }
    );
    const data = await res.json();
    if (!data.ok) console.warn("No se pudo cambiar estado:", data.mensaje);
    await renderCocina();
  } catch (e) {
    console.error("Error cambiando estado:", e);
  }
}

/* ── IMPRIMIR TICKET ──────────────────────────────────────────── */
async function imprimirPedido(idVenta) {
  try {
    const res = await fetch(`${BASE}/api/ventas?id=${idVenta}`);
    if (!res.ok) { alert("No se pudo cargar el pedido"); return; }
    const p   = await res.json();
    if (!p || p.ok === false) return;

    const hora = p.fechaVenta
      ? new Date(p.fechaVenta).toLocaleString("es-PE")
      : "—";

    const ventana = window.open("", "_blank", "width=400,height=600");
    ventana.document.write(`
      <!DOCTYPE html><html><head>
        <meta charset="UTF-8">
        <title>Ticket #${p.idVenta}</title>
        <style>
          body { font-family: monospace; padding: 12px; font-size: 13px; }
          h2   { text-align: center; }
          hr   { border: 1px dashed #000; }
          .fila { display:flex; justify-content:space-between; }
        </style>
      </head><body>
        <h2>🍽️ EL BATÁN</h2>
        <p style="text-align:center">Pedido #${p.idVenta}</p>
        <hr>
        <div class="fila"><span><b>Mesa:</b></span>   <span>${p.mesa || "—"}</span></div>
        <div class="fila"><span><b>Tipo:</b></span>   <span>${p.tipoServicio || "—"}</span></div>
        <div class="fila"><span><b>Pago:</b></span>   <span>${p.metodoPago || "—"}</span></div>
        <div class="fila"><span><b>Total:</b></span>  <span>S/ ${Number(p.total).toFixed(2)}</span></div>
        <hr>
        <p><b>Detalle:</b><br>${p.detalleTexto || "—"}</p>
        ${p.notas ? `<p><b>Notas:</b> ${p.notas}</p>` : ""}
        <hr>
        <p style="text-align:center"><small>${hora}</small></p>
        <script>window.print(); window.close();<\/script>
      </body></html>`);
    ventana.document.close();
  } catch (e) {
    alert("No se pudo generar el ticket: " + e.message);
  }
}

/* ── SALIR ────────────────────────────────────────────────────── */
function salir() {
  clearInterval(intervaloPolling);
  fetch(BASE + "/api/logout").finally(() => {
    window.location.href = BASE + "/pages/index.html";
  });
}

function redirigir() {
  window.location.href = BASE + "/pages/index.html";
}