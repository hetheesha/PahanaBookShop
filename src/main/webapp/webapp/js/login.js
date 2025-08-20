// Login page functionality

document.addEventListener('DOMContentLoaded', function() {
    // Check if user is already logged in
    if (isSessionValid()) {
        window.location.href = 'index.html';
        return;
    }

    // Initialize login form
    initializeLoginForm();
    
    // Load remembered username if available
    loadRememberedCredentials();
});

function initializeLoginForm() {
    const loginForm = document.getElementById('login-form');
    const loginBtn = document.getElementById('login-btn');
    
    if (!loginForm) return;

    loginForm.addEventListener('submit', handleLogin);
    
    // Add enter key support
    loginForm.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleLogin(e);
        }
    });

    // Add input validation
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');

    if (usernameInput) {
        usernameInput.addEventListener('blur', validateUsernameField);
        usernameInput.addEventListener('input', clearFieldError);
    }

    if (passwordInput) {
        passwordInput.addEventListener('blur', validatePasswordField);
        passwordInput.addEventListener('input', clearFieldError);
    }
}

async function handleLogin(e) {
    e.preventDefault();
    
    const form = e.target;
    const formData = getFormData(form);
    const loginBtn = document.getElementById('login-btn');
    
    // Clear previous errors
    clearFieldErrors();
    
    // Validate form
    if (!validateLoginForm(formData)) {
        return;
    }
    
    // Show loading state
    if (loginBtn) {
        const btnText = loginBtn.querySelector('.btn-text');
        const btnLoading = loginBtn.querySelector('.btn-loading');

        if (btnText && btnLoading) {
            btnText.style.display = 'none';
            btnLoading.style.display = 'flex';
        } else {
            loginBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Signing in...';
        }

        loginBtn.disabled = true;
    }

    showLoadingOverlay();
    
    try {
        const response = await authApi.login(formData.username, formData.password);
        
        if (response.data && response.data.success) {
            const userData = response.data.data.user || response.data.data || response.data.user;
            console.log('Login - User data received:', userData); // Debug log

            // Save user session
            saveUserSession(userData);
            
            // Save remember me preference
            if (formData['remember-me']) {
                saveToStorage(CONFIG.STORAGE_KEYS.REMEMBER_ME, {
                    username: formData.username,
                    rememberMe: true
                });
            } else {
                removeFromStorage(CONFIG.STORAGE_KEYS.REMEMBER_ME);
            }
            
            // Hide loading overlay
            hideLoadingOverlay();

            // Show success message
            showToast(CONFIG.SUCCESS_MESSAGES.LOGIN, 'success');

            // Redirect to main application
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1000);
            
        } else {
            throw new Error(response.data.message || 'Login failed');
        }
        
    } catch (error) {
        console.error('Login error:', error);

        // Hide loading overlay
        hideLoadingOverlay();

        let errorMessage = 'Login failed. Please try again.';

        if (error instanceof ApiError) {
            if (error.isUnauthorized()) {
                errorMessage = 'Invalid username or password.';
            } else if (error.isNetworkError()) {
                errorMessage = 'Network error. Please check your connection.';
            } else {
                errorMessage = error.message || errorMessage;
            }
        }

        showToast(errorMessage, 'error');

        // Show field-specific errors if available
        if (error.data && error.data.errors) {
            Object.keys(error.data.errors).forEach(field => {
                showFieldError(field, error.data.errors[field]);
            });
        }

    } finally {
        // Re-enable login button
        if (loginBtn) {
            const btnText = loginBtn.querySelector('.btn-text');
            const btnLoading = loginBtn.querySelector('.btn-loading');

            if (btnText && btnLoading) {
                btnText.style.display = 'flex';
                btnLoading.style.display = 'none';
            } else {
                loginBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i> Sign In';
            }

            loginBtn.disabled = false;
        }
    }
}

function validateLoginForm(formData) {
    let isValid = true;
    
    // Validate username
    if (!formData.username || formData.username.trim() === '') {
        showFieldError('username', 'Username is required');
        isValid = false;
    } else if (!validateUsername(formData.username)) {
        showFieldError('username', 'Invalid username format');
        isValid = false;
    }
    
    // Validate password
    if (!formData.password || formData.password.trim() === '') {
        showFieldError('password', 'Password is required');
        isValid = false;
    } else if (!validatePassword(formData.password)) {
        showFieldError('password', `Password must be at least ${CONFIG.VALIDATION.PASSWORD.MIN_LENGTH} characters`);
        isValid = false;
    }
    
    return isValid;
}

function validateUsernameField() {
    const usernameInput = document.getElementById('username');
    if (!usernameInput) return;
    
    const username = usernameInput.value.trim();
    
    if (username && !validateUsername(username)) {
        showFieldError('username', 'Username must be 3-50 characters and contain only letters, numbers, and underscores');
        return false;
    }
    
    return true;
}

function validatePasswordField() {
    const passwordInput = document.getElementById('password');
    if (!passwordInput) return;
    
    const password = passwordInput.value;
    
    if (password && !validatePassword(password)) {
        showFieldError('password', `Password must be at least ${CONFIG.VALIDATION.PASSWORD.MIN_LENGTH} characters`);
        return false;
    }
    
    return true;
}

function clearFieldError(e) {
    const field = e.target;
    const errorElement = field.parentElement.querySelector('.error-message');
    
    if (errorElement) {
        errorElement.remove();
    }
    
    field.classList.remove('invalid');
}

function loadRememberedCredentials() {
    const remembered = loadFromStorage(CONFIG.STORAGE_KEYS.REMEMBER_ME);
    
    if (remembered && remembered.rememberMe) {
        const usernameInput = document.getElementById('username');
        const rememberCheckbox = document.getElementById('remember-me');
        
        if (usernameInput) {
            usernameInput.value = remembered.username || '';
        }
        
        if (rememberCheckbox) {
            rememberCheckbox.checked = true;
        }
    }
}

function togglePassword() {
    const passwordInput = document.getElementById('password');
    const passwordEye = document.getElementById('password-eye');

    if (!passwordInput || !passwordEye) return;

    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        passwordEye.className = 'fas fa-eye-slash';
    } else {
        passwordInput.type = 'password';
        passwordEye.className = 'fas fa-eye';
    }
}

// Utility functions for modern login

function showForgotPassword() {
    document.getElementById('forgot-password-modal').style.display = 'flex';
}

function closeForgotPassword() {
    document.getElementById('forgot-password-modal').style.display = 'none';
}

function showHelp() {
    showToast('Contact support at: support@pahanaedu.com', 'info');
}

function showLoadingOverlay() {
    document.getElementById('loading-overlay').style.display = 'flex';
}

function hideLoadingOverlay() {
    document.getElementById('loading-overlay').style.display = 'none';
}

// Demo login function (for testing)
function demoLogin() {
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    
    if (usernameInput) usernameInput.value = 'admin';
    if (passwordInput) passwordInput.value = 'admin123';
    
    // Trigger login
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        const event = new Event('submit', { bubbles: true, cancelable: true });
        loginForm.dispatchEvent(event);
    }
}

// Add demo login button functionality
document.addEventListener('DOMContentLoaded', function() {
    const demoCredentials = document.querySelector('.demo-credentials');
    if (demoCredentials) {
        demoCredentials.style.cursor = 'pointer';
        demoCredentials.addEventListener('click', demoLogin);
        demoCredentials.title = 'Click to auto-fill demo credentials';
    }
});

// Handle browser back button
window.addEventListener('popstate', function() {
    if (isSessionValid()) {
        window.location.href = 'index.html';
    }
});

// Prevent form submission on enter in input fields (except password)
document.addEventListener('DOMContentLoaded', function() {
    const inputs = document.querySelectorAll('input:not([type="password"])');
    inputs.forEach(input => {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const form = this.closest('form');
                if (form) {
                    const event = new Event('submit', { bubbles: true, cancelable: true });
                    form.dispatchEvent(event);
                }
            }
        });
    });
});

// Export functions for global access
window.togglePassword = togglePassword;
window.demoLogin = demoLogin;
