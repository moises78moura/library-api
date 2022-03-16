package br.com.mmt.libraryapi.service;

import java.util.List;

public interface EmailService {
    void sendMails(String mensagem, List<String> mailList);
}
