package id.kasku;

import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.util.*;

public final class Records {
    private Records() {}
    public static Map<String,Object> find(Catalog.Module module,long userId,long id) throws SQLException {
        var rows=Database.query("SELECT * FROM "+module.getKey()+" WHERE id=? AND user_id=?",id,userId);
        return rows.isEmpty()?null:rows.get(0);
    }
    public static List<Map<String,Object>> list(Catalog.Module module,long userId) throws SQLException {
        String order=module.getKey().equals("transactions")?"transaction_date DESC, id DESC":"id DESC";
        return Database.query("SELECT * FROM "+module.getKey()+" WHERE user_id=? ORDER BY "+order,userId);
    }
    public static Map<String,Object> validate(Catalog.Module module,long userId,long id,Map<String,String> input,Map<String,String> errors) throws SQLException {
        Map<String,Object> values=new LinkedHashMap<>();
        for(Catalog.Field f:module.getFields()) {
            String value=input.getOrDefault(f.getName(),"").trim();
            if(f.isRequired()&&value.isEmpty()){errors.put(f.getName(),f.getLabel()+" wajib diisi.");continue;}
            if(f.getMax()>0&&value.length()>f.getMax()){errors.put(f.getName(),"Maksimal "+f.getMax()+" karakter.");continue;}
            try {
                switch(f.getType()) {
                    case "money" -> {BigDecimal n=new BigDecimal(value);boolean zero=Set.of("opening_balance","saved_amount","paid_amount").contains(f.getName());if(n.scale()>2||n.compareTo(new BigDecimal("9999999999999.99"))>0||n.signum()<(zero?0:1))throw new IllegalArgumentException();values.put(f.getName(),n.setScale(2));}
                    case "date" -> {LocalDate date=LocalDate.parse(value);if(date.getYear()<1900||date.getYear()>2100)throw new IllegalArgumentException();values.put(f.getName(),java.sql.Date.valueOf(date));}
                    case "month" -> {YearMonth month=YearMonth.parse(value);if(month.getYear()<1900||month.getYear()>2100)throw new IllegalArgumentException();values.put(f.getName(),month.toString());}
                    case "select" -> {if(!f.getOptions().containsKey(value))throw new IllegalArgumentException();values.put(f.getName(),value);}
                    case "reference" -> {
                        long ref=Long.parseLong(value);String table=f.getName().equals("wallet_id")?"wallets":"categories";
                        var rows=Database.query("SELECT * FROM "+table+" WHERE id=? AND user_id=?",ref,userId);
                        if(rows.isEmpty()||module.getKey().equals("budgets")&&!"expense".equals(rows.get(0).get("kind")))throw new IllegalArgumentException();
                        values.put(f.getName(),ref);
                    }
                    default -> values.put(f.getName(),value);
                }
            } catch(RuntimeException e){errors.put(f.getName(),f.getType().equals("money")?"Isi nominal yang valid, maksimal 2 angka desimal dan tidak negatif (nominal utama harus > 0).":"Pilihan atau format "+f.getLabel().toLowerCase()+" tidak valid.");}
        }
        if(module.getKey().equals("goals")) compareAmounts(values,errors,"saved_amount","target_amount");
        if(module.getKey().equals("debts")) compareAmounts(values,errors,"paid_amount","amount");
        if(module.getKey().equals("categories")&&id>0&&values.containsKey("kind")) {
            var old=find(module,userId,id);
            if(old!=null&&!old.get("kind").equals(values.get("kind"))) {
                if(!Database.query("SELECT id FROM transactions WHERE category_id=? AND user_id=?",id,userId).isEmpty()||!Database.query("SELECT id FROM budgets WHERE category_id=? AND user_id=?",id,userId).isEmpty()) errors.put("kind","Jenis kategori yang sudah digunakan tidak dapat diubah.");
            }
        }
        return values;
    }
    private static void compareAmounts(Map<String,Object> values,Map<String,String> errors,String a,String b){
        if(values.get(a) instanceof BigDecimal x&&values.get(b) instanceof BigDecimal y&&x.compareTo(y)>0)errors.put(a,"Jumlah tidak boleh melebihi nominal target/awal.");
    }
    public static long save(Catalog.Module module,long userId,long id,Map<String,Object> values) throws SQLException {
        List<String> columns=module.getFields().stream().map(Catalog.Field::getName).toList();
        List<Object> params=new ArrayList<>();for(String col:columns)params.add(values.get(col));
        if(id>0) {
            params.add(id);params.add(userId);
            int changed=Database.update("UPDATE "+module.getKey()+" SET "+String.join(",",columns.stream().map(c->c+"=?").toList())+" WHERE id=? AND user_id=?",params.toArray());
            if(changed!=1)throw new IllegalArgumentException("Data tidak ditemukan.");return id;
        }
        params.add(userId);
        return Database.insert("INSERT INTO "+module.getKey()+" ("+String.join(",",columns)+",user_id) VALUES ("+String.join(",",Collections.nCopies(params.size(),"?"))+")",params.toArray());
    }
    public static boolean delete(Catalog.Module module,long userId,long id) throws SQLException {return Database.update("DELETE FROM "+module.getKey()+" WHERE id=? AND user_id=?",id,userId)==1;}
    public static String sqlMessage(SQLException e){
        String state=e.getSQLState()==null?"":e.getSQLState();
        if(state.equals("23505"))return "Data sudah ada. Nama harus unik; satu kategori hanya boleh punya satu anggaran per bulan.";
        if(state.startsWith("23"))return "Data masih digunakan atau melanggar aturan relasi. Hapus transaksi/anggaran terkait terlebih dahulu.";
        return "Data belum tersimpan. Coba lagi sebentar atau periksa koneksi database.";
    }
}
