package br.com.mmt.libraryapi.service.impl;

import br.com.mmt.libraryapi.model.entity.Loan;
import br.com.mmt.libraryapi.service.EmailService;
import br.com.mmt.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";

    private final LoanService loanService;
    private final EmailService emailService;
    @Value("${application.mail.lateloans.message}")
    private String mensagem;

    @Scheduled(cron = CRON_LATE_LOANS)
    public void sendEmailToLateLoans(){
        List<Loan> allLateLoans = loanService.getAllLateLoans();
        List<String> mailList = allLateLoans.stream().map(
                loan -> loan.getCustomerEmail()
        ).collect(Collectors.toList());
        emailService.sendMails(mensagem, mailList);
    }

}
