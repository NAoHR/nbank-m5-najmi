package id.co.indivara.jdt12.najmi.nbank.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebResponse<T, E> {
    private String message;
    private LocalDateTime timestamp;
    private T data;
    private OccuredError<E> error;
}
