package service;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import spiderpak.service.BaseService;
import spiderpak.utils.Log;

import java.sql.SQLException;
import java.util.Set;


public class site1service extends BaseService {
    private static JedisPool jedisPool=null;
    private static void initCon() throws ClassNotFoundException, InterruptedException {

        int trycount=0;
        boolean ready=false;
        while(!ready) {
            try {
                GenericObjectPoolConfig config=new GenericObjectPoolConfig();
                config.setMaxWaitMillis(2000);
                jedisPool = new JedisPool(config,"192.168.0.3",6379,10000,"zxcvbnmmnbvcxz");

                ready=true;
            }catch (Exception e){
                //连接失败，阻塞重试
                Log.severe(("error: "+ ++trycount+""+e.getMessage()));
                Thread.sleep(10000);
            }
        }
    }

    public void init() throws Exception {

        initCon();

        preExisted();
        try {
            preUnViUrl();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public void destroy() throws SQLException {
        //ignore
    }

    public int addData(Object o)  {

        String res= (String) o;
        String[] kv=res.split("@",3);
        if(kv.length!=3)return 0;
        int delay=Integer.parseInt(kv[1]);
        Jedis jedis = jedisPool.getResource();
        jedis.select(1);
        jedis.hset("proxy_all",kv[0],kv[2]);
        //10000 最大10秒  10-delay
        if(delay<0){
            jedis.sadd("proxy_invalid",kv[0]);
        }else{
            jedis.zadd("proxy",10000-delay,kv[0]);
        }
        jedis.close();
        return 1;
    }


    private void preExisted() {
        //获取已存代理
        Jedis jedis = jedisPool.getResource();
        jedis.select(1);
        Set<String> set=jedis.hkeys("allproxy");
        jedis.close();
        for (String s:set) {
            task.getRepeatFilter().add(s);
        }
    }

    private void preUnViUrl() throws InterruptedException {
        String chinaproxy="https://ip.ihuan.me/address/5Lit5Zu9.html?page=b97827cc";

        task.getUrlManager().addUnvisitedUrl(chinaproxy);
    }

}
