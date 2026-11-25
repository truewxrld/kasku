package id.kasku;

import java.util.*;

/** Fixed module/column allowlist. User input never becomes an SQL identifier. */
public final class Catalog {
    private Catalog() {}
    public static class Field {
        private final String name,label,type; private final boolean required; private final int max; private final Map<String,String> options;
        public Field(String name,String label,String type,boolean required,int max,String... pairs) {
            this.name=name;this.label=label;this.type=type;this.required=required;this.max=max;options=new LinkedHashMap<>();
            for(int i=0;i<pairs.length;i+=2) options.put(pairs[i],pairs[i+1]);
        }
        public String getName(){return name;} public String getLabel(){return label;} public String getType(){return type;}
        public boolean isRequired(){return required;} public int getMax(){return max;} public Map<String,String> getOptions(){return options;}
    }
    public static class Module {
        private final String key,title,singular,description,icon,help; private final List<Field> fields;
        public Module(String key,String title,String singular,String description,String icon,String help,Field... fields){this.key=key;this.title=title;this.singular=singular;this.description=description;this.icon=icon;this.help=help;this.fields=List.of(fields);}
        public String getKey(){return key;} public String getTitle(){return title;} public String getSingular(){return singular;}
        public String getDescription(){return description;} public String getIcon(){return icon;} public String getHelp(){return help;} public List<Field> getFields(){return fields;}
    }
    static Field text(String n,String l,int max){return new Field(n,l,"text",true,max);}
    static Field money(String n,String l){return new Field(n,l,"money",true,0);}
    static Field date(String n,String l){return new Field(n,l,"date",true,0);}
    static Field select(String n,String l,String... options){return new Field(n,l,"select",true,0,options);}
    static Field area(String n,String l,int max,boolean required){return new Field(n,l,"textarea",required,max);}
    public static final List<Module> MODULES=List.of(
        new Module("transactions","Transaksi","transaksi","Setiap rupiah punya cerita. Catat semuanya di sini.","arrows","Saldo dompet dihitung dari saldo awal, pemasukan, dan pengeluaran. Jenis transaksi mengikuti kategorinya.",text("title","Nama transaksi",100),new Field("wallet_id","Dompet","reference",true,0),new Field("category_id","Kategori","reference",true,0),money("amount","Nominal (Rp)"),date("transaction_date","Tanggal transaksi"),area("note","Catatan",1000,false)),
        new Module("wallets","Dompet","dompet","Semua sumber danamu, dalam satu tempat.","wallet","Saldo saat ini dihitung otomatis. Dompet yang masih memiliki transaksi tidak dapat dihapus.",text("name","Nama dompet",80),select("type","Jenis dompet","bank","Rekening bank","ewallet","E-wallet","cash","Tunai"),money("opening_balance","Saldo awal (Rp)"),new Field("description","Deskripsi","text",false,255)),
        new Module("categories","Kategori","kategori","Beri setiap transaksi tempat yang tepat.","grid","Jenis kategori yang sudah dipakai transaksi atau anggaran tidak dapat diubah. Hapus data terkait sebelum menghapus kategori.",text("name","Nama kategori",80),select("kind","Jenis kategori","expense","Pengeluaran","income","Pemasukan"),select("color","Warna penanda","green","Hijau","orange","Oranye","blue","Biru","pink","Merah muda")),
        new Module("budgets","Anggaran","anggaran","Buat batas yang sehat untuk pengeluaranmu.","pie","Satu kategori pengeluaran hanya memiliki satu anggaran per bulan. Pemakaian dihitung dari transaksi pada bulan tersebut.",new Field("category_id","Kategori pengeluaran","reference",true,0),new Field("budget_month","Bulan anggaran","month",true,7),money("amount_limit","Batas anggaran (Rp)")),
        new Module("goals","Target tabungan","target","Rencana besar dimulai dari langkah kecil.","target","Dana terkumpul adalah catatan progres manual, bukan pemindahan dana dari dompet.",text("name","Nama target",100),money("target_amount","Target dana (Rp)"),money("saved_amount","Dana terkumpul (Rp)"),date("deadline","Tanggal target"),select("icon","Ikon target","target","Target","plane","Perjalanan","laptop","Laptop","home","Rumah")),
        new Module("bills","Tagihan","tagihan","Lebih tenang saat semua jatuh tempo tercatat.","receipt","Status lunas tidak otomatis mengubah saldo. Buat transaksi pengeluaran untuk pembayaran yang ingin masuk pembukuan.",text("name","Nama tagihan",100),money("amount","Nominal tagihan (Rp)"),date("due_date","Jatuh tempo"),select("status","Status","unpaid","Belum lunas","paid","Lunas"),area("note","Catatan",1000,false)),
        new Module("debts","Utang & piutang","utang atau piutang","Catat kewajiban dan hakmu dengan jelas.","people","Pelunasan dicatat manual dan tidak otomatis mengubah saldo dompet. Sisa dihitung dari nominal dikurangi jumlah terbayar.",text("person","Nama pihak",100),select("direction","Jenis","payable","Utang saya","receivable","Piutang saya"),money("amount","Nominal awal (Rp)"),money("paid_amount","Sudah dibayar (Rp)"),date("due_date","Jatuh tempo"),area("note","Catatan",1000,false)),
        new Module("notes","Catatan keuangan","catatan","Simpan ide dan rencana baik untuk uangmu.","note","Catatan membantu perencanaan. Isinya tidak mengubah perhitungan saldo atau laporan.",text("title","Judul catatan",100),area("content","Isi catatan",2000,true),select("label","Label","planning","Perencanaan","reminder","Pengingat","idea","Ide"))
    );
    public static Module get(String key){return MODULES.stream().filter(m->m.key.equals(key)).findFirst().orElse(null);}
}
