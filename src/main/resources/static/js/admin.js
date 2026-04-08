const adminModal = document.getElementById('admin-modal');
const openAdminModal = document.getElementById('open-admin-modal');
const closeAdminModal = document.getElementById('close-admin-modal');

function showAdminModal() {
    if (!adminModal) return;
    adminModal.classList.remove('hidden');
    adminModal.classList.add('flex');
}

function hideAdminModal() {
    if (!adminModal) return;
    adminModal.classList.remove('flex');
    adminModal.classList.add('hidden');
}

if (openAdminModal) {
    openAdminModal.addEventListener('click', showAdminModal);
}

if (closeAdminModal) {
    closeAdminModal.addEventListener('click', hideAdminModal);
}

if (adminModal) {
    adminModal.addEventListener('click', (event) => {
        if (event.target === adminModal) {
            hideAdminModal();
        }
    });

    if (adminModal.dataset.editOpen === 'true') {
        showAdminModal();
    }
}

const fechaInput = document.getElementById('fechaEvento');
if(fechaInput) fechaInput.min = new Date().toISOString().split('T')[0];

function setupCounter(inputId, counterId, limit) {
    const input = document.getElementById(inputId);
    const counter = document.getElementById(counterId);
    if(!input || !counter) return;

    input.addEventListener('input', () => {
        const length = input.value.length;
        counter.textContent = `${length}/${limit}`;
        if (length >= limit) {
            counter.classList.replace('text-gray-400', 'text-error');
            input.classList.add('ring-2', 'ring-error');
        } else {
            counter.classList.replace('text-error', 'text-gray-400');
            input.classList.remove('ring-error');
        }
    });
}
setupCounter('event-name', 'char-count', 100);
setupCounter('event-desc', 'desc-char-count', 500);

const ticketContainer = document.getElementById('tickets-container');
const ticketTemplate = document.getElementById('ticket-row-template');
const addTicketButton = document.getElementById('add-ticket-row');
const ticketHelp = document.getElementById('tickets-empty-help');
const eventForm = document.querySelector('form[th\\:object="${evento}"]') || document.querySelector('form');

function updateTicketEmptyMessage() {
    if (!ticketHelp || !ticketContainer) return;
    ticketHelp.classList.toggle('hidden', ticketContainer.querySelectorAll('.ticket-row').length > 0);
}

function reindexTicketRows() {
    if (!ticketContainer) return;
    const rows = ticketContainer.querySelectorAll('.ticket-row');
    rows.forEach((row, index) => {
        const typeInput = row.querySelector('.js-ticket-type');
        const priceInput = row.querySelector('.js-ticket-price');
        const quantityInput = row.querySelector('.js-ticket-quantity');

        if (typeInput) typeInput.name = `tickets[${index}].ticketTypeId`;
        if (priceInput) priceInput.name = `tickets[${index}].price`;
        if (quantityInput) quantityInput.name = `tickets[${index}].availableQuantity`;
    });
    updateTicketEmptyMessage();
}

function updateDisabledTicketOptions() {
    if (!ticketContainer) return;

    const allSelects = Array.from(ticketContainer.querySelectorAll('.js-ticket-type'));
    const selectedValues = allSelects.map(select => select.value).filter(value => value !== "");

    allSelects.forEach(select => {
        Array.from(select.options).forEach(option => {
            if (!option.value) return;
            if (selectedValues.includes(option.value) && select.value !== option.value) {
                option.disabled = true;
            } else {
                option.disabled = false;
            }
        });
    });
}

function addTicketRow() {
    if (!ticketContainer || !ticketTemplate) return;
    const row = ticketTemplate.content.firstElementChild.cloneNode(true);
    ticketContainer.appendChild(row);
    reindexTicketRows();
    updateDisabledTicketOptions();
}

function isTicketRowEmpty(row) {
    const type = row.querySelector('.js-ticket-type')?.value?.trim();
    const price = row.querySelector('.js-ticket-price')?.value?.trim();
    const quantity = row.querySelector('.js-ticket-quantity')?.value?.trim();
    return !type && !price && !quantity;
}

if (addTicketButton) {
    addTicketButton.addEventListener('click', addTicketRow);
}

if (ticketContainer) {
    ticketContainer.addEventListener('click', (event) => {
        const removeButton = event.target.closest('.remove-ticket-row');
        if (!removeButton) return;
        removeButton.closest('.ticket-row')?.remove();
        reindexTicketRows();
        updateDisabledTicketOptions();
    });

    ticketContainer.addEventListener('change', (event) => {
        if (event.target.classList.contains('js-ticket-type')) {
            updateDisabledTicketOptions();
        }
    });
}

if (eventForm) {
    eventForm.addEventListener('submit', () => {
        if (!ticketContainer) return;
        ticketContainer.querySelectorAll('.ticket-row').forEach((row) => {
            if (isTicketRowEmpty(row)) {
                row.remove();
            }
        });
        reindexTicketRows();
    });
}

reindexTicketRows();