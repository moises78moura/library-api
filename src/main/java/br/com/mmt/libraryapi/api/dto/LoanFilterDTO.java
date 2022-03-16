package br.com.mmt.libraryapi.api.dto;

import lombok.*;
import org.springframework.stereotype.Service;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LoanFilterDTO {

    private String isbn;
    private String customer;

}
