document.querySelectorAll('.nav-link, .btn-book').forEach(link => {
    link.addEventListener('click', function(e) {
        const href = this.getAttribute('href');
        if (href && href.startsWith('#')) {
            e.preventDefault();
            const target = document.querySelector(href);
            if (target) {
                target.scrollIntoView({ behavior: 'smooth' });
            }
        }
    });
});

const header = document.getElementById('header');
window.addEventListener('scroll', () => {
    if (window.scrollY > 10) {
        header.classList.add('scrolled');
    } else {
        header.classList.remove('scrolled');
    }
});

document.querySelectorAll('.doctor-details-btn').forEach(button => {
    button.addEventListener('click', function() {
        const doctorId = this.getAttribute('data-id');
        const detailsDiv = document.getElementById('details-' + doctorId);
        const isVisible = detailsDiv.style.display === 'block';

        document.querySelectorAll('.doctor-details').forEach(div => {
            if (div.id !== 'details-' + doctorId) {
                div.style.display = 'none';
            }
        });

        if (isVisible) {
            detailsDiv.style.display = 'none';
            this.textContent = 'Подробнее ▼';
        } else {
            detailsDiv.style.display = 'block';
            this.textContent = 'Свернуть ▲';
        }
    });
});

