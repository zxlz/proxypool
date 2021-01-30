package run;

import org.apache.http.util.Args;
import spiderpak.spider.Control;

public class startspider {
    public static void main(String args[]) throws Exception{

        Control control=new Control();
        String proxySpiderConfig="{'name':'proxy', 'start' : {'pageService': 'service.site1service','parse': 'parsemodel.site1parse'}}";



        Thread  pThread= control.CreateTh(proxySpiderConfig);

    }
}
