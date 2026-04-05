function formatDate(value) {
    if (!value) return 'Fecha no disponible';
    const date = new Date(`${value}T00:00:00`);
    return date.toLocaleDateString('es-CO', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    });
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
        const price = Number.isFinite(ticket.price) ? ticket.price : 0;
        const available = Number.isFinite(ticket.availableQuantity) ? ticket.availableQuantity : 0;

        return `
            <div class="bg-gray-50 border border-gray-100 rounded-xl p-3">
                <div class="flex items-center justify-between gap-2">
                    <span class="font-bold text-gray-800">${type}</span>
                    <span class="text-main font-black">$${price.toLocaleString('es-CO')}</span>
                </div>
                <p class="text-xs text-gray-500 mt-1">Disponibles: ${available}</p>
            </div>
        `;
    }).join('');
}

function openModal(detail) {
    document.getElementById('modalName').innerText = detail.name || 'Evento';
    document.getElementById('modalImg').src = detail.image || '';
    document.getElementById('modalDate').innerText = formatDate(detail.date);
    document.getElementById('modalCat').innerText = detail.category || 'GENERAL';
    document.getElementById('modalDesc').innerText = detail.description ||
        'No hay descripción disponible para este evento.';
    renderTickets(detail.tickets);

    const modal = document.getElementById('eventModal');
    modal.classList.remove('hidden');
    document.body.classList.add('modal-open');
}

function showLoadingState() {
    const ticketsContainer = document.getElementById('modalTickets');
    if (ticketsContainer) {
        ticketsContainer.innerHTML = '<p class="text-gray-400">Cargando entradas...</p>';
    }
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
                title: 'No pudimos cargar la información del evento.',
                showConfirmButton: false,
                timer: 3500
            });
        });
}

function closeModal() {
    const modal = document.getElementById('eventModal');
    modal.classList.add('hidden');
    document.body.classList.remove('modal-open');
}


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
    .then(response => {
        if (!response.ok && response.status !== 401) {
            throw new Error('Error de servidor');
        }
        return response.json();
    })
    .then(data => {
        if (data.message) {
            const Toast = Swal.mixin({
                toast: true,
                position: 'bottom-end', 
                showConfirmButton: false,
                timer: 3000,
                timerProgressBar: true,
                background: '#ffffff',
                color: '#333333',
                didOpen: (toast) => {
                    toast.onmouseenter = Swal.stopTimer;
                    toast.onmouseleave = Swal.resumeTimer;
                }
            });

            Toast.fire({
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
    .catch(error => {
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