function openModal(name, image, date, price, category, description) {
    document.getElementById('modalName').innerText = name;
    document.getElementById('modalImg').src = image;
    document.getElementById('modalDate').innerText = date;
    document.getElementById('modalPrice').innerText = price;
    document.getElementById('modalCat').innerText = category || 'GENERAL';
    document.getElementById('modalDesc').innerText = description || 
        'No hay descripción disponible para este evento.';

    const modal = document.getElementById('eventModal');
    modal.classList.remove('hidden');
    document.body.classList.add('modal-open');
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