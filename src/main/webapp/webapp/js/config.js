// Configuration file for the frontend application

const CONFIG = {
    // API Base URL - Same origin, so use relative path
    API_BASE_URL: '/bookshop/api',
    
    // Application settings
    APP_NAME: 'Pahana Edu Bookshop',
    VERSION: '1.0.0',
    
    // Pagination settings
    DEFAULT_PAGE_SIZE: 20,
    MAX_PAGE_SIZE: 100,
    
    // Toast notification settings
    TOAST_DURATION: 5000, // 5 seconds
    
    // Session settings
    SESSION_TIMEOUT: 30 * 60 * 1000, // 30 minutes in milliseconds
    
    // Local storage keys
    STORAGE_KEYS: {
        USER_DATA: 'bookshop_user_data',
        SESSION_TOKEN: 'bookshop_session_token',
        REMEMBER_ME: 'bookshop_remember_me',
        LAST_LOGIN: 'bookshop_last_login'
    },
    
    // API endpoints
    ENDPOINTS: {
        // Authentication
        LOGIN: '/auth/login',
        LOGOUT: '/auth/logout',
        PROFILE: '/auth/profile',
        CHANGE_PASSWORD: '/auth/change-password',
        
        // Users
        USERS: '/users',
        USER_BY_ID: '/users/{id}',
        
        // Customers
        CUSTOMERS: '/customers',
        CUSTOMER_BY_ID: '/customers/{id}',
        CUSTOMER_SEARCH: '/customers/search',
        CUSTOMER_BY_ACCOUNT: '/customers/account/{accountNo}',
        GENERATE_ACCOUNT_NO: '/customers/generate-account-no',
        
        // Categories
        CATEGORIES: '/categories',
        CATEGORY_BY_ID: '/categories/{id}',
        ACTIVE_CATEGORIES: '/categories/active',
        
        // Items
        ITEMS: '/items',
        ITEM_BY_ID: '/items/{id}',
        ITEM_SEARCH: '/items/search',
        ITEM_BY_CODE: '/items/code/{code}',
        ITEM_BY_BARCODE: '/items/barcode/{barcode}',
        ITEMS_BY_CATEGORY: '/items/category/{categoryId}',
        LOW_STOCK_ITEMS: '/items/low-stock',
        GENERATE_ITEM_CODE: '/items/generate-code/{categoryId}',
        ADJUST_STOCK: '/items/{id}/adjust-stock',
        
        // Bills
        BILLS: '/bills',
        BILL_BY_ID: '/bills/{id}',
        BILL_BY_NUMBER: '/bills/number/{billNumber}',
        BILLS_BY_CUSTOMER: '/bills/customer/{customerId}',
        BILLS_BY_DATE_RANGE: '/bills/date-range',
        GENERATE_BILL_NUMBER: '/bills/generate-number',
        CANCEL_BILL: '/bills/{id}/cancel',
        BILL_PDF: '/bills/{id}/pdf',
        BILL_PDF_BY_NUMBER: '/bills/number/{billNumber}/pdf'
    },
    
    // User roles
    USER_ROLES: {
        ADMIN: 'admin',
        MANAGER: 'manager',
        CASHIER: 'cashier'
    },
    
    // Status values
    STATUS: {
        ACTIVE: 'active',
        INACTIVE: 'inactive',
        PAID: 'paid',
        PENDING: 'pending',
        CANCELLED: 'cancelled'
    },
    
    // Payment methods
    PAYMENT_METHODS: {
        CASH: 'cash',
        CARD: 'card',
        BANK_TRANSFER: 'bank_transfer',
        OTHER: 'other'
    },
    
    // Gender options (backend expects uppercase)
    GENDER_OPTIONS: {
        MALE: 'MALE',
        FEMALE: 'FEMALE',
        OTHER: 'OTHER'
    },

    // Customer status options (backend expects uppercase)
    CUSTOMER_STATUS: {
        ACTIVE: 'ACTIVE',
        INACTIVE: 'INACTIVE'
    },
    
    // Validation rules
    VALIDATION: {
        USERNAME: {
            MIN_LENGTH: 3,
            MAX_LENGTH: 50,
            PATTERN: /^[a-zA-Z0-9_]+$/
        },
        PASSWORD: {
            MIN_LENGTH: 6,
            MAX_LENGTH: 100
        },
        EMAIL: {
            PATTERN: /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        },
        PHONE: {
            PATTERN: /^[\+]?[1-9][\d]{0,15}$/
        },
        PRICE: {
            MIN: 0,
            MAX: 999999.99
        },
        QUANTITY: {
            MIN: 0,
            MAX: 999999
        }
    },
    
    // Date formats
    DATE_FORMATS: {
        DISPLAY: 'DD/MM/YYYY',
        API: 'YYYY-MM-DD',
        DATETIME_DISPLAY: 'DD/MM/YYYY HH:mm',
        TIME_DISPLAY: 'HH:mm'
    },
    
    // Currency settings
    CURRENCY: {
        SYMBOL: 'LKR',
        CODE: 'LKR',
        DECIMAL_PLACES: 2
    },
    
    // Table settings
    TABLE: {
        ROWS_PER_PAGE_OPTIONS: [10, 20, 50, 100],
        DEFAULT_SORT_ORDER: 'asc'
    },
    
    // File upload settings
    FILE_UPLOAD: {
        MAX_SIZE: 5 * 1024 * 1024, // 5MB
        ALLOWED_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'application/pdf']
    },
    
    // Error messages
    ERROR_MESSAGES: {
        NETWORK_ERROR: 'Network error. Please check your connection.',
        UNAUTHORIZED: 'You are not authorized to perform this action.',
        FORBIDDEN: 'Access denied.',
        NOT_FOUND: 'The requested resource was not found.',
        SERVER_ERROR: 'Internal server error. Please try again later.',
        VALIDATION_ERROR: 'Please check your input and try again.',
        SESSION_EXPIRED: 'Your session has expired. Please login again.'
    },
    
    // Success messages
    SUCCESS_MESSAGES: {
        LOGIN: 'Login successful!',
        LOGOUT: 'Logout successful!',
        SAVE: 'Data saved successfully!',
        UPDATE: 'Data updated successfully!',
        DELETE: 'Data deleted successfully!',
        CREATE: 'Data created successfully!'
    }
};

// Utility function to get full API URL
CONFIG.getApiUrl = function(endpoint, params = {}) {
    let url = this.API_BASE_URL + endpoint;
    
    // Replace path parameters
    for (const [key, value] of Object.entries(params)) {
        url = url.replace(`{${key}}`, encodeURIComponent(value));
    }
    
    return url;
};

// Utility function to format currency
CONFIG.formatCurrency = function(amount) {
    const formattedAmount = parseFloat(amount).toFixed(this.CURRENCY.DECIMAL_PLACES);
    return this.CURRENCY.SYMBOL + ' ' + formattedAmount;
};

// Utility function to format date
CONFIG.formatDate = function(date, format = this.DATE_FORMATS.DISPLAY) {
    if (!date) return '';
    
    const d = new Date(date);
    if (isNaN(d.getTime())) return '';
    
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    
    switch (format) {
        case this.DATE_FORMATS.DISPLAY:
            return `${day}/${month}/${year}`;
        case this.DATE_FORMATS.API:
            return `${year}-${month}-${day}`;
        case this.DATE_FORMATS.DATETIME_DISPLAY:
            return `${day}/${month}/${year} ${hours}:${minutes}`;
        case this.DATE_FORMATS.TIME_DISPLAY:
            return `${hours}:${minutes}`;
        default:
            return date;
    }
};

// Make CONFIG available globally
window.CONFIG = CONFIG;
