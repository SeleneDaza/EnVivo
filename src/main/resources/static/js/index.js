const passwordInput   = document.getElementById('newPassword');
const confirmInput    = document.getElementById('confirmPassword');
const msg             = document.getElementById('msgConfirm');
const emailInput      = document.getElementById('newEmail');
const msgEmail        = document.getElementById('msgEmail');
const btnRegistrar    = document.getElementById('btnRegistrar');
const emailPattern    = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;

function validarRegistro() {
    if (!passwordInput || !confirmInput || !msg || !btnRegistrar || !emailInput || !msgEmail) {
        return;
    }

    const pass = passwordInput.value;
    const confirm = confirmInput.value;
    const email = emailInput.value.trim();
    let isEmailValid = false;

    if (email === '') {
        msgEmail.classList.add('hidden');
        emailInput.style.borderColor = '';
    } else {
        msgEmail.classList.remove('hidden');
        isEmailValid = emailPattern.test(email);
        if (isEmailValid) {
            msgEmail.textContent = '✓ Correo valido';
            msgEmail.style.color = '#06C270';
            emailInput.style.borderColor = '#06C270';
        } else {
            msgEmail.textContent = '✗ Ingresa un correo valido';
            msgEmail.style.color = '#FF3B3B';
            emailInput.style.borderColor = '#FF3B3B';
        }
    }

    let passwordsMatch = false;

    if (confirm === '') {
        msg.classList.add('hidden');
        confirmInput.style.borderColor = '';
    } else {
        msg.classList.remove('hidden');
        passwordsMatch = pass === confirm;
        if (passwordsMatch) {
            msg.textContent = '✓ Las contraseñas coinciden';
            msg.style.color = '#06C270';
            confirmInput.style.borderColor = '#06C270';
        } else {
            msg.textContent = '✗ Las contraseñas no coinciden';
            msg.style.color = '#FF3B3B';
            confirmInput.style.borderColor = '#FF3B3B';
        }
    }

    btnRegistrar.disabled = !(isEmailValid && passwordsMatch);
}

if (confirmInput) {
    confirmInput.addEventListener('input', validarRegistro);
}
if (passwordInput) {
    passwordInput.addEventListener('input', validarRegistro);
}
if (emailInput) {
    emailInput.addEventListener('input', validarRegistro);
}
