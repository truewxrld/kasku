package id.kasku;

import java.math.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Finance {
    private static final Locale ID=Locale.forLanguageTag("id-ID");
    public final Map<String,List<Map<String,Object>>> data=new LinkedHashMap<>();
    private final Map<Long,Map<String,Object>> wallets=new HashMap<>(),categories=new HashMap<>();
    public Finance(long userId) throws SQLException {
        for(Catalog.Module m:Catalog.MODULES)data.put(m.getKey(),Records.list(m,userId));
        for(var r:data.get("wallets"))wallets.put(number(r.get("id")),r);
        for(var r:data.get("categories"))categories.put(number(r.get("id")),r);
        for(var r:data.get("transactions")) {
            var cat=categories.get(number(r.get("category_id")));var wallet=wallets.get(number(r.get("wallet_id")));
            r.put("kind",cat.get("kind"));r.put("category_name",cat.get("name"));r.put("wallet_name",wallet.get("name"));r.put("color",cat.get("color"));
        }
        for(Catalog.Module m:Catalog.MODULES)for(var r:data.get(m.getKey()))decorate(m,r);
    }
    public static long number(Object n){return ((Number)n).longValue();}
    public static BigDecimal decimal(Object n){return n==null?BigDecimal.ZERO:new BigDecimal(n.toString());}
    public static String rupiah(Object amount){NumberFormat nf=NumberFormat.getNumberInstance(ID);nf.setMaximumFractionDigits(2);return "Rp "+nf.format(decimal(amount));}
    public static String date(Object value){return LocalDate.parse(value.toString()).format(DateTimeFormatter.ofPattern("dd MMM yyyy",ID));}
    public static int progress(Object a,Object b){BigDecimal denominator=decimal(b);return denominator.signum()==0?0:decimal(a).multiply(BigDecimal.valueOf(100)).divide(denominator,0,RoundingMode.HALF_UP).intValue();}
    private void decorate(Catalog.Module m,Map<String,Object> r) {
        String key=m.getKey();String title=Objects.toString(r.getOrDefault("name",r.getOrDefault("title",r.getOrDefault("person",""))),"");
        r.put("_title",title);r.put("_icon",m.getIcon());r.put("_tone","green");r.put("_subtitle","");r.put("_status","");r.put("_amount","");
        List<Map<String,String>> cells=new ArrayList<>();
        switch(key) {
            case "wallets" -> {
                BigDecimal balance=decimal(r.get("opening_balance"));
                for(var t:data.get("transactions"))if(number(t.get("wallet_id"))==number(r.get("id")))balance=balance.add(decimal(t.get("amount")).multiply(BigDecimal.valueOf(t.get("kind").equals("income")?1:-1)));
                r.put("balance",balance);r.put("_amount",rupiah(balance));r.put("_subtitle",label("type",r.get("type")));
                cells.add(cell("Saldo awal",rupiah(r.get("opening_balance"))));cells.add(cell("Jenis",label("type",r.get("type"))));
            }
            case "transactions" -> {
                boolean income=r.get("kind").equals("income");r.put("_icon",income?"down":"up");r.put("_tone",income?"green":"orange");
                r.put("_amount",(income?"+ ":"− ")+rupiah(r.get("amount")));r.put("_subtitle",r.get("category_name"));r.put("_status",income?"Pemasukan":"Pengeluaran");
                cells.add(cell("Dompet",r.get("wallet_name")));cells.add(cell("Tanggal",date(r.get("transaction_date"))));
            }
            case "categories" -> {
                r.put("_tone",r.get("color"));r.put("_status",label("kind",r.get("kind")));r.put("_subtitle",label("kind",r.get("kind")));
                long count=data.get("transactions").stream().filter(t->number(t.get("category_id"))==number(r.get("id"))).count();
                cells.add(cell("Digunakan",count+" transaksi"));cells.add(cell("Warna",label("color",r.get("color"))));
            }
            case "budgets" -> {
                var cat=categories.get(number(r.get("category_id")));r.put("_title",cat.get("name"));r.put("_subtitle",YearMonth.parse(r.get("budget_month").toString()).format(DateTimeFormatter.ofPattern("MMMM yyyy",ID)));r.put("_tone",cat.get("color"));
                BigDecimal used=BigDecimal.ZERO;for(var t:data.get("transactions"))if(number(t.get("category_id"))==number(r.get("category_id"))&&t.get("transaction_date").toString().startsWith(r.get("budget_month").toString()))used=used.add(decimal(t.get("amount")));
                int p=progress(used,r.get("amount_limit"));r.put("used",used);r.put("_amount",rupiah(r.get("amount_limit")));r.put("_progress",p);r.put("_bar",Math.min(p,100));r.put("_status",p>100?"Melebihi batas":p>=80?"Hampir penuh":"Terkendali");
                cells.add(cell("Terpakai",rupiah(used)));cells.add(cell("Sisa",rupiah(decimal(r.get("amount_limit")).subtract(used))));
            }
            case "goals" -> {
                int p=progress(r.get("saved_amount"),r.get("target_amount"));r.put("_icon",r.get("icon"));r.put("_progress",p);r.put("_bar",Math.min(100,p));r.put("_amount",rupiah(r.get("saved_amount")));r.put("_subtitle","Target "+rupiah(r.get("target_amount")));r.put("_status",p>=100?"Tercapai":"Sedang ditabung");
                cells.add(cell("Batas waktu",date(r.get("deadline"))));cells.add(cell("Progres",p+"% terkumpul"));
            }
            case "bills" -> {
                r.put("_amount",rupiah(r.get("amount")));boolean overdue="unpaid".equals(r.get("status"))&&LocalDate.parse(r.get("due_date").toString()).isBefore(LocalDate.now(ZoneId.of("Asia/Jakarta")));
                r.put("_status",overdue?"Terlambat":label("status",r.get("status")));r.put("_tone","paid".equals(r.get("status"))?"green":"orange");r.put("_subtitle","Jatuh tempo "+date(r.get("due_date")));
                cells.add(cell("Jatuh tempo",date(r.get("due_date"))));cells.add(cell("Status",r.get("_status")));
            }
            case "debts" -> {
                BigDecimal remain=decimal(r.get("amount")).subtract(decimal(r.get("paid_amount")));r.put("_amount",rupiah(remain));r.put("_subtitle",label("direction",r.get("direction")));r.put("_tone","payable".equals(r.get("direction"))?"orange":"blue");r.put("_status",remain.signum()==0?"Lunas":"Belum lunas");
                cells.add(cell("Sudah dibayar",rupiah(r.get("paid_amount"))));cells.add(cell("Jatuh tempo",date(r.get("due_date"))));
            }
            case "notes" -> {r.put("_subtitle",r.get("content"));r.put("_status",label("label",r.get("label")));cells.add(cell("Label",r.get("_status")));cells.add(cell("Dibuat",date(r.get("created_at").toString().substring(0,10))));}
        }
        r.put("_cells",cells);
        List<Map<String,String>> details=new ArrayList<>();
        for(Catalog.Field f:m.getFields()) {
            Object value=r.get(f.getName());String displayed=Objects.toString(value,"");
            if(f.getType().equals("money"))displayed=rupiah(value);
            if(f.getType().equals("date"))displayed=date(value);
            if(f.getType().equals("select"))displayed=f.getOptions().getOrDefault(displayed,displayed);
            if(f.getName().equals("wallet_id"))displayed=Objects.toString(wallets.get(number(value)).get("name"));
            if(f.getName().equals("category_id"))displayed=Objects.toString(categories.get(number(value)).get("name"));
            details.add(cell(f.getLabel(),displayed.isBlank()?"—":displayed));
        }
        r.put("_details",details);
    }
    public static String label(String name,Object value){for(Catalog.Module m:Catalog.MODULES)for(Catalog.Field f:m.getFields())if(f.getName().equals(name)&&f.getOptions().containsKey(Objects.toString(value)))return f.getOptions().get(value.toString());return Objects.toString(value,"");}
    private static Map<String,String> cell(String label,Object value){return Map.of("label",label,"value",Objects.toString(value,""));}
    public Map<String,Object> summary(LocalDate from,LocalDate to) {
        Map<String,Object> view=new HashMap<>();BigDecimal income=BigDecimal.ZERO,expense=BigDecimal.ZERO,balance=BigDecimal.ZERO;
        List<Map<String,Object>> filtered=new ArrayList<>();Map<String,BigDecimal> byCategory=new LinkedHashMap<>();
        for(var w:data.get("wallets"))balance=balance.add(decimal(w.get("balance")));
        for(var t:data.get("transactions")) {
            LocalDate date=LocalDate.parse(t.get("transaction_date").toString());if(date.isBefore(from)||date.isAfter(to))continue;filtered.add(t);
            if("income".equals(t.get("kind")))income=income.add(decimal(t.get("amount")));
            else {expense=expense.add(decimal(t.get("amount")));byCategory.merge(t.get("category_name").toString(),decimal(t.get("amount")),BigDecimal::add);}
        }
        view.put("income",rupiah(income));view.put("expense",rupiah(expense));view.put("balance",rupiah(balance));view.put("net",rupiah(income.subtract(expense)));
        view.put("netPositive",income.compareTo(expense)>=0);view.put("savingsRate",income.signum()>0?progress(income.subtract(expense),income):0);
        view.put("count",filtered.size());view.put("recent",filtered.stream().limit(5).toList());view.put("transactions",filtered);
        List<Map<String,Object>> segments=new ArrayList<>();String[] colors={"#214f43","#e4ad70","#a4b7a4","#d8d1c3","#84a6b5","#b695ab"};
        var sorted=byCategory.entrySet().stream().sorted(Map.Entry.<String,BigDecimal>comparingByValue().reversed()).toList();
        double angle=0;List<String> gradient=new ArrayList<>();int i=0;
        for(var entry:sorted){double next=angle+entry.getValue().doubleValue()/expense.doubleValue()*100;String color=colors[i++%colors.length];segments.add(Map.of("name",entry.getKey(),"amount",rupiah(entry.getValue()),"percent",progress(entry.getValue(),expense),"color",color));gradient.add(String.format(Locale.ROOT,"%s %.3f%% %.3f%%",color,angle,next));angle=next;}
        view.put("segments",segments);view.put("gradient",gradient.isEmpty()?"#e8e8e2 0% 100%":String.join(",",gradient));
        List<Map<String,Object>> chart=new ArrayList<>();YearMonth end=YearMonth.from(to);BigDecimal max=BigDecimal.ONE;
        for(int j=5;j>=0;j--){YearMonth m=end.minusMonths(j);BigDecimal a=BigDecimal.ZERO,b=BigDecimal.ZERO;for(var t:data.get("transactions"))if(t.get("transaction_date").toString().startsWith(m.toString())){if(t.get("kind").equals("income"))a=a.add(decimal(t.get("amount")));else b=b.add(decimal(t.get("amount")));}chart.add(new HashMap<>(Map.of("label",m.format(DateTimeFormatter.ofPattern("MMM",ID)),"income",a,"expense",b)));max=max.max(a).max(b);}
        double scale=Math.ceil(max.doubleValue()/1000000)*1000000;List<String> ap=new ArrayList<>(),bp=new ArrayList<>();
        for(int j=0;j<6;j++){var point=chart.get(j);double x=35+j*112;double ay=180-decimal(point.get("income")).doubleValue()/scale*140;double by=180-decimal(point.get("expense")).doubleValue()/scale*140;ap.add(String.format(Locale.ROOT,"%.1f,%.1f",x,ay));bp.add(String.format(Locale.ROOT,"%.1f,%.1f",x,by));point.put("x",(int)x);point.put("ay",String.format(Locale.ROOT,"%.1f",ay));point.put("by",String.format(Locale.ROOT,"%.1f",by));}
        view.put("chart",chart);view.put("incomePoints",String.join(" ",ap));view.put("expensePoints",String.join(" ",bp));view.put("chartMax",((int)(scale/1000000))+" jt");view.put("chartMid",String.format(Locale.ROOT,"%.1f jt",scale/2000000));
        view.put("wallets",data.get("wallets"));view.put("goals",data.get("goals").stream().limit(2).toList());
        view.put("budgets",data.get("budgets").stream().filter(b->b.get("budget_month").equals(end.toString())).limit(4).toList());
        view.put("bills",data.get("bills").stream().filter(b->b.get("status").equals("unpaid")).sorted(Comparator.comparing(b->b.get("due_date").toString())).limit(3).toList());
        return view;
    }
}
