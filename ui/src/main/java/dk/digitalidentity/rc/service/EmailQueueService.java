package dk.digitalidentity.rc.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dk.digitalidentity.rc.controller.mvc.viewmodel.InlineImageDTO;
import dk.digitalidentity.rc.dao.EmailQueueDao;
import dk.digitalidentity.rc.dao.model.AttachmentFile;
import dk.digitalidentity.rc.dao.model.EmailQueue;
import dk.digitalidentity.rc.dao.model.EmailTemplate;
import lombok.extern.log4j.Log4j;

@Log4j
@Service
public class EmailQueueService {
	
	@Autowired
	private EmailQueueDao emailQueueDao;
	
	@Autowired
	private EmailService emailService;
	
	public void queueEmail(String email, String title, String message, EmailTemplate template, List<AttachmentFile> attachments) {
		if (StringUtils.isEmpty(email)) {
			log.info("Not sending email '" + title + " because no recipient email supplied");
			return;
		}
		
		EmailQueue mail = new EmailQueue();
		mail.setEmail(email);
		mail.setMessage(message);
		mail.setTitle(title);
		mail.setDeliveryTts(new Date());
		mail.setEmailTemplate(template);
		if (attachments != null) {
			mail.addAllAttachments(attachments);
		}
		
		emailQueueDao.save(mail);
	}

	@Transactional
	public void sendPending() {
		List<EmailQueue> emails = findPending();
		
		for (EmailQueue email : emails) {
			EmailTemplate template = email.getEmailTemplate();
			
			email.forceLoadAttachments();
			List<AttachmentFile> attachments = (template != null && email.getAttachments() != null && email.getAttachments().size() > 0) ? email.getAttachments() : null;
			
			if (!StringUtils.isEmpty(email.getEmail())) {
				if (template != null) {
					List<InlineImageDTO> inlineImages = transformImages(email);

					if (attachments != null) {
						emailService.sendMessageWithAttachments(email.getEmail(), email.getTitle(), email.getMessage(), attachments, inlineImages);
					}
					else {
						emailService.sendMessage(email.getEmail(), email.getTitle(), email.getMessage(), inlineImages);
					}
				}
				else {
					if (attachments != null) {
						emailService.sendMessageWithAttachments(email.getEmail(), email.getTitle(), email.getMessage(), attachments);
					}
					else {
						emailService.sendMessage(email.getEmail(), email.getTitle(), email.getMessage());
					}
				}
			}
			else {
				log.warn("Cannot send message with title '" + email.getTitle() + "' due to no email");
			}
			
			emailQueueDao.delete(email);
		}
	}
	
	private List<EmailQueue> findPending() {
		Date tts = new Date();
		
		return emailQueueDao.findByDeliveryTtsBefore(tts);
	}
	
	public List<InlineImageDTO> transformImages(EmailQueue email) {
		List<InlineImageDTO> inlineImages = new ArrayList<>();
		String message = email.getMessage();
		Document doc = Jsoup.parse(message);

		int counter = 1;
		for (Element img : doc.select("img")) {
			String src = img.attr("src");
			if (src == null || src == "") {
				continue;
			}

			String filename = img.attr("data-filename");
			if (filename == null || filename == "") {
				filename = "file" + counter;
				counter ++;
			}

			InlineImageDTO inlineImageDto = new InlineImageDTO();
			inlineImageDto.setBase64(src.contains("base64"));
			if (inlineImageDto.isBase64()) {
				inlineImageDto.setUrl(false);
			}
			else {
				inlineImageDto.setUrl(true);
			}
			
			inlineImageDto.setCid(filename);
			inlineImageDto.setSrc(src);
			inlineImages.add(inlineImageDto);

			img.attr("src", "cid:" + filename);
		}
		
		email.setMessage(doc.html());

		return inlineImages;
	}
}