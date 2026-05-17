document.addEventListener('DOMContentLoaded', function() {
    const track = document.getElementById('galleryTrack');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const cards = document.querySelectorAll('.service-card');
    let currentIndex = 0;
    let cardsPerView = 1;

    function updateGallery() {
        const cardWidth = cards[0]?.offsetWidth || 0;
        track.style.transform = `translateX(-${currentIndex * cardWidth}px)`;
    }

    function nextSlide() {
        if (currentIndex < cards.length - cardsPerView) {
            currentIndex++;
            updateGallery();
        }
        else {
            currentIndex = 0;
            updateGallery();
        }
    }

    function prevSlide() {
        if (currentIndex > 0) {
            currentIndex--;
            updateGallery();
        }
        else{
            currentIndex = cards.length-1;
            updateGallery();
        }
    }

    if (prevBtn && nextBtn) {
        nextBtn.addEventListener('click', nextSlide);
        prevBtn.addEventListener('click', prevSlide);
        window.addEventListener('resize', updateGallery);
        updateGallery();
    }
});