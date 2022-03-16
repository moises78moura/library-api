package br.com.mmt.libraryapi.model.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn
    private Book book;

    @Column
    private String customer;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "loan_date")
    private LocalDate loanDate;

    @Column
    private Boolean returned;

}
