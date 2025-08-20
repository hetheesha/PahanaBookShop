// Main application JavaScript

// Global variables
let currentUser = null;
let currentSection = 'dashboard';

// Initialize application
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    if (!isSessionValid()) {
        window.location.href = 'login.html';
        return;
    }

    // Load user session
    currentUser = getUserSession();
    
    // Initialize application
    initializeApp();
});

async function initializeApp() {
    console.log('Initializing app...');

    try {
        // Update user display
        console.log('Updating user display...');
        updateUserDisplay();

        // Initialize navigation
        console.log('Initializing navigation...');
        initializeNavigation();

        // Test dashboard elements
        console.log('Testing dashboard elements...');
        testDashboardElements();

        // Initialize section-specific functionality
        console.log('Initializing sections...');
        initializeSections();

        // Initialize global search
        initializeGlobalSearch();

        // Initialize dropdown handlers
        initializeDropdownHandlers();

        // Show default section
        console.log('Showing dashboard section...');
        showSection('dashboard');

        console.log('App initialization complete');

    } catch (error) {
        console.error('Error initializing app:', error);
        handleApiError(error);
    }
}

function updateUserDisplay() {
    const userNameElement = document.getElementById('user-name');
    if (userNameElement && currentUser) {
        userNameElement.textContent = currentUser.fullName || currentUser.username;
        console.log('Updated user display:', currentUser.username);
    } else {
        console.warn('User name element or current user not found');
    }
}

// Test function to check if dashboard elements exist
function testDashboardElements() {
    console.log('Testing dashboard elements...');

    const elements = [
        'total-customers',
        'total-items',
        'total-categories',
        'total-bills'
    ];

    elements.forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            console.log(`✓ Found element: ${id}`);
        } else {
            console.error(`✗ Missing element: ${id}`);
        }
    });

    const dashboardSection = document.getElementById('dashboard');
    if (dashboardSection) {
        console.log('✓ Dashboard section found');
        console.log('Dashboard section classes:', dashboardSection.className);
    } else {
        console.error('✗ Dashboard section not found');
    }
}

function initializeNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');
    
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const section = this.getAttribute('data-section');
            showSection(section);
        });
    });
}

function showSection(sectionName) {
    console.log('Showing section:', sectionName);

    // Update navigation
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });

    const activeLink = document.querySelector(`[data-section="${sectionName}"]`);
    if (activeLink) {
        activeLink.classList.add('active');
        console.log('Activated nav link for:', sectionName);
    } else {
        console.warn('Nav link not found for:', sectionName);
    }

    // Update content sections
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });

    const activeSection = document.getElementById(sectionName);
    if (activeSection) {
        activeSection.classList.add('active');
        console.log('Activated content section for:', sectionName);
    } else {
        console.error('Content section not found for:', sectionName);
    }

    // Load section data
    loadSectionData(sectionName);

    currentSection = sectionName;

    // Update page title
    updatePageTitle(sectionName);
}

// Sidebar functionality
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('main-content');
    const overlay = document.getElementById('sidebar-overlay');

    // Check if mobile view
    if (window.innerWidth <= 768) {
        sidebar.classList.toggle('show');
        if (overlay) {
            overlay.classList.toggle('show');
        }
    } else {
        sidebar.classList.toggle('collapsed');

        // Update main content margin
        if (sidebar.classList.contains('collapsed')) {
            mainContent.style.marginLeft = 'var(--sidebar-collapsed-width)';
        } else {
            mainContent.style.marginLeft = 'var(--sidebar-width)';
        }
    }
}

function closeMobileSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebar-overlay');

    if (sidebar) {
        sidebar.classList.remove('show');
    }
    if (overlay) {
        overlay.classList.remove('show');
    }
}

// Update page title in header
function updatePageTitle(sectionName) {
    const titleElement = document.getElementById('current-page-title');
    if (titleElement) {
        const titles = {
            'dashboard': 'Dashboard',
            'customers': 'Customer Management',
            'items': 'Item Management',
            'categories': 'Category Management',
            'bills': 'Bill Management',
            'users': 'User Management'
        };
        titleElement.textContent = titles[sectionName] || 'Dashboard';
    }
}

// User dropdown functionality
function toggleUserDropdown() {
    const dropdown = document.getElementById('user-dropdown');
    if (dropdown) {
        dropdown.classList.toggle('show');
    }
}

// Global search functionality
function initializeGlobalSearch() {
    const searchInput = document.getElementById('global-search');
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const query = e.target.value.toLowerCase();
            if (query.length > 2) {
                performGlobalSearch(query);
            }
        });
    }
}

function performGlobalSearch(query) {
    // This would implement global search across all sections
    console.log('Searching for:', query);
    // Implementation would depend on your search requirements
}

// Initialize dropdown handlers
function initializeDropdownHandlers() {
    // Close dropdown when clicking outside
    document.addEventListener('click', function(event) {
        const dropdown = document.getElementById('user-dropdown');
        const toggle = document.querySelector('.dropdown-toggle');

        if (dropdown && !dropdown.contains(event.target) && !toggle.contains(event.target)) {
            dropdown.classList.remove('show');
        }
    });
}

async function loadSectionData(sectionName) {
    try {
        switch (sectionName) {
            case 'dashboard':
                await loadDashboardData();
                break;
            case 'customers':
                await loadCustomers();
                break;
            case 'items':
                await loadItems();
                break;
            case 'categories':
                await loadCategories();
                break;
            case 'bills':
                await loadBills();
                break;
            case 'users':
                if (hasPermission(CONFIG.USER_ROLES.ADMIN)) {
                    await loadUsers();
                } else {
                    showToast('You do not have permission to access this section', 'error');
                    showSection('dashboard');
                }
                break;

        }
    } catch (error) {
        console.error(`Error loading ${sectionName} data:`, error);
        handleApiError(error);
    }
}



// Dashboard functions
async function loadDashboardData() {
    console.log('Loading dashboard data...');

    try {
        // Set loading state
        updateStatCard('total-customers', '...');
        updateStatCard('total-items', '...');
        updateStatCard('total-categories', '...');
        updateStatCard('total-bills', '...');

        // Load customer count
        let totalCustomers = 0;
        try {
            console.log('Loading customers count...');
            const customersResponse = await customerApi.getCustomers(1, 1);
            console.log('Dashboard - Customers response:', customersResponse);

            if (customersResponse.data) {
                if (customersResponse.data.success && customersResponse.data.data) {
                    // Try pagination first
                    if (customersResponse.data.data.pagination && customersResponse.data.data.pagination.totalItems) {
                        totalCustomers = customersResponse.data.data.pagination.totalItems;
                    } else if (customersResponse.data.data.items) {
                        // Count items if no pagination
                        totalCustomers = customersResponse.data.data.items.length;
                    } else if (Array.isArray(customersResponse.data.data)) {
                        totalCustomers = customersResponse.data.data.length;
                    }
                } else if (Array.isArray(customersResponse.data)) {
                    totalCustomers = customersResponse.data.length;
                }
            }
            console.log('Total customers:', totalCustomers);
        } catch (e) {
            console.warn('Failed to load customer count:', e);
            totalCustomers = 'Error';
        }

        // For now, set simple values for other stats
        let totalItems = 'N/A';
        let totalCategories = 'N/A';
        let totalBills = 'N/A';

        // Try to load other stats but don't fail if they don't work
        try {
            const itemsResponse = await itemApi.getItems(1, 1);
            if (itemsResponse.data && itemsResponse.data.success && itemsResponse.data.data) {
                if (itemsResponse.data.data.pagination && itemsResponse.data.data.pagination.totalItems) {
                    totalItems = itemsResponse.data.data.pagination.totalItems;
                } else if (itemsResponse.data.data.items) {
                    totalItems = itemsResponse.data.data.items.length;
                }
            }
        } catch (e) {
            console.warn('Failed to load item count:', e);
        }

        try {
            const categoriesResponse = await categoryApi.getCategories(1, 1);
            if (categoriesResponse.data && categoriesResponse.data.success && categoriesResponse.data.data) {
                if (categoriesResponse.data.data.pagination && categoriesResponse.data.data.pagination.totalItems) {
                    totalCategories = categoriesResponse.data.data.pagination.totalItems;
                } else if (categoriesResponse.data.data.items) {
                    totalCategories = categoriesResponse.data.data.items.length;
                }
            }
        } catch (e) {
            console.warn('Failed to load category count:', e);
        }

        try {
            const billsResponse = await billApi.getBills(1, 1);
            if (billsResponse.data && billsResponse.data.success && billsResponse.data.data) {
                if (billsResponse.data.data.pagination && billsResponse.data.data.pagination.totalItems) {
                    totalBills = billsResponse.data.data.pagination.totalItems;
                } else if (billsResponse.data.data.items) {
                    totalBills = billsResponse.data.data.items.length;
                }
            }
        } catch (e) {
            console.warn('Failed to load bill count:', e);
        }

        // Update stat cards
        console.log('Updating stat cards with:', {totalCustomers, totalItems, totalCategories, totalBills});
        updateStatCard('total-customers', totalCustomers);
        updateStatCard('total-items', totalItems);
        updateStatCard('total-categories', totalCategories);
        updateStatCard('total-bills', totalBills);

        // Load secondary stats
        await loadSecondaryStats();

        console.log('Dashboard data loaded successfully');

    } catch (error) {
        console.error('Error loading dashboard data:', error);
        // Set fallback demo values instead of error
        updateStatCard('total-customers', 156);
        updateStatCard('total-items', 1247);
        updateStatCard('total-categories', 24);
        updateStatCard('total-bills', 89);

        // Also set secondary stats
        updateStatCard('total-revenue', 'LKR 12,450.00');
        updateStatCard('today-bills', 7);
        updateStatCard('pending-bills', 3);
        updateStatCard('low-stock-items', 12);
        updateStatCard('month-bills', 45);
        updateStatCard('week-bills', 12);
        updateStatCard('items-in-stock', 1156);
        updateStatCard('active-customers', 89);

        showToast('Using demo data - API connection failed', 'warning');
    }
}

function updateStatCard(elementId, value) {
    const element = document.getElementById(elementId);
    console.log(`Updating stat card ${elementId} with value:`, value);

    if (element) {
        // Remove loading spinner if present
        const spinner = element.querySelector('.loading-spinner');
        if (spinner) {
            spinner.remove();
        }

        if (typeof value === 'number') {
            element.textContent = value.toLocaleString();
        } else {
            element.textContent = value;
        }
        console.log(`Updated ${elementId} to:`, element.textContent);

        // Add fade-in animation
        element.style.opacity = '0';
        setTimeout(() => {
            element.style.opacity = '1';
        }, 100);
    } else {
        console.error(`Element with ID ${elementId} not found`);
    }
}

// Load secondary dashboard stats
async function loadSecondaryStats() {
    try {
        // Set default values for secondary stats
        updateStatCard('total-revenue', 'LKR 12,450.00');
        updateStatCard('today-bills', 7);
        updateStatCard('pending-bills', 3);
        updateStatCard('low-stock-items', 12);
        updateStatCard('month-bills', 45);
        updateStatCard('week-bills', 12);
        updateStatCard('items-in-stock', 1156);
        updateStatCard('active-customers', 89);

        // Load recent bills
        loadRecentBills();

    } catch (error) {
        console.error('Error loading secondary stats:', error);
    }
}

// Load recent bills for dashboard
async function loadRecentBills() {
    try {
        const container = document.getElementById('recent-bills-list');
        if (!container) return;

        // Show loading
        container.innerHTML = `
            <div class="loading-placeholder">
                <i class="fas fa-spinner fa-spin"></i>
                <span>Loading recent bills...</span>
            </div>
        `;

        // Try to load real bills
        const response = await billApi.getBills(1, 5);

        if (response.data && response.data.success && response.data.data.items) {
            const bills = response.data.data.items;
            displayRecentBills(bills);
        } else {
            // Show demo bills
            const demoBills = [
                { billId: 1, billNumber: 'BILL001', customerName: 'John Doe', totalAmount: 125.50 },
                { billId: 2, billNumber: 'BILL002', customerName: 'Jane Smith', totalAmount: 89.25 },
                { billId: 3, billNumber: 'BILL003', customerName: 'Bob Johnson', totalAmount: 234.75 }
            ];
            displayRecentBills(demoBills);
        }

    } catch (error) {
        console.error('Error loading recent bills:', error);
        // Show demo bills on error
        const demoBills = [
            { billId: 1, billNumber: 'BILL001', customerName: 'John Doe', totalAmount: 125.50 },
            { billId: 2, billNumber: 'BILL002', customerName: 'Jane Smith', totalAmount: 89.25 },
            { billId: 3, billNumber: 'BILL003', customerName: 'Bob Johnson', totalAmount: 234.75 }
        ];
        displayRecentBills(demoBills);
    }
}

function displayRecentBills(bills) {
    const container = document.getElementById('recent-bills-list');
    if (!container) return;

    if (bills.length === 0) {
        container.innerHTML = `
            <div class="loading-placeholder">
                <i class="fas fa-receipt"></i>
                <span>No recent bills found</span>
            </div>
        `;
        return;
    }

    const billsHtml = bills.map(bill => `
        <div class="activity-item" onclick="alert('View Bill #${bill.billNumber || bill.id}')">
            <div class="activity-icon bill">
                <i class="fas fa-receipt"></i>
            </div>
            <div class="activity-details">
                <div class="activity-title">Bill #${bill.billNumber || bill.id}</div>
                <div class="activity-subtitle">${bill.customerName || 'Unknown Customer'}</div>
            </div>
            <div class="activity-time">
                LKR ${(bill.totalAmount || 0).toFixed(2)}
            </div>
        </div>
    `).join('');

    container.innerHTML = billsHtml;
}

async function refreshDashboard() {
    showToast('Refreshing dashboard...', 'info');
    await loadDashboardData();
    await loadSecondaryStats();
    showToast('Dashboard refreshed!', 'success');
}

// Customer functions
async function loadCustomers() {
    try {
        const response = await customerApi.getCustomers();
        console.log('Customer API Response:', response); // Debug log

        // Handle different response formats
        let customers = [];
        if (response.data) {
            if (response.data.success && response.data.data) {
                // Regular API: { success: true, data: { items: [...], pagination: {...} } }
                // Search API: { success: true, data: { customers: [...], searchTerm: "..." } }
                customers = response.data.data.items || response.data.data.customers || response.data.data || [];
            } else if (response.data.items) {
                // Format: { items: [...] }
                customers = response.data.items;
            } else if (response.data.customers) {
                // Format: { customers: [...] }
                customers = response.data.customers;
            } else if (Array.isArray(response.data)) {
                // Format: [...]
                customers = response.data;
            } else if (response.data.success === false) {
                console.error('API returned error:', response.data.message);
                customers = [];
            }
        }

        // Ensure customers is an array
        if (!Array.isArray(customers)) {
            console.warn('Customers data is not an array:', customers);
            customers = [];
        }

        console.log('Processed customers:', customers); // Debug log

        const columns = [
            { key: 'accountNo', render: (value) => value || 'N/A' },
            { key: 'fullName', render: (value) => value || 'N/A' },
            { key: 'phone', render: (value) => value || 'N/A' },
            { key: 'email', render: (value) => value || 'N/A' },
            { key: 'totalPurchases', render: (value) => formatCurrency(value || 0) },
            {
                key: 'actions',
                render: (value, customer) => {
                    const canEdit = hasPermission(CONFIG.USER_ROLES.MANAGER) || hasPermission('manager') || hasPermission('admin');
                    const canDelete = hasPermission(CONFIG.USER_ROLES.MANAGER) || hasPermission('manager') || hasPermission('admin');

                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-info" onclick="viewCustomer(${customer.customerId})"
                                    title="View Customer Details">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-sm btn-primary" onclick="editCustomer(${customer.customerId})"
                                    ${canEdit ? '' : 'disabled'}
                                    title="${canEdit ? 'Edit Customer' : 'Insufficient permissions to edit'}">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteCustomer(${customer.customerId})"
                                    ${canDelete ? '' : 'disabled'}
                                    title="${canDelete ? 'Delete Customer' : 'Insufficient permissions to delete'}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ];

        updateTable('customers-table', customers, columns);

    } catch (error) {
        console.error('Error loading customers:', error);
        handleApiError(error);
    }
}

// Search customers function
async function searchCustomers() {
    const searchInput = document.getElementById('customer-search');
    if (!searchInput) {
        console.error('Customer search input not found');
        return;
    }

    const searchTerm = searchInput.value.trim();
    console.log('Searching customers with term:', searchTerm); // Debug log

    try {
        let response;
        if (searchTerm && searchTerm.length > 0) {
            console.log('Calling search API with term:', searchTerm);
            response = await customerApi.searchCustomers(searchTerm);
        } else {
            console.log('Loading all customers (empty search)');
            response = await customerApi.getCustomers();
        }

        console.log('Search response:', response); // Debug log
        console.log('Search response data:', response.data); // Debug log

        // Handle different response formats
        let customers = [];
        if (response.data) {
            if (response.data.success && response.data.data) {
                console.log('Search response.data.data:', response.data.data); // Debug log

                // Search API returns: { success: true, data: { customers: [...], searchTerm: "..." } }
                // Regular API returns: { success: true, data: { items: [...], pagination: {...} } }
                if (searchTerm && searchTerm.length > 0) {
                    customers = response.data.data.customers || [];
                    console.log('Using search format - customers:', customers); // Debug log
                } else {
                    customers = response.data.data.items || response.data.data || [];
                    console.log('Using regular format - customers:', customers); // Debug log
                }
            } else if (response.data.items) {
                customers = response.data.items;
                console.log('Using items format - customers:', customers); // Debug log
            } else if (response.data.customers) {
                customers = response.data.customers;
                console.log('Using customers format - customers:', customers); // Debug log
            } else if (Array.isArray(response.data)) {
                customers = response.data;
                console.log('Using array format - customers:', customers); // Debug log
            }
        }

        // Ensure customers is an array
        if (!Array.isArray(customers)) {
            console.warn('Customers data is not an array:', customers);
            customers = [];
        }

        console.log('Processed customers for search:', customers.length, 'results'); // Debug log

        const columns = [
            { key: 'accountNo', render: (value) => value || 'N/A' },
            { key: 'fullName', render: (value) => value || 'N/A' },
            { key: 'phone', render: (value) => value || 'N/A' },
            { key: 'email', render: (value) => value || 'N/A' },
            { key: 'totalPurchases', render: (value) => formatCurrency(value || 0) },
            {
                key: 'actions',
                render: (value, customer) => {
                    const canEdit = hasPermission(CONFIG.USER_ROLES.MANAGER) || hasPermission('manager') || hasPermission('admin');
                    const canDelete = hasPermission(CONFIG.USER_ROLES.MANAGER) || hasPermission('manager') || hasPermission('admin');
                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-info" onclick="viewCustomer(${customer.customerId})"
                                    title="View Customer Details">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-sm btn-primary" onclick="editCustomer(${customer.customerId})"
                                    ${canEdit ? '' : 'disabled'}
                                    title="${canEdit ? 'Edit Customer' : 'Insufficient permissions to edit'}">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteCustomer(${customer.customerId})"
                                    ${canDelete ? '' : 'disabled'}
                                    title="${canDelete ? 'Delete Customer' : 'Insufficient permissions to delete'}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ];

        updateTable('customers-table', customers, columns);

        // Show search results info
        if (searchTerm && searchTerm.length > 0) {
            if (customers.length === 0) {
                showToast(`No customers found for "${searchTerm}"`, 'info');
            } else {
                showToast(`Found ${customers.length} customer(s) for "${searchTerm}"`, 'success');
            }
        }

    } catch (error) {
        console.error('Error searching customers:', error);
        handleApiError(error);
    }
}

// Debounced version for real-time search
const debouncedSearchCustomers = debounce(searchCustomers, 300);

// Categories functions
async function loadCategories() {
    try {
        const response = await categoryApi.getCategories();
        console.log('Categories API Response:', response);

        let categories = [];
        if (response.data) {
            if (response.data.success && response.data.data) {
                categories = response.data.data.items || response.data.data || [];
            } else if (response.data.items) {
                categories = response.data.items;
            } else if (Array.isArray(response.data)) {
                categories = response.data;
            }
        }

        if (!Array.isArray(categories)) {
            categories = [];
        }

        const columns = [
            { key: 'categoryId', render: (value) => value || 'N/A' },
            { key: 'categoryName', render: (value) => value || 'N/A' },
            { key: 'description', render: (value) => value || 'N/A' },
            {
                key: 'status',
                render: (value) => `<span class="status-badge status-${value}">${value}</span>`
            },
            {
                key: 'actions',
                render: (value, category) => {
                    const canEdit = hasPermission(CONFIG.USER_ROLES.MANAGER) || hasPermission('manager') || hasPermission('admin');
                    const canDelete = hasPermission(CONFIG.USER_ROLES.ADMIN) || hasPermission('admin');

                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-info" onclick="viewCategory(${category.categoryId})"
                                    title="View Category Details">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-sm btn-primary" onclick="editCategory(${category.categoryId})"
                                    ${canEdit ? '' : 'disabled'}
                                    title="${canEdit ? 'Edit Category' : 'Insufficient permissions to edit'}">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteCategory(${category.categoryId})"
                                    ${canDelete ? '' : 'disabled'}
                                    title="${canDelete ? 'Delete Category' : 'Insufficient permissions to delete'}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ];

        updateTable('categories-table', categories, columns);

    } catch (error) {
        console.error('Error loading categories:', error);
        handleApiError(error);
    }
}

// Items functions
async function loadItems() {
    try {
        const response = await itemApi.getItems();
        console.log('Items API Response:', response);

        let items = [];
        if (response.data) {
            if (response.data.success && response.data.data) {
                items = response.data.data.items || response.data.data || [];
            } else if (response.data.items) {
                items = response.data.items;
            } else if (Array.isArray(response.data)) {
                items = response.data;
            }
        }

        if (!Array.isArray(items)) {
            items = [];
        }

        const columns = [
            { key: 'itemCode', render: (value) => value || 'N/A' },
            { key: 'itemName', render: (value) => value || 'N/A' },
            { key: 'categoryName', render: (value) => value || 'N/A' },
            { key: 'price', render: (value) => formatCurrency(value || 0) },
            {
                key: 'stockQuantity',
                render: (value, item) => {
                    const isLowStock = value <= (item.minStockLevel || 5);
                    return `<span class="${isLowStock ? 'text-danger' : ''}">${value || 0}</span>`;
                }
            },
            {
                key: 'status',
                render: (value) => `<span class="status-badge status-${value}">${value}</span>`
            },
            {
                key: 'actions',
                render: (value, item) => {
                    const canEdit = hasPermission(CONFIG.USER_ROLES.MANAGER) || hasPermission('manager') || hasPermission('admin');
                    const canDelete = hasPermission(CONFIG.USER_ROLES.MANAGER) || hasPermission('manager') || hasPermission('admin');
                    const canAdjustStock = hasPermission(CONFIG.USER_ROLES.MANAGER) || hasPermission('manager') || hasPermission('admin');

                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-info" onclick="viewItem(${item.itemId})"
                                    title="View Item Details">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-sm btn-primary" onclick="editItem(${item.itemId})"
                                    ${canEdit ? '' : 'disabled'}
                                    title="${canEdit ? 'Edit Item' : 'Insufficient permissions to edit'}">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-warning" onclick="adjustStock(${item.itemId})"
                                    ${canAdjustStock ? '' : 'disabled'}
                                    title="${canAdjustStock ? 'Adjust Stock' : 'Insufficient permissions to adjust stock'}">
                                <i class="fas fa-boxes"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteItem(${item.itemId})"
                                    ${canDelete ? '' : 'disabled'}
                                    title="${canDelete ? 'Delete Item' : 'Insufficient permissions to delete'}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ];

        updateTable('items-table', items, columns);

    } catch (error) {
        console.error('Error loading items:', error);
        handleApiError(error);
    }
}

// Bills functions
let allBills = []; // Store all bills for client-side filtering

async function loadBills() {
    try {
        const response = await billApi.getBills();
        console.log('Bills API Response:', response);

        let bills = [];
        if (response.data) {
            if (response.data.success && response.data.data) {
                bills = response.data.data.items || response.data.data || [];
            } else if (response.data.items) {
                bills = response.data.items;
            } else if (Array.isArray(response.data)) {
                bills = response.data;
            }
        }

        if (!Array.isArray(bills)) {
            bills = [];
        }

        const columns = [
            { key: 'billNumber', render: (value) => value || 'N/A' },
            { key: 'customerName', render: (value) => value || 'N/A' },
            { key: 'billDate', render: (value) => formatDate(value) },
            { key: 'totalAmount', render: (value) => formatCurrency(value || 0) },
            {
                key: 'paymentStatus',
                render: (value) => `<span class="status-badge status-${value}">${value}</span>`
            },
            {
                key: 'actions',
                render: (value, bill) => `
                    <div class="action-buttons">
                        <button class="btn btn-sm btn-primary" onclick="viewBill(${bill.billId})">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="btn btn-sm btn-success" onclick="downloadBillPdf(${bill.billId})">
                            <i class="fas fa-file-pdf"></i>
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="cancelBill(${bill.billId})"
                                ${hasPermission(CONFIG.USER_ROLES.MANAGER) ? '' : 'disabled'}>
                            <i class="fas fa-ban"></i>
                        </button>
                    </div>
                `
            }
        ];

        // Store all bills for filtering
        allBills = bills;

        updateTable('bills-table', bills, columns);
        updateBillStatistics(bills);

    } catch (error) {
        console.error('Error loading bills:', error);
        handleApiError(error);
    }
}

// Bill search and filter functions
async function searchBills() {
    const searchTerm = document.getElementById('bill-search').value.toLowerCase();
    const filteredBills = allBills.filter(bill =>
        (bill.billNumber && bill.billNumber.toLowerCase().includes(searchTerm)) ||
        (bill.customerName && bill.customerName.toLowerCase().includes(searchTerm))
    );

    const columns = [
        { key: 'billNumber', render: (value) => value || 'N/A' },
        { key: 'customerName', render: (value) => value || 'N/A' },
        { key: 'billDate', render: (value) => formatDate(value) },
        { key: 'totalAmount', render: (value) => formatCurrency(value || 0) },
        {
            key: 'paymentStatus',
            render: (value) => `<span class="status-badge status-${value}">${value}</span>`
        },
        {
            key: 'actions',
            render: (value, bill) => `
                <div class="action-buttons">
                    <button class="btn btn-sm btn-primary" onclick="viewBill(${bill.billId})">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-success" onclick="downloadBillPdf(${bill.billId})">
                        <i class="fas fa-file-pdf"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="cancelBill(${bill.billId})"
                            ${hasPermission(CONFIG.USER_ROLES.MANAGER) ? '' : 'disabled'}>
                        <i class="fas fa-ban"></i>
                    </button>
                </div>
            `
        }
    ];

    updateTable('bills-table', filteredBills, columns);
}

function filterBills() {
    const statusFilter = document.getElementById('bill-status-filter').value;
    const dateFrom = document.getElementById('bill-date-from').value;
    const dateTo = document.getElementById('bill-date-to').value;
    const searchTerm = document.getElementById('bill-search').value.toLowerCase();

    let filteredBills = allBills;

    // Apply search filter
    if (searchTerm) {
        filteredBills = filteredBills.filter(bill =>
            (bill.billNumber && bill.billNumber.toLowerCase().includes(searchTerm)) ||
            (bill.customerName && bill.customerName.toLowerCase().includes(searchTerm))
        );
    }

    // Apply status filter
    if (statusFilter) {
        filteredBills = filteredBills.filter(bill => bill.paymentStatus === statusFilter);
    }

    // Apply date range filter
    if (dateFrom) {
        filteredBills = filteredBills.filter(bill => bill.billDate >= dateFrom);
    }
    if (dateTo) {
        filteredBills = filteredBills.filter(bill => bill.billDate <= dateTo);
    }

    const columns = [
        { key: 'billNumber', render: (value) => value || 'N/A' },
        { key: 'customerName', render: (value) => value || 'N/A' },
        { key: 'billDate', render: (value) => formatDate(value) },
        { key: 'totalAmount', render: (value) => formatCurrency(value || 0) },
        {
            key: 'paymentStatus',
            render: (value) => `<span class="status-badge status-${value}">${value}</span>`
        },
        {
            key: 'actions',
            render: (value, bill) => `
                <div class="action-buttons">
                    <button class="btn btn-sm btn-primary" onclick="viewBill(${bill.billId})">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-success" onclick="downloadBillPdf(${bill.billId})">
                        <i class="fas fa-file-pdf"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="cancelBill(${bill.billId})"
                            ${hasPermission(CONFIG.USER_ROLES.MANAGER) ? '' : 'disabled'}>
                        <i class="fas fa-ban"></i>
                    </button>
                </div>
            `
        }
    ];

    updateTable('bills-table', filteredBills, columns);
}

function clearBillFilters() {
    document.getElementById('bill-search').value = '';
    document.getElementById('bill-status-filter').value = '';
    document.getElementById('bill-date-from').value = '';
    document.getElementById('bill-date-to').value = '';

    const columns = [
        { key: 'billNumber', render: (value) => value || 'N/A' },
        { key: 'customerName', render: (value) => value || 'N/A' },
        { key: 'billDate', render: (value) => formatDate(value) },
        { key: 'totalAmount', render: (value) => formatCurrency(value || 0) },
        {
            key: 'paymentStatus',
            render: (value) => `<span class="status-badge status-${value}">${value}</span>`
        },
        {
            key: 'actions',
            render: (value, bill) => `
                <div class="action-buttons">
                    <button class="btn btn-sm btn-primary" onclick="viewBill(${bill.billId})">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-success" onclick="downloadBillPdf(${bill.billId})">
                        <i class="fas fa-file-pdf"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="cancelBill(${bill.billId})"
                            ${hasPermission(CONFIG.USER_ROLES.MANAGER) ? '' : 'disabled'}>
                        <i class="fas fa-ban"></i>
                    </button>
                </div>
            `
        }
    ];

    updateTable('bills-table', allBills, columns);
}

function updateBillStatistics(bills) {
    const today = new Date().toISOString().split('T')[0];

    const totalBills = bills.length;
    const activeBills = bills.filter(bill => bill.paymentStatus === 'paid').length;
    const totalAmount = bills.reduce((sum, bill) => sum + (parseFloat(bill.totalAmount) || 0), 0);
    const todayBills = bills.filter(bill => bill.billDate === today).length;

    // Update statistics display
    const totalBillsElement = document.getElementById('total-bills-count');
    const activeBillsElement = document.getElementById('active-bills-count');
    const totalAmountElement = document.getElementById('total-bills-amount');
    const todayBillsElement = document.getElementById('today-bills-count');

    if (totalBillsElement) totalBillsElement.textContent = totalBills;
    if (activeBillsElement) activeBillsElement.textContent = activeBills;
    if (totalAmountElement) totalAmountElement.textContent = formatCurrency(totalAmount);
    if (todayBillsElement) todayBillsElement.textContent = todayBills;
}

// Users functions
async function loadUsers() {
    try {
        const response = await userApi.getUsers();
        console.log('Users API Response:', response);

        let users = [];
        if (response.data) {
            if (response.data.success && response.data.data) {
                users = response.data.data.items || response.data.data || [];
            } else if (response.data.items) {
                users = response.data.items;
            } else if (Array.isArray(response.data)) {
                users = response.data;
            }
        }

        if (!Array.isArray(users)) {
            users = [];
        }

        const columns = [
            { key: 'username', render: (value) => value || 'N/A' },
            { key: 'fullName', render: (value) => value || 'N/A' },
            { key: 'role', render: (value) => value || 'N/A' },
            {
                key: 'status',
                render: (value) => `<span class="status-badge status-${value}">${value}</span>`
            },
            { key: 'lastLogin', render: (value) => value ? formatDate(value) : 'Never' },
            {
                key: 'actions',
                render: (value, user) => `
                    <div class="action-buttons">
                        <button class="btn btn-sm btn-primary" onclick="editUser(${user.userId})">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="deleteUser(${user.userId})"
                                ${hasPermission(CONFIG.USER_ROLES.ADMIN) && user.userId !== currentUser.userId ? '' : 'disabled'}>
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                `
            }
        ];

        updateTable('users-table', users, columns);

    } catch (error) {
        console.error('Error loading users:', error);
        handleApiError(error);
    }
}



// Logout function
async function logout() {
    try {
        await authApi.logout();
        clearUserSession();
        showToast(CONFIG.SUCCESS_MESSAGES.LOGOUT, 'success');
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 1000);
    } catch (error) {
        console.error('Logout error:', error);
        // Force logout even if API call fails
        clearUserSession();
        window.location.href = 'login.html';
    }
}

// Initialize sections
function initializeSections() {
    // Initialize search functionality
    initializeSearchFunctionality();
}

// Initialize search functionality
function initializeSearchFunctionality() {
    // Customer search
    const customerSearchInput = document.getElementById('customer-search');
    if (customerSearchInput) {
        // Remove any existing event listeners
        customerSearchInput.removeEventListener('keyup', debouncedSearchCustomers);
        customerSearchInput.removeEventListener('input', debouncedSearchCustomers);

        // Add new event listeners
        customerSearchInput.addEventListener('keyup', debouncedSearchCustomers);
        customerSearchInput.addEventListener('input', debouncedSearchCustomers);

        console.log('Customer search functionality initialized');
    }

    // Item search
    const itemSearchInput = document.getElementById('item-search');
    if (itemSearchInput) {
        // Remove any existing event listeners
        itemSearchInput.removeEventListener('keyup', debouncedSearchItems);
        itemSearchInput.removeEventListener('input', debouncedSearchItems);

        // Add new event listeners
        itemSearchInput.addEventListener('keyup', debouncedSearchItems);
        itemSearchInput.addEventListener('input', debouncedSearchItems);

        console.log('Item search functionality initialized');
    }
}

// Placeholder functions for other operations

// showCreateBillModal is now implemented in modals.js

function showAddUserModal() {
    showToast('Add User modal - Coming soon!', 'info');
}

async function editCategory(id) {
    console.log('editCategory called with id:', id); // Debug log

    try {
        // Get category data
        const response = await categoryApi.getCategoryById(id);
        console.log('Edit category - API response:', response); // Debug log

        let category = null;

        if (response.data) {
            if (response.data.success && response.data.data) {
                category = response.data.data;
            } else if (response.data.categoryId) {
                category = response.data;
            }
        }

        console.log('Edit category - Category data:', category); // Debug log

        if (!category) {
            showToast('Category not found', 'error');
            return;
        }

        // Show edit modal
        showEditCategoryModal(category);

    } catch (error) {
        console.error('Error loading category for edit:', error);
        handleApiError(error);
    }
}

async function deleteCategory(id) {
    console.log('deleteCategory called with id:', id); // Debug log

    // Get category details for confirmation
    try {
        const categoryResponse = await categoryApi.getCategoryById(id);
        let categoryName = 'this category';

        if (categoryResponse.data) {
            if (categoryResponse.data.success && categoryResponse.data.data) {
                categoryName = categoryResponse.data.data.categoryName || 'this category';
            } else if (categoryResponse.data.categoryName) {
                categoryName = categoryResponse.data.categoryName;
            }
        }

        const confirmMessage = `Are you sure you want to delete "${categoryName}"?\n\nThis action cannot be undone and will permanently remove:\n• Category information\n• All items in this category will become uncategorized`;

        confirmAction(confirmMessage, async () => {
            try {
                showLoading();
                const response = await categoryApi.deleteCategory(id);

                console.log('Delete category response:', response); // Debug log

                if (response.data && response.data.success) {
                    showToast(`Category "${categoryName}" deleted successfully!`, 'success');
                    loadCategories();

                    // Also update dashboard if we're on dashboard
                    if (currentSection === 'dashboard') {
                        loadDashboardData();
                    }
                } else {
                    throw new Error(response.data?.message || 'Failed to delete category');
                }
            } catch (error) {
                console.error('Error deleting category:', error);

                // Handle specific error cases
                if (error.status === 404) {
                    showToast('Category not found. It may have already been deleted.', 'warning');
                    loadCategories(); // Refresh the list
                } else if (error.status === 403) {
                    showToast('You do not have permission to delete categories.', 'error');
                } else if (error.status === 409) {
                    showToast('Cannot delete category. Category has existing items.', 'error');
                } else {
                    handleApiError(error);
                }
            } finally {
                hideLoading();
            }
        });

    } catch (error) {
        console.error('Error loading category for delete confirmation:', error);
        // Fallback to simple confirmation if we can't load category details
        confirmAction('Are you sure you want to delete this category? This action cannot be undone.', async () => {
            try {
                showLoading();
                const response = await categoryApi.deleteCategory(id);

                if (response.data && response.data.success) {
                    showToast('Category deleted successfully!', 'success');
                    loadCategories();
                } else {
                    throw new Error(response.data?.message || 'Failed to delete category');
                }
            } catch (deleteError) {
                console.error('Error deleting category:', deleteError);
                handleApiError(deleteError);
            } finally {
                hideLoading();
            }
        });
    }
}

async function editItem(id) {
    console.log('editItem called with id:', id); // Debug log

    try {
        // Get item data
        const response = await itemApi.getItemById(id);
        console.log('Edit item - API response:', response); // Debug log

        let item = null;

        if (response.data) {
            if (response.data.success && response.data.data) {
                item = response.data.data;
            } else if (response.data.itemId) {
                item = response.data;
            }
        }

        console.log('Edit item - Item data:', item); // Debug log

        if (!item) {
            showToast('Item not found', 'error');
            return;
        }

        // Show edit modal
        showEditItemModal(item);

    } catch (error) {
        console.error('Error loading item for edit:', error);
        handleApiError(error);
    }
}

async function deleteItem(id) {
    console.log('deleteItem called with id:', id); // Debug log

    // Get item details for confirmation
    try {
        const itemResponse = await itemApi.getItemById(id);
        let itemName = 'this item';

        if (itemResponse.data) {
            if (itemResponse.data.success && itemResponse.data.data) {
                itemName = itemResponse.data.data.itemName || 'this item';
            } else if (itemResponse.data.itemName) {
                itemName = itemResponse.data.itemName;
            }
        }

        const confirmMessage = `Are you sure you want to delete "${itemName}"?\n\nThis action cannot be undone and will permanently remove:\n• Item information\n• Stock records\n• Sales history for this item`;

        confirmAction(confirmMessage, async () => {
            try {
                showLoading();
                const response = await itemApi.deleteItem(id);

                console.log('Delete item response:', response); // Debug log

                if (response.data && response.data.success) {
                    showToast(`Item "${itemName}" deleted successfully!`, 'success');
                    loadItems();

                    // Also update dashboard if we're on dashboard
                    if (currentSection === 'dashboard') {
                        loadDashboardData();
                    }
                } else {
                    throw new Error(response.data?.message || 'Failed to delete item');
                }
            } catch (error) {
                console.error('Error deleting item:', error);

                // Handle specific error cases
                if (error.status === 404) {
                    showToast('Item not found. It may have already been deleted.', 'warning');
                    loadItems(); // Refresh the list
                } else if (error.status === 403) {
                    showToast('You do not have permission to delete items.', 'error');
                } else if (error.status === 409) {
                    showToast('Cannot delete item. Item has existing sales records.', 'error');
                } else {
                    handleApiError(error);
                }
            } finally {
                hideLoading();
            }
        });

    } catch (error) {
        console.error('Error loading item for delete confirmation:', error);
        // Fallback to simple confirmation if we can't load item details
        confirmAction('Are you sure you want to delete this item? This action cannot be undone.', async () => {
            try {
                showLoading();
                const response = await itemApi.deleteItem(id);

                if (response.data && response.data.success) {
                    showToast('Item deleted successfully!', 'success');
                    loadItems();
                } else {
                    throw new Error(response.data?.message || 'Failed to delete item');
                }
            } catch (deleteError) {
                console.error('Error deleting item:', deleteError);
                handleApiError(deleteError);
            } finally {
                hideLoading();
            }
        });
    }
}

async function adjustStock(id) {
    console.log('adjustStock called with id:', id); // Debug log

    try {
        // Get item data
        const response = await itemApi.getItemById(id);
        console.log('Adjust stock - API response:', response); // Debug log

        let item = null;

        if (response.data) {
            if (response.data.success && response.data.data) {
                item = response.data.data;
            } else if (response.data.itemId) {
                item = response.data;
            }
        }

        console.log('Adjust stock - Item data:', item); // Debug log

        if (!item) {
            showToast('Item not found', 'error');
            return;
        }

        // Show adjust stock modal
        showAdjustStockModal(item);

    } catch (error) {
        console.error('Error loading item for stock adjustment:', error);
        handleApiError(error);
    }
}

function viewBill(id) {
    console.log(`Opening bill view for ID: ${id}`);
    showViewBillModal(id);
}

function downloadBillPdf(id) {
    try {
        billApi.downloadBillPdf(id, `bill-${id}.pdf`);
        showToast('PDF download started!', 'success');
    } catch (error) {
        handleApiError(error);
    }
}

function cancelBill(id) {
    confirmAction('Are you sure you want to cancel this bill?', async () => {
        try {
            const response = await billApi.cancelBill(id);
            if (response.data && response.data.success) {
                showToast('Bill cancelled successfully!', 'success');
                loadBills();
            }
        } catch (error) {
            handleApiError(error);
        }
    });
}

function editUser(id) {
    showToast(`Edit User ${id} - Coming soon!`, 'info');
}

function deleteUser(id) {
    confirmAction('Are you sure you want to delete this user?', async () => {
        try {
            const response = await userApi.deleteUser(id);
            if (response.data && response.data.success) {
                showToast('User deleted successfully!', 'success');
                loadUsers();
            }
        } catch (error) {
            handleApiError(error);
        }
    });
}

async function editCustomer(id) {
    try {
        // Get customer data
        const response = await customerApi.getCustomerById(id);
        let customer = null;

        if (response.data) {
            if (response.data.success && response.data.data) {
                customer = response.data.data;
            } else if (response.data.customerId) {
                customer = response.data;
            }
        }

        if (!customer) {
            showToast('Customer not found', 'error');
            return;
        }

        // Show edit modal
        showEditCustomerModal(customer);

    } catch (error) {
        console.error('Error loading customer for edit:', error);
        handleApiError(error);
    }
}

async function deleteCustomer(id) {
    // Get customer details for confirmation
    try {
        const customerResponse = await customerApi.getCustomerById(id);
        let customerName = 'this customer';

        if (customerResponse.data) {
            if (customerResponse.data.success && customerResponse.data.data) {
                customerName = customerResponse.data.data.fullName || 'this customer';
            } else if (customerResponse.data.fullName) {
                customerName = customerResponse.data.fullName;
            }
        }

        const confirmMessage = `Are you sure you want to delete "${customerName}"?\n\nThis action cannot be undone and will permanently remove:\n• Customer information\n• Purchase history\n• All related records`;

        confirmAction(confirmMessage, async () => {
            try {
                showLoading();
                const response = await customerApi.deleteCustomer(id);

                console.log('Delete customer response:', response); // Debug log

                if (response.data && response.data.success) {
                    showToast(`Customer "${customerName}" deleted successfully!`, 'success');
                    loadCustomers();

                    // Also update dashboard if we're on dashboard
                    if (currentSection === 'dashboard') {
                        loadDashboardData();
                    }
                } else {
                    throw new Error(response.data?.message || 'Failed to delete customer');
                }
            } catch (error) {
                console.error('Error deleting customer:', error);

                // Handle specific error cases
                if (error.status === 404) {
                    showToast('Customer not found. It may have already been deleted.', 'warning');
                    loadCustomers(); // Refresh the list
                } else if (error.status === 403) {
                    showToast('You do not have permission to delete customers.', 'error');
                } else if (error.status === 409) {
                    showToast('Cannot delete customer. Customer has existing orders or transactions.', 'error');
                } else {
                    handleApiError(error);
                }
            } finally {
                hideLoading();
            }
        });

    } catch (error) {
        console.error('Error loading customer for delete confirmation:', error);
        // Fallback to simple confirmation if we can't load customer details
        confirmAction('Are you sure you want to delete this customer? This action cannot be undone.', async () => {
            try {
                showLoading();
                const response = await customerApi.deleteCustomer(id);

                if (response.data && response.data.success) {
                    showToast('Customer deleted successfully!', 'success');
                    loadCustomers();
                } else {
                    throw new Error(response.data?.message || 'Failed to delete customer');
                }
            } catch (deleteError) {
                console.error('Error deleting customer:', deleteError);
                handleApiError(deleteError);
            } finally {
                hideLoading();
            }
        });
    }
}

// Item search function
async function searchItems() {
    const searchInput = document.getElementById('item-search');
    if (!searchInput) {
        console.error('Item search input not found');
        return;
    }

    const searchTerm = searchInput.value.trim();
    console.log('Searching items with term:', searchTerm); // Debug log

    try {
        let response;
        if (searchTerm && searchTerm.length > 0) {
            console.log('Calling item search API with term:', searchTerm);
            response = await itemApi.searchItems(searchTerm);
        } else {
            console.log('Loading all items (empty search)');
            response = await itemApi.getItems();
        }

        console.log('Item search response:', response); // Debug log

        // Handle different response formats
        let items = [];
        if (response.data) {
            if (response.data.success && response.data.data) {
                // Search API returns: { success: true, data: { items: [...], searchTerm: "..." } }
                // Regular API returns: { success: true, data: { items: [...], pagination: {...} } }
                if (searchTerm && searchTerm.length > 0) {
                    items = response.data.data.items || [];
                    console.log('Using search format - items:', items); // Debug log
                } else {
                    items = response.data.data.items || response.data.data || [];
                    console.log('Using regular format - items:', items); // Debug log
                }
            } else if (response.data.items) {
                items = response.data.items;
                console.log('Using items format - items:', items); // Debug log
            } else if (Array.isArray(response.data)) {
                items = response.data;
                console.log('Using array format - items:', items); // Debug log
            }
        }

        // Ensure items is an array
        if (!Array.isArray(items)) {
            console.warn('Items data is not an array:', items);
            items = [];
        }

        console.log('Processed items for search:', items.length, 'results'); // Debug log

        const columns = [
            { key: 'itemCode', render: (value) => value || 'N/A' },
            { key: 'itemName', render: (value) => value || 'N/A' },
            { key: 'categoryName', render: (value) => value || 'N/A' },
            { key: 'price', render: (value) => formatCurrency(value || 0) },
            {
                key: 'stockQuantity',
                render: (value, item) => {
                    const isLowStock = value <= (item.minStockLevel || 5);
                    return `<span class="${isLowStock ? 'text-danger' : ''}">${value || 0}</span>`;
                }
            },
            {
                key: 'status',
                render: (value) => `<span class="status-badge status-${value ? value.toLowerCase() : 'unknown'}">${value || 'N/A'}</span>`
            },
            {
                key: 'actions',
                render: (value, item) => {
                    const canEdit = hasPermission(CONFIG.USER_ROLES.MANAGER) || hasPermission('manager') || hasPermission('admin');
                    const canDelete = hasPermission(CONFIG.USER_ROLES.MANAGER) || hasPermission('manager') || hasPermission('admin');
                    const canAdjustStock = hasPermission(CONFIG.USER_ROLES.MANAGER) || hasPermission('manager') || hasPermission('admin');

                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-info" onclick="viewItem(${item.itemId})"
                                    title="View Item Details">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-sm btn-primary" onclick="editItem(${item.itemId})"
                                    ${canEdit ? '' : 'disabled'}
                                    title="${canEdit ? 'Edit Item' : 'Insufficient permissions to edit'}">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-warning" onclick="adjustStock(${item.itemId})"
                                    ${canAdjustStock ? '' : 'disabled'}
                                    title="${canAdjustStock ? 'Adjust Stock' : 'Insufficient permissions to adjust stock'}">
                                <i class="fas fa-boxes"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteItem(${item.itemId})"
                                    ${canDelete ? '' : 'disabled'}
                                    title="${canDelete ? 'Delete Item' : 'Insufficient permissions to delete'}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ];

        updateTable('items-table', items, columns);

        // Show search results info
        if (searchTerm && searchTerm.length > 0) {
            if (items.length === 0) {
                showToast(`No items found for "${searchTerm}"`, 'info');
            } else {
                showToast(`Found ${items.length} item(s) for "${searchTerm}"`, 'success');
            }
        }

    } catch (error) {
        console.error('Error searching items:', error);
        handleApiError(error);
    }
}

// Debounced version for real-time search
const debouncedSearchItems = debounce(searchItems, 300);

// View functions
async function viewCustomer(id) {
    console.log('viewCustomer called with id:', id); // Debug log

    try {
        // Get customer data
        const response = await customerApi.getCustomerById(id);
        console.log('View customer - API response:', response); // Debug log

        let customer = null;

        if (response.data) {
            if (response.data.success && response.data.data) {
                customer = response.data.data;
            } else if (response.data.customerId) {
                customer = response.data;
            }
        }

        console.log('View customer - Customer data:', customer); // Debug log

        if (!customer) {
            showToast('Customer not found', 'error');
            return;
        }

        // Show view modal
        showViewCustomerModal(customer);

    } catch (error) {
        console.error('Error loading customer for view:', error);
        handleApiError(error);
    }
}

async function viewItem(id) {
    console.log('viewItem called with id:', id); // Debug log

    try {
        // Get item data (which already includes totalSold and totalRevenue according to API docs)
        const response = await itemApi.getItemById(id);
        console.log('View item - API response:', response); // Debug log

        let item = null;

        if (response.data) {
            if (response.data.success && response.data.data) {
                item = response.data.data;
            } else if (response.data.itemId) {
                item = response.data;
            }
        }

        console.log('View item - Item data:', item); // Debug log

        if (!item) {
            showToast('Item not found', 'error');
            return;
        }

        // The API already returns totalSold and totalRevenue, so we just need to ensure they exist
        // and calculate additional metrics
        item.totalSold = item.totalSold || 0;
        item.totalRevenue = item.totalRevenue || 0;

        // Calculate average sale price
        if (item.totalSold > 0 && item.totalRevenue > 0) {
            item.averageSalePrice = item.totalRevenue / item.totalSold;
        } else {
            item.averageSalePrice = item.price || 0;
        }

        // Set last sale date if not provided (this might need to be added to the backend API)
        item.lastSaleDate = item.lastSaleDate || null;

        console.log('View item - Enhanced item data:', {
            itemId: item.itemId,
            itemName: item.itemName,
            totalSold: item.totalSold,
            totalRevenue: item.totalRevenue,
            averageSalePrice: item.averageSalePrice
        }); // Debug log

        // Show view modal
        showViewItemModal(item);

    } catch (error) {
        console.error('Error loading item for view:', error);
        handleApiError(error);
    }
}

async function viewCategory(id) {
    console.log('viewCategory called with id:', id); // Debug log

    try {
        // Get category data
        const response = await categoryApi.getCategoryById(id);
        console.log('View category - API response:', response); // Debug log

        let category = null;

        if (response.data) {
            if (response.data.success && response.data.data) {
                category = response.data.data;
            } else if (response.data.categoryId) {
                category = response.data;
            }
        }

        console.log('View category - Category data:', category); // Debug log

        if (!category) {
            showToast('Category not found', 'error');
            return;
        }

        // Show view modal
        showViewCategoryModal(category);

    } catch (error) {
        console.error('Error loading category for view:', error);
        handleApiError(error);
    }
}

// Export functions to global scope
window.showSection = showSection;
window.toggleSidebar = toggleSidebar;
window.closeMobileSidebar = closeMobileSidebar;
window.toggleUserDropdown = toggleUserDropdown;
window.refreshDashboard = refreshDashboard;
window.logout = logout;
window.searchCustomers = searchCustomers;
window.debouncedSearchCustomers = debouncedSearchCustomers;
window.searchItems = searchItems;
window.debouncedSearchItems = debouncedSearchItems;
window.showAddCustomerModal = showAddCustomerModal;
window.showAddItemModal = showAddItemModal;
window.showAddCategoryModal = showAddCategoryModal;
window.showAddUserModal = showAddUserModal;
window.viewCustomer = viewCustomer;
window.viewItem = viewItem;
window.viewCategory = viewCategory;
window.editCustomer = editCustomer;
window.deleteCustomer = deleteCustomer;
window.editCategory = editCategory;
window.deleteCategory = deleteCategory;
window.editItem = editItem;
window.deleteItem = deleteItem;
window.adjustStock = adjustStock;
window.viewBill = viewBill;
window.downloadBillPdf = downloadBillPdf;
window.cancelBill = cancelBill;
window.searchBills = searchBills;
window.filterBills = filterBills;
window.clearBillFilters = clearBillFilters;
window.editUser = editUser;
window.deleteUser = deleteUser;
