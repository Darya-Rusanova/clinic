const clientId = window.location.pathname.split('/')[3];
const appointmentsBody = document.getElementById('appointmentsTableBody');
const clientNameSpan = document.getElementById('clientName');
const statusFilter = document.getElementById('statusFilter');
let pendingCancelId = null;

let currentPage = 0;
let totalPages = 0;
let pageSize = 5;

fetch(`/admin/api/clients/${clientId}`)
    .then(response => response.json())
    .then(client => {
        clientNameSpan.textContent = client.name;
    })
    .catch(error => console.error('Ошибка загрузки клиента:', error));

function loadAppointments() {
    const status = statusFilter?.value || 'all';

    fetch(`/api/client/${clientId}/appointments?filter=${status}&page=${currentPage}&size=${pageSize}&sort=dateTime&direction=desc`)
        .then(response => response.json())
        .then(data => {
            console.log('Получены данные:', data);


            const appointments = data.appointments || [];


            totalPages = data.totalPages || 0;

            renderAppointments(appointments);
            renderPagination(data.currentPage || 0, totalPages);
        })
        .catch(error => {
            console.error('Ошибка загрузки записей:', error);
            appointmentsBody.innerHTML = '<tr><td colspan="5" style="text-align: center">Ошибка загрузки: ' + error.message + '<\/td></tr>';
        });
}

function renderAppointments(appointments) {
    if (!appointments || appointments.length === 0) {
        appointmentsBody.innerHTML = '<tr><td colspan="5" style="text-align: center">Нет записей<\/td></tr>';
        return;
    }

    appointmentsBody.innerHTML = appointments.map(app => `
        <tr>
            <td>${formatDateTime(app.date, app.time)}</td>
            <td>${escapeHtml(app.service?.name || 'Услуга удалена')}</td>
            <td>${escapeHtml(app.doctor?.name || 'Врач удалён')}</td>
            <td><span class="status ${app.status?.toLowerCase()}">${getStatusText(app.status)}</span></td>
            <td>
                ${app.status === 'SCHEDULED' ? `
                    <a href="/booking/edit/${app.id}" class="action-btn edit">
                        <i class="fas fa-edit"></i>
                    </a>
                    <button class="action-btn delete" data-id="${app.id}"><i class="fas fa-trash-alt"></i></button>
                ` : ''}
            </td>
        </tr>
    `).join('');

    attachCancelHandlers();
}

function renderPagination(currentPage, totalPages) {
    const container = document.getElementById('paginationContainer');
    if (!container) return;

    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let html = '';
    html += `<button class="page-btn" onclick="goToPage(${currentPage - 1})" ${currentPage === 0 ? 'disabled' : ''}>←</button>`;

    let startPage = Math.max(0, currentPage - 2);
    let endPage = Math.min(totalPages - 1, currentPage + 2);

    if (startPage > 0) {
        html += `<button class="page-btn" onclick="goToPage(0)">1</button>`;
        if (startPage > 1) html += `<span class="page-dots">...</span>`;
    }

    for (let i = startPage; i <= endPage; i++) {
        html += `<button class="page-btn ${i === currentPage ? 'active' : ''}" onclick="goToPage(${i})">${i + 1}</button>`;
    }

    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) html += `<span class="page-dots">...</span>`;
        html += `<button class="page-btn" onclick="goToPage(${totalPages - 1})">${totalPages}</button>`;
    }

    html += `<button class="page-btn" onclick="goToPage(${currentPage + 1})" ${currentPage === totalPages - 1 ? 'disabled' : ''}>→</button>`;
    container.innerHTML = html;
}

function goToPage(page) {
    if (page < 0 || page >= totalPages) return;
    currentPage = page;
    loadAppointments();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function attachCancelHandlers() {
    document.querySelectorAll('.action-btn.delete').forEach(btn => {
        btn.removeEventListener('click', handleCancelClick);
        btn.addEventListener('click', handleCancelClick);
    });
}

function handleCancelClick(e) {
    const btn = e.currentTarget;
    pendingCancelId = btn.getAttribute('data-id');
    document.getElementById('cancelModal').classList.add('show');
}

function getStatusText(status) {
    switch(status) {
        case 'SCHEDULED': return 'Предстоит';
        case 'COMPLETED': return 'Завершено';
        case 'CANCELLED': return 'Отменено';
        default: return status || 'Неизвестно';
    }
}

function formatDateTime(date, time) {
    if (!date) return '';
    return `${date} ${time || ''}`;
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

document.getElementById('confirmCancelBtn')?.addEventListener('click', () => {
    if (pendingCancelId) {
        fetch(`/api/appointments/${pendingCancelId}/cancel`, { method: 'DELETE' })
            .then(() => {
                document.getElementById('cancelModal').classList.remove('show');
                currentPage = 0;
                loadAppointments();
                pendingCancelId = null;
            })
            .catch(error => console.error('Ошибка отмены:', error));
    }
});

document.querySelectorAll('.close-cancel-modal').forEach(btn => {
    btn.addEventListener('click', () => {
        document.getElementById('cancelModal').classList.remove('show');
        pendingCancelId = null;
    });
});

window.addEventListener('click', (e) => {
    const cancelModal = document.getElementById('cancelModal');
    if (e.target === cancelModal) {
        cancelModal.classList.remove('show');
        pendingCancelId = null;
    }
});

statusFilter?.addEventListener('change', () => {
    currentPage = 0;
    loadAppointments();
});

loadAppointments();