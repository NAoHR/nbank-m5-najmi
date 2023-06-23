package id.co.indivara.jdt12.najmi.nbank.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OccuredError<T> {
    private String name;
    private T detail;
}
