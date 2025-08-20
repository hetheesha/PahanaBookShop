// Utility functions for the frontend application

// Loading spinner functions
function showLoading() {
    const loading = document.getElementById('loading');
    if (loading) {
        loading.style.display = 'flex';
    }
}

function hideLoading() {
    const loading = document.getElementById('loading');
    if (loading) {
        loading.style.display = 'none';
    }
}

// Toast notification functions
function showToast(message, type = 'info', duration = CONFIG.TOAST_DURATION) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <i class="fas ${getToastIcon(type)}"></i>
            <span>${message}</span>
        </div>
        <button class="toast-close" onclick="removeToast(this.parentElement)">
            <i class="fas fa-times"></i>
        </button>
    `;

    container.appendChild(toast);

    // Auto remove after duration
    setTimeout(() => {
        removeToast(toast);
    }, duration);
}

function getToastIcon(type) {
    const icons = {
        success: 'fa-check-circle',
        error: 'fa-exclamation-circle',
        warning: 'fa-exclamation-triangle',
        info: 'fa-info-circle'
    };
    return icons[type] || icons.info;
}

function removeToast(toast) {
    if (toast && toast.parentElement) {
        toast.style.animation = 'slideOut 0.3s ease forwards';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }
}

// Form validation functions
function validateEmail(email) {
    return CONFIG.VALIDATION.EMAIL.PATTERN.test(email);
}

function validatePhone(phone) {
    return CONFIG.VALIDATION.PHONE.PATTERN.test(phone);
}

function validateUsername(username) {
    return username.length >= CONFIG.VALIDATION.USERNAME.MIN_LENGTH &&
           username.length <= CONFIG.VALIDATION.USERNAME.MAX_LENGTH &&
           CONFIG.VALIDATION.USERNAME.PATTERN.test(username);
}

function validatePassword(password) {
    return password.length >= CONFIG.VALIDATION.PASSWORD.MIN_LENGTH &&
           password.length <= CONFIG.VALIDATION.PASSWORD.MAX_LENGTH;
}

function validatePrice(price) {
    const num = parseFloat(price);
    return !isNaN(num) && num >= CONFIG.VALIDATION.PRICE.MIN && num <= CONFIG.VALIDATION.PRICE.MAX;
}

function validateQuantity(quantity) {
    const num = parseInt(quantity);
    return !isNaN(num) && num >= CONFIG.VALIDATION.QUANTITY.MIN && num <= CONFIG.VALIDATION.QUANTITY.MAX;
}

// Form utility functions
function getFormData(formElement) {
    const formData = new FormData(formElement);
    const data = {};
    
    for (let [key, value] of formData.entries()) {
        // Handle checkboxes
        if (formElement.querySelector(`[name="${key}"]`).type === 'checkbox') {
            data[key] = formElement.querySelector(`[name="${key}"]`).checked;
        } else {
            data[key] = value;
        }
    }
    
    return data;
}

function setFormData(formElement, data) {
    Object.keys(data).forEach(key => {
        const field = formElement.querySelector(`[name="${key}"]`);
        if (field) {
            if (field.type === 'checkbox') {
                field.checked = data[key];
            } else {
                field.value = data[key] || '';
            }
        }
    });
}

function clearForm(formElement) {
    formElement.reset();
    // Clear any validation errors
    const errorElements = formElement.querySelectorAll('.error-message');
    errorElements.forEach(el => el.remove());
    
    const invalidFields = formElement.querySelectorAll('.invalid');
    invalidFields.forEach(field => field.classList.remove('invalid'));
}

function showFieldError(fieldName, message) {
    const field = document.querySelector(`[name="${fieldName}"]`);
    if (!field) return;
    
    // Remove existing error
    const existingError = field.parentElement.querySelector('.error-message');
    if (existingError) {
        existingError.remove();
    }
    
    // Add error class
    field.classList.add('invalid');
    
    // Add error message
    const errorElement = document.createElement('div');
    errorElement.className = 'error-message';
    errorElement.textContent = message;
    errorElement.style.color = '#e74c3c';
    errorElement.style.fontSize = '12px';
    errorElement.style.marginTop = '5px';
    
    field.parentElement.appendChild(errorElement);
}

function clearFieldErrors() {
    const errorElements = document.querySelectorAll('.error-message');
    errorElements.forEach(el => el.remove());
    
    const invalidFields = document.querySelectorAll('.invalid');
    invalidFields.forEach(field => field.classList.remove('invalid'));
}

// Date utility functions
function formatDate(date, format = CONFIG.DATE_FORMATS.DISPLAY) {
    return CONFIG.formatDate(date, format);
}

function parseDate(dateString) {
    return new Date(dateString);
}

function getCurrentDate() {
    return new Date().toISOString().split('T')[0];
}

function getCurrentDateTime() {
    return new Date().toISOString();
}

// Currency utility functions
function formatCurrency(amount) {
    return CONFIG.formatCurrency(amount);
}

function parseCurrency(currencyString) {
    return parseFloat(currencyString.replace(/[^0-9.-]+/g, ''));
}

// Local storage utility functions
function saveToStorage(key, data) {
    try {
        localStorage.setItem(key, JSON.stringify(data));
        return true;
    } catch (error) {
        console.error('Error saving to localStorage:', error);
        return false;
    }
}

function loadFromStorage(key, defaultValue = null) {
    try {
        const data = localStorage.getItem(key);
        return data ? JSON.parse(data) : defaultValue;
    } catch (error) {
        console.error('Error loading from localStorage:', error);
        return defaultValue;
    }
}

function removeFromStorage(key) {
    try {
        localStorage.removeItem(key);
        return true;
    } catch (error) {
        console.error('Error removing from localStorage:', error);
        return false;
    }
}

function clearStorage() {
    try {
        localStorage.clear();
        return true;
    } catch (error) {
        console.error('Error clearing localStorage:', error);
        return false;
    }
}

// Session management functions
function saveUserSession(userData) {
    saveToStorage(CONFIG.STORAGE_KEYS.USER_DATA, userData);
    saveToStorage(CONFIG.STORAGE_KEYS.LAST_LOGIN, getCurrentDateTime());
}

function getUserSession() {
    return loadFromStorage(CONFIG.STORAGE_KEYS.USER_DATA);
}

function clearUserSession() {
    removeFromStorage(CONFIG.STORAGE_KEYS.USER_DATA);
    removeFromStorage(CONFIG.STORAGE_KEYS.SESSION_TOKEN);
    removeFromStorage(CONFIG.STORAGE_KEYS.LAST_LOGIN);
}

function isSessionValid() {
    const userData = getUserSession();
    const lastLogin = loadFromStorage(CONFIG.STORAGE_KEYS.LAST_LOGIN);
    
    if (!userData || !lastLogin) {
        return false;
    }
    
    const lastLoginTime = new Date(lastLogin).getTime();
    const currentTime = new Date().getTime();
    const timeDiff = currentTime - lastLoginTime;
    
    return timeDiff < CONFIG.SESSION_TIMEOUT;
}

// URL utility functions
function getQueryParam(param) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param);
}

function setQueryParam(param, value) {
    const url = new URL(window.location);
    url.searchParams.set(param, value);
    window.history.pushState({}, '', url);
}

function removeQueryParam(param) {
    const url = new URL(window.location);
    url.searchParams.delete(param);
    window.history.pushState({}, '', url);
}

// Table utility functions
function createTableRow(data, columns) {
    const row = document.createElement('tr');
    
    columns.forEach(column => {
        const cell = document.createElement('td');
        
        if (typeof column.render === 'function') {
            cell.innerHTML = column.render(data[column.key], data);
        } else {
            cell.textContent = data[column.key] || '';
        }
        
        if (column.className) {
            cell.className = column.className;
        }
        
        row.appendChild(cell);
    });
    
    return row;
}

function updateTable(tableId, data, columns) {
    const tbody = document.querySelector(`#${tableId} tbody`);
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (data.length === 0) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = columns.length;
        cell.textContent = 'No data available';
        cell.style.textAlign = 'center';
        cell.style.padding = '20px';
        cell.style.color = '#6c757d';
        row.appendChild(cell);
        tbody.appendChild(row);
        return;
    }
    
    data.forEach(item => {
        const row = createTableRow(item, columns);
        tbody.appendChild(row);
    });
}

// Debounce function for search inputs
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Confirmation dialog
function confirmAction(message, callback) {
    // Create a more styled confirmation dialog
    const confirmed = confirm(message);
    if (confirmed) {
        callback();
    }
}

// Enhanced confirmation dialog with custom styling (future enhancement)
function confirmActionEnhanced(title, message, callback, options = {}) {
    const {
        confirmText = 'Delete',
        cancelText = 'Cancel',
        type = 'danger'
    } = options;

    // For now, use the standard confirm dialog
    // In the future, this could be replaced with a custom modal
    const confirmed = confirm(`${title}\n\n${message}`);
    if (confirmed) {
        callback();
    }
}

// Generate random ID
function generateId() {
    return Math.random().toString(36).substr(2, 9);
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Check if user has permission
function hasPermission(requiredRole) {
    const userData = getUserSession();
    console.log('Permission check - User data:', userData); // Debug log
    console.log('Permission check - Required role:', requiredRole); // Debug log

    if (!userData) {
        console.log('Permission check - No user data found');
        return false;
    }

    // Handle different possible role field names
    const userRole = userData.role || userData.userRole || userData.authority || 'cashier';
    console.log('Permission check - User role:', userRole); // Debug log

    const roleHierarchy = {
        'admin': 3,
        'ADMIN': 3,
        'manager': 2,
        'MANAGER': 2,
        'cashier': 1,
        'CASHIER': 1
    };

    const userLevel = roleHierarchy[userRole] || 1;
    const requiredLevel = roleHierarchy[requiredRole] || 1;

    console.log('Permission check - User level:', userLevel, 'Required level:', requiredLevel); // Debug log

    const hasAccess = userLevel >= requiredLevel;
    console.log('Permission check - Has access:', hasAccess); // Debug log

    return hasAccess;
}

// Initialize tooltips (if using a tooltip library)
function initializeTooltips() {
    // Implementation depends on tooltip library used
}

// Print function
function printElement(elementId) {
    const element = document.getElementById(elementId);
    if (!element) return;
    
    const printWindow = window.open('', '_blank');
    printWindow.document.write(`
        <html>
            <head>
                <title>Print</title>
                <style>
                    body { font-family: Arial, sans-serif; }
                    table { width: 100%; border-collapse: collapse; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background-color: #f2f2f2; }
                </style>
            </head>
            <body>
                ${element.innerHTML}
            </body>
        </html>
    `);
    printWindow.document.close();
    printWindow.print();
}

// Export functions to global scope
window.showLoading = showLoading;
window.hideLoading = hideLoading;
window.showToast = showToast;
window.removeToast = removeToast;
window.validateEmail = validateEmail;
window.validatePhone = validatePhone;
window.validateUsername = validateUsername;
window.validatePassword = validatePassword;
window.validatePrice = validatePrice;
window.validateQuantity = validateQuantity;
window.getFormData = getFormData;
window.setFormData = setFormData;
window.clearForm = clearForm;
window.showFieldError = showFieldError;
window.clearFieldErrors = clearFieldErrors;
window.formatDate = formatDate;
window.formatCurrency = formatCurrency;
window.saveUserSession = saveUserSession;
window.getUserSession = getUserSession;
window.clearUserSession = clearUserSession;
window.isSessionValid = isSessionValid;
window.updateTable = updateTable;
window.debounce = debounce;
window.confirmAction = confirmAction;
window.hasPermission = hasPermission;
window.printElement = printElement;
