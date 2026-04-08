function toggleInterest(buttonElement) {
    const eventId = buttonElement.getAttribute('data-event-id');
    const headers = { 'Content-Type': 'application/json' };
    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

    if (csrfTokenMeta && csrfHeaderMeta) {
        headers[csrfHeaderMeta.getAttribute('content')] = csrfTokenMeta.getAttribute('content');
    }

    fetch(`/evento/${eventId}/interest`, {
        method: 'POST',
        headers: headers
    })
        .then(response => response.json().then(data => {
            if (!response.ok) {
                throw new Error(data.message || 'No se pudo actualizar tu favorito.');
            }
            return data;
        }))
        .then(data => {
            if (!data.interested) {
                buttonElement.closest('.relative').style.opacity = '0.5';
                window.location.reload();
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert(error.message || 'No se pudo actualizar tu favorito.');
        });
}