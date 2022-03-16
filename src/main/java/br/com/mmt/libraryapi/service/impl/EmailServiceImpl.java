package br.com.mmt.libraryapi.service.impl;

import br.com.mmt.libraryapi.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${application.mail.default-rementent}")
    private String remetent;

    @Override
    public void sendMails(String mensagem, List<String> mailList) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        String[] emails = mailList.toArray(new String[mailList.size()]);
        mailMessage.setFrom(remetent);
        mailMessage.setSubject("Livro com emprestimo atrasado.");
        mailMessage.setText(mensagem);
        mailMessage.setTo(emails);

        javaMailSender.send(mailMessage);
    }
}
