// Modal functionality for the application

// Modal utility functions
function createModal(title, content, size = 'medium') {
    const modalId = 'modal-' + generateId();
    const sizeClass = size === 'large' ? 'modal-lg' : size === 'small' ? 'modal-sm' : '';
    
    const modalHTML = `
        <div class="modal-overlay" id="${modalId}" onclick="closeModal('${modalId}')">
            <div class="modal-dialog ${sizeClass}" onclick="event.stopPropagation()">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="modal-title">${title}</h3>
                        <button class="modal-close" onclick="closeModal('${modalId}')">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                    <div class="modal-body">
                        ${content}
                    </div>
                </div>
            </div>
        </div>
    `;
    
    const container = document.getElementById('modal-container');
    if (container) {
        container.innerHTML = modalHTML;
        
        // Add modal styles if not already present
        addModalStyles();
        
        // Show modal with animation
        setTimeout(() => {
            const modal = document.getElementById(modalId);
            if (modal) {
                modal.classList.add('show');
            }
        }, 10);
    }
    
    return modalId;
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            modal.remove();
        }, 300);
    }
}

function addModalStyles() {
    if (document.getElementById('modal-styles')) return;
    
    const styles = `
        <style id="modal-styles">
            .modal-overlay {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.5);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 10000;
                opacity: 0;
                transition: opacity 0.3s ease;
            }
            
            .modal-overlay.show {
                opacity: 1;
            }
            
            .modal-dialog {
                background: white;
                border-radius: 10px;
                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
                max-width: 500px;
                width: 90%;
                max-height: 90vh;
                overflow-y: auto;
                transform: translateY(-50px);
                transition: transform 0.3s ease;
            }
            
            .modal-overlay.show .modal-dialog {
                transform: translateY(0);
            }
            
            .modal-dialog.modal-sm { max-width: 300px; }
            .modal-dialog.modal-lg { max-width: 800px; }
            
            .modal-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 20px;
                border-bottom: 1px solid #e9ecef;
                background: linear-gradient(135deg, #3498db, #2980b9);
                color: white;
                border-radius: 10px 10px 0 0;
            }
            
            .modal-title {
                margin: 0;
                font-size: 1.25rem;
                font-weight: 600;
            }
            
            .modal-close {
                background: none;
                border: none;
                color: white;
                font-size: 1.2rem;
                cursor: pointer;
                padding: 5px;
                border-radius: 3px;
                transition: background-color 0.3s ease;
            }
            
            .modal-close:hover {
                background-color: rgba(255, 255, 255, 0.2);
            }
            
            .modal-body {
                padding: 20px;
            }
            
            .form-group {
                margin-bottom: 15px;
            }
            
            .form-group label {
                display: block;
                margin-bottom: 5px;
                font-weight: 500;
                color: #2c3e50;
            }
            
            .form-group input,
            .form-group select,
            .form-group textarea {
                width: 100%;
                padding: 10px;
                border: 2px solid #e9ecef;
                border-radius: 5px;
                font-size: 14px;
                transition: border-color 0.3s ease;
            }
            
            .form-group input:focus,
            .form-group select:focus,
            .form-group textarea:focus {
                outline: none;
                border-color: #3498db;
            }
            
            .form-group textarea {
                resize: vertical;
                min-height: 80px;
            }
            
            .form-row {
                display: flex;
                gap: 15px;
            }
            
            .form-row .form-group {
                flex: 1;
            }
            
            .modal-footer {
                display: flex;
                justify-content: flex-end;
                gap: 10px;
                padding: 20px;
                border-top: 1px solid #e9ecef;
                background-color: #f8f9fa;
                border-radius: 0 0 10px 10px;
            }
            
            .error-message {
                color: #e74c3c;
                font-size: 12px;
                margin-top: 5px;
            }
            
            .form-group input.invalid,
            .form-group select.invalid,
            .form-group textarea.invalid {
                border-color: #e74c3c;
            }
        </style>
    `;
    
    document.head.insertAdjacentHTML('beforeend', styles);
}

// Customer Modal
function showAddCustomerModal() {
    const content = `
        <form id="customer-form">
            <div class="form-row">
                <div class="form-group">
                    <label for="accountNo">Account Number</label>
                    <div style="display: flex; gap: 10px; align-items: flex-end;">
                        <input type="text" id="accountNo" name="accountNo" readonly style="flex: 1;">
                        <button type="button" class="btn btn-sm btn-secondary" onclick="generateAccountNumber()">
                            <i class="fas fa-refresh"></i> Generate
                        </button>
                    </div>
                </div>
                <div class="form-group">
                    <label for="fullName">Full Name *</label>
                    <input type="text" id="fullName" name="fullName" required>
                </div>
            </div>
            
            <div class="form-group">
                <label for="address">Address *</label>
                <textarea id="address" name="address" required></textarea>
            </div>
            
            <div class="form-row">
                <div class="form-group">
                    <label for="phone">Phone *</label>
                    <input type="tel" id="phone" name="phone" required>
                </div>
                <div class="form-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email">
                </div>
            </div>
            
            <div class="form-row">
                <div class="form-group">
                    <label for="dateOfBirth">Date of Birth</label>
                    <input type="date" id="dateOfBirth" name="dateOfBirth">
                </div>
                <div class="form-group">
                    <label for="gender">Gender</label>
                    <select id="gender" name="gender">
                        <option value="">Select Gender</option>
                        <option value="male">Male</option>
                        <option value="female">Female</option>
                        <option value="other">Other</option>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label for="status">Status</label>
                <select id="status" name="status">
                    <option value="active" selected>Active</option>
                    <option value="inactive">Inactive</option>
                </select>
            </div>
            
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                    Cancel
                </button>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save"></i> Save Customer
                </button>
            </div>
        </form>
    `;
    
    const modalId = createModal('Add New Customer', content, 'large');

    // Add form submit handler
    const form = document.getElementById('customer-form');
    if (form) {
        form.addEventListener('submit', handleCustomerSubmit);
    }

    // Generate account number automatically after modal is created
    setTimeout(() => {
        generateAccountNumber();
    }, 100);

    return modalId;
}

async function generateAccountNumber() {
    try {
        showLoading();
        const response = await customerApi.generateAccountNumber();
        console.log('Generate Account Number Response:', response); // Debug log

        const accountNoField = document.getElementById('accountNo');
        if (accountNoField) {
            if (response.data) {
                // Handle different response formats
                let accountNo = null;
                if (response.data.success && response.data.data && response.data.data.accountNo) {
                    accountNo = response.data.data.accountNo;
                } else if (response.data.accountNo) {
                    accountNo = response.data.accountNo;
                } else if (typeof response.data === 'string') {
                    accountNo = response.data;
                }

                if (accountNo) {
                    accountNoField.value = accountNo;
                    showToast('Account number generated successfully!', 'success');
                } else {
                    throw new Error('No account number in response');
                }
            } else {
                throw new Error('Invalid response format');
            }
        } else {
            console.error('Account number field not found');
        }
    } catch (error) {
        console.error('Error generating account number:', error);
        showToast('Error generating account number: ' + (error.message || 'Unknown error'), 'error');
    } finally {
        hideLoading();
    }
}

async function handleCustomerSubmit(e) {
    e.preventDefault();

    const form = e.target;
    const formData = getFormData(form);

    // Clear previous errors
    clearFieldErrors();

    // Validate form
    if (!validateCustomerForm(formData)) {
        return;
    }

    // Transform data to match backend expectations
    const customerData = {
        accountNo: formData.accountNo,
        fullName: formData.fullName,
        address: formData.address,
        phone: formData.phone,
        email: formData.email || null,
        dateOfBirth: formData.dateOfBirth || null,
        gender: formData.gender ? formData.gender.toUpperCase() : null,
        status: formData.status ? formData.status.toUpperCase() : 'ACTIVE'
    };

    // Remove empty values
    Object.keys(customerData).forEach(key => {
        if (customerData[key] === '' || customerData[key] === undefined) {
            customerData[key] = null;
        }
    });

    console.log('Creating customer with data:', customerData); // Debug log

    try {
        const response = await customerApi.createCustomer(customerData);

        if (response.data && response.data.success) {
            showToast('Customer created successfully!', 'success');
            closeCurrentModal();

            // Reload customers if on customers page
            if (currentSection === 'customers') {
                loadCustomers();
            }
        } else {
            throw new Error(response.data?.message || 'Failed to create customer');
        }

    } catch (error) {
        console.error('Error creating customer:', error);
        console.error('Error details:', error.data); // Debug log
        handleApiError(error);

        // Show field-specific errors if available
        if (error.data && error.data.errors) {
            Object.keys(error.data.errors).forEach(field => {
                showFieldError(field, error.data.errors[field]);
            });
        }
    }
}

function validateCustomerForm(formData) {
    let isValid = true;
    
    // Required fields
    if (!formData.fullName || formData.fullName.trim() === '') {
        showFieldError('fullName', 'Full name is required');
        isValid = false;
    }
    
    if (!formData.address || formData.address.trim() === '') {
        showFieldError('address', 'Address is required');
        isValid = false;
    }
    
    if (!formData.phone || formData.phone.trim() === '') {
        showFieldError('phone', 'Phone is required');
        isValid = false;
    } else if (!validatePhone(formData.phone)) {
        showFieldError('phone', 'Invalid phone number format');
        isValid = false;
    }
    
    // Optional email validation
    if (formData.email && !validateEmail(formData.email)) {
        showFieldError('email', 'Invalid email format');
        isValid = false;
    }
    
    return isValid;
}

// Category Modal
function showAddCategoryModal() {
    console.log('showAddCategoryModal called'); // Debug log

    const content = `
        <form id="category-form">
            <div class="form-group">
                <label for="categoryName">Category Name *</label>
                <input type="text" id="categoryName" name="categoryName" required placeholder="Enter category name">
            </div>

            <div class="form-group">
                <label for="description">Description</label>
                <textarea id="description" name="description" placeholder="Enter category description (optional)"></textarea>
            </div>

            <div class="form-group">
                <label for="status">Status</label>
                <select id="status" name="status">
                    <option value="active" selected>Active</option>
                    <option value="inactive">Inactive</option>
                </select>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                    Cancel
                </button>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save"></i> Save Category
                </button>
            </div>
        </form>
    `;

    console.log('Creating category modal...'); // Debug log
    const modalId = createModal('Add New Category', content);

    // Add form submit handler
    setTimeout(() => {
        const form = document.getElementById('category-form');
        if (form) {
            console.log('Category form found, adding event listener'); // Debug log
            form.addEventListener('submit', handleCategorySubmit);
        } else {
            console.error('Category form not found!'); // Debug log
        }
    }, 100);

    return modalId;
}

async function handleCategorySubmit(e) {
    e.preventDefault();

    const form = e.target;
    const formData = getFormData(form);

    // Clear previous errors
    clearFieldErrors();

    // Validate form
    if (!formData.categoryName || formData.categoryName.trim() === '') {
        showFieldError('categoryName', 'Category name is required');
        return;
    }

    // Transform data to match backend expectations
    const categoryData = {
        categoryName: formData.categoryName.trim(),
        description: formData.description ? formData.description.trim() : null,
        status: formData.status ? formData.status.toUpperCase() : 'ACTIVE'
    };

    // Remove empty values
    Object.keys(categoryData).forEach(key => {
        if (categoryData[key] === '' || categoryData[key] === undefined) {
            categoryData[key] = null;
        }
    });

    console.log('Creating category with data:', categoryData); // Debug log

    try {
        const response = await categoryApi.createCategory(categoryData);

        console.log('Create category response:', response); // Debug log

        if (response.data && response.data.success) {
            showToast('Category created successfully!', 'success');
            closeCurrentModal();

            // Reload categories if on categories page
            if (currentSection === 'categories') {
                loadCategories();
            }

            // Also update dashboard if we're on dashboard
            if (currentSection === 'dashboard') {
                loadDashboardData();
            }
        } else {
            throw new Error(response.data?.message || 'Failed to create category');
        }

    } catch (error) {
        console.error('Error creating category:', error);
        console.error('Error details:', error.data); // Debug log
        handleApiError(error);

        // Show field-specific errors if available
        if (error.data && error.data.errors) {
            Object.keys(error.data.errors).forEach(field => {
                showFieldError(field, error.data.errors[field]);
            });
        }
    }
}

// Utility function to get current modal ID
function getCurrentModalId() {
    const modal = document.querySelector('.modal-overlay');
    return modal ? modal.id : null;
}

// Helper function to close current modal
function closeCurrentModal() {
    const modalId = getCurrentModalId();
    if (modalId) {
        closeModal(modalId);
    }
}

// Edit Customer Modal
function showEditCustomerModal(customer) {
    const content = `
        <form id="edit-customer-form">
            <input type="hidden" id="customerId" name="customerId" value="${customer.customerId}">

            <div class="form-row">
                <div class="form-group">
                    <label for="accountNo">Account Number</label>
                    <input type="text" id="accountNo" name="accountNo" value="${customer.accountNo || ''}" readonly>
                </div>
                <div class="form-group">
                    <label for="fullName">Full Name *</label>
                    <input type="text" id="fullName" name="fullName" value="${customer.fullName || ''}" required>
                </div>
            </div>

            <div class="form-group">
                <label for="address">Address *</label>
                <textarea id="address" name="address" required>${customer.address || ''}</textarea>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="phone">Phone *</label>
                    <input type="tel" id="phone" name="phone" value="${customer.phone || ''}" required>
                </div>
                <div class="form-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" value="${customer.email || ''}">
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="dateOfBirth">Date of Birth</label>
                    <input type="date" id="dateOfBirth" name="dateOfBirth" value="${customer.dateOfBirth || ''}">
                </div>
                <div class="form-group">
                    <label for="gender">Gender</label>
                    <select id="gender" name="gender">
                        <option value="">Select Gender</option>
                        <option value="male" ${customer.gender === 'MALE' ? 'selected' : ''}>Male</option>
                        <option value="female" ${customer.gender === 'FEMALE' ? 'selected' : ''}>Female</option>
                        <option value="other" ${customer.gender === 'OTHER' ? 'selected' : ''}>Other</option>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label for="status">Status</label>
                <select id="status" name="status">
                    <option value="active" ${customer.status === 'ACTIVE' ? 'selected' : ''}>Active</option>
                    <option value="inactive" ${customer.status === 'INACTIVE' ? 'selected' : ''}>Inactive</option>
                </select>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                    Cancel
                </button>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save"></i> Update Customer
                </button>
            </div>
        </form>
    `;

    const modalId = createModal('Edit Customer', content, 'large');

    // Add form submit handler
    const form = document.getElementById('edit-customer-form');
    if (form) {
        form.addEventListener('submit', handleEditCustomerSubmit);
    }

    return modalId;
}

async function handleEditCustomerSubmit(e) {
    e.preventDefault();

    const form = e.target;
    const formData = getFormData(form);
    const customerId = formData.customerId;

    // Remove customerId from formData as it's not needed in the request body
    delete formData.customerId;

    // Clear previous errors
    clearFieldErrors();

    // Validate form
    if (!validateCustomerForm(formData)) {
        return;
    }

    // Transform data to match backend expectations
    const customerData = {
        accountNo: formData.accountNo,
        fullName: formData.fullName,
        address: formData.address,
        phone: formData.phone,
        email: formData.email || null,
        dateOfBirth: formData.dateOfBirth || null,
        gender: formData.gender ? formData.gender.toUpperCase() : null,
        status: formData.status ? formData.status.toUpperCase() : 'ACTIVE'
    };

    // Remove empty values
    Object.keys(customerData).forEach(key => {
        if (customerData[key] === '' || customerData[key] === undefined) {
            customerData[key] = null;
        }
    });

    console.log('Sending customer data:', customerData); // Debug log

    try {
        const response = await customerApi.updateCustomer(customerId, customerData);

        if (response.data && response.data.success) {
            showToast('Customer updated successfully!', 'success');
            closeCurrentModal();

            // Reload customers if on customers page
            if (currentSection === 'customers') {
                loadCustomers();
            }
        } else {
            throw new Error(response.data?.message || 'Failed to update customer');
        }

    } catch (error) {
        console.error('Error updating customer:', error);
        console.error('Error details:', error.data); // Debug log
        handleApiError(error);

        // Show field-specific errors if available
        if (error.data && error.data.errors) {
            Object.keys(error.data.errors).forEach(field => {
                showFieldError(field, error.data.errors[field]);
            });
        }
    }
}

// Edit Category Modal
function showEditCategoryModal(category) {
    console.log('showEditCategoryModal called with:', category); // Debug log

    const content = `
        <form id="edit-category-form">
            <input type="hidden" id="categoryId" name="categoryId" value="${category.categoryId}">

            <div class="form-group">
                <label for="categoryName">Category Name *</label>
                <input type="text" id="categoryName" name="categoryName" value="${category.categoryName || ''}" required placeholder="Enter category name">
            </div>

            <div class="form-group">
                <label for="description">Description</label>
                <textarea id="description" name="description" placeholder="Enter category description (optional)">${category.description || ''}</textarea>
            </div>

            <div class="form-group">
                <label for="status">Status</label>
                <select id="status" name="status">
                    <option value="active" ${category.status === 'ACTIVE' ? 'selected' : ''}>Active</option>
                    <option value="inactive" ${category.status === 'INACTIVE' ? 'selected' : ''}>Inactive</option>
                </select>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                    Cancel
                </button>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save"></i> Update Category
                </button>
            </div>
        </form>
    `;

    console.log('Creating edit category modal...'); // Debug log
    const modalId = createModal('Edit Category', content);

    // Add form submit handler
    setTimeout(() => {
        const form = document.getElementById('edit-category-form');
        if (form) {
            console.log('Edit category form found, adding event listener'); // Debug log
            form.addEventListener('submit', handleEditCategorySubmit);
        } else {
            console.error('Edit category form not found!'); // Debug log
        }
    }, 100);

    return modalId;
}

async function handleEditCategorySubmit(e) {
    e.preventDefault();

    const form = e.target;
    const formData = getFormData(form);
    const categoryId = formData.categoryId;

    // Remove categoryId from formData as it's not needed in the request body
    delete formData.categoryId;

    // Clear previous errors
    clearFieldErrors();

    // Validate form
    if (!formData.categoryName || formData.categoryName.trim() === '') {
        showFieldError('categoryName', 'Category name is required');
        return;
    }

    // Transform data to match backend expectations
    const categoryData = {
        categoryName: formData.categoryName.trim(),
        description: formData.description ? formData.description.trim() : null,
        status: formData.status ? formData.status.toUpperCase() : 'ACTIVE'
    };

    // Remove empty values
    Object.keys(categoryData).forEach(key => {
        if (categoryData[key] === '' || categoryData[key] === undefined) {
            categoryData[key] = null;
        }
    });

    console.log('Updating category with data:', categoryData); // Debug log

    try {
        const response = await categoryApi.updateCategory(categoryId, categoryData);

        console.log('Update category response:', response); // Debug log

        if (response.data && response.data.success) {
            showToast('Category updated successfully!', 'success');
            closeCurrentModal();

            // Reload categories if on categories page
            if (currentSection === 'categories') {
                loadCategories();
            }

            // Also update dashboard if we're on dashboard
            if (currentSection === 'dashboard') {
                loadDashboardData();
            }
        } else {
            throw new Error(response.data?.message || 'Failed to update category');
        }

    } catch (error) {
        console.error('Error updating category:', error);
        console.error('Error details:', error.data); // Debug log
        handleApiError(error);

        // Show field-specific errors if available
        if (error.data && error.data.errors) {
            Object.keys(error.data.errors).forEach(field => {
                showFieldError(field, error.data.errors[field]);
            });
        }
    }
}

// Add Item Modal
function showAddItemModal() {
    console.log('showAddItemModal called'); // Debug log

    const content = `
        <form id="item-form">
            <div class="form-row">
                <div class="form-group">
                    <label for="itemCode">Item Code</label>
                    <div style="display: flex; gap: 10px; align-items: flex-end;">
                        <input type="text" id="itemCode" name="itemCode" readonly style="flex: 1;">
                        <button type="button" class="btn btn-sm btn-secondary" onclick="generateItemCode()">
                            <i class="fas fa-refresh"></i> Generate
                        </button>
                    </div>
                </div>
                <div class="form-group">
                    <label for="itemName">Item Name *</label>
                    <input type="text" id="itemName" name="itemName" required placeholder="Enter item name">
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="categoryId">Category *</label>
                    <select id="categoryId" name="categoryId" required>
                        <option value="">Select Category</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="barcode">Barcode</label>
                    <input type="text" id="barcode" name="barcode" placeholder="Enter barcode (optional)">
                </div>
            </div>

            <div class="form-group">
                <label for="description">Description</label>
                <textarea id="description" name="description" placeholder="Enter item description (optional)"></textarea>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="price">Selling Price *</label>
                    <input type="number" id="price" name="price" step="0.01" min="0" required placeholder="0.00">
                </div>
                <div class="form-group">
                    <label for="costPrice">Cost Price</label>
                    <input type="number" id="costPrice" name="costPrice" step="0.01" min="0" placeholder="0.00">
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="stockQuantity">Initial Stock</label>
                    <input type="number" id="stockQuantity" name="stockQuantity" min="0" value="0" placeholder="0">
                </div>
                <div class="form-group">
                    <label for="unit">Unit</label>
                    <select id="unit" name="unit">
                        <option value="piece" selected>Piece</option>
                        <option value="box">Box</option>
                        <option value="pack">Pack</option>
                        <option value="set">Set</option>
                        <option value="kg">Kilogram</option>
                        <option value="liter">Liter</option>
                    </select>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="minStockLevel">Min Stock Level</label>
                    <input type="number" id="minStockLevel" name="minStockLevel" min="0" value="5" placeholder="5">
                </div>
                <div class="form-group">
                    <label for="maxStockLevel">Max Stock Level</label>
                    <input type="number" id="maxStockLevel" name="maxStockLevel" min="0" value="1000" placeholder="1000">
                </div>
            </div>

            <!-- Book-specific fields -->
            <div class="form-section">
                <h4>Book Information (Optional)</h4>
                <div class="form-row">
                    <div class="form-group">
                        <label for="isbn">ISBN</label>
                        <input type="text" id="isbn" name="isbn" placeholder="Enter ISBN">
                    </div>
                    <div class="form-group">
                        <label for="author">Author</label>
                        <input type="text" id="author" name="author" placeholder="Enter author name">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="publisher">Publisher</label>
                        <input type="text" id="publisher" name="publisher" placeholder="Enter publisher">
                    </div>
                    <div class="form-group">
                        <label for="publicationYear">Publication Year</label>
                        <input type="number" id="publicationYear" name="publicationYear" min="1900" max="2030" placeholder="2024">
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label for="status">Status</label>
                <select id="status" name="status">
                    <option value="active" selected>Active</option>
                    <option value="inactive">Inactive</option>
                    <option value="discontinued">Discontinued</option>
                </select>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                    Cancel
                </button>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save"></i> Save Item
                </button>
            </div>
        </form>
    `;

    console.log('Creating item modal...'); // Debug log
    const modalId = createModal('Add New Item', content, 'large');

    // Load categories and setup form
    setTimeout(async () => {
        await loadCategoriesForSelect();
        generateItemCode();

        const form = document.getElementById('item-form');
        if (form) {
            console.log('Item form found, adding event listener'); // Debug log
            form.addEventListener('submit', handleItemSubmit);
        } else {
            console.error('Item form not found!'); // Debug log
        }

        // Add event listener for category change to regenerate item code
        const categorySelect = document.getElementById('categoryId');
        if (categorySelect) {
            categorySelect.addEventListener('change', function() {
                if (this.value) {
                    generateItemCode();
                }
            });
        }
    }, 100);

    return modalId;
}

// Load categories for the select dropdown
async function loadCategoriesForSelect() {
    try {
        const response = await categoryApi.getActiveCategories();
        console.log('Categories for select:', response); // Debug log

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

        const categorySelect = document.getElementById('categoryId');
        if (categorySelect && categories.length > 0) {
            // Clear existing options except the first one
            categorySelect.innerHTML = '<option value="">Select Category</option>';

            categories.forEach(category => {
                if (category.status === 'ACTIVE' || category.status === 'active') {
                    const option = document.createElement('option');
                    option.value = category.categoryId;
                    option.textContent = category.categoryName;
                    categorySelect.appendChild(option);
                }
            });

            console.log('Loaded', categories.length, 'categories for select');
        } else {
            console.warn('No categories found or category select not found');
        }
    } catch (error) {
        console.error('Error loading categories for select:', error);
        showToast('Error loading categories', 'error');
    }
}

// Generate item code
async function generateItemCode() {
    try {
        const categorySelect = document.getElementById('categoryId');
        const categoryId = categorySelect ? categorySelect.value : null;

        if (!categoryId) {
            // If no category selected, generate a generic code
            const itemCodeField = document.getElementById('itemCode');
            if (itemCodeField) {
                itemCodeField.value = 'ITEM' + Date.now().toString().slice(-6);
            }
            return;
        }

        showLoading();
        const response = await itemApi.generateItemCode(categoryId);
        console.log('Generate Item Code Response:', response); // Debug log

        const itemCodeField = document.getElementById('itemCode');
        if (itemCodeField) {
            if (response.data) {
                let itemCode = null;
                if (response.data.success && response.data.data && response.data.data.itemCode) {
                    itemCode = response.data.data.itemCode;
                } else if (response.data.itemCode) {
                    itemCode = response.data.itemCode;
                } else if (typeof response.data === 'string') {
                    itemCode = response.data;
                }

                if (itemCode) {
                    itemCodeField.value = itemCode;
                    showToast('Item code generated successfully!', 'success');
                } else {
                    throw new Error('No item code in response');
                }
            } else {
                throw new Error('Invalid response format');
            }
        }
    } catch (error) {
        console.error('Error generating item code:', error);
        showToast('Error generating item code: ' + (error.message || 'Unknown error'), 'error');

        // Fallback to manual code generation
        const itemCodeField = document.getElementById('itemCode');
        if (itemCodeField) {
            itemCodeField.value = 'ITEM' + Date.now().toString().slice(-6);
        }
    } finally {
        hideLoading();
    }
}

// Create Bill Modal
function showCreateBillModal() {
    console.log('showCreateBillModal function called from webapp modals.js');
    const content = `
        <form id="bill-form">
            <!-- Customer Selection -->
            <div class="form-group">
                <label for="customerId">Customer *</label>
                <div class="customer-selection">
                    <select id="customerId" name="customerId" required>
                        <option value="">Select Customer</option>
                    </select>
                    <button type="button" class="btn btn-sm btn-secondary" onclick="refreshCustomers()" style="margin-left: 10px;">
                        <i class="fas fa-refresh"></i>
                    </button>
                </div>
            </div>

            <!-- Bill Details -->
            <div class="form-row">
                <div class="form-group">
                    <label for="billNumber">Bill Number</label>
                    <input type="text" id="billNumber" name="billNumber" readonly>
                    <button type="button" class="btn btn-sm btn-secondary" onclick="generateBillNumber()" style="margin-top: 5px;">
                        Generate
                    </button>
                </div>
                <div class="form-group">
                    <label for="billDate">Bill Date *</label>
                    <input type="date" id="billDate" name="billDate" required>
                </div>
            </div>

            <!-- Items Section -->
            <div class="bill-items-section">
                <div class="section-header">
                    <h4>Items</h4>
                    <div>
                        <button type="button" class="btn btn-sm btn-primary" onclick="addBillItem()">
                            <i class="fas fa-plus"></i> Add Item
                        </button>

                    </div>
                </div>
                <div id="bill-items-container">
                    <!-- Bill items will be added here -->
                </div>
            </div>

            <!-- Totals Section -->
            <div class="bill-totals-section">
                <div class="form-row">
                    <div class="form-group">
                        <label for="discountPercentage">Discount (%)</label>
                        <input type="number" id="discountPercentage" name="discountPercentage"
                               min="0" max="100" step="0.01" value="0" onchange="calculateTotals()">
                    </div>
                    <div class="form-group">
                        <label for="taxPercentage">Tax (%)</label>
                        <input type="number" id="taxPercentage" name="taxPercentage"
                               min="0" max="100" step="0.01" value="0" onchange="calculateTotals()">
                    </div>
                </div>

                <div class="totals-display">
                    <div class="total-row">
                        <span>Subtotal:</span>
                        <span id="subtotal-display">LKR 0.00</span>
                    </div>
                    <div class="total-row">
                        <span>Discount:</span>
                        <span id="discount-display">LKR 0.00</span>
                    </div>
                    <div class="total-row">
                        <span>Tax:</span>
                        <span id="tax-display">LKR 0.00</span>
                    </div>
                    <div class="total-row total-final">
                        <span>Total:</span>
                        <span id="total-display">LKR 0.00</span>
                    </div>
                </div>
            </div>

            <!-- Payment Details -->
            <div class="form-row">
                <div class="form-group">
                    <label for="paymentMethod">Payment Method *</label>
                    <select id="paymentMethod" name="paymentMethod" required>
                        <option value="CASH">Cash</option>
                        <option value="CARD">Card</option>
                        <option value="BANK_TRANSFER">Bank Transfer</option>
                        <option value="CHEQUE">Cheque</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="paymentStatus">Payment Status *</label>
                    <select id="paymentStatus" name="paymentStatus" required>
                        <option value="PAID">Paid</option>
                        <option value="PENDING">Pending</option>
                        <option value="PARTIAL">Partial</option>
                    </select>
                </div>
            </div>

            <!-- Notes -->
            <div class="form-group">
                <label for="notes">Notes</label>
                <textarea id="notes" name="notes" rows="3"></textarea>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                    Cancel
                </button>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save"></i> Create Bill
                </button>
            </div>
        </form>
    `;

    const modalId = createModal('Create New Bill', content, 'large');

    // Initialize the form
    initializeBillForm();

    return modalId;
}

// Bill form initialization and helper functions
async function initializeBillForm() {
    try {
        console.log('Initializing bill form...');

        // Set current date
        const today = new Date().toISOString().split('T')[0];
        const billDateField = document.getElementById('billDate');
        if (billDateField) {
            billDateField.value = today;
            console.log('Set bill date to:', today);
        }

        // Load customers
        console.log('Loading customers...');
        await loadCustomersForBill();

        // Generate bill number
        console.log('Generating bill number...');
        await generateBillNumber();

        // Add initial item row
        console.log('Adding initial item row...');
        addBillItem();

        // Add form submit handler
        const form = document.getElementById('bill-form');
        if (form) {
            form.addEventListener('submit', handleBillSubmit);
            console.log('Added form submit handler');
        } else {
            console.error('Bill form not found');
        }

        console.log('Bill form initialization completed');

    } catch (error) {
        console.error('Error initializing bill form:', error);
        showToast('Error initializing bill form: ' + (error.message || 'Unknown error'), 'error');
    }
}

async function loadCustomersForBill() {
    try {
        console.log('Loading customers for bill...');
        const response = await customerApi.getCustomers(1, 100); // Get first 100 customers
        console.log('Customers API response:', response);

        const customerSelect = document.getElementById('customerId');
        if (!customerSelect) {
            console.error('Customer select element not found');
            return;
        }

        let customers = [];

        // Handle different response structures
        if (response.data) {
            if (response.data.success && response.data.data) {
                customers = response.data.data.items || response.data.data || [];
            } else if (response.data.items) {
                customers = response.data.items;
            } else if (Array.isArray(response.data)) {
                customers = response.data;
            }
        }

        console.log('Processed customers:', customers);

        // Clear existing options except the first one
        customerSelect.innerHTML = '<option value="">Select Customer</option>';

        if (customers.length > 0) {
            customers.forEach(customer => {
                const option = document.createElement('option');
                option.value = customer.customerId;
                option.textContent = `${customer.accountNo || customer.accountNumber || 'N/A'} - ${customer.fullName || customer.name || 'Unknown'}`;
                customerSelect.appendChild(option);
            });
            console.log(`Loaded ${customers.length} customers`);
        } else {
            console.warn('No customers found');
            showToast('No customers found. Please add customers first.', 'warning');
        }

    } catch (error) {
        console.error('Error loading customers:', error);
        showToast('Error loading customers: ' + (error.message || 'Unknown error'), 'error');
    }
}

async function generateBillNumber() {
    try {
        console.log('Generating bill number...');
        const response = await billApi.generateBillNumber();
        console.log('Bill number API response:', response);

        const billNumberField = document.getElementById('billNumber');
        if (!billNumberField) {
            console.error('Bill number field not found');
            return;
        }

        let billNumber = null;

        // Handle different response structures
        if (response.data) {
            if (response.data.success && response.data.data) {
                billNumber = response.data.data.billNumber || response.data.data;
            } else if (response.data.billNumber) {
                billNumber = response.data.billNumber;
            } else if (typeof response.data === 'string') {
                billNumber = response.data;
            }
        }

        if (billNumber) {
            billNumberField.value = billNumber;
            console.log('Generated bill number:', billNumber);
        } else {
            console.warn('No bill number received from API');
            // Generate a fallback bill number
            const timestamp = Date.now();
            billNumber = `BILL${timestamp}`;
            billNumberField.value = billNumber;
            showToast('Using fallback bill number', 'warning');
        }

    } catch (error) {
        console.error('Error generating bill number:', error);
        showToast('Error generating bill number: ' + (error.message || 'Unknown error'), 'error');

        // Generate a fallback bill number
        const timestamp = Date.now();
        const billNumberField = document.getElementById('billNumber');
        if (billNumberField) {
            billNumberField.value = `BILL${timestamp}`;
        }
    }
}

async function refreshCustomers() {
    await loadCustomersForBill();
    showToast('Customers refreshed', 'success');
}

// Debug function to test item API directly
async function debugItemApi() {
    try {
        console.log('=== DEBUG: Testing Item API ===');
        const response = await itemApi.getItems(1, 10);
        console.log('Raw API response:', response);

        if (response.data) {
            console.log('Response data:', response.data);
            if (response.data.success) {
                console.log('Success response, data:', response.data.data);
            }
        }

        showToast('Item API debug completed - check console', 'info');
    } catch (error) {
        console.error('Item API debug error:', error);
        showToast('Item API debug failed: ' + error.message, 'error');
    }
}

// Bill item management functions
let billItemCounter = 0;

function addBillItem() {
    billItemCounter++;
    const container = document.getElementById('bill-items-container');
    if (!container) return;

    const itemRow = document.createElement('div');
    itemRow.className = 'bill-item-row';
    itemRow.id = `bill-item-${billItemCounter}`;

    itemRow.innerHTML = `
        <div class="item-row-content">
            <div class="form-group item-select">
                <label>Item *</label>
                <select name="itemId" required onchange="onItemSelect(this, ${billItemCounter})">
                    <option value="">Select Item</option>
                </select>
            </div>
            <div class="form-group item-quantity">
                <label>Quantity *</label>
                <input type="number" name="quantity" min="1" step="1" required
                       onchange="calculateItemTotal(${billItemCounter})">
            </div>
            <div class="form-group item-price">
                <label>Unit Price</label>
                <input type="number" name="unitPrice" min="0" step="0.01" readonly>
            </div>
            <div class="form-group item-discount">
                <label>Discount (%)</label>
                <input type="number" name="discountPercentage" min="0" max="100" step="0.01" value="0"
                       onchange="calculateItemTotal(${billItemCounter})">
            </div>
            <div class="form-group item-total">
                <label>Line Total</label>
                <input type="text" name="lineTotal" readonly>
            </div>
            <div class="form-group item-actions">
                <button type="button" class="btn btn-sm btn-danger" onclick="removeBillItem(${billItemCounter})">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `;

    container.appendChild(itemRow);

    // Load items for this row
    loadItemsForBillRow(billItemCounter);
}

async function loadItemsForBillRow(rowId) {
    try {
        console.log(`Loading items for row ${rowId}...`);
        const response = await itemApi.getItems(1, 100); // Get first 100 items
        console.log('Items API response:', response);

        const itemSelect = document.querySelector(`#bill-item-${rowId} select[name="itemId"]`);
        if (!itemSelect) {
            console.error(`Item select element not found for row ${rowId}`);
            return;
        }

        let items = [];

        // Handle different response structures
        if (response.data) {
            if (response.data.success && response.data.data) {
                items = response.data.data.items || response.data.data || [];
            } else if (response.data.items) {
                items = response.data.items;
            } else if (Array.isArray(response.data)) {
                items = response.data;
            }
        }

        console.log('Processed items:', items);
        console.log('Sample item structure:', items[0]);

        // Clear existing options except the first one
        itemSelect.innerHTML = '<option value="">Select Item</option>';

        if (items.length > 0) {
            // First, let's see what statuses we have
            const statuses = [...new Set(items.map(item => item.status))];
            console.log('Available item statuses:', statuses);

            // Check stock quantities
            const stockInfo = items.map(item => ({
                id: item.itemId || item.id,
                code: item.itemCode || item.code,
                stock: item.stockQuantity || item.stock || 0,
                status: item.status
            }));
            console.log('Stock information:', stockInfo);

            // More flexible filtering - show all active items, even with 0 stock for debugging
            let availableItems = items.filter(item => {
                const isActive = item.status === 'active' || item.status === 'Active' || item.status === 'ACTIVE';
                return isActive;
            });

            console.log('Active items (before stock filter):', availableItems.length);

            // If no active items, show all items for debugging
            if (availableItems.length === 0) {
                console.warn('No active items found, showing all items for debugging');
                availableItems = items;
            }

            // Further filter by stock if we have active items
            const itemsWithStock = availableItems.filter(item => {
                const stock = item.stockQuantity || item.stock || 0;
                return stock > 0;
            });

            console.log('Items with stock:', itemsWithStock.length);

            // Use items with stock if available, otherwise use all active items
            const finalItems = itemsWithStock.length > 0 ? itemsWithStock : availableItems;

            finalItems.forEach(item => {
                const option = document.createElement('option');
                option.value = item.itemId || item.id;
                const itemCode = item.itemCode || item.code || 'N/A';
                const itemName = item.itemName || item.name || 'Unknown';
                const stockQuantity = item.stockQuantity || item.stock || 0;
                const price = item.price || item.unitPrice || item.sellingPrice || 0;

                option.textContent = `${itemCode} - ${itemName} (Stock: ${stockQuantity})`;
                option.dataset.price = price;
                option.dataset.stock = stockQuantity;
                itemSelect.appendChild(option);
            });

            console.log(`Loaded ${finalItems.length} items for row ${rowId}`);

            if (finalItems.length === 0) {
                showToast('No items available', 'warning');
            } else if (itemsWithStock.length === 0 && finalItems.length > 0) {
                showToast('Items loaded but no stock available', 'warning');
            }
        } else {
            console.warn('No items found in API response');
            showToast('No items found. Please add items first.', 'warning');
        }

    } catch (error) {
        console.error('Error loading items:', error);
        showToast('Error loading items: ' + (error.message || 'Unknown error'), 'error');
    }
}

function onItemSelect(selectElement, rowId) {
    const selectedOption = selectElement.options[selectElement.selectedIndex];
    const priceInput = document.querySelector(`#bill-item-${rowId} input[name="unitPrice"]`);
    const quantityInput = document.querySelector(`#bill-item-${rowId} input[name="quantity"]`);

    if (selectedOption.value && priceInput) {
        priceInput.value = selectedOption.dataset.price || 0;

        // Set max quantity based on stock
        if (quantityInput) {
            quantityInput.max = selectedOption.dataset.stock || 999;
            quantityInput.value = 1; // Default quantity
        }

        calculateItemTotal(rowId);
    }
}

function calculateItemTotal(rowId) {
    const row = document.getElementById(`bill-item-${rowId}`);
    if (!row) return;

    const quantity = parseFloat(row.querySelector('input[name="quantity"]').value) || 0;
    const unitPrice = parseFloat(row.querySelector('input[name="unitPrice"]').value) || 0;
    const discountPercentage = parseFloat(row.querySelector('input[name="discountPercentage"]').value) || 0;

    const subtotal = quantity * unitPrice;
    const discountAmount = subtotal * (discountPercentage / 100);
    const lineTotal = subtotal - discountAmount;

    row.querySelector('input[name="lineTotal"]').value = CONFIG.formatCurrency(lineTotal);

    // Recalculate bill totals
    calculateTotals();
}

function removeBillItem(rowId) {
    const row = document.getElementById(`bill-item-${rowId}`);
    if (row) {
        row.remove();
        calculateTotals();
    }
}

// Bill calculation functions
function calculateTotals() {
    const itemRows = document.querySelectorAll('.bill-item-row');
    let subtotal = 0;

    // Calculate subtotal from all items
    itemRows.forEach(row => {
        const quantity = parseFloat(row.querySelector('input[name="quantity"]').value) || 0;
        const unitPrice = parseFloat(row.querySelector('input[name="unitPrice"]').value) || 0;
        const discountPercentage = parseFloat(row.querySelector('input[name="discountPercentage"]').value) || 0;

        const itemSubtotal = quantity * unitPrice;
        const itemDiscount = itemSubtotal * (discountPercentage / 100);
        const lineTotal = itemSubtotal - itemDiscount;

        subtotal += lineTotal;
    });

    // Get bill-level discount and tax
    const billDiscountPercentage = parseFloat(document.getElementById('discountPercentage').value) || 0;
    const billTaxPercentage = parseFloat(document.getElementById('taxPercentage').value) || 0;

    // Calculate bill-level discount and tax
    const billDiscountAmount = subtotal * (billDiscountPercentage / 100);
    const afterDiscount = subtotal - billDiscountAmount;
    const billTaxAmount = afterDiscount * (billTaxPercentage / 100);
    const totalAmount = afterDiscount + billTaxAmount;

    // Update display
    document.getElementById('subtotal-display').textContent = CONFIG.formatCurrency(subtotal);
    document.getElementById('discount-display').textContent = CONFIG.formatCurrency(billDiscountAmount);
    document.getElementById('tax-display').textContent = CONFIG.formatCurrency(billTaxAmount);
    document.getElementById('total-display').textContent = CONFIG.formatCurrency(totalAmount);
}

// Bill form submission
async function handleBillSubmit(e) {
    e.preventDefault();

    console.log('Bill form submitted');

    const form = e.target;
    const formData = getFormData(form);
    console.log('Form data:', formData);

    // Clear previous errors
    clearFieldErrors();

    // Validate form
    if (!validateBillForm(formData)) {
        console.log('Form validation failed');
        return;
    }

    // Prepare bill data
    const billData = prepareBillData(formData);
    console.log('Prepared bill data:', billData);

    // Show loading state
    const submitButton = form.querySelector('button[type="submit"]');
    const originalText = submitButton.innerHTML;
    submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating Bill...';
    submitButton.disabled = true;

    try {
        console.log('Sending bill creation request...');
        const response = await billApi.createBill(billData);
        console.log('Bill creation response:', response);

        // Handle different response structures
        let success = false;
        let billId = null;
        let billNumber = null;

        if (response.data) {
            if (response.data.success) {
                success = true;
                if (response.data.data) {
                    billId = response.data.data.billId || response.data.data.id;
                    billNumber = response.data.data.billNumber || response.data.data.number;
                }
            } else if (response.data.billId || response.data.id) {
                // Direct response without success wrapper
                success = true;
                billId = response.data.billId || response.data.id;
                billNumber = response.data.billNumber || response.data.number;
            }
        }

        if (success) {
            showToast('Bill created successfully!', 'success');
            closeCurrentModal();

            // Reload bills if on bills page
            if (typeof currentSection !== 'undefined' && currentSection === 'bills') {
                loadBills();
            }

            // Optionally show the created bill
            if (billId) {
                setTimeout(() => {
                    if (confirm('Bill created successfully! Would you like to download the PDF?')) {
                        downloadBillPdf(billId, billNumber || 'bill');
                    }
                }, 500);
            }
        } else {
            throw new Error('Bill creation failed: Invalid response structure');
        }

    } catch (error) {
        console.error('Error creating bill:', error);

        let errorMessage = 'Failed to create bill';
        if (error.message) {
            errorMessage += ': ' + error.message;
        }

        showToast(errorMessage, 'error');

        // Show field-specific errors if available
        if (error.data && error.data.errors) {
            Object.keys(error.data.errors).forEach(field => {
                showFieldError(field, error.data.errors[field]);
            });
        }

        // Handle API errors
        if (typeof handleApiError === 'function') {
            handleApiError(error);
        }

    } finally {
        // Restore button state
        submitButton.innerHTML = originalText;
        submitButton.disabled = false;
    }
}

// Bill form validation
function validateBillForm(formData) {
    let isValid = true;

    // Validate customer
    if (!formData.customerId) {
        showFieldError('customerId', 'Customer is required');
        isValid = false;
    }

    // Validate bill date
    if (!formData.billDate) {
        showFieldError('billDate', 'Bill date is required');
        isValid = false;
    }

    // Validate items
    const itemRows = document.querySelectorAll('.bill-item-row');
    if (itemRows.length === 0) {
        showToast('At least one item is required', 'error');
        isValid = false;
    }

    let hasValidItems = false;
    itemRows.forEach((row, index) => {
        const itemId = row.querySelector('select[name="itemId"]').value;
        const quantity = row.querySelector('input[name="quantity"]').value;
        const unitPrice = row.querySelector('input[name="unitPrice"]').value;

        if (itemId && quantity && unitPrice) {
            hasValidItems = true;

            // Validate quantity
            if (parseInt(quantity) <= 0) {
                showToast(`Item ${index + 1}: Quantity must be greater than 0`, 'error');
                isValid = false;
            }

            // Validate stock availability
            const selectElement = row.querySelector('select[name="itemId"]');
            const selectedOption = selectElement.options[selectElement.selectedIndex];
            const availableStock = parseInt(selectedOption.dataset.stock) || 0;

            if (parseInt(quantity) > availableStock) {
                showToast(`Item ${index + 1}: Quantity exceeds available stock (${availableStock})`, 'error');
                isValid = false;
            }
        }
    });

    if (!hasValidItems) {
        showToast('At least one valid item is required', 'error');
        isValid = false;
    }

    return isValid;
}

// Prepare bill data for API
function prepareBillData(formData) {
    console.log('Preparing bill data from form data:', formData);

    const billItems = [];
    const itemRows = document.querySelectorAll('.bill-item-row');

    itemRows.forEach((row, index) => {
        const itemId = row.querySelector('select[name="itemId"]').value;
        const quantity = row.querySelector('input[name="quantity"]').value;
        const unitPrice = row.querySelector('input[name="unitPrice"]').value;
        const discountPercentage = row.querySelector('input[name="discountPercentage"]').value || 0;

        console.log(`Item ${index + 1}:`, { itemId, quantity, unitPrice, discountPercentage });

        if (itemId && quantity && unitPrice) {
            billItems.push({
                itemId: parseInt(itemId),
                quantity: parseInt(quantity),
                unitPrice: parseFloat(unitPrice),
                discountPercentage: parseFloat(discountPercentage)
            });
        }
    });

    // Use enum names as expected by Jackson
    const paymentMethod = formData.paymentMethod || 'CASH';
    const paymentStatus = formData.paymentStatus || 'PAID';

    const billData = {
        bill: {
            billNumber: formData.billNumber,
            customerId: parseInt(formData.customerId),
            billDate: formData.billDate,
            discountPercentage: parseFloat(formData.discountPercentage) || 0,
            taxPercentage: parseFloat(formData.taxPercentage) || 0,
            paymentMethod: paymentMethod,
            paymentStatus: paymentStatus,
            notes: formData.notes || ''
        },
        billItems: billItems
    };

    console.log('Final bill data prepared:', billData);
    return billData;
}

// View Bill Modal with PDF Preview
async function showViewBillModal(billId) {
    try {
        console.log(`Loading bill details for ID: ${billId}`);

        // Show loading modal first
        const loadingContent = `
            <div class="loading-container">
                <div class="loading-spinner">
                    <i class="fas fa-spinner fa-spin"></i>
                </div>
                <p>Loading bill details...</p>
            </div>
        `;

        const modalId = createModal('Loading Bill...', loadingContent, 'large');

        // Load bill details
        const response = await billApi.getBillById(billId);
        console.log('Bill details response:', response);

        let bill = null;
        if (response.data) {
            if (response.data.success && response.data.data) {
                bill = response.data.data;
            } else if (response.data.billId || response.data.id) {
                bill = response.data;
            }
        }

        if (!bill) {
            throw new Error('Bill not found or invalid response structure');
        }

        console.log('Loaded bill:', bill);

        // Create the bill details content
        const content = createBillDetailsContent(bill);

        // Update the modal with bill details
        updateModalContent(modalId, `Bill Details - ${bill.billNumber || bill.id}`, content);

        // Initialize PDF preview
        initializePdfPreview(billId);

    } catch (error) {
        console.error('Error loading bill details:', error);
        showToast('Error loading bill details: ' + (error.message || 'Unknown error'), 'error');

        // Show error in modal
        const errorContent = `
            <div class="error-container">
                <div class="error-icon">
                    <i class="fas fa-exclamation-triangle"></i>
                </div>
                <h3>Error Loading Bill</h3>
                <p>${error.message || 'Unable to load bill details. Please try again.'}</p>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                        Close
                    </button>
                </div>
            </div>
        `;

        createModal('Error', errorContent, 'medium');
    }
}

function createBillDetailsContent(bill) {
    const billItemsHtml = bill.billItems && bill.billItems.length > 0
        ? bill.billItems.map(item => `
            <tr>
                <td>${escapeHtml(item.itemCode || item.code || 'N/A')}</td>
                <td>${escapeHtml(item.itemName || item.name || 'N/A')}</td>
                <td>${item.quantity || 0}</td>
                <td>${CONFIG.formatCurrency(item.unitPrice || 0)}</td>
                <td>${item.discountPercentage || 0}%</td>
                <td>${CONFIG.formatCurrency(item.lineTotal || (item.quantity * item.unitPrice * (1 - (item.discountPercentage || 0) / 100)))}</td>
            </tr>
        `).join('')
        : '<tr><td colspan="6" class="text-center">No items found</td></tr>';

    return `
        <div class="bill-details-container">
            <!-- Bill Header -->
            <div class="bill-header">
                <div class="bill-info-grid">
                    <div class="bill-info-item">
                        <label>Bill Number:</label>
                        <span class="value">${escapeHtml(bill.billNumber || bill.id || 'N/A')}</span>
                    </div>
                    <div class="bill-info-item">
                        <label>Date:</label>
                        <span class="value">${CONFIG.formatDate(bill.billDate || bill.createdAt)}</span>
                    </div>
                    <div class="bill-info-item">
                        <label>Customer:</label>
                        <span class="value">${escapeHtml(bill.customerName || bill.customer?.fullName || 'N/A')}</span>
                    </div>
                    <div class="bill-info-item">
                        <label>Status:</label>
                        <span class="status-badge status-${(bill.status || 'active').toLowerCase()}">
                            ${(bill.status || 'active').toUpperCase()}
                        </span>
                    </div>
                </div>
            </div>

            <!-- Bill Content Tabs -->
            <div class="bill-tabs">
                <div class="tab-buttons">
                    <button class="tab-button active" onclick="switchBillTab('details', ${bill.billId || bill.id})">
                        <i class="fas fa-list"></i> Details
                    </button>
                    <button class="tab-button" onclick="switchBillTab('pdf', ${bill.billId || bill.id})">
                        <i class="fas fa-file-pdf"></i> PDF Preview
                    </button>
                </div>

                <!-- Details Tab -->
                <div id="bill-tab-details" class="tab-content active">
                    <div class="bill-items">
                        <h4>Items</h4>
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Code</th>
                                        <th>Name</th>
                                        <th>Qty</th>
                                        <th>Unit Price</th>
                                        <th>Discount</th>
                                        <th>Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${billItemsHtml}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="bill-totals">
                        <div class="totals-grid">
                            <div class="total-row">
                                <span>Subtotal:</span>
                                <span>${CONFIG.formatCurrency(bill.subtotal || 0)}</span>
                            </div>
                            <div class="total-row">
                                <span>Discount (${bill.discountPercentage || 0}%):</span>
                                <span>${CONFIG.formatCurrency(bill.discountAmount || 0)}</span>
                            </div>
                            <div class="total-row">
                                <span>Tax (${bill.taxPercentage || 0}%):</span>
                                <span>${CONFIG.formatCurrency(bill.taxAmount || 0)}</span>
                            </div>
                            <div class="total-row total-final">
                                <span>Total Amount:</span>
                                <span>${CONFIG.formatCurrency(bill.totalAmount || 0)}</span>
                            </div>
                        </div>
                    </div>

                    <div class="bill-payment">
                        <div class="payment-grid">
                            <div class="payment-info-item">
                                <label>Payment Method:</label>
                                <span class="value">${(bill.paymentMethod || 'cash').toUpperCase()}</span>
                            </div>
                            <div class="payment-info-item">
                                <label>Payment Status:</label>
                                <span class="status-badge status-${(bill.paymentStatus || 'paid').toLowerCase()}">
                                    ${(bill.paymentStatus || 'paid').toUpperCase()}
                                </span>
                            </div>
                        </div>
                        ${bill.notes ? `
                            <div class="notes-section">
                                <label>Notes:</label>
                                <p class="notes-content">${escapeHtml(bill.notes)}</p>
                            </div>
                        ` : ''}
                    </div>
                </div>

                <!-- PDF Preview Tab -->
                <div id="bill-tab-pdf" class="tab-content">
                    <div class="pdf-preview-container">
                        <div class="pdf-toolbar">
                            <button class="btn btn-sm btn-primary" onclick="downloadCurrentBillPdf()">
                                <i class="fas fa-download"></i> Download PDF
                            </button>
                            <button class="btn btn-sm btn-secondary" onclick="refreshPdfPreview()">
                                <i class="fas fa-refresh"></i> Refresh
                            </button>
                        </div>
                        <div id="pdf-preview-content">
                            <div class="pdf-loading">
                                <i class="fas fa-spinner fa-spin"></i>
                                <p>Loading PDF preview...</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Modal Footer -->
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="downloadCurrentBillPdf()">
                    <i class="fas fa-download"></i> Download PDF
                </button>
                ${(bill.status || 'active').toLowerCase() === 'active' ? `
                    <button type="button" class="btn btn-danger" onclick="cancelCurrentBill()">
                        <i class="fas fa-ban"></i> Cancel Bill
                    </button>
                ` : ''}
                <button type="button" class="btn btn-primary" onclick="closeCurrentModal()">
                    Close
                </button>
            </div>
        </div>
    `;
}

// Tab switching functionality
let currentBillId = null;

function switchBillTab(tabName, billId) {
    currentBillId = billId;

    // Update tab buttons
    document.querySelectorAll('.tab-button').forEach(btn => btn.classList.remove('active'));
    document.querySelector(`.tab-button[onclick*="${tabName}"]`).classList.add('active');

    // Update tab content
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    document.getElementById(`bill-tab-${tabName}`).classList.add('active');

    // Load PDF preview if switching to PDF tab
    if (tabName === 'pdf') {
        loadPdfPreview(billId);
    }
}

// PDF Preview functionality
async function initializePdfPreview(billId) {
    currentBillId = billId;
    // PDF will be loaded when user switches to PDF tab
}

async function loadPdfPreview(billId) {
    const pdfContainer = document.getElementById('pdf-preview-content');
    if (!pdfContainer) return;

    try {
        pdfContainer.innerHTML = `
            <div class="pdf-loading">
                <i class="fas fa-spinner fa-spin"></i>
                <p>Loading PDF preview...</p>
            </div>
        `;

        // Get PDF URL
        const pdfUrl = `${CONFIG.API_BASE_URL}/bills/${billId}/pdf`;
        console.log('Loading PDF from:', pdfUrl);

        // Create PDF embed/iframe
        const pdfEmbed = `
            <div class="pdf-viewer">
                <iframe
                    src="${pdfUrl}"
                    width="100%"
                    height="600px"
                    style="border: 1px solid #ddd; border-radius: 5px;"
                    onload="onPdfLoaded()"
                    onerror="onPdfError()">
                    <p>Your browser doesn't support PDF viewing.
                       <a href="${pdfUrl}" target="_blank">Click here to download the PDF</a>
                    </p>
                </iframe>
            </div>
        `;

        pdfContainer.innerHTML = pdfEmbed;

    } catch (error) {
        console.error('Error loading PDF preview:', error);
        pdfContainer.innerHTML = `
            <div class="pdf-error">
                <i class="fas fa-exclamation-triangle"></i>
                <h4>PDF Preview Error</h4>
                <p>Unable to load PDF preview. You can still download the PDF.</p>
                <button class="btn btn-primary" onclick="downloadCurrentBillPdf()">
                    <i class="fas fa-download"></i> Download PDF
                </button>
            </div>
        `;
    }
}

function onPdfLoaded() {
    console.log('PDF loaded successfully');
}

function onPdfError() {
    console.error('PDF loading failed');
    const pdfContainer = document.getElementById('pdf-preview-content');
    if (pdfContainer) {
        pdfContainer.innerHTML = `
            <div class="pdf-error">
                <i class="fas fa-exclamation-triangle"></i>
                <h4>PDF Preview Not Available</h4>
                <p>PDF preview is not supported in your browser or the PDF couldn't be loaded.</p>
                <button class="btn btn-primary" onclick="downloadCurrentBillPdf()">
                    <i class="fas fa-download"></i> Download PDF Instead
                </button>
            </div>
        `;
    }
}

async function refreshPdfPreview() {
    if (currentBillId) {
        await loadPdfPreview(currentBillId);
        showToast('PDF preview refreshed', 'success');
    }
}

async function downloadCurrentBillPdf() {
    if (currentBillId) {
        try {
            await billApi.downloadBillPdf(currentBillId, `bill-${currentBillId}.pdf`);
            showToast('PDF download started!', 'success');
        } catch (error) {
            console.error('Error downloading PDF:', error);
            showToast('Error downloading PDF: ' + (error.message || 'Unknown error'), 'error');
        }
    }
}

async function cancelCurrentBill() {
    if (currentBillId) {
        if (confirm('Are you sure you want to cancel this bill? This action cannot be undone.')) {
            try {
                const response = await billApi.cancelBill(currentBillId);
                if (response.data && response.data.success) {
                    showToast('Bill cancelled successfully!', 'success');
                    closeCurrentModal();

                    // Reload bills if on bills page
                    if (typeof currentSection !== 'undefined' && currentSection === 'bills') {
                        loadBills();
                    }
                }
            } catch (error) {
                console.error('Error cancelling bill:', error);
                showToast('Error cancelling bill: ' + (error.message || 'Unknown error'), 'error');
            }
        }
    }
}

// Utility function to update modal content
function updateModalContent(modalId, title, content) {
    const modal = document.getElementById(modalId);
    if (modal) {
        const titleElement = modal.querySelector('.modal-title');
        const contentElement = modal.querySelector('.modal-content');

        if (titleElement) titleElement.textContent = title;
        if (contentElement) contentElement.innerHTML = content;
    }
}

// Export functions to global scope
window.createModal = createModal;
window.closeModal = closeModal;
window.closeCurrentModal = closeCurrentModal;
window.showAddCustomerModal = showAddCustomerModal;
window.showEditCustomerModal = showEditCustomerModal;
window.showAddCategoryModal = showAddCategoryModal;
window.showEditCategoryModal = showEditCategoryModal;
window.showAddItemModal = showAddItemModal;
window.showEditItemModal = showEditItemModal;
window.showAdjustStockModal = showAdjustStockModal;
window.showViewCustomerModal = showViewCustomerModal;
window.showViewItemModal = showViewItemModal;
window.showViewCategoryModal = showViewCategoryModal;
window.showCreateBillModal = showCreateBillModal;
window.generateAccountNumber = generateAccountNumber;
window.generateItemCode = generateItemCode;
window.generateBillNumber = generateBillNumber;
window.refreshCustomers = refreshCustomers;
window.addBillItem = addBillItem;
window.removeBillItem = removeBillItem;
window.onItemSelect = onItemSelect;
window.calculateItemTotal = calculateItemTotal;
window.calculateTotals = calculateTotals;
window.debugItemApi = debugItemApi;
window.showViewBillModal = showViewBillModal;
window.switchBillTab = switchBillTab;
window.refreshPdfPreview = refreshPdfPreview;
window.downloadCurrentBillPdf = downloadCurrentBillPdf;
window.cancelCurrentBill = cancelCurrentBill;
window.updateAdjustmentUI = updateAdjustmentUI;
window.calculateNewStock = calculateNewStock;
window.toggleCustomReason = toggleCustomReason;

// Helper functions for item view calculations
function calculateProfitMargin(sellingPrice, costPrice) {
    if (!sellingPrice || !costPrice || costPrice === 0) {
        return 'N/A';
    }

    const margin = ((sellingPrice - costPrice) / sellingPrice) * 100;
    return margin.toFixed(1);
}

function calculateStockTurnover(totalSold, currentStock) {
    if (!totalSold || totalSold === 0) {
        return '<span class="turnover turnover-none">No Sales</span>';
    }

    if (!currentStock || currentStock === 0) {
        return '<span class="turnover turnover-out">Out of Stock</span>';
    }

    const turnover = totalSold / currentStock;
    if (turnover >= 10) {
        return '<span class="turnover turnover-high">High</span>';
    } else if (turnover >= 3) {
        return '<span class="turnover turnover-medium">Medium</span>';
    } else if (turnover >= 1) {
        return '<span class="turnover turnover-low">Low</span>';
    } else {
        return '<span class="turnover turnover-very-low">Very Low</span>';
    }
}

window.calculateProfitMargin = calculateProfitMargin;
window.calculateStockTurnover = calculateStockTurnover;
window.showProfileModal = showProfileModal;
window.switchProfileTab = switchProfileTab;
window.resetProfilePasswordForm = resetProfilePasswordForm;
window.togglePasswordVisibility = togglePasswordVisibility;

// View Customer Modal
function showViewCustomerModal(customer) {
    console.log('showViewCustomerModal called with:', customer); // Debug log

    const content = `
        <div class="view-details">
            <div class="detail-section">
                <h4><i class="fas fa-user"></i> Customer Information</h4>
                <div class="detail-grid">
                    <div class="detail-item">
                        <label>Account Number:</label>
                        <span>${customer.accountNo || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Full Name:</label>
                        <span>${customer.fullName || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Phone:</label>
                        <span>${customer.phone || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Email:</label>
                        <span>${customer.email || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Address:</label>
                        <span>${customer.address || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Date of Birth:</label>
                        <span>${customer.dateOfBirth ? formatDate(customer.dateOfBirth) : 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Gender:</label>
                        <span>${customer.gender || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Status:</label>
                        <span class="status-badge status-${customer.status ? customer.status.toLowerCase() : 'unknown'}">${customer.status || 'N/A'}</span>
                    </div>
                </div>
            </div>

            <div class="detail-section">
                <h4><i class="fas fa-shopping-cart"></i> Purchase Summary</h4>
                <div class="detail-grid">
                    <div class="detail-item">
                        <label>Total Purchases:</label>
                        <span class="amount">${formatCurrency(customer.totalPurchases || 0)}</span>
                    </div>
                    <div class="detail-item">
                        <label>Total Orders:</label>
                        <span>${customer.totalOrders || 0}</span>
                    </div>
                    <div class="detail-item">
                        <label>Last Purchase:</label>
                        <span>${customer.lastPurchaseDate ? formatDate(customer.lastPurchaseDate) : 'Never'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Customer Since:</label>
                        <span>${customer.createdAt ? formatDate(customer.createdAt) : 'N/A'}</span>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                    Close
                </button>
                <button type="button" class="btn btn-primary" onclick="editCustomer(${customer.customerId})">
                    <i class="fas fa-edit"></i> Edit Customer
                </button>
            </div>
        </div>
    `;

    const modalId = createModal('Customer Details', content, 'large');
    return modalId;
}

// View Item Modal
function showViewItemModal(item) {
    console.log('showViewItemModal called with:', item); // Debug log

    const isLowStock = (item.stockQuantity || 0) <= (item.minStockLevel || 5);
    const stockClass = isLowStock ? 'low-stock' : 'normal-stock';

    const content = `
        <div class="view-details">
            <div class="detail-section">
                <h4><i class="fas fa-box"></i> Item Information</h4>
                <div class="detail-grid">
                    <div class="detail-item">
                        <label>Item Code:</label>
                        <span class="code">${item.itemCode || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Item Name:</label>
                        <span>${item.itemName || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Category:</label>
                        <span>${item.categoryName || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Barcode:</label>
                        <span class="code">${item.barcode || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Description:</label>
                        <span>${item.description || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Status:</label>
                        <span class="status-badge status-${item.status ? item.status.toLowerCase() : 'unknown'}">${item.status || 'N/A'}</span>
                    </div>
                </div>
            </div>

            <div class="detail-section">
                <h4><i class="fas fa-coins"></i> Pricing & Stock</h4>
                <div class="detail-grid">
                    <div class="detail-item">
                        <label>Selling Price:</label>
                        <span class="amount">${formatCurrency(item.price || 0)}</span>
                    </div>
                    <div class="detail-item">
                        <label>Cost Price:</label>
                        <span class="amount">${formatCurrency(item.costPrice || 0)}</span>
                    </div>
                    <div class="detail-item">
                        <label>Current Stock:</label>
                        <span class="${stockClass}">${item.stockQuantity || 0} ${item.unit || 'pieces'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Min Stock Level:</label>
                        <span>${item.minStockLevel || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Max Stock Level:</label>
                        <span>${item.maxStockLevel || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Unit:</label>
                        <span>${item.unit || 'N/A'}</span>
                    </div>
                </div>
            </div>

            <div class="detail-section">
                <h4><i class="fas fa-chart-line"></i> Sales Performance</h4>
                <div class="detail-grid">
                    <div class="detail-item">
                        <label>Total Sold:</label>
                        <span class="sales-quantity">${item.totalSold || 0} ${item.unit || 'pieces'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Total Revenue:</label>
                        <span class="amount">${formatCurrency(item.totalRevenue || 0)}</span>
                    </div>
                    <div class="detail-item">
                        <label>Average Sale Price:</label>
                        <span class="amount">${formatCurrency(item.averageSalePrice || 0)}</span>
                    </div>
                    <div class="detail-item">
                        <label>Last Sale Date:</label>
                        <span>${item.lastSaleDate ? formatDate(item.lastSaleDate) : 'Never'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Profit Margin:</label>
                        <span class="profit-margin">${calculateProfitMargin(item.averageSalePrice || item.price, item.costPrice)}%</span>
                    </div>
                    <div class="detail-item">
                        <label>Stock Turnover:</label>
                        <span>${calculateStockTurnover(item.totalSold, item.stockQuantity)}</span>
                    </div>
                </div>
            </div>

            ${item.isbn || item.author || item.publisher || item.publicationYear ? `
            <div class="detail-section">
                <h4><i class="fas fa-book"></i> Book Information</h4>
                <div class="detail-grid">
                    <div class="detail-item">
                        <label>ISBN:</label>
                        <span class="code">${item.isbn || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Author:</label>
                        <span>${item.author || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Publisher:</label>
                        <span>${item.publisher || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Publication Year:</label>
                        <span>${item.publicationYear || 'N/A'}</span>
                    </div>
                </div>
            </div>
            ` : ''}

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                    Close
                </button>
                <button type="button" class="btn btn-warning" onclick="adjustStock(${item.itemId})">
                    <i class="fas fa-boxes"></i> Adjust Stock
                </button>
                <button type="button" class="btn btn-primary" onclick="editItem(${item.itemId})">
                    <i class="fas fa-edit"></i> Edit Item
                </button>
            </div>
        </div>
    `;

    const modalId = createModal('Item Details', content, 'large');
    return modalId;
}

// View Category Modal
function showViewCategoryModal(category) {
    console.log('showViewCategoryModal called with:', category); // Debug log

    const content = `
        <div class="view-details">
            <div class="detail-section">
                <h4><i class="fas fa-tags"></i> Category Information</h4>
                <div class="detail-grid">
                    <div class="detail-item">
                        <label>Category ID:</label>
                        <span>${category.categoryId || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Category Name:</label>
                        <span>${category.categoryName || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Description:</label>
                        <span>${category.description || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Status:</label>
                        <span class="status-badge status-${category.status ? category.status.toLowerCase() : 'unknown'}">${category.status || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Total Items:</label>
                        <span>${category.itemCount || 0}</span>
                    </div>
                    <div class="detail-item">
                        <label>Created Date:</label>
                        <span>${category.createdAt ? formatDate(category.createdAt) : 'N/A'}</span>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                    Close
                </button>
                <button type="button" class="btn btn-primary" onclick="editCategory(${category.categoryId})">
                    <i class="fas fa-edit"></i> Edit Category
                </button>
            </div>
        </div>
    `;

    const modalId = createModal('Category Details', content, 'medium');
    return modalId;
}

// Handle adjust stock form submission
async function handleAdjustStockSubmit(e) {
    e.preventDefault();

    const form = e.target;
    const formData = getFormData(form);
    const itemId = formData.itemId;

    // Clear previous errors
    clearFieldErrors();

    // Validate form
    if (!validateAdjustStockForm(formData)) {
        return;
    }

    // Get current stock for calculation
    const currentStock = parseInt(document.querySelector('.current-stock').textContent) || 0;
    const quantity = parseInt(formData.quantity);
    let newStock = currentStock;

    // Calculate new stock based on adjustment type
    if (formData.adjustmentType === 'increase') {
        newStock = currentStock + quantity;
    } else if (formData.adjustmentType === 'decrease') {
        newStock = currentStock - quantity;
    } else if (formData.adjustmentType === 'set') {
        newStock = quantity;
    }

    // Prevent negative stock
    if (newStock < 0) {
        showFieldError('quantity', 'This adjustment would result in negative stock');
        return;
    }

    // Prepare adjustment data
    const adjustmentData = {
        itemId: parseInt(itemId),
        adjustmentType: formData.adjustmentType.toUpperCase(),
        quantity: quantity,
        newStockLevel: newStock,
        reason: formData.reason === 'other' ? formData.customReason : formData.reason,
        notes: formData.notes ? formData.notes.trim() : null
    };

    console.log('Adjusting stock with data:', adjustmentData); // Debug log

    try {
        const response = await itemApi.adjustStock(adjustmentData);

        console.log('Adjust stock response:', response); // Debug log

        if (response.data && response.data.success) {
            const adjustmentTypeText = formData.adjustmentType === 'increase' ? 'increased' :
                                     formData.adjustmentType === 'decrease' ? 'decreased' : 'set';
            showToast(`Stock ${adjustmentTypeText} successfully! New stock: ${newStock}`, 'success');
            closeCurrentModal();

            // Reload items if on items page
            if (currentSection === 'items') {
                loadItems();
            }

            // Also update dashboard if we're on dashboard
            if (currentSection === 'dashboard') {
                loadDashboardData();
            }
        } else {
            throw new Error(response.data?.message || 'Failed to adjust stock');
        }

    } catch (error) {
        console.error('Error adjusting stock:', error);
        console.error('Error details:', error.data); // Debug log
        handleApiError(error);

        // Show field-specific errors if available
        if (error.data && error.data.errors) {
            Object.keys(error.data.errors).forEach(field => {
                showFieldError(field, error.data.errors[field]);
            });
        }
    }
}

// Validate adjust stock form
function validateAdjustStockForm(formData) {
    let isValid = true;

    // Required fields
    if (!formData.adjustmentType || formData.adjustmentType === '') {
        showFieldError('adjustmentType', 'Adjustment type is required');
        isValid = false;
    }

    if (!formData.quantity || formData.quantity === '' || parseInt(formData.quantity) <= 0) {
        showFieldError('quantity', 'Valid quantity is required');
        isValid = false;
    }

    if (!formData.reason || formData.reason === '') {
        showFieldError('reason', 'Reason is required');
        isValid = false;
    }

    if (formData.reason === 'other' && (!formData.customReason || formData.customReason.trim() === '')) {
        showFieldError('customReason', 'Please specify the custom reason');
        isValid = false;
    }

    return isValid;
}

// Adjust Stock Modal
function showAdjustStockModal(item) {
    console.log('showAdjustStockModal called with:', item); // Debug log

    const content = `
        <form id="adjust-stock-form">
            <input type="hidden" id="itemId" name="itemId" value="${item.itemId}">

            <div class="stock-info-section">
                <h4>Current Stock Information</h4>
                <div class="stock-info-grid">
                    <div class="stock-info-item">
                        <label>Item Name:</label>
                        <span>${item.itemName}</span>
                    </div>
                    <div class="stock-info-item">
                        <label>Item Code:</label>
                        <span>${item.itemCode}</span>
                    </div>
                    <div class="stock-info-item">
                        <label>Current Stock:</label>
                        <span class="current-stock">${item.stockQuantity || 0} ${item.unit || 'pieces'}</span>
                    </div>
                    <div class="stock-info-item">
                        <label>Min Stock Level:</label>
                        <span>${item.minStockLevel || 5}</span>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label for="adjustmentType">Adjustment Type *</label>
                <select id="adjustmentType" name="adjustmentType" required onchange="updateAdjustmentUI()">
                    <option value="">Select Adjustment Type</option>
                    <option value="increase">Stock In (Increase)</option>
                    <option value="decrease">Stock Out (Decrease)</option>
                    <option value="set">Set Exact Quantity</option>
                </select>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="quantity">Quantity *</label>
                    <input type="number" id="quantity" name="quantity" min="0" step="1" required placeholder="Enter quantity">
                </div>
                <div class="form-group">
                    <label for="newStock">New Stock Level</label>
                    <input type="number" id="newStock" name="newStock" readonly class="calculated-field">
                </div>
            </div>

            <div class="form-group">
                <label for="reason">Reason *</label>
                <select id="reason" name="reason" required onchange="toggleCustomReason()">
                    <option value="">Select Reason</option>
                    <option value="purchase">New Purchase/Delivery</option>
                    <option value="sale">Sale/Customer Purchase</option>
                    <option value="return">Customer Return</option>
                    <option value="damaged">Damaged/Expired</option>
                    <option value="lost">Lost/Stolen</option>
                    <option value="transfer">Transfer to Another Location</option>
                    <option value="correction">Stock Count Correction</option>
                    <option value="other">Other (Specify)</option>
                </select>
            </div>

            <div class="form-group" id="customReasonGroup" style="display: none;">
                <label for="customReason">Custom Reason</label>
                <input type="text" id="customReason" name="customReason" placeholder="Please specify the reason">
            </div>

            <div class="form-group">
                <label for="notes">Additional Notes</label>
                <textarea id="notes" name="notes" placeholder="Optional notes about this stock adjustment"></textarea>
            </div>

            <div class="stock-warning" id="stockWarning" style="display: none;">
                <i class="fas fa-exclamation-triangle"></i>
                <span id="warningMessage"></span>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                    Cancel
                </button>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save"></i> Adjust Stock
                </button>
            </div>
        </form>
    `;

    console.log('Creating adjust stock modal...'); // Debug log
    const modalId = createModal('Adjust Stock', content, 'medium');

    // Setup form after modal is created
    setTimeout(() => {
        const form = document.getElementById('adjust-stock-form');
        if (form) {
            console.log('Adjust stock form found, adding event listener'); // Debug log
            form.addEventListener('submit', handleAdjustStockSubmit);

            // Add quantity input listener for real-time calculation
            const quantityInput = document.getElementById('quantity');
            if (quantityInput) {
                quantityInput.addEventListener('input', calculateNewStock);
            }
        } else {
            console.error('Adjust stock form not found!'); // Debug log
        }
    }, 100);

    return modalId;
}

// Update UI based on adjustment type
function updateAdjustmentUI() {
    const adjustmentType = document.getElementById('adjustmentType').value;
    const quantityInput = document.getElementById('quantity');
    const quantityLabel = document.querySelector('label[for="quantity"]');

    if (adjustmentType === 'increase') {
        quantityLabel.textContent = 'Quantity to Add *';
        quantityInput.placeholder = 'Enter quantity to add';
    } else if (adjustmentType === 'decrease') {
        quantityLabel.textContent = 'Quantity to Remove *';
        quantityInput.placeholder = 'Enter quantity to remove';
    } else if (adjustmentType === 'set') {
        quantityLabel.textContent = 'New Stock Quantity *';
        quantityInput.placeholder = 'Enter exact stock quantity';
    }

    calculateNewStock();
}

// Calculate new stock level in real-time
function calculateNewStock() {
    const currentStock = parseInt(document.querySelector('.current-stock').textContent) || 0;
    const adjustmentType = document.getElementById('adjustmentType').value;
    const quantity = parseInt(document.getElementById('quantity').value) || 0;
    const newStockField = document.getElementById('newStock');
    const warningDiv = document.getElementById('stockWarning');
    const warningMessage = document.getElementById('warningMessage');

    let newStock = currentStock;

    if (adjustmentType === 'increase') {
        newStock = currentStock + quantity;
    } else if (adjustmentType === 'decrease') {
        newStock = currentStock - quantity;
    } else if (adjustmentType === 'set') {
        newStock = quantity;
    }

    newStockField.value = newStock;

    // Show warnings
    if (newStock < 0) {
        warningDiv.style.display = 'block';
        warningMessage.textContent = 'Warning: New stock level cannot be negative!';
        warningDiv.className = 'stock-warning error';
    } else if (newStock === 0) {
        warningDiv.style.display = 'block';
        warningMessage.textContent = 'Warning: This will set stock to zero (out of stock).';
        warningDiv.className = 'stock-warning warning';
    } else if (newStock <= 5) { // Assuming min stock level of 5
        warningDiv.style.display = 'block';
        warningMessage.textContent = 'Warning: New stock level is below minimum stock level.';
        warningDiv.className = 'stock-warning warning';
    } else {
        warningDiv.style.display = 'none';
    }
}

// Toggle custom reason field
function toggleCustomReason() {
    const reason = document.getElementById('reason').value;
    const customReasonGroup = document.getElementById('customReasonGroup');
    const customReasonInput = document.getElementById('customReason');

    if (reason === 'other') {
        customReasonGroup.style.display = 'block';
        customReasonInput.required = true;
    } else {
        customReasonGroup.style.display = 'none';
        customReasonInput.required = false;
        customReasonInput.value = '';
    }
}

// Handle edit item form submission
async function handleEditItemSubmit(e) {
    e.preventDefault();

    const form = e.target;
    const formData = getFormData(form);
    const itemId = formData.itemId;

    // Remove itemId from formData as it's not needed in the request body
    delete formData.itemId;

    // Clear previous errors
    clearFieldErrors();

    // Validate form
    if (!validateItemForm(formData)) {
        return;
    }

    // Transform data to match backend expectations
    const itemData = {
        itemCode: formData.itemCode,
        itemName: formData.itemName.trim(),
        categoryId: parseInt(formData.categoryId),
        description: formData.description ? formData.description.trim() : null,
        price: parseFloat(formData.price),
        costPrice: formData.costPrice ? parseFloat(formData.costPrice) : null,
        stockQuantity: formData.stockQuantity ? parseInt(formData.stockQuantity) : 0,
        minStockLevel: formData.minStockLevel ? parseInt(formData.minStockLevel) : 5,
        maxStockLevel: formData.maxStockLevel ? parseInt(formData.maxStockLevel) : 1000,
        unit: formData.unit || 'piece',
        barcode: formData.barcode ? formData.barcode.trim() : null,
        isbn: formData.isbn ? formData.isbn.trim() : null,
        author: formData.author ? formData.author.trim() : null,
        publisher: formData.publisher ? formData.publisher.trim() : null,
        publicationYear: formData.publicationYear ? parseInt(formData.publicationYear) : null,
        status: formData.status ? formData.status.toUpperCase() : 'ACTIVE'
    };

    // Remove empty values
    Object.keys(itemData).forEach(key => {
        if (itemData[key] === '' || itemData[key] === undefined || (typeof itemData[key] === 'string' && itemData[key].trim() === '')) {
            itemData[key] = null;
        }
    });

    console.log('Updating item with data:', itemData); // Debug log

    try {
        const response = await itemApi.updateItem(itemId, itemData);

        console.log('Update item response:', response); // Debug log

        if (response.data && response.data.success) {
            showToast('Item updated successfully!', 'success');
            closeCurrentModal();

            // Reload items if on items page
            if (currentSection === 'items') {
                loadItems();
            }

            // Also update dashboard if we're on dashboard
            if (currentSection === 'dashboard') {
                loadDashboardData();
            }
        } else {
            throw new Error(response.data?.message || 'Failed to update item');
        }

    } catch (error) {
        console.error('Error updating item:', error);
        console.error('Error details:', error.data); // Debug log
        handleApiError(error);

        // Show field-specific errors if available
        if (error.data && error.data.errors) {
            Object.keys(error.data.errors).forEach(field => {
                showFieldError(field, error.data.errors[field]);
            });
        }
    }
}

// Handle item form submission
async function handleItemSubmit(e) {
    e.preventDefault();

    const form = e.target;
    const formData = getFormData(form);

    // Clear previous errors
    clearFieldErrors();

    // Validate form
    if (!validateItemForm(formData)) {
        return;
    }

    // Transform data to match backend expectations
    const itemData = {
        itemCode: formData.itemCode,
        itemName: formData.itemName.trim(),
        categoryId: parseInt(formData.categoryId),
        description: formData.description ? formData.description.trim() : null,
        price: parseFloat(formData.price),
        costPrice: formData.costPrice ? parseFloat(formData.costPrice) : null,
        stockQuantity: formData.stockQuantity ? parseInt(formData.stockQuantity) : 0,
        minStockLevel: formData.minStockLevel ? parseInt(formData.minStockLevel) : 5,
        maxStockLevel: formData.maxStockLevel ? parseInt(formData.maxStockLevel) : 1000,
        unit: formData.unit || 'piece',
        barcode: formData.barcode ? formData.barcode.trim() : null,
        isbn: formData.isbn ? formData.isbn.trim() : null,
        author: formData.author ? formData.author.trim() : null,
        publisher: formData.publisher ? formData.publisher.trim() : null,
        publicationYear: formData.publicationYear ? parseInt(formData.publicationYear) : null,
        status: formData.status ? formData.status.toUpperCase() : 'ACTIVE'
    };

    // Remove empty values
    Object.keys(itemData).forEach(key => {
        if (itemData[key] === '' || itemData[key] === undefined || (typeof itemData[key] === 'string' && itemData[key].trim() === '')) {
            itemData[key] = null;
        }
    });

    console.log('Creating item with data:', itemData); // Debug log

    try {
        const response = await itemApi.createItem(itemData);

        console.log('Create item response:', response); // Debug log

        if (response.data && response.data.success) {
            showToast('Item created successfully!', 'success');
            closeCurrentModal();

            // Reload items if on items page
            if (currentSection === 'items') {
                loadItems();
            }

            // Also update dashboard if we're on dashboard
            if (currentSection === 'dashboard') {
                loadDashboardData();
            }
        } else {
            throw new Error(response.data?.message || 'Failed to create item');
        }

    } catch (error) {
        console.error('Error creating item:', error);
        console.error('Error details:', error.data); // Debug log
        handleApiError(error);

        // Show field-specific errors if available
        if (error.data && error.data.errors) {
            Object.keys(error.data.errors).forEach(field => {
                showFieldError(field, error.data.errors[field]);
            });
        }
    }
}

// Validate item form
function validateItemForm(formData) {
    let isValid = true;

    // Required fields
    if (!formData.itemName || formData.itemName.trim() === '') {
        showFieldError('itemName', 'Item name is required');
        isValid = false;
    }

    if (!formData.categoryId || formData.categoryId === '') {
        showFieldError('categoryId', 'Category is required');
        isValid = false;
    }

    if (!formData.price || formData.price === '' || parseFloat(formData.price) <= 0) {
        showFieldError('price', 'Valid selling price is required');
        isValid = false;
    }

    // Optional validations
    if (formData.costPrice && parseFloat(formData.costPrice) < 0) {
        showFieldError('costPrice', 'Cost price cannot be negative');
        isValid = false;
    }

    if (formData.stockQuantity && parseInt(formData.stockQuantity) < 0) {
        showFieldError('stockQuantity', 'Stock quantity cannot be negative');
        isValid = false;
    }

    if (formData.minStockLevel && parseInt(formData.minStockLevel) < 0) {
        showFieldError('minStockLevel', 'Min stock level cannot be negative');
        isValid = false;
    }

    if (formData.maxStockLevel && parseInt(formData.maxStockLevel) < 0) {
        showFieldError('maxStockLevel', 'Max stock level cannot be negative');
        isValid = false;
    }

    if (formData.minStockLevel && formData.maxStockLevel &&
        parseInt(formData.minStockLevel) > parseInt(formData.maxStockLevel)) {
        showFieldError('maxStockLevel', 'Max stock level must be greater than min stock level');
        isValid = false;
    }

    if (formData.publicationYear && (parseInt(formData.publicationYear) < 1900 || parseInt(formData.publicationYear) > 2030)) {
        showFieldError('publicationYear', 'Publication year must be between 1900 and 2030');
        isValid = false;
    }

    return isValid;
}

window.handleItemSubmit = handleItemSubmit;

// Edit Item Modal
function showEditItemModal(item) {
    console.log('showEditItemModal called with:', item); // Debug log

    const content = `
        <form id="edit-item-form">
            <input type="hidden" id="itemId" name="itemId" value="${item.itemId}">

            <div class="form-row">
                <div class="form-group">
                    <label for="itemCode">Item Code</label>
                    <input type="text" id="itemCode" name="itemCode" value="${item.itemCode || ''}" readonly>
                </div>
                <div class="form-group">
                    <label for="itemName">Item Name *</label>
                    <input type="text" id="itemName" name="itemName" value="${item.itemName || ''}" required placeholder="Enter item name">
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="categoryId">Category *</label>
                    <select id="categoryId" name="categoryId" required>
                        <option value="">Select Category</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="barcode">Barcode</label>
                    <input type="text" id="barcode" name="barcode" value="${item.barcode || ''}" placeholder="Enter barcode (optional)">
                </div>
            </div>

            <div class="form-group">
                <label for="description">Description</label>
                <textarea id="description" name="description" placeholder="Enter item description (optional)">${item.description || ''}</textarea>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="price">Selling Price *</label>
                    <input type="number" id="price" name="price" step="0.01" min="0" value="${item.price || ''}" required placeholder="0.00">
                </div>
                <div class="form-group">
                    <label for="costPrice">Cost Price</label>
                    <input type="number" id="costPrice" name="costPrice" step="0.01" min="0" value="${item.costPrice || ''}" placeholder="0.00">
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="stockQuantity">Current Stock</label>
                    <input type="number" id="stockQuantity" name="stockQuantity" min="0" value="${item.stockQuantity || 0}" placeholder="0">
                </div>
                <div class="form-group">
                    <label for="unit">Unit</label>
                    <select id="unit" name="unit">
                        <option value="piece" ${item.unit === 'piece' ? 'selected' : ''}>Piece</option>
                        <option value="box" ${item.unit === 'box' ? 'selected' : ''}>Box</option>
                        <option value="pack" ${item.unit === 'pack' ? 'selected' : ''}>Pack</option>
                        <option value="set" ${item.unit === 'set' ? 'selected' : ''}>Set</option>
                        <option value="kg" ${item.unit === 'kg' ? 'selected' : ''}>Kilogram</option>
                        <option value="liter" ${item.unit === 'liter' ? 'selected' : ''}>Liter</option>
                    </select>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="minStockLevel">Min Stock Level</label>
                    <input type="number" id="minStockLevel" name="minStockLevel" min="0" value="${item.minStockLevel || 5}" placeholder="5">
                </div>
                <div class="form-group">
                    <label for="maxStockLevel">Max Stock Level</label>
                    <input type="number" id="maxStockLevel" name="maxStockLevel" min="0" value="${item.maxStockLevel || 1000}" placeholder="1000">
                </div>
            </div>

            <!-- Book-specific fields -->
            <div class="form-section">
                <h4>Book Information (Optional)</h4>
                <div class="form-row">
                    <div class="form-group">
                        <label for="isbn">ISBN</label>
                        <input type="text" id="isbn" name="isbn" value="${item.isbn || ''}" placeholder="Enter ISBN">
                    </div>
                    <div class="form-group">
                        <label for="author">Author</label>
                        <input type="text" id="author" name="author" value="${item.author || ''}" placeholder="Enter author name">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="publisher">Publisher</label>
                        <input type="text" id="publisher" name="publisher" value="${item.publisher || ''}" placeholder="Enter publisher">
                    </div>
                    <div class="form-group">
                        <label for="publicationYear">Publication Year</label>
                        <input type="number" id="publicationYear" name="publicationYear" min="1900" max="2030" value="${item.publicationYear || ''}" placeholder="2024">
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label for="status">Status</label>
                <select id="status" name="status">
                    <option value="active" ${item.status === 'ACTIVE' ? 'selected' : ''}>Active</option>
                    <option value="inactive" ${item.status === 'INACTIVE' ? 'selected' : ''}>Inactive</option>
                    <option value="discontinued" ${item.status === 'DISCONTINUED' ? 'selected' : ''}>Discontinued</option>
                </select>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                    Cancel
                </button>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save"></i> Update Item
                </button>
            </div>
        </form>
    `;

    console.log('Creating edit item modal...'); // Debug log
    const modalId = createModal('Edit Item', content, 'large');

    // Load categories and setup form
    setTimeout(async () => {
        await loadCategoriesForEditItem(item.categoryId);

        const form = document.getElementById('edit-item-form');
        if (form) {
            console.log('Edit item form found, adding event listener'); // Debug log
            form.addEventListener('submit', handleEditItemSubmit);
        } else {
            console.error('Edit item form not found!'); // Debug log
        }
    }, 100);

    return modalId;
}

// Load categories for edit item and select the current category
async function loadCategoriesForEditItem(currentCategoryId) {
    try {
        const response = await categoryApi.getCategories();
        console.log('Categories for edit item:', response); // Debug log

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

        const categorySelect = document.getElementById('categoryId');
        if (categorySelect && categories.length > 0) {
            // Clear existing options except the first one
            categorySelect.innerHTML = '<option value="">Select Category</option>';

            categories.forEach(category => {
                const option = document.createElement('option');
                option.value = category.categoryId;
                option.textContent = category.categoryName;

                // Select the current category
                if (category.categoryId == currentCategoryId) {
                    option.selected = true;
                }

                categorySelect.appendChild(option);
            });

            console.log('Loaded', categories.length, 'categories for edit item, selected:', currentCategoryId);
        } else {
            console.warn('No categories found or category select not found');
        }
    } catch (error) {
        console.error('Error loading categories for edit item:', error);
        showToast('Error loading categories', 'error');
    }
}

// Profile Modal
async function showProfileModal() {
    try {
        console.log('Opening profile modal...');

        // Show loading modal first
        const loadingContent = `
            <div class="loading-container">
                <div class="loading-spinner">
                    <i class="fas fa-spinner fa-spin"></i>
                </div>
                <p>Loading profile...</p>
            </div>
        `;

        const modalId = createModal('Profile & Settings', loadingContent, 'large');

        // Load profile data
        let profile = null;
        try {
            const response = await userApi.getProfile();
            console.log('Profile API Response:', response);

            if (response.data) {
                if (response.data.success && response.data.data) {
                    profile = response.data.data;
                } else if (response.data.userId || response.data.username) {
                    profile = response.data;
                }
            }
        } catch (error) {
            console.error('Error loading profile:', error);
            // Use fallback profile data
            profile = {
                username: 'demo_user',
                fullName: 'Demo User',
                email: 'demo@example.com',
                role: 'ADMIN',
                lastLogin: new Date().toISOString(),
                createdAt: new Date().toISOString()
            };
        }

        if (!profile) {
            profile = {
                username: 'unknown_user',
                fullName: 'Unknown User',
                email: 'unknown@example.com',
                role: 'USER',
                lastLogin: new Date().toISOString(),
                createdAt: new Date().toISOString()
            };
        }

        // Create the profile content
        const content = createProfileModalContent(profile);

        // Update the modal with profile content
        updateModalContent(modalId, 'Profile & Settings', content);

        // Initialize profile modal functionality
        initializeProfileModal();

    } catch (error) {
        console.error('Error showing profile modal:', error);
        showToast('Error loading profile', 'error');
    }
}

function createProfileModalContent(profile) {
    return `
        <div class="profile-modal-container">
            <!-- Profile Header -->
            <div class="profile-modal-header">
                <div class="profile-avatar">
                    <i class="fas fa-user"></i>
                </div>
                <div class="profile-info">
                    <h2>${escapeHtml(profile.fullName || profile.username || 'Unknown User')}</h2>
                    <p>${escapeHtml(profile.email || 'No email provided')}</p>
                    <span class="profile-role">${escapeHtml(profile.role || 'USER')}</span>
                </div>
                <div class="profile-status">
                    <span class="status-badge status-active">Active</span>
                </div>
            </div>

            <!-- Profile Tabs -->
            <div class="profile-tabs">
                <div class="tab-buttons">
                    <button class="tab-button active" onclick="switchProfileTab('info')">
                        <i class="fas fa-user"></i> Profile Info
                    </button>
                    <button class="tab-button" onclick="switchProfileTab('password')">
                        <i class="fas fa-lock"></i> Change Password
                    </button>
                    <button class="tab-button" onclick="switchProfileTab('security')">
                        <i class="fas fa-shield-alt"></i> Security
                    </button>
                </div>

                <!-- Profile Info Tab -->
                <div id="profile-tab-info" class="tab-content active">
                    <div class="profile-details">
                        <div class="detail-grid">
                            <div class="detail-item">
                                <label>Username:</label>
                                <span>${escapeHtml(profile.username || 'N/A')}</span>
                            </div>
                            <div class="detail-item">
                                <label>Full Name:</label>
                                <span>${escapeHtml(profile.fullName || 'N/A')}</span>
                            </div>
                            <div class="detail-item">
                                <label>Email:</label>
                                <span>${escapeHtml(profile.email || 'N/A')}</span>
                            </div>
                            <div class="detail-item">
                                <label>Role:</label>
                                <span>${escapeHtml(profile.role || 'N/A')}</span>
                            </div>
                            <div class="detail-item">
                                <label>Last Login:</label>
                                <span>${profile.lastLogin ? CONFIG.formatDate(profile.lastLogin) : 'Never'}</span>
                            </div>
                            <div class="detail-item">
                                <label>Account Created:</label>
                                <span>${profile.createdAt ? CONFIG.formatDate(profile.createdAt) : 'Unknown'}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Change Password Tab -->
                <div id="profile-tab-password" class="tab-content">
                    <form id="profile-change-password-form" class="change-password-form">
                        <div class="form-group">
                            <label for="profile-current-password">Current Password *</label>
                            <div class="password-input-group">
                                <input type="password" id="profile-current-password" name="currentPassword" required>
                                <button type="button" class="password-toggle" onclick="togglePasswordVisibility('profile-current-password')">
                                    <i class="fas fa-eye"></i>
                                </button>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="profile-new-password">New Password *</label>
                            <div class="password-input-group">
                                <input type="password" id="profile-new-password" name="newPassword" required minlength="6">
                                <button type="button" class="password-toggle" onclick="togglePasswordVisibility('profile-new-password')">
                                    <i class="fas fa-eye"></i>
                                </button>
                            </div>
                            <div class="password-requirements">
                                <small>Password must be at least 6 characters long</small>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="profile-confirm-password">Confirm New Password *</label>
                            <div class="password-input-group">
                                <input type="password" id="profile-confirm-password" name="confirmPassword" required minlength="6">
                                <button type="button" class="password-toggle" onclick="togglePasswordVisibility('profile-confirm-password')">
                                    <i class="fas fa-eye"></i>
                                </button>
                            </div>
                        </div>

                        <div class="password-strength" id="profile-password-strength">
                            <div class="strength-bar">
                                <div class="strength-fill" id="profile-strength-fill"></div>
                            </div>
                            <span class="strength-text" id="profile-strength-text">Enter a password</span>
                        </div>

                        <div class="form-actions">
                            <button type="button" class="btn btn-secondary" onclick="resetProfilePasswordForm()">
                                <i class="fas fa-undo"></i> Reset
                            </button>
                            <button type="submit" class="btn btn-primary">
                                <i class="fas fa-save"></i> Change Password
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Security Tab -->
                <div id="profile-tab-security" class="tab-content">
                    <div class="security-info">
                        <div class="security-item">
                            <div class="security-icon">
                                <i class="fas fa-key"></i>
                            </div>
                            <div class="security-details">
                                <h4>Password</h4>
                                <p>Last changed: <span id="profile-password-last-changed">${profile.passwordLastChanged ? CONFIG.formatDate(profile.passwordLastChanged) : 'Never'}</span></p>
                            </div>
                            <div class="security-action">
                                <span class="security-status good">Strong</span>
                            </div>
                        </div>

                        <div class="security-item">
                            <div class="security-icon">
                                <i class="fas fa-clock"></i>
                            </div>
                            <div class="security-details">
                                <h4>Last Login</h4>
                                <p>From: <span>${profile.lastLoginLocation || 'Unknown'}</span></p>
                            </div>
                            <div class="security-action">
                                <span class="security-status good">Secure</span>
                            </div>
                        </div>

                        <div class="security-item">
                            <div class="security-icon">
                                <i class="fas fa-user-shield"></i>
                            </div>
                            <div class="security-details">
                                <h4>Account Status</h4>
                                <p>Your account is active and secure</p>
                            </div>
                            <div class="security-action">
                                <span class="security-status good">Active</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Modal Footer -->
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeCurrentModal()">
                    Close
                </button>
            </div>
        </div>
    `;
}

// Profile Modal Utility Functions
function initializeProfileModal() {
    // Initialize change password form
    const form = document.getElementById('profile-change-password-form');
    if (form) {
        form.addEventListener('submit', handleProfileChangePassword);

        // Add password strength checker
        const newPasswordInput = document.getElementById('profile-new-password');
        if (newPasswordInput) {
            newPasswordInput.addEventListener('input', checkProfilePasswordStrength);
        }

        // Add password confirmation checker
        const confirmPasswordInput = document.getElementById('profile-confirm-password');
        if (confirmPasswordInput) {
            confirmPasswordInput.addEventListener('input', checkProfilePasswordMatch);
        }
    }
}

function switchProfileTab(tabName) {
    // Remove active class from all tabs
    document.querySelectorAll('.tab-button').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

    // Add active class to selected tab
    const tabButton = document.querySelector(`[onclick="switchProfileTab('${tabName}')"]`);
    const tabContent = document.getElementById(`profile-tab-${tabName}`);

    if (tabButton) tabButton.classList.add('active');
    if (tabContent) tabContent.classList.add('active');
}

async function handleProfileChangePassword(event) {
    event.preventDefault();

    const form = event.target;
    const formData = new FormData(form);

    const currentPassword = formData.get('currentPassword');
    const newPassword = formData.get('newPassword');
    const confirmPassword = formData.get('confirmPassword');

    // Validation
    if (!currentPassword || !newPassword || !confirmPassword) {
        showToast('Please fill in all password fields', 'error');
        return;
    }

    if (newPassword.length < 6) {
        showToast('New password must be at least 6 characters long', 'error');
        return;
    }

    if (newPassword !== confirmPassword) {
        showToast('New password and confirmation do not match', 'error');
        return;
    }

    if (currentPassword === newPassword) {
        showToast('New password must be different from current password', 'error');
        return;
    }

    try {
        // Check if user is authenticated
        if (!isSessionValid()) {
            showToast('Please login to change password', 'error');
            return;
        }

        const userData = getUserSession();
        if (!userData) {
            showToast('User session not found. Please login again.', 'error');
            return;
        }

        // Show loading state
        const submitButton = form.querySelector('button[type="submit"]');
        const originalText = submitButton.innerHTML;
        submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Changing Password...';
        submitButton.disabled = true;

        // Call change password API
        console.log('Calling authApi.changePassword with:', {
            currentPassword: '***',
            newPassword: '***',
            confirmPassword: '***'
        });

        const response = await authApi.changePassword(currentPassword, newPassword, confirmPassword);
        console.log('Change password response:', response);

        console.log('Password change response received:', response);

        if (response && response.data && response.data.success) {
            showToast('Password changed successfully!', 'success');
            form.reset();

            // Update password last changed date
            const passwordLastChanged = document.getElementById('profile-password-last-changed');
            if (passwordLastChanged) {
                passwordLastChanged.textContent = 'Just now';
            }

            // Reset password strength indicator
            resetProfilePasswordForm();

        } else if (response && response.success) {
            // Handle case where success is directly on response
            showToast('Password changed successfully!', 'success');
            form.reset();
            resetProfilePasswordForm();
        } else {
            const errorMsg = (response && response.data && response.data.message) ||
                           (response && response.message) ||
                           'Failed to change password';
            throw new Error(errorMsg);
        }

    } catch (error) {
        console.error('Error changing password:', error);
        console.error('Error details:', error.response || error.data || error);

        let errorMessage = 'Unknown error occurred';

        // Handle different error response formats
        if (error.response && error.response.data) {
            // Axios-style error response
            const errorData = error.response.data;
            errorMessage = errorData.message || errorData.error || 'Server error';
        } else if (error.data) {
            // Custom API error response
            errorMessage = error.data.message || error.data.error || 'API error';
        } else if (error.message) {
            // Standard error message
            errorMessage = error.message;
        }

        // Handle specific error cases
        if (errorMessage.toLowerCase().includes('current password') ||
            errorMessage.toLowerCase().includes('incorrect') ||
            errorMessage.toLowerCase().includes('wrong password')) {
            showToast('Current password is incorrect', 'error');
        } else if (errorMessage.toLowerCase().includes('unauthorized') ||
                   error.status === 401 ||
                   (error.response && error.response.status === 401)) {
            showToast('Session expired. Please login again.', 'error');
            setTimeout(() => logout(), 2000);
        } else if (errorMessage.toLowerCase().includes('validation') ||
                   error.status === 400 ||
                   (error.response && error.response.status === 400)) {
            showToast('Invalid password format. Please check requirements.', 'error');
        } else {
            showToast('Error changing password: ' + errorMessage, 'error');
        }

    } finally {
        // Reset button state
        const submitButton = form.querySelector('button[type="submit"]');
        submitButton.innerHTML = '<i class="fas fa-save"></i> Change Password';
        submitButton.disabled = false;
    }
}

function checkProfilePasswordStrength() {
    const password = document.getElementById('profile-new-password').value;
    const strengthFill = document.getElementById('profile-strength-fill');
    const strengthText = document.getElementById('profile-strength-text');

    if (!strengthFill || !strengthText) return;

    let strength = 0;
    let strengthLabel = 'Very Weak';

    if (password.length >= 6) strength += 1;
    if (password.length >= 8) strength += 1;
    if (/[a-z]/.test(password)) strength += 1;
    if (/[A-Z]/.test(password)) strength += 1;
    if (/[0-9]/.test(password)) strength += 1;
    if (/[^A-Za-z0-9]/.test(password)) strength += 1;

    // Remove existing classes
    strengthFill.className = 'strength-fill';

    if (strength === 0) {
        strengthLabel = 'Enter a password';
    } else if (strength <= 2) {
        strengthFill.classList.add('weak');
        strengthLabel = 'Weak';
    } else if (strength <= 3) {
        strengthFill.classList.add('fair');
        strengthLabel = 'Fair';
    } else if (strength <= 4) {
        strengthFill.classList.add('good');
        strengthLabel = 'Good';
    } else {
        strengthFill.classList.add('strong');
        strengthLabel = 'Strong';
    }

    strengthText.textContent = strengthLabel;
}

function checkProfilePasswordMatch() {
    const newPassword = document.getElementById('profile-new-password').value;
    const confirmPassword = document.getElementById('profile-confirm-password').value;
    const confirmInput = document.getElementById('profile-confirm-password');

    if (!confirmInput) return;

    if (confirmPassword && newPassword !== confirmPassword) {
        confirmInput.setCustomValidity('Passwords do not match');
        confirmInput.style.borderColor = 'var(--danger-color)';
    } else {
        confirmInput.setCustomValidity('');
        confirmInput.style.borderColor = '';
    }
}

function resetProfilePasswordForm() {
    const form = document.getElementById('profile-change-password-form');
    if (form) {
        form.reset();

        // Reset password strength indicator
        const strengthFill = document.getElementById('profile-strength-fill');
        const strengthText = document.getElementById('profile-strength-text');

        if (strengthFill) {
            strengthFill.className = 'strength-fill';
        }
        if (strengthText) {
            strengthText.textContent = 'Enter a password';
        }

        // Reset password match validation
        const confirmInput = document.getElementById('profile-confirm-password');
        if (confirmInput) {
            confirmInput.setCustomValidity('');
            confirmInput.style.borderColor = '';
        }
    }
}

function togglePasswordVisibility(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;

    const button = input.parentElement.querySelector('.password-toggle');
    const icon = button.querySelector('i');

    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fas fa-eye-slash';
    } else {
        input.type = 'password';
        icon.className = 'fas fa-eye';
    }
}


