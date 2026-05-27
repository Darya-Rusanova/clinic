const doctorId = window.location.pathname.split('/')[3];

const days = [
    { id: 1, name: 'Понедельник' },
    { id: 2, name: 'Вторник' },
    { id: 3, name: 'Среда' },
    { id: 4, name: 'Четверг' },
    { id: 5, name: 'Пятница' },
    { id: 6, name: 'Суббота' },
    { id: 7, name: 'Воскресенье' }
];

function loadDoctorInfo() {
    fetch(`/admin/api/doctors?search=`)
        .then(response => response.json())
        .then(doctors => {
            const doctor = doctors.find(d => d.id == doctorId);
            if (doctor) {
                const doctorNameSpan = document.getElementById('doctorName');
                if (doctorNameSpan) doctorNameSpan.textContent = doctor.name;
            }
        })
        .catch(error => console.error('Ошибка загрузки информации о враче:', error));
}

function loadSchedule() {
    Promise.all([
        fetch(`/admin/api/doctors/${doctorId}/schedule`).then(res => res.json()),
        fetch(`/admin/api/doctors/${doctorId}/appointments-count`).then(res => res.json())
    ])
    .then(([scheduleData, appointmentsCount]) => {
        renderSchedule(scheduleData, appointmentsCount);
    })
    .catch(error => {
        console.error('Ошибка загрузки:', error);
        showNotification('Ошибка загрузки расписания', 'error');
    });
}

function renderSchedule(scheduleData, appointmentsCount) {
    const container = document.getElementById('scheduleContent');
    if (!container) return;
    container.innerHTML = '';

    for (const day of days) {
        const dayData = scheduleData[day.id] || {
            startTime: '09:00',
            endTime: '18:00',
            isDayOff: false,
            breaks: []
        };

        const hasAppointments = appointmentsCount && appointmentsCount[day.id] > 0;
        const isDisabled = hasAppointments;

        const startTimeValue = (dayData.startTime && dayData.startTime !== 'null') ? dayData.startTime : '09:00';
        const endTimeValue = (dayData.endTime && dayData.endTime !== 'null') ? dayData.endTime : '18:00';
        const isDayOffValue = dayData.isDayOff === true;

        const dayCard = document.createElement('div');
        dayCard.className = 'day-card';
        dayCard.dataset.dayId = day.id;
        dayCard.innerHTML = `
            <div class="day-header">
                <span>${day.name}</span>
                <label class="day-off-label ${isDisabled ? 'disabled' : ''}">
                    <input type="checkbox" class="day-off-checkbox"
                           data-day="${day.id}"
                           ${isDayOffValue ? 'checked' : ''}
                           ${isDisabled ? 'disabled' : ''}>
                    <span class="checkbox-custom"></span>
                    <span>Выходной</span>
                </label>
            </div>
            ${hasAppointments ? `
                <div class="has-appointments-warning">
                    <i class="fas fa-exclamation-triangle"></i>
                    На этот день есть записи, выходной и перерывы нельзя изменять
                </div>
            ` : ''}
            <div class="working-hours" ${isDayOffValue ? 'style="display: none"' : ''}>
                <label>Рабочее время:</label>
                <input type="time" class="time-input work-start" value="${startTimeValue}" ${hasAppointments ? 'disabled' : ''}>
                <span class="time-sep">—</span>
                <input type="time" class="time-input work-end" value="${endTimeValue}" ${hasAppointments ? 'disabled' : ''}>
                <span class="error-hint work-error hidden">Начало должно быть раньше конца</span>
            </div>
            <div class="breaks-section" ${isDayOffValue ? 'style="display: none"' : ''}>
                <label>Перерывы:</label>
                <div class="breaks-list"></div>
                ${!hasAppointments ? `
                    <button type="button" class="btn-add-break" data-day="${day.id}">
                        <i class="fas fa-plus"></i> Добавить перерыв
                    </button>
                ` : ''}
            </div>
        `;
        container.appendChild(dayCard);

        const breaksList = dayCard.querySelector('.breaks-list');
        const breaks = dayData.breaks || [];
        for (const b of breaks) {
            if (b.startTime && b.endTime && b.startTime !== 'null' && b.endTime !== 'null') {
                addBreakItem(breaksList, day.id, b.startTime, b.endTime, hasAppointments);
            }
        }
    }

    attachEventHandlers();
}

function addBreakItem(breaksList, dayId, startTime = '12:00', endTime = '13:00', isDisabled = false) {
    const breakItem = document.createElement('div');
    breakItem.className = 'break-item';
    breakItem.innerHTML = `
        <input type="time" class="time-input break-start" value="${startTime}" ${isDisabled ? 'disabled' : ''}>
        <span class="time-sep">—</span>
        <input type="time" class="time-input break-end" value="${endTime}" ${isDisabled ? 'disabled' : ''}>
        <button type="button" class="btn-remove-break" data-day="${dayId}" ${isDisabled ? 'disabled' : ''}>
            <i class="fas fa-trash-alt"></i>
        </button>
        <span class="break-error hidden">Начало перерыва должно быть раньше конца</span>
    `;
    breaksList.appendChild(breakItem);

    if (!isDisabled) {
        const removeBtn = breakItem.querySelector('.btn-remove-break');
        removeBtn.onclick = handleRemoveBreak;

        attachBreakValidation(breakItem);
    }
}

function attachBreakValidation(breakItem) {
    const breakStart = breakItem.querySelector('.break-start');
    const breakEnd = breakItem.querySelector('.break-end');
    const breakError = breakItem.querySelector('.break-error');

    const validateBreak = function() {
        if (breakStart.value && breakEnd.value && breakStart.value >= breakEnd.value) {
            breakError.classList.remove('hidden');
        } else {
            breakError.classList.add('hidden');
        }
    };

    breakStart.onchange = validateBreak;
    breakEnd.onchange = validateBreak;
}

function attachEventHandlers() {

    document.querySelectorAll('.day-off-checkbox').forEach(checkbox => {
        checkbox.onchange = handleDayOffChange;
    });

    document.querySelectorAll('.day-card').forEach(card => {
        const workStart = card.querySelector('.work-start');
        const workEnd = card.querySelector('.work-end');
        const workError = card.querySelector('.work-error');

        if (workStart && workEnd) {
            const validateWork = function() {
                if (workStart.value && workEnd.value && workStart.value >= workEnd.value) {
                    workError.classList.remove('hidden');
                } else {
                    workError.classList.add('hidden');
                }
            };

            workStart.onchange = validateWork;
            workEnd.onchange = validateWork;
        }
    });

    document.querySelectorAll('.btn-add-break').forEach(btn => {
        btn.onclick = handleAddBreak;
    });
}

function handleDayOffChange(e) {
    const checkbox = e.currentTarget;
    const dayCard = checkbox.closest('.day-card');
    const workingHoursDiv = dayCard.querySelector('.working-hours');
    const breaksSection = dayCard.querySelector('.breaks-section');
    const hasAppointmentsWarning = dayCard.querySelector('.has-appointments-warning');
    const hasAppointments = hasAppointmentsWarning !== null;

    if (checkbox.checked && hasAppointments) {
        showNotification('На этот день есть записи, нельзя сделать выходным', 'error');
        checkbox.checked = false;
        return;
    }

    if (checkbox.checked) {
        workingHoursDiv.style.display = 'none';
        breaksSection.style.display = 'none';
    } else {
        workingHoursDiv.style.display = 'block';
        breaksSection.style.display = 'block';
    }
}

function handleAddBreak(e) {
    const btn = e.currentTarget;
    const dayId = btn.dataset.day;
    const dayCard = document.querySelector(`.day-card[data-day-id="${dayId}"]`);
    const breaksList = dayCard.querySelector('.breaks-list');
    addBreakItem(breaksList, dayId);
}

function handleRemoveBreak(e) {
    e.stopPropagation();
    const btn = e.currentTarget;
    const breakItem = btn.closest('.break-item');
    if (breakItem) {
        breakItem.remove();
    }
}

function collectScheduleData() {
    const data = {};

    document.querySelectorAll('.day-card').forEach(card => {
        const dayId = card.dataset.dayId;
        const isDayOffCheckbox = card.querySelector('.day-off-checkbox');
        const isDayOff = isDayOffCheckbox ? isDayOffCheckbox.checked : false;
        const hasAppointmentsWarning = card.querySelector('.has-appointments-warning');
        const hasAppointments = hasAppointmentsWarning !== null;

        if (hasAppointments) {
            return;
        }

        if (isDayOff) {
            data[dayId] = { isDayOff: true, startTime: null, endTime: null, breaks: [] };
        } else {
            let workStart = card.querySelector('.work-start')?.value;
            let workEnd = card.querySelector('.work-end')?.value;

            if (!workStart) workStart = '09:00';
            if (!workEnd) workEnd = '18:00';

            const breaks = [];
            card.querySelectorAll('.break-item').forEach(breakItem => {
                const breakStart = breakItem.querySelector('.break-start')?.value;
                const breakEnd = breakItem.querySelector('.break-end')?.value;
                if (breakStart && breakEnd && breakStart !== '' && breakEnd !== '') {
                    breaks.push({ startTime: breakStart, endTime: breakEnd });
                }
            });

            data[dayId] = { isDayOff: false, startTime: workStart, endTime: workEnd, breaks: breaks };
        }
    });

    return data;
}

function validateAll() {
    let isValid = true;

    document.querySelectorAll('.day-card').forEach(card => {
        const isDayOff = card.querySelector('.day-off-checkbox')?.checked;
        const hasAppointmentsWarning = card.querySelector('.has-appointments-warning');
        const hasAppointments = hasAppointmentsWarning !== null;

        if (hasAppointments) return;
        if (isDayOff) return;

        const workStart = card.querySelector('.work-start')?.value;
        const workEnd = card.querySelector('.work-end')?.value;

        if (workStart && workEnd && workStart >= workEnd) {
            const workError = card.querySelector('.work-error');
            if (workError) workError.classList.remove('hidden');
            isValid = false;
        }

        card.querySelectorAll('.break-item').forEach(breakItem => {
            const breakStart = breakItem.querySelector('.break-start')?.value;
            const breakEnd = breakItem.querySelector('.break-end')?.value;
            if (breakStart && breakEnd && breakStart >= breakEnd) {
                const breakError = breakItem.querySelector('.break-error');
                if (breakError) breakError.classList.remove('hidden');
                isValid = false;
            }
        });
    });

    return isValid;
}

function saveSchedule() {
    if (!validateAll()) {
        showNotification('Исправьте ошибки в расписании', 'error');
        return;
    }

    const saveBtn = document.getElementById('saveScheduleBtn');
    const originalText = saveBtn.textContent;
    saveBtn.textContent = 'Сохранение...';
    saveBtn.disabled = true;

    const data = collectScheduleData();

    console.log('Отправляемые данные:', JSON.stringify(data, null, 2));

    fetch(`/admin/api/doctors/${doctorId}/schedule`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(result => {
        if (result.success) {
            showNotification('Расписание сохранено', 'success');
            setTimeout(() => {
                window.location.href = '/admin/doctors';
            }, 500);
        } else {
            showNotification(result.error || 'Ошибка сохранения', 'error');
            saveBtn.textContent = originalText;
            saveBtn.disabled = false;
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        showNotification('Ошибка сервера', 'error');
        saveBtn.textContent = originalText;
        saveBtn.disabled = false;
    });
}

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

document.addEventListener('DOMContentLoaded', () => {
    const saveBtn = document.getElementById('saveScheduleBtn');
    if (saveBtn) {
        const newSaveBtn = saveBtn.cloneNode(true);
        saveBtn.parentNode.replaceChild(newSaveBtn, saveBtn);
        newSaveBtn.addEventListener('click', saveSchedule);
    }
    loadDoctorInfo();
    loadSchedule();
});