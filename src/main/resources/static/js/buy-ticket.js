const pageMain = document.querySelector('main[data-event-id]');
const eventId = Number(pageMain ? pageMain.dataset.eventId : 0);

const steps = Array.from(document.querySelectorAll('[data-step]'));
const indicators = Array.from(document.querySelectorAll('[data-step-indicator]'));
const progress = document.getElementById('step-progress');

const prevStepButton = document.getElementById('prev-step');
const nextStepButton = document.getElementById('next-step');
const confirmStepButton = document.getElementById('confirm-step');
const errorBox = document.getElementById('step-error');

const ticketTypeOptions = Array.from(document.querySelectorAll('input[name="ticketType"]'));
const personalForm = document.getElementById('personal-form');
const bankForm = document.getElementById('bank-form');

const cancelButton = document.getElementById('cancel-btn');

let currentStep = 1;
let isSubmitting = false;
let lastPaymentStatus = null;
let lastPurchaseId = null;
let failCount = 0;
let activeStompClient = null;

function showError(message) {
    errorBox.classList.remove('bg-success/10', 'border-success/30', 'text-success');
    errorBox.classList.add('bg-error/5', 'border-error/20', 'text-error');
    errorBox.textContent = message;
    errorBox.classList.remove('hidden');
}

function showSuccess(htmlContent) {
    errorBox.classList.remove('bg-error/5', 'border-error/20', 'text-error');
    errorBox.classList.add('bg-success/10', 'border-success/30', 'text-success');
    errorBox.innerHTML = htmlContent;
    errorBox.classList.remove('hidden');
}

function hideError() {
    errorBox.classList.add('hidden');
    errorBox.textContent = '';
}

function getTicketPrice(input) {
    return Number(input ? input.dataset.ticketPrice : 0);
}

function getTicketAvailable(input) {
    return Number(input ? input.dataset.ticketAvailable : 0);
}

function getTicketQuantityInput(option) {
    const ticketOption = option.closest('[data-ticket-option]');
    return ticketOption ? ticketOption.querySelector('[data-ticket-quantity]') : null;
}

function getSelectedItems() {
    return ticketTypeOptions
        .filter((option) => option.checked)
        .map((option) => {
            const quantityInput = getTicketQuantityInput(option);
            const quantity = Number(quantityInput ? quantityInput.value : 0);

            return {
                ticketId: Number(option.value),
                ticketName: option.dataset.ticketName || 'Boleta',
                unitPrice: getTicketPrice(option),
                available: getTicketAvailable(option),
                quantity,
                subtotal: Number(quantity) * getTicketPrice(option)
            };
        });
}

function setOptionSubtotal(option) {
    const ticketOption = option.closest('[data-ticket-option]');
    if (!ticketOption) {
        return;
    }

    const quantityInput = getTicketQuantityInput(option);
    const subtotalNode = ticketOption.querySelector('[data-ticket-subtotal]');
    if (!quantityInput || !subtotalNode) {
        return;
    }

    const quantity = Number(quantityInput.value || 0);
    const subtotal = option.checked ? quantity * getTicketPrice(option) : 0;
    subtotalNode.textContent = `Subtotal: $${subtotal.toLocaleString('es-CO')}`;
}

function syncTicketOption(option) {
    const quantityInput = getTicketQuantityInput(option);
    if (!quantityInput) {
        return;
    }

    const available = getTicketAvailable(option);
    quantityInput.max = String(available);

    if (available <= 0) {
        option.checked = false;
        option.disabled = true;
        quantityInput.disabled = true;
        quantityInput.value = '0';
        setOptionSubtotal(option);
        return;
    }

    if (option.checked) {
        quantityInput.disabled = false;
        if (!quantityInput.value || Number(quantityInput.value) < 1) {
            quantityInput.value = '1';
        }
    } else {
        quantityInput.disabled = true;
        quantityInput.value = '1';
    }

    if (Number(quantityInput.value) > available) {
        quantityInput.value = String(available);
    }

    setOptionSubtotal(option);
}

function updateStepOneSummary() {
    const selectedItems = getSelectedItems();
    const selectedTypes = selectedItems.length;
    const totalQuantity = selectedItems.reduce((acc, item) => acc + item.quantity, 0);
    const total = selectedItems.reduce((acc, item) => acc + item.subtotal, 0);

    document.getElementById('step1SelectedTypes').textContent = String(selectedTypes);
    document.getElementById('step1SelectedQuantity').textContent = String(totalQuantity);
    document.getElementById('step1SelectedTotal').textContent = `$${total.toLocaleString('es-CO')}`;
}

function updateStepUI() {
    steps.forEach((section) => {
        const sectionStep = Number(section.dataset.step);
        section.classList.toggle('hidden', sectionStep !== currentStep);
    });

    indicators.forEach((indicator) => {
        const indicatorStep = Number(indicator.dataset.stepIndicator);
        if (indicatorStep === currentStep) {
            indicator.classList.remove('text-gray-400', 'font-bold');
            indicator.classList.add('text-gray-900', 'font-black');
        } else {
            indicator.classList.remove('text-gray-900', 'font-black');
            indicator.classList.add('text-gray-400', 'font-bold');
        }
    });

    progress.style.width = `${currentStep * 20}%`;

    prevStepButton.classList.toggle('hidden', currentStep === 1 || currentStep >= 5);
    nextStepButton.classList.toggle('hidden', currentStep >= 4);
    confirmStepButton.classList.toggle('hidden', currentStep !== 4);

    const inProcessing = currentStep === 5;
    document.getElementById('step-description')?.classList.toggle('hidden', inProcessing);
    document.getElementById('step-nav')?.classList.toggle('hidden', inProcessing);

    hideError();

    if (currentStep === 4) {
        fillSummary();
    }
}

function validateStep(step) {
    if (step === 1) {
        const selectedItems = getSelectedItems();
        if (!selectedItems.length) {
            showError('Debes seleccionar al menos un tipo de boleta para continuar.');
            return false;
        }

        for (const item of selectedItems) {
            if (!Number.isInteger(item.quantity) || item.quantity <= 0) {
                showError(`La cantidad para ${item.ticketName} debe ser mayor a cero.`);
                return false;
            }

            if (item.quantity > item.available) {
                showError(`La cantidad para ${item.ticketName} supera la disponibilidad.`);
                return false;
            }
        }

        return true;
    }

    if (step === 2) {
        if (!personalForm.reportValidity()) {
            showError('Completa todos los datos personales obligatorios.');
            return false;
        }
        return true;
    }

    if (step === 3) {
        if (!bankForm.reportValidity()) {
            showError('Completa los datos bancarios requeridos.');
            return false;
        }
        const cardDigits = document.getElementById('cardNumber').value.replace(/\D/g, '');
        const cvv = document.getElementById('cvv').value.replace(/\D/g, '');

        const cardType = (document.querySelector('input[name="cardType"]:checked') || {}).value || null;
        if (!cardType) {
            showError('Debes seleccionar el tipo de tarjeta (Visa, Mastercard o Nu).');
            return false;
        }

        if (cardDigits.length < 13 || cardDigits.length > 19) {
            showError('El numero de tarjeta debe tener entre 13 y 19 digitos.');
            return false;
        }

        if (cvv.length !== 3) {
            showError('El CVV debe tener 3 digitos.');
            return false;
        }

        return true;
    }

    return true;
}

function maskCardNumber(rawCard) {
    const digits = (rawCard || '').replace(/\D/g, '');
    if (digits.length <= 4) {
        return `**** ${digits || '0000'}`;
    }
    const lastFour = digits.slice(-4);
    return `**** **** **** ${lastFour}`;
}

function fillSummary() {
    const selectedItems = getSelectedItems();
    const summaryItemsContainer = document.getElementById('summaryTicketItems');
    const totalQuantity = selectedItems.reduce((acc, item) => acc + item.quantity, 0);
    const total = selectedItems.reduce((acc, item) => acc + item.subtotal, 0);

    summaryItemsContainer.innerHTML = '';
    if (!selectedItems.length) {
        summaryItemsContainer.textContent = '-';
    } else {
        selectedItems.forEach((item) => {
            const itemLine = document.createElement('p');
            itemLine.textContent = `${item.ticketName} x${item.quantity} - $${item.subtotal.toLocaleString('es-CO')}`;
            summaryItemsContainer.appendChild(itemLine);
        });
    }

    document.getElementById('summaryQuantity').textContent = String(totalQuantity);
    document.getElementById('summaryTotal').textContent = `$${total.toLocaleString('es-CO')}`;

    document.getElementById('summaryName').textContent = document.getElementById('fullName').value || '-';
    document.getElementById('summaryEmail').textContent = document.getElementById('email').value || '-';
    document.getElementById('summaryDocument').textContent = document.getElementById('document').value || '-';
    const typeInput = document.querySelector('input[name="cardType"]:checked');
    const cardType = typeInput ? typeInput.value : null;
    document.getElementById('summaryCard').textContent = cardType ? `${cardType.toUpperCase()} ${maskCardNumber(document.getElementById('cardNumber').value)}` : maskCardNumber(document.getElementById('cardNumber').value);
    document.getElementById('summaryCvv').textContent = '***';
}

async function submitCheckout() {
    const selectedItems = getSelectedItems();
    if (!selectedItems.length) {
        throw new Error('Debes seleccionar al menos un tipo de boleta.');
    }

    const payload = {
        eventId,
        items: selectedItems.map((item) => ({
            ticketId: item.ticketId,
            quantity: item.quantity
        })),
        buyer: {
            fullName: document.getElementById('fullName').value,
            document: document.getElementById('document').value,
            email: document.getElementById('email').value,
            phone: document.getElementById('phone').value
        },
        payment: {
            cardHolder: document.getElementById('cardHolder').value,
            cardNumber: document.getElementById('cardNumber').value,
            expiry: document.getElementById('expiry').value,
            cvv: document.getElementById('cvv').value,
            tipo_tarjeta: ((document.querySelector('input[name="cardType"]:checked') || {}).value || null)?.toLowerCase()
        }
    };

    const response = await fetch('/api/purchases/checkout', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    });

    const result = await response.json();
    if (!response.ok || !result.success) {
        const err = new Error(result.message || 'No fue posible registrar la compra.');
        err.paymentHandled = result.paymentHandled === true;
        throw err;
    }

    return result.purchase;
}

nextStepButton.addEventListener('click', () => {
    if (!validateStep(currentStep)) return;
    if (currentStep < 4) {
        currentStep += 1;
        updateStepUI();
    }
});

prevStepButton.addEventListener('click', () => {
    if (currentStep > 1) {
        currentStep -= 1;
        updateStepUI();
    }
});

confirmStepButton.addEventListener('click', async () => {
    if (isSubmitting) return;
    if (!validateStep(3)) return;
    if (!eventId || eventId <= 0) {
        showError('No se pudo identificar el evento para registrar la compra.');
        return;
    }

    isSubmitting = true;
    confirmStepButton.disabled = true;
    confirmStepButton.setAttribute('aria-busy', 'true');
    confirmStepButton.textContent = 'Conectando...';

    try {
        // 1. Desconectar cliente STOMP previo si existe
        if (activeStompClient && activeStompClient.connected) {
            try { activeStompClient.disconnect(); } catch (e) {}
            activeStompClient = null;
        }

        // 2. Conectar STOMP y esperar suscripción ANTES de llamar al checkout
        await new Promise(function (resolve, reject) {
            const sessionId = document.querySelector('meta[name="session-id"]')
                ?.getAttribute('content');
            if (!sessionId) { resolve(); return; }

            const socket = new SockJS('/ws');
            const stomp = Stomp.over(socket);
            stomp.debug = null;
            stomp.connect({}, function () {
                activeStompClient = stomp;
                stomp.subscribe('/topic/payment-progress/' + sessionId, function (frame) {
                    try { handleMessage(JSON.parse(frame.body)); } catch (e) {}
                });
                resolve(); // ← resuelve SOLO después de suscribirse
            }, function () {
                resolve(); // si falla STOMP, continuar igual
            });
        });

        // 2. STOMP listo — ahora sí llamar al checkout
        confirmStepButton.textContent = 'Procesando...';
        currentStep = 5;
        updateStepUI();
        startDots();
        const purchase = await submitCheckout();
        lastPurchaseId = purchase.purchaseId;
        lastPaymentStatus = 'aprobado';

        confirmStepButton.classList.add('hidden');
        prevStepButton.classList.add('hidden');
        cancelButton.textContent = 'Volver a la cartelera';
        cancelButton.classList.remove('btn-ghost');
        cancelButton.classList.add('btn-neutral');

        // STOMP sigue abierto — espera el mensaje bonito de la IA

    } catch (error) {
        isSubmitting = false;
        confirmStepButton.disabled = false;
        confirmStepButton.removeAttribute('aria-busy');
        confirmStepButton.textContent = 'Confirmar compra';
        if (!error.paymentHandled) {
            showError(error.message || 'No fue posible confirmar la compra.');
        } else {
            lastPaymentStatus = 'rechazado';
        }
    }
});

ticketTypeOptions.forEach((option) => {
    const quantityInput = getTicketQuantityInput(option);

    option.addEventListener('change', () => {
        syncTicketOption(option);
        updateStepOneSummary();
    });

    if (quantityInput) {
        quantityInput.addEventListener('input', () => {
            const available = getTicketAvailable(option);
            if (available <= 0) {
                quantityInput.value = '0';
                setOptionSubtotal(option);
                updateStepOneSummary();
                return;
            }

            const value = Number(quantityInput.value || 0);

            if (value < 1) {
                quantityInput.value = '1';
            }

            if (Number(quantityInput.value) > available) {
                quantityInput.value = String(available);
            }

            setOptionSubtotal(option);
            updateStepOneSummary();
        });
    }

    syncTicketOption(option);
});

updateStepOneSummary();

updateStepUI();

const input = document.getElementById('expiry');
const hoy = new Date();
const anio = hoy.getFullYear();
// Los meses en JS van de 0 a 11, sumamos 1 y rellenamos con un cero si es necesario
const mes = String(hoy.getMonth() + 1).padStart(2, '0');

// Establece el valor mínimo como el mes actual (Ej: "2026-04")
input.min = `${anio}-${mes}`;

const phaseQueue = [];
let phaseDisplaying = false;
let pendingFinalAction = null;
let dotsInterval = null;

function startDots() {
    const el = document.getElementById('phase-dots');
    if (!el) return;
    el.textContent = '•';
    el.classList.remove('hidden');
    let count = 1;
    dotsInterval = setInterval(() => {
        count = count >= 3 ? 1 : count + 1;
        el.textContent = '•'.repeat(count);
    }, 500);
}

function stopDots() {
    if (dotsInterval) { clearInterval(dotsInterval); dotsInterval = null; }
    const el = document.getElementById('phase-dots');
    if (el) el.classList.add('hidden');
}

const ERROR_KEYWORDS = ['no se pudo', 'error', 'falló', 'fallo', 'no fue posible', 'rechaz', 'denegado', 'cancelado', 'imposible', 'fallida'];

function isErrorPhase(text) {
    const lower = text.toLowerCase();
    return ERROR_KEYWORDS.some(kw => lower.includes(kw));
}

function processPhaseQueue() {
    if (phaseQueue.length === 0) {
        phaseDisplaying = false;
        if (pendingFinalAction) {
            const action = pendingFinalAction;
            pendingFinalAction = null;
            setTimeout(action, 400);
        }
        return;
    }
    stopDots();
    phaseDisplaying = true;
    const text = phaseQueue.shift();
    const list = document.getElementById('phases-list');
    if (!list) { processPhaseQueue(); return; }
    const isError = isErrorPhase(text);
    const item = document.createElement('div');
    item.className = `flex items-center gap-3 text-sm ${isError ? 'text-error' : 'text-gray-700'}`;
    item.style.cssText = 'opacity:0; transform:translateY(6px); transition:opacity 0.35s ease, transform 0.35s ease;';
    const icon = isError
        ? `<i class="fa-solid fa-circle-xmark text-error text-lg shrink-0"></i>`
        : `<i class="fa-solid fa-circle-check text-main text-lg shrink-0"></i>`;
    item.innerHTML = `${icon}<span>${text}</span>`;
    list.appendChild(item);
    requestAnimationFrame(() => requestAnimationFrame(() => {
        item.style.opacity = '1';
        item.style.transform = 'translateY(0)';
    }));
    setTimeout(processPhaseQueue, 900);
}

function appendPhaseLog(text) {
    if (!text) return;
    phaseQueue.push(text);
    if (!phaseDisplaying) processPhaseQueue();
}

function handleMessage(dto) {
    if (dto.fase === 'fase_progreso') {
        appendPhaseLog(dto.detalle);
        return;
    }

    if (dto.fase === 'MENSAJE_BONITO') {
        const esExito = dto.estadoTransaccion === 'aprobado' || lastPaymentStatus === 'aprobado';
        const showFinalMessage = () => {
        const resultBtn = document.getElementById('payment-result-btn');
        if (esExito) {
            showSuccess(`
                <div class="flex flex-col sm:flex-row items-center justify-between gap-4">
                    <div>
                        <p class="text-sm font-normal">${dto.detalle}</p>
                        <p class="text-xs font-normal mt-1 opacity-70">ID de compra: ${lastPurchaseId}</p>
                    </div>
                    <a href="/api/purchases/${lastPurchaseId}/descargar-entradas"
                       target="_blank"
                       class="bg-success text-white px-5 py-2.5 rounded-2xl hover:bg-success/90 transition-colors flex items-center gap-2 whitespace-nowrap">
                        <i class="fa-solid fa-download"></i>
                        Descargar Entradas (PDF)
                    </a>
                </div>`);
            if (resultBtn) {
                resultBtn.textContent = 'Finalizar';
                resultBtn.classList.remove('hidden');
                resultBtn.onclick = () => { window.location.href = '/'; };
            }
        } else {
            failCount += 1;
            showError(dto.detalle);
            if (resultBtn) {
                resultBtn.textContent = 'Reintentar pago';
                resultBtn.classList.remove('hidden');
                resultBtn.onclick = () => {
                    currentStep = 4;
                    isSubmitting = false;
                    confirmStepButton.disabled = false;
                    confirmStepButton.removeAttribute('aria-busy');
                    confirmStepButton.textContent = 'Confirmar compra';
                    confirmStepButton.classList.remove('hidden');
                    stopDots();
                    document.getElementById('phases-list').innerHTML = '';
                    document.getElementById('phase-dots').classList.remove('hidden');
                    phaseQueue.length = 0;
                    phaseDisplaying = false;
                    pendingFinalAction = null;
                    document.getElementById('notify-btn').classList.add('hidden');
                    resultBtn.classList.add('hidden');
                    updateStepUI();
                };
            }
            if (failCount >= 3) {
                document.getElementById('notify-btn').classList.remove('hidden');
            }
        }
        }; // fin showFinalMessage
        if (phaseDisplaying || phaseQueue.length > 0) {
            pendingFinalAction = showFinalMessage;
        } else {
            showFinalMessage();
        }
        return;
    }

    if (dto.fase === 'resultado_final') {
        lastPaymentStatus = dto.estadoTransaccion;
    }

    if (typeof appendLog === 'function') appendLog(dto);
    if (typeof faseToStep === 'function' && typeof advanceTo === 'function') advanceTo(faseToStep(dto.fase));

    if (dto.fase === 'resultado_final') {
        if (typeof setCircle === 'function') {
            setCircle(3, dto.estadoTransaccion === 'aprobado' ? 'bg-success' : 'bg-error');
        }
    }
}

(function initNotifyModal() {
    const modal        = document.getElementById('notify-modal');
    const panelConfirm = document.getElementById('notify-panel-confirm');
    const panelForm    = document.getElementById('notify-panel-form');
    const panelDesc    = document.getElementById('notify-panel-desc');
    const panelSuccess = document.getElementById('notify-panel-success');
    const contactValue = document.getElementById('notify-contact-value');
    const dropdownBtn    = document.getElementById('notify-dropdown-btn');
    const dropdownOpts   = document.getElementById('notify-dropdown-options');
    const dropdownLabel  = document.getElementById('notify-dropdown-label');
    const dropdownArrow  = document.getElementById('notify-dropdown-arrow');

    let selectedContactType = 'email';

    function showPanel(panel) {
        [panelConfirm, panelForm, panelDesc, panelSuccess].forEach(p => p?.classList.add('hidden'));
        panel.classList.remove('hidden');
    }

    function openModal() {
        showPanel(panelConfirm);
        modal.classList.remove('hidden');
    }

    function closeModal() {
        modal.classList.add('hidden');
    }

    function toggleDropdown(open) {
        dropdownOpts.classList.toggle('hidden', !open);
        dropdownArrow.style.transform = open ? 'rotate(180deg)' : '';
    }

    dropdownBtn.addEventListener('click', () => {
        const isOpen = !dropdownOpts.classList.contains('hidden');
        toggleDropdown(!isOpen);
    });

    dropdownOpts.querySelectorAll('button[data-value]').forEach(opt => {
        opt.addEventListener('click', () => {
            selectedContactType = opt.dataset.value;
            dropdownLabel.textContent = opt.textContent.trim();
            toggleDropdown(false);
            if (selectedContactType === 'email') {
                contactValue.type = 'email';
                contactValue.pattern = '';
                contactValue.placeholder = 'correo@ejemplo.com';
            } else {
                contactValue.type = 'tel';
                contactValue.pattern = '[0-9+\\s\\-]{7,15}';
                contactValue.placeholder = '+57 300 000 0000';
            }
            contactValue.value = '';
        });
    });

    document.addEventListener('click', (e) => {
        if (!dropdownBtn.contains(e.target) && !dropdownOpts.contains(e.target)) {
            toggleDropdown(false);
        }
    });

    document.getElementById('notify-btn').addEventListener('click', openModal);
    document.getElementById('notify-no').addEventListener('click', closeModal);
    document.getElementById('notify-yes').addEventListener('click', () => showPanel(panelForm));
    document.getElementById('notify-cancel').addEventListener('click', closeModal);
    document.getElementById('notify-cancel-desc').addEventListener('click', closeModal);

    document.getElementById('notify-next').addEventListener('click', () => {
        if (!contactValue.checkValidity() || !contactValue.value.trim()) {
            contactValue.reportValidity();
            return;
        }
        showPanel(panelDesc);
    });

    document.getElementById('notify-send').addEventListener('click', () => {
        showPanel(panelSuccess);
        setTimeout(() => { closeModal(); window.location.href = '/'; }, 3000);
    });
}());