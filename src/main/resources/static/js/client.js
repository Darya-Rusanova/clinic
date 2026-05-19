// Выпадающее меню пользователя
const userMenu = document.getElementById('userMenu');
const userDropdown = document.getElementById('userDropdown');

if (userMenu) {
    userMenu.addEventListener('click', function(e) {
        e.stopPropagation();
        userDropdown.classList.toggle('show');
    });

    document.addEventListener('click', function() {
        userDropdown.classList.remove('show');
    });
}

// Модальное окно настроек
const modal = document.getElementById('settingsModal');
const settingsBtn = document.getElementById('settingsBtn');
const settingsBtnSidebar = document.getElementById('settingsBtnSidebar');
const closeModal = document.querySelector('.close-modal');
const cancelBtn = document.getElementById('cancelBtn');
const profileForm = document.getElementById('profileForm');
const userNameSpan = document.querySelector('.user-name');

// Открыть модальное окно (оба триггера)
function openModal() {
    modal.classList.add('show');
}

if (settingsBtn) settingsBtn.addEventListener('click', (e) => { e.preventDefault(); openModal(); });
if (settingsBtnSidebar) settingsBtnSidebar.addEventListener('click', openModal);

// Закрыть модальное окно
function closeModalWindow() {
    modal.classList.remove('show');
}

if (closeModal) closeModal.addEventListener('click', closeModalWindow);
if (cancelBtn) cancelBtn.addEventListener('click', closeModalWindow);

// Закрыть по клику вне окна
window.addEventListener('click', function(e) {
    if (e.target === modal) {
        closeModalWindow();
    }
});

// Обновление профиля на странице
function updateProfileUI() {
    const lastName = document.getElementById('lastName').value;
    const firstName = document.getElementById('firstName').value;
    const patronymic = document.getElementById('patronymic').value;
    const birthdate = document.getElementById('birthdate').value;
    const gender = document.getElementById('gender').value;
    const phone = document.getElementById('phone').value;
    const email = document.getElementById('email').value;
    const city = document.getElementById('city').value;

    const formattedDate = new Date(birthdate).toLocaleDateString('ru-RU', {
        day: 'numeric', month: 'long', year: 'numeric'
    });

    const genderText = gender === 'female' ? 'Женский' : 'Мужской';
    const genderIcon = gender === 'female' ? 'fas fa-venus' : 'fas fa-mars';

    // Обновляем имя в меню
    if (userNameSpan) {
        userNameSpan.innerHTML = `<i class="fas fa-user-circle"></i> ${firstName} ${lastName} <i class="fas fa-chevron-down"></i>`;
    }

    // Обновляем карточку профиля
    document.getElementById('profileName').textContent = `${firstName} ${lastName}`;
    document.getElementById('profilePatronymic').textContent = patronymic;
    document.getElementById('profileBirthdate').textContent = formattedDate;
    document.getElementById('profileGender').innerHTML = `<i class="${genderIcon}"></i> ${genderText}`;
    document.getElementById('profilePhone').innerHTML = `<i class="fas fa-phone"></i> ${phone}`;
    document.getElementById('profileEmail').innerHTML = `<i class="fas fa-envelope"></i> ${email}`;
    document.getElementById('profileCity').innerHTML = `<i class="fas fa-city"></i> ${city}`;

    alert('Данные сохранены!');
}

// Сохранение профиля
if (profileForm) {
    profileForm.addEventListener('submit', function(e) {
        e.preventDefault();
        updateProfileUI();
        closeModalWindow();
    });
}