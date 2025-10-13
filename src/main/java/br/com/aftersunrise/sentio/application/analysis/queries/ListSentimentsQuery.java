package br.com.aftersunrise.sentio.application.analysis.queries;

import br.com.aftersunrise.sentio.application.abstractions.interfaces.IQuery;
import br.com.aftersunrise.sentio.application.abstractions.models.PageSettings;
import br.com.aftersunrise.sentio.application.abstractions.models.PagedResult;
import br.com.aftersunrise.sentio.application.analysis.data.ListSentimentsResponse;
import br.com.aftersunrise.sentio.application.analysis.data.SentimentResponse;
import br.com.aftersunrise.sentio.application.analysis.queries.enums.SentimentOrderBy;
import br.com.aftersunrise.sentio.domain.models.analysis.enums.SentimentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListSentimentsQuery extends PageSettings<SentimentOrderBy>
        implements IQuery<PagedResult<SentimentResponse>> {
    private SentimentType sentimentType;
}
