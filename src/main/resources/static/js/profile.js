function toggleEditForm() {
    const form = document.getElementById('editProfileForm');
    if (form) {
        form.classList.toggle('hidden');
    }
}

window.addEventListener('DOMContentLoaded', function () {
    const params = new URLSearchParams(window.location.search);
    if (params.has('error')) {
        const form = document.getElementById('editProfileForm');
        if (form) {
            form.classList.remove('hidden');
        }
    }
});