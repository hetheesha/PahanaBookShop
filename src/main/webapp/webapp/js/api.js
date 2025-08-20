// API utility functions for making HTTP requests

class ApiService {
    constructor() {
        this.baseUrl = CONFIG.API_BASE_URL;
        this.defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
    }

    // Generic request method
    async request(url, options = {}) {
        const config = {
            method: 'GET',
            headers: { ...this.defaultHeaders },
            credentials: 'include', // Re-enabled for same-origin requests
            ...options
        };

        try {
            showLoading();
            const response = await fetch(url, config);
            
            // Handle different response types
            const contentType = response.headers.get('content-type');
            let data;
            
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else if (contentType && contentType.includes('application/pdf')) {
                data = await response.blob();
            } else {
                data = await response.text();
            }

            if (!response.ok) {
                console.error('API Error Response:', {
                    status: response.status,
                    statusText: response.statusText,
                    data: data,
                    url: url
                });
                throw new ApiError(response.status, data.message || data || 'Request failed', data);
            }

            return { data, status: response.status, headers: response.headers };
        } catch (error) {
            if (error instanceof ApiError) {
                throw error;
            }
            throw new ApiError(0, 'Network error', error);
        } finally {
            hideLoading();
        }
    }

    // GET request
    async get(endpoint, params = {}) {
        let url = CONFIG.getApiUrl(endpoint, params);

        // Add query parameters
        const queryParams = new URLSearchParams();
        Object.keys(params).forEach(key => {
            if (!endpoint.includes(`{${key}}`)) {
                queryParams.append(key, params[key]);
            }
        });

        // Append query string if there are parameters
        if (queryParams.toString()) {
            url += (url.includes('?') ? '&' : '?') + queryParams.toString();
        }

        return this.request(url);
    }

    // POST request
    async post(endpoint, data = null, params = {}) {
        const url = CONFIG.getApiUrl(endpoint, params);
        return this.request(url, {
            method: 'POST',
            body: data ? JSON.stringify(data) : null
        });
    }

    // PUT request
    async put(endpoint, data = null, params = {}) {
        const url = CONFIG.getApiUrl(endpoint, params);
        return this.request(url, {
            method: 'PUT',
            body: data ? JSON.stringify(data) : null
        });
    }

    // DELETE request
    async delete(endpoint, params = {}) {
        const url = CONFIG.getApiUrl(endpoint, params);
        return this.request(url, {
            method: 'DELETE'
        });
    }

    // Download file (for PDFs, etc.)
    async download(endpoint, params = {}, filename = null) {
        const url = CONFIG.getApiUrl(endpoint, params);
        const response = await this.request(url);
        
        if (response.data instanceof Blob) {
            const blob = response.data;
            const downloadUrl = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = downloadUrl;
            link.download = filename || 'download';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(downloadUrl);
        }
        
        return response;
    }
}

// Custom error class for API errors
class ApiError extends Error {
    constructor(status, message, data = null) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.data = data;
    }

    isNetworkError() {
        return this.status === 0;
    }

    isUnauthorized() {
        return this.status === 401;
    }

    isForbidden() {
        return this.status === 403;
    }

    isNotFound() {
        return this.status === 404;
    }

    isServerError() {
        return this.status >= 500;
    }

    isValidationError() {
        return this.status === 400;
    }
}

// Create global API instance
const api = new ApiService();

// Authentication API methods
const authApi = {
    async login(username, password) {
        return api.post(CONFIG.ENDPOINTS.LOGIN, { username, password });
    },

    async logout() {
        return api.post(CONFIG.ENDPOINTS.LOGOUT);
    },

    async getProfile() {
        return api.get(CONFIG.ENDPOINTS.PROFILE);
    },

    async changePassword(currentPassword, newPassword, confirmPassword) {
        return api.post(CONFIG.ENDPOINTS.CHANGE_PASSWORD, {
            currentPassword,
            newPassword,
            confirmPassword
        });
    }
};

// User API methods
const userApi = {
    async getUsers(page = 1, pageSize = CONFIG.DEFAULT_PAGE_SIZE) {
        return api.get(CONFIG.ENDPOINTS.USERS, { page, pageSize });
    },

    async getUserById(id) {
        return api.get(CONFIG.ENDPOINTS.USER_BY_ID, { id });
    },

    async createUser(userData) {
        return api.post(CONFIG.ENDPOINTS.USERS, userData);
    },

    async updateUser(id, userData) {
        return api.put(CONFIG.ENDPOINTS.USER_BY_ID, userData, { id });
    },

    async deleteUser(id) {
        return api.delete(CONFIG.ENDPOINTS.USER_BY_ID, { id });
    },

    async getProfile() {
        return api.get(CONFIG.ENDPOINTS.PROFILE);
    }
};

// Customer API methods
const customerApi = {
    async getCustomers(page = 1, pageSize = CONFIG.DEFAULT_PAGE_SIZE) {
        return api.get(CONFIG.ENDPOINTS.CUSTOMERS, { page, pageSize });
    },

    async getCustomerById(id) {
        return api.get(CONFIG.ENDPOINTS.CUSTOMER_BY_ID, { id });
    },

    async searchCustomers(query, page = 1, pageSize = CONFIG.DEFAULT_PAGE_SIZE) {
        return api.get(CONFIG.ENDPOINTS.CUSTOMER_SEARCH, { q: query, page, pageSize });
    },

    async getCustomerByAccount(accountNo) {
        return api.get(CONFIG.ENDPOINTS.CUSTOMER_BY_ACCOUNT, { accountNo });
    },

    async generateAccountNumber() {
        return api.get(CONFIG.ENDPOINTS.GENERATE_ACCOUNT_NO);
    },

    async createCustomer(customerData) {
        return api.post(CONFIG.ENDPOINTS.CUSTOMERS, customerData);
    },

    async updateCustomer(id, customerData) {
        return api.put(CONFIG.ENDPOINTS.CUSTOMER_BY_ID, customerData, { id });
    },

    async deleteCustomer(id) {
        return api.delete(CONFIG.ENDPOINTS.CUSTOMER_BY_ID, { id });
    }
};

// Category API methods
const categoryApi = {
    async getCategories(page = 1, pageSize = CONFIG.DEFAULT_PAGE_SIZE) {
        return api.get(CONFIG.ENDPOINTS.CATEGORIES, { page, pageSize });
    },

    async getActiveCategories() {
        return api.get(CONFIG.ENDPOINTS.ACTIVE_CATEGORIES);
    },

    async getCategoryById(id) {
        return api.get(CONFIG.ENDPOINTS.CATEGORY_BY_ID, { id });
    },

    async createCategory(categoryData) {
        return api.post(CONFIG.ENDPOINTS.CATEGORIES, categoryData);
    },

    async updateCategory(id, categoryData) {
        return api.put(CONFIG.ENDPOINTS.CATEGORY_BY_ID, categoryData, { id });
    },

    async deleteCategory(id) {
        return api.delete(CONFIG.ENDPOINTS.CATEGORY_BY_ID, { id });
    }
};

// Item API methods
const itemApi = {
    async getItems(page = 1, pageSize = CONFIG.DEFAULT_PAGE_SIZE) {
        return api.get(CONFIG.ENDPOINTS.ITEMS, { page, pageSize });
    },

    async getItemById(id) {
        return api.get(CONFIG.ENDPOINTS.ITEM_BY_ID, { id });
    },

    async searchItems(query, page = 1, pageSize = CONFIG.DEFAULT_PAGE_SIZE) {
        return api.get(CONFIG.ENDPOINTS.ITEM_SEARCH, { q: query, page, pageSize });
    },

    async getItemByCode(code) {
        return api.get(CONFIG.ENDPOINTS.ITEM_BY_CODE, { code });
    },

    async getItemByBarcode(barcode) {
        return api.get(CONFIG.ENDPOINTS.ITEM_BY_BARCODE, { barcode });
    },

    async getItemsByCategory(categoryId) {
        return api.get(CONFIG.ENDPOINTS.ITEMS_BY_CATEGORY, { categoryId });
    },

    async getLowStockItems() {
        return api.get(CONFIG.ENDPOINTS.LOW_STOCK_ITEMS);
    },

    async generateItemCode(categoryId) {
        return api.get(CONFIG.ENDPOINTS.GENERATE_ITEM_CODE, { categoryId });
    },

    async createItem(itemData) {
        return api.post(CONFIG.ENDPOINTS.ITEMS, itemData);
    },

    async updateItem(id, itemData) {
        return api.put(CONFIG.ENDPOINTS.ITEM_BY_ID, itemData, { id });
    },

    async adjustStock(adjustmentData) {
        const { itemId, newStockLevel, reason } = adjustmentData;
        return api.post(CONFIG.ENDPOINTS.ADJUST_STOCK, { newQuantity: newStockLevel, reason }, { id: itemId });
    },

    async deleteItem(id) {
        return api.delete(CONFIG.ENDPOINTS.ITEM_BY_ID, { id });
    }
};

// Bill API methods
const billApi = {
    async getBills(page = 1, pageSize = CONFIG.DEFAULT_PAGE_SIZE) {
        return api.get(CONFIG.ENDPOINTS.BILLS, { page, pageSize });
    },

    async getBillById(id) {
        return api.get(CONFIG.ENDPOINTS.BILL_BY_ID, { id });
    },

    async getBillByNumber(billNumber) {
        return api.get(CONFIG.ENDPOINTS.BILL_BY_NUMBER, { billNumber });
    },

    async getBillsByCustomer(customerId, page = 1, pageSize = CONFIG.DEFAULT_PAGE_SIZE) {
        return api.get(CONFIG.ENDPOINTS.BILLS_BY_CUSTOMER, { customerId, page, pageSize });
    },

    async getBillsByDateRange(startDate, endDate, page = 1, pageSize = CONFIG.DEFAULT_PAGE_SIZE) {
        return api.get(CONFIG.ENDPOINTS.BILLS_BY_DATE_RANGE, {
            startDate,
            endDate,
            page,
            pageSize
        });
    },

    async generateBillNumber() {
        return api.get(CONFIG.ENDPOINTS.GENERATE_BILL_NUMBER);
    },

    async createBill(billData) {
        return api.post(CONFIG.ENDPOINTS.BILLS, billData);
    },

    async cancelBill(id) {
        return api.post(CONFIG.ENDPOINTS.CANCEL_BILL, null, { id });
    },

    async downloadBillPdf(id, filename = null) {
        return api.download(CONFIG.ENDPOINTS.BILL_PDF, { id }, filename);
    },

    async downloadBillPdfByNumber(billNumber, filename = null) {
        return api.download(CONFIG.ENDPOINTS.BILL_PDF_BY_NUMBER, { billNumber }, filename);
    }
};

// Error handling utility
function handleApiError(error) {
    console.error('API Error:', error);
    
    let message = CONFIG.ERROR_MESSAGES.SERVER_ERROR;
    
    if (error instanceof ApiError) {
        if (error.isNetworkError()) {
            message = CONFIG.ERROR_MESSAGES.NETWORK_ERROR;
        } else if (error.isUnauthorized()) {
            message = CONFIG.ERROR_MESSAGES.UNAUTHORIZED;
            // Redirect to login if session expired
            if (window.location.pathname !== '/login.html') {
                window.location.href = 'login.html';
            }
        } else if (error.isForbidden()) {
            message = CONFIG.ERROR_MESSAGES.FORBIDDEN;
        } else if (error.isNotFound()) {
            message = CONFIG.ERROR_MESSAGES.NOT_FOUND;
        } else if (error.isValidationError()) {
            message = error.message || CONFIG.ERROR_MESSAGES.VALIDATION_ERROR;
        } else if (error.isServerError()) {
            message = CONFIG.ERROR_MESSAGES.SERVER_ERROR;
        } else {
            message = error.message;
        }
    }
    
    showToast(message, 'error');
    return message;
}

// Make API methods available globally
window.api = api;
window.authApi = authApi;
window.userApi = userApi;
window.customerApi = customerApi;
window.categoryApi = categoryApi;
window.itemApi = itemApi;
window.billApi = billApi;
window.handleApiError = handleApiError;
window.ApiError = ApiError;
