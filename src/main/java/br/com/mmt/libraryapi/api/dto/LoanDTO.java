package br.com.mmt.libraryapi.api.dto;


import br.com.mmt.libraryapi.model.entity.Book;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class LoanDTO {

    private Long id;
    @NotEmpty
    private String isbn;
    @NotEmpty
    private String customer;
    @NotEmpty
    private String customerEmail;
    @NotEmpty
    private BookDTO book;

}
