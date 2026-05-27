let currentPage = 0;
  let totalPages = 0;

  function loadReportData() {
      const startDate = document.getElementById('startDate').value;
      const endDate = document.getElementById('endDate').value;
      const status = document.getElementById('status').value;

      let url = `/admin/reports/data?page=${currentPage}&size=10`;
      if (startDate) url += `&startDate=${startDate}`;
      if (endDate) url += `&endDate=${endDate}`;
      if (status && status !== 'all') url += `&status=${status}`;

      document.getElementById('reportsBody').innerHTML = '<tr><td colspan="8" class="loading"><i class="fas fa-spinner fa-pulse"></i> Загрузка...<\/td></tr>';

      fetch(url)
          .then(response => response.json())
          .then(data => {
              if (data.appointments && data.appointments.length > 0) {
                  renderReports(data.appointments);
              } else {
                  document.getElementById('reportsBody').innerHTML = '<tr><td colspan="8" class="loading">Нет записей<\/td></tr>';
              }

              if (data.totalPages !== undefined) {
                  totalPages = data.totalPages;
                  renderPagination(data.currentPage || 0, data.totalPages);
              }

              if (data.statistics) {
                  document.getElementById('totalCount').innerText = data.statistics.total || 0;
                  document.getElementById('scheduledCount').innerText = data.statistics.scheduled || 0;
                  document.getElementById('completedCount').innerText = data.statistics.completed || 0;
                  document.getElementById('cancelledCount').innerText = data.statistics.cancelled || 0;
                  document.getElementById('revenue').innerText = (data.statistics.totalRevenue || 0).toLocaleString() + ' ₽';
              }
          })
          .catch(error => {
              console.error('Ошибка:', error);
              document.getElementById('reportsBody').innerHTML = '<tr><td colspan="8" class="loading">Ошибка загрузки<\/td></tr>';
          });
  }

  function renderReports(appointments) {
      const tbody = document.getElementById('reportsBody');
      let html = '';

      for (const app of appointments) {
          let dateTimeStr = '';
          if (app.dateTime) {
              const date = new Date(app.dateTime);
              dateTimeStr = date.toLocaleString('ru-RU', {
                  day: '2-digit',
                  month: '2-digit',
                  year: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit'
              });
          }

          let statusText = '';
          let statusClass = '';
          switch (app.status) {
              case 'SCHEDULED': statusText = 'Предстоит'; statusClass = 'SCHEDULED'; break;
              case 'COMPLETED': statusText = 'Завершено'; statusClass = 'COMPLETED'; break;
              case 'CANCELLED': statusText = 'Отменено'; statusClass = 'CANCELLED'; break;
              default: statusText = app.status || 'Неизвестно'; statusClass = '';
          }

          html += `
              <tr>
                  <td>${app.id || ''}</td>
                  <td>${dateTimeStr}</td>
                  <td>${escapeHtml(app.clientName || 'Клиент удалён')}</td>
                  <td>${escapeHtml(app.clientPhone || '-')}</td>
                  <td>${escapeHtml(app.doctorName || 'Доктор уволен')}</td>
                  <td>${escapeHtml(app.serviceName || 'Услуга удалена')}</td>
                  <td>${(app.price || 0).toLocaleString()} ₽</td>
                  <td><span class="status ${statusClass}">${statusText}</span></td>
              </tr>
          `;
      }
      tbody.innerHTML = html;
  }

  function renderPagination(currentPage, totalPages) {
      const container = document.getElementById('paginationContainer');
      if (!container) return;

      if (totalPages <= 1) {
          container.innerHTML = '';
          return;
      }

      let html = '';
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
      container.innerHTML = html;
  }

  function goToPage(page) {
      if (page < 0 || page >= totalPages) return;
      currentPage = page;
      loadReportData();
      window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function exportToExcel() {
      const startDate = document.getElementById('startDate').value;
      const endDate = document.getElementById('endDate').value;
      const status = document.getElementById('status').value;

      let url = `/admin/reports/export/excel?`;
      if (startDate) url += `startDate=${startDate}&`;
      if (endDate) url += `endDate=${endDate}&`;
      if (status && status !== 'all') url += `status=${status}`;

      window.location.href = url;
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

  document.getElementById('applyFilters').addEventListener('click', () => {
      currentPage = 0;
      loadReportData();
  });

  document.getElementById('exportExcel').addEventListener('click', exportToExcel);

  const today = new Date();
  const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
  document.getElementById('startDate').value = firstDayOfMonth.toISOString().split('T')[0];
  document.getElementById('endDate').value = today.toISOString().split('T')[0];

  loadReportData();
  window.addEventListener('scroll', function() {
    var header = document.querySelector('.header');
    if (window.scrollY > 10) {
        header.classList.add('scrolled');
    } else {
        header.classList.remove('scrolled');
    }
});