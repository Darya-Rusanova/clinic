const categoryFilter = document.getElementById('categoryFilter');
const servicesBody = document.getElementById('servicesTableBody');
const deleteModal = document.getElementById('deleteModal');
const deleteServiceNameSpan = document.getElementById('deleteServiceName');
let pendingDeleteId = null;

function loadServices() {
  const categoryId = categoryFilter?.value || '';
  let url = '/admin/api/services';
  if (categoryId) url += `?categoryId=${categoryId}`;

  fetch(url)
      .then(response => response.json())
      .then(services => {
          if (services.length === 0) {
              servicesBody.innerHTML = '<tr><td colspan="6" style="text-align: center">Нет услуг</td></tr>';
              return;
          }
          servicesBody.innerHTML = services.map(service => `
              <tr>
                  <td>${escapeHtml(service.name)}</td>
                  <td>${escapeHtml(service.categoryName || '—')}</td>
                  <td>${service.duration} мин</td>
                  <td>${service.price.toLocaleString()} ₽</td>
                  <td>${service.doctors || 0} чел.</td>
                  <td>
                      <a href="/admin/services/edit/${service.id}" class="action-btn edit"><i class="fas fa-edit"></i></a>
                      <button class="action-btn delete" data-id="${service.id}" data-name="${escapeHtml(service.name)}"><i class="fas fa-trash-alt"></i></button>
                  </td>
              </tr>
          `).join('');

          document.querySelectorAll('.action-btn.delete').forEach(btn => {
              btn.addEventListener('click', () => {
                  const id = btn.getAttribute('data-id');
                  const name = btn.getAttribute('data-name');
                  pendingDeleteId = id;
                  deleteServiceNameSpan.textContent = name;
                  deleteModal.classList.add('show');
              });
          });
      })
      .catch(error => {
          console.error('Ошибка:', error);
          servicesBody.innerHTML = '<tr><td colspan="6" style="text-align: center">Ошибка загрузки</td></tr>';
      });
}

const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
if (confirmDeleteBtn) {
confirmDeleteBtn.addEventListener('click', () => {
    if (pendingDeleteId) {
        fetch(`/admin/services/delete?id=${pendingDeleteId}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                deleteModal.classList.remove('show');
                loadServices();
                pendingDeleteId = null;
                showNotification('Услуга успешно удалена', 'success');
            } else {
                showNotification(result.error || 'Ошибка при удалении', 'error');
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            showNotification('Ошибка сервера', 'error');
        });
    } else {
        deleteModal.classList.remove('show');
    }
});
}


function showNotification(message, type) {
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
`;
notification.style.backgroundColor = type === 'success' ? '#4CAF50' : '#F44336';
document.body.appendChild(notification);
setTimeout(() => notification.remove(), 3000);
}

function closeDeleteModal() {
  deleteModal.classList.remove('show');
  pendingDeleteId = null;
}

document.querySelectorAll('.close-delete-modal').forEach(btn => {
  btn.addEventListener('click', closeDeleteModal);
});

window.addEventListener('click', (e) => {
  if (e.target === deleteModal) closeDeleteModal();
});

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/[&<>]/g, function(m) {
      if (m === '&') return '&amp;';
      if (m === '<') return '&lt;';
      if (m === '>') return '&gt;';
      return m;
  });
}

categoryFilter.addEventListener('change', loadServices);
loadServices();