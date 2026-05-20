// Скролл хедера
const header = document.getElementById('header');
window.addEventListener('scroll', () => {
    if (window.scrollY > 10) {
        header.classList.add('scrolled');
    } else {
        header.classList.remove('scrolled');
    }
});

// Переключение между разделами
const servicesLink = document.getElementById('servicesLink');
const scheduleLink = document.getElementById('scheduleLink');
const servicesSection = document.getElementById('servicesSection');
const scheduleSection = document.getElementById('scheduleSection');

if (servicesLink) {
    servicesLink.addEventListener('click', (e) => {
        e.preventDefault();
        servicesSection.classList.add('active');
        scheduleSection.classList.remove('active');
    });
}

if (scheduleLink) {
    scheduleLink.addEventListener('click', (e) => {
        e.preventDefault();
        scheduleSection.classList.add('active');
        servicesSection.classList.remove('active');
    });
}

// Выпадающее меню пользователя
const userMenu = document.getElementById('userMenu');
const userDropdown = document.getElementById('userDropdown');

if (userMenu) {
    userMenu.addEventListener('click', (e) => {
        e.stopPropagation();
        userDropdown.classList.toggle('show');
    });

    document.addEventListener('click', () => {
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

function openModal() {
    modal.classList.add('show');
}

function closeModalWindow() {
    modal.classList.remove('show');
}

if (settingsBtn) settingsBtn.addEventListener('click', (e) => { e.preventDefault(); openModal(); });
if (settingsBtnSidebar) settingsBtnSidebar.addEventListener('click', openModal);
if (closeModal) closeModal.addEventListener('click', closeModalWindow);
if (cancelBtn) cancelBtn.addEventListener('click', closeModalWindow);

window.addEventListener('click', (e) => {
    if (e.target === modal) closeModalWindow();
});

// Обновление профиля
function updateProfileUI() {
    const lastName = document.getElementById('lastName').value;
    const firstName = document.getElementById('firstName').value;
    const patronymic = document.getElementById('patronymic').value;
    const birthdate = document.getElementById('birthdate').value;
    const gender = document.getElementById('gender').value;
    const phone = document.getElementById('phone').value;
    const email = document.getElementById('email').value;
    const specialization = document.getElementById('specialization').value;
    const experience = document.getElementById('experience').value;

    const formattedDate = new Date(birthdate).toLocaleDateString('ru-RU', {
        day: 'numeric', month: 'long', year: 'numeric'
    });

    const genderText = gender === 'female' ? 'Женский' : 'Мужской';
    const genderIcon = gender === 'female' ? 'fa-venus' : 'fa-mars';

    document.getElementById('userNameBtn').innerHTML = `<i class="fas fa-user-md"></i> ${firstName} ${lastName} <i class="fas fa-chevron-down"></i>`;
    document.getElementById('profileName').textContent = `${firstName} ${lastName}`;
    document.getElementById('profileSpecialization').textContent = specialization;
    document.getElementById('profileBirthdate').textContent = formattedDate;
    document.getElementById('profileGender').innerHTML = `<i class="fas ${genderIcon}"></i> ${genderText}`;
    document.getElementById('profilePhone').innerHTML = `<i class="fas fa-phone"></i> ${phone}`;
    document.getElementById('profileEmail').innerHTML = `<i class="fas fa-envelope"></i> ${email}`;
    document.getElementById('profileExperience').innerHTML = `<i class="fas fa-stethoscope"></i> Опыт: ${experience} лет`;

    alert('Данные сохранены!');
}

if (profileForm) {
    profileForm.addEventListener('submit', (e) => {
        e.preventDefault();
        updateProfileUI();
        closeModalWindow();
    });
}