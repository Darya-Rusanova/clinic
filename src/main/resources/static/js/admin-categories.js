document.addEventListener('DOMContentLoaded', function() {
const modal = document.getElementById('categoryModal');
const deleteModal = document.getElementById('deleteModal');
const modalTitle = document.getElementById('modalTitle');
const categoryForm = document.getElementById('categoryForm');
const categoryIdInput = document.getElementById('categoryId');
const categoryNameInput = document.getElementById('categoryName');

let pendingDeleteId = null;

const openAddModalBtn = document.getElementById('openAddModalBtn');
if (openAddModalBtn) {
  openAddModalBtn.addEventListener('click', () => {
      modalTitle.textContent = 'Добавить категорию';
      categoryIdInput.value = '';
      categoryNameInput.value = '';
      categoryForm.action = '/admin/categories/add';
      modal.classList.add('show');
  });
}

window.openEditModal = function(id, name) {
  modalTitle.textContent = 'Редактировать категорию';
  categoryIdInput.value = id;
  categoryNameInput.value = name;
  categoryForm.action = '/admin/categories/edit';
  modal.classList.add('show');
};

function showDeleteModal(id) {
  pendingDeleteId = id;
  deleteModal.classList.add('show');
}

function closeModal() {
  modal.classList.remove('show');
  deleteModal.classList.remove('show');
  pendingDeleteId = null;
}

const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
if (confirmDeleteBtn) {
  confirmDeleteBtn.addEventListener('click', () => {
      if (pendingDeleteId) {
          window.location.href = '/admin/categories/delete?id=' + pendingDeleteId;
      }
  });
}

document.querySelectorAll('.close-modal, .close-delete-modal').forEach(btn => {
  btn.addEventListener('click', closeModal);
});

document.getElementById('cancelModalBtn')?.addEventListener('click', closeModal);

window.addEventListener('click', (e) => {
  if (e.target === modal) closeModal();
  if (e.target === deleteModal) closeModal();
});

document.querySelectorAll('.action-btn.edit').forEach(btn => {
  btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-id');
      const name = btn.getAttribute('data-name');
      openEditModal(id, name);
  });
});

document.querySelectorAll('.action-btn.delete').forEach(btn => {
  btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-id');
      showDeleteModal(id);
  });
});
});