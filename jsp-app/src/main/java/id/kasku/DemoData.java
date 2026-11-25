package id.kasku;

import java.sql.*;
import java.time.*;
import java.util.*;

public final class DemoData {
    private DemoData() {}
    public static long create() throws SQLException {
        try(Connection c=Database.connection()) {
            c.setAutoCommit(false);
            try {
                long user=Database.insert(c,"INSERT INTO users(name,email,password_hash,is_demo) VALUES(?,?,?,?)","Muhammad Elmiro Rayhan","demo-"+UUID.randomUUID()+"@kasku.local",Passwords.hash(UUID.randomUUID().toString()),true);
                seed(c,user);c.commit();return user;
            }catch(Exception e){c.rollback();throw e;}
        }
    }
    private static void seed(Connection c,long user) throws SQLException {
        long bank=Database.insert(c,"INSERT INTO wallets(user_id,name,type,opening_balance,description) VALUES(?,?,?,?,?)",user,"Tabungan utama","bank",4500000,"Rekening untuk kebutuhan dan rencana besar");
        long cash=Database.insert(c,"INSERT INTO wallets(user_id,name,type,opening_balance,description) VALUES(?,?,?,?,?)",user,"Dompet harian","cash",450000,"Uang tunai sehari-hari");
        long ewallet=Database.insert(c,"INSERT INTO wallets(user_id,name,type,opening_balance,description) VALUES(?,?,?,?,?)",user,"GoPay","ewallet",350000,"Praktis untuk transportasi dan makan");
        Map<String,Long> cats=new LinkedHashMap<>();
        for(String[] item:new String[][]{{"Uang saku","income","green"},{"Freelance","income","blue"},{"Makan & minum","expense","orange"},{"Transportasi","expense","blue"},{"Belanja","expense","pink"},{"Pendidikan","expense","green"},{"Hiburan","expense","pink"},{"Tagihan","expense","orange"}}) {
            cats.put(item[0],Database.insert(c,"INSERT INTO categories(user_id,name,kind,color) VALUES(?,?,?,?)",user,item[0],item[1],item[2]));
        }
        LocalDate today=LocalDate.now(ZoneId.of("Asia/Jakarta"));YearMonth month=YearMonth.from(today);
        for(int offset=5;offset>=0;offset--) {
            YearMonth m=month.minusMonths(offset);
            tx(c,user,bank,cats.get("Uang saku"),"Uang saku bulanan",2500000,m.atDay(1));
            tx(c,user,bank,cats.get("Freelance"),"Proyek desain website",850000+(5-offset)*75000,m.atDay(Math.min(5,today.getDayOfMonth())));
            tx(c,user,bank,cats.get("Makan & minum"),"Belanja kebutuhan dapur",360000+offset*20000,m.atDay(2));
            tx(c,user,bank,cats.get("Tagihan"),"Internet & listrik",425000,m.atDay(3));
            tx(c,user,bank,cats.get("Belanja"),"Kebutuhan bulanan",330000+offset*15000,m.atDay(4));
            tx(c,user,bank,cats.get("Pendidikan"),"Kursus pemrograman",175000,m.atDay(6));
            tx(c,user,bank,cats.get("Transportasi"),"Transportasi kampus",210000,m.atDay(7));
            tx(c,user,bank,cats.get("Hiburan"),"Langganan & rekreasi",95000,m.atDay(8));
        }
        tx(c,user,cash,cats.get("Makan & minum"),"Makan siang di kantin",28000,today);
        tx(c,user,ewallet,cats.get("Transportasi"),"Perjalanan ke kampus",18000,today);
        tx(c,user,cash,cats.get("Makan & minum"),"Kopi setelah kelas",24000,today.minusDays(1));
        tx(c,user,bank,cats.get("Belanja"),"Buku catatan & alat tulis",65000,today.minusDays(2));
        for(Object[] b:new Object[][]{{"Makan & minum",800000},{"Transportasi",300000},{"Belanja",500000},{"Hiburan",200000}})
            Database.insert(c,"INSERT INTO budgets(user_id,category_id,budget_month,amount_limit) VALUES(?,?,?,?)",user,cats.get(b[0]),month.toString(),b[1]);
        Database.insert(c,"INSERT INTO goals(user_id,name,target_amount,saved_amount,deadline,icon) VALUES(?,?,?,?,?,?)",user,"Laptop impian",15000000,8250000,java.sql.Date.valueOf(today.plusMonths(8)),"laptop");
        Database.insert(c,"INSERT INTO goals(user_id,name,target_amount,saved_amount,deadline,icon) VALUES(?,?,?,?,?,?)",user,"Liburan ke Bali",4000000,2400000,java.sql.Date.valueOf(today.plusMonths(4)),"plane");
        Database.insert(c,"INSERT INTO goals(user_id,name,target_amount,saved_amount,deadline,icon) VALUES(?,?,?,?,?,?)",user,"Dana darurat",10000000,3200000,java.sql.Date.valueOf(today.plusMonths(12)),"home");
        Database.insert(c,"INSERT INTO bills(user_id,name,amount,due_date,status,note) VALUES(?,?,?,?,?,?)",user,"Internet kos",175000,java.sql.Date.valueOf(today.plusDays(3)),"unpaid","Bayar sebelum tanggal jatuh tempo");
        Database.insert(c,"INSERT INTO bills(user_id,name,amount,due_date,status,note) VALUES(?,?,?,?,?,?)",user,"Spotify Student",30000,java.sql.Date.valueOf(today.plusDays(6)),"unpaid","Langganan bulanan");
        Database.insert(c,"INSERT INTO bills(user_id,name,amount,due_date,status,note) VALUES(?,?,?,?,?,?)",user,"Listrik kos",250000,java.sql.Date.valueOf(today.minusDays(4)),"paid","Sudah dibayar bulan ini");
        Database.insert(c,"INSERT INTO debts(user_id,person,direction,amount,paid_amount,due_date,note) VALUES(?,?,?,?,?,?,?)",user,"Rizky","receivable",250000,100000,java.sql.Date.valueOf(today.plusDays(10)),"Patungan kegiatan kampus");
        Database.insert(c,"INSERT INTO debts(user_id,person,direction,amount,paid_amount,due_date,note) VALUES(?,?,?,?,?,?,?)",user,"Andi","payable",150000,0,java.sql.Date.valueOf(today.plusDays(5)),"Titip pembelian buku");
        Database.insert(c,"INSERT INTO notes(user_id,title,content,label) VALUES(?,?,?,?)",user,"Rencana keuangan semester ini","Sisihkan sebagian pemasukan freelance untuk laptop. Evaluasi anggaran makan setiap akhir bulan dan catat semua pengeluaran kecil.","planning");
        Database.insert(c,"INSERT INTO notes(user_id,title,content,label) VALUES(?,?,?,?)",user,"Cek langganan bulanan","Tinjau kembali langganan yang jarang dipakai sebelum diperpanjang.","reminder");
        Database.insert(c,"INSERT INTO notes(user_id,title,content,label) VALUES(?,?,?,?)",user,"Ide pemasukan tambahan","Buka jasa pembuatan landing page untuk UMKM sekitar kampus.","idea");
    }
    private static void tx(Connection c,long u,long w,long cat,String title,int amount,LocalDate date) throws SQLException {
        LocalDate today=LocalDate.now(ZoneId.of("Asia/Jakarta"));if(date.isAfter(today))date=today;
        Database.insert(c,"INSERT INTO transactions(user_id,wallet_id,category_id,title,amount,transaction_date,note) VALUES(?,?,?,?,?,?,?)",u,w,cat,title,amount,java.sql.Date.valueOf(date),"Data contoh untuk demonstrasi");
    }
}
