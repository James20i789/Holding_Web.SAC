/* ================================================================
   mozo.js  –  Panel del Mozo
   GET  /Holding_Web/api/productos
   GET  /Holding_Web/api/categorias
   GET  /Holding_Web/api/sesion
   POST /Holding_Web/api/ventas
   GET  /Holding_Web/api/ventas?estado=pendiente  (ver pedidos)
   PUT  /Holding_Web/api/ventas?id=&estado=       (cambiar estado)
   ================================================================ */

const BASE = "/Holding_Web";

let productos  = [];
let categorias = [];
let comanda    = [];

/* ================================================================
   INIT
   ================================================================ */
document.addEventListener("DOMContentLoaded", async () => {
  await verificarSesionMozo();
  await Promise.all([cargarCategorias(), cargarProductos()]);

  document.getElementById("buscarProducto")
    ?.addEventListener("input",  mostrarProductos);
  document.getElementById("filtroCategoria")
    ?.addEventListener("change", mostrarProductos);
  document.getElementById("filtroServicio")
    ?.addEventListener("change", () => verPedidos());
});

/* ================================================================
   SESIÓN
   ================================================================ */
async function verificarSesionMozo() {
  try {
    const res  = await fetch(BASE + "/api/sesion");
    const data = await res.json();
    if (!res.ok || !data.ok) {
      window.location.href = BASE + "/pages/index.html"; return;
    }
    const rolesPermitidos = ["admin", "mozo"];
    if (!rolesPermitidos.includes(data.rol?.toLowerCase())) {
      window.location.href = BASE + "/pages/index.html"; return;
    }
  } catch {
    window.location.href = BASE + "/pages/index.html";
  }
}

/* ================================================================
   CARGAR DATOS
   ================================================================ */
async function cargarCategorias() {
  try {
    const res  = await fetch(BASE + "/api/categorias");
    categorias = await res.json();

    const select = document.getElementById("filtroCategoria");
    if (!select) return;
    select.innerHTML = `<option value="all">Todas</option>`;
    categorias.forEach(c => {
      select.innerHTML +=
        `<option value="${c.idCategoria}">${c.nombre}</option>`;
    });
  } catch (e) { console.error("Error categorías:", e); }
}

async function cargarProductos() {
  try {
    const res  = await fetch(BASE + "/api/productos");
    productos  = await res.json();
    mostrarProductos();
  } catch (e) { console.error("Error productos:", e); }
}

/* ================================================================
   RENDER PRODUCTOS
   ================================================================ */
function mostrarProductos() {
  const cont   = document.getElementById("listaProductos");
  const filtro = document.getElementById("filtroCategoria")?.value || "all";
  const texto  = document.getElementById("buscarProducto")
    ?.value.toLowerCase() || "";
  if (!cont) return;

  let lista = productos.filter(p => {
    const catOK  = filtro === "all" || String(p.idCategoria) === String(filtro);
    const textOK = (p.nombre || "").toLowerCase().includes(texto);
    return catOK && textOK;
  });

  cont.innerHTML = "";

  if (lista.length === 0) {
    cont.innerHTML = `<div class="col-12 text-center text-muted py-4">
      No se encontraron productos</div>`;
    return;
  }

  lista.forEach(p => {
    const cat     = categorias.find(c => String(c.idCategoria) === String(p.idCategoria));
    const sinStock = p.stock <= 0;

    cont.innerHTML += `
      <div class="col-6 col-md-4 col-lg-3">
        <div class="card h-100 shadow-sm ${sinStock ? "opacity-50" : ""}"
             style="cursor:${sinStock ? "default" : "pointer"}"
             onclick="${sinStock ? "" : `agregar(${p.idProducto})`}">
          <span class="badge position-absolute top-0 end-0 m-2"
                style="background:${cat?.color || "#999"}">
            ${cat?.nombre || "General"}
          </span>
          <img src="${p.imagen || "/Holding_Web/img/noimage.png"}"
               class="card-img-top"
               style="height:120px;object-fit:cover"
               onerror="this.src='/Holding_Web/img/noimage.png'">
          <div class="card-body p-2 text-center">
            <h6 class="small mb-1">${p.nombre}</h6>
            <p class="fw-bold text-primary mb-0">
              S/ ${Number(p.precio).toFixed(2)}
            </p>
            <small class="${sinStock ? "text-danger fw-bold" : "text-muted"}">
              ${sinStock ? "AGOTADO" : "Stock: " + p.stock}
            </small>
          </div>
        </div>
      </div>`;
  });
}

/* ================================================================
   COMANDA (carrito del mozo)
   ================================================================ */
function agregar(id) {
  const prod = productos.find(p => p.idProducto == id);
  if (!prod) return;

  const item = comanda.find(i => i.idProducto == id);
  const cantActual = item ? item.cantidad : 0;

  if (cantActual >= prod.stock) {
    Swal.fire("Sin stock", "No hay más unidades disponibles.", "warning");
    return;
  }

  if (item) item.cantidad++;
  else comanda.push({ ...prod, cantidad: 1 });

  actualizarComanda();
}

function actualizarComanda() {
  const cont   = document.getElementById("listaComanda");
  const total  = document.getElementById("totalPedido");
  const count  = document.getElementById("itemCount");
  if (!cont) return;

  cont.innerHTML = "";
  let totalVal = 0;
  let items    = 0;

  comanda.forEach(i => {
    const subtotal = i.precio * i.cantidad;
    totalVal += subtotal;
    items    += i.cantidad;

    cont.innerHTML += `
      <tr>
        <td>
          <strong>${i.nombre}</strong><br>
          <small class="text-muted">${i.cantidad} × S/ ${Number(i.precio).toFixed(2)}</small>
        </td>
        <td class="text-end">
          S/ ${subtotal.toFixed(2)}<br>
          <button class="btn btn-sm btn-outline-danger mt-1"
                  onclick="eliminarDeComanda(${i.idProducto})">
            <i class="bi bi-trash"></i>
          </button>
        </td>
      </tr>`;
  });

  if (total) total.textContent  = "S/ " + totalVal.toFixed(2);
  if (count) count.textContent  = items + " items";
}

function eliminarDeComanda(id) {
  comanda = comanda.filter(i => i.idProducto !== id);
  actualizarComanda();
}

/* ================================================================
   FINALIZAR PEDIDO  →  POST /api/ventas
   ================================================================ */
async function finalizarPedido() {
  const mesa  = document.getElementById("mesa")?.value.trim();
  const tipo  = document.getElementById("tipoPedido")?.value || "Mesa";
  const notas = document.getElementById("notas")?.value || "";
  const pago  = document.querySelector('input[name="pago"]:checked')?.value || "Efectivo";

  if (comanda.length === 0) {
    return Swal.fire("Comanda vacía", "Agrega productos", "warning");
  }
  if (!mesa) {
    return Swal.fire("Falta dato", "Ingresa mesa o nombre del cliente", "warning");
  }

  const total = comanda.reduce((a, i) => a + i.precio * i.cantidad, 0);

  try {
    const params = new URLSearchParams();
    params.append("mesa",         mesa);
    params.append("tipoServicio", tipo);
    params.append("metodoPago",   pago);
    params.append("notas",        notas);
    params.append("total",        total.toString());

    comanda.forEach(i => {
      params.append("idProducto",    i.idProducto);
      params.append("cantidad",      i.cantidad);
      params.append("precioUnitario",i.precio);
    });

    const res  = await fetch(BASE + "/api/ventas", {
      method:  "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body:    params.toString()
    });
    const data = await res.json();

    if (res.ok && data.ok) {
      await Swal.fire("✅ Pedido enviado a cocina", "", "success");
      comanda = [];
      actualizarComanda();
      await cargarProductos(); /* actualiza stock visible */
      location.reload();
    } else {
      Swal.fire("Error",
        data.mensaje || "No se pudo registrar (verifica stock)", "error");
    }
  } catch (e) {
    Swal.fire("Error de red", e.message, "error");
  }
}

/* ================================================================
   VER PEDIDOS ACTIVOS (modal o sección de historial)
   ================================================================ */
async function verPedidos() {
  const filtroServicio =
    document.getElementById("filtroServicio")?.value || "all";

  try {
    let url = BASE + "/api/ventas";
    /* Filtrar por tipo de servicio si no es "all" */
    if (filtroServicio !== "all") {
      url += "?tipo=" + encodeURIComponent(filtroServicio);
    }

    const res   = await fetch(url);
    const ventas = await res.json();

    if (!Array.isArray(ventas) || ventas.length === 0) {
      return Swal.fire("Sin pedidos",
        "No hay pedidos registrados aún.", "info");
    }

    /* Construir tabla en un SweetAlert2 */
    let html = `
      <div class="table-responsive" style="max-height:400px;overflow-y:auto">
        <table class="table table-sm table-hover align-middle">
          <thead class="table-light">
            <tr>
              <th>#</th><th>Mesa</th><th>Tipo</th>
              <th>Detalle</th><th>Estado</th><th>Total</th>
            </tr>
          </thead><tbody>`;

    ventas.forEach(v => {
      const badge = badgeEstado(v.estado);
      html += `
        <tr>
          <td>${v.idVenta}</td>
          <td>${v.mesa || "—"}</td>
          <td>${v.tipoServicio || "—"}</td>
          <td><small>${v.detalleTexto || v.detalleTxt || "—"}</small></td>
          <td>${badge}</td>
          <td class="fw-bold">S/ ${Number(v.total).toFixed(2)}</td>
        </tr>`;
    });

    html += `</tbody></table></div>`;

    Swal.fire({
      title: "📋 Pedidos Activos",
      html,
      width: "90%",
      confirmButtonText: "Cerrar",
      confirmButtonColor: "#0d6efd"
    });

  } catch (e) {
    Swal.fire("Error", "No se pudieron cargar los pedidos.", "error");
  }
}

/* ================================================================
   HELPER: badge de estado
   ================================================================ */
function badgeEstado(estado) {
  const mapa = {
    nuevo:     "bg-warning text-dark",
    pendiente: "bg-orange text-white",
    preparando:"bg-primary",
    listo:     "bg-success",
    entregado: "bg-secondary",
    rechazado: "bg-danger"
  };
  const cls = mapa[estado] || "bg-secondary";
  return `<span class="badge ${cls}">${estado || "—"}</span>`;
}