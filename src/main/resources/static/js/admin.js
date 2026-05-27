const userMenu = document.getElementById('userMenu');
const userDropdown = document.getElementById('userDropdown');

if (userMenu) {
    userMenu.addEventListener('click', (e) => {
        e.stopPropagation();
        userDropdown.classList.toggle('show');
    });
}

document.addEventListener('click', () => {
    if (userDropdown) {
        userDropdown.classList.remove('show');
    }
});

window.addEventListener('scroll', function() {
    var header = document.querySelector('.header');
    if (header) {
        if (window.scrollY > 10) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
    }
});