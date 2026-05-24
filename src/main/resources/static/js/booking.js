// ========== ШАГ 1 ==========
const categoryFilter = document.getElementById('categoryFilter');
const doctorFilter = document.getElementById('doctorFilter');
const servicesList = document.getElementById('servicesList');
const urlParams = new URLSearchParams(window.location.search);
const preSelectedClientId = urlParams.get('clientId');
const nextBtn = document.getElementById('nextBtn');

let selectedServiceId = null;
let selectedDoctorId = null;

function loadServices() {
    const categoryId = categoryFilter?.value || '';
    const doctorId = doctorFilter?.value || '';

    let url = '/api/services?';
    if (categoryId) url += `categoryId=${categoryId}&`;
    if (doctorId) url += `doctorId=${doctorId}`;

    fetch(url)
        .then(response => response.json())
        .then(services => {
            servicesList.innerHTML = services.map(service => `
                <div class="service-card" data-service-id="${service.id}">
                    <div class="service-info">
                        <h4>${escapeHtml(service.name)}</h4>
                        <p>${escapeHtml(service.description || '')}</p>
                        <div class="service-meta">
                            <span> ${service.duration} мин</span>
                            <span> ${service.price} ₽</span>
                        </div>
                    </div>
                    <button class="btn-select-service">Выбрать</button>
                </div>
            `).join('');

            attachServiceHandlers();
        })
        .catch(error => {
            console.error('Ошибка:', error);
            servicesList.innerHTML = '<div class="error">Ошибка загрузки услуг</div>';
        });
}

function attachServiceHandlers() {
    document.querySelectorAll('.service-card').forEach(card => {
        card.addEventListener('click', (e) => {
            if (e.target.classList.contains('btn-select-service')) {
                selectedServiceId = card.dataset.serviceId;

                fetch(`/api/services/${selectedServiceId}/doctors`)
                    .then(response => response.json())
                    .then(doctors => {
                        if (doctors.length === 1) {
                            selectedDoctorId = doctors[0].userId;
                            nextBtn.disabled = false;
                        } else if (doctors.length > 1) {
                            showDoctorSelection(doctors);
                        } else {
                            alert('Нет доступных врачей для этой услуги');
                        }
                    })
                    .catch(error => {
                        console.error('Ошибка загрузки врачей:', error);
                        alert('Ошибка загрузки списка врачей');
                    });

                document.querySelectorAll('.service-card').forEach(c => c.classList.remove('selected'));
                card.classList.add('selected');
            }
        });
    });
}

function showDoctorSelection(doctors) {
    const modal = document.createElement('div');
    modal.className = 'doctor-modal';
    modal.innerHTML = `
        <div class="doctor-modal-content">
            <h3>Выберите врача</h3>
            ${doctors.map(doc => `
                <button class="doctor-option" data-doctor-id="${doc.userId}">
                    ${escapeHtml(doc.name)} (стаж ${doc.experienceYears} лет)
                </button>
            `).join('')}
            <button class="doctor-option-cancel">Отмена</button>
        </div>
    `;
    document.body.appendChild(modal);

    modal.querySelectorAll('.doctor-option').forEach(btn => {
        btn.addEventListener('click', () => {
            selectedDoctorId = btn.dataset.doctorId;
            modal.remove();
            nextBtn.disabled = false;
        });
    });

    modal.querySelector('.doctor-option-cancel').addEventListener('click', () => {
        modal.remove();
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

if (categoryFilter) categoryFilter.addEventListener('change', loadServices);
if (doctorFilter) doctorFilter.addEventListener('change', loadServices);

if (nextBtn) {
    nextBtn.addEventListener('click', () => {
        if (selectedServiceId && selectedDoctorId) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '/booking/select';
            form.innerHTML = `
                <input type="hidden" name="serviceId" value="${selectedServiceId}">
                <input type="hidden" name="doctorId" value="${selectedDoctorId}">
                ${preSelectedClientId ? `<input type="hidden" name="clientId" value="${preSelectedClientId}">` : ''}
            `;
            document.body.appendChild(form);
            form.submit();
        }
    });
}

if (servicesList) loadServices();

// ========== ШАГ 2 ==========
const datePicker = document.getElementById('datePicker');
const slotsContainer = document.getElementById('slotsContainer');
let selectedSlot = null;


const editMode = urlParams.get('edit');
const preSelectedDate = urlParams.get('date');
const preSelectedTime = urlParams.get('time');
const appointmentIdFromUrl = urlParams.get('appointmentId');

if (datePicker) {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    const minDate = tomorrow.toISOString().split('T')[0];

    datePicker.min = minDate;

    if (editMode && preSelectedDate) {
        datePicker.value = preSelectedDate;
    } else {
        datePicker.value = minDate;
    }

    datePicker.addEventListener('change', loadSlots);
    loadSlots();
}

function loadSlots() {
    const doctorId = document.getElementById('doctorId').value;
    const serviceId = document.getElementById('serviceId').value;
    const date = datePicker.value;

    if (!date) return;

    fetch(`/api/slots?doctorId=${doctorId}&serviceId=${serviceId}&date=${date}`)
        .then(response => response.json())
        .then(slots => {
            if (slots.length === 0) {
                slotsContainer.innerHTML = '<div class="no-slots">Нет доступных слотов на эту дату</div>';
                return;
            }

            slotsContainer.innerHTML = slots.map(slot => `
                <button class="slot-btn" data-slot="${slot}">
                    ${slot}
                </button>
            `).join('');

            attachSlotHandlers();

            if (editMode && preSelectedTime) {
                setTimeout(() => {
                    const slotBtn = document.querySelector(`.slot-btn[data-slot="${preSelectedTime}"]`);
                    if (slotBtn) {
                        slotBtn.click();
                    }
                }, 100);
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            slotsContainer.innerHTML = '<div class="error">Ошибка загрузки слотов</div>';
        });
}

function attachSlotHandlers() {
    document.querySelectorAll('.slot-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.slot-btn').forEach(b => b.classList.remove('selected'));
            btn.classList.add('selected');
            selectedSlot = btn.dataset.slot;
            document.getElementById('nextBtn').disabled = false;
        });
    });
}

const step2NextBtn = document.getElementById('nextBtn');
if (step2NextBtn && slotsContainer) {
    step2NextBtn.addEventListener('click', () => {
        if (selectedSlot) {
            const serviceId = document.getElementById('serviceId').value;
            const doctorId = document.getElementById('doctorId').value;
            const date = datePicker.value;
            const clientId = document.getElementById('clientId')?.value || preSelectedClientId;
            const appointmentId = appointmentIdFromUrl;

            const data = {
                serviceId: parseInt(serviceId),
                doctorId: parseInt(doctorId),
                clientId: parseInt(clientId),
                date: date,
                time: selectedSlot,
                appointmentId: appointmentId ? parseInt(appointmentId) : null
            };

            fetch('/booking/step3', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    window.location.href = result.redirectUrl;
                } else {
                    alert(result.error);
                }
            });
        }
    });
}