const ticketTypeModal = document.getElementById('ticket-type-modal');
const openTicketTypeModal = document.getElementById('open-ticket-type-modal');
const closeTicketTypeModal = document.getElementById('close-ticket-type-modal');

function showTicketTypeModal() {
    if (!ticketTypeModal) return;
    ticketTypeModal.classList.remove('hidden');
    ticketTypeModal.classList.add('flex');
}

function hideTicketTypeModal() {
    if (!ticketTypeModal) return;
    ticketTypeModal.classList.remove('flex');
    ticketTypeModal.classList.add('hidden');
}

if (openTicketTypeModal) {
    openTicketTypeModal.addEventListener('click', showTicketTypeModal);
}

if (closeTicketTypeModal) {
    closeTicketTypeModal.addEventListener('click', hideTicketTypeModal);
}

if (ticketTypeModal) {
    ticketTypeModal.addEventListener('click', (event) => {
        if (event.target === ticketTypeModal) {
            hideTicketTypeModal();
        }
    });

    if (ticketTypeModal.dataset.editOpen === 'true') {
        showTicketTypeModal();
    }
}