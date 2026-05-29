const searchInput = document.getElementById('searchInput');
const doctorsBody = document.getElementById('doctorsTableBody');
let searchTimeout;
let allServices = [];

const editModal = document.getElementById('editDoctorModal');
const editForm = document.getElementById('editDoctorForm');
const deleteModal = document.getElementById('deleteDoctorModal');
const deleteDoctorNameSpan = document.getElementById('deleteDoctorName');
let pendingDeleteId = null;
let currentEditDoctorId = null;  // ← добавить

function loadServicesCheckboxes(selectedServiceIds) {
    fetch('/api/services')
        .then(response => response.json())
        .then(services => {
            allServices = services;
            const container = document.getElementById('servicesCheckboxes');
            container.innerHTML = services.map(service => `
                <label class="checkbox-label">
                    <input type="checkbox" name="serviceId" value="${service.id}"
                           ${selectedServiceIds && selectedServiceIds.includes(service.id) ? 'checked' : ''}>
                    <span>${escapeHtml(service.name)} - ${service.price} ₽ (${service.duration} мин)</span>
                </label>
            `).join('');
        })
        .catch(error => {
            console.error('Ошибка загрузки услуг:', error);
            document.getElementById('servicesCheckboxes').innerHTML = '<div class="error">Ошибка загрузки услуг</div>';
        });
}

function getSelectedServiceIds() {
    const checkboxes = document.querySelectorAll('#servicesCheckboxes input[type="checkbox"]:checked');
    return Array.from(checkboxes).map(cb => parseInt(cb.value));
}

function setupImageUpload(fileInputId, previewId, placeholderId, hiddenInputId, previewImgId, uploadAreaId, removeType) {
    const fileInput = document.getElementById(fileInputId);
    const uploadArea = document.getElementById(uploadAreaId);

    if (!fileInput) return;

    fileInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);

        const placeholder = document.getElementById(placeholderId);
        if (placeholder) {
            placeholder.innerHTML = '<i class="fas fa-spinner fa-pulse"></i><p>Загрузка...</p>';
        }

        fetch('/admin/api/upload-image', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                document.getElementById(hiddenInputId).value = data.url;

                const oldPlaceholder = document.getElementById(placeholderId);
                if (oldPlaceholder) oldPlaceholder.remove();

                const oldPreview = document.getElementById(previewId);
                if (oldPreview) oldPreview.remove();

                const newPreview = document.createElement('div');
                newPreview.id = previewId;
                newPreview.className = 'image-preview';
                newPreview.innerHTML = `
                    <img src="${data.url}" alt="Preview">
                    <button type="button" class="remove-image" onclick="removeImage('${removeType}')"><i class="fas fa-times"></i></button>
                `;
                uploadArea.appendChild(newPreview);
            } else {
                alert('Ошибка загрузки');
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            alert('Ошибка загрузки');
        });
    });
}

function removeImage(type) {
    if (type === 'image') {
        document.getElementById('editImagePath').value = '';
        const preview = document.getElementById('imagePreview');
        if (preview) preview.remove();

        const uploadArea = document.getElementById('imageUploadArea');
        const newPlaceholder = document.createElement('div');
        newPlaceholder.id = 'imagePlaceholder';
        newPlaceholder.className = 'upload-placeholder';
        newPlaceholder.onclick = () => document.getElementById('imageFile').click();
        newPlaceholder.innerHTML = '<i class="fas fa-cloud-upload-alt"></i><p>Загрузить фото</p><span class="upload-hint">PNG, JPG, JPEG до 5MB</span>';
        uploadArea.appendChild(newPlaceholder);
        document.getElementById('imageFile').value = '';
    } else if (type === 'license') {
        document.getElementById('editLicensePath').value = '';
        const preview = document.getElementById('licensePreview');
        if (preview) preview.remove();

        const uploadArea = document.getElementById('licenseUploadArea');
        const newPlaceholder = document.createElement('div');
        newPlaceholder.id = 'licensePlaceholder';
        newPlaceholder.className = 'upload-placeholder';
        newPlaceholder.onclick = () => document.getElementById('licenseFile').click();
        newPlaceholder.innerHTML = '<i class="fas fa-cloud-upload-alt"></i><p>Загрузить лицензию</p><span class="upload-hint">PNG, JPG, PDF до 5MB</span>';
        uploadArea.appendChild(newPlaceholder);
        document.getElementById('licenseFile').value = '';
    }
}

window.removeImage = removeImage;

function loadDoctors() {
    const search = searchInput?.value || '';
    fetch(`/admin/api/doctors?search=${encodeURIComponent(search)}`)
        .then(response => response.json())
        .then(doctors => {
            if (doctors.length === 0) {
                doctorsBody.innerHTML = '<tr><td colspan="5" style="text-align: center">Врачи не найдены</td></tr>';
                return;
            }
            doctorsBody.innerHTML = doctors.map(doctor => `
                <tr>
                    <td>
                        <div class="table-user">
                            <i class="fas fa-user-md"></i>
                            <span>${escapeHtml(doctor.name)}</span>
                        </div>
                    </td>
                    <td>
                        ${escapeHtml(doctor.phone)}<br>
                        <span class="small-text">${escapeHtml(doctor.email || '—')}</span>
                    </td>
                    <td>${doctor.experienceYear || 0} лет</td>
                    <td>${doctor.services || 0}</td>
                    <td>
                        <a href="/admin/doctors/${doctor.id}/schedule" class="action-btn schedule">
                            <i class="fas fa-calendar-alt"></i>
                        </a>
                        <button class="action-btn edit-doctor" data-id="${doctor.id}">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="action-btn delete-doctor" data-id="${doctor.id}" data-name="${escapeHtml(doctor.name)}">
                            <i class="fas fa-trash-alt"></i>
                        </button>
                    </td>
                </tr>
            `).join('');

            attachEditHandlers();
            attachDeleteHandlers();
        })
        .catch(error => {
            console.error('Ошибка:', error);
            doctorsBody.innerHTML = '<tr><td colspan="5" style="text-align: center">Ошибка загрузки</td></tr>';
        });
}

function attachEditHandlers() {
    document.querySelectorAll('.action-btn.edit-doctor').forEach(btn => {
        btn.addEventListener('click', () => {
            const doctorId = btn.getAttribute('data-id');
            currentEditDoctorId = doctorId;

            fetch(`/admin/api/doctors/${doctorId}`)
                .then(response => response.json())
                .then(doctor => {
                    document.getElementById('editDoctorId').value = doctor.id;
                    document.getElementById('editLastName').value = doctor.lastName || '';
                    document.getElementById('editFirstName').value = doctor.firstName || '';
                    document.getElementById('editPatronymic').value = doctor.patronymic || '';
                    document.getElementById('editPhone').value = doctor.phone || '';
                    document.getElementById('editEmail').value = doctor.email || '';
                    document.getElementById('editGender').value = doctor.gender || 'true';
                    document.getElementById('editExperience').value = doctor.experienceYear || 0;
                    document.getElementById('editBio').value = doctor.bio || '';

                    const imagePath = doctor.imagePath;
                    const licensePath = doctor.licensePath;

                    const imageUploadArea = document.getElementById('imageUploadArea');
                    const oldImagePreview = document.getElementById('imagePreview');
                    const oldImagePlaceholder = document.getElementById('imagePlaceholder');

                    if (oldImagePreview) oldImagePreview.remove();
                    if (oldImagePlaceholder) oldImagePlaceholder.remove();

                    if (imagePath && imagePath !== '') {
                        document.getElementById('editImagePath').value = imagePath;
                        const newPreview = document.createElement('div');
                        newPreview.id = 'imagePreview';
                        newPreview.className = 'image-preview';
                        newPreview.innerHTML = `
                            <img src="${imagePath}" alt="Preview">
                            <button type="button" class="remove-image" onclick="removeImage('image')"><i class="fas fa-times"></i></button>
                        `;
                        imageUploadArea.appendChild(newPreview);
                    } else {
                        document.getElementById('editImagePath').value = '';
                        const newPlaceholder = document.createElement('div');
                        newPlaceholder.id = 'imagePlaceholder';
                        newPlaceholder.className = 'upload-placeholder';
                        newPlaceholder.onclick = () => document.getElementById('imageFile').click();
                        newPlaceholder.innerHTML = '<i class="fas fa-cloud-upload-alt"></i><p>Загрузить фото</p><span class="upload-hint">PNG, JPG, JPEG до 5MB</span>';
                        imageUploadArea.appendChild(newPlaceholder);
                    }

                    // Обработка лицензии
                    const licenseUploadArea = document.getElementById('licenseUploadArea');
                    const oldLicensePreview = document.getElementById('licensePreview');
                    const oldLicensePlaceholder = document.getElementById('licensePlaceholder');

                    if (oldLicensePreview) oldLicensePreview.remove();
                    if (oldLicensePlaceholder) oldLicensePlaceholder.remove();

                    if (licensePath && licensePath !== '') {
                        document.getElementById('editLicensePath').value = licensePath;
                        const newPreview = document.createElement('div');
                        newPreview.id = 'licensePreview';
                        newPreview.className = 'image-preview';
                        newPreview.innerHTML = `
                            <img src="${licensePath}" alt="License Preview">
                            <button type="button" class="remove-image" onclick="removeImage('license')"><i class="fas fa-times"></i></button>
                        `;
                        licenseUploadArea.appendChild(newPreview);
                    } else {
                        document.getElementById('editLicensePath').value = '';
                        const newPlaceholder = document.createElement('div');
                        newPlaceholder.id = 'licensePlaceholder';
                        newPlaceholder.className = 'upload-placeholder';
                        newPlaceholder.onclick = () => document.getElementById('licenseFile').click();
                        newPlaceholder.innerHTML = '<i class="fas fa-cloud-upload-alt"></i><p>Загрузить лицензию</p><span class="upload-hint">PNG, JPG, PDF до 5MB</span>';
                        licenseUploadArea.appendChild(newPlaceholder);
                    }

                    // Загружаем услуги врача
                    fetch(`/admin/api/doctors/${doctorId}/services`)
                        .then(response => response.json())
                        .then(serviceIds => {
                            loadServicesCheckboxes(serviceIds);
                        });

                    editModal.classList.add('show');
                })
                .catch(error => {
                    console.error('Ошибка загрузки данных врача:', error);
                    showNotification('Ошибка загрузки данных врача', 'error');
                });
        });
    });
}

function attachDeleteHandlers() {
    document.querySelectorAll('.action-btn.delete-doctor').forEach(btn => {
        btn.addEventListener('click', () => {
            pendingDeleteId = btn.getAttribute('data-id');
            deleteDoctorNameSpan.textContent = btn.getAttribute('data-name');
            deleteModal.classList.add('show');
        });
    });
}

editForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const formData = new FormData(editForm);
    const selectedServiceIds = getSelectedServiceIds();

    const data = {
        lastName: formData.get('lastName'),
        firstName: formData.get('firstName'),
        patronymic: formData.get('patronymic'),
        phone: formData.get('phone'),
        email: formData.get('email'),
        gender: formData.get('gender') === 'true',
        experienceYear: parseInt(formData.get('experienceYear')),
        imagePath: formData.get('imagePath'),
        licensePath: formData.get('licensePath'),
        bio: formData.get('bio'),
        serviceIds: selectedServiceIds
    };
    const doctorId = formData.get('doctorId');

    fetch(`/admin/api/doctors/${doctorId}/edit`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(result => {
        if (result.success) {
            editModal.classList.remove('show');
            loadDoctors();
            showNotification('Врач успешно обновлён', 'success');
        } else {
            showNotification(result.error || 'Ошибка при сохранении', 'error');
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        showNotification('Ошибка сервера', 'error');
    });
});

document.getElementById('confirmDeleteBtn')?.addEventListener('click', () => {
    if (pendingDeleteId) {
        fetch(`/admin/api/doctors/${pendingDeleteId}/delete`, { method: 'DELETE' })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    deleteModal.classList.remove('show');
                    loadDoctors();
                    pendingDeleteId = null;
                    showNotification('Врач успешно удалён', 'success');
                } else {
                    showNotification(result.error || 'Ошибка при удалении', 'error');
                }
            })
            .catch(error => {
                console.error('Ошибка:', error);
                showNotification('Ошибка сервера', 'error');
            });
    }
});

function closeEditModal() {
    editModal.classList.remove('show');
    currentEditDoctorId = null;
}

function closeDeleteModal() {
    deleteModal.classList.remove('show');
    pendingDeleteId = null;
}

document.querySelectorAll('.close-edit-modal').forEach(btn => {
    btn.addEventListener('click', closeEditModal);
});
document.querySelectorAll('.close-delete-modal').forEach(btn => {
    btn.addEventListener('click', closeDeleteModal);
});
window.addEventListener('click', (e) => {
    if (e.target === editModal) closeEditModal();
    if (e.target === deleteModal) closeDeleteModal();
});

function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
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

setupImageUpload('imageFile', 'imagePreview', 'imagePlaceholder', 'editImagePath', 'previewImg', 'imageUploadArea', 'image');
setupImageUpload('licenseFile', 'licensePreview', 'licensePlaceholder', 'editLicensePath', 'licensePreviewImg', 'licenseUploadArea', 'license');

if (searchInput) {
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => loadDoctors(), 300);
    });
}

loadDoctors();