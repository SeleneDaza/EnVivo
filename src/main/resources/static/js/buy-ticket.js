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

    progress.style.width = `${currentStep * 25}%`;

    prevStepButton.classList.toggle('hidden', currentStep === 1);
    nextStepButton.classList.toggle('hidden', currentStep === 4);
    confirmStepButton.classList.toggle('hidden', currentStep !== 4);

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
        throw new Error(result.message || 'No fue posible registrar la compra.');
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
        showError('No se pudo identificar el evento.');
        return;
    }

    isSubmitting = true;
    confirmStepButton.disabled = true;
    confirmStepButton.setAttribute('aria-busy', 'true');
    confirmStepButton.textContent = 'Conectando...';

    try {
        // 1. Conectar STOMP y esperar suscripción antes de cualquier otra cosa
        await new Promise(function (resolve, reject) {
            const sessionId = document.querySelector('meta[name="session-id"]')?.getAttribute('content');
            if (!sessionId) { resolve(); return; }

            const socket = new SockJS('/ws');
            const stomp = Stomp.over(socket);
            stomp.debug = null;
            stomp.connect({}, function () {
                stomp.subscribe('/topic/payment-progress/' + sessionId, function (frame) {
                    try {
                        const dto = JSON.parse(frame.body);

                        // Limpiar mensaje inicial
                        const empty = document.getElementById('progress-log-empty');
                        if (empty) empty.remove();

                        // Agregar al log
                        const log = document.getElementById('progress-log');
                        if (log) {
                            const row = document.createElement('div');
                            row.className = 'flex flex-col gap-0.5 pb-2 border-b border-gray-50';

                            const top = document.createElement('div');
                            top.className = 'flex items-center gap-2';

                            const badge = document.createElement('span');
                            badge.className = 'text-[10px] font-black uppercase tracking-wide px-2 py-0.5 rounded-full bg-gray-100 text-gray-500 shrink-0';
                            badge.textContent = dto.fase || '';

                            const msg = document.createElement('span');
                            msg.className = 'text-sm font-bold text-gray-800';
                            msg.textContent = dto.mensaje || '';

                            top.appendChild(badge);
                            top.appendChild(msg);
                            row.appendChild(top);

                            if (dto.detalle) {
                                const detail = document.createElement('p');
                                detail.className = 'text-xs text-gray-400 mt-0.5';
                                detail.textContent = dto.detalle;
                                row.appendChild(detail);
                            }

                            log.appendChild(row);
                            log.scrollTop = log.scrollHeight;
                        }

                        // Actualizar círculos de progreso
                        const fase = dto.fase;
                        if (fase === 'transaccion_creada') {
                            activateCircle(1);
                        } else if (fase === 'verificando_tarjeta' || fase === 'respuesta_tarjeta') {
                            activateCircle(2);
                        } else if (fase === 'resultado_final') {
                            const ok = (dto.estado_transaccion || dto.estadoTransaccion) === 'aprobado';
                            activateCircle(3, ok ? 'bg-success' : 'bg-error');
                            if (!ok) showError(dto.detalle || 'Pago rechazado.');
                        }
                    } catch (e) {}
                });
                resolve(); // ← resuelve SOLO después de suscribirse
            }, function (err) {
                resolve(); // si falla, continuar igual
            });
        });

        // 2. STOMP suscrito — ahora sí llamar al checkout
        confirmStepButton.textContent = 'Procesando...';
        const purchase = await submitCheckout();

        confirmStepButton.classList.add('hidden');
        prevStepButton.classList.add('hidden');
        cancelButton.textContent = 'Volver a la cartelera';
        cancelButton.classList.remove('btn-ghost');
        cancelButton.classList.add('btn-neutral');

        showSuccess(`
            <div class="flex flex-col sm:flex-row items-center justify-between gap-4">
                <div>
                    <p class="font-black">¡Compra registrada exitosamente!</p>
                    <p class="text-sm font-normal mt-1">ID de compra: ${purchase.purchaseId}</p>
                </div>
                <a href="/api/purchases/${purchase.purchaseId}/descargar-entradas" target="_blank"
                   class="bg-success text-white px-5 py-2.5 rounded-2xl hover:bg-success/90 transition-colors flex items-center gap-2 whitespace-nowrap">
                    <i class="fa-solid fa-download"></i>
                    Descargar Entradas (PDF)
                </a>
            </div>`);

    } catch (error) {
        isSubmitting = false;
        confirmStepButton.disabled = false;
        confirmStepButton.removeAttribute('aria-busy');
        confirmStepButton.textContent = 'Confirmar compra';
        showError(error.message || 'No fue posible confirmar la compra.');
    }
});

function activateCircle(num, colorClass) {
    const color = colorClass || 'bg-main';
    for (let i = 1; i <= num; i++) {
        const circle = document.getElementById('prog-circle-' + i);
        if (!circle) continue;
        circle.classList.remove('bg-gray-200', 'text-gray-400');
        circle.classList.add(i === num ? color : 'bg-main', 'text-white');
    }
    if (num >= 1) {
        const f1 = document.getElementById('prog-line-fill-1');
        if (f1) f1.style.width = '100%';
    }
    if (num >= 2) {
        const f2 = document.getElementById('prog-line-fill-2');
        if (f2) f2.style.width = '100%';
    }
}

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