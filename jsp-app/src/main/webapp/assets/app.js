(() => {
  const menu = document.getElementById('menu-button');
  const backdrop = document.getElementById('sidebar-backdrop');
  const closeMenu = () => { document.body.classList.remove('menu-open'); menu?.setAttribute('aria-expanded', 'false'); };
  menu?.addEventListener('click', () => { const open = document.body.classList.toggle('menu-open'); menu.setAttribute('aria-expanded', String(open)); });
  backdrop?.addEventListener('click', closeMenu);
  document.addEventListener('keydown', e => { if (e.key === 'Escape') closeMenu(); });
  const passwordToggle = document.getElementById('toggle-password');
  passwordToggle?.addEventListener('click', () => { const field = document.getElementById('password'); const visible = field.type === 'password'; field.type = visible ? 'text' : 'password'; passwordToggle.setAttribute('aria-label', visible ? 'Sembunyikan kata sandi' : 'Tampilkan kata sandi'); });
  const dialog = document.getElementById('delete-dialog');
  let pendingForm = null;
  document.querySelectorAll('.delete-form').forEach(form => form.addEventListener('submit', e => { e.preventDefault(); pendingForm = form; document.getElementById('delete-description').textContent = `“${form.dataset.title}” akan dihapus. Tindakan ini tidak dapat dikembalikan.`; dialog.showModal(); }));
  document.getElementById('cancel-delete')?.addEventListener('click', () => { pendingForm = null; dialog.close(); });
  document.getElementById('confirm-delete')?.addEventListener('click', () => { if (pendingForm) { const form = pendingForm; pendingForm = null; dialog.close(); HTMLFormElement.prototype.submit.call(form); } });
  dialog?.addEventListener('cancel', () => { pendingForm = null; });
})();
