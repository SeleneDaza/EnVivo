const passwordInput   = document.getElementById('newPassword');
const confirmInput    = document.getElementById('confirmPassword');
const msg             = document.getElementById('msgConfirm');
const btnRegistrar    = document.getElementById('btnRegistrar');

function validarContrasenas() {
    const pass    = passwordInput.value;
    const confirm = confirmInput.value;

    if (confirm === '') {
        msg.classList.add('hidden');
        btnRegistrar.disabled = false;
        return;
    }

    msg.classList.remove('hidden');

    if (pass === confirm) {
        msg.textContent = '✓ Las contraseñas coinciden';
        msg.style.color = '#06C270';
        confirmInput.style.borderColor = '#06C270';
        btnRegistrar.disabled = false;
    } else {
        msg.textContent = '✗ Las contraseñas no coinciden';
        // Usando color Error #FF3B3B
        msg.style.color = '#FF3B3B';
        confirmInput.style.borderColor = '#FF3B3B';
        btnRegistrar.disabled = true;
    }
}

confirmInput.addEventListener('input', validarContrasenas);
passwordInput.addEventListener('input', validarContrasenas);