function formatDate(value) {
    if (!value) return 'Fecha no disponible';
    const date = new Date(`${value}T00:00:00`);
    return date.toLocaleDateString('es-CO', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    });
}

function formatPrice(value) {
    const price = Number(value);
    return Number.isFinite(price) ? `$${price.toLocaleString('es-CO')}` : '$0';
}

function renderTickets(tickets) {
    const container = document.getElementById('modalTickets');
    if (!container) return;

    if (!Array.isArray(tickets) || tickets.length === 0) {
        container.innerHTML = '<p class="text-gray-400">Este evento no tiene entradas configuradas.</p>';
        return;
    }

    container.innerHTML = tickets.map((ticket) => {
        const type = ticket.ticketTypeName || 'Tipo no definido';
        const price = formatPrice(ticket.price);
        const available = Number.isFinite(ticket.availableQuantity) ? ticket.availableQuantity : 0;

        return `
            <div class="bg-gray-50 border border-gray-100 rounded-xl p-3">
                <div class="flex items-center justify-between gap-2">
                    <span class="font-bold text-gray-800">${type}</span>
                    <span class="text-main font-black">${price}</span>
                </div>
                <p class="text-xs text-gray-500 mt-1">Disponibles: ${available}</p>
            </div>
        `;
    }).join('');
}

function setBuyTicketLink(eventId) {
    const buyButton = document.getElementById('buyTicketButton');
    if (!buyButton) return;

    if (!eventId) {
        buyButton.setAttribute('href', '#');
        buyButton.setAttribute('aria-disabled', 'true');
        buyButton.classList.add('opacity-50', 'pointer-events-none');
        return;
    }

    buyButton.setAttribute('href', `/buy-ticket/${eventId}`);
    buyButton.removeAttribute('aria-disabled');
    buyButton.classList.remove('opacity-50', 'pointer-events-none');
}

function openModal(detail) {
    const modal = document.getElementById('eventModal');
    if (!modal) return;

    const eventId = detail && detail.eventId ? detail.eventId : null;

    const modalName = document.getElementById('modalName');
    const modalImg = document.getElementById('modalImg');
    const modalDate = document.getElementById('modalDate');
    const modalCat = document.getElementById('modalCat');
    const modalDesc = document.getElementById('modalDesc');

    if (modalName) modalName.innerText = detail.name || 'Evento';
    if (modalImg) modalImg.src = detail.image || '';
    if (modalDate) modalDate.innerText = formatDate(detail.date);
    if (modalCat) modalCat.innerText = detail.category || 'GENERAL';
    if (modalDesc) {
        modalDesc.innerText = detail.description || 'No hay descripcion disponible para este evento.';
    }

    renderTickets(detail.tickets);
    setBuyTicketLink(eventId);

    modal.classList.remove('hidden');
    document.body.classList.add('modal-open');
}

function closeModal() {
    const modal = document.getElementById('eventModal');
    if (!modal) return;

    modal.classList.add('hidden');
    document.body.classList.remove('modal-open');
}

function showLoadingState() {
    const ticketsContainer = document.getElementById('modalTickets');
    if (ticketsContainer) {
        ticketsContainer.innerHTML = '<p class="text-gray-400">Cargando entradas...</p>';
    }
    setBuyTicketLink(null);
}

function openEventDetail(eventId) {
    showLoadingState();

    fetch(`/api/eventos/${eventId}/detalle`)
        .then((response) => {
            if (!response.ok) {
                throw new Error('No fue posible obtener el detalle del evento.');
            }
            return response.json();
        })
        .then((data) => {
            if (!data.success || !data.event) {
                throw new Error(data.message || 'Evento no disponible.');
            }
            openModal(data.event);
        })
        .catch((error) => {
            console.error('Error loading event detail:', error);
            Swal.fire({
                toast: true,
                position: 'bottom-end',
                icon: 'error',
                title: 'No pudimos cargar la informacion del evento.',
                showConfirmButton: false,
                timer: 3500
            });
        });
}

document.addEventListener('DOMContentLoaded', () => {
    const cards = document.querySelectorAll('.js-event-card[data-event-id]');
    cards.forEach((card) => {
        card.addEventListener('click', () => {
            const eventId = card.getAttribute('data-event-id');
            if (!eventId) return;
            openEventDetail(eventId);
        });
    });
});

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeModal();
});

function toggleInterest(buttonElement) {
    const eventId = buttonElement.getAttribute('data-event-id');
    const svgElement = buttonElement.querySelector('.svg-heart');

    const headers = { 'Content-Type': 'application/json' };
    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

    if (csrfTokenMeta && csrfHeaderMeta) {
        headers[csrfHeaderMeta.getAttribute('content')] =
            csrfTokenMeta.getAttribute('content');
    }

    fetch(`/evento/${eventId}/interest`, {
        method: 'POST',
        headers: headers
    })
        .then((response) => {
            if (!response.ok && response.status !== 401) {
                throw new Error('Error de servidor');
            }
            return response.json();
        })
        .then((data) => {
            if (data.message) {
                const toast = Swal.mixin({
                    toast: true,
                    position: 'bottom-end',
                    showConfirmButton: false,
                    timer: 3000,
                    timerProgressBar: true,
                    background: '#ffffff',
                    color: '#333333',
                    didOpen: (toastElement) => {
                        toastElement.onmouseenter = Swal.stopTimer;
                        toastElement.onmouseleave = Swal.resumeTimer;
                    }
                });

                toast.fire({
                    icon: data.interested ? 'success' : 'info',
                    title: data.message
                });
            }

            if (data.interested) {
                buttonElement.classList.remove('text-white');
                buttonElement.classList.add('text-red-500');
                if (svgElement) svgElement.setAttribute('fill', 'currentColor');
            } else {
                buttonElement.classList.add('text-white');
                buttonElement.classList.remove('text-red-500');
                if (svgElement) svgElement.setAttribute('fill', 'none');
            }
        })
        .catch((error) => {
            console.error('Error:', error);
            Swal.fire({
                toast: true,
                position: 'bottom-end',
                icon: 'error',
                title: 'No pudimos procesar tu solicitud en este momento.',
                showConfirmButton: false,
                timer: 3500
            });
        });
}