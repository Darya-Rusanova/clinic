
const servicesLink = document.getElementById('servicesLink');
const scheduleLink = document.getElementById('scheduleLink');
const clientsLink = document.getElementById('clientsLink');

const servicesSection = document.getElementById('servicesSection');
const scheduleSection = document.getElementById('scheduleSection');
const clientsSection = document.getElementById('clientsSection');

function showTab(tabId) {
    servicesSection.classList.remove('active');
    scheduleSection.classList.remove('active');
    clientsSection.classList.remove('active');

    if (tabId === 'services') servicesSection.classList.add('active');
    if (tabId === 'schedule') {
        scheduleSection.classList.add('active');
        if (currentWeekOffset === 0) {
            loadSchedule(0);
        }
    }
    if (tabId === 'clients') {
        clientsSection.classList.add('active');
        loadClients('all');
    }
}

if (servicesLink) {
    servicesLink.addEventListener('click', (e) => {
        e.preventDefault();
        showTab('services');
    });
}

if (scheduleLink) {
    scheduleLink.addEventListener('click', (e) => {
        e.preventDefault();
        showTab('schedule');
    });
}

if (clientsLink) {
    clientsLink.addEventListener('click', (e) => {
        e.preventDefault();
        showTab('clients');
    });
}

function loadClients(filter) {
    const doctorId = window.location.pathname.split('/')[2];

    fetch(`/api/doctor/${doctorId}/clients?filter=${filter}`)
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

            const container = document.getElementById('clientsListContainer');
            if (container) {
                container.innerHTML = renderClients(data.clients);
            }

            document.querySelectorAll('.status-tab').forEach(btn => {
                btn.classList.remove('active');
                if (btn.getAttribute('data-filter') === filter) {
                    btn.classList.add('active');
                }
            });

            attachCompleteHandlers();
        })
        .catch(error => {
            console.error('Ошибка:', error);
            const container = document.getElementById('clientsListContainer');
            if (container) {
                container.innerHTML = '<div class="no-clients">Ошибка загрузки</div>';
            }
        });
}

function renderClients(clients) {
    if (!clients || clients.length === 0) {
        return '<div class="no-clients">Нет клиентов</div>';
    }

    let html = '';
    for (const client of clients) {
        const firstApp = client.appointments[0];
        let badgeClass = 'client-status-badge';
        let badgeText = 'Нет записей';

        if (firstApp) {
            if (firstApp.status === 'COMPLETED') {
                badgeClass += ' completed';
                badgeText = 'Завершено';
            } else if (firstApp.status === 'SCHEDULED') {
                badgeClass += ' scheduled';
                badgeText = 'Предстоит';
            } else if (firstApp.status === 'CANCELLED') {
                badgeClass += ' cancelled';
                badgeText = 'Отменено';
            }
        }

        html += `
            <div class="client-card">
                <div class="client-header">
                    <div class="client-avatar">
                        <i class="fas fa-user-circle"></i>
                    </div>
                    <div class="client-info">
                        <h4 class="client-name">${escapeHtml(client.name)}</h4>
                        <p class="client-contact">${escapeHtml(client.phone)} • ${escapeHtml(client.email)}</p>
                    </div>
                    <div class="${badgeClass}">${badgeText}</div>
                </div>
                <div class="client-appointments">
                    ${client.appointments.map(app => `
                        <div class="appointment-history-item" data-appointment-id="${app.id || ''}">
                            <div class="appointment-date">${app.date} ${app.time}</div>
                            <div class="appointment-service">${escapeHtml(app.service)}</div>
                            ${app.status === 'SCHEDULED' ? `
                                <button class="btn-complete-appointment" data-appointment-id="${app.id}" data-client-name="${escapeHtml(client.name)}" data-service="${escapeHtml(app.service)}">
                                    <i class="fas fa-check-circle"></i> Завершить
                                </button>
                            ` : ''}
                            <div class="appointment-status ${app.status.toLowerCase()}">
                                ${app.status === 'COMPLETED' ? ' Завершено' :
                                  app.status === 'SCHEDULED' ? ' Предстоит' : ' Отменено'}
                            </div>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }
    return html;
}

let pendingAppointmentId = null;

function attachCompleteHandlers() {
    document.querySelectorAll('.btn-complete-appointment').forEach(btn => {
        btn.removeEventListener('click', handleCompleteClick);
        btn.addEventListener('click', handleCompleteClick);
    });
}

function handleCompleteClick(e) {
    e.stopPropagation();
    const btn = e.currentTarget;
    const appointmentId = btn.getAttribute('data-appointment-id');
    const clientName = btn.getAttribute('data-client-name');
    const serviceName = btn.getAttribute('data-service');

    pendingAppointmentId = appointmentId;

    const clientNameSpan = document.getElementById('completeClientName');
    const serviceNameSpan = document.getElementById('completeServiceName');

    if (clientNameSpan) clientNameSpan.innerText = clientName;
    if (serviceNameSpan) serviceNameSpan.innerText = serviceName;

    const modal = document.getElementById('completeAppointmentModal');
    if (modal) modal.classList.add('show');
}

function completeAppointment() {
    if (!pendingAppointmentId) return;

    fetch(`/api/doctor/appointments/${pendingAppointmentId}/complete`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(result => {
        if (result.success) {
            showNotification('Прием успешно завершен', 'success');
            closeCompleteModal();
            const activeFilter = document.querySelector('.status-tab.active');
            const filter = activeFilter ? activeFilter.getAttribute('data-filter') : 'all';
            loadClients(filter);
        } else {
            showNotification(result.error || 'Ошибка при завершении приема', 'error');
            closeCompleteModal();
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        showNotification('Ошибка сервера', 'error');
        closeCompleteModal();
    });
}

function closeCompleteModal() {
    const modal = document.getElementById('completeAppointmentModal');
    if (modal) modal.classList.remove('show');
    pendingAppointmentId = null;
}

let currentWeekOffset = 0;

function loadSchedule(offset) {
    const doctorId = window.location.pathname.split('/')[2];
    currentWeekOffset = offset;

    fetch(`/api/doctor/${doctorId}/schedule?weekOffset=${offset}`)
        .then(response => response.json())
        .then(data => {
            const weekRangeSpan = document.getElementById('weekRange');
            if (weekRangeSpan) {
                weekRangeSpan.innerText = formatDate(data.weekStart) + ' - ' + formatDate(data.weekEnd);
            }

            const container = document.getElementById('scheduleContainer');
            if (container) {
                container.innerHTML = renderSchedule(data.weekSchedule);
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            const container = document.getElementById('scheduleContainer');
            if (container) {
                container.innerHTML = '<div class="empty-slots">Ошибка загрузки</div>';
            }
        });
}

function renderSchedule(weekSchedule) {
    let html = '';
    for (const day of weekSchedule) {
        html += `
            <div class="day-schedule-card">
                <div class="day-header">
                    <span class="day-name">${day.dayName}</span>
                    <span class="day-date">${formatDate(day.date)}</span>
                </div>
                <div class="slots-container">
        `;

        if (day.slots.length === 0) {
            html += '<div class="empty-slots"><p>Нет расписания</p></div>';
        } else {
            for (const slot of day.slots) {
                html += `
                    <div class="slot-row">
                        <div class="slot-time">${slot.startTime ? slot.startTime.substring(0, 5) : ''} - ${slot.endTime ? slot.endTime.substring(0, 5) : ''}</div>
                        <div>
                            <span class="slot-status ${slot.status ? slot.status.toLowerCase() : ''}">${slot.statusText || slot.status || ''}</span>
                        </div>
                        <div class="slot-client">
                            ${slot.clientName ? escapeHtml(slot.clientName) + ' - ' + escapeHtml(slot.serviceName) : ''}
                        </div>
                    </div>
                `;
            }
        }

        html += `
                </div>
            </div>
        `;
    }
    return html;
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' });
}

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
const profileForm = document.getElementById('profileForm');

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

if (profileForm) {
    profileForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const doctorId = window.location.pathname.split('/')[2];

        const lastNameInput = document.getElementById('lastName');
        const firstNameInput = document.getElementById('firstName');
        const patronymicInput = document.getElementById('patronymic');
        const phoneInput = document.getElementById('phone');
        const emailInput = document.getElementById('email');
        const genderSelect = document.getElementById('gender');
        const experienceInput = document.getElementById('experience');
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
            experience: experienceInput ? parseInt(experienceInput.value) : 0,
            oldPassword: oldPasswordInput ? oldPasswordInput.value : '',
            newPassword: newPasswordInput ? newPasswordInput.value : '',
            confirmPassword: confirmPasswordInput ? confirmPasswordInput.value : ''
        };

        const submitBtn = profileForm.querySelector('button[type="submit"]');
        let originalText = '';
        if (submitBtn) {
            originalText = submitBtn.textContent;
            submitBtn.textContent = 'Сохранение...';
            submitBtn.disabled = true;
        }

        fetch(`/api/doctor/${doctorId}/profile`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                showNotification('Профиль успешно обновлен', 'success');
                if (modal) modal.classList.remove('show');
                setTimeout(() => {
                    location.reload();
                }, 1500);
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

document.addEventListener('click', function(e) {
    const filterBtn = e.target.closest('.status-tab');
    if (filterBtn && filterBtn.getAttribute('data-filter')) {
        e.preventDefault();
        const filter = filterBtn.getAttribute('data-filter');
        loadClients(filter);
    }
});

document.addEventListener('click', function(e) {
    const navBtn = e.target.closest('.date-nav-btn');
    if (navBtn && navBtn.getAttribute('data-offset')) {
        e.preventDefault();
        const offset = parseInt(navBtn.getAttribute('data-offset'));
        loadSchedule(currentWeekOffset + offset);
    }
});

document.addEventListener('DOMContentLoaded', () => {
    const confirmBtn = document.getElementById('confirmCompleteBtn');
    if (confirmBtn) {
        confirmBtn.addEventListener('click', completeAppointment);
    }

    const closeButtons = document.querySelectorAll('.close-complete-modal');
    closeButtons.forEach(btn => {
        btn.addEventListener('click', closeCompleteModal);
    });

    const completeModal = document.getElementById('completeAppointmentModal');
    if (completeModal) {
        window.addEventListener('click', (e) => {
            if (e.target === completeModal) closeCompleteModal();
        });
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

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}