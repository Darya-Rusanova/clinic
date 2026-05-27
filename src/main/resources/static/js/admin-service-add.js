
const doctorCards = document.querySelectorAll('.doctor-card-select');
const selectedDoctorIdsSet = new Set();
const doctorIdsInput = document.getElementById('selectedDoctorIds');

doctorCards.forEach(card => {
  const doctorId = card.getAttribute('data-doctor-id');
  if (card.classList.contains('selected')) {
      selectedDoctorIdsSet.add(doctorId);
  }
});
doctorIdsInput.value = Array.from(selectedDoctorIdsSet).join(',');

doctorCards.forEach(card => {
  card.addEventListener('click', () => {
      const doctorId = card.getAttribute('data-doctor-id');
      const squareIcon = card.querySelector('.fa-square');
      const checkIcon = card.querySelector('.fa-check-square');

      if (selectedDoctorIdsSet.has(doctorId)) {
          selectedDoctorIdsSet.delete(doctorId);
          card.classList.remove('selected');
          squareIcon.style.display = 'block';
          checkIcon.style.display = 'none';
      } else {
          selectedDoctorIdsSet.add(doctorId);
          card.classList.add('selected');
          squareIcon.style.display = 'none';
          checkIcon.style.display = 'block';
      }

      doctorIdsInput.value = Array.from(selectedDoctorIdsSet).join(',');
  });
});

const imageFileInput = document.getElementById('imageFile');
const imagePathInput = document.getElementById('imagePath');

if (imageFileInput) {
    imageFileInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);

        const placeholder = document.getElementById('uploadPlaceholder');
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
                imagePathInput.value = data.url;

                const oldPlaceholder = document.getElementById('uploadPlaceholder');
                if (oldPlaceholder) oldPlaceholder.remove();

                const oldPreview = document.getElementById('imagePreview');
                if (oldPreview) oldPreview.remove();

                const imageUploadArea = document.getElementById('imageUploadArea');
                const newPreview = document.createElement('div');
                newPreview.id = 'imagePreview';
                newPreview.className = 'image-preview';
                newPreview.innerHTML = `
                    <img src="${data.url}" alt="Preview">
                    <button type="button" class="remove-image" onclick="removeImage()"><i class="fas fa-times"></i></button>
                `;
                imageUploadArea.appendChild(newPreview);
            } else {
                alert('Ошибка загрузки');
                location.reload();
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            alert('Ошибка загрузки');
            location.reload();
        });
    });
}

window.removeImage = function() {
    const imagePathInput = document.getElementById('imagePath');
    imagePathInput.value = '';

    const preview = document.getElementById('imagePreview');
    if (preview) preview.remove();


    const imageUploadArea = document.getElementById('imageUploadArea');
    const newPlaceholder = document.createElement('div');
    newPlaceholder.id = 'uploadPlaceholder';
    newPlaceholder.className = 'upload-placeholder';
    newPlaceholder.onclick = () => document.getElementById('imageFile').click();
    newPlaceholder.innerHTML = '<i class="fas fa-cloud-upload-alt"></i><p>Нажмите или перетащите изображение</p><span class="upload-hint">PNG, JPG, JPEG до 5MB</span>';
    imageUploadArea.appendChild(newPlaceholder);

    window.uploadPlaceholder = newPlaceholder;
};
