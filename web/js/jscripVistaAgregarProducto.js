/* ================================================================
   jscripVistaAgregarProducto.js  –  Panel Administrador
   GET    /api/productos
   POST   /api/productos          → crear
   POST   /api/productos?id=      → actualizar
   DELETE /api/productos?id=
   GET    /api/categorias
   POST   /api/categorias
   PUT    /api/categorias?id=
   DELETE /api/categorias?id=
   GET    /api/ventas
   GET    /api/ventas?reporte=ingresos
   GET    /api/sesion
   ================================================================ */

const BASE = "/Holding_Web";
const IMG_PH = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='90'%3E%3Crect width='120' height='90' fill='%23e9ecef'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' font-size='11' fill='%23adb5bd'%3ESin imagen%3C/text%3E%3C/svg%3E";

let productos  = [];
let categorias = [];

/* ── INIT ─────────────────────────────────────────────────────── */
document.addEventListener("DOMContentLoaded", async () => {
  await verificarSesionAdmin();
  await Promise.all([cargarCategorias(), cargarProductos()]);
  await cargarVentas();
  await cargarEstadisticas();

  document.getElementById("searchProduct") ?.addEventListener("input",  renderProductos);
  document.getElementById("filterCategory")?.addEventListener("change", renderProductos);
  document.getElementById("sortProducts")  ?.addEventListener("change", renderProductos);
  document.getElementById("prodImagen")    ?.addEventListener("input",  previewImagen);
  document.getElementById("prodImagenFile")?.addEventListener("change", previewImagenFile);

  // Sidebar SPA
  document.querySelectorAll(".sidebar .nav-link").forEach(link => {
    link.addEventListener("click", e => {
      e.preventDefault();
      document.querySelectorAll(".content-section").forEach(s => s.classList.add("d-none"));
      document.querySelectorAll(".sidebar .nav-link").forEach(l => l.classList.remove("active"));
      document.getElementById("section-" + link.dataset.section)?.classList.remove("d-none");
      link.classList.add("active");
      if (link.dataset.section === "ventas")       cargarVentas();
      if (link.dataset.section === "estadisticas") cargarEstadisticas();
    });
  });
});

/* ── SESIÓN ───────────────────────────────────────────────────── */
async function verificarSesionAdmin() {
  try {
    const res  = await fetch(BASE + "/api/sesion");
    const data = await res.json();
    if (!res.ok || !data.ok || data.rol?.toLowerCase() !== "admin") {
      window.location.href = BASE + "/pages/index.html";
      return;
    }
    const el = document.getElementById("userName");
    if (el) el.textContent = data.nombre || "Admin";
  } catch {
    window.location.href = BASE + "/pages/index.html";
  }
}

/* ================================================================
   CATEGORÍAS
   ================================================================ */
async function cargarCategorias() {
  try {
    const res  = await fetch(BASE + "/api/categorias");
    categorias = await res.json();
    if (!Array.isArray(categorias)) categorias = [];
    renderCategorias();
    poblarSelectCategorias();
  } catch (e) { console.error("Error categorías:", e); }
}

function renderCategorias() {
  const cont  = document.getElementById("categoriesList");
  const empty = document.getElementById("emptyCategories");
  if (!cont) return;
  cont.innerHTML = "";

  if (categorias.length === 0) { empty?.classList.remove("d-none"); return; }
  empty?.classList.add("d-none");

  categorias.forEach(c => {
    cont.innerHTML += `
      <div class="col-md-4 col-lg-3">
        <div class="card h-100 border-0 shadow-sm"
             style="border-left:5px solid ${c.color} !important;">
          <div class="card-body d-flex justify-content-between align-items-center p-3">
            <span class="fw-medium">${c.nombre}</span>
            <div class="btn-group">
              <button onclick="editarCategoria(${c.idCategoria})"
                      class="btn btn-sm btn-light">
                <i class="bi bi-pencil text-primary"></i>
              </button>
              <button onclick="eliminarCategoria(${c.idCategoria})"
                      class="btn btn-sm btn-light">
                <i class="bi bi-trash text-danger"></i>
              </button>
            </div>
          </div>
        </div>
      </div>`;
  });
}

function poblarSelectCategorias() {
  const selProd = document.getElementById("prodCategoria");
  const selFil  = document.getElementById("filterCategory");
  if (selProd) {
    selProd.innerHTML = `<option value="">Seleccionar...</option>`;
    categorias.forEach(c =>
      selProd.innerHTML += `<option value="${c.idCategoria}">${c.nombre}</option>`);
  }
  if (selFil) {
    selFil.innerHTML = `<option value="">Todas las categorías</option>`;
    categorias.forEach(c =>
      selFil.innerHTML += `<option value="${c.idCategoria}">${c.nombre}</option>`);
  }
}

async function guardarCategoria() {
  const id     = document.getElementById("categoriaId").value.trim();
  const nombre = document.getElementById("catNombre").value.trim();
  const color  = document.getElementById("catColor").value;

  if (!nombre) return Swal.fire("Error", "El nombre es obligatorio", "error");

  try {
    const params = new URLSearchParams({ nombre, color });
    let url = BASE + "/api/categorias";
    let method = "POST";

    if (id) {
      params.append("id", id);
      method = "PUT";
      // El servlet PUT espera ?id= en query string
      url += "?id=" + encodeURIComponent(id);
    }

    const res  = await fetch(url, {
      method,
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ nombre, color }).toString()
    });
    const data = await res.json();

    if (data.ok) {
      bootstrap.Modal.getInstance(document.getElementById("modalCategoria"))?.hide();
      Swal.fire({ icon: "success", title: "Categoría guardada", timer: 1500, showConfirmButton: false });
      await cargarCategorias();
      await cargarEstadisticas();
    } else {
      Swal.fire("Error", data.mensaje || "No se pudo guardar", "error");
    }
  } catch (e) { Swal.fire("Error de red", e.message, "error"); }
}

function editarCategoria(id) {
  const c = categorias.find(x => x.idCategoria == id);
  if (!c) return;
  document.getElementById("categoriaId").value              = c.idCategoria;
  document.getElementById("catNombre").value                = c.nombre;
  document.getElementById("catColor").value                 = c.color;
  document.getElementById("modalCategoriaTitle").textContent = "Editar Categoría";
  new bootstrap.Modal(document.getElementById("modalCategoria")).show();
}

async function eliminarCategoria(id) {
  const r = await Swal.fire({
    icon: "warning", title: "¿Eliminar categoría?",
    text: "Los productos vinculados quedarán sin categoría.",
    showCancelButton: true, cancelButtonText: "Cancelar",
    confirmButtonColor: "#d33", confirmButtonText: "Sí, eliminar"
  });
  if (!r.isConfirmed) return;

  try {
    const res  = await fetch(`${BASE}/api/categorias?id=${id}`, { method: "DELETE" });
    const data = await res.json();
    if (data.ok) {
      Swal.fire({ icon: "success", title: "Eliminada", timer: 1200, showConfirmButton: false });
      await cargarCategorias();
      await cargarProductos();
      await cargarEstadisticas();
    } else {
      Swal.fire("Error", data.mensaje || "No se pudo eliminar", "error");
    }
  } catch (e) { Swal.fire("Error de red", e.message, "error"); }
}

/* ================================================================
   PRODUCTOS
   ================================================================ */
async function cargarProductos() {
  try {
    const res = await fetch(BASE + "/api/productos");
    if (!res.ok) throw new Error("HTTP " + res.status);
    productos = await res.json();
    if (!Array.isArray(productos)) productos = [];
    renderProductos();
  } catch (e) { console.error("Error productos:", e); }
}

function renderProductos() {
  const cont  = document.getElementById("productsList");
  const empty = document.getElementById("emptyProducts");
  if (!cont) return;

  let lista = [...productos];
  const q = document.getElementById("searchProduct")?.value.toLowerCase() || "";
  if (q) lista = lista.filter(p => p.nombre.toLowerCase().includes(q));
  const cat = document.getElementById("filterCategory")?.value || "";
  if (cat) lista = lista.filter(p => String(p.idCategoria) === String(cat));
  const ord = document.getElementById("sortProducts")?.value || "nombre";
  if (ord === "precio-asc")  lista.sort((a, b) => a.precio - b.precio);
  if (ord === "precio-desc") lista.sort((a, b) => b.precio - a.precio);
  if (ord === "nombre")      lista.sort((a, b) => a.nombre.localeCompare(b.nombre));

  cont.innerHTML = "";
  if (lista.length === 0) { empty?.classList.remove("d-none"); return; }
  empty?.classList.add("d-none");

  lista.forEach(p => {
    const imgSrc    = (p.imagen && p.imagen.trim()) ? p.imagen.trim() : IMG_PH;
    const stockBajo = p.stock <= 5;
    const cat       = categorias.find(c => c.idCategoria == p.idCategoria);

    cont.innerHTML += `
      <div class="product-card shadow-sm">
        <img src="${imgSrc}" class="product-img" alt="${p.nombre}"
             onerror="this.onerror=null;this.src='${IMG_PH}'">
        <div class="product-info">
          <h6>${p.nombre}</h6>
          <span class="badge" style="background:${cat?.color || '#999'}">
            ${cat?.nombre || "Sin categoría"}
          </span>
          <p class="price">S/ ${Number(p.precio).toFixed(2)}</p>
          <p class="stock ${stockBajo ? "text-danger fw-bold" : ""}">
            Stock: ${p.stock}${stockBajo ? " ⚠️" : ""}
          </p>
        </div>
        <div class="product-actions">
          <button class="btn btn-warning btn-sm"
                  onclick="editarProducto(${p.idProducto})">
            <i class="bi bi-pencil"></i>
          </button>
          <button class="btn btn-danger btn-sm"
                  onclick="eliminarProducto(${p.idProducto})">
            <i class="bi bi-trash"></i>
          </button>
        </div>
      </div>`;
  });
}

function limpiarFormProducto() {
  ["productId","prodNombre","prodDescripcion","prodPrecio","prodImagen"].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = "";
  });
  const stockEl = document.getElementById("prodStock");
  if (stockEl) stockEl.value = "0";
  const catEl = document.getElementById("prodCategoria");
  if (catEl) catEl.value = "";
  const fileEl = document.getElementById("prodImagenFile");
  if (fileEl) fileEl.value = "";
  const preview = document.getElementById("imagePreview");
  if (preview) preview.classList.add("d-none");
}

function nuevoProducto() {
  limpiarFormProducto();
  poblarSelectCategorias();
  document.getElementById("modalProductoTitle").textContent = "Nuevo Producto";
  new bootstrap.Modal(document.getElementById("modalProducto")).show();
}

function editarProducto(id) {
  const p = productos.find(x => x.idProducto == id);
  if (!p) return;
  limpiarFormProducto();
  poblarSelectCategorias();

  document.getElementById("productId").value       = p.idProducto;
  document.getElementById("prodNombre").value      = p.nombre;
  document.getElementById("prodDescripcion").value = p.descripcion || "";
  document.getElementById("prodPrecio").value      = p.precio;
  document.getElementById("prodStock").value       = p.stock ?? 0;
  document.getElementById("prodCategoria").value   = p.idCategoria;

  if (p.imagen && p.imagen.trim()) {
    document.getElementById("prodImagen").value = p.imagen;
    document.getElementById("previewImg").src   = p.imagen;
    document.getElementById("imagePreview").classList.remove("d-none");
  }
  document.getElementById("modalProductoTitle").textContent = "Editar Producto";
  new bootstrap.Modal(document.getElementById("modalProducto")).show();
}

async function guardarProducto() {
  const id          = document.getElementById("productId").value.trim();
  const nombre      = document.getElementById("prodNombre").value.trim();
  const idCategoria = document.getElementById("prodCategoria").value;
  const precio      = document.getElementById("prodPrecio").value.trim();
  const stock       = document.getElementById("prodStock").value.trim();
  const descripcion = document.getElementById("prodDescripcion").value.trim();
  const imagenURL   = document.getElementById("prodImagen").value.trim();
  const fileInput   = document.getElementById("prodImagenFile");

  if (!nombre)      return Swal.fire("Falta nombre",    "Ingresa el nombre del producto", "warning");
  if (!idCategoria) return Swal.fire("Falta categoría", "Selecciona una categoría",       "warning");
  if (!precio || isNaN(precio) || Number(precio) <= 0)
                    return Swal.fire("Precio inválido",  "Ingresa un precio mayor a 0",   "warning");

  const fd = new FormData();
  fd.append("nombre",      nombre);
  fd.append("idCategoria", idCategoria);
  fd.append("precio",      precio);
  fd.append("stock",       stock || "0");
  fd.append("descripcion", descripcion);
  fd.append("imagen",      imagenURL);
  if (fileInput?.files.length > 0) fd.append("imagenFile", fileInput.files[0]);

  // Si hay id, lo mandamos en la query string (el servlet lo detecta)
  const url = id
    ? `${BASE}/api/productos?id=${encodeURIComponent(id)}`
    : `${BASE}/api/productos`;

  try {
    const res  = await fetch(url, { method: "POST", body: fd });
    let data = {};
    try { data = await res.json(); } catch (_) {}

    if (data.ok) {
      bootstrap.Modal.getInstance(document.getElementById("modalProducto"))?.hide();
      Swal.fire({ icon: "success",
        title: id ? "Producto actualizado ✓" : "Producto creado ✓",
        timer: 1600, showConfirmButton: false });
      await cargarProductos();
      await cargarEstadisticas();
    } else {
      Swal.fire("Error al guardar", data.mensaje || `Error HTTP ${res.status}`, "error");
    }
  } catch (e) {
    Swal.fire("Error de conexión", e.message, "error");
  }
}

async function eliminarProducto(id) {
  const r = await Swal.fire({
    icon: "warning", title: "¿Eliminar producto?",
    text: "Esta acción no se puede deshacer.",
    showCancelButton: true, cancelButtonText: "Cancelar",
    confirmButtonText: "Sí, eliminar", confirmButtonColor: "#d33"
  });
  if (!r.isConfirmed) return;

  try {
    const res  = await fetch(`${BASE}/api/productos?id=${id}`, { method: "DELETE" });
    let data = {};
    try { data = await res.json(); } catch (_) {}

    if (data.ok) {
      Swal.fire({ icon: "success", title: "Producto eliminado ✓", timer: 1200, showConfirmButton: false });
      await cargarProductos();
      await cargarEstadisticas();
    } else {
      Swal.fire("Error al eliminar", data.mensaje || `Error HTTP ${res.status}`, "error");
    }
  } catch (e) { Swal.fire("Error de conexión", e.message, "error"); }
}

/* ================================================================
   VENTAS
   ================================================================ */
async function cargarVentas() {
  try {
    const res    = await fetch(BASE + "/api/ventas");
    const ventas = await res.json();
    const tbody  = document.getElementById("ventasList");
    if (!tbody) return;
    tbody.innerHTML = "";

    if (!Array.isArray(ventas) || ventas.length === 0) {
      tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted py-3">Sin ventas registradas</td></tr>`;
      return;
    }

    ventas.forEach(v => {
      const estado = v.estado || "—";
      tbody.innerHTML += `
        <tr>
          <td><small>${v.fechaVenta || "—"}</small></td>
          <td><strong>${v.nombreCliente || "—"}</strong></td>
          <td><span class="text-muted">${v.detalleTexto || "—"}</span></td>
          <td class="fw-bold text-success">S/ ${Number(v.total).toFixed(2)}</td>
        </tr>`;
    });
  } catch (e) { console.error("Error ventas:", e); }
}

/* ================================================================
   ESTADÍSTICAS
   ================================================================ */
async function cargarEstadisticas() {
  try {
    const [resProds, resCats, resIngresos] = await Promise.all([
      fetch(BASE + "/api/productos"),
      fetch(BASE + "/api/categorias"),
      fetch(BASE + "/api/ventas?reporte=ingresos")
    ]);
    const prods    = await resProds.json();
    const cats     = await resCats.json();
    const ingresos = await resIngresos.json();

    const el = id => document.getElementById(id);
    if (el("statProductos"))      el("statProductos").textContent  = Array.isArray(prods) ? prods.length : 0;
    if (el("statCategorias"))     el("statCategorias").textContent = Array.isArray(cats)  ? cats.length  : 0;
    if (el("statIngresosVentas")) el("statIngresosVentas").textContent = "S/ " + Number(ingresos?.total || 0).toFixed(2);
    if (el("statValor")) {
      const valor = Array.isArray(prods)
        ? prods.reduce((s, p) => s + (p.precio || 0) * (p.stock || 0), 0)
        : 0;
      el("statValor").textContent = "S/ " + valor.toFixed(2);
    }
  } catch (e) { console.error("Error estadísticas:", e); }
}

/* ── PREVIEW IMAGEN ───────────────────────────────────────────── */
function previewImagen() {
  const url  = document.getElementById("prodImagen").value.trim();
  const prev = document.getElementById("imagePreview");
  if (!url) { prev?.classList.add("d-none"); return; }
  document.getElementById("previewImg").src = url;
  prev?.classList.remove("d-none");
}

function previewImagenFile() {
  const file = document.getElementById("prodImagenFile")?.files[0];
  if (!file) return;
  document.getElementById("prodImagen").value = "";
  const reader = new FileReader();
  reader.onload = e => {
    document.getElementById("previewImg").src = e.target.result;
    document.getElementById("imagePreview").classList.remove("d-none");
  };
  reader.readAsDataURL(file);
}

/* ── CERRAR SESIÓN ────────────────────────────────────────────── */
function cerrarSesion() {
  fetch(BASE + "/api/logout").finally(() => {
    window.location.href = BASE + "/pages/index.html";
  });
}

function resetearSistema() {
  Swal.fire({
    icon: "info", title: "No disponible",
    text: "Administra los datos directamente desde la base de datos."
  });
}