// Получаем ID клиента из URL
const clientId = window.location.pathname.split('/')[2];

// Загрузка записей
function loadAppointments(filter) {
    fetch(`/api/client/${clientId}/appointments?filter=${filter}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('allCount').innerText = data.allCount;
            document.getElementById('scheduledCount').innerText = data.scheduledCount;
            document.getElementById('completedCount').innerText = data.completedCount;
            document.getElementById('cancelledCount').innerText = data.cancelledCount;
            
            const container = document.getElementById('appointmentsContainer');
            container.innerHTML = renderAppointments(data.appointments);
            
            document.querySelectorAll('.status-tab').forEach(btn => {
                btn.classList.remove('active');
                if (btn.getAttribute('data-filter') === filter) {
                    btn.classList.add('active');
                }
            });
        })
        .catch(error => {
            console.error('Ошибка:', error);
            document.getElementById('appointmentsContainer').innerHTML = '<div class="appointments-empty"><i class="fas fa-calendar-alt"></i><h3>Ошибка загрузки</h3></div>';
        });
}

function renderAppointments(appointmentsMap) {
    const appointments = Object.values(appointmentsMap);
    
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
        let statusIcon = '';
        let showActions = false;
        
        switch (app.status) {
            case 'SCHEDULED':
                statusClass = 'scheduled';
                statusText = 'Предстоит';
                showActions = true;
                break;
            case 'COMPLETED':
                statusClass = 'completed';
                statusText = 'Завершено';
                showActions = false;
                break;
            case 'CANCELLED':
                statusClass = 'cancelled';
                statusText = 'Отменено';
                showActions = false;
                break;
        }
        
        html += `
            <div class="appointment-item" data-id="${app.id}">
                <div class="appointment-time">
                    <i class="far fa-calendar"></i> ${app.date}<br>
                    <i class="far fa-clock"></i> ${app.time}
                </div>
                <div class="appointment-info">
                    <strong>${escapeHtml(app.service.name)}</strong>
                    <div class="appointment-details">
                        ️${escapeHtml(app.doctor.name)}<br>
                        ${app.service.price} ₽ ${app.service.duration} мин
                    </div>
                </div>
                <div class="appointment-status ${statusClass}">
                    ${statusIcon} ${statusText}
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

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}



// Обработчики фильтров
document.querySelectorAll('.status-tab').forEach(btn => {
    btn.addEventListener('click', function() {
        const filter = this.getAttribute('data-filter');
        loadAppointments(filter);
    });
});

// ========== ВЫПАДАЮЩЕЕ МЕНЮ ==========
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

// ========== СКРОЛЛ ХЕДЕРА ==========
const header = document.getElementById('header');
window.addEventListener('scroll', () => {
    if (window.scrollY > 10) {
        header.classList.add('scrolled');
    } else {
        header.classList.remove('scrolled');
    }
});

// ========== МОДАЛЬНОЕ ОКНО ПРОФИЛЯ ==========
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

// Загружаем записи при загрузке страницы
loadAppointments('all');
// ========== МОДАЛЬНОЕ ОКНО ПОДТВЕРЖДЕНИЯ ОТМЕНЫ ==========
let pendingCancelId = null;

const confirmModal = document.getElementById('confirmModal');
const confirmCancelBtn = document.getElementById('confirmCancelBtn');
const closeConfirmModalBtns = document.querySelectorAll('.close-confirm-modal');

function showConfirmModal(appointmentId) {
    pendingCancelId = appointmentId;
    confirmModal.classList.add('show');
}

function hideConfirmModal() {
    confirmModal.classList.remove('show');
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

// Закрытие по клику вне модалки
window.addEventListener('click', (e) => {
    if (e.target === confirmModal) {
        hideConfirmModal();
    }
});
// ========== ОТМЕНА ЗАПИСИ ==========
document.addEventListener('click', function(e) {
    const cancelBtn = e.target.closest('.btn-cancel-appointment');
    if (cancelBtn) {
        e.preventDefault();
        const appointmentId = cancelBtn.getAttribute('data-id');
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
            const activeFilter = document.querySelector('.status-tab.active').getAttribute('data-filter');
            loadAppointments(activeFilter);
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
// Обработчик редактирования записи
document.addEventListener('click', function(e) {
    const editBtn = e.target.closest('.btn-edit-appointment');
    if (editBtn) {
        e.preventDefault();
        const appointmentId = editBtn.getAttribute('data-id');
        window.location.href = `/booking/edit/${appointmentId}`;
    }
});
// ========== УВЕДОМЛЕНИЯ ==========
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 3000);
}