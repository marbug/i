package org.igov.service.controller;

import org.igov.io.sms.ManagerSMS_New;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.Api;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.igov.io.GeneralConfig;
import org.igov.io.web.HttpRequester;
import org.igov.util.JSON.JsonRestUtils;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Api(tags = { "SubjectMessageCommonController -- Сообщения субьектов" })
@RequestMapping(value = "/subject/message")
public class SubjectMessageCommonController {
    private static final Logger LOG = LoggerFactory.getLogger(SubjectMessageCommonController.class);

    @Autowired
    private ManagerSMS_New managerSMS;
    
    @Autowired
    GeneralConfig generalConfig;
    
    @Autowired
    private HttpRequester oHttpRequester;

    /**
     * Колбек для сервиса отправки СМС
     * 
     * @param soData_JSON
     */
    @RequestMapping(value = "/getCallbackSMS_PB", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "text/plain;charset=UTF-8")
    public @ResponseBody String callbackSMS(@RequestBody String soData_JSON) {
	LOG.debug("callback JSON={}", soData_JSON);
	String ret = managerSMS.saveCallbackSMS(soData_JSON);
	LOG.info("save callback JSON={}", ret);

	return "";
    }

    /**
     * Колбек для сервиса отправки СМС
     * 
     * @param soData_JSON
     */
    @RequestMapping(value = "/sentSms", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "text/plain;charset=UTF-8")
    public @ResponseBody String sentSms(@RequestParam(value = "number", required = false) 
            String number, @RequestParam(value = "message", required = false) String message) throws Exception{
	
        String resp = "Error: number is not equal mask";
        String URL = "https://api.life.com.ua./ip2sms/";
        
        byte[] utf8Message = message.getBytes("UTF-8");
        
        //if (number.startsWith("+38063")||number.startsWith("+38093"))
        //{
            String body = new StringBuilder("<message>")
                    .append("<service id='single' source='iGov'/>")
                    .append("<to>").append(number).append("</to>")
                    .append("<body content-type=\"text/plain\">").append(message).append("</body>")
                    .append("</message>").toString();
            
            //resp = oHttpRequester.postInside(URL, null, body, "text/xml; charset=utf-8", "trywWDjcF27368908", "Vf2k8ip1xvzgscqoo");
        //}
        
	return body;
    }
    
    

}
