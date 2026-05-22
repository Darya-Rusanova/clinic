// Переключение вкладок
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

servicesLink.addEventListener('click', (e) => {
    e.preventDefault();
    showTab('services');
});

scheduleLink.addEventListener('click', (e) => {
    e.preventDefault();
    showTab('schedule');
});

clientsLink.addEventListener('click', (e) => {
    e.preventDefault();
    showTab('clients');
});


function loadClients(filter) {
    const doctorId = window.location.pathname.split('/')[2];

    fetch(`/api/doctor/${doctorId}/clients?filter=${filter}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('allCount').innerText = data.allCount;
            document.getElementById('scheduledCount').innerText = data.scheduledCount;
            document.getElementById('completedCount').innerText = data.completedCount;
            document.getElementById('cancelledCount').innerText = data.cancelledCount;

            const container = document.getElementById('clientsListContainer');
            container.innerHTML = renderClients(data.clients);

            document.querySelectorAll('.status-tab').forEach(btn => {
                btn.classList.remove('active');
                if (btn.getAttribute('data-filter') === filter) {
                    btn.classList.add('active');
                }
            });
        })
        .catch(error => {
            console.error('Ошибка:', error);
            document.getElementById('clientsListContainer').innerHTML = '<div class="no-clients">Ошибка загрузки</div>';
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
                        <div class="appointment-history-item">
                            <div class="appointment-date">${app.date} ${app.time}</div>
                            <div class="appointment-service">${escapeHtml(app.service)}</div>
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

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
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

const userMenu = document.getElementById('userMenu');
const userDropdown = document.getElementById('userDropdown');

if (userMenu) {
    userMenu.addEventListener('click', (e) => {
        e.stopPropagation();
        userDropdown.classList.toggle('show');
    });
}

document.addEventListener('click', () => {
    if (userDropdown) userDropdown.classList.remove('show');
});

const header = document.getElementById('header');
window.addEventListener('scroll', () => {
    if (window.scrollY > 10) {
        header.classList.add('scrolled');
    } else {
        header.classList.remove('scrolled');
    }
});

// Загрузка расписания
let currentWeekOffset = 0;

function loadSchedule(offset) {
    const doctorId = window.location.pathname.split('/')[2];
    currentWeekOffset = offset;

    fetch(`/api/doctor/${doctorId}/schedule?weekOffset=${offset}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('weekRange').innerText =
                formatDate(data.weekStart) + ' - ' + formatDate(data.weekEnd);

            const container = document.getElementById('scheduleContainer');
            container.innerHTML = renderSchedule(data.weekSchedule);
        })
        .catch(error => {
            console.error('Ошибка:', error);
            document.getElementById('scheduleContainer').innerHTML = '<div class="empty-slots">Ошибка загрузки</div>';
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
                        <div class="slot-time">${slot.startTime.substring(0, 5)} - ${slot.endTime.substring(0, 5)}</div>
                        <div>
                            <span class="slot-status ${slot.status.toLowerCase()}">${slot.statusText}</span>
                        </div>
                        <div class="slot-client">
                            ${slot.clientName ? `${slot.clientName} - ${slot.serviceName}` : ''}
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

    html += '<button class="add-break-btn" id="addBreakBtn"><i class="fas fa-plus"></i> Добавить перерыв</button>';
    return html;
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' });
}

document.addEventListener('click', function(e) {
    const navBtn = e.target.closest('.date-nav-btn');
    if (navBtn && navBtn.getAttribute('data-offset')) {
        e.preventDefault();
        const offset = parseInt(navBtn.getAttribute('data-offset'));
        loadSchedule(currentWeekOffset + offset);
    }
});

const modal = document.getElementById('settingsModal');
const settingsBtns = document.querySelectorAll('#settingsBtn, #settingsBtnSidebar');
const closeModal = document.querySelector('#settingsModal .close-modal');
const cancelBtn = document.getElementById('cancelBtn');

if (settingsBtns) {
    settingsBtns.forEach(btn => {
        btn.addEventListener('click', () => {
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