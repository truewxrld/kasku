<%@ page contentType="text/html; charset=UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %>
<a class="back-link" href="${ctx}/app/${module.key}">← Kembali ke <c:out value="${module.title}"/></a><section class="page-heading"><div><div class="eyebrow"><span></span> CATATAN YANG MEMBUAT PERBEDAAN</div><h1>${editing?'Edit':'Tambah'} <c:out value="${module.singular}"/></h1><p>Lengkapi detail di bawah. Kolom bertanda * wajib diisi.</p></div></section>
<div class="form-layout"><section class="panel form-panel">
  <div class="panel-heading"><div><h2>Detail <c:out value="${module.singular}"/></h2><p>Informasi yang rapi, keputusan yang lebih baik.</p></div><span class="item-icon green"><svg><use href="${ctx}/assets/icons.svg#${module.icon}"/></svg></span></div>
  <form method="post" action="${ctx}/app/${module.key}/save">
    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
    <c:if test="${editing}"><input type="hidden" name="id" value="${record.id}"></c:if>
    <div class="form-grid"><c:forEach var="field" items="${module.fields}">
      <div class="form-field ${field.type=='textarea'?'full-width':''}">
        <label for="field-${field.name}"><c:out value="${field.label}"/>${field.required?' *':''}</label>
        <c:choose>
          <c:when test="${field.type=='select'}">
            <select id="field-${field.name}" name="${field.name}" required aria-describedby="error-${field.name}">
              <c:forEach var="option" items="${field.options}"><option value="${option.key}" ${record[field.name]==option.key?'selected':''}><c:out value="${option.value}"/></option></c:forEach>
            </select>
          </c:when>
          <c:when test="${field.type=='reference'}">
            <select id="field-${field.name}" name="${field.name}" required aria-describedby="error-${field.name}">
              <option value="">Pilih <c:out value="${field.label}"/></option>
              <c:forEach var="option" items="${choices[field.name]}"><option value="${option.id}" ${record[field.name]==option.id?'selected':''}><c:out value="${option.name}"/><c:if test="${field.name=='category_id'}"> · ${option.kind=='income'?'Pemasukan':'Pengeluaran'}</c:if></option></c:forEach>
            </select>
            <c:if test="${empty choices[field.name]}"><span class="field-hint">Belum ada pilihan. <a href="${ctx}/app/${field.name=='wallet_id'?'wallets':'categories'}/new">Tambahkan <c:out value="${field.label}"/> dahulu</a>.</span></c:if>
          </c:when>
          <c:when test="${field.type=='textarea'}">
            <textarea id="field-${field.name}" name="${field.name}" rows="5" maxlength="${field.max}" ${field.required?'required':''} aria-describedby="error-${field.name}"><c:out value="${record[field.name]}"/></textarea>
          </c:when>
          <c:otherwise>
            <input id="field-${field.name}" name="${field.name}" type="${field.type=='money'?'number':field.type}" value="<c:out value='${record[field.name]}'/>" ${field.required?'required':''} <c:if test="${field.max>0}">maxlength="${field.max}"</c:if> <c:if test="${field.type=='money'}">min="${field.name=='opening_balance'||field.name=='saved_amount'||field.name=='paid_amount'?'0':'0.01'}" max="9999999999999.99" step="0.01" inputmode="decimal"</c:if> aria-describedby="error-${field.name}">
          </c:otherwise>
        </c:choose>
        <span class="field-error" id="error-${field.name}"><c:out value="${errors[field.name]}"/></span>
      </div>
    </c:forEach></div>
    <div class="form-actions"><a class="btn secondary" href="${ctx}/app/${module.key}">Batal</a><button class="btn primary" type="submit"><svg><use href="${ctx}/assets/icons.svg#check"/></svg><c:choose><c:when test="${editing}">Simpan perubahan</c:when><c:otherwise>Simpan <c:out value="${module.singular}"/></c:otherwise></c:choose></button></div>
  </form>
</section><aside class="form-aside"><span class="aside-spark">✳</span><h2>Sedikit lebih rapi.<br>Sedikit lebih tenang.</h2><p><c:out value="${module.help}"/></p><div class="aside-divider"></div><svg><use href="${ctx}/assets/icons.svg#shield"/></svg><strong>Hanya untuk akunmu</strong><p>Data ini tersimpan pada akun yang sedang kamu gunakan.</p></aside></div>
