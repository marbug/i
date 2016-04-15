package org.igov.service.business.action.task.listener.doc;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Level;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.task.Attachment;
import org.igov.io.GeneralConfig;
import org.igov.io.db.kv.temp.model.ByteArrayMultipartFile;
import org.igov.io.web.RestRequest;
import org.igov.service.business.action.task.core.AbstractModelTask;
import org.igov.service.business.action.task.systemtask.doc.util.UkrDocUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.activiti.engine.impl.util.json.JSONObject;

@Component("GetDocument_UkrDoc")
public class GetDocument_UkrDoc extends AbstractModelTask implements TaskListener {

    private final static Logger LOG = LoggerFactory.getLogger(GetDocument_UkrDoc.class);

    private Expression sID_Document;

    @Autowired
    GeneralConfig generalConfig;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Override
    public void notify(DelegateTask delegateTask) {
        DelegateExecution execution = delegateTask.getExecution();
        String sID_Document = getStringFromFieldExpression(this.sID_Document, execution);

        LOG.info("Parameters of the task sID_Document:{}", sID_Document);

        String sessionId = UkrDocUtil.getSessionId(generalConfig.getSID_login(), generalConfig.getSID_password(),
                generalConfig.sURL_AuthSID_PB() + "?lang=UA");

        String[] documentIDs = sID_Document.split(":");
        if (documentIDs.length > 1) {
            String url = String.format("/%s/%s/content", documentIDs[1], documentIDs[0]);

            LOG.info("Retrieved session ID:{} and created URL to request: {}", sessionId, url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "promin.privatbank.ua/EXCL " + sessionId);

            String resp = new RestRequest().get(generalConfig.getsUkrDocServerAddress() + url, MediaType.APPLICATION_JSON, StandardCharsets.UTF_8, String.class, headers);

            LOG.info("Ukrdoc response getDocument:" + resp);
            JSONObject respJson = new JSONObject(resp);
            Object content = respJson.get("content");

            if (content != null) {

                String name = (String) ((JSONObject) content).get("name");
                runtimeService.setVariable(execution.getProcessInstanceId(), "sHead_Document_UkrDoc", name);
                String text = (String) ((JSONObject) content).get("text");
                runtimeService.setVariable(execution.getProcessInstanceId(), "sBody_Document_UkrDoc", text);
                try {
                    LOG.info("class: " + ((JSONObject) ((JSONObject) content).get("extensions")).get("files").getClass());
                    JSONArray files = (JSONArray) ((JSONObject) ((JSONObject) content).get("extensions")).get("files");
                    //получение контента файла и прикрипление его в качестве атача к таске
                    if (files != null && files.length() > 0) {
                        StringBuilder anID_Attach_UkrDoc = new StringBuilder();
                        for (int i = 0; i < files.length(); i++) {
                            JSONObject file = (JSONObject) files.get(i);
                            String view_url = file.getString("view_url").replaceFirst("/docs", ""); //docs/2016/10300131/files/10300000/content?type=.jpg
                            String fileNameOrigin = file.getString("file"); //a10300000.jpg
                            String fileName = file.getString("name");

                            LOG.info("view_url:" + generalConfig.getsUkrDocServerAddress() + view_url + " fileName: " + fileName);
                            resp = new RestRequest().get(generalConfig.getsUkrDocServerAddress() + view_url, MediaType.APPLICATION_JSON,
                                    StandardCharsets.UTF_8, String.class, headers);
                            LOG.info("Ukrdoc response getContentFile:" + resp);
                            try {
                                ByteArrayMultipartFile oByteArrayMultipartFile
                                        = new ByteArrayMultipartFile(contentStringToByte(resp), fileName, fileNameOrigin, "application/octet-stream");
                                //Attachment attachment = createAttachment(oByteArrayMultipartFile, delegateTask, fileName);
                                //Attachment attachment = ((org.igov.service.conf.TaskServiceImpl) taskService)
                                //        .createAttachment("application/octet-stream", delegateTask.getId(), execution.getProcessInstanceId(),
                                //                fileNameOrigin, fileName, oByteArrayMultipartFile.getInputStream());
                                Attachment attachment = taskService.createAttachment(oByteArrayMultipartFile.getContentType() + ";" + oByteArrayMultipartFile.getExp(), 
                                        delegateTask.getId(), execution.getProcessInstanceId(), 
                                        fileNameOrigin, fileName, oByteArrayMultipartFile.getInputStream());
                          
                                if (attachment != null) {
                                    anID_Attach_UkrDoc.append(attachment.getId()).append(",");
                                    LOG.info("attachment: " + attachment.getId());
                                }
                            } catch (Exception ex) {
                                java.util.logging.Logger.getLogger(GetDocument_UkrDoc.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        if (anID_Attach_UkrDoc.length() > 0) {
                            runtimeService.setVariable(execution.getProcessInstanceId(), "anID_Attach_UkrDoc",
                                    anID_Attach_UkrDoc.deleteCharAt(anID_Attach_UkrDoc.length() - 1).toString());
                        }
                    }
                } catch (Exception ex) {
                    LOG.error("error getFiles: ", ex);
                }

            }
        }
        LOG.info("close task aythomaticaly: " + delegateTask.getId() + "...");
        taskService.complete(delegateTask.getId(), new HashMap());
        LOG.info("close task aythomaticaly: " + delegateTask.getId() + " ok!");
    }
}
