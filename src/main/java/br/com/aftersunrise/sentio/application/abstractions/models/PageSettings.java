package br.com.aftersunrise.sentio.application.abstractions.models;

import br.com.aftersunrise.sentio.application.abstractions.models.enums.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageSettings<T extends Enum<T>> extends SimplePageSettings{
    private SortDirection sort = SortDirection.DESC;
    private T orderBy;
}
