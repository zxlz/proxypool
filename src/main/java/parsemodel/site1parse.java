package parsemodel;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import spiderpak.parsemodel.BaseParse;
import spiderpak.utils.Log;
import tool.PingTool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class site1parse extends BaseParse {
    private volatile AtomicInteger async;
    private static final ExecutorService ex =  Executors.newCachedThreadPool();

    @Override
    public void init() throws Exception {
        async=new AtomicInteger();

    }
    @Override
    public void destroy() throws Exception {

    }

    public long parse(Object o) throws Exception {
        int coun=0;
        HtmlPage page=(HtmlPage)o;
        Document doc = Jsoup.parse(page.asXml());

        Elements trs= doc.select("div[class=table-responsive]>table>tbody>tr");
        if(trs==null || trs.size()==0)return 0;
        int repeatCount=0;
        for (int i=0;i<trs.size();i++){
            coun++;
            Element tr = trs.get(i);
            String ip=tr.child(0).text();
            int port;
            try {
                port= Integer.parseInt(tr.child(1).text());
            }catch (Exception e){
                continue;
            }

            String ipport=ip+":"+port;
            //连续5个重复的url，终止任务----
            if(task.getRepeatFilter().isExit(ipport)){
                Log.info("repeatCount "+ ++repeatCount);
//                if(++repeatCount>5){
//                    task.getTaskContext().put("state","over");
//                    break;
//                };
                continue;
            }
            task.getRepeatFilter().add(ipport);


            CompletableFuture.supplyAsync(()->{
                int a=async.incrementAndGet();
                //测试有效,10秒延迟
                Log.info("ping："+ipport+" 目前ping任务数："+a);
                return PingTool.pingUrl(ip,port,10,"baidu.com","http");
            },ex).thenAccept((delay)->{//在pinghttp线程继续
                String addr=tr.child(2).text();
                String isp=tr.child(3).text();
                String https=tr.child(4).text();
                String post=tr.child(5).text();
                String hidden=tr.child(6).text();
                String speed=tr.child(7).text();
                String create=tr.child(8).text();
                String check=tr.child(9).text();
                String content="{\"addr\":\""+addr+"\",\"isp\":\""+isp+"\"，\"https\":\""+https+"\"，\"post\":\""+post+"\"" +
                        "，\"hidden\":\""+hidden+"\"，\"speed\":\""+speed+"\"，\"create\":\""+create+"\"，\"check\":\""+check+"\"" +
                        "}";
                try {
                    task.getDataPipeline().OutDataPut(ipport+"@"+delay+"@"+content);
                } catch (InterruptedException e) {
                }
                async.decrementAndGet();
            });

//            int delay = PingTool.pingUrl(ip,port,10,"baidu.com","http");
//          if(delay<0){
//              continue;
//          };
            //重复处理结束-----


        }

        Elements nexts=doc.select("ul[class=pagination]>li");
        String pre=page.getUrl().getProtocol()+"://"+page.getUrl().getHost()+page.getUrl().getPath();

        for (int i=1;i<nexts.size();i++){
            Element next = nexts.get(i);
            //只取前百页
//            if("100".equals(next.child(0).text())){
//                break;
//            }
            String url=next.child(0).attr("href");
            task.getUrlManager().addUnvisitedUrl(pre+url);
        }

        return coun;
    }



}
