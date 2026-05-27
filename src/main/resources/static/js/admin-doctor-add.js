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

function removeImage(type) {
  if (type === 'image') {
      document.getElementById('imagePath').value = '';
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
      document.getElementById('licensePath').value = '';
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

setupImageUpload('imageFile', 'imagePreview', 'imagePlaceholder', 'imagePath', 'previewImg', 'imageUploadArea', 'image');
setupImageUpload('licenseFile', 'licensePreview', 'licensePlaceholder', 'licensePath', 'licensePreviewImg', 'licenseUploadArea', 'license');