
const clientId = window.location.pathname.split('/')[2];

let currentPage = 0;
let pageSize = 5;
let sortField = 'dateTime';
let sortDirection = 'desc';
let currentFilter = 'all';


function loadAppointments() {
    const container = document.getElementById('appointmentsContainer');
    if (!container) return;
    container.innerHTML = '<div class="loading">Загрузка...</div>';

    fetch(`/api/client/${clientId}/appointments?filter=${currentFilter}&page=${currentPage}&size=${pageSize}&sort=${sortField}&direction=${sortDirection}`)
        .then(response => response.json())
        .then(data => {
            const allCountSpan = document.getElementById('allCount');
            const scheduledCountSpan = document.getElementById('scheduledCount');
            const completedCountSpan = document.getElementById('completedCount');
            const cancelledCountSpan = document.getElementById('cancelledCount');

            if (allCountSpan) allCountSpan.innerText = data.allCount;
            if (scheduledCountSpan) scheduledCountSpan.innerText = data.scheduledCount;
            if (completedCountSpan) completedCountSpan.innerText = data.completedCount;
            if (cancelledCountSpan) cancelledCountSpan.innerText = data.cancelledCount;

            container.innerHTML = renderAppointments(data.appointments);
            renderPagination(data.currentPage, data.totalPages);

            document.querySelectorAll('.status-tab').forEach(btn => {
                btn.classList.remove('active');
                if (btn.getAttribute('data-filter') === currentFilter) {
                    btn.classList.add('active');
                }
            });

            updateSortIcons();
        })
        .catch(error => {
            console.error('Ошибка:', error);
            container.innerHTML = '<div class="appointments-empty"><i class="fas fa-calendar-alt"></i><h3>Ошибка загрузки</h3></div>';
        });
}

function renderAppointments(appointments) {
    if (!appointments || appointments.length === 0) {
        return `
            <div class="appointments-empty">
                <i class="fas fa-calendar-alt"></i>
                <h3>У вас пока нет записей</h3>
                <p>Запишитесь на процедуру через главную страницу</p>
                <a href="/" class="btn-book">Записаться</a>
            </div>
        `;
    }

    let html = '<div class="appointments-list">';
    for (const app of appointments) {
        let statusClass = '';
        let statusText = '';
        let showActions = false;

        switch (app.status) {
            case 'SCHEDULED':
                statusClass = 'scheduled';
                statusText = ' Предстоит';
                showActions = true;
                break;
            case 'COMPLETED':
                statusClass = 'completed';
                statusText = ' Завершено';
                showActions = false;
                break;
            case 'CANCELLED':
                statusClass = 'cancelled';
                statusText = ' Отменено';
                showActions = false;
                break;
        }

        const serviceName = app.service && app.service.name ? escapeHtml(app.service.name) : 'Услуга удалена';
        const doctorName = app.doctor && app.doctor.name ? escapeHtml(app.doctor.name) : 'Врач больше не работает';
        const servicePrice = app.service && app.service.price ? app.service.price : 0;
        const serviceDuration = app.service && app.service.duration ? app.service.duration : 0;

        html += `
            <div class="appointment-item" data-id="${app.id}">
                <div class="appointment-time">
                    <i class="far fa-calendar"></i> ${app.date}<br>
                    <i class="far fa-clock"></i> ${app.time}
                </div>
                <div class="appointment-info">
                    <strong>${serviceName}</strong>
                    <div class="appointment-details">
                        ${doctorName}<br>
                        ${servicePrice} ₽ ${serviceDuration} мин
                    </div>
                </div>
                <div class="appointment-status ${statusClass}">
                    ${statusText}
                </div>
                ${showActions ? `
                    <div class="appointment-actions">
                        <button class="btn-edit-appointment" data-id="${app.id}">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn-cancel-appointment" data-id="${app.id}">
                            <i class="fas fa-trash-alt"></i>
                        </button>
                    </div>
                ` : ''}
            </div>
        `;
    }
    html += '</div>';
    return html;
}

function renderPagination(currentPage, totalPages) {
    const paginationContainer = document.getElementById('paginationContainer');
    if (!paginationContainer) return;

    if (totalPages <= 1) {
        paginationContainer.innerHTML = '';
        return;
    }

    let html = '<div class="pagination">';
    html += `<button class="page-btn" onclick="goToPage(${currentPage - 1})" ${currentPage === 0 ? 'disabled' : ''}>←</button>`;

    let startPage = Math.max(0, currentPage - 2);
    let endPage = Math.min(totalPages - 1, currentPage + 2);

    if (startPage > 0) {
        html += `<button class="page-btn" onclick="goToPage(0)">1</button>`;
        if (startPage > 1) html += '<span class="page-dots">...</span>';
    }

    for (let i = startPage; i <= endPage; i++) {
        html += `<button class="page-btn ${i === currentPage ? 'active' : ''}" onclick="goToPage(${i})">${i + 1}</button>`;
    }

    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) html += '<span class="page-dots">...</span>';
        html += `<button class="page-btn" onclick="goToPage(${totalPages - 1})">${totalPages}</button>`;
    }

    html += `<button class="page-btn" onclick="goToPage(${currentPage + 1})" ${currentPage === totalPages - 1 ? 'disabled' : ''}>→</button>`;
    html += '</div>';

    paginationContainer.innerHTML = html;
}

function goToPage(page) {
    if (page < 0) return;
    currentPage = page;
    loadAppointments();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function changeSort(field) {
    if (sortField === field) {
        sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
        sortField = field;
        sortDirection = 'desc';
    }
    currentPage = 0;
    loadAppointments();
}

function updateSortIcons() {
    document.querySelectorAll('.sort-btn').forEach(btn => {
        const field = btn.getAttribute('data-sort');
        const icon = btn.querySelector('.sort-icon');
        if (!icon) return;
        icon.classList.remove('fa-arrow-down', 'fa-arrow-up', 'fa-sort');

        if (field === sortField) {
            if (sortDirection === 'asc') {
                icon.classList.add('fa-arrow-up');
            } else {
                icon.classList.add('fa-arrow-down');
            }
            icon.classList.add('active');
        } else {
            icon.classList.add('fa-sort');
            icon.classList.remove('active');
        }
    });
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

document.querySelectorAll('.status-tab').forEach(btn => {
    btn.addEventListener('click', function() {
        currentFilter = this.getAttribute('data-filter');
        currentPage = 0;
        loadAppointments();
    });
});

document.querySelectorAll('.sort-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        const field = this.getAttribute('data-sort');
        changeSort(field);
    });
});

const userMenu = document.getElementById('userMenu');
const userDropdown = document.getElementById('userDropdown');

if (userMenu) {
    userMenu.addEventListener('click', (e) => {
        e.stopPropagation();
        if (userDropdown) userDropdown.classList.toggle('show');
    });
}

document.addEventListener('click', () => {
    if (userDropdown) userDropdown.classList.remove('show');
});

const header = document.getElementById('header');
if (header) {
    window.addEventListener('scroll', () => {
        if (window.scrollY > 10) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
    });
}

const modal = document.getElementById('settingsModal');
const settingsBtns = document.querySelectorAll('#settingsBtn, #settingsBtnSidebar');
const closeModal = document.querySelector('#settingsModal .close-modal');
const cancelBtn = document.getElementById('cancelBtn');

function loadProfileData() {
    const oldPasswordInput = document.getElementById('oldPassword');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    if (oldPasswordInput) oldPasswordInput.value = '';
    if (newPasswordInput) newPasswordInput.value = '';
    if (confirmPasswordInput) confirmPasswordInput.value = '';
}

if (settingsBtns) {
    settingsBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            loadProfileData();
            if (modal) modal.classList.add('show');
        });
    });
}

if (closeModal) {
    closeModal.addEventListener('click', () => {
        if (modal) modal.classList.remove('show');
    });
}

if (cancelBtn) {
    cancelBtn.addEventListener('click', () => {
        if (modal) modal.classList.remove('show');
    });
}

if (modal) {
    window.addEventListener('click', (e) => {
        if (e.target === modal) modal.classList.remove('show');
    });
}

const profileForm = document.getElementById('profileForm');
if (profileForm) {
    profileForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const lastNameInput = document.getElementById('lastName');
        const firstNameInput = document.getElementById('firstName');
        const patronymicInput = document.getElementById('patronymic');
        const phoneInput = document.getElementById('phone');
        const emailInput = document.getElementById('email');
        const genderSelect = document.getElementById('gender');
        const birthdateInput = document.getElementById('birthdate');
        const oldPasswordInput = document.getElementById('oldPassword');
        const newPasswordInput = document.getElementById('newPassword');
        const confirmPasswordInput = document.getElementById('confirmPassword');

        const data = {
            lastName: lastNameInput ? lastNameInput.value : '',
            firstName: firstNameInput ? firstNameInput.value : '',
            patronymic: patronymicInput ? patronymicInput.value : '',
            phone: phoneInput ? phoneInput.value : '',
            email: emailInput ? emailInput.value : '',
            gender: genderSelect ? genderSelect.value === 'true' : true,
            birthDate: birthdateInput ? birthdateInput.value : '',
            oldPassword: oldPasswordInput ? oldPasswordInput.value : '',
            newPassword: newPasswordInput ? newPasswordInput.value : '',
            confirmPassword: confirmPasswordInput ? confirmPasswordInput.value : ''
        };

        const submitBtn = profileForm.querySelector('button[type="submit"]');
        const originalText = submitBtn ? submitBtn.textContent : 'Сохранить';
        if (submitBtn) {
            submitBtn.textContent = 'Сохранение...';
            submitBtn.disabled = true;
        }

        fetch(`/api/client/${clientId}/profile`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                if (modal) modal.classList.remove('show');
                showNotification('Профиль успешно обновлён', 'success');
                setTimeout(() => location.reload(), 500);
            } else {
                showNotification(result.error || 'Ошибка при сохранении', 'error');
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            showNotification('Ошибка сервера', 'error');
        })
        .finally(() => {
            if (submitBtn) {
                submitBtn.textContent = originalText;
                submitBtn.disabled = false;
            }
        });
    });
}

let pendingCancelId = null;

const confirmModal = document.getElementById('confirmModal');
const confirmCancelBtn = document.getElementById('confirmCancelBtn');
const closeConfirmModalBtns = document.querySelectorAll('.close-confirm-modal');

function showConfirmModal(appointmentId) {
    pendingCancelId = appointmentId;
    if (confirmModal) confirmModal.classList.add('show');
}

function hideConfirmModal() {
    if (confirmModal) confirmModal.classList.remove('show');
    pendingCancelId = null;
}

if (confirmCancelBtn) {
    confirmCancelBtn.addEventListener('click', function() {
        if (pendingCancelId) {
            cancelAppointment(pendingCancelId);
        }
        hideConfirmModal();
    });
}

closeConfirmModalBtns.forEach(btn => {
    btn.addEventListener('click', hideConfirmModal);
});

window.addEventListener('click', (e) => {
    if (e.target === confirmModal) {
        hideConfirmModal();
    }
});

document.addEventListener('click', function(e) {
    const cancelBtnEl = e.target.closest('.btn-cancel-appointment');
    if (cancelBtnEl) {
        e.preventDefault();
        const appointmentId = cancelBtnEl.getAttribute('data-id');
        if (appointmentId && appointmentId !== 'undefined') {
            showConfirmModal(appointmentId);
        } else {
            showNotification('Ошибка: ID записи не найден', 'error');
        }
    }
});

function cancelAppointment(appointmentId) {
    fetch(`/api/appointments/${appointmentId}/cancel`, {
        method: 'DELETE'
    })
    .then(response => {
        if (response.ok) {
            loadAppointments();
            showNotification('Запись успешно отменена', 'success');
        } else {
            showNotification('Ошибка при отмене записи', 'error');
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        showNotification('Ошибка сервера', 'error');
    });
}

// ========== РЕДАКТИРОВАНИЕ ЗАПИСИ ==========
document.addEventListener('click', function(e) {
    const editBtn = e.target.closest('.btn-edit-appointment');
    if (editBtn) {
        e.preventDefault();
        const appointmentId = editBtn.getAttribute('data-id');
        window.location.href = `/booking/edit/${appointmentId}`;
    }
});

function showNotification(message, type) {
    const oldNotifications = document.querySelectorAll('.notification');
    oldNotifications.forEach(n => n.remove());

    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        padding: 12px 24px;
        border-radius: 8px;
        color: white;
        z-index: 9999;
        animation: fadeInOut 3s ease;
        background-color: ${type === 'success' ? '#4CAF50' : '#F44336'};
    `;
    document.body.appendChild(notification);
    setTimeout(() => notification.remove(), 3000);
}

let notifications = [];
let unreadCount = 0;
let currentNotificationPage = 0;
let totalNotificationPages = 0;

function loadNotifications(page = 0, append = false) {
    fetch(`/api/client/${clientId}/notifications?page=${page}`)
        .then(response => response.json())
        .then(data => {
            if (!append) {
                notifications = data.notifications;
            } else {
                notifications = [...notifications, ...data.notifications];
            }
            unreadCount = data.unreadCount;
            currentNotificationPage = data.currentPage;
            totalNotificationPages = data.totalPages;

            updateBellIcon();
            renderNotificationsList();

            if (currentNotificationPage + 1 >= totalNotificationPages) {
                document.getElementById('loadMoreNotifications').style.display = 'none';
            } else {
                document.getElementById('loadMoreNotifications').style.display = 'block';
            }
        })
        .catch(error => console.error('Ошибка:', error));
}

function updateBellIcon() {
    const dot = document.getElementById('notificationDot');
    if (dot) {
        dot.style.display = unreadCount > 0 ? 'block' : 'none';
    }
}

function renderNotificationsList() {
    const container = document.getElementById('notificationsList');
    if (!container) return;

    if (notifications.length === 0) {
        container.innerHTML = '<div class="notifications-empty">Нет уведомлений</div>';
        return;
    }

    container.innerHTML = notifications.map(n => `
        <div class="notification-item ${!n.read ? 'unread' : ''}">
            <div class="notification-icon ${n.type.toLowerCase()}">
                <i class="${getIconByType(n.type)}"></i>
            </div>
            <div class="notification-content">
                <div class="notification-title">${escapeHtml(n.title)}</div>
                <div class="notification-message">${escapeHtml(n.message)}</div>
                <div class="notification-time">${formatDate(n.createdAt)}</div>
            </div>
        </div>
    `).join('');
}

function getIconByType(type) {
    const icons = {
        'INFO': 'fas fa-info-circle',
        'SUCCESS': 'fas fa-check-circle',
        'WARNING': 'fas fa-exclamation-triangle',
        'APPOINTMENT_REMINDER': 'fas fa-calendar-day',
        'APPOINTMENT_CANCELLED': 'fas fa-calendar-times',
        'APPOINTMENT_COMPLETED': 'fas fa-check-double'
    };
    return icons[type] || 'fas fa-bell';
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    const now = new Date();
    const diffDays = Math.floor((now - date) / 86400000);

    if (diffDays === 0) return 'Сегодня';
    if (diffDays === 1) return 'Вчера';
    return date.toLocaleDateString('ru-RU');
}

function markAllNotificationsRead() {
    fetch(`/api/client/${clientId}/notifications/read`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
    .then(response => response.json())
    .then(result => {
        if (result.success) {
            unreadCount = 0;
            updateBellIcon();
            notifications.forEach(n => n.read = true);
            renderNotificationsList();
        }
    })
    .catch(error => console.error('Ошибка:', error));
}

function loadMoreNotifications() {
    if (currentNotificationPage + 1 < totalNotificationPages) {
        loadNotifications(currentNotificationPage + 1, true);
    }
}

function initNotifications() {
    const bell = document.getElementById('notificationBell');
    const dropdown = document.getElementById('notificationsDropdown');
    const markReadBtn = document.getElementById('markAllReadBtn');
    const loadMoreBtn = document.getElementById('loadMoreNotifications');

    if (bell) {
        bell.addEventListener('click', (e) => {
            e.stopPropagation();
            dropdown.classList.toggle('show');
            if (dropdown.classList.contains('show')) {
                loadNotifications(0);
            }
        });
    }

    if (markReadBtn) {
        markReadBtn.addEventListener('click', markAllNotificationsRead);
    }

    if (loadMoreBtn) {
        loadMoreBtn.addEventListener('click', loadMoreNotifications);
    }

    document.addEventListener('click', () => {
        if (dropdown) dropdown.classList.remove('show');
    });

    loadNotifications(0);
}

document.addEventListener('DOMContentLoaded', () => {
    initNotifications();
    loadAppointments();
});