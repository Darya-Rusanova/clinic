 const searchInput = document.getElementById('searchInput');
  const clientsBody = document.getElementById('clientsTableBody');
  let searchTimeout;

  const editModal = document.getElementById('editClientModal');
  const editForm = document.getElementById('editClientForm');
  let currentEditId = null;

  const deleteModal = document.getElementById('deleteClientModal');
  const deleteClientNameSpan = document.getElementById('deleteClientName');
  let pendingDeleteId = null;

  function loadClients() {
      const search = searchInput?.value || '';
      fetch(`/admin/api/clients?search=${encodeURIComponent(search)}`)
          .then(response => response.json())
          .then(clients => {
              if (clients.length === 0) {
                  clientsBody.innerHTML = '<tr><td colspan="5" style="text-align: center">Клиенты не найдены<\/td></tr>';
                  return;
              }
              clientsBody.innerHTML = clients.map(client => `
                  <tr>
                      <td>
                          <div class="table-user">
                              <i class="fas fa-user-circle"></i>
                              <span>${escapeHtml(client.name)}</span>
                          </div>
                      <\/td>
                      <td>
                          ${escapeHtml(client.phone)}<br>
                          <span class="small-text">${escapeHtml(client.email || '—')}</span>
                      <\/td>
                      <td>${client.birthDate || '—'}<\/td>
                      <td>${client.appointmentsCount || 0}<\/td>
                      <td>
                          <button class="action-btn edit-client" data-id="${client.id}"
                                  data-name="${escapeHtml(client.name)}" data-phone="${escapeHtml(client.phone)}"
                                  data-email="${escapeHtml(client.email || '')}" data-birth="${client.birthDate || ''}"
                                  data-lastname="${escapeHtml(client.lastName || '')}" data-firstname="${escapeHtml(client.firstName || '')}"
                                  data-patronymic="${escapeHtml(client.patronymic || '')}" data-gender="${client.gender || 'true'}">
                              <i class="fas fa-edit"></i>
                          </button>
                          <a href="/admin/clients/${client.id}/appointments" class="action-btn view"><i class="fas fa-calendar-alt"></i></a>
                          <button class="action-btn delete-client" data-id="${client.id}" data-name="${escapeHtml(client.name)}"><i class="fas fa-trash-alt"></i></button>
                      <\/td>
                  </tr>
              `).join('');

              attachEditHandlers();
              attachDeleteHandlers();
          })
          .catch(error => {
              console.error('Ошибка:', error);
              clientsBody.innerHTML = '<tr><td colspan="5" style="text-align: center">Ошибка загрузки<\/td></tr>';
          });
  }

  function attachEditHandlers() {
      document.querySelectorAll('.action-btn.edit-client').forEach(btn => {
          btn.addEventListener('click', () => {
              currentEditId = btn.getAttribute('data-id');
              document.getElementById('editClientId').value = currentEditId;
              document.getElementById('editLastName').value = btn.getAttribute('data-lastname') || '';
              document.getElementById('editFirstName').value = btn.getAttribute('data-firstname') || '';
              document.getElementById('editPatronymic').value = btn.getAttribute('data-patronymic') || '';
              document.getElementById('editPhone').value = btn.getAttribute('data-phone') || '';
              document.getElementById('editEmail').value = btn.getAttribute('data-email') || '';
              document.getElementById('editBirthDate').value = btn.getAttribute('data-birth') || '';
              document.getElementById('editGender').value = btn.getAttribute('data-gender') || 'true';
              editModal.classList.add('show');
          });
      });
  }

  function attachDeleteHandlers() {
      document.querySelectorAll('.action-btn.delete-client').forEach(btn => {
          btn.addEventListener('click', () => {
              pendingDeleteId = btn.getAttribute('data-id');
              deleteClientNameSpan.textContent = btn.getAttribute('data-name');
              deleteModal.classList.add('show');
          });
      });
  }

  editForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const formData = new FormData(editForm);
      const data = {
          lastName: formData.get('lastName'),
          firstName: formData.get('firstName'),
          patronymic: formData.get('patronymic'),
          phone: formData.get('phone'),
          email: formData.get('email'),
          gender: formData.get('gender') === 'true',
          birthDate: formData.get('birthDate')
      };

      fetch(`/admin/api/clients/${currentEditId}/edit`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(data)
      }).then(() => {
          editModal.classList.remove('show');
          loadClients();
      });
  });

  document.getElementById('confirmDeleteBtn')?.addEventListener('click', () => {
      if (pendingDeleteId) {
          fetch(`/admin/api/clients/${pendingDeleteId}/delete`, { method: 'DELETE' })
              .then(() => {
                  deleteModal.classList.remove('show');
                  loadClients();
                  pendingDeleteId = null;
              });
      }
  });

  function closeEditModal() {
      editModal.classList.remove('show');
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
      return str.replace(/[&<>]/g, function(m) {
          if (m === '&') return '&amp;';
          if (m === '<') return '&lt;';
          if (m === '>') return '&gt;';
          return m;
      });
  }

  if (searchInput) {
      searchInput.addEventListener('input', function() {
          clearTimeout(searchTimeout);
          searchTimeout = setTimeout(() => loadClients(), 300);
      });
  }
  loadClients();
