package id.kasku;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

class FinanceTest {
    long owner,other;
    @BeforeEach void setup() throws Exception {
        Database.useTestDatabase("jdbc:h2:mem:test"+UUID.randomUUID()+";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        owner=Database.insert("INSERT INTO users(name,email,password_hash) VALUES(?,?,?)","Owner","owner@unit.test","hash");
        other=Database.insert("INSERT INTO users(name,email,password_hash) VALUES(?,?,?)","Other","other@unit.test","hash");
    }
    long save(String key,long user,Map<String,String> input) throws Exception {
        Map<String,String> errors=new HashMap<>();var m=Catalog.get(key);var values=Records.validate(m,user,0,input,errors);assertTrue(errors.isEmpty(),errors.toString());return Records.save(m,user,0,values);
    }
    @Test void allEightModulesCreateReadUpdateDelete() throws Exception {
        long wallet=save("wallets",owner,Map.of("name","Cash","type","cash","opening_balance","100000","description","Test"));
        long category=save("categories",owner,Map.of("name","Food","kind","expense","color","orange"));
        Map<String,Map<String,String>> records=new LinkedHashMap<>();
        records.put("transactions",Map.of("title","Lunch","wallet_id",""+wallet,"category_id",""+category,"amount","25000","transaction_date","2026-11-01","note",""));
        records.put("budgets",Map.of("category_id",""+category,"budget_month","2026-11","amount_limit","300000"));
        records.put("goals",Map.of("name","Laptop","target_amount","10000000","saved_amount","1000000","deadline","2027-01-01","icon","laptop"));
        records.put("bills",Map.of("name","Internet","amount","100000","due_date","2026-12-01","status","unpaid","note",""));
        records.put("debts",Map.of("person","Test","direction","payable","amount","100000","paid_amount","20000","due_date","2026-12-01","note",""));
        records.put("notes",Map.of("title","Planning","content","Save monthly","label","planning"));
        for(var item:records.entrySet()) {
            var m=Catalog.get(item.getKey());long id=save(item.getKey(),owner,item.getValue());assertNotNull(Records.find(m,owner,id));
            Map<String,String> input=new HashMap<>(item.getValue());String field=m.getFields().get(0).getName();
            if(field.equals("category_id"))input.put("amount_limit","400000");else input.put(field,item.getValue().get(field)+" updated");
            Map<String,String> errors=new HashMap<>();var vals=Records.validate(m,owner,id,input,errors);assertTrue(errors.isEmpty());Records.save(m,owner,id,vals);assertEquals(vals.get(field),Records.find(m,owner,id).get(field));
            assertNull(Records.find(m,other,id));assertFalse(Records.delete(m,other,id));assertTrue(Records.delete(m,owner,id));assertNull(Records.find(m,owner,id));
        }
        for(String key:List.of("wallets","categories")) {
            var m=Catalog.get(key);long id=key.equals("wallets")?wallet:category;var original=Records.find(m,owner,id);Map<String,Object> updated=new HashMap<>(original);updated.put("name","Updated "+key);Records.save(m,owner,id,updated);assertEquals("Updated "+key,Records.find(m,owner,id).get("name"));assertTrue(Records.delete(m,owner,id));
        }
    }
    @Test void balanceChangesOnTransactionUpdateAndDelete() throws Exception {
        long w=save("wallets",owner,Map.of("name","Cash","type","cash","opening_balance","100000"));
        long cat=save("categories",owner,Map.of("name","Food","kind","expense","color","green"));
        long tx=save("transactions",owner,Map.of("title","Lunch","wallet_id",""+w,"category_id",""+cat,"amount","25000.50","transaction_date","2026-11-01"));
        assertEquals(new BigDecimal("74999.50"),new Finance(owner).data.get("wallets").get(0).get("balance"));
        var record=Records.find(Catalog.get("transactions"),owner,tx);record.put("amount",new BigDecimal("30000.25"));Records.save(Catalog.get("transactions"),owner,tx,record);
        assertEquals(new BigDecimal("69999.75"),new Finance(owner).data.get("wallets").get(0).get("balance"));
        assertThrows(java.sql.SQLException.class,()->Records.delete(Catalog.get("wallets"),owner,w));
        Records.delete(Catalog.get("transactions"),owner,tx);
        assertEquals(new BigDecimal("100000.00"),new Finance(owner).data.get("wallets").get(0).get("balance"));
    }
    @Test void crossAccountForeignKeysAreRejected() throws Exception {
        long w=save("wallets",other,Map.of("name","Private","type","bank","opening_balance","0"));
        long cat=save("categories",owner,Map.of("name","Food","kind","expense","color","green"));
        var errors=new HashMap<String,String>();Records.validate(Catalog.get("transactions"),owner,0,Map.of("title","Bad","wallet_id",""+w,"category_id",""+cat,"amount","10","transaction_date","2026-11-01"),errors);
        assertTrue(errors.containsKey("wallet_id"));
        assertThrows(java.sql.SQLException.class,()->Database.insert("INSERT INTO transactions(user_id,wallet_id,category_id,title,amount,transaction_date) VALUES(?,?,?,?,?,?)",owner,w,cat,"Bad",10,java.sql.Date.valueOf("2026-11-01")));
    }
    @Test void validationRejectsNegativeAndOverpaidValues() throws Exception {
        var errors=new HashMap<String,String>();Records.validate(Catalog.get("goals"),owner,0,Map.of("name","Goal","target_amount","100","saved_amount","101","deadline","2027-01-01","icon","home"),errors);assertTrue(errors.containsKey("saved_amount"));
        errors.clear();Records.validate(Catalog.get("wallets"),owner,0,Map.of("name","X","type","cash","opening_balance","-1"),errors);assertTrue(errors.containsKey("opening_balance"));
        errors.clear();Records.validate(Catalog.get("debts"),owner,0,Map.of("person","X","direction","payable","amount","100","paid_amount","120","due_date","2027-01-01"),errors);assertTrue(errors.containsKey("paid_amount"));
    }
    @Test void budgetsCountOnlyTheirMonthAndCategory() throws Exception {
        long w=save("wallets",owner,Map.of("name","Cash","type","cash","opening_balance","100000"));long cat=save("categories",owner,Map.of("name","Food","kind","expense","color","green"));
        save("budgets",owner,Map.of("category_id",""+cat,"budget_month","2026-11","amount_limit","50000"));
        for(String date:List.of("2026-11-03","2026-10-30"))save("transactions",owner,Map.of("title","Lunch","wallet_id",""+w,"category_id",""+cat,"amount","10000","transaction_date",date));
        assertEquals(new BigDecimal("10000.00"),new Finance(owner).data.get("budgets").get(0).get("used"));
        var errors=new HashMap<String,String>();Records.validate(Catalog.get("categories"),owner,cat,Map.of("name","Food","kind","income","color","green"),errors);assertTrue(errors.containsKey("kind"));
    }
    @Test void passwordAndCsvSafety() {
        String a=Passwords.hash("secure-password"),b=Passwords.hash("secure-password");assertNotEquals(a,b);assertTrue(Passwords.verify("secure-password",a));assertFalse(Passwords.verify("incorrect",a));
        assertEquals("\"'=1+1\"",AppServlet.csv("=1+1"));assertEquals("\"hello,world\"",AppServlet.csv("hello,world"));assertEquals("\"quote\"\"test\"",AppServlet.csv("quote\"test"));
    }
    @Test void reportsRespectInclusiveDatesAndEmptyAccounts() throws Exception {
        var empty=new Finance(owner).summary(LocalDate.of(2026,11,1),LocalDate.of(2026,11,30));assertEquals(0,empty.get("count"));assertEquals("Rp 0",empty.get("balance"));
        long demo=DemoData.create();var report=new Finance(demo).summary(LocalDate.of(1900,1,1),LocalDate.of(2100,1,1));assertEquals(52,report.get("count"));
    }
}
