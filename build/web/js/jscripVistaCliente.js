/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

const BASE = "/Holding_Web";

let productos = [];
let categorias = [];
let carrito = [];
let productoActual = null;

/* ── INIT ─────────────────────────────────────────────────────── */
document.addEventListener("DOMContentLoaded", async () => {
    await verificarSesionCliente();
    await Promise.all([cargarCategorias(), cargarProductos()]);
    actualizarCarritoUI();
});

/* ── SESIÓN ───────────────────────────────────────────────────── */
async function verificarSesionCliente() {
    try {
        const res = await fetch(BASE + "/api/sesion");
        const data = await res.json();
        if (!res.ok || !data.ok) {
            redirigir();
            return;
        }
        const el = document.getElementById("userName");
        if (el)
            el.textContent = "👋 Hola, " + (data.nombre?.split(" ")[0] || "Cliente");
    } catch {
        redirigir();
    }
}

/* ── CARGAR DATOS ─────────────────────────────────────────────── */
async function cargarCategorias() {
    try {
        const res = await fetch(BASE + "/api/categorias");
        categorias = await res.json();
        if (!Array.isArray(categorias))
            categorias = [];
        renderFiltrosCategorias();
    } catch (e) {
        console.error("Error categorías:", e);
    }
}

async function cargarProductos(filtro = "all") {
    try {
        let url = BASE + "/api/productos";
        if (filtro !== "all")
            url += "?categoria=" + filtro;
        const res = await fetch(url);
        productos = await res.json();
        if (!Array.isArray(productos))
            productos = [];
        renderProductos();
    } catch (e) {
        console.error("Error productos:", e);
}
}

/* ── RENDER ───────────────────────────────────────────────────── */
function renderFiltrosCategorias() {
    const cont = document.getElementById("categoryFilters");
    if (!cont)
        return;

    cont.innerHTML = `<button class="btn btn-sm active" data-cat="all">🍽️ Todos</button>`;
    categorias.forEach(c => {
        cont.innerHTML += `
      <button class="btn btn-sm" data-cat="${c.idCategoria}"
              style="background:${c.color}22;border:1px solid ${c.color}">
        ${c.nombre}
      </button>`;
    });

    cont.querySelectorAll(".btn").forEach(btn => {
        btn.onclick = () => {
            cont.querySelectorAll(".btn").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
            cargarProductos(btn.dataset.cat);
        };
    });
}

function renderProductos() {
    const grid = document.getElementById("productsGrid");
    const empty = document.getElementById("emptyProducts");
    if (!grid)
        return;

    if (productos.length === 0) {
        grid.innerHTML = "";
        empty?.classList.remove("d-none");
        return;
    }
    empty?.classList.add("d-none");
    grid.innerHTML = "";

    productos.forEach(p => {
        const cat = categorias.find(c => c.idCategoria == p.idCategoria);
        const sinStock = p.stock <= 0;
        const img = p.imagen || "/Holding_Web/img/noimage.png";

        grid.innerHTML += `
      <div class="product-card ${sinStock ? "opacity-75" : ""}"
           onclick="${sinStock ? "" : `abrirModal(${p.idProducto})`}">
        <button class="add-cart-btn"
                onclick="agregarDesdeCard(event, ${p.idProducto})"
                ${sinStock ? "disabled" : ""}>
          <i class="bi ${sinStock ? "bi-slash-circle" : "bi-cart-plus"}"></i>
        </button>
        <img src="${img}" alt="${p.nombre}"
             onerror="this.src='/Holding_Web/img/noimage.png'">
        <div class="card-body">
          <h6>${p.nombre}</h6>
          <span class="badge mb-2" style="background:${cat?.color || "#999"}">
            ${cat?.nombre || "General"}
          </span>
          <p class="price">S/ ${Number(p.precio).toFixed(2)}</p>
          <p class="stock-text ${sinStock ? "text-danger fw-bold" : "text-muted"}">
            ${sinStock ? "AGOTADO" : "Stock: " + p.stock}
          </p>
        </div>
      </div>`;
    });
}

/* ── MODAL PRODUCTO ───────────────────────────────────────────── */
function abrirModal(id) {
    productoActual = productos.find(p => p.idProducto == id);
    if (!productoActual)
        return;

    const cat = categorias.find(c => c.idCategoria == productoActual.idCategoria);

    document.getElementById("modalImg").src
            = productoActual.imagen || "/Holding_Web/img/noimage.png";
    document.getElementById("modalNombre").textContent
            = productoActual.nombre;
    document.getElementById("modalCategoria").textContent
            = cat?.nombre || "General";
    document.getElementById("modalDescripcion").textContent
            = productoActual.descripcion || "Sin descripción.";
    document.getElementById("modalPrecio").textContent
            = "S/ " + Number(productoActual.precio).toFixed(2);

    new bootstrap.Modal(document.getElementById("modalProducto")).show();
}

function agregarAlCarrito() {
    if (productoActual && validarStock(productoActual)) {
        insertarAlCarrito(productoActual);
        bootstrap.Modal.getInstance(document.getElementById("modalProducto"))?.hide();
    }
}

function agregarDesdeCard(e, id) {
    e.stopPropagation();
    const prod = productos.find(p => p.idProducto == id);
    if (prod && validarStock(prod))
        insertarAlCarrito(prod);
}

/* ── CARRITO ──────────────────────────────────────────────────── */
function validarStock(prod) {
    const item = carrito.find(i => i.idProducto === prod.idProducto);
    const cant = item ? item.cantidad : 0;
    if (prod.stock <= 0) {
        Swal.fire("Agotado", "Este producto no tiene stock.", "error");
        return false;
    }
    if (cant >= prod.stock) {
        Swal.fire("Límite de stock",
                `Solo hay ${prod.stock} unidades disponibles.`, "warning");
        return false;
    }
    return true;
}

function insertarAlCarrito(prod) {
    const item = carrito.find(i => i.idProducto === prod.idProducto);
    if (item)
        item.cantidad++;
    else
        carrito.push({...prod, cantidad: 1});
    actualizarCarritoUI();
    Swal.mixin({toast: true, position: "top-end",
        showConfirmButton: false, timer: 1800, timerProgressBar: true})
            .fire({icon: "success", title: `${prod.nombre} agregado 🛒`});
}

function actualizarCarritoUI() {
    const total = carrito.reduce((a, i) => a + i.cantidad, 0);
    const el = document.getElementById("cartItems");
    if (el)
        el.textContent = total;
}

/* ── FINALIZAR PEDIDO ─────────────────────────────────────────── */
async function finalizarPedido() {
    if (carrito.length === 0) {
        return Swal.fire("Carrito vacío", "Agrega productos primero.", "info");
    }

    const totalCompra = carrito.reduce((a, p) => a + p.precio * p.cantidad, 0);

    const detalleHtml = `
    <div class="text-start">
      ${carrito.map(p => `
        <div class="d-flex justify-content-between border-bottom py-1">
          <span>${p.cantidad}x ${p.nombre}</span>
          <span>S/ ${(p.precio * p.cantidad).toFixed(2)}</span>
        </div>`).join("")}
      <div class="d-flex justify-content-between fw-bold mt-2 fs-5 text-primary">
        <span>TOTAL</span>
        <span>S/ ${totalCompra.toFixed(2)}</span>
      </div>
    </div>`;

    const {value: nombreCliente} = await Swal.fire({
        title: "🛒 Confirmar Pedido",
        html: detalleHtml,
        input: "text",
        inputLabel: "Tu nombre para el pedido",
        inputPlaceholder: "Ej: Juan Pérez",
        showCancelButton: true,
        confirmButtonText: "Confirmar 🍗",
        cancelButtonText: "Seguir comprando",
        inputValidator: v => !v?.trim() ? "¡El nombre es obligatorio!" : null
    });

    if (!nombreCliente)
        return;
    await procesarVenta(nombreCliente.trim(), totalCompra);
}

async function procesarVenta(nombreCliente, total) {
    try {
        const params = new URLSearchParams();
        params.append("nombreCliente", nombreCliente);
        params.append("tipoServicio", "Mesa");
        params.append("metodoPago", "Efectivo");
        params.append("total", total.toFixed(2));

        carrito.forEach(p => {
            params.append("idProducto", p.idProducto);
            params.append("cantidad", p.cantidad);
            params.append("precioUnitario", p.precio);
        });

        const res = await fetch(BASE + "/api/ventas", {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: params.toString()
        });
        const data = await res.json();

        if (res.ok && data.ok) {
            carrito = [];
            actualizarCarritoUI();
            await cargarProductos();
            Swal.fire({
                title: "¡Pedido realizado! 🎉",
                text: `Gracias ${nombreCliente}, tu pedido está en camino a cocina.`,
                icon: "success", confirmButtonColor: "#1b5e20"
            });
        } else {
            Swal.fire("Error", data.mensaje || "No se pudo registrar el pedido.", "error");
        }
    } catch (e) {
        Swal.fire("Error de conexión", e.message, "error");
    }
}

function redirigir() {
    window.location.href = BASE + "/pages/index.html";
}