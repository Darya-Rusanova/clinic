 let currentPage = 0;
  let totalPages = 0;
  let sortField = 'dateTime';
  let sortDirection = 'desc';

  function loadAppointments() {
      const status = document.getElementById('statusFilter').value;

      let url = `/admin/api/appointments?page=${currentPage}&size=7&status=${status}&sort=${sortField}&direction=${sortDirection}`;

      fetch(url)
          .then(response => response.json())
          .then(data => {
              renderAppointments(data.appointments);
              renderPagination(data.currentPage, data.totalPages);
              totalPages = data.totalPages;
              updateSortIcons();
          })
          .catch(error => {
              console.error('Ошибка:', error);
              document.getElementById('appointmentsBody').innerHTML = '<tr><td colspan="5" style="text-align: center">Ошибка загрузки</td></tr>';
          });
  }

  function renderAppointments(appointments) {
      const tbody = document.getElementById('appointmentsBody');

      if (!appointments || appointments.length === 0) {
          tbody.innerHTML = '<tr><td colspan="5" style="text-align: center">Нет записей</td></tr>';
          return;
      }

      tbody.innerHTML = appointments.map(app => `
          <tr>
              <td>${formatDateTime(app.dateTime)}</td>
              <td>${escapeHtml(app.clientName)}</td>
              <td>${escapeHtml(app.serviceName)}</td>
              <td>${escapeHtml(app.doctorName)}</td>
              <td><span class="status ${app.status.toLowerCase()}">${getStatusText(app.status)}</span></td>
          </tr>
      `).join('');
  }

  function renderPagination(currentPage, totalPages) {
      const container = document.getElementById('paginationContainer');

      if (totalPages <= 1) {
          container.innerHTML = '';
          return;
      }

      let html = '<div class="pagination">';
      html += `<button class="page-btn" onclick="goToPage(${currentPage - 1})" ${currentPage === 0 ? 'disabled' : ''}>←</button>`;

      let startPage = Math.max(0, currentPage - 2);
      let endPage = Math.min(totalPages - 1, currentPage + 2);

      if (startPage > 0) {
          html += `<button class="page-btn" onclick="goToPage(0)">1</button>`;
          if (startPage > 1) html += `<span class="page-dots">...</span>`;
      }

      for (let i = startPage; i <= endPage; i++) {
          html += `<button class="page-btn ${i === currentPage ? 'active' : ''}" onclick="goToPage(${i})">${i + 1}</button>`;
      }

      if (endPage < totalPages - 1) {
          if (endPage < totalPages - 2) html += `<span class="page-dots">...</span>`;
          html += `<button class="page-btn" onclick="goToPage(${totalPages - 1})">${totalPages}</button>`;
      }

      html += `<button class="page-btn" onclick="goToPage(${currentPage + 1})" ${currentPage === totalPages - 1 ? 'disabled' : ''}>→</button>`;
      html += '</div>';

      container.innerHTML = html;
  }

  function goToPage(page) {
      if (page < 0 || page >= totalPages) return;
      currentPage = page;
      loadAppointments();
      window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function changeSort(field) {
      if (sortField === field) {
          sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
      } else {
          sortField = field;
          sortDirection = 'desc';
      }
      currentPage = 0;
      loadAppointments();
  }

  function updateSortIcons() {
      document.querySelectorAll('.sort-btn').forEach(btn => {
          const field = btn.getAttribute('data-sort');
          const icon = btn.querySelector('.sort-icon');
          icon.classList.remove('fa-arrow-down', 'fa-arrow-up', 'fa-sort');

          if (field === sortField) {
              if (sortDirection === 'asc') {
                  icon.classList.add('fa-arrow-up');
              } else {
                  icon.classList.add('fa-arrow-down');
              }
              btn.classList.add('active');
          } else {
              icon.classList.add('fa-sort');
              btn.classList.remove('active');
          }
      });
  }

  function formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '';
      const date = new Date(dateTimeStr);
      return date.toLocaleString('ru-RU', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
      });
  }

  function getStatusText(status) {
      switch(status) {
          case 'SCHEDULED': return 'Предстоит';
          case 'COMPLETED': return 'Завершено';
          case 'CANCELLED': return 'Отменено';
          default: return status;
      }
  }

  function escapeHtml(str) {
      if (!str) return '';
      return str.replace(/[&<>]/g, function(m) {
          if (m === '&') return '&amp;';
          if (m === '<') return '&lt;';
          if (m === '>') return '&gt;';
          return m;
      });
  }

  document.getElementById('statusFilter').addEventListener('change', () => {
      currentPage = 0;
      loadAppointments();
  });

  document.querySelectorAll('.sort-btn').forEach(btn => {
      btn.addEventListener('click', () => {
          const field = btn.getAttribute('data-sort');
          changeSort(field);
      });
  });

  loadAppointments();