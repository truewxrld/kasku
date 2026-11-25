package id.kasku;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AppServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Locale ID=Locale.forLanguageTag("id-ID");
    private static LocalDate today(){return LocalDate.now(ZoneId.of("Asia/Jakarta"));}
    private String path(HttpServletRequest req){return req.getRequestURI().substring(req.getContextPath().length());}
    private void redirect(HttpServletRequest req,HttpServletResponse res,String target) throws IOException{res.sendRedirect(req.getContextPath()+target);}
    private void render(HttpServletRequest req,HttpServletResponse res,String view) throws ServletException,IOException {
        req.setAttribute("modules",Catalog.MODULES);req.setAttribute("today",today().toString());req.setAttribute("todayLabel",today().format(DateTimeFormatter.ofPattern("d MMMM yyyy",ID)));
        req.setAttribute("monthLabel",today().format(DateTimeFormatter.ofPattern("MMMM yyyy",ID)));
        req.setAttribute("demoEnabled",!"false".equals(System.getenv("DEMO_ENABLED")));
        req.setAttribute("pageView",view);res.setContentType("text/html;charset=UTF-8");
        req.getRequestDispatcher("/WEB-INF/views/"+(view.equals("auth")?"auth":"layout")+".jsp").forward(req,res);
    }
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        try {
            String path=path(req);
            if(path.equals("/health")){Database.query("SELECT 1");res.setContentType("text/plain;charset=UTF-8");res.getWriter().write("KasKu OK");return;}
            if(path.startsWith("/auth/")) {
                if(!Set.of("/auth/login","/auth/register").contains(path)){res.sendError(404);return;}
                if(req.getSession().getAttribute("userId")!=null){redirect(req,res,"/app/dashboard");return;}
                req.setAttribute("register",path.endsWith("register"));render(req,res,"auth");return;
            }
            long user=(Long)req.getSession().getAttribute("userId");
            if(path.equals("/app/dashboard")||path.equals("/app/reports")||path.equals("/app/export")) {
                LocalDate from=today().withDayOfMonth(1),to=today().withDayOfMonth(today().lengthOfMonth());
                try {if(req.getParameter("from")!=null)from=LocalDate.parse(req.getParameter("from"));if(req.getParameter("to")!=null)to=LocalDate.parse(req.getParameter("to"));if(from.isAfter(to))throw new IllegalArgumentException();}
                catch(RuntimeException e){req.setAttribute("error","Rentang tanggal tidak valid. Menampilkan bulan berjalan.");from=today().withDayOfMonth(1);to=today().withDayOfMonth(today().lengthOfMonth());}
                Finance f=new Finance(user);var summary=f.summary(from,to);
                req.setAttribute("summary",summary);req.setAttribute("from",from.toString());req.setAttribute("to",to.toString());
                if(path.endsWith("export")){export(res,summary);return;}
                String page=path.endsWith("reports")?"reports":"dashboard";req.setAttribute("active",page);req.setAttribute("pageTitle",page.equals("reports")?"Laporan keuangan":"Ringkasan keuangan");render(req,res,page);return;
            }
            String[] parts=path.split("/");Catalog.Module m=parts.length>2?Catalog.get(parts[2]):null;
            if(m==null||parts.length>4){res.sendError(404);return;}
            req.setAttribute("module",m);req.setAttribute("active",m.getKey());req.setAttribute("pageTitle",m.getTitle());
            Finance f=new Finance(user);String action=parts.length==4?parts[3]:"list";
            if(action.equals("new")||action.equals("edit")||action.equals("detail")) {
                Map<String,Object> row=new HashMap<>();
                if(!action.equals("new")) {
                    long id=parseId(req);row=f.data.get(m.getKey()).stream().filter(r->Finance.number(r.get("id"))==id).findFirst().orElse(null);
                    if(row==null){res.sendError(404);return;}
                } else {row.put("transaction_date",today().toString());row.put("budget_month",YearMonth.from(today()).toString());for(String name:List.of("opening_balance","saved_amount","paid_amount"))row.put(name,"0");}
                req.setAttribute("record",row);req.setAttribute("editing",action.equals("edit"));choices(req,m,f);render(req,res,action.equals("detail")?"detail":"form");return;
            }
            if(!action.equals("list")){res.sendError(404);return;}
            String q=Objects.toString(req.getParameter("q"),"").trim().toLowerCase(ID),kind=Objects.toString(req.getParameter("kind"),"");
            List<Map<String,Object>> rows=f.data.get(m.getKey()).stream().filter(r->(q.isEmpty()||r.values().toString().toLowerCase(ID).contains(q))&&(kind.isEmpty()||kind.equals(r.get("kind")))).toList();
            int total=rows.size(),pages=Math.max(1,(total+9)/10),page=1;try{page=Math.max(1,Math.min(pages,Integer.parseInt(req.getParameter("page"))));}catch(RuntimeException ignored){}
            req.setAttribute("records",rows.subList((page-1)*10,Math.min(page*10,total)));req.setAttribute("total",total);req.setAttribute("page",page);req.setAttribute("pages",pages);req.setAttribute("query",q);req.setAttribute("kind",kind);render(req,res,"list");
        } catch(NumberFormatException e){res.sendError(404);} catch(SQLException e){getServletContext().log("Database read failed",e);res.setStatus(503);req.setAttribute("error","Database belum dapat diakses. Coba muat ulang sebentar lagi.");render(req,res,"error");}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        try {
            String path=path(req);
            if(path.equals("/auth/logout")){req.getSession().invalidate();redirect(req,res,"/auth/login");return;}
            if(path.startsWith("/auth/")){authenticate(req,res,path);return;}
            long user=(Long)req.getSession().getAttribute("userId");String[] parts=path.split("/");Catalog.Module m=parts.length==4?Catalog.get(parts[2]):null;
            if(m==null){res.sendError(404);return;}
            long id=req.getParameter("id")==null||req.getParameter("id").isBlank()?0:parseId(req);
            if(id>0&&Records.find(m,user,id)==null){res.sendError(404);return;}
            if(parts[3].equals("delete")) {
                if(id==0){res.sendError(400);return;}
                try {Records.delete(m,user,id);req.getSession().setAttribute("flash","Data berhasil dihapus.");}catch(SQLException e){req.getSession().setAttribute("flashError",Records.sqlMessage(e));}
                redirect(req,res,"/app/"+m.getKey());return;
            }
            if(!parts[3].equals("save")){res.sendError(404);return;}
            Map<String,String> input=new LinkedHashMap<>(),errors=new LinkedHashMap<>();for(var field:m.getFields())input.put(field.getName(),Objects.toString(req.getParameter(field.getName()),""));
            var values=Records.validate(m,user,id,input,errors);
            if(errors.isEmpty()) {
                try {Records.save(m,user,id,values);req.getSession().setAttribute("flash",id==0?"Data baru berhasil ditambahkan.":"Perubahan berhasil disimpan.");redirect(req,res,"/app/"+m.getKey());return;}
                catch(SQLException e){req.setAttribute("error",Records.sqlMessage(e));}
            }
            res.setStatus(422);Map<String,Object> form=new HashMap<>(input);if(id>0)form.put("id",id);
            req.setAttribute("record",form);req.setAttribute("errors",errors);req.setAttribute("module",m);req.setAttribute("active",m.getKey());req.setAttribute("pageTitle",m.getTitle());req.setAttribute("editing",id>0);choices(req,m,new Finance(user));render(req,res,"form");
        }catch(NumberFormatException e){res.sendError(404);}catch(SQLException e){getServletContext().log("Database write failed",e);res.setStatus(503);req.setAttribute("error","Database belum dapat diakses. Silakan coba lagi.");render(req,res,"error");}
    }
    private static long parseId(HttpServletRequest req){long id=Long.parseLong(req.getParameter("id"));if(id<1)throw new NumberFormatException();return id;}
    private void choices(HttpServletRequest req,Catalog.Module m,Finance f){
        Map<String,List<Map<String,Object>>> choices=new HashMap<>();choices.put("wallet_id",f.data.get("wallets"));
        choices.put("category_id",f.data.get("categories").stream().filter(c->!m.getKey().equals("budgets")||c.get("kind").equals("expense")).toList());req.setAttribute("choices",choices);
    }
    private void authenticate(HttpServletRequest req,HttpServletResponse res,String path) throws SQLException,ServletException,IOException {
        if(!Set.of("/auth/demo","/auth/login","/auth/register").contains(path)){res.sendError(404);return;}
        if(req.getSession().getAttribute("userId")!=null){redirect(req,res,"/app/dashboard");return;}
        if(path.equals("/auth/demo")) {
            if("false".equals(System.getenv("DEMO_ENABLED"))){res.sendError(404);return;}
            long id=DemoData.create();signedIn(req,id,"Muhammad Elmiro Rayhan",true);redirect(req,res,"/app/dashboard");return;
        }
        boolean register=path.endsWith("register");String email=Objects.toString(req.getParameter("email"),"").strip().toLowerCase(Locale.ROOT),password=Objects.toString(req.getParameter("password"),""),name=Objects.toString(req.getParameter("name"),"").strip();
        String error=null;
        if(email.length()>160||!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")||password.length()<8||password.length()>128)error="Gunakan email yang valid dan kata sandi 8–128 karakter.";
        if(register&&(name.length()<2||name.length()>100))error="Nama harus 2–100 karakter.";
        if(error==null) {
            if(register) {
                try{long id=Database.insert("INSERT INTO users(name,email,password_hash) VALUES(?,?,?)",name,email,Passwords.hash(password));signedIn(req,id,name,false);redirect(req,res,"/app/dashboard");return;}
                catch(SQLException e){if("23505".equals(e.getSQLState()))error="Email sudah terdaftar. Silakan masuk.";else throw e;}
            }else {
                var users=Database.query("SELECT * FROM users WHERE email=? AND is_demo=FALSE",email);
                if(!users.isEmpty()&&Passwords.verify(password,users.get(0).get("password_hash").toString())){var u=users.get(0);signedIn(req,Finance.number(u.get("id")),u.get("name").toString(),false);redirect(req,res,"/app/dashboard");return;}
                error="Email atau kata sandi belum cocok.";
            }
        }
        res.setStatus(422);req.setAttribute("error",error);req.setAttribute("email",email);req.setAttribute("name",name);req.setAttribute("register",register);render(req,res,"auth");
    }
    private void signedIn(HttpServletRequest req,long id,String name,boolean demo){req.changeSessionId();req.getSession().setAttribute("userId",id);req.getSession().setAttribute("userName",name);req.getSession().setAttribute("shortName",name.contains("Elmiro")?"Elmiro":name.split(" ")[0]);req.getSession().setAttribute("isDemo",demo);}
    private void export(HttpServletResponse res,Map<String,Object> summary) throws IOException {
        res.setContentType("text/csv;charset=UTF-8");res.setHeader("Content-Disposition","attachment; filename=\"kasku-transaksi.csv\"");var out=res.getWriter();out.write('\uFEFF');out.println("Tanggal,Transaksi,Jenis,Kategori,Dompet,Nominal,Catatan");
        @SuppressWarnings("unchecked") var rows=(List<Map<String,Object>>)summary.get("transactions");
        for(var row:rows){List<String> cells=new ArrayList<>();for(String col:List.of("transaction_date","title","kind","category_name","wallet_name","amount","note"))cells.add(csv(row.get(col)));out.println(String.join(",",cells));}
    }
    public static String csv(Object raw){String value=Objects.toString(raw,"");if(value.matches("(?s)^[\\s]*[=+@-].*")||value.startsWith("\t")||value.startsWith("\r"))value="'"+value;return "\""+value.replace("\"","\"\"")+"\"";}
}
